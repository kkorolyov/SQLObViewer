package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.*;

import java.awt.GridLayout;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;

import dev.kkorolyov.sqlobviewer.assets.Assets.Config;
import dev.kkorolyov.sqlobviewer.assets.Assets.Strings;
import dev.kkorolyov.sqlobviewer.gui.event.GuiListener;
import dev.kkorolyov.sqlobviewer.gui.event.GuiSubject;

/**
 * A prebuilt login screen.
 */
public class LoginScreen extends JPanel implements GuiSubject {
	private static final long serialVersionUID = -7337254975219769022L;
	
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
	 */
	public LoginScreen() {
		BoxLayout loginLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(loginLayout);
		
		initComponents();
		buildComponents();
	}
	private void initComponents() {
		hostLabel = new JLabel(Strings.get(HOST_TEXT));
		databaseLabel = new JLabel(Strings.get(DATABASE_TEXT));
		userLabel = new JLabel(Strings.get(USER_TEXT));
		passwordLabel = new JLabel(Strings.get(PASSWORD_TEXT));
		
		hostField = new JTextField(Config.get(SAVED_HOST));
		databaseField = new JTextField(Config.get(SAVED_DATABASE));
		userField = new JTextField(Config.get(SAVED_USER));
		passwordField = new JPasswordField(Config.get(SAVED_PASSWORD));
		
		loginButton = new JButton(Strings.get(LOG_IN_TEXT));
		loginButton.addActionListener(e -> notifySubmitButtonPressed());
		
		dataPanel = new JPanel();
		GridLayout dataLayout = new GridLayout(4, 2);
		dataPanel.setLayout(dataLayout);
	}
	private void buildComponents() {
		dataPanel.add(hostLabel);
		dataPanel.add(hostField);
		dataPanel.add(databaseLabel);
		dataPanel.add(databaseField);
		dataPanel.add(userLabel);
		dataPanel.add(userField);
		dataPanel.add(passwordLabel);
		dataPanel.add(passwordField);
		
		add(dataPanel);
		add(loginButton);
	}
	
	/** @return current text in host field */
	public String getHost() {
		return hostField.getText();
	}
	/** @return current text in database field */
	public String getDatabase() {
		return databaseField.getText();
	}
	/** @return current text in user field */
	public String getUser() {
		return userField.getText();
	}
	/** @return current text in password field */
	public String getPassword() {
		return passwordField.getText();
	}
	
	private void notifySubmitButtonPressed() {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.submitButtonPressed(this);
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
