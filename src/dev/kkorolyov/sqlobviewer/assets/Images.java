package dev.kkorolyov.sqlobviewer.assets;

import java.awt.Image;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;

/**
 * Provides access to all application images and icons.
 */
public class Images {
	private static final String SUBFOLDER_MAIN_ICON = "icons/main-icon/";
	
	/** @return all main application icons, for use by the application frame */
	public static List<Image> getMainIcons() {
		List<Image> mainIcons = new LinkedList<>();
		
		File mainIconFolder = FileLocator.locateFile(SUBFOLDER_MAIN_ICON);
		
		if (mainIconFolder != null && mainIconFolder.exists()) {
			for (File mainIconFile : mainIconFolder.listFiles())
				mainIcons.add(new ImageIcon(mainIconFile.getAbsolutePath()).getImage());
		}
		return mainIcons;
	}
}
