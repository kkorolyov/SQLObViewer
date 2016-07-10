package dev.kkorolyov.sqlobviewer.gui;

import java.awt.GridLayout;

import javax.swing.JPanel;

import dev.kkorolyov.sqlobviewer.gui.event.FilterChangeListener;
import dev.kkorolyov.sqlobviewer.gui.event.FilterChangeSubject;
import dev.kkorolyov.sqlobviewer.gui.table.SQLObTable;

/**
 * Displays filter info for a single {@code SQLObTable}.
 */
public class FilterInfoDisplay implements Screen, FilterChangeListener {		
	private SQLObTable table;
	private FilterInfoBlock[] filterDisplays;
	
	private JPanel panel;
	
	/**
	 * Constructs a new table filter info display.
	 * @param table table to display filter info on
	 */
	public FilterInfoDisplay(SQLObTable table) {
		initComponents();

		setTable(table);	
	}
	
	private void initComponents() {
		panel = new JPanel(new GridLayout(0, 1));
	}
	
	/**
	 * Adds info for a filter to the display.
	 * Overwrites any preexisting filter info for the same column.
	 * @param column index of filtered column
	 * @param columnName name of filtered column
	 * @param filterText text used to filter
	 */
	public void putFilter(int column, String columnName, String filterText) {		
		FilterInfoBlock newFilterDisplay = new FilterInfoBlock(column, columnName, filterText);
		newFilterDisplay.addRemovalListener(e -> table.removeFilter(column));
		
		filterDisplays[column] = newFilterDisplay;
		
		applyFilterDisplays();
	}
	/**
	 * Removes info for a filter.
	 * @param column index of column of filter info to remove
	 */
	public void removeFilter(int column) {
		filterDisplays[column] = null;
		
		applyFilterDisplays();
	}
	
	private void applyFilterDisplays() {
		panel.removeAll();
		
		for (FilterInfoBlock display : filterDisplays) {
			if (display != null)
				panel.add(display.getPanel());
		}
		panel.revalidate();
		panel.repaint();
	}
	
	/** @param newTable new table to display filter info on */
	public void setTable(SQLObTable newTable) {
		table = newTable;
		table.addFilterChangeListener(this);
		
		filterDisplays = new FilterInfoBlock[table.getColumnCount()];
	}
	
	@Override
	public JPanel getPanel() {
		return panel; 
	}

	@Override
	public void filterAdded(int column, String columnName, String filterText, FilterChangeSubject source) {
		if (source == table)
			putFilter(column, columnName, filterText);
	}

	@Override
	public void filterRemoved(int column, String columnName, String filterText,	FilterChangeSubject source) {
		if (source == table)
			removeFilter(column);
	}
}
