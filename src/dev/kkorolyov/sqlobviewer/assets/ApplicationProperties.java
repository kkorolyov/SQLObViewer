package dev.kkorolyov.sqlobviewer.assets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.util.Objects;

import dev.kkorolyov.simplelogs.Logger;
import dev.kkorolyov.simplelogs.Logger.Level;
import dev.kkorolyov.simpleprops.EncryptedProperties;
import dev.kkorolyov.simpleprops.Properties;

/**
 * Centralized access to application properties.
 */
public class ApplicationProperties {
	private static final Logger log = Logger.getLogger(ApplicationProperties.class.getName(), Level.DEBUG, (PrintWriter[]) null);
	
	private static Properties config,
														strings;
	private static byte[] key = {99, 47, 68, 0, 14};	// TODO Randomized key for new configs
	
	static {	// Should only be initialized once
		initConfig();
		initStrings();
		
		log.debug("Initialized application properties");
	}

	@SuppressWarnings("synthetic-access")
	private static void initConfig() {
		config = new EncryptedProperties(new File(Defaults.CONFIG_FILE), Defaults.buildConfig(), key);
		save(config);
		
		log.debug("Initialized config file");
	}
	@SuppressWarnings("synthetic-access")
	private static void initStrings() {
		strings = new Properties(new File(Config.get(Keys.LANG_FILE)), Defaults.buildStrings());
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
	@SuppressWarnings("javadoc")
	public static class Keys {
		public static final String	WINDOW_WIDTH = "WINDOW_WIDTH",	// Config keys
																WINDOW_HEIGHT = "WINDOW_HEIGHT",
																
																MAX_TABLES_X = "MAX_TABLES_X",
																MAX_TABLES_Y = "MAX_TABLES_Y",
																CURRENT_TABLES_X = "CURRENT_TABLES_X",
																CURRENT_TABLES_Y = "CURRENT_TABLES_Y",
																
																SAVED_HOST = "SAVED_HOST",
																SAVED_DATABASE = "SAVED_DATABASE",
																SAVED_DATABASE_TYPE = "SAVED_DATABASE_TYPE",
																SAVED_USER = "SAVED_USER",
																SAVED_PASSWORD = "SAVED_PASSWORD",
																
																ASSETS_FOLDER = "ASSETS_FOLDER",
																LANG_FILE = "LANG_FILE",
																LOG_FILE = "LOG_FILE",
																
																LOGGING_ENABLED = "LOGGING_ENABLED",
																LOGGING_LEVEL = "LOGGING_LEVEL";
		
		public static final String 	TITLE_WINDOW = "TITLE_WINDOW",	// Lang keys
				
																TITLE_EXCEPTION = "TITLE_EXCEPTION",
																TITLE_ERROR = "TITLE_ERROR",
																
																MESSAGE_EXPAND_ERROR = "MESSAGE_EXPAND_ERROR",
																MESSAGE_APPLICATION_CLOSING = "MESSAGE_APPLICATION_CLOSING",
																
																MESSAGE_HOST = "MESSAGE_HOST",
																MESSAGE_DATABASE = "MESSAGE_DATABASE",
																MESSAGE_DATABASE_TYPE = "MESSAGE_DATABASE_TYPE",
																MESSAGE_USER = "MESSAGE_USER",
																MESSAGE_PASSWORD = "MESSAGE_PASSWORD",
																
																MESSAGE_ROWS_SELECTED = "MESSAGE_ROWS_SELECTED",
																
																MESSAGE_TIP_TABLE_NAME = "MESSAGE_TIP_TABLE_NAME",
																
																MESSAGE_TIP_COLUMN_NAME = "MESSAGE_TIP_COLUMN_NAME",
																MESSAGE_TIP_COLUMN_TYPE = "MESSAGE_TIP_COLUMN_TYPE",
																
																MESSAGE_TIP_CURRENT_FILTER = "MESSAGE_TIP_CURRENT_FILTER",
																
																MESSAGE_TIP_LAST_STATEMENT = "MESSAGE_TIP_LAST_STATEMENT",
																
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
																
																ACTION_OPTIONS = "ACTION_OPTIONS",
																ACTION_OPTIONS_BACK = "ACTION_OPTIONS_BACK",
																ACTION_OPTIONS_SAVE = "ACTION_OPTIONS_SAVE",
																ACTION_OPTIONS_DISCARD = "ACTION_OPTIONS_DISCARD",
																
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
																SAVED_DATABASE_TYPE = "",
																SAVED_USER = "",
																SAVED_PASSWORD = "",
																
																ASSETS_FOLDER = "assets/",
																CONFIG_FILE = ASSETS_FOLDER + "config.ini",
																LANG_FILE = ASSETS_FOLDER + "lang/en.lang",
																LOG_FILE = "sqlobviewer.log",
																
																LOGGING_ENABLED = "false",
																LOGGING_LEVEL = "INFO";
		
		private static final String TITLE_WINDOW = "SQLObViewer",	// Lang defaults
																
																TITLE_EXCEPTION = "ERROR",
																TITLE_ERROR = "FATAL ERROR",
																
																MESSAGE_EXPAND_ERROR = "CLICK TO EXPAND",
																MESSAGE_APPLICATION_CLOSING = "The application will now exit",
																
																MESSAGE_HOST = "Host",
																MESSAGE_DATABASE = "Database",
																MESSAGE_DATABASE_TYPE = "Database Type",
																MESSAGE_USER = "User",
																MESSAGE_PASSWORD = "Password",
																
																MESSAGE_ROWS_SELECTED = "Row(s) selected",
																
																MESSAGE_TIP_TABLE_NAME = "Table name",
																
																MESSAGE_TIP_COLUMN_NAME = "Column name",
																MESSAGE_TIP_COLUMN_TYPE = "Column type",
																
																MESSAGE_TIP_CURRENT_FILTER = "Current filter",
																
																MESSAGE_TIP_LAST_STATEMENT = "Last executed statement",
																
																MESSAGE_CONFIRM_REMOVE_TABLE = "Are you sure you want to drop the selected table?",
																MESSAGE_CONFIRM_REMOVE_ROW = "Are you sure you want to delete the selected row(s)?",
																
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
																
																ACTION_OPTIONS = "Options",
																ACTION_OPTIONS_BACK = "Back",
																ACTION_OPTIONS_SAVE = "Save",
																ACTION_OPTIONS_DISCARD = "Discard changes",
																
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
			defaults.put(Keys.SAVED_DATABASE_TYPE, SAVED_DATABASE_TYPE);
			defaults.put(Keys.SAVED_USER, SAVED_USER);
			defaults.put(Keys.SAVED_PASSWORD, SAVED_PASSWORD);
			
			defaults.put(Keys.ASSETS_FOLDER, ASSETS_FOLDER);
			defaults.put(Keys.LANG_FILE, LANG_FILE);
			defaults.put(Keys.LOG_FILE, LOG_FILE);

			defaults.put(Keys.LOGGING_ENABLED, LOGGING_ENABLED);
			defaults.put(Keys.LOGGING_LEVEL, LOGGING_LEVEL);
			
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
			defaults.put(Keys.MESSAGE_DATABASE_TYPE, MESSAGE_DATABASE_TYPE);
			defaults.put(Keys.MESSAGE_PASSWORD, MESSAGE_PASSWORD);
			
			defaults.put(Keys.MESSAGE_ROWS_SELECTED, MESSAGE_ROWS_SELECTED);
			
			defaults.put(Keys.MESSAGE_TIP_TABLE_NAME, MESSAGE_TIP_TABLE_NAME);
			
			defaults.put(Keys.MESSAGE_TIP_COLUMN_NAME, MESSAGE_TIP_COLUMN_NAME);
			defaults.put(Keys.MESSAGE_TIP_COLUMN_TYPE, MESSAGE_TIP_COLUMN_TYPE);
			
			defaults.put(Keys.MESSAGE_TIP_CURRENT_FILTER, MESSAGE_TIP_CURRENT_FILTER);
			
			defaults.put(Keys.MESSAGE_TIP_LAST_STATEMENT, MESSAGE_TIP_LAST_STATEMENT);
			
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
			
			defaults.put(Keys.ACTION_OPTIONS, ACTION_OPTIONS);
			defaults.put(Keys.ACTION_OPTIONS_BACK, ACTION_OPTIONS_BACK);
			defaults.put(Keys.ACTION_OPTIONS_SAVE, ACTION_OPTIONS_SAVE);
			defaults.put(Keys.ACTION_OPTIONS_DISCARD, ACTION_OPTIONS_DISCARD);
			
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
	@SuppressWarnings("synthetic-access")
	public static class Config {
		/**
		 * Retrieves the configuration value of a key.
		 * @param key key to use
		 * @return value of the specified key.
		 */
		public static String get(String key) {
			return ApplicationProperties.get(key, config);
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
			return ApplicationProperties.set(key, value, config);
		}
		
		/**
		 * Saves current configuration.
		 */
		public static void save() {
			ApplicationProperties.save(config);
		}
		
		/** @return backing {@code Properties} object */
		public static Properties getProperties() {
			return config;
		}
	}
	/**
	 * Provides access to language-specific strings.
	 */
	@SuppressWarnings("synthetic-access")
	public static class Lang {		
		/**
		 * Retrieves the loaded language file's value of a key.
		 * @param key key to use
		 * @return value for the specified key.
		 */
		public static String get(String key) {
			return ApplicationProperties.get(key, strings);
		}
	}
}
