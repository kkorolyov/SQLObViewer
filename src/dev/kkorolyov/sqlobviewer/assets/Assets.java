package dev.kkorolyov.sqlobviewer.assets;

import java.io.IOException;
import java.io.UncheckedIOException;

import dev.kkorolyov.simpleprops.Properties;

/**
 * Centralized access to all assets.
 */
public class Assets {
	@SuppressWarnings("javadoc")
	public static final String PROPERTIES_NAME = "SQLOb.ini";
	private static final String HOST = "HOST",
															DATABASE = "DATABASE",
															USER = "USER",
															PASSWORD = "PASSWORD";

	private static Properties props;

	/**
	 * Initializes all assets.
	 * @return {@code true} if properties were just initialized and must be set.
	 */
	public static boolean init() {
		boolean firstInit = false;
		
		props = Properties.getInstance((PROPERTIES_NAME));
		if (props.size() < 4) {
			initProperties();
			
			firstInit = true;
		}
		return firstInit;
	}
	private static void initProperties() {
		props.clear();
		
		props.addProperty(HOST, "");
		props.addProperty(DATABASE, "");
		props.addProperty(USER, "");
		props.addProperty(PASSWORD, "");
		
		save();
	}
	
	/** @return host value */
	public static String host() {
		return props.getValue(HOST);
	}
	/** @param newHost new host value */
	public static void setHost(String newHost) {
		props.addProperty(HOST, newHost);
	}
	
	/** @return database value */
	public static String database() {
		return props.getValue(DATABASE);
	}
	/** @param newDatabase new database value */
	public static void setDatabase(String newDatabase) {
		props.addProperty(DATABASE, newDatabase);
	}
	
	/** @return user value */
	public static String user() {
		return props.getValue(USER);
	}
	/** @param newUser new user value */
	public static void setUser(String newUser) {
		props.addProperty(USER, newUser);
	}
	
	/** @return password value */
	public static String password() {
		return props.getValue(PASSWORD);
	}
	/** @param newPassword new password value */
	public static void setPassword(String newPassword) {
		props.addProperty(PASSWORD, newPassword);
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
