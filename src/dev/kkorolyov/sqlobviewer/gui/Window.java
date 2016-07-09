package dev.kkorolyov.sqlobviewer.gui;

/**
 * Displays a single {@code Screen} at a time.
 */
public interface Window {
	/**
	 * Displays a {@code Screen}.
	 * @param screen screen to show
	 * @param fitToScreen if {@code true}, window will be resized to fit the screen, else, the screen will be resized to fit the window
	 */
	void setScreen(Screen screen, boolean fitToScreen);
}
