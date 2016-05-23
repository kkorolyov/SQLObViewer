package dev.kkorolyov.sqlobviewer;

import java.sql.SQLException;

import dev.kkorolyov.sqlob.connection.DatabaseConnection;
import dev.kkorolyov.sqlobviewer.assets.Assets;
import dev.kkorolyov.sqlobviewer.gui.Backend;
import dev.kkorolyov.sqlobviewer.gui.Frontend;
import dev.kkorolyov.sqlobviewer.setup.Setup;

/**
 * Launcher SQLObViewer
 */
public class Launcher {
	private static final String TITLE = "SQLObViewer";
	private static final int 	WIDTH = 720,
														HEIGHT = 480;

	/**
	 * Main method.
	 * @param args arguments
	 * @throws SQLException if a database connection error occurs
	 */
	public static void main(String[] args) throws SQLException {
		boolean launchSetup = Assets.init();
		
		Frontend frontend = new Frontend(TITLE, WIDTH, HEIGHT);
		
		if (launchSetup)
			Setup.setup(frontend);
		
		Backend backend = new Backend(new DatabaseConnection(Assets.host(), Assets.database(), Assets.user(), Assets.password()));
		
		frontend.setBackend(backend);
	}
}
