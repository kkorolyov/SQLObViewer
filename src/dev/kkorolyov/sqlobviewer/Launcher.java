package dev.kkorolyov.sqlobviewer;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Config.*;

import java.sql.SQLException;

import javax.swing.SwingUtilities;

import dev.kkorolyov.simplelogs.Logger;
import dev.kkorolyov.simplelogs.Logger.Level;
import dev.kkorolyov.sqlobviewer.assets.Assets;
import dev.kkorolyov.sqlobviewer.assets.Assets.Config;
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
		
		Assets.initConfig();
		Assets.initStrings();
		
		MainWindow window = buildWindow();

		SwingUtilities.invokeLater(new Runnable() {			
			@Override
			public void run() {
				new Controller(window);
				
				Config.save();	// Save properties after exit
			}
		});
	}
	private static MainWindow buildWindow() {
		String title = Config.get(WINDOW_TITLE);
		int width = DEFAULT_WIDTH,
				height = DEFAULT_HEIGHT;
		
		try {
			String 	widthString = Config.get(WINDOW_WIDTH),
							heightString = Config.get(WINDOW_HEIGHT);
			
			width = Integer.parseInt(widthString);
			height = Integer.parseInt(heightString);
		} catch (Exception e) {
			// Keep default sizes
		}
		return new MainWindow(title, width, height);
	}
}
