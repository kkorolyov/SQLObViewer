package dev.kkorolyov.sqlobviewer.gui;

import java.awt.GridLayout;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import dev.kkorolyov.sqlobviewer.gui.table.SQLObTable;
import dev.kkorolyov.sqlobviewer.gui.table.SQLObTableModel;

/**
 * A screen containing multiple {@code SQLobTables} using the same backing {@code SQLObTableModel}.
 */
public class TablesScreen implements Screen {
	private SQLObTableModel model;
	private List<SQLObTable> tables = new LinkedList<>();
	
	private JPanel panel = new JPanel(new GridLayout(1, 1));
	private int rows = 1;
	
	/**
	 * Constructs a new tables screen.
	 * @param model table model to use
	 */
	public TablesScreen(SQLObTableModel model) {
		setModel(model);
	}
	
	/**
	 * Adds a new table matching this screen's current table model.
	 */
	public void spawnTable() {
		SQLObTable newTable = new SQLObTable(model);
		
		tables.add(newTable);
		
		if (tables.size() > Math.pow(rows, rows)) {
			rows++;
			panel.setLayout(new GridLayout(rows, rows));
		}
		panel.add(newTable.getScrollPane());
		
		panel.revalidate();
		panel.repaint();
	}
	
	/** @return all tables */
	public List<SQLObTable> getTables() {
		return new LinkedList<>(tables);
	}
	
	/** @return table model */
	public SQLObTableModel getModel() {
		return model;
	}
	/** @param newModel new table model */
	public void setModel(SQLObTableModel newModel) {
		//tables.clear();
		//panel.removeAll();
		
		model = newModel;
		for (SQLObTable table : tables)
			table.setModel(model);
	}
	
	@Override
	public JPanel getPanel() {
		return panel;
	}
}
