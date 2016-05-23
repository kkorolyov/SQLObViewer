package dev.kkorolyov.sqlobviewer.setup;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import dev.kkorolyov.sqlobviewer.assets.Assets;
import dev.kkorolyov.sqlobviewer.gui.Frontend;

/**
 * Responsible for application setup.
 */
public class Setup {
	private static final String SETUP_TITLE = "SQLObViewer First-Time Setup";
	private static final String SETUP_MESSAGE_0 = "First-time application launch detected" + System.lineSeparator() + "Please proceed with setup";
	private static final String LABEL_HOST = "Host:",
															LABEL_DATABASE = "Database:",
															LABEL_USER = "User:",
															LABEL_PASSWORD = "Password:";
	
	/**
	 * Launches setup in a specified window.
	 * @param launchIn frontend window to launch setup in
	 */
	public static void setup(Frontend launchIn) {
		JFrame frame = launchIn.getFrame();
		
		JOptionPane.showMessageDialog(frame, SETUP_MESSAGE_0, SETUP_TITLE, JOptionPane.INFORMATION_MESSAGE);
		
		JTextField 	hostField = new JTextField(),
								databaseField = new JTextField();
		Object[] hostSetup = {LABEL_HOST, hostField,
													LABEL_DATABASE, databaseField};
		JOptionPane.showMessageDialog(frame, hostSetup, SETUP_TITLE, JOptionPane.QUESTION_MESSAGE);
		
		JTextField	userField = new JTextField(),
								passwordField = new JPasswordField();
		Object[] userSetup = {LABEL_USER, userField,
													LABEL_PASSWORD, passwordField};
		JOptionPane.showMessageDialog(frame, userSetup, SETUP_TITLE, JOptionPane.QUESTION_MESSAGE);
		
		String 	host = hostField.getText(),
						database = databaseField.getText(),
						user = userField.getText(),
						password = passwordField.getText();
		
		Assets.setHost(host);
		Assets.setDatabase(database);
		Assets.setUser(user);
		Assets.setPassword(password);
		
		Assets.save();
	}
}
