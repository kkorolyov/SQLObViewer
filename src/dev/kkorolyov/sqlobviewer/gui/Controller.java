package dev.kkorolyov.sqlobviewer.gui;

import java.sql.SQLException;

import dev.kkorolyov.sqlob.connection.DatabaseConnection;
import dev.kkorolyov.sqlobviewer.assets.Assets;

/**
 * Centralized SQLObViewer application control.
 */
public class Controller implements GuiListener {
	private DatabaseConnection dbConn;	// Model
	private MainWindow window;	// View
	
	/**
	 * Constructs a new controller for the specified window
	 * @param window application window
	 */
	public Controller(MainWindow window) {
		setWindow(window);
	}
	
	@Override
	public void logInButtonPressed(String host, String database, String user, String password, MainWindow context) {
		Assets.setHost(host);
		Assets.setDatabase(database);
		Assets.setUser(user);
		Assets.setPassword(password);
		
		Assets.save();
		
		try {
			setDatabaseConnection(new DatabaseConnection(host, database, user, password));
		} catch (SQLException e) {
			context.displayError(e.getMessage());
		}
	}
	
	/** @param newDatabaseConnection new database connection */
	public void setDatabaseConnection(DatabaseConnection newDatabaseConnection) {
		dbConn = newDatabaseConnection;
	}
	/** @param newWindow new application window */
	public void setWindow(MainWindow newWindow) {
		window = newWindow;
		window.addListener(this);
	}
}
