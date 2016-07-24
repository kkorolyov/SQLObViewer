package dev.kkorolyov.sqlobviewer;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

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
	private static final Logger log = Logger.getLogger(Launcher.class.getName(), Level.DEBUG, (PrintWriter[]) null);
	
	/**
	 * Main method.
	 * @param args arguments
	 */
	public static void main(String[] args) {
		setLogging();
		
		MainWindow window = buildWindow();

		setExceptionHandler(window);	// Will display uncaught exceptions in this window
		
		log.info("Launching GUI...");
		SwingUtilities.invokeLater(() -> new Controller(window));
		log.info("GUI launched");
	}
	
	private static MainWindow buildWindow() {
		String title = Lang.get(TITLE_WINDOW);
		int width = Config.getInt(WINDOW_WIDTH),
				height = Config.getInt(WINDOW_HEIGHT);
		
		log.debug("Built application window with title=" + title + ", width=" + width + ", height=" + height);
		
		return new MainWindow(title, width, height);
	}
	
	private static void setLogging() {
		Level loggingLevel = Level.SEVERE;
		boolean loggingEnabled = Config.get(LOGGING_ENABLED).equalsIgnoreCase(Boolean.TRUE.toString());
		String loggingFile = Config.get(LOG_FILE);
		
		String loggingLevelString = Config.get(LOGGING_LEVEL);
		for (Level level : Level.values()) {
			if (loggingLevelString.equalsIgnoreCase(level.toString())) {
				loggingLevel = level;
				break;
			}
		}
		Logger rootLogger = Logger.getLogger("", loggingLevel, (PrintWriter[]) null);
		rootLogger.addWriter(new PrintWriter(System.err));
		try {
			rootLogger.addWriter(new PrintWriter(loggingFile));
		} catch (FileNotFoundException e) {
			log.severe("Cannot create log file, will not log to file");
			log.exception(e);
		}
		rootLogger.setLevel(loggingLevel);
		rootLogger.setEnabled(loggingEnabled);
		
		log.severe("Logging at level=" + loggingLevel);
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
