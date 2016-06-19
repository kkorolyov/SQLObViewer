package dev.kkorolyov.sqlobviewer;

import static dev.kkorolyov.sqlobviewer.assets.Assets.*;

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
			@SuppressWarnings("synthetic-access")
			@Override
			public void run() {
				new Controller(buildWindow());
				
				save();	// Save properties after exit
			}
		});
	}
	private static MainWindow buildWindow() {
		String title = Assets.get(WINDOW_TITLE);
		int width = Assets.DEFAULT_WIDTH,
				height = Assets.DEFAULT_HEIGHT;
		
		try {
			String 	widthString = Assets.get(WINDOW_WIDTH),
							heightString = Assets.get(WINDOW_HEIGHT);
			
			width = Integer.parseInt(widthString);
			height = Integer.parseInt(heightString);
		} catch (Exception e) {
			// Keep default sizes
		}
		return new MainWindow(title, width, height);
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
	private static void save() {
		Assets.save();
		Strings.save();
	}
}
