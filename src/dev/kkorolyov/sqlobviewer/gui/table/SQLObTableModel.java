package dev.kkorolyov.sqlobviewer.gui.table;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
	
	private List<TableRequestListener> requestListeners = new CopyOnWriteArrayList<>();
	private List<ChangeListener> changeListeners = new CopyOnWriteArrayList<>();
	
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
		requestInsertRow(newRow);
	}
	/**
	 * @param newValues new values
	 * @param criteria criteria to match
	 */
	public void updateRow(RowEntry[] newValues, RowEntry[] criteria) {
		requestUpdateRow(newValues, criteria);
	}
	/** @param criteria criteria to match */
	public void deleteRow(RowEntry[] criteria) {
		requestDeleteRow(criteria);
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
		boolean columnsChanged = !Arrays.equals(columns, newColumns),
						dataChanged = !Arrays.deepEquals(data, newData);
		
		columns = newColumns;
		data = newData;
		
		if (columnsChanged) {
			log.debug(this + " - columns changed");
			fireTableChanged(null);
		}
		if (dataChanged)
			log.debug(this + " - data changed");
			fireStateChanged();
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
	
	/** 
	 * @param index data index
	 * @return row at the specified index
	 */
	public RowEntry[] getRow(int index) {
		return data[index];
	}
	/**
	 * @param row row to search for
	 * @return index of the first occuring match, or {@code -1} if not found
	 */
	public int getIndex(RowEntry[] row) {
		log.debug("Searching for row: " + row);
		
		for (int i = 0; i < data.length; i++) {
			if (Arrays.equals(data[i], row)) {
				log.debug("Found a matching row at index: " + i);
				return i;
			}
		}
		log.debug("Failed to find a matching row");
		return -1;
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
			
			requestUpdateRow(newValues, criteria);
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
	
	private void requestUpdateRow(RowEntry[] newValues, RowEntry[] criteria) {		
		for (TableRequestListener listener : requestListeners)
			listener.updateRow(newValues, criteria, this);
	}
	private void requestInsertRow(RowEntry[] rowValues) {		
		for (TableRequestListener listener : requestListeners)
			listener.insertRow(rowValues, this);
	}
	private void requestDeleteRow(RowEntry[] criteria) {		
		for (TableRequestListener listener : requestListeners)
			listener.deleteRow(criteria, this);
	}
	
	private void fireStateChanged() {
		for (ChangeListener listener : changeListeners)
			listener.stateChanged(new ChangeEvent(this));
	}
	
	/** @param listener	request listener to add */
	public void addRequestListener(TableRequestListener listener) {
		requestListeners.add(listener);
	}
	/** @param listener	request listener to remove */
	public void removeRequestListener(TableRequestListener listener) {
		requestListeners.remove(listener);
	}
	
	/** @param listener change listener to add */
	public void addChangeListener(ChangeListener listener) {
		changeListeners.add(listener);
	}
	/** @param listener change listener to remove */
	public void removeChangeListener(ChangeListener listener) {
		changeListeners.remove(listener);
	}
	
	/**
	 * Clears all listeners.
	 */
	public void clearListeners() {
		requestListeners.clear();
		changeListeners.clear();
	}
}
