package dev.kkorolyov.sqlobviewer.gui;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlob.construct.RowEntry;
import dev.kkorolyov.sqlob.exceptions.MismatchedTypeException;
import dev.kkorolyov.sqlobviewer.gui.event.GuiListener;
import dev.kkorolyov.sqlobviewer.gui.event.GuiSubject;

/** A {@code JTable} displaying database information. */
public class DatabaseTable extends JTable implements GuiSubject {
	private static final long serialVersionUID = 899876032885503098L;
	
	private Column[] columns;
	private RowEntry[][] data;
	private Set<GuiListener> 	listeners = new HashSet<>(),
														listenersToRemove = new HashSet<>();
	/**
	 * Constructs a new database table.
	 * @see #rebuild(Column[], RowEntry[][])
	 */
	public DatabaseTable(Column[] columns, RowEntry[][] data) {
		setFillsViewportHeight(true);
		setAutoCreateRowSorter(true);
		
		rebuild(columns, data);
	}
	
	/**
	 * Rebuilds this table using the specified properties.
	 * @param columns table columns
	 * @param data table data
	 */
	public void rebuild(Column[] columns, RowEntry[][] data) {
		setData(columns, data);
		setModel(buildModel());
	}
	private void setData(Column[] newColumns, RowEntry[][] newData) {
		columns = newColumns;
		data = newData;
	}
	private AbstractTableModel buildModel() {
		return new AbstractTableModel() {
			private static final long serialVersionUID = 5281540525032945988L;
			
			@SuppressWarnings("synthetic-access")
			@Override
			public int getColumnCount() {
				return columns.length;
			}
			@SuppressWarnings("synthetic-access")
			@Override
			public int getRowCount() {
				return data.length;
			}
			
			@SuppressWarnings("synthetic-access")
			@Override
			public String getColumnName(int column) {
				return columns[column].getName();
			}
			
			@SuppressWarnings("synthetic-access")
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return columns[columnIndex].getType().getTypeClass();
			}
			
			@SuppressWarnings("synthetic-access")
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				return data[rowIndex][columnIndex].getValue();
			}
			@SuppressWarnings("synthetic-access")
			@Override
			public void setValueAt(Object value, int rowIndex, int columnIndex) {
				RowEntry[] criteria = saveRow(rowIndex);
				
				try {
					data[rowIndex][columnIndex] = new RowEntry(columns[columnIndex], value);
				} catch (MismatchedTypeException e) {
					throw new RuntimeException(e);
				}
				RowEntry[] newValues = new RowEntry[]{data[rowIndex][columnIndex]};	// New values after updating table value
				
				notifyUpdateRows(newValues, criteria);
			}
			
			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return true;
			}
		};
	}
	private RowEntry[] saveRow(int rowIndex) {
		RowEntry[] savedRow = new RowEntry[data[rowIndex].length];
		
		for (int i = 0; i < savedRow.length; i++)
			savedRow[i] = data[rowIndex][i];
		
		return savedRow;
	}
	
	/** @return row at the specified model index */
	public RowEntry[] getRow(int rowIndex) {
		return data[rowIndex];
	}
	/** @return row at the specified view index */
	public RowEntry[] getSelectedRow(int rowIndex) {
		return getRow(convertRowIndexToModel(rowIndex));
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
	
	private void notifyUpdateRows(RowEntry[] newValues, RowEntry[] criteria) {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.updateRows(newValues, criteria, this);
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
}
