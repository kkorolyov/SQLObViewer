package dev.kkorolyov.sqlobviewer.gui;

/**
 * Listens for GUI events.
 */
public interface GuiListener {
	/**
	 * Called when the observed application window has its log in button pressed.
	 * @param host input host
	 * @param database input database
	 * @param user input user
	 * @param password input password
	 * @param context window calling this event
	 */
	void logInButtonPressed(String host, String database, String user, String password, MainWindow context);
}
