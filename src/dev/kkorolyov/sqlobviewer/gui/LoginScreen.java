package dev.kkorolyov.sqlobviewer.gui;

import java.awt.GridLayout;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;

import dev.kkorolyov.sqlobviewer.gui.event.GuiListener;
import dev.kkorolyov.sqlobviewer.gui.event.GuiSubject;

/**
 * A prebuilt login screen.
 */
public class LoginScreen extends JPanel implements GuiSubject {
	private static final long serialVersionUID = -7337254975219769022L;
	private static final String HOST_LABEL_NAME = "Host",
															DATABASE_LABEL_NAME = "Database",
															USER_LABEL_NAME = "User",
															PASSWORD_LABEL_NAME = "Password",
															LOGIN_BUTTON_NAME = "Log In";
	
	private JPanel dataPanel;
	private JLabel 	hostLabel,
									databaseLabel,
									userLabel,
									passwordLabel;
	private JTextField	hostField,
											databaseField,
											userField,
											passwordField;
	private JButton loginButton;
	private Set<GuiListener> 	listeners = new HashSet<>(),
														listenersToRemove = new HashSet<>();
	
	/**
	 * Constructs a new login screen.
	 * @see #rebuild(String, String, String, String)
	 */
	public LoginScreen(String startHost, String startDatabase, String startUser, String startPassword) {
		BoxLayout loginLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(loginLayout);
		
		rebuild(startHost, startDatabase, startUser, startPassword);
	}
	
	/**
	 * Rebuilds this screen using the specified properties.
	 * @param startHost pre-filled host value
	 * @param startDatabase pre-filled database value
	 * @param startUser pre-filled user value
	 * @param startPassword pre-filled password value
	 */
	public void rebuild(String startHost, String startDatabase, String startUser, String startPassword) {
		removeAll();
		
		setLabels(startHost, startDatabase, startUser, startPassword);
		setDataPanel();
		setButton();
		
		add(dataPanel);
		add(loginButton);
		
		revalidate();
		repaint();
	}
	private void setLabels(String startHost, String startDatabase, String startUser, String startPassword) {
		hostLabel = new JLabel(HOST_LABEL_NAME);
		databaseLabel = new JLabel(DATABASE_LABEL_NAME);
		userLabel = new JLabel(USER_LABEL_NAME);
		passwordLabel = new JLabel(PASSWORD_LABEL_NAME);
		
		hostField = new JTextField(startHost);
		databaseField = new JTextField(startDatabase);
		userField = new JTextField(startUser);
		passwordField = new JPasswordField(startPassword);
	}
	private void setDataPanel() {
		dataPanel = new JPanel();
		GridLayout dataLayout = new GridLayout(4, 2);
		dataPanel.setLayout(dataLayout);
		
		dataPanel.add(hostLabel);
		dataPanel.add(hostField);
		dataPanel.add(databaseLabel);
		dataPanel.add(databaseField);
		dataPanel.add(userLabel);
		dataPanel.add(userField);
		dataPanel.add(passwordLabel);
		dataPanel.add(passwordField);
	}
	private void setButton() {
		loginButton = new JButton(LOGIN_BUTTON_NAME);
		loginButton.addActionListener(e -> notifyLogInButtonPressed(hostField.getText(), databaseField.getText(), userField.getText(), passwordField.getText()));
	}
	
	private void notifyLogInButtonPressed(String host, String database, String user, String password) {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.logInButtonPressed(host, database, user, password, this);
	}
	
	@Override
	public void addListener(GuiListener listener) {
		listeners.add(listener);
	}
	@Override
	public void removeListener(GuiListener listener) {
		listenersToRemove.add(listener);
	}
	
	@Override
	public void clearListeners() {
		listeners.clear();
	}
	
	private void removeQueuedListeners() {
		for (GuiListener listener : listenersToRemove)
			listeners.remove(listener);
		
		listenersToRemove.clear();
	}
}
