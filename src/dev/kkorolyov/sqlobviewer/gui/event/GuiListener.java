package dev.kkorolyov.sqlobviewer.gui.event;

/**
 * Listens for GUI events.
 */
public interface GuiListener {
	/**
	 * Called when the the log in button is pressed.
	 * @param host input host
	 * @param database input database
	 * @param user input user
	 * @param password input password
	 * @param GUI component calling this event
	 */
	void logInButtonPressed(String host, String database, String user, String password, GuiSubject context);
	
	/**
	 * Called when a table is selected.
	 * @param table name of selected table
	 */
	void tableSelected(String table);
}
