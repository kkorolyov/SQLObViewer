package dev.kkorolyov.sqlobviewer.gui.event;

/**
 * A GUI component which notifies {@code GuiListener} objects.
 */
public interface GuiSubject {
	/** @param listener listener to add */
	void addListener(GuiListener listener);
	/** @param listener listener to remove */
	void removeListener(GuiListener listener);
}
