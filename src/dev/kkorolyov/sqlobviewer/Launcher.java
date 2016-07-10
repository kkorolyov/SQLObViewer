package dev.kkorolyov.sqlobviewer;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.WINDOW_HEIGHT;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.WINDOW_TITLE;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.WINDOW_WIDTH;

import java.sql.SQLException;

import javax.swing.SwingUtilities;

import dev.kkorolyov.simplelogs.Logger;
import dev.kkorolyov.simplelogs.Logger.Level;
import dev.kkorolyov.sqlobviewer.assets.Assets;
import dev.kkorolyov.sqlobviewer.assets.Assets.Config;
import dev.kkorolyov.sqlobviewer.assets.Assets.Strings;
import dev.kkorolyov.sqlobviewer.gui.MainWindow;

/**
 * Launcher SQLObViewer
 */
public class Launcher {
	private static final Logger log = Logger.getLogger(Launcher.class.getName());
	private static final Level LEVEL = Level.DEBUG;
	
	/**
	 * Main method.
	 * @param args arguments
	 * @throws SQLException if a database connection error occurs
	 */
	public static void main(String[] args) throws SQLException {
		Logger.setGlobalLevel(LEVEL);
		log.severe("Logging at level=" + LEVEL);
		
		Assets.init();
		
		MainWindow window = buildWindow();

		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {	// Display application errors + terminate
			@SuppressWarnings("synthetic-access")
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				log.severe("Error caught by default ExceptionHandler");
				log.exception((Exception) e, Level.SEVERE);
				
				window.displayError(e, true);
				
				log.severe("Terminated gracefully");
			}
		});
		SwingUtilities.invokeLater(new Runnable() {			
			@Override
			public void run() {
				new Controller(window);
			}
		});
	}
	private static MainWindow buildWindow() {
		String title = Strings.get(WINDOW_TITLE);
		int width = Integer.parseInt(Config.get(WINDOW_WIDTH)),
				height = Integer.parseInt(Config.get(WINDOW_HEIGHT));
		
		log.debug("Built application window with title=" + title + ", width=" + width + ", height=" + height);
		
		return new MainWindow(title, width, height);
	}
}
