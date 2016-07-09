package dev.kkorolyov.sqlobviewer.gui.event;

import dev.kkorolyov.sqlobviewer.gui.Subject;

/**
 * Fires cancellation events.
 */
public interface CancelSubject extends Subject {
	/** @param listener cancel listener to add */
	public void addCancelListener(CancelListener listener);
	/** @param listener cancel listener to remove */
	public void removeCancelListener(CancelListener listener);
}
