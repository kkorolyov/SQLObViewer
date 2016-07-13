package dev.kkorolyov.sqlobviewer.gui.table;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import dev.kkorolyov.simplelogs.Logger;
import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlob.construct.MismatchedTypeException;
import dev.kkorolyov.sqlob.construct.RowEntry;
import dev.kkorolyov.sqlobviewer.gui.event.SqlRequestListener;
import dev.kkorolyov.sqlobviewer.gui.event.SqlRequestSubject;

/**
 * A {@code TableModel} backed by {@code SQLOb} data.
 */
public class SQLObTableModel extends AbstractTableModel implements SqlRequestSubject {
	private static final long serialVersionUID = 8155987048579413913L;
	private static final Logger log = Logger.getLogger(SQLObTableModel.class.getName());

	private List<Column> columns = new LinkedList<>();
	private List<RowEntry[]> data = new LinkedList<>();
	private boolean editable;
	
	private Set<SqlRequestListener> sqlRequestListeners = new CopyOnWriteArraySet<>();
	private Set<ChangeListener> changeListeners = new CopyOnWriteArraySet<>();
	
	/**
	 * Constructs a new model.
	 * @param columns model columns
	 * @param data model data
	 * @param isEditable whether the data in this model may be edited
	 */
	public SQLObTableModel(Column[] columns, RowEntry[][] data, boolean isEditable) {	// TODO Table name
		setData(columns, data);
		setEditable(isEditable);
	}
	
	/**
	 * @param column column index
	 * @return all unique values under the specified column, sorted in ascending order
	 */
	public Object[] getUniqueValues(int column) {
		Set<Object> uniqueValues = new TreeSet<>();
		
		for (RowEntry[] row : data)
			uniqueValues.add(row[column].getValue());
		
		log.debug("Returning " + uniqueValues.size() + " unique values for column=" + getColumnName(column).toUpperCase());
		return uniqueValues.toArray(new Object[uniqueValues.size()]);
	}
	
	/** @return all columns in this model */
	public Column[] getColumns() {
		return columns.toArray(new Column[columns.size()]);
	}
	/** @return all rows in this model */
	public RowEntry[][] getRows() {
		return data.toArray(new RowEntry[data.size()][]);
	}
	
	/**
	 * Sets this model's data.
	 * @param newColumns new columns
	 * @param newData new data
	 */
	public void setData(Column[] newColumns, RowEntry[][] newData) {
		boolean columnsChanged = !Arrays.equals(columns.toArray(), newColumns),
						dataChanged = !Arrays.deepEquals(data.toArray(), newData);
		
		columns.clear();
		for (Column column : newColumns)
			columns.add(column);
		
		data.clear();
		for (RowEntry[] datum : newData)
			data.add(datum);
		
		if (columnsChanged) {
			log.debug(this + ": COLUMNS CHANGED (current column count=" + getColumnCount() + ")");
			fireTableChanged(null);
		}
		if (dataChanged)
			log.debug(this + ": DATA CHANGED (current row count=" + getRowCount() + ")");
			fireStateChanged();
	}
	
	/** @return {@code true} if the data in this model may be edited */
	public boolean isEditable() {
		return editable;
	}
	/** @param isEditable whether the data in this model may be edited */
	public void setEditable(boolean isEditable) {
		editable = isEditable;
	}
	
	/** @return	a model with a single row of empty data matching this model's data types */
	public SQLObTableModel getEmptyTableModel() {
		return new SQLObTableModel(columns.toArray(new Column[columns.size()]), buildEmptyData(), true);
	}
	private RowEntry[][] buildEmptyData() {
		RowEntry[][] emptyData = new RowEntry[1][columns.size()];
		
		for (int i = 0; i < emptyData[0].length; i++) {
			Column currentColumn = columns.get(i);
			Object currentEmptyData = null;

			switch (currentColumn.getType()) {			
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
				emptyData[0][i] = new RowEntry(currentColumn, currentEmptyData);
			} catch (MismatchedTypeException e) {
				throw new RuntimeException(e);
			}
		}
		return emptyData;
	}
	
	@Override
	public int getColumnCount() {
		return columns.size();
	}
	@Override
	public int getRowCount() {
		return data.size();
	}
	
	/**
	 * @param index column index
	 * @return column at the specified index
	 */
	public Column getColumn(int index) {
		return columns.get(index);
	}
	@Override
	public String getColumnName(int column) {
		return columns.get(column).getName();
	}
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columns.get(columnIndex).getType().getTypeClass();
	}
	
	/** 
	 * @param index row index
	 * @return row at the specified index
	 */
	public RowEntry[] getRow(int index) {
		return data.get(index);
	}
	/**
	 * @param row row to search for
	 * @return index of the first occuring match, or {@code -1} if not found
	 */
	public int getIndex(RowEntry[] row) {
		log.debug("Searching for row: " + row);
		
		for (int i = 0; i < data.size(); i++) {
			if (Arrays.equals(data.get(i), row)) {
				log.debug("Found a matching row at index: " + i);
				return i;
			}
		}
		log.debug("Failed to find a matching row");
		return -1;
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data.get(rowIndex)[columnIndex].getValue();
	}
	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		if (!Objects.equals(getValueAt(rowIndex, columnIndex), value)) {	// No point updating with equal value
			RowEntry[] criteria = saveRow(rowIndex);
			
			try {
				data.get(rowIndex)[columnIndex] = new RowEntry(columns.get(columnIndex), value);
			} catch (MismatchedTypeException e) {
				throw new RuntimeException(e);
			}
			RowEntry[] newValues = new RowEntry[]{data.get(rowIndex)[columnIndex]};	// New values after updating table value
			
			fireTableRowsUpdated(rowIndex, rowIndex);
			requestUpdateRow(newValues, criteria);
		}
	}
	
	/**
	 * Inserts a new row
	 * @param newRow row to insert
	 */
	public void insertRow(RowEntry[] newRow) {
		data.add(newRow);
		
		fireTableRowsInserted(data.size() - 1, data.size() - 1);
		requestInsertRow(newRow);
	}
	/**
	 * Deletes all rows matching the specified criteria.
	 * @param criteria criteria to match
	 */
	public void deleteRow(RowEntry[] criteria) {
		int index;
		while ((index = getIndex(criteria)) >= 0) {
			data.remove(index);
			fireTableRowsDeleted(index, index);
		}
		requestDeleteRow(criteria);
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return editable;
	}
	
	private RowEntry[] saveRow(int rowIndex) {
		RowEntry[] savedRow = new RowEntry[data.get(rowIndex).length];
		
		for (int i = 0; i < savedRow.length; i++)
			savedRow[i] = data.get(rowIndex)[i];
		
		return savedRow;
	}
	
	private void requestUpdateRow(RowEntry[] newValues, RowEntry[] criteria) {		
		for (SqlRequestListener listener : sqlRequestListeners)
			listener.updateRow(newValues, criteria, this);
	}
	private void requestInsertRow(RowEntry[] rowValues) {		
		for (SqlRequestListener listener : sqlRequestListeners)
			listener.insertRow(rowValues, this);
	}
	private void requestDeleteRow(RowEntry[] criteria) {		
		for (SqlRequestListener listener : sqlRequestListeners)
			listener.deleteRow(criteria, this);
	}
	
	private void fireStateChanged() {
		for (ChangeListener listener : changeListeners)
			listener.stateChanged(new ChangeEvent(this));
	}
	
	/** @param listener	request listener to add */
	public void addSqlRequestListener(SqlRequestListener listener) {
		sqlRequestListeners.add(listener);
	}
	/** @param listener	request listener to remove */
	public void removeSqlRequestListener(SqlRequestListener listener) {
		sqlRequestListeners.remove(listener);
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
		sqlRequestListeners.clear();
		changeListeners.clear();
	}
}
