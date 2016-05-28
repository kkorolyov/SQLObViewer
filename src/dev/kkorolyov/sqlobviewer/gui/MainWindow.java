package dev.kkorolyov.sqlobviewer.gui;

import java.awt.Dimension;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import dev.kkorolyov.sqlob.connection.TableConnection;
import dev.kkorolyov.sqlobviewer.gui.event.GuiListener;
import dev.kkorolyov.sqlobviewer.gui.event.GuiSubject;

/**
 * Main SQLObViewer application window.
 */
public class MainWindow implements GuiSubject {
	private static final Dimension LOGIN_DIMENSION = new Dimension(240, 160);
	
	private JFrame frame;
	private LoginScreen loginScreen;
	private ViewScreen viewScreen;
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
	 */
	public void showViewPanel() {
		showPanel(viewScreen, null);
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
	
	/**
	 * Sets the currently-viewed table.
	 * @param newTable table to set view to
	 */
	public void setViewedTable(TableConnection newTable) {
		viewScreen.setViewedTable(newTable);
	}
	
	/**
	 * Displays an error message in this application window.
	 * @param message error message to display
	 */
	public void displayError(String message) {
		JOptionPane.showMessageDialog(frame, message, frame.getTitle(), JOptionPane.ERROR_MESSAGE);
	}
	
	@Override
	public void addListener(GuiListener listener) {
		listeners.add(listener);
	}
	@Override
	public void removeListener(GuiListener listener) {
		listenersToRemove.add(listener);
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
		
		forwardListeners(loginScreen);
	}
	/** @param newViewScreen database view screen */
	public void setViewScreen(ViewScreen newViewScreen) {
		viewScreen = newViewScreen;
		
		forwardListeners(viewScreen);
	}
	
	private void forwardListeners(GuiSubject subject) {
		for (GuiListener listener : listeners)
			subject.addListener(listener);
	}
}
