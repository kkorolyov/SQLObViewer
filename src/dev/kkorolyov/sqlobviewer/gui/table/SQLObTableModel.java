package dev.kkorolyov.sqlobviewer.gui.table;

import java.util.*;

import javax.swing.table.AbstractTableModel;

import dev.kkorolyov.simplelogs.Logger;
import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlob.construct.MismatchedTypeException;
import dev.kkorolyov.sqlob.construct.RowEntry;

/**
 * A {@code TableModel} backed by {@code SQLOb} data.
 */
public class SQLObTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 8155987048579413913L;
	private static final Logger log = Logger.getLogger(SQLObTableModel.class.getName());

	private Column[] columns;
	private RowEntry[][] data;
	
	private Set<TableRequestListener> listeners = new HashSet<>(),
																		listenersToRemove = new HashSet<>();
	
	/**
	 * Constructs a new model.
	 * @param columns model columns
	 * @param data model data
	 */
	public SQLObTableModel(Column[] columns, RowEntry[][] data) {
		setData(columns, data);
	}
	
	/** @param newRow row to insert */
	public void insertRow(RowEntry[] newRow) {
		notifyInsertRow(newRow);
	}
	/**
	 * @param newValues new values
	 * @param criteria criteria to match
	 */
	public void updateRow(RowEntry[] newValues, RowEntry[] criteria) {
		notifyUpdateRow(newValues, criteria);
	}
	/** @param criteria criteria to match */
	public void deleteRow(RowEntry[] criteria) {
		notifyDeleteRow(criteria);
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
	
	/**
	 * Sets this model's data.
	 * @param newColumns new columns
	 * @param newData new data
	 */
	public void setData(Column[] newColumns, RowEntry[][] newData) {
		boolean headerChanged = !Arrays.equals(columns, newColumns);
		
		columns = newColumns;
		data = newData;
		
		if (headerChanged)
			fireTableChanged(null);		
	}
	
	/** @return	a model with a single row of empty data matching this model's data types */
	public SQLObTableModel getEmptyTableModel() {
		return new SQLObTableModel(columns, buildEmptyData());
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
	
	@Override
	public int getColumnCount() {
		return columns.length;
	}
	@Override
	public int getRowCount() {
		return data.length;
	}
	
	/** @return column at the specified index */
	public Column getColumn(int index) {
		return columns[index];
	}
	@Override
	public String getColumnName(int column) {
		return columns[column].getName();
	}
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columns[columnIndex].getType().getTypeClass();
	}
	
	/** @return row at the specified index */
	public RowEntry[] getRow(int index) {
		return data[index];
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
			
			notifyUpdateRow(newValues, criteria);
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
	
	private void notifyUpdateRow(RowEntry[] newValues, RowEntry[] criteria) {
		removeQueuedListeners();
		
		for (TableRequestListener listener : listeners)
			listener.updateRow(newValues, criteria, this);
	}
	private void notifyInsertRow(RowEntry[] rowValues) {
		removeQueuedListeners();
		
		for (TableRequestListener listener : listeners)
			listener.insertRow(rowValues, this);
	}
	private void notifyDeleteRow(RowEntry[] criteria) {
		removeQueuedListeners();
		
		for (TableRequestListener listener : listeners)
			listener.deleteRow(criteria, this);
	}
	
	/** @param listener	listener to add */
	public void addListener(TableRequestListener listener) {
		listeners.add(listener);
	}
	/** @param listener	listener to remove */
	public void removeListener(TableRequestListener listener) {
		listenersToRemove.add(listener);
	}
	
	/**
	 * Clears all listeners
	 */
	public void clearListeners() {
		listeners.clear();
	}
	
	private void removeQueuedListeners() {
		for (TableRequestListener listener : listenersToRemove)
			listeners.remove(listener);
		
		listenersToRemove.clear();
	}
}
