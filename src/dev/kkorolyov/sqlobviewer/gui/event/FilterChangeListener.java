package dev.kkorolyov.sqlobviewer.gui.event;

/**
 * Listens for filter change events.
 */
public interface FilterChangeListener {
	/**
	 * Invoked when a filter is added.
	 * @param column index of column filter added to
	 * @param columnName name of column filter added to
	 * @param filterText specification of added filter
	 * @param source entity firing this event
	 */
	void filterAdded(int column, String columnName, String filterText, FilterChangeSubject source);
	/**
	 * Invoked when a filter is removed.
	 * @param column index of column filter removed from
	 * @param columnName name of column filter removed from
	 * @param filterText specification of removed filter
	 * @param source entity firing this event
	 */
	void filterRemoved(int column, String columnName, String filterText, FilterChangeSubject source);
}
