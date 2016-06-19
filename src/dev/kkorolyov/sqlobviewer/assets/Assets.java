package dev.kkorolyov.sqlobviewer.assets;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

import dev.kkorolyov.simpleprops.Properties;

/**
 * Centralized access to application properties.
 */
public class Assets {
	private static final String EMPTY_VALUE = "";
	private static Properties 	config,
															strings;

	/**
	 * Initializes configuration assets.
	 * @return {@code true} if the properties file was just created.
	 */
	public static boolean initConfig() {
		String configFilename = Config.DEFAULT_CONFIG_FILENAME;
		initFile(configFilename);
		config = Properties.getInstance(configFilename);
		
		return config.size() <= 0;
	}
	/**
	 * Initializes language assets.
	 * @return {@code true} if the properties file was just created.
	 */
	public static boolean initStrings() {
		String stringsFilename = Config.get(Config.STRINGS_FILENAME);
		if (stringsFilename == null || stringsFilename.equals(EMPTY_VALUE)) {
			Config.set(Config.STRINGS_FILENAME, Config.DEFAULT_STRINGS_FILENAME);
			stringsFilename = Config.get(Config.STRINGS_FILENAME);
		}
		initFile(stringsFilename);
		strings = Properties.getInstance(stringsFilename);
		
		return strings.size() <= 0;
	}
	
	private static void initFile(String filepath) {
		File file = new File(filepath);
		
		if (!file.exists())
			file.getParentFile().mkdirs();
	}
	
	private static String get(String key, Properties props) {
		if (props.getValue(key) == null)
			props.addProperty(key, EMPTY_VALUE);
		
		return props.getValue(key);
	}
	
	private static boolean set(String key, String value, Properties props) {
		boolean change = !Objects.equals(props.getValue(key), value);
		
		props.addProperty(key, value);
		
		return change;
	}
	
	/**
	 * All configuration keys.
	 */
	@SuppressWarnings("javadoc")
	public static class Config {
		public static final String	WINDOW_TITLE = "WINDOW_TITLE",
																WINDOW_WIDTH = "WINDOW_WIDTH",
																WINDOW_HEIGHT = "WINDOW_HEIGHT";
		public static final String	SAVED_HOST = "SAVED_HOST",
																SAVED_DATABASE = "SAVED_DATABASE",
																SAVED_USER = "SAVED_USER",
																SAVED_PASSWORD = "SAVED_PASSWORD";
		public static final int DEFAULT_WIDTH = 720,
														DEFAULT_HEIGHT = 480;
		
		private static final String DEFAULT_CONFIG_FILENAME = "assets/config.ini",
																DEFAULT_STRINGS_FILENAME = "assets/lang/en.lang";
		private static final String STRINGS_FILENAME = "LANG_FILE";
		
		/**
		 * Retrieves the configuration value for a key.
		 * @param key key to use
		 * @return value for the specified key.
		 */
		@SuppressWarnings("synthetic-access")
		public static String get(String key) {
			return Assets.get(key, config);
		}
		/**
		 * Sets the configuration value for a specified key. If such a key is not found within the loaded configuration, it is added instead.
		 * @param key key to use
		 * @param value value to set
		 * @return {@code true} if invoking this method results in a change to the backing properties
		 */
		@SuppressWarnings("synthetic-access")
		public static boolean set(String key, String value) {
			return Assets.set(key, value, config);
		}
		
		/**
		 * Saves current configuration.
		 */
		@SuppressWarnings("synthetic-access")
		public static void save() {
			try {
				config.saveToFile();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
	/**
	 * All language keys.
	 */
	@SuppressWarnings("javadoc")
	public static class Strings {
		public static final String 	HOST = "HOST_TEXT",
																DATABASE = "DATABASE_TEXT",
																USER = "USER_TEXT",
																PASSWORD = "PASSWORD_TEXT",
																REFRESH_TABLE = "REFRESH_TABLE_TEXT",
																NEW_TABLE = "NEW_TABLE_TEXT",
																ADD_ROW = "ADD_ROW_TEXT",
																DELETE_ROW = "DELETE_ROW_TEXT",
																UNDO_STATEMENT = "UNDO_STATEMENT_TEXT",
																LOG_IN = "LOG_IN_TEXT",
																LOG_OUT = "LOG_OUT_TEXT";
		
		/**
		 * Retrieves the loaded language file's value for a key.
		 * @param key key to use
		 * @return value for the specified key.
		 */
		@SuppressWarnings("synthetic-access")
		public static String get(String key) {
			return Assets.get(key, strings);
		}
	}
}
