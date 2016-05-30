package dev.kkorolyov.sqlobviewer.gui.event;

import dev.kkorolyov.sqlob.construct.RowEntry;

/**
 * Listens for GUI events.
 */
public interface GuiListener {
	/**
	 * Invoked when the the log in button is pressed.
	 * @param host input host
	 * @param database input database
	 * @param user input user
	 * @param password input password
	 * @param GUI component calling this event
	 */
	void logInButtonPressed(String host, String database, String user, String password, GuiSubject context);
	/**
	 * Invoked when the back button is pressed.
	 * Implementations may vary based on the calling subject.
	 * @param context GUI component calling this event
	 */
	void backButtonPressed(GuiSubject context);
	/**
	 * Invoked when the new table button is pressed.
	 * @param context GUI component calling this event
	 */
	void newTableButtonPressed(GuiSubject context);
	
	/**
	 * Invoked when a table is selected.
	 * @param table name of selected table
	 * @param context GUI component calling this event
	 */
	void tableSelected(String table, GuiSubject context);
	
	/**
	 * Invoked when a table {@code INSERT INTO} operation is selected.
	 * @param rowValues values of row to insert
	 * @param context GUI component calling this event
	 */
	void insertRow(RowEntry[] rowValues, GuiSubject context);
	/**
	 * Invoked when a table {@code UPDATE} operation is selected.
	 * @param newValues new values to update with
	 * @param criteria {@code UPDATE} operation criteria
	 * @param context GUI component calling this event
	 */
	void updateRows(RowEntry[] newValues, RowEntry[] criteria, GuiSubject context);
	/**
	 * Invoked when a table {@code DELETE FROM} operation is selected.
	 * @param criteria {@code DELETE FROM} operation criteria
	 * @param context GUI component calling this event
	 */
	void deleteRows(RowEntry[] criteria, GuiSubject context);
	
	/**
	 * Invoked when a GUI component is closed.
	 * @param context GUI component calling this event
	 */
	void closed(GuiSubject context);
}
