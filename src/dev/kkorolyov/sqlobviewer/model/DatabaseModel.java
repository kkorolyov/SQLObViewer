package dev.kkorolyov.sqlobviewer.model;

import javax.swing.event.ChangeListener;

import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlob.construct.RowEntry;
import dev.kkorolyov.sqlob.construct.statement.UpdateStatement;
import dev.kkorolyov.sqlobviewer.gui.event.Subject;

/**
 * Maintains the main model for the {@code SQLObViewer} application.
 */
public interface DatabaseModel extends Subject {
	/** @return name of current database connection, or {@code null} if current connection is {@code null} */
	String getDatabase();
	/** @return name of current table connection, or {@code null} if current connection is {@code null} */
	String getTable();
	/** @return tables under current database connection */
	String[] getTables();
	
	/** @return columns of current table connection, or an empty array if current connection is {@code null} */
	Column[] getTableColumns();
	/** @return data of current table connection, or an empty array if current connection is {@code null} */
	RowEntry[][] getTableData();
	
	/** @return last-executed {@code UpdateStatement}, or {@code null} if no such statement */
	UpdateStatement getLastStatement();
	
	/** @param listener change listener to add */
	void addChangeListener(ChangeListener listener);
	/** @param listener change listener to remove */
	void removeChangeListener(ChangeListener listener);
}
