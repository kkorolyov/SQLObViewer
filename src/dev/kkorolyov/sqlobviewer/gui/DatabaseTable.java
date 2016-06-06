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
	
	private Set<GuiListener> 	listeners = new HashSet<>(),
														listenersToRemove = new HashSet<>();

	/**
	 * Constructs a new database table.
	 * @see #rebuild(Column[], RowEntry[][])
	 */
	public DatabaseTable(Column[] columns, RowEntry[][] data) {
		setFillsViewportHeight(true);
		rebuild(columns, data);
	}
	
	/**
	 * Rebuilds this table using the specified properties.
	 * @param columns table columns
	 * @param data table data
	 */
	public void rebuild(Column[] columns, RowEntry[][] data) {
		setModel(buildModel(columns, data));
	}
	private AbstractTableModel buildModel(Column[] c, RowEntry[][] d) {
		return new AbstractTableModel() {
			private static final long serialVersionUID = 5281540525032945988L;
			
			private Column[] columns = c;
			private RowEntry[][] data = d;
			
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
			@SuppressWarnings("synthetic-access")
			@Override
			public void setValueAt(Object value, int rowIndex, int columnIndex) {
				RowEntry[] criteria = data[rowIndex];	// Criteria before updating table value
				
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
