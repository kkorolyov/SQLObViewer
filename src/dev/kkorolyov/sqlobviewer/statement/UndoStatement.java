package dev.kkorolyov.sqlobviewer.statement;

import dev.kkorolyov.sqlob.connection.DatabaseConnection;
import dev.kkorolyov.sqlob.construct.RowEntry;

/**
 * An executable command to undo a previously-executed SQL statement.
 */
public class UndoStatement {
	private static final String UPDATE_COMMAND = "UPDATE",
															DELETE_COMMAND = "DELETE",
															CREATE_TABLE_COMMAND = "CREATE TABLE";

	private String baseStatement;
	private RowEntry[] 	values,
											criteria;
	
	/**
	 * Executes this undo statement using a specified database connection.
	 * @param databaseConnection connection with which to execute statement
	 */
	public void execute(DatabaseConnection databaseConnection) {
		
	}
	
	/** @return base statement */
	public String getBaseStatement() {
		return baseStatement;
	}
	
	/** @return base statement */
	@Override
	public String toString() {
		return getBaseStatement();
	}
}
