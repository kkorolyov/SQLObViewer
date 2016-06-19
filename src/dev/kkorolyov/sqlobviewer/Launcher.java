package dev.kkorolyov.sqlobviewer;

import java.io.File;
import java.sql.SQLException;

import javax.swing.SwingUtilities;

import dev.kkorolyov.simplelogs.Logger;
import dev.kkorolyov.simplelogs.Logger.Level;
import dev.kkorolyov.sqlobviewer.assets.Assets;
import dev.kkorolyov.sqlobviewer.assets.Strings;
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
		Logger.setGlobalLevel(Level.DEBUG);
		
		initStrings();
		initAssets();
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				MainWindow window = new MainWindow(TITLE, WIDTH, HEIGHT);
				
				new Controller(window);
			}
		});
	}
	
	private static void initStrings() {
		String langFilename = "assets/lang/en.lang";
		
		initFile(langFilename);
		Strings.init(langFilename);
	}
	private static void initAssets() {
		String assetsFilename = "assets/config.ini";
		
		initFile(assetsFilename);
		Assets.init(assetsFilename);
	}
	private static void initFile(String filepath) {
		File file = new File(filepath);
		
		if (!file.exists())
			file.getParentFile().mkdirs();
	}
}
