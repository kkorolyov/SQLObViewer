package dev.kkorolyov.sqlobviewer.gui.event;

/**
 * Fires options request events.
 */
public interface OptionsSubject extends Subject {
	/** @param listener options listener to add */
	void addOptionsListener(OptionsListener listener);
	/** @param listener options listener to remove */
	void removeOptionsListener(OptionsListener listener);
}
