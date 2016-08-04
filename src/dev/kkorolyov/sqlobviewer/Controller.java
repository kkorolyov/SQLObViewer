package dev.kkorolyov.sqlobviewer;

import static dev.kkorolyov.sqlobviewer.assets.ApplicationProperties.Keys.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.sql.SQLException;

import dev.kkorolyov.simplelogs.Logger;
import dev.kkorolyov.simplelogs.Logger.Level;
import dev.kkorolyov.sqlob.connection.DatabaseConnection;
import dev.kkorolyov.sqlob.connection.DatabaseConnection.DatabaseType;
import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlob.construct.RowEntry;
import dev.kkorolyov.sqlob.construct.statement.StatementCommand;
import dev.kkorolyov.sqlobviewer.assets.ApplicationProperties.Config;
import dev.kkorolyov.sqlobviewer.gui.LoginScreen;
import dev.kkorolyov.sqlobviewer.gui.MainScreen;
import dev.kkorolyov.sqlobviewer.gui.MainWindow;
import dev.kkorolyov.sqlobviewer.gui.OptionsScreen;
import dev.kkorolyov.sqlobviewer.gui.event.*;
import dev.kkorolyov.sqlobviewer.gui.table.SQLObTableModel;
import dev.kkorolyov.sqlobviewer.model.DatabaseModel;

/**
 * Centralized SQLObViewer application control.
 */
public class Controller implements SubmitListener, CancelListener, OptionsListener, SqlRequestListener {
	private static final Logger log = Logger.getLogger(Controller.class.getName(), Level.DEBUG, (PrintWriter[]) null);
	
	private DatabaseModel dbModel;	// Model
	private MainWindow window;	// View
	
	/**
	 * Constructs a new controller for the specified window
	 * @param window application window
	 */
	public Controller(MainWindow window) {
		dbModel = new DatabaseModel();
		SQLObTableModel tableModel = new SQLObTableModel(new Column[0], new RowEntry[0][0], true);
		tableModel.addSqlRequestListener(this);
		dbModel.setTableModel(tableModel);
		
		this.window = window;
		this.window.addWindowListener(new WindowAdapter() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void windowClosing(WindowEvent e) {
				log.debug("Received WINDOW CLOSING event from: " + e.getSource());
				
				dbModel.clearListeners();
				dbModel.setDatabaseConnection(null);
			}
		});
		goToLoginScreen();
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
		MainScreen mainScreen = new MainScreen(dbModel);
		mainScreen.addCancelListener(this);
		mainScreen.addSqlRequestListener(this);
		String[] tables = dbModel.getTables();
		if (tables.length > 0)
			dbModel.selectTable(tables[0]);
		
		log.debug("Built new main screen = " + mainScreen);

		return mainScreen;
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
			DatabaseType databaseType = loginContext.getDatabaseType();
			
			Config.set(SAVED_HOST, host);
			Config.set(SAVED_DATABASE, database);
			Config.set(SAVED_DATABASE_TYPE, databaseType.toString());
			Config.set(SAVED_USER, user);
			Config.set(SAVED_PASSWORD, password);
	
			Config.save();
			try {
				dbModel.setDatabaseConnection(new DatabaseConnection(host, database, databaseType, user, password));
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
			dbModel.setDatabaseConnection(null);
			
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

		dbModel.updateTableModel();
	}
	
	@Override
	public void selectTable(String table, SqlRequestSubject source) {
		log.debug("Received SELECT TABLE (" + table + ") event from: " + source);

		dbModel.selectTable(table);
	}
	
	@Override
	public void createTable(String table, Column[] columns, SqlRequestSubject source) {
		log.debug("Received CREATE TABLE event from: " + source);

		dbModel.createTable(table, columns);
	}
	@Override
	public void dropTable(String table, SqlRequestSubject source) {
		log.debug("Received DROP TABLE event from: " + source);

		dbModel.dropTable(table);
	}
	
	@Override
	public void updateRow(RowEntry[] newValues, RowEntry[] criteria, SqlRequestSubject source) {
		log.debug("Received UPDATE ROW event from: " + source);

		dbModel.updateRow(newValues, criteria);
	}
	@Override
	public void insertRow(RowEntry[] rowValues, SqlRequestSubject source) {
		log.debug("Received INSERT ROW event from: " + source);

		dbModel.insertRow(rowValues);
	}
	@Override
	public void deleteRow(RowEntry[] criteria, SqlRequestSubject source) {
		log.debug("Received DELETE ROW event from: " + source);

		dbModel.deleteRow(criteria);
	}
	
	@Override
	public void revertStatement(StatementCommand statement, SqlRequestSubject source) {
		log.debug("Received REVERT STATEMENT event from: " + source + "; statement = " + statement);

		dbModel.undoStatement(statement);
	}
}
