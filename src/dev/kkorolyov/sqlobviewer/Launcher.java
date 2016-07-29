package dev.kkorolyov.sqlobviewer;

import static dev.kkorolyov.sqlobviewer.assets.ApplicationProperties.Keys.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.swing.SwingUtilities;

import dev.kkorolyov.simplelogs.Logger;
import dev.kkorolyov.simplelogs.Logger.Level;
import dev.kkorolyov.sqlobviewer.assets.ApplicationProperties.Config;
import dev.kkorolyov.sqlobviewer.assets.ApplicationProperties.Lang;
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
		Logger rootLogger = Logger.getLogger("", parseLoggingLevel(), (PrintWriter[]) null);
		rootLogger.setEnabled(parseLoggingEnabled());

		if (rootLogger.isEnabled()) {	// Avoid adding useless loggers
			rootLogger.addWriter(new PrintWriter(System.err));
			try {
				rootLogger.addWriter(new PrintWriter(Config.get(LOG_FILE)));
			} catch (FileNotFoundException e) {
				log.severe("Cannot create log file, will not log to file");
				log.exception(e);
			}
		}
		log.severe("Logging at level=" + rootLogger.getLevel());
	}
	private static Level parseLoggingLevel() {
		Level loggingLevel = Level.SEVERE;
		
		String loggingLevelString = Config.get(LOGGING_LEVEL);

		for (Level level : Level.values()) {
			if (loggingLevelString.equalsIgnoreCase(level.toString())) {
				loggingLevel = level;
				break;
			}
		}
		return loggingLevel;
	}
	private static boolean parseLoggingEnabled() {
		return Config.get(LOGGING_ENABLED).equalsIgnoreCase(Boolean.TRUE.toString());
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
