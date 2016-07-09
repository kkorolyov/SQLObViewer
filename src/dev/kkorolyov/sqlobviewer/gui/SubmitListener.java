package dev.kkorolyov.sqlobviewer.gui;

/**
 * Listens for information submission events.
 */
public interface SubmitListener {
	/**
	 * Invoked when information has been submitted in some way.
	 * @param context entity firing this event
	 */
	void submitted(SubmitSubject context);
}
