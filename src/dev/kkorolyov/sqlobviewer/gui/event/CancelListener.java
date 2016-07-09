package dev.kkorolyov.sqlobviewer.gui.event;

/**
 * Listens for cancellation events.
 */
public interface CancelListener {
	/**
	 * Invoked when a cancel action occurs.
	 * @param source entity firing this event
	 */
	void canceled(CancelSubject source);
}
