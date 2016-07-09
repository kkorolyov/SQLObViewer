package dev.kkorolyov.sqlobviewer.gui;

import javax.swing.JPanel;

/**
 * Creates and maintains a single {@code JPanel} and its components.
 */
public interface Screen {
	/** @return {@code JPanel} maintained by this {@code Screen} */
	JPanel getPanel();
}
