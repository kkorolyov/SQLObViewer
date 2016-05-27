package dev.kkorolyov.sqlobviewer.gui;

/**
 * Listens for login screen events.
 */
public interface LoginScreenListener {
	/**
	 * Called when this screen's log in button is pressed.
	 * @param host input host
	 * @param database input database
	 * @param user input user
	 * @param password input password
	 * @param context screen firing this event
	 */
	void logInPressed(String host, String database, String user, String password, LoginScreen context);
}
