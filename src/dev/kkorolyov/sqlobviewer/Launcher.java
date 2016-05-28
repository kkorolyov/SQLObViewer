package dev.kkorolyov.sqlobviewer;

import java.sql.SQLException;

import javax.swing.SwingUtilities;

import dev.kkorolyov.sqlobviewer.assets.Assets;
import dev.kkorolyov.sqlobviewer.gui.MainWindow;

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
		Assets.init();
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				MainWindow window = new MainWindow(TITLE, WIDTH, HEIGHT);
				
				new Controller(window);
			}
		});
	}
}
