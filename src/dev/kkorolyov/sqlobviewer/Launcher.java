package dev.kkorolyov.sqlobviewer;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.TITLE_WINDOW;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.WINDOW_HEIGHT;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.WINDOW_WIDTH;

import javax.swing.SwingUtilities;

import dev.kkorolyov.simplelogs.Logger;
import dev.kkorolyov.simplelogs.Logger.Level;
import dev.kkorolyov.sqlobviewer.assets.Assets.Config;
import dev.kkorolyov.sqlobviewer.assets.Assets.Lang;
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
	 */
	public static void main(String[] args) {
		Logger.setGlobalLevel(LEVEL);
		log.severe("Logging at level=" + LEVEL);
		
		MainWindow window = buildWindow();

		setExceptionHandler(window);	// Will display uncaught exceptions in this window
		
		log.info("Launching GUI");
		SwingUtilities.invokeLater(() -> new Controller(window));
	}
	
	private static MainWindow buildWindow() {
		String title = Lang.get(TITLE_WINDOW);
		int width = Config.getInt(WINDOW_WIDTH),
				height = Config.getInt(WINDOW_HEIGHT);
		
		log.debug("Built application window with title=" + title + ", width=" + width + ", height=" + height);
		
		return new MainWindow(title, width, height);
	}
	
	private static void setExceptionHandler(MainWindow window) {
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {	// Display application errors + terminate
			@SuppressWarnings("synthetic-access")
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				log.severe("Error caught by default ExceptionHandler");
				log.exception((Exception) e, Level.SEVERE);
				
				log.severe("Terminating application");
				window.displayError(e, true);
				log.severe("Terminated application successfully");
			}
		});
	}
}
