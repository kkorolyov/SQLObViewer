package dev.kkorolyov.sqlobviewer.gui;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
	private CreateTableScreen createTableScreen;
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
		frame.addWindowListener(new WindowListener() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void windowClosing(WindowEvent e) {
				notifyClosed();
			}
			@Override
			public void windowClosed(WindowEvent e) {
				//
			}
			@Override
			public void windowOpened(WindowEvent e) {
				//
			}
			@Override
			public void windowIconified(WindowEvent e) {
				//
			}
			@Override
			public void windowDeiconified(WindowEvent e) {
				//
			}
			@Override
			public void windowDeactivated(WindowEvent e) {
				//
			}
			@Override
			public void windowActivated(WindowEvent e) {
				//
			}
		});
	}
	
	/**
	 * Displays the login screen in this window.
	 */
	public void showLoginScreen() {
		showScreen(loginScreen, LOGIN_DIMENSION);
	}
	/**
	 * Displays the database view screen in this window.
	 */
	public void showViewScreen() {
		showScreen(viewScreen, null);
	}
	/**
	 * Displays the create table screen in this window.
	 */
	public void showCreateTableScreen() {
		showScreen(createTableScreen, null);
	}
	
	private void showScreen(JPanel toShow, Dimension size) {
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
	 * Displays an error message in this application window.
	 * @param message error message to display
	 */
	public void displayError(String message) {
		JOptionPane.showMessageDialog(frame, message, frame.getTitle(), JOptionPane.ERROR_MESSAGE);
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
		if (loginScreen != null)
			loginScreen.clearListeners();
		
		loginScreen = newLoginScreen;
		
		forwardListeners(loginScreen);
	}
	/** @param newViewScreen database view screen */
	public void setViewScreen(ViewScreen newViewScreen) {
		if (viewScreen != null)
			viewScreen.clearListeners();
		
		viewScreen = newViewScreen;
		
		forwardListeners(viewScreen);
	}
	/**	@param newCreateTableScreen create table screen */
	public void setCreateTableScreen(CreateTableScreen newCreateTableScreen) {
		createTableScreen = newCreateTableScreen;
		
		forwardListeners(createTableScreen);
	}
	
	private void notifyClosed() {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.closed(this);
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
	
	private void forwardListeners(GuiSubject subject) {
		for (GuiListener listener : listeners)
			subject.addListener(listener);
	}
}
