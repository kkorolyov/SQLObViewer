package dev.kkorolyov.sqlobviewer.gui;

import javax.swing.JPanel;

/**
 * Creates and maintains a single {@code JPanel} and its components.
 */
public interface Screen {
	/**
	 * Attempts to grant focus to the screen's default focusable component.
	 * @return {@code true} if the request is likely to succeed
	 */
	boolean focusDefaultComponent();
	
	/** @return {@code JPanel} maintained by this {@code Screen} */
	JPanel getPanel();
}
