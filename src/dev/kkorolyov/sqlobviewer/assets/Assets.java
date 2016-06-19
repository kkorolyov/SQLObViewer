package dev.kkorolyov.sqlobviewer.assets;

import java.io.IOException;
import java.io.UncheckedIOException;

import dev.kkorolyov.simpleprops.Properties;

/**
 * Centralized access to all assets.
 */
public class Assets {
	@SuppressWarnings("javadoc")
	public static final String	WINDOW_TITLE = "WINDOW_TITLE",
															WINDOW_WIDTH = "WINDOW_WIDTH",
															WINDOW_HEIGHT = "WINDOW_HEIGHT",
															SAVED_HOST = "SAVED_HOST",
															SAVED_DATABASE = "SAVED_DATABASE",
															SAVED_USER = "SAVED_USER",
															SAVED_PASSWORD = "SAVED_PASSWORD";
	@SuppressWarnings("javadoc")
	public static final int DEFAULT_WIDTH = 720,
													DEFAULT_HEIGHT = 480;
	
	private static Properties props;

	/**
	 * Initializes all assets.
	 * @return {@code true} if properties were just initialized and must be set.
	 */
	public static boolean init(String filename) {		
		props = Properties.getInstance((filename));
		
		return props.size() <= 0;
	}
	
	/**
	 * Retrieves the value for a key.
	 * @param key key to use
	 * @return value for the specified key.
	 */
	public static String get(String key) {
		if (props.getValue(key) == null)
			props.addProperty(key, "");
		
		return props.getValue(key);
	}
	/**
	 * Sets the value matching a specified key. If such a key is not found within the loaded assets, it is added instead.
	 * @param key key to use
	 * @param value value to set
	 */
	public static void set(String key, String value) {
		props.addProperty(key, value);
	}
	
	/**
	 * Saves current assets.
	 */
	public static void save() {
		try {
			props.saveToFile();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
