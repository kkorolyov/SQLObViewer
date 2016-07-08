package dev.kkorolyov.sqlobviewer.gui.table;

import dev.kkorolyov.sqlob.construct.RowEntry;

/**
 * Listens for SQL table operation requests.
 */
public interface TableRequestListener {
	/**
	 * Invoked when an {@code UPDATE} operation is requested.
	 * @param newValues new values to set
	 * @param criteria operation criteria
	 * @param context model requesting this operation
	 */
	void updateRow(RowEntry[] newValues, RowEntry[] criteria, SQLObTableModel context);
	/**
	 * Invoked when an {@code INSERT} operation is requested.
	 * @param rowValues row values to insert
	 * @param context model requesting this operation
	 */
	void insertRow(RowEntry[] rowValues, SQLObTableModel context);
	/**
	 * Invoked when a {@code DELETE} operation is requested.
	 * @param criteria operation criteria
	 * @param context model requesting this operation
	 */
	void deleteRow(RowEntry[] criteria, SQLObTableModel context);
}
