package dev.kkorolyov.sqlobviewer.assets;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.IMAGES_FOLDER;

import java.awt.Image;
import java.io.File;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;

import dev.kkorolyov.simplelogs.Logger;
import dev.kkorolyov.simplelogs.Logger.Level;
import dev.kkorolyov.sqlobviewer.assets.Assets.Config;

/**
 * Provides access to all application images and icons.
 */
public class Images {
	private static final String SUBFOLDER_MAIN_ICON = "main-icon/";
	private static final Logger log = Logger.getLogger(Images.class.getName(), Level.DEBUG, (PrintWriter[]) null);

	/** @return all main application icons, for use by the application frame */
	public static List<Image> getMainIcons() {
		List<Image> mainIcons = new LinkedList<>();
		
		File mainIconFolder = locateFile(SUBFOLDER_MAIN_ICON);
		
		if (mainIconFolder != null && mainIconFolder.exists()) {
			for (File mainIconFile : mainIconFolder.listFiles())
				mainIcons.add(new ImageIcon(mainIconFile.getAbsolutePath()).getImage());
		}
		return mainIcons;
	}
	
	private static File locateFile(String filename) {	// Attempts to load custom file on filesystem before loading bundled file
		File file = new File(Config.get(IMAGES_FOLDER) + filename);	// System file
		
		if (file.exists())
			log.info("Located custom " + filename + " at " + file.getAbsolutePath());
		else {
			try {
				file = new File(Images.class.getResource(filename).toURI());	// Bundled file
				log.info("Using bundled " + filename + " at " + file.getAbsolutePath());
			} catch (URISyntaxException | NullPointerException e) {
				log.severe("Unable to locate bundled " + filename);
				log.exception(e);
			}
		}
		return file;
	}
}
