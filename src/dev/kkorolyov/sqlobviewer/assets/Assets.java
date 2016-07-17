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
		public static final String	WINDOW_WIDTH = "WINDOW_WIDTH",	// Config keys
																WINDOW_HEIGHT = "WINDOW_HEIGHT",
																
																MAX_TABLES_X = "MAX_TABLES_X",
																MAX_TABLES_Y = "MAX_TABLES_Y",
																CURRENT_TABLES_X = "CURRENT_TABLES_X",
																CURRENT_TABLES_Y = "CURRENT_TABLES_Y",
																
																SAVED_HOST = "SAVED_HOST",
																SAVED_DATABASE = "SAVED_DATABASE",
																SAVED_USER = "SAVED_USER",
																SAVED_PASSWORD = "SAVED_PASSWORD",
																
																STRINGS_FILENAME = "LANG_FILE";
		
		public static final String 	TITLE_WINDOW = "TITLE_WINDOW",	// Lang keys
				
																TITLE_EXCEPTION = "TITLE_EXCEPTION",
																TITLE_ERROR = "TITLE_ERROR",
																
																MESSAGE_EXPAND_ERROR = "MESSAGE_EXPAND_ERROR",
																MESSAGE_APPLICATION_CLOSING = "MESSAGE_APPLICATION_CLOSING",
																
																MESSAGE_HOST = "MESSAGE_HOST",
																MESSAGE_DATABASE = "MESSAGE_DATABASE",
																MESSAGE_USER = "MESSAGE_USER",
																MESSAGE_PASSWORD = "MESSAGE_PASSWORD",
																
																MESSAGE_TIP_TABLE_NAME = "MESSAGE_TIP_TABLE_NAME",
																
																MESSAGE_TIP_COLUMN_NAME = "MESSAGE_TIP_COLUMN_NAME",
																MESSAGE_TIP_COLUMN_TYPE = "MESSAGE_TIP_COLUMN_TYPE",
																
																MESSAGE_TIP_CURRENT_FILTER = "MESSAGE_TIP_CURRENT_FILTER",
																
																MESSAGE_CONFIRM_REMOVE_TABLE = "MESSAGE_CONFIRM_REMOVE_TABLE",
																MESSAGE_CONFIRM_REMOVE_ROW = "MESSAGE_CONFIRM_REMOVE_ROW",
																
																OPTION_SUBMIT = "OPTION_SUBMIT",
																OPTION_CANCEL = "OPTION_CANCEL",
																OPTION_YES = "OPTION_YES",
																OPTION_NO = "OPTION_NO",
																
																DYNAMIC_ACTION_TABLE = "DYNAMIC_ACTION_TABLE",
																DYNAMIC_ACTION_ROW = "DYNAMIC_ACTION_ROW",
																
																ACTION_SUBMIT = "ACTION_SUBMIT",
																ACTION_BACK = "ACTION_BACK",
																ACTION_CANCEL = "ACTION_CANCEL",
																
																ACTION_COPY = "ACTION_COPY",
																
																ACTION_LOG_IN = "ACTION_LOG_IN",
																ACTION_LOG_OUT = "ACTION_LOG_OUT",
																
																ACTION_REFRESH_TABLE = "ACTION_REFRESH_TABLE",
																ACTION_TIP_REFRESH_TABLE = "ACTION_TIP_REFRESH_TABLE",
																
																ACTION_ADD_TABLE = "ACTION_ADD_TABLE",
																ACTION_TIP_ADD_TABLE = "ACTION_TIP_ADD_TABLE",
																ACTION_REMOVE_TABLE = "ACTION_REMOVE_TABLE",
																ACTION_TIP_REMOVE_TABLE = "ACTION_TIP_REMOVE_TABLE",
																
																ACTION_ADD_ROW = "ACTION_ADD_ROW",
																ACTION_TIP_ADD_ROW = "ACTION_TIP_ADD_ROW",
																ACTION_REMOVE_ROW = "ACTION_REMOVE_ROW",
																ACTION_TIP_REMOVE_ROW = "ACTION_TIP_REMOVE_ROW",
																
																ACTION_ADD_COLUMN = "ACTION_ADD_COLUMN",
																ACTION_TIP_ADD_COLUMN = "ACTION_TIP_ADD_COLUMN",
																ACTION_REMOVE_COLUMN = "ACTION_REMOVE_COLUMN",
																
																ACTION_TIP_ADD_FILTER = "ACTION_TIP_ADD_FILTER",
																ACTION_TIP_REMOVE_FILTER = "ACTION_TIP_REMOVE_FILTER",
																
																ACTION_UNDO_STATEMENT = "ACTION_UNDO_STATEMENT";
	}
	
	private static class Defaults {
		private static final String WINDOW_WIDTH = "720",	// Config defaults
																WINDOW_HEIGHT = "480",
																
																MAX_TABLES_X = "4",
																MAX_TABLES_Y = "4",
																CURRENT_TABLES_X = "1",
																CURRENT_TABLES_Y = "1",
																
																SAVED_HOST = "",
																SAVED_DATABASE = "",
																SAVED_USER = "",
																SAVED_PASSWORD = "",
																
																CONFIG_FILENAME = "assets/config.ini",
																STRINGS_FILENAME = "assets/lang/en.lang";
		
		private static final String TITLE_WINDOW = "SQLObViewer",	// Lang defaults
																
																TITLE_EXCEPTION = "ERROR",
																TITLE_ERROR = "FATAL ERROR",
																
																MESSAGE_EXPAND_ERROR = "CLICK TO EXPAND",
																MESSAGE_APPLICATION_CLOSING = "The application will now exit",
																
																MESSAGE_HOST = "Host",
																MESSAGE_DATABASE = "Database",
																MESSAGE_USER = "User",
																MESSAGE_PASSWORD = "Password",
																
																MESSAGE_TIP_TABLE_NAME = "Table name",
																
																MESSAGE_TIP_COLUMN_NAME = "Column name",
																MESSAGE_TIP_COLUMN_TYPE = "Column type",
																
																MESSAGE_TIP_CURRENT_FILTER = "Current filter",
																
																MESSAGE_CONFIRM_REMOVE_TABLE = "Are you sure you want to drop the selected table?",
																MESSAGE_CONFIRM_REMOVE_ROW = "Are you sure you want to drop the selected row(s)?",
																
																OPTION_SUBMIT = "OK",
																OPTION_CANCEL = "Cancel",
																OPTION_YES = "Yes",
																OPTION_NO = "No",
																
																DYNAMIC_ACTION_TABLE = "Table",
																DYNAMIC_ACTION_ROW = "Row",
																
																ACTION_SUBMIT = "Submit",
																ACTION_BACK = "Back",
																ACTION_CANCEL = "Cancel",
																
																ACTION_COPY = "Copy",
																
																ACTION_LOG_IN = "Log In",
																ACTION_LOG_OUT = "Log Out",
																
																ACTION_REFRESH_TABLE = "R",
																ACTION_TIP_REFRESH_TABLE = "Refresh table",
																
																ACTION_ADD_TABLE = "+",
																ACTION_TIP_ADD_TABLE = "Create table",
																ACTION_REMOVE_TABLE = "-",
																ACTION_TIP_REMOVE_TABLE = "Drop table",
																
																ACTION_ADD_ROW = "+",
																ACTION_TIP_ADD_ROW = "Add row",
																ACTION_REMOVE_ROW = "-",
																ACTION_TIP_REMOVE_ROW = "Delete row(s)",
																
																ACTION_ADD_COLUMN = "+",
																ACTION_TIP_ADD_COLUMN = "Add Column",
																ACTION_REMOVE_COLUMN = "Remove Column",
																
																ACTION_TIP_ADD_FILTER = "Set filter",
																ACTION_TIP_REMOVE_FILTER = "Remove filter",
																
																ACTION_UNDO_STATEMENT = "Undo";
																
		private static Properties buildConfig() {
			Properties defaults = new Properties();
			
			defaults.put(Keys.WINDOW_WIDTH, WINDOW_WIDTH);
			defaults.put(Keys.WINDOW_HEIGHT, WINDOW_HEIGHT);
			
			defaults.put(Keys.MAX_TABLES_X, MAX_TABLES_X);
			defaults.put(Keys.MAX_TABLES_Y, MAX_TABLES_Y);
			defaults.put(Keys.CURRENT_TABLES_X, CURRENT_TABLES_X);
			defaults.put(Keys.CURRENT_TABLES_Y, CURRENT_TABLES_Y);
			
			defaults.put(Keys.SAVED_HOST, SAVED_HOST);
			defaults.put(Keys.SAVED_DATABASE, SAVED_DATABASE);
			defaults.put(Keys.SAVED_USER, SAVED_USER);
			defaults.put(Keys.SAVED_PASSWORD, SAVED_PASSWORD);
			
			defaults.put(Keys.STRINGS_FILENAME, STRINGS_FILENAME);
			
			return defaults;
		}
		private static Properties buildStrings() {
			Properties defaults = new Properties();
			
			defaults.put(Keys.TITLE_WINDOW, TITLE_WINDOW);
			
			defaults.put(Keys.TITLE_EXCEPTION, TITLE_EXCEPTION);
			defaults.put(Keys.TITLE_ERROR, TITLE_ERROR);
			
			defaults.put(Keys.MESSAGE_EXPAND_ERROR, MESSAGE_EXPAND_ERROR);
			defaults.put(Keys.MESSAGE_APPLICATION_CLOSING, MESSAGE_APPLICATION_CLOSING);
			
			defaults.put(Keys.MESSAGE_HOST, MESSAGE_HOST);
			defaults.put(Keys.MESSAGE_USER, MESSAGE_USER);
			defaults.put(Keys.MESSAGE_DATABASE, MESSAGE_DATABASE);
			defaults.put(Keys.MESSAGE_PASSWORD, MESSAGE_PASSWORD);
			
			defaults.put(Keys.MESSAGE_TIP_TABLE_NAME, MESSAGE_TIP_TABLE_NAME);
			
			defaults.put(Keys.MESSAGE_TIP_COLUMN_NAME, MESSAGE_TIP_COLUMN_NAME);
			defaults.put(Keys.MESSAGE_TIP_COLUMN_TYPE, MESSAGE_TIP_COLUMN_TYPE);
			
			defaults.put(Keys.MESSAGE_TIP_CURRENT_FILTER, MESSAGE_TIP_CURRENT_FILTER);
			
			defaults.put(Keys.MESSAGE_CONFIRM_REMOVE_TABLE, MESSAGE_CONFIRM_REMOVE_TABLE);
			defaults.put(Keys.MESSAGE_CONFIRM_REMOVE_ROW, MESSAGE_CONFIRM_REMOVE_ROW);
			
			defaults.put(Keys.OPTION_SUBMIT, OPTION_SUBMIT);
			defaults.put(Keys.OPTION_CANCEL, OPTION_CANCEL);
			defaults.put(Keys.OPTION_YES, OPTION_YES);
			defaults.put(Keys.OPTION_NO, OPTION_NO);
			
			defaults.put(Keys.DYNAMIC_ACTION_TABLE, DYNAMIC_ACTION_TABLE);
			defaults.put(Keys.DYNAMIC_ACTION_ROW, DYNAMIC_ACTION_ROW);
			
			defaults.put(Keys.ACTION_SUBMIT, ACTION_SUBMIT);
			defaults.put(Keys.ACTION_BACK, ACTION_BACK);
			defaults.put(Keys.ACTION_CANCEL, ACTION_CANCEL);
			
			defaults.put(Keys.ACTION_COPY, ACTION_COPY);
			
			defaults.put(Keys.ACTION_LOG_IN, ACTION_LOG_IN);
			defaults.put(Keys.ACTION_LOG_OUT, ACTION_LOG_OUT);
			
			defaults.put(Keys.ACTION_REFRESH_TABLE, ACTION_REFRESH_TABLE);
			defaults.put(Keys.ACTION_TIP_REFRESH_TABLE, ACTION_TIP_REFRESH_TABLE);
			
			defaults.put(Keys.ACTION_ADD_TABLE, ACTION_ADD_TABLE);
			defaults.put(Keys.ACTION_TIP_ADD_TABLE, ACTION_TIP_ADD_TABLE);
			defaults.put(Keys.ACTION_REMOVE_TABLE, ACTION_REMOVE_TABLE);
			defaults.put(Keys.ACTION_TIP_REMOVE_TABLE, ACTION_TIP_REMOVE_TABLE);
			
			defaults.put(Keys.ACTION_ADD_ROW, ACTION_ADD_ROW);
			defaults.put(Keys.ACTION_TIP_ADD_ROW, ACTION_TIP_ADD_ROW);
			defaults.put(Keys.ACTION_REMOVE_ROW, ACTION_REMOVE_ROW);
			defaults.put(Keys.ACTION_TIP_REMOVE_ROW, ACTION_TIP_REMOVE_ROW);
			
			defaults.put(Keys.ACTION_ADD_COLUMN, ACTION_ADD_COLUMN);
			defaults.put(Keys.ACTION_TIP_ADD_COLUMN, ACTION_TIP_ADD_COLUMN);
			defaults.put(Keys.ACTION_REMOVE_COLUMN, ACTION_REMOVE_COLUMN);
			
			defaults.put(Keys.ACTION_TIP_ADD_FILTER, ACTION_TIP_ADD_FILTER);
			defaults.put(Keys.ACTION_TIP_REMOVE_FILTER, ACTION_TIP_REMOVE_FILTER);
			
			defaults.put(Keys.ACTION_UNDO_STATEMENT, ACTION_UNDO_STATEMENT);
			
			return defaults;
		}
	}
	
	/**
	 * Provides access to configuration properties.
	 */
	public static class Config {
		/**
		 * Retrieves the configuration value of a key.
		 * @param key key to use
		 * @return value of the specified key.
		 */
		public static String get(String key) {
			return Assets.get(key, config);
		}
		/**
		 * Retrieves the integer value of a key.
		 * @param key key to use
		 * @return integer value of specified key
		 */
		public static int getInt(String key) {
			return Integer.parseInt(get(key));
		}
		
		/**
		 * Sets the configuration value of a key. If such a key is not found within the loaded configuration, it is added instead.
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
	 * Provides access to language-specific strings.
	 */
	public static class Lang {		
		/**
		 * Retrieves the loaded language file's value of a key.
		 * @param key key to use
		 * @return value for the specified key.
		 */
		public static String get(String key) {
			return Assets.get(key, strings);
		}
	}
}
