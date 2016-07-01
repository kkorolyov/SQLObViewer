package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.*;

import java.util.HashSet;
import java.util.Set;

import javax.swing.*;

import dev.kkorolyov.sqlobviewer.assets.Assets.Config;
import dev.kkorolyov.sqlobviewer.assets.Assets.Strings;
import dev.kkorolyov.sqlobviewer.gui.event.GuiListener;
import dev.kkorolyov.sqlobviewer.gui.event.GuiSubject;
import net.miginfocom.swing.MigLayout;

/**
 * A prebuilt login screen.
 */
public class LoginScreen extends JPanel implements GuiSubject {
	private static final long serialVersionUID = -7337254975219769022L;
	private static final int DEFAULT_FIELD_COLUMNS = 15;
	
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
		MigLayout loginLayout = new MigLayout("insets 4px, wrap 2", "[fill][fill, grow]", "[][][][]8px push[]");
		setLayout(loginLayout);
		
		initComponents();
		buildComponents();
	}
	private void initComponents() {
		hostLabel = new JLabel(Strings.get(HOST_TEXT));
		databaseLabel = new JLabel(Strings.get(DATABASE_TEXT));
		userLabel = new JLabel(Strings.get(USER_TEXT));
		passwordLabel = new JLabel(Strings.get(PASSWORD_TEXT));
		
		hostField = new JTextField(Config.get(SAVED_HOST), DEFAULT_FIELD_COLUMNS);
		databaseField = new JTextField(Config.get(SAVED_DATABASE), DEFAULT_FIELD_COLUMNS);
		userField = new JTextField(Config.get(SAVED_USER), DEFAULT_FIELD_COLUMNS);
		passwordField = new JPasswordField(Config.get(SAVED_PASSWORD), DEFAULT_FIELD_COLUMNS);
		
		loginButton = new JButton(Strings.get(LOG_IN_TEXT));
		loginButton.addActionListener(e -> notifySubmitButtonPressed());
	}
	private void buildComponents() {
		add(hostLabel);
		add(hostField);
		add(databaseLabel);
		add(databaseField);
		add(userLabel);
		add(userField);
		add(passwordLabel);
		add(passwordField);
		add(loginButton, "span, align 75%, grow 0");
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
