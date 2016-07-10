package dev.kkorolyov.sqlobviewer.gui;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import dev.kkorolyov.sqlobviewer.gui.table.SQLObTable;
import net.miginfocom.swing.MigLayout;

/**
 * A prebuilt screen displaying filter info.
 */
public class FilterInfoScreen implements Screen {
	private List<TableFilterInfoScreen> tableFilterScreens = new LinkedList<>();
	
	private JPanel panel;
	
	/** @param tables tables to display filter info for */
	public void setTables(List<SQLObTable> tables) {
		tableFilterScreens.clear();
		
		for (SQLObTable table : tables)
			tableFilterScreens.add(new TableFilterInfoScreen(table));
	}
	
	@Override
	public JPanel getPanel() {
		return panel;
	}

	private class TableFilterInfoScreen implements Screen {		
		private SQLObTable table;
		private Set<Integer> currentColumns = new HashSet<>();
		
		private JPanel panel;
		
		TableFilterInfoScreen(SQLObTable table) {
			setTable(table);
			
			initComponents();
		}
		
		private void initComponents() {
			panel = new JPanel(new MigLayout("insets 0, gap 4px", "", ""));
		}
		
		private void putFilter(int column, String columnName, String filterText) {
			removeFilter(column);
			
			JLabel text = new JLabel("[" + column + "] " + columnName + ": " + filterText);
			
			JButton removeButton = new JButton("-");
			removeButton.addActionListener(e -> table.removeFilter(column));
			
			panel.add(text, "cell 0 " + column);
			panel.add(removeButton, "cell 1 " + column);
			
			currentColumns.add(column);
		}
		private void removeFilter(int column) {
			if (currentColumns.contains(column)) {
				int firstIndex = column / 2;
				
				for (int i = 0; i < 2; i++)
					panel.remove(firstIndex);	// Components shifted
				
				currentColumns.remove(column);
			}
		}
		
		private void setTable(SQLObTable newTable) {
			table = newTable;
		}
		
		@Override
		public JPanel getPanel() {
			return panel; 
		}
	}
}
