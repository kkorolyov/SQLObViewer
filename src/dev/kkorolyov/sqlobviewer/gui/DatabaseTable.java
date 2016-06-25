package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.REMOVE_FILTER_TEXT;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;

import dev.kkorolyov.simplelogs.Logger;
import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlob.construct.RowEntry;
import dev.kkorolyov.sqlob.exceptions.MismatchedTypeException;
import dev.kkorolyov.sqlobviewer.assets.Assets.Strings;
import dev.kkorolyov.sqlobviewer.gui.event.GuiListener;
import dev.kkorolyov.sqlobviewer.gui.event.GuiSubject;

/**
 * A {@code JTable} displaying database information. 
 */
public class DatabaseTable extends JTable implements GuiSubject {
	private static final long serialVersionUID = 899876032885503098L;
	private static final Logger log = Logger.getLogger(DatabaseTable.class.getName());
	private static final int DEFAULT_POPUP_HEIGHT = 32;
	
	private Column[] columns;
	private RowEntry[][] data;
	private Map<Integer, RowFilter<DatabaseTableModel, Integer>> filters = new HashMap<>();
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
				tryShowFilterPopup(e);
			}
			@SuppressWarnings("synthetic-access")
			@Override
			public void mouseReleased(MouseEvent e) {
				tryShowFilterPopup(e);
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
	 * Adds a filter to this table.
	 * @param filter value to filter by
	 * @param column index of column to apply filter on
	 */
	public void addFilter(String filter, int column) {
		String exactFilter = '^' + filter + '$';
		
		filters.put(column, RowFilter.regexFilter(exactFilter, column));
		log.debug("Added filter=" + exactFilter + " for column=" + columns[column].getName().toUpperCase());
		
		applyFilters();
	}
	
	/**
	 * Removes the filter for a specified column.
	 * @param column index of column to remove filter of
	 */
	public void removeFilter(int column) {		
		if (filters.remove(column) == null)
			log.debug("No filter to remove for column=" + columns[column].getName().toUpperCase());
		else
			log.debug("Removed filter for column=" + columns[column].getName().toUpperCase());

		applyFilters();
	}
	/**
	 * Removes all filters.
	 */
	public void clearFilters() {
		filters.clear();
		applyFilters();
	}
	
	private void applyFilters() {
		getCastedRowSorter().setRowFilter(RowFilter.andFilter(filters.values()));
	}
	
	/**
	 * Sorts this table based on its sorter's current sort keys.
	 */
	public void sort() {
		getCastedRowSorter().sort();
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
	
	private void tryShowFilterPopup(MouseEvent e) {
		if (e.isPopupTrigger())
			showFilterPopup(e);
	}
	private void showFilterPopup(MouseEvent e) {
		int column = getColumnModel().getColumnIndexAtX(e.getX());
		
		buildFilterPopup(column).show(e.getComponent(), e.getX(), e.getY());
	}
	private JPopupMenu buildFilterPopup(int column) {
		JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
		int popupHeight = frame == null ? DEFAULT_POPUP_HEIGHT : frame.getHeight() / DEFAULT_POPUP_HEIGHT;

		JPopupMenu filterPopup = new JScrollablePopupMenu(popupHeight);
		
		JMenuItem removeFilterItem = new JMenuItem(Strings.get(REMOVE_FILTER_TEXT));
		removeFilterItem.addActionListener(e -> removeFilter(column));
		
		filterPopup.add(removeFilterItem);
		filterPopup.addSeparator();
		
		for (Object value : getUniqueValues(column)) {
			JMenuItem currentMenuItem = new JMenuItem(value.toString());
			currentMenuItem.addActionListener(e -> addFilter(value.toString(), column));
			
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
	public boolean editCellAt(int row, int column, EventObject e) {
		boolean result = super.editCellAt(row, column, e);
		
		if (result)
			selectAll(e);
		
		return result;
	}
	private void selectAll(EventObject e) {
		JTextComponent editor = (JTextComponent) getEditorComponent();
		
		if (editor != null) {
			if (e instanceof MouseEvent) {	// If triggered by mouse, must be run on event thread
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						editor.selectAll();
					}
				});
			}
			else	
				editor.selectAll();
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
	
	@SuppressWarnings("unchecked")
	private TableRowSorter<DatabaseTableModel> getCastedRowSorter() {	// Convenience casting method
		return (TableRowSorter<DatabaseTableModel>) getRowSorter();
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
