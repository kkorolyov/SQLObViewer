package dev.kkorolyov.sqlobviewer.assets;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

import dev.kkorolyov.simpleprops.Properties;

/**
 * Centralized access to application properties.
 */
@SuppressWarnings({"synthetic-access", "javadoc"})
public class Assets {
	private static Properties config,
														strings;

	/**
	 * Initializes configuration assets.
	 */
	public static void initConfig() {
		config = new Properties(new File(Defaults.CONFIG_FILENAME), Defaults.buildConfig(), true);
		try {
			config.saveFile();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	/**
	 * Initializes language assets.
	 */
	public static void initStrings() {
		strings = new Properties(new File(Config.get(Keys.STRINGS_FILENAME)), Defaults.buildStrings(), true);
		try {
			strings.saveFile();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	private static String get(String key, Properties props) {
		if (!props.contains(key))
			props.put(key, key);
		
		return props.get(key);
	}
	
	private static boolean set(String key, String value, Properties props) {
		boolean change = !Objects.equals(props.get(key), value);
		
		props.put(key, value);
		
		return change;
	}
	
	private static class Keys {
		private static final String	WINDOW_TITLE = "WINDOW_TITLE",
																WINDOW_WIDTH = "WINDOW_WIDTH",
																WINDOW_HEIGHT = "WINDOW_HEIGHT";
		
		private static final String	SAVED_HOST = "SAVED_HOST",
																SAVED_DATABASE = "SAVED_DATABASE",
																SAVED_USER = "SAVED_USER",
																SAVED_PASSWORD = "SAVED_PASSWORD";
		
		private static final String STRINGS_FILENAME = "LANG_FILE";
		
		private static final String HOST_TEXT = "HOST_TEXT",
																DATABASE_TEXT = "DATABASE_TEXT",
																USER_TEXT = "USER_TEXT",
																PASSWORD_TEXT = "PASSWORD_TEXT",
																REFRESH_TABLE_TEXT = "REFRESH_TABLE_TEXT",
																NEW_TABLE_TEXT = "NEW_TABLE_TEXT",
																ADD_ROW_TEXT = "ADD_ROW_TEXT",
																DELETE_ROW_TEXT = "DELETE_ROW_TEXT",
																UNDO_STATEMENT_TEXT = "UNDO_STATEMENT_TEXT",
																LOG_IN_TEXT = "LOG_IN_TEXT",
																LOG_OUT_TEXT = "LOG_OUT_TEXT";
	}
	private static class Defaults {
		private static final String WINDOW_WIDTH = "720",
																WINDOW_HEIGHT = "480";
		private static final String CONFIG_FILENAME = "assets/config.ini",
																STRINGS_FILENAME = "assets/lang/en.lang";
		
		private static Properties buildConfig() {
			Properties defaults = new Properties();
			defaults.put(Keys.WINDOW_WIDTH, WINDOW_WIDTH);
			defaults.put(Keys.WINDOW_HEIGHT, WINDOW_HEIGHT);
			defaults.put(Keys.STRINGS_FILENAME, STRINGS_FILENAME);
			
			return defaults;
		}
		private static Properties buildStrings() {
			Properties defaults = null;
			
			return defaults;
		}
	}
	
	/**
	 * All configuration keys.
	 */
	public static class Config {
		public static final String	WINDOW_WIDTH = Keys.WINDOW_WIDTH,
																WINDOW_HEIGHT = Keys.WINDOW_HEIGHT;
		public static final String	SAVED_HOST = Keys.SAVED_HOST,
																SAVED_DATABASE = Keys.SAVED_DATABASE,
																SAVED_USER = Keys.SAVED_USER,
																SAVED_PASSWORD = Keys.SAVED_PASSWORD;
		
		/**
		 * Retrieves the configuration value for a key.
		 * @param key key to use
		 * @return value for the specified key.
		 */
		public static String get(String key) {
			return Assets.get(key, config);
		}
		/**
		 * Sets the configuration value for a specified key. If such a key is not found within the loaded configuration, it is added instead.
		 * @param key key to use
		 * @param value value to set
		 * @return {@code true} if invoking this method results in a change to the backing properties
		 */
		public static boolean set(String key, String value) {
			return Assets.set(key, value, config);
		}
		
		/**
		 * Saves current configuration.
		 */
		public static void save() {
			try {
				config.saveFile();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
	/**
	 * All language keys.
	 */
	public static class Strings {
		public static final String 	WINDOW_TITLE = Keys.WINDOW_TITLE,
																HOST = Keys.HOST_TEXT,
																DATABASE = Keys.DATABASE_TEXT,
																USER = Keys.USER_TEXT,
																PASSWORD = Keys.PASSWORD_TEXT,
																LOG_IN = Keys.LOG_IN_TEXT,
																LOG_OUT = Keys.LOG_OUT_TEXT,
																REFRESH_TABLE = Keys.REFRESH_TABLE_TEXT,
																NEW_TABLE = Keys.NEW_TABLE_TEXT,
																ADD_ROW = Keys.ADD_ROW_TEXT,
																DELETE_ROW = Keys.DELETE_ROW_TEXT,
																UNDO_STATEMENT = Keys.UNDO_STATEMENT_TEXT;
		
		/**
		 * Retrieves the loaded language file's value for a key.
		 * @param key key to use
		 * @return value for the specified key.
		 */
		public static String get(String key) {
			return Assets.get(key, strings);
		}
	}
}
