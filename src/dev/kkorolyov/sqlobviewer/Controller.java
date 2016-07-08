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
import dev.kkorolyov.sqlobviewer.gui.event.GuiListener;
import dev.kkorolyov.sqlobviewer.gui.event.GuiSubject;
import dev.kkorolyov.sqlobviewer.gui.table.SQLObTableModel;
import dev.kkorolyov.sqlobviewer.gui.table.TableRequestListener;
import dev.kkorolyov.sqlobviewer.statement.UndoStatement;

/**
 * Centralized SQLObViewer application control.
 */
public class Controller implements GuiListener, TableRequestListener {
	private static final int MAX_UNDO_STATEMENTS = Integer.MAX_VALUE;
	private static final Logger log = Logger.getLogger(Controller.class.getName());
	
	private DatabaseConnection dbConn;	// Model
	private TableConnection tableConn;
	private SQLObTableModel tableModel;
	private Stack<UndoStatement> undoStatements;
	
	private MainWindow window;	// View
	private LoginScreen loginScreen;
	private ViewScreen viewScreen;
	private CreateTableScreen createTableScreen;
	
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
		loginScreen.addListener(this);
		
		window.showScreen(loginScreen, true);
	}
	private void goToViewScreen() {
		if (viewScreen == null) {
			viewScreen = new ViewScreen();		
			viewScreen.addListener(this);
			viewScreen.setTables(dbConn.getTables());
			viewScreen.setTableModel(tableModel);
			viewScreen.spawnTable();
		}
		viewScreen.setTables(dbConn.getTables());
		viewScreen.setTableModel(tableModel);
		
		window.showScreen(viewScreen, false);
	}
	private void goToCreateTableScreen() {
		if (createTableScreen != null)
			createTableScreen.clearListeners();
		
		createTableScreen = new CreateTableScreen();
		createTableScreen.addListener(this);
		
		window.showScreen(createTableScreen.getPanel(), true);
	}
	
	@Override
	public void submitButtonPressed(GuiSubject context) {
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
	public void backButtonPressed(GuiSubject context) {
		if (context instanceof ViewScreen) {
			setDatabaseConnection(null);
			
			goToLoginScreen();
		}
		else if (context instanceof CreateTableScreen) {
			goToViewScreen();
		}
	}
	@Override
	public void refreshTableButtonPressed(GuiSubject context) {
		updateTableModel();
	}
	@Override
	public void addTableButtonPressed(GuiSubject context) {
		goToCreateTableScreen();
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
	public void removeTable(String table, GuiSubject context) {
		dbConn.dropTable(table);
		
		if (tableConn.getTableName().equals(table))
			setTableConnection(null);
		
		viewScreen.setTables(dbConn.getTables());
		goToViewScreen();
	}
	
	@Override
	public void insertRow(RowEntry[] rowValues, SQLObTableModel context) {
		String statement = String.valueOf(tableConn.insert(rowValues));
		updateTableModel();

		pushUndoStatement(new UndoStatement());
		
		viewScreen.setLastStatement(statement);
	}
	@Override
	public void updateRow(RowEntry[] newValues, RowEntry[] criteria, SQLObTableModel context) {
		log.debug("Updating " + newValues.length + " row");
		
		int result = tableConn.update(newValues, criteria);
		updateTableModel();
		
		String statement = String.valueOf(result);
		
		pushUndoStatement(new UndoStatement());
		
		viewScreen.setLastStatement(statement);
	}
	@Override
	public void deleteRow(RowEntry[] criteria, SQLObTableModel context) {
		String statement = String.valueOf(tableConn.delete(criteria));
		updateTableModel();
		
		pushUndoStatement(new UndoStatement());
		
		viewScreen.setLastStatement(statement);
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
		
		tableModel = new SQLObTableModel(getTableColumns(), getTableData());
		tableModel.addRequestListener(this);
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
