package dev.kkorolyov.sqlobviewer;

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
	private ViewScreen viewScreen;
	
	/**
	 * Constructs a new controller for the specified window
	 * @param window application window
	 */
	public Controller(MainWindow window) {
		setWindow(window);
		
		String 	startHost = Assets.host(),
						startDatabase = Assets.database(),
						startUser = Assets.user(),
						startPassword = Assets.password();
		
		this.window.setLoginScreen(new LoginScreen(startHost, startDatabase, startUser, startPassword));
		this.window.showLoginScreen();
	}
	
	@Override
	public void logInButtonPressed(String host, String database, String user, String password, GuiSubject context) {
		Assets.setHost(host);
		Assets.setDatabase(database);
		Assets.setUser(user);
		Assets.setPassword(password);
		
		Assets.save();
		
		try {
			setDatabaseConnection(new DatabaseConnection(host, database, user, password));
		} catch (SQLException e) {
			window.displayError(e.getMessage());
			
			return;
		}
		String[] dbTables = dbConn.getTables();
		
		setTableConnection(dbTables.length > 0 ? dbConn.connect(dbTables[0]) : null);
		
		setViewScreen(new ViewScreen(dbTables, extractColumnNames(), extractData()));
		
		window.showViewScreen();
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
		viewScreen.setViewedTable(extractColumnNames(), extractData());
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
		
		viewScreen.setViewedTable(extractColumnNames(), extractData());
	}
	
	@Override
	public void insertRow(RowEntry[] rowValues, GuiSubject context) {
		try {
			tableConn.insert(rowValues);
		} catch (SQLException e) {
			window.displayError(e.getMessage());
		}
		viewScreen.setViewedTable(extractColumnNames(), extractData());
	}
	@Override
	public void updateRows(RowEntry[] newValues, RowEntry[] criteria, GuiSubject context) {
		try {
			if (tableConn.update(newValues, criteria) > 1)
				viewScreen.setViewedTable(extractColumnNames(), extractData());	// Reset table to match database
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
		viewScreen.setViewedTable(extractColumnNames(), extractData());
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
	
	private String[] extractColumnNames() {
		if (tableConn == null)
			return null;
		
		Column[] columns = tableConn.getColumns();
		String[] columnNames = new String[columns.length];
		
		for (int i = 0; i < columnNames.length; i++)
			columnNames[i] = columns[i].getName();
		
		return columnNames;
	}
	private Object[][] extractData() {
		if (tableConn == null)
			return null;
		
		List<Object[]> data = new LinkedList<>();

		try {
			Results allResults = tableConn.select(null);
			
			RowEntry[] currentRow;
			while ((currentRow = allResults.getNextRow()) != null) {
				Object[] currentRowData = new Object[currentRow.length];
				
				for (int i = 0; i < currentRowData.length; i++)
					currentRowData[i] = currentRow[i].getValue();
				
				data.add(currentRowData);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return data.toArray(new Object[data.size()][]);
	}
	
	/** @param newWindow new application window */
	public void setWindow(MainWindow newWindow) {
		window = newWindow;
		window.addListener(this);
	}
	/** @param newViewScreen new view screen */
	public void setViewScreen(ViewScreen newViewScreen) {
		viewScreen = newViewScreen;
		viewScreen.addListener(this);
		
		window.setViewScreen(viewScreen);
	}
}
