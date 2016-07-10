package dev.kkorolyov.sqlobviewer.gui.event;

import dev.kkorolyov.sqlobviewer.gui.Subject;

/**
 * Fires filter change events.
 */
public interface FilterChangeSubject extends Subject {
	/** @param listener filter change listener to add */
	void addFilterChangeListener(FilterChangeListener listener);
	/** @param listener filter change listener to remove */
	void removeFilterChangeListener(FilterChangeListener listener);
}
