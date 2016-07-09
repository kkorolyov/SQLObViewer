package dev.kkorolyov.sqlobviewer.gui.event;

/**
 * Listens for information submission events.
 */
public interface SubmitListener {
	/**
	 * Invoked when information has been submitted in some way.
	 * @param source entity firing this event
	 */
	void submitted(SubmitSubject source);
}
