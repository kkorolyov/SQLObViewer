package dev.kkorolyov.sqlobviewer.assets;

import java.io.IOException;
import java.io.UncheckedIOException;

import dev.kkorolyov.simpleprops.Properties;

/**
 * Centralized access to all strings.
 */
public class Strings {
	@SuppressWarnings("javadoc")
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

	private static Properties props;
	
	/**
	 * Initializes all assets.
	 * @return {@code true} if properties were just initialized and must be set.
	 */
	public static boolean init(String filename) {
		boolean firstInit = false;
		
		props = Properties.getInstance(filename);
		
		if (firstInit = (props.size() <= 0)) {
			initProperties(	HOST,
											DATABASE,
											USER,
											PASSWORD,
											REFRESH_TABLE,
											NEW_TABLE,
											ADD_ROW,
											DELETE_ROW,
											UNDO_STATEMENT,
											LOG_IN,
											LOG_OUT);
		}
		return firstInit;
	}
	private static void initProperties(String... newProperties) {
		props.clear();
		
		for (String newProperty : newProperties)
			props.addProperty(newProperty, "");
		
		save();
	}
	
	/**
	 * Retrieves the value for a key.
	 * @param key key to use
	 * @return value for the specified key.
	 */
	public static String get(String key) {
		return props.getValue(key) != "" ? props.getValue(key) : key;
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
