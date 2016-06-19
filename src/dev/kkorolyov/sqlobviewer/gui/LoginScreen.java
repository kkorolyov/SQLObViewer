package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Strings.*;

import java.awt.GridLayout;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;

import dev.kkorolyov.sqlobviewer.assets.Assets;
import dev.kkorolyov.sqlobviewer.assets.Strings;
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
		hostLabel = new JLabel(Strings.get(HOST));
		databaseLabel = new JLabel(Strings.get(DATABASE));
		userLabel = new JLabel(Strings.get(USER));
		passwordLabel = new JLabel(Strings.get(PASSWORD));
		
		hostField = new JTextField(Assets.host());
		databaseField = new JTextField(Assets.database());
		userField = new JTextField(Assets.user());
		passwordField = new JPasswordField(Assets.password());
		
		loginButton = new JButton(Strings.get(LOG_IN));
		loginButton.addActionListener(e -> notifyLogInButtonPressed(hostField.getText(), databaseField.getText(), userField.getText(), passwordField.getText()));
		
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
