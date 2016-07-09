package dev.kkorolyov.sqlobviewer.gui;

/**
 * Fires cancellation events.
 */
public interface CancelSubject extends Subject {
	/** @param listener cancel listener to add */
	public void addCancelListener(CancelListener listener);
	/** @param listener cancel listener to remove */
	public void removeCancelListener(CancelListener listener);
}
