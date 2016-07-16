package dev.kkorolyov.sqlobviewer.gui;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import dev.kkorolyov.sqlobviewer.gui.table.SQLObTable;
import dev.kkorolyov.sqlobviewer.gui.table.SQLObTableModel;

/**
 * A screen containing multiple {@code SQLObTables} using the same backing {@code SQLObTableModel}.
 */
public class TableGrid implements Screen {
	private SQLObTableModel model;
	private List<List<SQLObTable>> tableGrid = new ArrayList<>();
	
	private JPanel panel;
	
	/**
	 * Constructs a new tables screen with 1 displayed table.
	 * @see #TableGrid(SQLObTableModel, int, int)
	 */
	public TableGrid(SQLObTableModel model) {
		this(model, 1, 1);
	}
	/**
	 * Constructs a new tables screen.
	 * @param model table model to use
	 * @param rows number of rows of tables on this screen
	 * @param columns number of columns of tables on this screen
	 */
	public TableGrid(SQLObTableModel model, int rows, int columns) {
		panel = new JPanel();
		
		setModel(model);
		setTables(rows, columns);
	}
	
	/** @return number of rows on this screen */
	public int getRows() {
		return tableGrid.size();
	}
	/** @return number of columns on this screen */
	public int getColumns() {
		return (tableGrid.size() > 0) ? tableGrid.get(0).size() : 0;
	}
	
	/** @return table model backing all tables displayed on this screen */
	public SQLObTableModel getModel() {
		return model;
	}
	/** @param newModel new table model */
	public void setModel(SQLObTableModel newModel) {
		model = newModel;
		applyModel();
	}
	private void applyModel() {
		for (List<SQLObTable> tableRow : tableGrid) {
			for (SQLObTable table : tableRow)
				table.setModel(model);
		}
	}
	
	/** @return all tables displayed on this screen */
	public List<SQLObTable> getTables() {
		LinkedList<SQLObTable> tables = new LinkedList<>();
		
		for (List<SQLObTable> tableRow : tableGrid)
			tables.addAll(tableRow);
		
		return tables;
	}
	/**
	 * Sets the number of tables displayed by this screen.
	 * The number of tables displayed is defined as {@code rows * columns}.
	 * @param rows number of rows to display
	 * @param columns number of columns to display
	 */
	public void setTables(int rows, int columns) {		
		panel.removeAll();
		panel.setLayout(new GridLayout(rows, columns));
		
		fillGrid(rows, columns);
		trimGrid(rows, columns);

		panel.revalidate();
		panel.repaint();
	}
	private void trimGrid(int rows, int columns) {
		for (int i = (tableGrid.size() - 1); i >= rows ; i--) {
			for (SQLObTable table : tableGrid.remove(i))
				table.setModel(null);
		}
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++)
				panel.add(tableGrid.get(i).get(j).getScrollPane());	// Add current tables to panel
			
			for (int j = (tableGrid.get(i).size() - 1); j >= columns; j--)
				tableGrid.get(i).remove(j).setModel(null);
		}
	}
	private void fillGrid(int rows, int columns) {
		for (int i = tableGrid.size(); i < rows; i++)
			tableGrid.add(new ArrayList<SQLObTable>());
		
		for (int i = 0; i < rows; i++) {
			for (int j = tableGrid.get(i).size(); j < columns; j++)
				tableGrid.get(i).add(new SQLObTable(model));
		}
	}
	
	@Override
	public boolean focusDefaultComponent() {
		return false;
	}
	
	@Override
	public JPanel getPanel() {
		return panel;
	}
}
