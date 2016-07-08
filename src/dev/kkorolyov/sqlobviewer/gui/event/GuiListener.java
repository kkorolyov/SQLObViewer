package dev.kkorolyov.sqlobviewer.gui.event;

import dev.kkorolyov.sqlob.construct.RowEntry;

/**
 * Listens for GUI events.
 */
public interface GuiListener {
	/**
	 * Invoked when the the submit button is pressed.
	 * @param context GUI component firing this event
	 */
	void submitButtonPressed(GuiSubject context);
	/**
	 * Invoked when the back button is pressed.
	 * Implementations may vary based on the calling subject.
	 * @param context GUI component firing this event
	 */
	void backButtonPressed(GuiSubject context);
	/**
	 * Invoked when the refresh table button is pressed.
	 * @param context GUI component firing this event
	 */
	void refreshTableButtonPressed(GuiSubject context);
	/**
	 * Invoked when the new table button is pressed.
	 * @param context GUI component firing this event
	 */
	void addTableButtonPressed(GuiSubject context);
	/**
	 * Invoked when the undo statement button is pressed.
	 * @param context GUI component firing this event
	 */
	void undoStatementButtonPressed(GuiSubject context);
	
	/**
	 * Invoked when a table is selected.
	 * @param table name of selected table
	 * @param context GUI component firing this event
	 */
	void tableSelected(String table, GuiSubject context);
	
	/**
	 * Invoked a {@code DROP TABLE} operation is selected.
	 * @param table name table to remove
	 * @param context GUI component firing this event
	 */
	void removeTable(String table, GuiSubject context);
	
	/**
	 * Invoked when a GUI component is closed.
	 * @param context GUI component firing this event
	 */
	void closed(GuiSubject context);
}
