package dev.kkorolyov.sqlobviewer.assets;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

import dev.kkorolyov.simplelogs.Logger;
import dev.kkorolyov.simpleprops.EncryptedProperties;
import dev.kkorolyov.simpleprops.Properties;

/**
 * Centralized access to application properties.
 */
@SuppressWarnings({"synthetic-access", "javadoc"})
public class Assets {
	private static final Logger log = Logger.getLogger(Assets.class.getName());
	
	private static Properties config,
														strings;
	private static byte[] key = {2};

	/**
	 * Loads assets.
	 * Creates necessary asset files if needed.
	 */
	public static void init() {
		initConfig();
		initStrings();
		
		log.debug("Initialized assets");
	}
	
	private static void initConfig() {
		config = new EncryptedProperties(new File(Defaults.CONFIG_FILENAME), Defaults.buildConfig(), key);
		save(config);
		
		log.debug("Initialized config file");
	}
	private static void initStrings() {
		strings = new Properties(new File(Config.get(Keys.STRINGS_FILENAME)), Defaults.buildStrings());
		save(strings);
		
		log.debug("Initialized strings file");
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
	
	private static void save(Properties props) {
		try {
			props.saveFile(true);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	/**
	 * All configuration and string keys.
	 */
	public static class Keys {
		public static final String	WINDOW_WIDTH = "WINDOW_WIDTH",
																WINDOW_HEIGHT = "WINDOW_HEIGHT";
		
		public static final String	SAVED_HOST = "SAVED_HOST",
																SAVED_DATABASE = "SAVED_DATABASE",
																SAVED_USER = "SAVED_USER",
																SAVED_PASSWORD = "SAVED_PASSWORD";
		
		public static final String STRINGS_FILENAME = "LANG_FILE";
		
		public static final String 	WINDOW_TITLE = "WINDOW_TITLE",
																EXCEPTION_TITLE_SUFFIX = "EXCEPTION_TITLE_SUFFIX",
																ERROR_TITLE_SUFFIX = "ERROR_TITLE_SUFFIX",
																EXPAND_ERROR_TEXT = "EXPAND_ERROR_TEXT",
																APPLICATION_CLOSING_TEXT = "APPLICATION_CLOSING_TEXT",
																HOST_TEXT = "HOST_TEXT",
																DATABASE_TEXT = "DATABASE_TEXT",
																USER_TEXT = "USER_TEXT",
																PASSWORD_TEXT = "PASSWORD_TEXT",
																REFRESH_TABLE_TEXT = "REFRESH_TABLE_TEXT",
																ADD_TABLE_TEXT = "ADD_TABLE_TEXT",
																REMOVE_TABLE_TEXT = "REMOVE_TABLE_TEXT",
																DEFAULT_TABLE_NAME_TEXT = "DEFAULT_TABLE_NAME_TEXT",
																ADD_ROW_TEXT = "ADD_ROW_TEXT",
																REMOVE_ROW_TEXT = "REMOVE_ROW_TEXT",
																ADD_COLUMN_TEXT = "ADD_COLUMN_TEXT",
																REMOVE_COLUMN_TEXT = "REMOVE_COLUMN_TEXT",
																UNDO_STATEMENT_TEXT = "UNDO_STATEMENT_TEXT",
																REMOVE_FILTER_TEXT = "REMOVE_FILTER_TEXT",
																COPY_TEXT = "COPY_TEXT",
																SUBMIT_TEXT = "SUBMIT_TEXT",
																BACK_TEXT = "BACK_TEXT",
																LOG_IN_TEXT = "LOG_IN_TEXT",
																LOG_OUT_TEXT = "LOG_OUT_TEXT";
	}
	
	private static class Defaults {
		private static final String WINDOW_WIDTH = "720",
																WINDOW_HEIGHT = "480";
		private static final String	SAVED_HOST = "",
																SAVED_DATABASE = "",
																SAVED_USER = "",
																SAVED_PASSWORD = "";
		private static final String CONFIG_FILENAME = "assets/config.ini",
																STRINGS_FILENAME = "assets/lang/en.lang";
		
		private static final String WINDOW_TITLE = "SQLObViewer",
																EXCEPTION_TITLE_SUFFIX = "ERROR",
																ERROR_TITLE_SUFFIX = "FATAL ERROR",
																EXPAND_ERROR_TEXT = "CLICK TO EXPAND",
																APPLICATION_CLOSING_TEXT = "The application will now exit",
																HOST_TEXT = "Host",
																DATABASE_TEXT = "Database",
																USER_TEXT = "User",
																PASSWORD_TEXT = "Password",
																REFRESH_TABLE_TEXT = "R",
																ADD_TABLE_TEXT = "+Table",
																REMOVE_TABLE_TEXT = "-Table",
																DEFAULT_TABLE_NAME_TEXT = "TABLE_NAME",
																ADD_ROW_TEXT = "+Row",
																REMOVE_ROW_TEXT = "-Row",
																ADD_COLUMN_TEXT = "Add Column",
																REMOVE_COLUMN_TEXT = "Remove Column",
																UNDO_STATEMENT_TEXT = "Undo",
																REMOVE_FILTER_TEXT = "Remove Filter",
																COPY_TEXT = "Copy",
																SUBMIT_TEXT = "Submit",
																BACK_TEXT = "Back",
																LOG_IN_TEXT = "Log In",
																LOG_OUT_TEXT = "Log Out";
		
		private static Properties buildConfig() {
			Properties defaults = new Properties();
			
			defaults.put(Keys.WINDOW_WIDTH, WINDOW_WIDTH);
			defaults.put(Keys.WINDOW_HEIGHT, WINDOW_HEIGHT);
			
			defaults.put(Keys.SAVED_HOST, SAVED_HOST);
			defaults.put(Keys.SAVED_DATABASE, SAVED_DATABASE);
			defaults.put(Keys.SAVED_USER, SAVED_USER);
			defaults.put(Keys.SAVED_PASSWORD, SAVED_PASSWORD);
			
			defaults.put(Keys.STRINGS_FILENAME, STRINGS_FILENAME);
			
			return defaults;
		}
		private static Properties buildStrings() {
			Properties defaults = new Properties();
			
			defaults.put(Keys.WINDOW_TITLE, WINDOW_TITLE);
			defaults.put(Keys.EXCEPTION_TITLE_SUFFIX, EXCEPTION_TITLE_SUFFIX);
			defaults.put(Keys.ERROR_TITLE_SUFFIX, ERROR_TITLE_SUFFIX);
			defaults.put(Keys.EXPAND_ERROR_TEXT, EXPAND_ERROR_TEXT);
			defaults.put(Keys.APPLICATION_CLOSING_TEXT, APPLICATION_CLOSING_TEXT);
			
			defaults.put(Keys.HOST_TEXT, HOST_TEXT);
			defaults.put(Keys.USER_TEXT, USER_TEXT);
			defaults.put(Keys.DATABASE_TEXT, DATABASE_TEXT);
			defaults.put(Keys.PASSWORD_TEXT, PASSWORD_TEXT);
			defaults.put(Keys.REFRESH_TABLE_TEXT, REFRESH_TABLE_TEXT);
			defaults.put(Keys.ADD_TABLE_TEXT, ADD_TABLE_TEXT);
			defaults.put(Keys.REMOVE_TABLE_TEXT, REMOVE_TABLE_TEXT);
			defaults.put(Keys.DEFAULT_TABLE_NAME_TEXT, DEFAULT_TABLE_NAME_TEXT);
			defaults.put(Keys.ADD_ROW_TEXT, ADD_ROW_TEXT);
			defaults.put(Keys.REMOVE_ROW_TEXT, REMOVE_ROW_TEXT);
			defaults.put(Keys.ADD_COLUMN_TEXT, ADD_COLUMN_TEXT);
			defaults.put(Keys.REMOVE_COLUMN_TEXT, REMOVE_COLUMN_TEXT);
			defaults.put(Keys.UNDO_STATEMENT_TEXT, UNDO_STATEMENT_TEXT);
			defaults.put(Keys.REMOVE_FILTER_TEXT, REMOVE_FILTER_TEXT);
			defaults.put(Keys.COPY_TEXT, COPY_TEXT);
			defaults.put(Keys.SUBMIT_TEXT, SUBMIT_TEXT);
			defaults.put(Keys.BACK_TEXT, BACK_TEXT);
			defaults.put(Keys.LOG_IN_TEXT, LOG_IN_TEXT);
			defaults.put(Keys.LOG_OUT_TEXT, LOG_OUT_TEXT);

			return defaults;
		}
	}
	
	/**
	 * Provides access to configuration properties.
	 */
	public static class Config {
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
			Assets.save(config);
		}
	}
	/**
	 * Provides access to displayed strings.
	 */
	public static class Strings {		
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
