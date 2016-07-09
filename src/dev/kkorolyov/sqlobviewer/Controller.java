package dev.kkorolyov.sqlobviewer;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.SAVED_DATABASE;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.SAVED_HOST;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.SAVED_PASSWORD;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.SAVED_USER;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import dev.kkorolyov.simplelogs.Logger;
import dev.kkorolyov.simplelogs.Logger.Level;
import dev.kkorolyov.sqlob.connection.DatabaseConnection;
import dev.kkorolyov.sqlob.connection.PostgresDatabaseConnection;
import dev.kkorolyov.sqlob.connection.TableConnection;
import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlob.construct.Results;
import dev.kkorolyov.sqlob.construct.RowEntry;
import dev.kkorolyov.sqlobviewer.assets.Assets.Config;
import dev.kkorolyov.sqlobviewer.gui.*;
import dev.kkorolyov.sqlobviewer.gui.event.*;
import dev.kkorolyov.sqlobviewer.gui.table.SQLObTableModel;
import dev.kkorolyov.sqlobviewer.statement.UndoStatement;

/**
 * Centralized SQLObViewer application control.
 */
public class Controller implements GuiListener, SubmitListener, CancelListener, SqlRequestListener {
	private static final int MAX_UNDO_STATEMENTS = Integer.MAX_VALUE;
	private static final Logger log = Logger.getLogger(Controller.class.getName());
	
	private DatabaseConnection dbConn;	// Model
	private TableConnection tableConn;
	private SQLObTableModel tableModel;
	private Stack<UndoStatement> undoStatements;
	
	private MainWindow window;	// View
	private LoginScreen loginScreen;
	private MainScreen mainScreen;	// TODO Getters which initialize these
	
	/**
	 * Constructs a new controller for the specified window
	 * @param window application window
	 */
	public Controller(MainWindow window) {
		this.window = window;
		this.window.addListener(this);
		
		goToLoginScreen();
	}
	
	private void goToLoginScreen() {
		if (loginScreen != null)
			loginScreen.clearListeners();
		
		loginScreen = new LoginScreen();
		loginScreen.addSubmitListener(this);
		
		window.setScreen(loginScreen, true);
	}
	private void goToViewScreen() {
		if (mainScreen == null) {
			mainScreen = new MainScreen();		
			mainScreen.addListener(this);
			mainScreen.addCancelListener(this);
			mainScreen.addSqlRequestListener(this);
			mainScreen.setTables(dbConn.getTables());
			mainScreen.setTableModel(tableModel);
		}
		mainScreen.setTables(dbConn.getTables());
		mainScreen.setTableModel(tableModel);
		
		window.setScreen(mainScreen, false);
	}
	
	@Override
	public void submitted(SubmitSubject context) {
		if (context instanceof LoginScreen) {
			LoginScreen loginContext = (LoginScreen) context;
			String 	host = loginContext.getHost(),
							database = loginContext.getDatabase(),
							user = loginContext.getUser(),
							password = loginContext.getPassword();
			
			Config.set(SAVED_HOST, host);
			Config.set(SAVED_DATABASE, database);
			Config.set(SAVED_USER, user);
			Config.set(SAVED_PASSWORD, password);
	
			Config.save();
			try {
				setDatabaseConnection(new PostgresDatabaseConnection(host, database, user, password));
			} catch (SQLException e) {
				log.exception(e, Level.WARNING);
				window.displayException(e);
				
				return;
			}
			String[] dbTables = dbConn.getTables();
			
			setTableConnection(dbTables.length > 0 ? dbConn.connect(dbTables[0]) : null);
						
			goToViewScreen();
		}
		else if (context instanceof CreateTableScreen) {
			CreateTableScreen createTableContext = (CreateTableScreen) context;
			
			dbConn.createTable(createTableContext.getName(), createTableContext.getColumns());
			
			goToViewScreen();
		}
	}
	@Override
	public void canceled(CancelSubject context) {
		if (context instanceof MainScreen) {
			setDatabaseConnection(null);
			
			goToLoginScreen();
		}
		else if (context instanceof CreateTableScreen) {
			goToViewScreen();
		}
	}
	@Override
	public void submitButtonPressed(GuiSubject context) {
		
	}
	@Override
	public void backButtonPressed(GuiSubject context) {
		
	}
	@Override
	public void refreshTableButtonPressed(GuiSubject context) {
		updateTableModel();
	}
	@Override
	public void undoStatementButtonPressed(GuiSubject context) {
		undoLastStatement();
	}
	
	@Override
	public void tableSelected(String table, GuiSubject context) {
		setTableConnection(dbConn.connect(table));
				
		goToViewScreen();
	}
	
	@Override
	public void createTable(String table, Column[] columns, SqlRequestSubject source) {
		dbConn.createTable(table, columns);
	}
	@Override
	public void dropTable(String table, SqlRequestSubject source) {
		
	}
	@Override
	public void removeTable(String table, GuiSubject context) {
		dbConn.dropTable(table);
		
		if (tableConn.getTableName().equals(table))
			setTableConnection(null);
		
		mainScreen.setTables(dbConn.getTables());
		goToViewScreen();
	}
	
	@Override
	public void insertRow(RowEntry[] rowValues, SqlRequestSubject context) {
		String statement = String.valueOf(tableConn.insert(rowValues));
		updateTableModel();

		pushUndoStatement(new UndoStatement());
		
		mainScreen.setLastStatement(statement);
	}
	@Override
	public void updateRow(RowEntry[] newValues, RowEntry[] criteria, SqlRequestSubject context) {
		log.debug("Updating " + newValues.length + " row");
		
		int result = tableConn.update(newValues, criteria);
		if (result > 1)
			updateTableModel();
		
		String statement = String.valueOf(result);
		
		pushUndoStatement(new UndoStatement());
		
		mainScreen.setLastStatement(statement);
	}
	@Override
	public void deleteRow(RowEntry[] criteria, SqlRequestSubject context) {
		String statement = String.valueOf(tableConn.delete(criteria));
		updateTableModel();
		
		pushUndoStatement(new UndoStatement());
		
		mainScreen.setLastStatement(statement);
	}
	
	@Override
	public void closed(GuiSubject context) {
		setDatabaseConnection(null);
	}
	
	/**
	 * Pushes a new undo statement
	 * @param statement undo statement to push
	 */
	public void pushUndoStatement(UndoStatement statement) {
		if (undoStatements == null) {
			undoStatements = new Stack<>();
			
			log.debug("Created new undoStatements Stack");
		}
		if (undoStatements.size() >= MAX_UNDO_STATEMENTS) {
			log.debug("Number of undo statements (" + undoStatements.size() + ") has reached MAX_UNDO_STATEMENTS=" + MAX_UNDO_STATEMENTS + ", removing oldest undo statement");
			
			undoStatements.remove(0);
		}
		undoStatements.push(statement);
		
		log.debug("Pushed new undo statement = '" + statement + "'");
	}
	/**
	 * Undoes the last SQL statement.
	 */
	public void undoLastStatement() {
		if (undoStatements != null && undoStatements.size() > 0) {
			UndoStatement statement = undoStatements.pop();
			
			// TODO
		}
	}
	
	/** @param newDatabaseConnection new database connection */
	public void setDatabaseConnection(DatabaseConnection newDatabaseConnection) {
		if (dbConn != null) {
			dbConn.close();
			
			log.debug("Closed old dbConn: " + dbConn);
		}
		dbConn = newDatabaseConnection;
		
		log.debug("Set dbConn=" + dbConn);
	}
	/** @param newTableConnection new table connection */
	public void setTableConnection(TableConnection newTableConnection) {
		tableConn = newTableConnection;
		
		resetTableModel();
	}
	private void resetTableModel() {
		if (tableModel != null)
			tableModel.clearListeners();
		
		tableModel = new SQLObTableModel(getTableColumns(), getTableData(), true);
		tableModel.addSqlRequestListener(this);
	}
	private void updateTableModel() {
		tableModel.setData(getTableColumns(), getTableData());
	}
	
	private Column[] getTableColumns() {
		return tableConn != null ? tableConn.getColumns() : new Column[0];
	}
	private RowEntry[][] getTableData() {
		if (tableConn == null)
			return new RowEntry[0][0];
		
		List<RowEntry[]> data = new LinkedList<>();

		Results allResults = tableConn.select(null);
		
		RowEntry[] currentRow;
		while ((currentRow = allResults.getNextRow()) != null)				
			data.add(currentRow);
			
		return data.toArray(new RowEntry[data.size()][]);
	}
}
