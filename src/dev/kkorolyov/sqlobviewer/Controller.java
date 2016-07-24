package dev.kkorolyov.sqlobviewer;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

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

/**
 * Centralized SQLObViewer application control.
 */
public class Controller implements SubmitListener, CancelListener, OptionsListener, SqlRequestListener {
	private static final Logger log = Logger.getLogger(Controller.class.getName());
	
	private DatabaseConnection dbConn;	// Model
	private TableConnection tableConn;
	private SQLObTableModel tableModel;
	
	private MainWindow window;	// View
	
	/**
	 * Constructs a new controller for the specified window
	 * @param window application window
	 */
	public Controller(MainWindow window) {
		this.window = window;
		this.window.addWindowListener(new WindowAdapter() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void windowClosing(WindowEvent e) {
				log.debug("Received WINDOW CLOSING event from: " + e.getSource());
				setDatabaseConnection(null);
			}
		});
		goToLoginScreen();
	}
	
	/**
	 * Undoes the last SQL statement.
	 */
	public void undoLastStatement() {
		// TODO
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
	
	/** @param newDatabaseConnection new database connection */
	public void setDatabaseConnection(DatabaseConnection newDatabaseConnection) {
		if (dbConn != null)
			dbConn.close();
		tableConn = null;
		
		dbConn = newDatabaseConnection;
		log.debug("Set new database connection = " + dbConn);
		
		updateTableModel();
	}
	/** @param newTableConnection new table connection */
	public void setTableConnection(TableConnection newTableConnection) {
		tableConn = newTableConnection;
		log.debug("Set new table connection = " + tableConn);
		
		updateTableModel();
	}
	
	private void goToLoginScreen() {
		window.setScreen(buildLoginScreen(), true);
		log.debug("Swapped to login screen");
	}
	private LoginScreen buildLoginScreen() {
		LoginScreen loginScreen = new LoginScreen();
		loginScreen.addSubmitListener(this);
		loginScreen.addOptionsListener(this);
		log.debug("Built new login screen = " + loginScreen);

		return loginScreen;
	}
	
	private void goToOptionsScreen() {
		window.setScreen(buildOptionsScreen(), true);
		log.debug("Swapped to options screen");
	}
	private OptionsScreen buildOptionsScreen() {
		OptionsScreen optionsScreen = new OptionsScreen();
		optionsScreen.addCancelListener(this);
		log.debug("Built new options screen = " + optionsScreen);

		return optionsScreen;
	}
	
	private void goToMainScreen() {
		window.setScreen(buildMainScreen(), false);
		log.debug("Swapped to main screen");
	}
	private MainScreen buildMainScreen() {
		MainScreen mainScreen = new MainScreen();
		mainScreen.addCancelListener(this);
		mainScreen.addSqlRequestListener(this);
		mainScreen.setTables(dbConn.getTables());
		mainScreen.setTableModel(tableModel);
		
		dbConn.addStatementListener(mainScreen);
		
		log.debug("Built new main screen = " + mainScreen);

		return mainScreen;
	}
	
	private void updateTableModel() {
		if (tableConn == null) {
			tableModel = null;
			
			log.warning("No table connection set, aborting table model update");
			return;
		}
		log.debug("Updating table model...");
		
		if (tableModel == null) {
			tableModel = new SQLObTableModel(getTableColumns(), getTableData(), true);
			tableModel.addSqlRequestListener(this);
		}
		else
			tableModel.setData(getTableColumns(), getTableData());
		
		log.debug("Done updating table model");
	}
	
	private void updateView() {
		log.debug("Updating view...");
		
		Screen currentScreen = window.getScreen();
		if (currentScreen instanceof MainScreen) {
			MainScreen mainScreen = (MainScreen) currentScreen;
			mainScreen.setTables(dbConn.getTables());
			mainScreen.setTableModel(tableModel);
		}
		log.debug("Done updating view");
	}
	
	@Override
	public void submitted(SubmitSubject source) {
		log.debug("Received SUBMITTED event from: " + source);
		
		if (source instanceof LoginScreen) {
			LoginScreen loginContext = (LoginScreen) source;
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
			loginContext.removeSubmitListener(this);

			goToMainScreen();
		}
	}
	@Override
	public void canceled(CancelSubject source) {
		log.debug("Received CANCELED event from: " + source);

		if (source instanceof MainScreen) {
			setDatabaseConnection(null);
			
			goToLoginScreen();
		} else if (source instanceof OptionsScreen) {
			((OptionsScreen) source).clearListeners();
			
			window.setSize(Config.getInt(WINDOW_WIDTH), Config.getInt(WINDOW_HEIGHT));
			
			goToLoginScreen();
		}
	}
	
	@Override
	public void optionsRequested(OptionsSubject source) {
		log.debug("Received OPTIONS REQUESTED event from " + source);
		
		if (source instanceof LoginScreen)
			goToOptionsScreen();
	}
	
	@Override
	public void update(SqlRequestSubject source) {
		log.debug("Received UPDATE event from: " + source);

		updateTableModel();
	}
	
	@Override
	public void selectTable(String table, SqlRequestSubject source) {
		log.debug("Received SELECT TABLE event from: " + source);

		setTableConnection(dbConn.connect(table));
	}
	
	@Override
	public void createTable(String table, Column[] columns, SqlRequestSubject source) {
		log.debug("Received CREATE TABLE event from: " + source);

		dbConn.createTable(table, columns);
		
		updateView();
	}
	@Override
	public void dropTable(String table, SqlRequestSubject source) {
		log.debug("Received DROP TABLE event from: " + source);

		dbConn.dropTable(table);
		
		if (tableConn.getTableName().equals(table))
			setTableConnection(null);
		
		updateView();
	}
	
	@Override
	public void updateRow(RowEntry[] newValues, RowEntry[] criteria, SqlRequestSubject source) {
		log.debug("Received UPDATE ROW event from: " + source);

		int result = tableConn.update(newValues, criteria);
		if (result > 1)
			updateTableModel();
	}
	@Override
	public void insertRow(RowEntry[] rowValues, SqlRequestSubject source) {
		log.debug("Received INSERT ROW event from: " + source);

		int result = tableConn.insert(rowValues);
		if (result > 1)
			updateTableModel();
	}
	@Override
	public void deleteRow(RowEntry[] criteria, SqlRequestSubject source) {
		log.debug("Received DELETE ROW event from: " + source);

		int result = tableConn.delete(criteria);
		if (result > 1)
			updateTableModel();
	}
	
	@Override
	public void revertStatement(String statement, SqlRequestSubject source) {
		log.debug("Received REVERT STATEMENT event from: " + source + "; statement = " + statement);

		undoLastStatement();
	}
}
