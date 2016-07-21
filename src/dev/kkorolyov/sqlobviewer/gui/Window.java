package dev.kkorolyov.sqlobviewer.gui;

/**
 * Displays {@code Screens}.
 * @see Screen
 */
public interface Window {
	/** @return displayed screen, or {@code null} if no screen is displayed */
	Screen getScreen();
	/**
	 * Displays a {@code Screen}.
	 * @param screen screen to show
	 * @param fitToScreen if {@code true}, window will be resized to fit the screen, else, the screen will be resized to fit the window
	 */
	void setScreen(Screen screen, boolean fitToScreen);
}
