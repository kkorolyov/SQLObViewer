package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.APPLICATION_CLOSING_TEXT;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.ERROR_TITLE_SUFFIX;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.EXCEPTION_TITLE_SUFFIX;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.EXPAND_ERROR_TEXT;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.*;

import dev.kkorolyov.sqlobviewer.assets.Assets.Strings;

/**
 * Main SQLObViewer application window.
 */
public class MainWindow implements Window {
	private String title;
	private int width,
							height;
	
	private JFrame frame;

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
	public void setScreen(Screen screen, boolean fitToScreen) {		
		frame.getContentPane().removeAll();
		frame.add(screen.getPanel());
		
		frame.setPreferredSize(fitToScreen ? null : new Dimension(width, height));
		frame.pack();
		
		frame.setVisible(true);
	}
	
	/**
	 * Displays an exception through a popup spawned by this application window.
	 * @param e exception to display
	 */
	public void displayException(Throwable e) {
		String title = frame.getTitle() + " - " + Strings.get(EXCEPTION_TITLE_SUFFIX);
		JOptionPane.showMessageDialog(frame, buildExceptionPanel(e), title, JOptionPane.WARNING_MESSAGE);
	}
	/**
	 * Displays an error through a popup spawned by this application window.
	 * @param e error to display
	 * @param terminate if {@code true} this window will dispose itself after displaying the error message
	 */
	public void displayError(Throwable e, boolean terminate) {
		String 	title = frame.getTitle() + " - " + Strings.get(ERROR_TITLE_SUFFIX);
		JOptionPane.showMessageDialog(frame, buildExceptionPanel(e), title, JOptionPane.ERROR_MESSAGE);
		
		if (terminate) {
			String closeMessage = Strings.get(APPLICATION_CLOSING_TEXT);
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
		text.setText(basicMessage + System.lineSeparator() + Strings.get(EXPAND_ERROR_TEXT));

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
