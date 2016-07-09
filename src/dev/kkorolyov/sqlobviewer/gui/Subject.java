package dev.kkorolyov.sqlobviewer.gui;

/**
 * Fires events noted by listeners.
 */
public interface Subject {
	/**
	 * Clears all listeners.
	 */
	void clearListeners();
}
