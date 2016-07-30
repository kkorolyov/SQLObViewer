package dev.kkorolyov.sqlobviewer.assets;

import java.awt.Image;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import dev.kkorolyov.simpleprops.Properties;

/**
 * An asset used by the application.
 */
public enum Asset {
	/** Default configuration file */
	DEFAULT_CONFIG("default.ini"),
	/** Default language file */
	DEFAULT_LANG("default.lang"),
	
	/** Main application icons directory */
	MAIN_ICON("icons/main-icon/"),
	/** Options icon file */
	OPTIONS_ICON("icons/gear.png"),
	/** Refresh icon file */
	REFRESH_ICON("icons/refresh.png");
	
	private String path;
	private boolean directory;
	
	private Asset(String path) {
		this.path = path;
		directory = this.path.endsWith("/");
	}
	
	/**
	 * @return this asset as a properties file
	 * @throws IllegalArgumentException if this asset is not a file
	 */
	public Properties asProperties() {
		return asProperties(null);
	}
	/**
	 * @return this asset as a properties file with {@code defaults} as the default properties
	 * @throws IllegalArgumentException if this asset is not a file
	 */
	public Properties asProperties(Properties defaults) {
		assertFile();
		
		return new Properties(FileLocator.locateFile(path), defaults);
	}
	
	/**
	 * @return this asset as an icon
	 * @throws IllegalArgumentException if this asset is not a file
	 */
	public Icon asIcon() {
		assertFile();
		
		return new ImageIcon(FileLocator.locateFile(path).getAbsolutePath());
	}
	
	/**
	 * @return this asset as a list of images
	 * @throws IllegalArgumentException if this asset is not a directory
	 */
	public List<Image> asImages() {
		assertDirectory();
		
		File mainIconFolder = FileLocator.locateFile(path);
		
		List<Image> mainIcons = new LinkedList<>();

		if (mainIconFolder != null && mainIconFolder.exists()) {
			for (File mainIconFile : mainIconFolder.listFiles())
				mainIcons.add(new ImageIcon(mainIconFile.getAbsolutePath()).getImage());
		}
		return mainIcons;
	}
	
	/** @return relative path to this asset */
	public String getPath() {
		return path;
	}
	
	/** @return {@code true} if this asset is a file */
	public boolean isFile() {
		return !directory;
	}
	/** @return {@code false} if this asset is a directory */
	public boolean isDirectory() {
		return directory;
	}
	
	private void assertFile() {
		if (directory)
			throw new IllegalArgumentException("Asset is not a file");
	}
	private void assertDirectory() {
		if (!directory)
			throw new IllegalArgumentException("Asset is not a directory");
	}
}
