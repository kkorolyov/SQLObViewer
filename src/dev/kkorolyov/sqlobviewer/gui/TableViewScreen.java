package dev.kkorolyov.sqlobviewer.gui;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import dev.kkorolyov.sqlob.connection.TableConnection;
import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlob.construct.Results;
import dev.kkorolyov.sqlob.construct.RowEntry;
import dev.kkorolyov.sqlobviewer.gui.event.GuiListener;
import dev.kkorolyov.sqlobviewer.gui.event.GuiSubject;

/**
 * A prebuilt view of a database table.
 */
public class TableViewScreen extends JPanel implements GuiSubject {
	private static final long serialVersionUID = -7921939710945709683L;
	
	private JTable resultsTable;
	private Set<GuiListener> 	listeners = new HashSet<>(),
														listenersToRemove = new HashSet<>();
	
	/**
	 * Constructs a new table view screen.
	 * @see #rebuild(TableConnection)
	 */
	public TableViewScreen(TableConnection table) {
		BoxLayout tableViewLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(tableViewLayout);
		
		rebuild(table);
	}
	
	/**
	 * Rebuilds this screen using the specified properties.
	 * @param table table to display
	 */
	public void rebuild(TableConnection table) {
		if (table == null)
			return;
		
		removeAll();
		
		setTable(table);
		
		add(buildTableScrollPane());
		
		revalidate();
		repaint();
	}
	private JScrollPane buildTableScrollPane() {
		JScrollPane tableScrollPane = new JScrollPane(resultsTable);
		resultsTable.setFillsViewportHeight(true);
		
		return tableScrollPane;
	}
	
	/** @param newTable	new table to display */
	public void setTable(TableConnection newTable) {
		if (resultsTable == null)
			resultsTable = new JTable();
		
		String[] columnNames = extractColumnNames(newTable);
		Object[][] data = extractData(newTable);
		
		TableModel resultsModel = new AbstractTableModel() {
			private static final long serialVersionUID = 5281540525032945988L;
			
			private String[] modelColumnNames = columnNames;
			private Object[][] modelRowData = data;
			
			@Override
			public int getColumnCount() {
				return modelColumnNames.length;
			}
			@Override
			public int getRowCount() {
				return modelRowData.length;
			}
			
			@Override
			public String getColumnName(int column) {
				return modelColumnNames[column];
			}
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return getValueAt(0, columnIndex).getClass();
			}
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				return modelRowData[rowIndex][columnIndex];
			}
			
			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return true;
			}
		};
		resultsTable.setModel(resultsModel);
	}
	
	private static String[] extractColumnNames(TableConnection table) {
		Column[] columns = table.getColumns();
		String[] columnNames = new String[columns.length];
		
		for (int i = 0; i < columnNames.length; i++)
			columnNames[i] = columns[i].getName();
		
		return columnNames;
	}
	private static Object[][] extractData(TableConnection table) {
		List<Object[]> data = new LinkedList<>();

		try {
			Results allResults = table.select(null);
			
			RowEntry[] currentRow;
			while ((currentRow = allResults.getNextRow()) != null) {
				Object[] currentRowData = new Object[currentRow.length];
				
				for (int i = 0; i < currentRowData.length; i++)
					currentRowData[i] = currentRow[i].getValue();
				
				data.add(currentRowData);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return data.toArray(new Object[data.size()][]);
	}
	
	@Override
	public void addListener(GuiListener listener) {
		listeners.add(listener);
	}
	@Override
	public void removeListener(GuiListener listener) {
		listenersToRemove.add(listener);
	}
	private void removeQueuedListeners() {
		for (GuiListener listener : listenersToRemove)
			listeners.remove(listener);
		
		listenersToRemove.clear();
	}
}
