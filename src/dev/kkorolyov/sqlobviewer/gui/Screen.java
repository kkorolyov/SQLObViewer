package dev.kkorolyov.sqlobviewer.gui;

import javax.swing.JPanel;

/**
 * Creates and maintains a single {@code JPanel} and its components.
 */
public interface Screen {
	/** @return {@code JPanel} maintained by this {@code Screen} */
	JPanel getPanel();
	
	/**
	 * A preference for displaying a {@code Screen} on a {@code Window}.
	 */
	public static enum DisplayPreference {
		/** The window will be resized to match the preferred size of the screen */
		FIT_TO_SCREEN,
		/** The screen will be resized to match the preferred size of the window */
		FIT_TO_WINDOW,
		/** The screen will be shown in a separate option pane with yes-no options */
		OPTION_PANE_YES_NO,
		/** The screen will be shown in a separate option pane with yes-no-cancel options */
		OPTION_PANE_YES_NO_CANCEL,
		/** The screen will be shown in a separate option pane with ok-cancel options */
		OPTION_PANE_OK_CANCEL;
	}
}
