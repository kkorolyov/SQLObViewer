package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.MESSAGE_APPLICATION_CLOSING;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.MESSAGE_EXPAND_ERROR;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.TITLE_ERROR;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.TITLE_EXCEPTION;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.*;

import dev.kkorolyov.sqlobviewer.assets.Assets.Lang;
import dev.kkorolyov.sqlobviewer.assets.Images;

/**
 * Main SQLObViewer application window.
 */
public class MainWindow implements Window {
	private String title;
	private int width,
							height;
	
	private JFrame frame;
	private Screen currentScreen;

	/**
	 * Constructs a new main application window.
	 * @param title window title
	 * @param width initial width
	 * @param height initial height
	 */
	public MainWindow(String title, int width, int height) {
		setTitle(title);
		setSize(width, height);
		
		buildFrame();		
	}
	private void buildFrame() {
		frame = new JFrame(title);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setIconImages(Images.getMainIcons());
	}
	
	/** @param listener window listener to add */
	public void addWindowListener(WindowListener listener) {
		frame.addWindowListener(listener);
	}
	
	/**
	 * Exits this window and releases all resources.
	 */
	public void exit() {
		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	}
	
	@Override
	public Screen getScreen() {
		return currentScreen;
	}
	@Override
	public void setScreen(Screen screen, boolean fitToScreen) {
		currentScreen = screen;
		
		Point lastCenter = frame.isVisible() ? translateToCenter(frame.getLocation()) : null;
		
		frame.getContentPane().removeAll();
		frame.add(screen.getPanel());
		screen.focusDefaultComponent();
		
		frame.setPreferredSize(fitToScreen ? null : new Dimension(width, height));
		frame.pack();
		
		if (lastCenter == null) 
			frame.setLocationRelativeTo(null);
		else
			frame.setLocation(translateToCorner(lastCenter));
		
		frame.setVisible(true);
	}
	
	private Point translateToCenter(Point original) {
		original.translate(frame.getWidth() / 2, frame.getHeight() / 2);
		return original;
	}
	private Point translateToCorner(Point initial) {
		initial.translate(-1 * (frame.getWidth() / 2), -1 * (frame.getHeight() / 2));
		return initial;
	}
	
	/**
	 * Displays an exception through a popup spawned by this application window.
	 * @param e exception to display
	 */
	public void displayException(Throwable e) {
		String title = frame.getTitle() + " - " + Lang.get(TITLE_EXCEPTION);
		JOptionPane.showMessageDialog(frame, buildExceptionPanel(e), title, JOptionPane.WARNING_MESSAGE);
	}
	/**
	 * Displays an error through a popup spawned by this application window.
	 * @param e error to display
	 * @param terminate if {@code true} this window will dispose itself after displaying the error message
	 */
	public void displayError(Throwable e, boolean terminate) {
		String 	title = frame.getTitle() + " - " + Lang.get(TITLE_ERROR);
		JOptionPane.showMessageDialog(frame, buildExceptionPanel(e), title, JOptionPane.ERROR_MESSAGE);
		
		if (terminate) {
			String closeMessage = Lang.get(MESSAGE_APPLICATION_CLOSING);
			JOptionPane.showMessageDialog(frame, closeMessage, title, JOptionPane.ERROR_MESSAGE);
			
			exit();
		}
	}
	private static JPanel buildExceptionPanel(Throwable exception) {
		String 	basicMessage = (exception.getCause() == null) ? exception.getMessage() : exception.getCause().getMessage(),
						extendedMessage = basicMessage + System.lineSeparator() + buildExceptionStackString(exception);
		
		JTextArea text = new JTextArea(extendedMessage);
		JScrollPane scrollPane = new JScrollPane(text);
		scrollPane.setPreferredSize(new Dimension((int) scrollPane.getPreferredSize().getWidth(), (int) text.getPreferredSize().getHeight() / 3));
		scrollPane.getVerticalScrollBar().setUnitIncrement(8);
		
		text.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				text.setText(extendedMessage);
				text.setCaretPosition(0);
			}
		});
		text.setText(basicMessage + System.lineSeparator() + Lang.get(MESSAGE_EXPAND_ERROR));

		JPanel panel = new JPanel();
		panel.add(scrollPane);
				
		return panel;
	}
	private static String buildExceptionStackString(Throwable e) {
		StringBuilder builder = new StringBuilder();
		
		for (StackTraceElement element : e.getStackTrace())
			builder.append(element.toString()).append(System.lineSeparator());
				
		return builder.toString();
	}
	
	/** @param newTitle new title */
	public void setTitle(String newTitle) {
		title = newTitle;
	}
	/**
	 * @param newWidth new width in pixels
	 * @param newHeight new height in pixels
	 */
	public void setSize(int newWidth, int newHeight) {
		width = newWidth;
		height = newHeight;
	}
}
