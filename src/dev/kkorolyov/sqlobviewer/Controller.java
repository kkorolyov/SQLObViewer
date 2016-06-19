package dev.kkorolyov.sqlobviewer;

import static dev.kkorolyov.sqlobviewer.assets.Assets.*;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import dev.kkorolyov.simplelogs.Logger;
import dev.kkorolyov.sqlob.connection.DatabaseConnection;
import dev.kkorolyov.sqlob.connection.TableConnection;
import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlob.construct.Results;
import dev.kkorolyov.sqlob.construct.RowEntry;
import dev.kkorolyov.sqlobviewer.assets.Assets;
import dev.kkorolyov.sqlobviewer.gui.DatabaseTable;
import dev.kkorolyov.sqlobviewer.gui.LoginScreen;
import dev.kkorolyov.sqlobviewer.gui.MainWindow;
import dev.kkorolyov.sqlobviewer.gui.ViewScreen;
import dev.kkorolyov.sqlobviewer.gui.event.GuiListener;
import dev.kkorolyov.sqlobviewer.gui.event.GuiSubject;
import dev.kkorolyov.sqlobviewer.statement.UndoStatement;

/**
 * Centralized SQLObViewer application control.
 */
public class Controller implements GuiListener {
	private static final int MAX_UNDO_STATEMENTS = Integer.MAX_VALUE;
	private static final Logger log = Logger.getLogger(Controller.class.getName());
	
	private DatabaseConnection dbConn;	// Model
	private TableConnection tableConn;
	private Stack<UndoStatement> undoStatements;
	private MainWindow window;	// View
	private DatabaseTable databaseTable;
	
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
		LoginScreen loginScreen = new LoginScreen();
		
		window.setLoginScreen(loginScreen);
		window.showLoginScreen();
	}
	private void goToViewScreen() {
		ViewScreen viewScreen = new ViewScreen(dbConn.getTables(), databaseTable);		
		
		window.setViewScreen(viewScreen);
		window.showViewScreen();
	}
	
	@Override
	public void logInButtonPressed(String host, String database, String user, String password, GuiSubject context) {
		Assets.set(SAVED_HOST, host);
		Assets.set(SAVED_DATABASE, database);
		Assets.set(SAVED_USER, user);
		Assets.set(SAVED_PASSWORD, password);
		
		Assets.save();
		
		try {
			setDatabaseConnection(new DatabaseConnection(host, database, user, password));
		} catch (SQLException e) {
			window.displayError(e.getMessage());
			
			return;
		}
		String[] dbTables = dbConn.getTables();
		
		setTableConnection(dbTables.length > 0 ? dbConn.connect(dbTables[0]) : null);
		
		setDatabaseTable(new DatabaseTable(getTableColumns(), getTableData()));
		
		goToViewScreen();
	}
	@Override
	public void backButtonPressed(GuiSubject context) {
		if (context instanceof ViewScreen) {
			setDatabaseConnection(null);
			
			window.showLoginScreen();
		}
	}
	@Override
	public void refreshTableButtonPressed(GuiSubject context) {
		databaseTable.rebuild(getTableColumns(), getTableData());
	}
	@Override
	public void newTableButtonPressed(GuiSubject context) {
		window.showCreateTableScreen();
	}
	@Override
	public void undoStatementButtonPressed(GuiSubject context) {
		undoLastStatement();
	}
	
	@Override
	public void tableSelected(String table, GuiSubject context) {
		setTableConnection(dbConn.connect(table));
		
		databaseTable.rebuild(getTableColumns(), getTableData());
	}
	
	@Override
	public void insertRow(RowEntry[] rowValues, GuiSubject context) {
		try {
			tableConn.insert(rowValues);
		} catch (SQLException e) {
			window.displayError(e.getMessage());
		}
		databaseTable.rebuild(getTableColumns(), getTableData());
	}
	@Override
	public void updateRows(RowEntry[] newValues, RowEntry[] criteria, GuiSubject context) {
		try {
			if (tableConn.update(newValues, criteria) > 1)
				databaseTable.rebuild(getTableColumns(), getTableData());	// Rebuild table to match database
		} catch (SQLException e) {
			window.displayError(e.getMessage());
		}
	}
	@Override
	public void deleteRows(RowEntry[] criteria, GuiSubject context) {
		try {
			tableConn.delete(criteria);
		} catch (SQLException e) {
			window.displayError(e.getMessage());
		}
		databaseTable.rebuild(getTableColumns(), getTableData());
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
			log.debug("Number of undo statements (" + undoStatements.size() + ") has reach MAX_UNDO_STATEMENTS=" + MAX_UNDO_STATEMENTS + ", removing oldest undo statement");
			
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
	}
	
	private Column[] getTableColumns() {
		return tableConn != null ? tableConn.getColumns() : null;
	}
	private RowEntry[][] getTableData() {
		if (tableConn == null)
			return null;
		
		List<RowEntry[]> data = new LinkedList<>();

		try {
			Results allResults = tableConn.select(null);
			
			RowEntry[] currentRow;
			while ((currentRow = allResults.getNextRow()) != null)				
				data.add(currentRow);
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return data.toArray(new RowEntry[data.size()][]);
	}
	
	private void setDatabaseTable(DatabaseTable newDatabaseTable) {
		if (databaseTable != null)
			databaseTable.removeListener(this);
		
		databaseTable = newDatabaseTable;
		databaseTable.addListener(this);
	}
}
