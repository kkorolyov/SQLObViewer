package dev.kkorolyov.sqlobviewer.gui.event;

import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlob.construct.RowEntry;

/**
 * Listens for SQL operation requests.
 */
public interface SqlRequestListener {
	/**
	 * Invoked when a {@code CREATE TABLE} operation is requested.
	 * @param name name of table to create
	 * @param columns table columns
	 * @param source entity requesting this operation
	 */
	void createTable(String name, Column[] columns, SqlRequestSubject source);
	/**
	 * Invoked when a {@code DROP TABLE} operation is request.
	 * @param table name of table to drop
	 * @param source entity requesting this operation
	 */
	void dropTable(String table, SqlRequestSubject source);
	/**
	 * Invoked when an {@code UPDATE TABLE} operation is requested.
	 * @param newValues new values to set
	 * @param criteria operation criteria
	 * @param source entity requesting this operation
	 */
	void updateRow(RowEntry[] newValues, RowEntry[] criteria, SqlRequestSubject source);	// TODO Table name as request param
	/**
	 * Invoked when an {@code INSERT INTO TABLE} operation is requested.
	 * @param rowValues row values to insert
	 * @param source entity requesting this operation
	 */
	void insertRow(RowEntry[] rowValues, SqlRequestSubject source);
	/**
	 * Invoked when a {@code DELETE FROM TABLE} operation is requested.
	 * @param criteria operation criteria
	 * @param source entity requesting this operation
	 */
	void deleteRow(RowEntry[] criteria, SqlRequestSubject source);
}
