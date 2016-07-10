package dev.kkorolyov.sqlobviewer.gui;

import java.awt.GridLayout;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import dev.kkorolyov.sqlobviewer.gui.table.SQLObTable;
import dev.kkorolyov.sqlobviewer.gui.table.SQLObTableModel;

/**
 * A screen containing multiple {@code SQLObTables} using the same backing {@code SQLObTableModel}.
 */
public class TablesScreen implements Screen {
	private SQLObTableModel model;
	private List<SQLObTable> tables = new LinkedList<>();
	private int rows = 1,
							columns = 1;
	
	private JPanel panel = new JPanel(new GridLayout(rows, columns));
	
	/**
	 * Constructs a new tables screen with 1 displayed table.
	 * @param model table model to use
	 */
	public TablesScreen(SQLObTableModel model) {
		setModel(model);
	}
	
	/** @return number of rows on this screen */
	public int getRows() {
		return rows;
	}
	/** @return number of columns on this screen */
	public int getColumns() {
		return columns;
	}
	
	/** @return table model backing all tables displayed on this screen */
	public SQLObTableModel getModel() {
		return model;
	}
	/** @param newModel new table model */
	public void setModel(SQLObTableModel newModel) {
		if (newModel == null)
			return;
		
		model = newModel;
		
		setTables(rows, columns);
	}
	
	/** @return all tables displayed on this screen */
	public List<SQLObTable> getTables() {
		return new LinkedList<>(tables);
	}
	/**
	 * Sets the number of tables displayed by this screen.
	 * The number of tables displayed is defined as {@code rows * columns}.
	 * @param rows number of rows to display
	 * @param columns number of columns to display
	 */
	public void setTables(int rows, int columns) {
		tables.clear();
		
		panel.removeAll();
		panel.setLayout(new GridLayout(rows, columns));
		
		createTables(rows * columns);
		
		panel.revalidate();
		panel.repaint();
	}
	private void createTables(int numTables) {
		for (int i = 0; i < rows * columns; i++) {
			SQLObTable newTable = new SQLObTable(model);
			
			tables.add(newTable);
			panel.add(newTable.getScrollPane());
		}
	}
	
	@Override
	public JPanel getPanel() {
		return panel;
	}
}
