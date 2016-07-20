package dev.kkorolyov.sqlobviewer.gui.event;

/**
 * Fires events noted by listeners.
 */
public interface Subject {
	/**
	 * Clears all listeners.
	 */
	void clearListeners();
}
