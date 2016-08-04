package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.ApplicationProperties.Keys.*;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.*;

import dev.kkorolyov.sqlob.connection.DatabaseConnection.DatabaseType;
import dev.kkorolyov.sqlobviewer.assets.ApplicationProperties.Config;
import dev.kkorolyov.sqlobviewer.assets.ApplicationProperties.Lang;
import dev.kkorolyov.sqlobviewer.assets.Asset;
import dev.kkorolyov.sqlobviewer.gui.event.OptionsListener;
import dev.kkorolyov.sqlobviewer.gui.event.OptionsSubject;
import dev.kkorolyov.sqlobviewer.gui.event.SubmitListener;
import dev.kkorolyov.sqlobviewer.gui.event.SubmitSubject;
import net.miginfocom.swing.MigLayout;

/**
 * The login screen.
 */
public class LoginScreen implements Screen, SubmitSubject, OptionsSubject {
	private static final int DEFAULT_FIELD_COLUMNS = 15;
	
	private JPanel panel;
	private JLabel 	hostLabel,
									databaseLabel,
									databaseTypeLabel,
									userLabel,
									passwordLabel;
	private JTextField	hostField,
											databaseField,
											userField,
											passwordField;
	private JComboBox<DatabaseType> databaseTypeComboBox;
	private JButton loginButton,
									optionsButton;

	private Set<SubmitListener> submitListeners = new CopyOnWriteArraySet<>();
	private Set<OptionsListener> optionsListeners = new CopyOnWriteArraySet<>();
	
	/**
	 * Constructs a new login screen.
	 */
	public LoginScreen() {
		initComponents();
		
		buildComponents();
	}
	@SuppressWarnings("synthetic-access")
	private void initComponents() {
		panel = new JPanel(new MigLayout("insets 4px, wrap 2", "[fill][fill, grow]", "[][][][][]8px push[]"));
		
		hostLabel = new JLabel(Lang.get(MESSAGE_HOST));
		databaseLabel = new JLabel(Lang.get(MESSAGE_DATABASE));
		databaseTypeLabel = new JLabel(Lang.get(MESSAGE_DATABASE_TYPE));
		userLabel = new JLabel(Lang.get(MESSAGE_USER));
		passwordLabel = new JLabel(Lang.get(MESSAGE_PASSWORD));
		
		hostField = new JTextField(Config.get(SAVED_HOST), DEFAULT_FIELD_COLUMNS);
		databaseField = new JTextField(Config.get(SAVED_DATABASE), DEFAULT_FIELD_COLUMNS);
		userField = new JTextField(Config.get(SAVED_USER), DEFAULT_FIELD_COLUMNS);
		passwordField = new JPasswordField(Config.get(SAVED_PASSWORD), DEFAULT_FIELD_COLUMNS);
		
		databaseTypeComboBox = new JComboBox<>(DatabaseType.values());
		databaseTypeComboBox.setBackground(databaseField.getBackground());
		
		loginButton = new JButton(Lang.get(ACTION_LOG_IN));
		loginButton.addActionListener(e -> fireSubmitted());
		
		optionsButton = new JButton(Asset.OPTIONS_ICON.asIcon());
		optionsButton.setToolTipText(Lang.get(ACTION_OPTIONS));
		optionsButton.addActionListener(e -> fireOptions());
		
		KeyListener submitKeyListener = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					fireSubmitted();
			}
		};
		panel.addKeyListener(submitKeyListener);
		hostField.addKeyListener(submitKeyListener);
		databaseField.addKeyListener(submitKeyListener);
		userField.addKeyListener(submitKeyListener);
		passwordField.addKeyListener(submitKeyListener);
	}
	private void buildComponents() {
		panel.add(hostLabel);
		panel.add(hostField);
		panel.add(databaseLabel);
		panel.add(databaseField);
		panel.add(databaseTypeLabel);
		panel.add(databaseTypeComboBox);
		panel.add(userLabel);
		panel.add(userField);
		panel.add(passwordLabel);
		panel.add(passwordField);
		panel.add(optionsButton, "span, split 2, align 25%, grow 0");
		panel.add(loginButton, "align 75%, grow 0");
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
	
	/** @return selected item in database type combo box */
	public DatabaseType getDatabaseType() {
		return (DatabaseType) databaseTypeComboBox.getSelectedItem();
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
	
	private void fireOptions() {
		for (OptionsListener listener : optionsListeners)
			listener.optionsRequested(this);
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
	public void addOptionsListener(OptionsListener listener) {
		optionsListeners.add(listener);
	}
	@Override
	public void removeOptionsListener(OptionsListener listener) {
		optionsListeners.remove(listener);
	}
	
	@Override
	public void clearListeners() {
		submitListeners.clear();
		optionsListeners.clear();
	}
}
