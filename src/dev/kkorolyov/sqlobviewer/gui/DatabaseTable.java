package dev.kkorolyov.sqlobviewer.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;

import dev.kkorolyov.simplelogs.Logger;
import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlob.construct.RowEntry;
import dev.kkorolyov.sqlob.exceptions.MismatchedTypeException;
import dev.kkorolyov.sqlobviewer.gui.event.GuiListener;
import dev.kkorolyov.sqlobviewer.gui.event.GuiSubject;

/** A {@code JTable} displaying database information. */
public class DatabaseTable extends JTable implements GuiSubject {
	private static final String REMOVE_FILTER_TEXT = "Reset Filter";
	private static final long serialVersionUID = 899876032885503098L;
	private static final Logger log = Logger.getLogger(DatabaseTable.class.getName());
	
	private Column[] columns;
	private RowEntry[][] data;
	private Set<GuiListener> 	listeners = new HashSet<>(),
														listenersToRemove = new HashSet<>();
	/**
	 * Constructs a new database table.
	 * @see #rebuild(Column[], RowEntry[][])
	 */
	@SuppressWarnings("synthetic-access")
	public DatabaseTable(Column[] columns, RowEntry[][] data) {
		setFillsViewportHeight(true);
		setAutoCreateRowSorter(true);
		addFilterPopupListener();
		
		setData(columns, data);		
		setModel(new DatabaseTableModel());
	}	
	private void addFilterPopupListener() {
		getTableHeader().addMouseListener(new MouseAdapter() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger())
					showFilterPopup(e);
			}
			@SuppressWarnings("synthetic-access")
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger())
					showFilterPopup(e);
			}
		});
	}
	
	/**
	 * Rebuilds this table using the specified properties.
	 * @param columns table columns
	 * @param data table data
	 */
	public void rebuild(Column[] columns, RowEntry[][] data) {
		setData(columns, data);
		
		sort();
	}
	private void setData(Column[] newColumns, RowEntry[][] newData) {
		columns = newColumns;
		data = newData;
	}
	
	/**
	 * Filters the table by the specified value.
	 * @param filter value to filter by
	 * @param column index of column to apply filter on
	 */
	@SuppressWarnings("unchecked")
	public void filterBy(String filter, int column) {
		String exactFilter = '^' + filter + '$';
		
		((TableRowSorter<TableModel>) getRowSorter()).setRowFilter(RowFilter.regexFilter(exactFilter, column));
	}
	/**
	 * Removes this table's current filter.
	 */
	@SuppressWarnings("unchecked")
	public void removeFilter() {
		((TableRowSorter<TableModel>) getRowSorter()).setRowFilter(null);
	}
	
	/**
	 * Sorts this table based on its sorter's current sort keys.
	 */
	@SuppressWarnings("unchecked")
	public void sort() {
		((TableRowSorter<TableModel>) getRowSorter()).sort();
	}
	
	/** @return row at the specified view index */
	public RowEntry[] getSelectedRow(int rowIndex) {
		return getRow(convertRowIndexToModel(rowIndex));
	}
	/** @return row at the specified model index */
	private RowEntry[] getRow(int rowIndex) {
		return data[rowIndex];
	}
	
	/**
	 * @param column column index
	 * @return all unique values under the specified column, sorted in ascending order
	 */
	public Object[] getUniqueValues(int column) {
		Set<Object> uniqueValues = new TreeSet<>();
		
		for (RowEntry[] row : data)
			uniqueValues.add(row[column].getValue());
		
		log.debug("Returning " + uniqueValues.size() + " unique values for column=" + columns[column].getName().toUpperCase());
		return uniqueValues.toArray(new Object[uniqueValues.size()]);
	}
	
	/** @return	an empty row of data reflective of this table's data */
	public DatabaseTable getEmptyTable() {
		return new DatabaseTable(columns, buildEmptyData());
	}
	private RowEntry[][] buildEmptyData() {
		RowEntry[][] emptyData = new RowEntry[1][columns.length];
		
		for (int i = 0; i < emptyData[0].length; i++) {
			Object currentEmptyData = null;

			switch (columns[i].getType()) {			
				case BOOLEAN:
					currentEmptyData = false;
					break;
				case SMALLINT:
					currentEmptyData = (short) 0;
					break;
				case INTEGER:
					currentEmptyData = (int) 0;
					break;
				case BIGINT:
					currentEmptyData = (long) 0;
					break;
				case REAL:
					currentEmptyData = (float) 0;
					break;
				case DOUBLE:
					currentEmptyData = (double) 0;
					break;
				case CHAR:
					currentEmptyData = ' ';
					break;
				default:
					currentEmptyData = "";
			}
			try {
				emptyData[0][i] = new RowEntry(columns[i], currentEmptyData);
			} catch (MismatchedTypeException e) {
				throw new RuntimeException(e);
			}
		}
		return emptyData;
	}
	
	private void showFilterPopup(MouseEvent e) {
		int column = getColumnModel().getColumnIndexAtX(e.getX());
		
		buildFilterPopup(column).show(e.getComponent(), e.getX(), e.getY());
	}
	private JPopupMenu buildFilterPopup(int column) {
		JPopupMenu filterPopup = new JPopupMenu();
		
		JMenuItem removeFilterItem = new JMenuItem(REMOVE_FILTER_TEXT);
		removeFilterItem.addActionListener(e -> removeFilter());
		
		filterPopup.add(removeFilterItem);
		filterPopup.addSeparator();
		
		for (Object value : getUniqueValues(column)) {
			JMenuItem currentMenuItem = new JMenuItem(value.toString());
			currentMenuItem.addActionListener(e -> filterBy(value.toString(), column));
			
			filterPopup.add(currentMenuItem);
		}
		return filterPopup;
	}
	
	private void notifyUpdateRows(RowEntry[] newValues, RowEntry[] criteria) {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.updateRows(newValues, criteria, this);
	}
	
	@Override
	public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
		super.changeSelection(rowIndex, columnIndex, toggle, extend);
		
		if (!(toggle || extend)) {
			if (editCellAt(rowIndex, columnIndex)) {
				JTextComponent editor = (JTextComponent) getEditorComponent();
				editor.requestFocusInWindow();
				editor.selectAll();
			}
		}
	}
	
	@Override
	public void addListener(GuiListener listener) {
		listeners.add(listener);
	}
	@Override
	public void removeListener(GuiListener listener) {
		listenersToRemove.add(listener);
	}
	
	@Override
	public void clearListeners() {
		listeners.clear();
	}
	
	private void removeQueuedListeners() {
		for (GuiListener listener : listenersToRemove)
			listeners.remove(listener);
		
		listenersToRemove.clear();
	}
	
	@SuppressWarnings("synthetic-access")
	private class DatabaseTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 8155987048579413913L;

		@Override
		public int getColumnCount() {
			return columns.length;
		}
		@Override
		public int getRowCount() {
			return data.length;
		}
		
		@Override
		public String getColumnName(int column) {
			return columns[column].getName();
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return columns[columnIndex].getType().getTypeClass();
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return data[rowIndex][columnIndex].getValue();
		}
		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (!Objects.equals(getValueAt(rowIndex, columnIndex), value)) {	// No point updating with equal value
				RowEntry[] criteria = saveRow(rowIndex);
				
				try {
					data[rowIndex][columnIndex] = new RowEntry(columns[columnIndex], value);
				} catch (MismatchedTypeException e) {
					throw new RuntimeException(e);
				}
				RowEntry[] newValues = new RowEntry[]{data[rowIndex][columnIndex]};	// New values after updating table value
				
				notifyUpdateRows(newValues, criteria);
			}
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}
		
		private RowEntry[] saveRow(int rowIndex) {
			RowEntry[] savedRow = new RowEntry[data[rowIndex].length];
			
			for (int i = 0; i < savedRow.length; i++)
				savedRow[i] = data[rowIndex][i];
			
			return savedRow;
		}
	}
}
