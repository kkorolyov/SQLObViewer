package dev.kkorolyov.sqlobviewer;

import static dev.kkorolyov.sqlobviewer.assets.ApplicationProperties.Keys.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import dev.kkorolyov.simplelogs.Logger;
import dev.kkorolyov.simplelogs.Logger.Level;
import dev.kkorolyov.sqlob.connection.DatabaseConnection;
import dev.kkorolyov.sqlob.connection.DatabaseConnection.DatabaseType;
import dev.kkorolyov.sqlob.connection.TableConnection;
import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlob.construct.Results;
import dev.kkorolyov.sqlob.construct.RowEntry;
import dev.kkorolyov.sqlob.construct.statement.StatementCommand;
import dev.kkorolyov.sqlob.construct.statement.UpdateStatement;
import dev.kkorolyov.sqlobviewer.assets.ApplicationProperties.Config;
import dev.kkorolyov.sqlobviewer.gui.LoginScreen;
import dev.kkorolyov.sqlobviewer.gui.MainScreen;
import dev.kkorolyov.sqlobviewer.gui.MainWindow;
import dev.kkorolyov.sqlobviewer.gui.OptionsScreen;
import dev.kkorolyov.sqlobviewer.gui.event.*;
import dev.kkorolyov.sqlobviewer.model.DatabaseModel;

/**
 * Centralized SQLObViewer application control.
 */
public class Controller implements DatabaseModel, SubmitListener, CancelListener, OptionsListener, SqlRequestListener {
	private static final Logger log = Logger.getLogger(Controller.class.getName(), Level.DEBUG, (PrintWriter[]) null);
	
	private DatabaseConnection dbConn;
	private TableConnection tableConn;
	
	private MainWindow window;	// View
	
	private Set<ChangeListener> changeListeners = new CopyOnWriteArraySet<>();
	
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
				
				clearListeners();
				setDatabaseConnection(null);
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
		MainScreen mainScreen = new MainScreen(this);
		mainScreen.addCancelListener(this);
		mainScreen.addSqlRequestListener(this);
		
		log.debug("Built new main screen = " + mainScreen);

		return mainScreen;
	}
	
	@Override
	public String getDatabase() {
		return dbConn != null ? dbConn.getDatabaseName() : null;
	}
	@Override
	public String getTable() {
		return tableConn != null ? tableConn.getTableName() : null;
	}
	@Override
	public String[] getTables() {
		return dbConn.getTables();
	}
	
	@Override
	public Column[] getTableColumns() {
		return tableConn != null ? tableConn.getColumns() : new Column[0];
	}
	@Override
	public RowEntry[][] getTableData() {
		if (tableConn == null)
			return new RowEntry[0][0];
		
		List<RowEntry[]> data = new LinkedList<>();

		Results allResults = tableConn.select(null);
		
		RowEntry[] currentRow;
		while ((currentRow = allResults.getNextRow()) != null)				
			data.add(currentRow);
			
		return data.toArray(new RowEntry[data.size()][]);
	}
	
	@Override
	public UpdateStatement getLastStatement() {
		UpdateStatement lastStatement = null;
		
		if (dbConn != null) {
			for (int i = dbConn.getStatementLog().size() - 1; i >= 0; i--) {
				StatementCommand currentStatement = dbConn.getStatementLog().get(i);
				if (currentStatement instanceof UpdateStatement) {
					lastStatement = (UpdateStatement) currentStatement;
					break;
				}
			}
		}
		return lastStatement;
	}
	
	private void setDatabaseConnection(DatabaseConnection newDatabaseConnection) {
		if (dbConn != null)
			dbConn.close();
		tableConn = null;
		
		dbConn = newDatabaseConnection;
		log.debug("Set database connection = " + getDatabase());
		
		if (dbConn != null)
			setDefaultTableConnection();
		
		fireStateChanged();
	}
	private void setTableConnection(TableConnection newTableConnection) {
		tableConn = newTableConnection;
		log.debug("Set table connection = " + getTable());
		
		fireStateChanged();
	}
	private void setDefaultTableConnection() {
		String[] tables = getTables();
		
		if (tables.length > 0)
			setTableConnection(dbConn.connect(tables[0]));
		else
			setTableConnection(null);
	}
	
	private void applyOptions() {
		window.setSize(Config.getInt(WINDOW_WIDTH), Config.getInt(WINDOW_HEIGHT));
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
				setDatabaseConnection(new DatabaseConnection(host, database, databaseType, user, password));
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

		source.clearListeners();

		if (source instanceof MainScreen) {
			clearListeners();
			setDatabaseConnection(null);
			
			goToLoginScreen();
		} else if (source instanceof OptionsScreen) {
			applyOptions();
			
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

		fireStateChanged();
	}
	
	@Override
	public void selectTable(String table, SqlRequestSubject source) {
		log.debug("Received SELECT TABLE (" + table + ") event from: " + source);

		setTableConnection(dbConn.connect(table));
	}
	
	@Override
	public void createTable(String table, Column[] columns, SqlRequestSubject source) {
		log.debug("Received CREATE TABLE event from: " + source);

		setTableConnection(dbConn.createTable(table, columns));
	}
	@Override
	public void dropTable(String table, SqlRequestSubject source) {
		log.debug("Received DROP TABLE event from: " + source);

		dbConn.dropTable(table);
		
		if (getTable().equals(table))
			setTableConnection(null);
		else
			fireStateChanged();
	}
	
	@Override
	public void updateRow(RowEntry[] newValues, RowEntry[] criteria, SqlRequestSubject source) {
		log.debug("Received UPDATE ROW event from: " + source);

		tableConn.update(newValues, criteria);
		
		fireStateChanged();
	}
	@Override
	public void insertRow(RowEntry[] rowValues, SqlRequestSubject source) {
		log.debug("Received INSERT ROW event from: " + source);

		tableConn.insert(rowValues);
		
		fireStateChanged();
	}
	@Override
	public void deleteRow(RowEntry[] criteria, SqlRequestSubject source) {
		log.debug("Received DELETE ROW event from: " + source);

		tableConn.delete(criteria);
		
		fireStateChanged();
	}
	
	@Override
	public void revertStatement(StatementCommand statement, SqlRequestSubject source) {
		log.debug("Received REVERT STATEMENT event from: " + source + "; statement = " + statement);

		dbConn.getStatementLog().revert((UpdateStatement) statement, true);
		
		if (getTable() == null || !dbConn.containsTable(getTable()))
			setDefaultTableConnection();

		fireStateChanged();
	}
	
	private void fireStateChanged() {
		for (ChangeListener listener : changeListeners)
			listener.stateChanged(new ChangeEvent(this));
	}
	
	@Override
	public void addChangeListener(ChangeListener listener) {
		changeListeners.add(listener);
	}
	@Override
	public void removeChangeListener(ChangeListener listener) {
		changeListeners.remove(listener);
	}
	
	@Override
	public void clearListeners() {
		changeListeners.clear();
	}
}
