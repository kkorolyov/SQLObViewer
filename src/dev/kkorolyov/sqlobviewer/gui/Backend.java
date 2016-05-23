package dev.kkorolyov.sqlobviewer.gui;

import dev.kkorolyov.sqlob.connection.DatabaseConnection;

/**
 * SQLObViewer backend.
 */
public class Backend {
	private DatabaseConnection dbConn;
	
	/**
	 * Constructs a new backend for a specified database connection.
	 * @param dbConn database connection
	 */
	public Backend(DatabaseConnection dbConn) {
		this.dbConn = dbConn;
	}
}
