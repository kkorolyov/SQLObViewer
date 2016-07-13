package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.*;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.*;

import dev.kkorolyov.sqlobviewer.assets.Assets.Config;
import dev.kkorolyov.sqlobviewer.assets.Assets.Strings;
import dev.kkorolyov.sqlobviewer.gui.event.SubmitListener;
import dev.kkorolyov.sqlobviewer.gui.event.SubmitSubject;
import net.miginfocom.swing.MigLayout;

/**
 * The login screen.
 */
public class LoginScreen implements Screen, SubmitSubject {
	private static final int DEFAULT_FIELD_COLUMNS = 15;
	
	private JPanel panel;
	private JLabel 	hostLabel,
									databaseLabel,
									userLabel,
									passwordLabel;
	private JTextField	hostField,
											databaseField,
											userField,
											passwordField;
	private JButton loginButton;

	private Set<SubmitListener> submitListeners = new CopyOnWriteArraySet<>();
	
	/**
	 * Constructs a new login screen.
	 */
	public LoginScreen() {
		initComponents();
		addSubmitListeners();
		
		buildComponents();
	}
	private void addSubmitListeners() {
		KeyListener submitKeyListener = buildSubmitListener();
		
		panel.addKeyListener(submitKeyListener);
		hostField.addKeyListener(submitKeyListener);
		databaseField.addKeyListener(submitKeyListener);
		userField.addKeyListener(submitKeyListener);
		passwordField.addKeyListener(submitKeyListener);
	}
	private KeyListener buildSubmitListener() {
		return new KeyAdapter() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					fireSubmitted();
			}
		};
	}
	
	private void initComponents() {
		panel = new JPanel(new MigLayout("insets 4px, wrap 2", "[fill][fill, grow]", "[][][][]8px push[]"));
		
		hostLabel = new JLabel(Strings.get(HOST_TEXT));
		databaseLabel = new JLabel(Strings.get(DATABASE_TEXT));
		userLabel = new JLabel(Strings.get(USER_TEXT));
		passwordLabel = new JLabel(Strings.get(PASSWORD_TEXT));
		
		hostField = new JTextField(Config.get(SAVED_HOST), DEFAULT_FIELD_COLUMNS);
		databaseField = new JTextField(Config.get(SAVED_DATABASE), DEFAULT_FIELD_COLUMNS);
		userField = new JTextField(Config.get(SAVED_USER), DEFAULT_FIELD_COLUMNS);
		passwordField = new JPasswordField(Config.get(SAVED_PASSWORD), DEFAULT_FIELD_COLUMNS);
		
		loginButton = new JButton(Strings.get(LOG_IN_TEXT));
		loginButton.addActionListener(e -> fireSubmitted());
	}
	private void buildComponents() {
		panel.add(hostLabel);
		panel.add(hostField);
		panel.add(databaseLabel);
		panel.add(databaseField);
		panel.add(userLabel);
		panel.add(userField);
		panel.add(passwordLabel);
		panel.add(passwordField);
		panel.add(loginButton, "span, align 75%, grow 0");
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
	
	@Override
	public boolean focusDefaultComponent() {
		return hostField.requestFocusInWindow();
	}
	@Override
	public JPanel getPanel() {
		return panel;
	}
	
	private void fireSubmitted() {		
		for (SubmitListener listener : submitListeners)
			listener.submitted(this);
	}
	
	@Override
	public void addSubmitListener(SubmitListener listener) {
		submitListeners.add(listener);
	}
	@Override
	public void removeSubmitListener(SubmitListener listener) {
		submitListeners.remove(listener);
	}
	
	@Override
	public void clearListeners() {
		submitListeners.clear();
	}
}
