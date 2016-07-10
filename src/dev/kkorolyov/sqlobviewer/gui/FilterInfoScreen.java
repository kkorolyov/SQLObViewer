package dev.kkorolyov.sqlobviewer.gui;

import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import dev.kkorolyov.sqlobviewer.gui.table.SQLObTable;

/**
 * Displays filter info for multiple {@code SQLObTables}.
 */
public class FilterInfoScreen implements Screen {
	private SQLObTable[] tables;
	private Map<SQLObTable, FilterInfoDisplay> tableFilterDisplays = new HashMap<>();
	
	private JPanel panel;
	
	/**
	 * Constructs a new filter info screen.
	 * @param tables tables to display filter info on
	 */
	public FilterInfoScreen(SQLObTable[] tables) {
		panel = new JPanel(new GridLayout(3, 0));

		setTables(tables);
	}
	
	/** @param newTables tables to display filter info on */
	public void setTables(SQLObTable[] newTables) {
		if (newTables == null)
			return;
		
		tableFilterDisplays.clear();
		
		tables = newTables;
		
		for (SQLObTable table : tables)
			tableFilterDisplays.put(table, new FilterInfoDisplay(table));
		
		buildPanel();
	}	
	private void buildPanel() {		
		for (FilterInfoDisplay display : tableFilterDisplays.values())
			panel.add(display.getPanel());
	}
	
	@Override
	public JPanel getPanel() {
		return panel;
	}
}
