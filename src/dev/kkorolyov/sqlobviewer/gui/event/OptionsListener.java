package dev.kkorolyov.sqlobviewer.gui.event;

/**
 * Listens for options requests.
 */
public interface OptionsListener {
	/**
	 * Invoked when options are requested.
	 * @param source entity firing this event
	 */
	void optionsRequested(OptionsSubject source);
}
