package dev.kkorolyov.sqlobviewer.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;

import dev.kkorolyov.sqlob.connection.TableConnection;

/**
 * Main SQLObViewer application window.
 */
public class MainWindow implements LoginScreenListener {
	private static final Dimension LOGIN_DIMENSION = new Dimension(240, 160);
	
	private JFrame frame;
	private LoginScreen loginScreen;
	private Set<GuiListener> 	listeners = new HashSet<>(),
														listenersToRemove = new HashSet<>();
	
	/**
	 * Constructs a new main application window.
	 * @param title frame title
	 * @param width initial width
	 * @param height initial height
	 */
	public MainWindow(String title, int width, int height) {
		buildFrame(title, width, height);		
	}
	private void buildFrame(String title, int width, int height) {
		frame = new JFrame();
		setTitle(title);
		setSize(width, height);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	/**
	 * Displays the login panel in this window.
	 */
	public void showLoginScreen() {
		showPanel(loginScreen, LOGIN_DIMENSION);
	}
	/**
	 * Displays the viewer panel in this window.
	 * @param tables all viewable tables
	 */
	public void showViewPanel(String[] tables) {
		showPanel(buildViewPanel(tables), null);
		
		if (tables.length > 0)
			notifySelectedTable(tables[0]);
	}
	
	private void showPanel(JPanel toShow, Dimension size) {
		frame.getContentPane().removeAll();
		frame.add(toShow);
		frame.revalidate();
		frame.repaint();
		
		if (size != null)
			frame.setSize(size);
		else
			frame.pack();
		
		frame.setVisible(true);
	}
	
	@Override
	public void logInPressed(String host, String database, String user,	String password, LoginScreen context) {
		notifyLogInButtonPressed(host, database, user, password);
	}
	private void notifyLogInButtonPressed(String host, String database, String user, String password) {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.logInButtonPressed(host, database, user, password, this);
	}
	
	private JPanel buildViewPanel(String[] tables) {	// TODO Extract into ViewScreen class
		JComboBox<String> tableComboBox = new JComboBox<>(tables);
		tableComboBox.setSelectedIndex(tables.length > 0 ? 0 : -1);
		tableComboBox.addActionListener(new ActionListener() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void actionPerformed(ActionEvent e) {
				@SuppressWarnings("unchecked")
				String selection = (String) ((JComboBox<String>) e.getSource()).getSelectedItem();
				
				notifySelectedTable(selection);
			}
		});
		
		JPanel tableViewPanel = new JPanel();
		BoxLayout tableViewLayout = new BoxLayout(tableViewPanel, BoxLayout.Y_AXIS);
		tableViewPanel.setLayout(tableViewLayout);
		
		JPanel viewPanel = new JPanel();
		BorderLayout viewLayout = new BorderLayout();
		viewPanel.setLayout(viewLayout);
		
		viewPanel.add(tableComboBox, BorderLayout.NORTH);
		viewPanel.add(tableViewPanel, BorderLayout.CENTER);
		
		return viewPanel;
	}
	private void notifySelectedTable(String table) {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.tableSelected(table);
	}
	/**
	 * Sets the currently-viewed table.
	 * @param newTable table to set view to
	 */
	public void setViewedTable(TableConnection newTable) {
		// TODO STUB
		JOptionPane.showMessageDialog(frame, "Called on " + newTable);
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
	/** @param newLoginScreen login screen */
	public void setLoginScreen(LoginScreen newLoginScreen) {
		loginScreen = newLoginScreen;
		loginScreen.addListener(this);
	}
}
