package dev.kkorolyov.sqlobviewer.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.*;

import dev.kkorolyov.sqlobviewer.assets.Assets;

/**
 * Main SQLObViewer application window.
 */
public class MainWindow {
	private static final String HOST_LABEL = "Host",
															DATABASE_LABEL = "Database",
															USER_LABEL = "User",
															PASSWORD_LABEL = "Password",
															LOGIN_BUTTON = "Log In";
	private static final Dimension LOGIN_DIMENSION = new Dimension(240, 160);
	
	private JFrame frame;
	private List<GuiListener> listeners = new LinkedList<>(),
														listenersToRemove = new LinkedList<>();
	
	/**
	 * Constructs a new frontend.
	 * @param title frame title
	 * @param width initial width
	 * @param height initial height
	 */
	public MainWindow(String title, int width, int height) {
		buildFrame(title, width, height);
		
		showPanel(buildLoginPanel(), true);
	}
	private void buildFrame(String title, int width, int height) {
		frame = new JFrame();
		setTitle(title);
		setSize(width, height);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private void showPanel(JPanel toShow, boolean matchPanelSize) {
		frame.getContentPane().removeAll();
		frame.add(toShow);
		frame.revalidate();
		frame.repaint();
		
		if (matchPanelSize)
			frame.setSize(toShow.getPreferredSize());
		else
			frame.pack();
		
		frame.setVisible(true);
	}
	
	private JPanel buildLoginPanel() {		
		String 	savedHost = Assets.host(),
						savedDatabase = Assets.database(),
						savedUser = Assets.user(),
						savedPassword = Assets.password();
		
		JLabel 	hostLabel = new JLabel(HOST_LABEL),
						databaseLabel = new JLabel(DATABASE_LABEL),
						userLabel = new JLabel(USER_LABEL),
						passwordLabel = new JLabel(PASSWORD_LABEL);
		
		JTextField 	hostField = new JTextField(savedHost),
								databaseField = new JTextField(savedDatabase),
								userField = new JTextField(savedUser),
								passwordField = new JPasswordField(savedPassword);
		
		JButton loginButton = new JButton(LOGIN_BUTTON);
		loginButton.addActionListener(new ActionListener() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void actionPerformed(ActionEvent e) {
				notifyLogInButtonPressed(hostField.getText(), databaseField.getText(), userField.getText(), passwordField.getText());
			}
		});
				
		JPanel loginDataPanel = new JPanel();
		GridLayout loginDataLayout = new GridLayout(4, 2);
		loginDataPanel.setLayout(loginDataLayout);
		
		loginDataPanel.add(hostLabel);
		loginDataPanel.add(hostField);
		loginDataPanel.add(databaseLabel);
		loginDataPanel.add(databaseField);
		loginDataPanel.add(userLabel);
		loginDataPanel.add(userField);
		loginDataPanel.add(passwordLabel);
		loginDataPanel.add(passwordField);

		JPanel loginPanel = new JPanel();
		BoxLayout loginLayout = new BoxLayout(loginPanel, BoxLayout.Y_AXIS);
		loginPanel.setLayout(loginLayout);
		loginPanel.setPreferredSize(LOGIN_DIMENSION);
		
		loginPanel.add(loginDataPanel);
		loginPanel.add(loginButton);

		return loginPanel;
	}
	private void notifyLogInButtonPressed(String host, String database, String user, String password) {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.logInButtonPressed(host, database, user, password, this);
	}
	
	/**
	 * Displays an error message in this application window.
	 * @param message error message to display
	 */
	public void displayError(String message) {
		JOptionPane.showMessageDialog(frame, message, frame.getTitle(), JOptionPane.ERROR_MESSAGE);
	}
	
	/** @param listener listener to add */
	public void addListener(GuiListener listener) {
		listeners.add(listener);
	}
	/** @param listener listener to remove */
	public void removeListener(GuiListener listener) {
		listenersToRemove.add(listener);
	}
	private void removeQueuedListeners() {
		for (GuiListener listener : listenersToRemove)
			listeners.remove(listener);
		
		listenersToRemove.clear();
	}
	
	/** @param newTitle new title */
	public void setTitle(String newTitle) {
		frame.setTitle(newTitle);
	}
	/**
	 * @param newWidth new width in pixels
	 * @param newHeight new height in pixels
	 */
	public void setSize(int newWidth, int newHeight) {
		frame.setPreferredSize(new Dimension(newWidth, newHeight));
		frame.pack();
	}
}
