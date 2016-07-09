package dev.kkorolyov.sqlobviewer.gui;

/**
 * Listens for cancellation events.
 */
public interface CancelListener {
	/**
	 * Invoked when a cancel action occurs.
	 * @param context entity firing this event
	 */
	void canceled(CancelSubject context);
}
