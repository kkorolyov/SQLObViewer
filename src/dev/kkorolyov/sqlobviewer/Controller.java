package dev.kkorolyov.sqlobviewer;

import java.sql.SQLException;

import dev.kkorolyov.sqlob.connection.DatabaseConnection;
import dev.kkorolyov.sqlob.connection.TableConnection;
import dev.kkorolyov.sqlobviewer.assets.Assets;
import dev.kkorolyov.sqlobviewer.gui.LoginScreen;
import dev.kkorolyov.sqlobviewer.gui.MainWindow;
import dev.kkorolyov.sqlobviewer.gui.ViewScreen;
import dev.kkorolyov.sqlobviewer.gui.event.GuiListener;
import dev.kkorolyov.sqlobviewer.gui.event.GuiSubject;

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
		
		window.setViewScreen(new ViewScreen(dbTables));
		window.showViewPanel();
	}
	@Override
	public void tableSelected(String table) {
		TableConnection selectedTable = dbConn.connect(table);
		
		window.setViewedTable(selectedTable);
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
