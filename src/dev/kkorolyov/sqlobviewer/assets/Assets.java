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
																TABLE_OPTIONS_TEXT = "TABLE_OPTIONS_TEXT",
																ADD_TABLE_TEXT = "ADD_TABLE_TEXT",
																ADD_TABLE_TIP = "ADD_TABLE_TIP",
																REMOVE_TABLE_TEXT = "REMOVE_TABLE_TEXT",
																REMOVE_TABLE_TIP = "REMOVE_TABLE_TIP",
																CONFIRM_REMOVE_TABLE_TEXT = "CONFIRM_REMOVE_TABLE_TEXT",
																TABLE_NAME_TIP = "TABLE_NAME_TIP",
																ROW_OPTIONS_TEXT = "ROW_OPTIONS_TEXT",
																ADD_ROW_TEXT = "ADD_ROW_TEXT",
																ADD_ROW_TIP = "ADD_ROW_TIP",
																REMOVE_ROW_TEXT = "REMOVE_ROW_TEXT",
																REMOVE_ROW_TIP = "REMOVE_ROW_TIP",
																CONFIRM_REMOVE_ROW_TEXT = "CONFIRM_REMOVE_ROW_TEXT",
																ADD_COLUMN_TEXT = "ADD_COLUMN_TEXT",
																ADD_COLUMN_TIP = "ADD_COLUMN_TIP",
																REMOVE_COLUMN_TEXT = "REMOVE_COLUMN_TEXT",
																COLUMN_NAME_TIP = "COLUMN_NAME_TIP",
																COLUMN_TYPE_TIP = "COLUMN_TYPE_TIP",
																UNDO_STATEMENT_TEXT = "UNDO_STATEMENT_TEXT",
																REMOVE_FILTER_TEXT = "REMOVE_FILTER_TEXT",
																MINIMAL_REMOVE_TEXT = "MINIMAL_REMOVE_TEXT",
																COPY_TEXT = "COPY_TEXT",
																SUBMIT_TEXT = "SUBMIT_TEXT",
																BACK_TEXT = "BACK_TEXT",
																CANCEL_TEXT = "CANCEL_TEXT",
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
																TABLE_OPTIONS_TEXT = "Table",
																ADD_TABLE_TEXT = "+",
																ADD_TABLE_TIP = "Create table",
																REMOVE_TABLE_TEXT = "-",
																REMOVE_TABLE_TIP = "Drop table",
																CONFIRM_REMOVE_TABLE_TEXT = "Are you sure you want to drop the selected table?",
																TABLE_NAME_TIP = "Table name",
																ROW_OPTIONS_TEXT = "Row",
																ADD_ROW_TEXT = "+",
																ADD_ROW_TIP = "Add row",
																REMOVE_ROW_TEXT = "-",
																REMOVE_ROW_TIP = "Delete row(s)",
																CONFIRM_REMOVE_ROW_TEXT = "Are you sure you want to drop the selected row(s)?",
																ADD_COLUMN_TEXT = "+",
																ADD_COLUMN_TIP = "Add Column",
																REMOVE_COLUMN_TEXT = "Remove Column",
																COLUMN_NAME_TIP = "Column name",
																COLUMN_TYPE_TIP = "Column type",
																UNDO_STATEMENT_TEXT = "Undo",
																REMOVE_FILTER_TEXT = "Remove Filter",
																MINIMAL_REMOVE_TEXT = "-",
																COPY_TEXT = "Copy",
																SUBMIT_TEXT = "Submit",
																BACK_TEXT = "Back",
																CANCEL_TEXT = "Cancel",
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
			defaults.put(Keys.TABLE_OPTIONS_TEXT, TABLE_OPTIONS_TEXT);
			defaults.put(Keys.ADD_TABLE_TEXT, ADD_TABLE_TEXT);
			defaults.put(Keys.ADD_TABLE_TIP, ADD_TABLE_TIP);
			defaults.put(Keys.REMOVE_TABLE_TEXT, REMOVE_TABLE_TEXT);
			defaults.put(Keys.REMOVE_TABLE_TIP, REMOVE_TABLE_TIP);
			defaults.put(Keys.CONFIRM_REMOVE_TABLE_TEXT, CONFIRM_REMOVE_TABLE_TEXT);
			defaults.put(Keys.TABLE_NAME_TIP, TABLE_NAME_TIP);
			defaults.put(Keys.ROW_OPTIONS_TEXT, ROW_OPTIONS_TEXT);
			defaults.put(Keys.ADD_ROW_TEXT, ADD_ROW_TEXT);
			defaults.put(Keys.ADD_ROW_TIP, ADD_ROW_TIP);
			defaults.put(Keys.REMOVE_ROW_TEXT, REMOVE_ROW_TEXT);
			defaults.put(Keys.REMOVE_ROW_TIP, REMOVE_ROW_TIP);
			defaults.put(Keys.CONFIRM_REMOVE_ROW_TEXT, CONFIRM_REMOVE_ROW_TEXT);
			defaults.put(Keys.ADD_COLUMN_TEXT, ADD_COLUMN_TEXT);
			defaults.put(Keys.ADD_COLUMN_TIP, ADD_COLUMN_TIP);
			defaults.put(Keys.REMOVE_COLUMN_TEXT, REMOVE_COLUMN_TEXT);
			defaults.put(Keys.COLUMN_NAME_TIP, COLUMN_NAME_TIP);
			defaults.put(Keys.COLUMN_TYPE_TIP, COLUMN_TYPE_TIP);
			defaults.put(Keys.UNDO_STATEMENT_TEXT, UNDO_STATEMENT_TEXT);
			defaults.put(Keys.REMOVE_FILTER_TEXT, REMOVE_FILTER_TEXT);
			defaults.put(Keys.MINIMAL_REMOVE_TEXT, MINIMAL_REMOVE_TEXT);
			defaults.put(Keys.COPY_TEXT, COPY_TEXT);
			defaults.put(Keys.SUBMIT_TEXT, SUBMIT_TEXT);
			defaults.put(Keys.BACK_TEXT, BACK_TEXT);
			defaults.put(Keys.CANCEL_TEXT, CANCEL_TEXT);
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
