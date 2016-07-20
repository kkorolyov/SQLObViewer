package dev.kkorolyov.sqlobviewer.gui.event;

/**
 * Fires submission events.
 */
public interface SubmitSubject extends Subject {
	/** @param listener submit listener to add */
	public void addSubmitListener(SubmitListener listener);
	/** @param listener submit listener to remove */
	public void removeSubmitListener(SubmitListener listener);
}
