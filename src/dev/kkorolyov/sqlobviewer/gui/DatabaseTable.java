package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.COPY_TEXT;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.REMOVE_FILTER_TEXT;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;

import dev.kkorolyov.simplelogs.Logger;
import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlob.construct.MismatchedTypeException;
import dev.kkorolyov.sqlob.construct.RowEntry;
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
	
	private JScrollPane scrollPane;
	
	private Set<GuiListener> 	listeners = new HashSet<>(),
														listenersToRemove = new HashSet<>();
	
	/**
	 * Constructs an empty table with no data.
	 * @see #DatabaseTable(Column[], RowEntry[][])
	 */
	public DatabaseTable() {
		this(new Column[0], new RowEntry[0][0]);
	}
	/**
	 * Constructs a new database table.
	 * @param columns table columns
	 * @param data table data
	 */
	@SuppressWarnings("synthetic-access")
	public DatabaseTable(Column[] columns, RowEntry[][] data) {
		setPreferredScrollableViewportSize(new Dimension((int) getPreferredScrollableViewportSize().getWidth(), getRowHeight()));
		setFillsViewportHeight(true);
		
		setAutoCreateRowSorter(true);
		
		addDeselectionListeners();
		addHeaderPopupListener();
		addCellPopupListener();
		
		setData(columns, data);		
		setModel(new DatabaseTableModel());
		
		scrollPane = new JScrollPane(this);
	}
	private void addDeselectionListeners() {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {				
				if (rowAtPoint(e.getPoint()) < 0)
					deselect();
			}
		});
		addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				// Ignore
			}
			@Override
			public void keyReleased(KeyEvent e) {
				// Ignore
			}
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					deselect();
			}
		});
	}
	private void addHeaderPopupListener() {
		getTableHeader().addMouseListener(new MouseAdapter() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void mousePressed(MouseEvent e) {
				tryShowHeaderPopup(e);
			}
			@SuppressWarnings("synthetic-access")
			@Override
			public void mouseReleased(MouseEvent e) {
				tryShowHeaderPopup(e);
			}
		});
	}
	@SuppressWarnings("synthetic-access")
	private void addCellPopupListener() {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				tryShowCellPopup(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				tryShowCellPopup(e);
			}
		});
	}
	
	/**
	 * Deselects any currently-selected data in this table.
	 * Cancels any pending cell editing.
	 */
	public void deselect() {
		int selectedRow = getSelectedRow(),
				selectedColumn = getSelectedColumn();
		
		if (selectedRow >= 0 || selectedColumn >= 0) {	// Something is selected
			clearSelection();
			
			if (getCellEditor() != null)
				getCellEditor().cancelCellEditing();
		}
	}
	
	/**
	 * Sets the displayed data of this table.
	 * @param newColumns new table columns
	 * @param newData new table data
	 */
	public void setData(Column[] newColumns, RowEntry[][] newData) {
		boolean headerChanged = !Arrays.equals(columns, newColumns);
		
		columns = newColumns;
		data = newData;
		
		if (headerChanged)
			((AbstractTableModel) getModel()).fireTableChanged(null);
		
		sort();	
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
	
	/*** @return auto-updating {@code JScrollPane} containing this table */
	public JScrollPane getScrollPane() {
		return scrollPane;
	}
	
	private void tryShowCellPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			int row = rowAtPoint(e.getPoint()),
					column = columnAtPoint(e.getPoint());
			
			if (row >= 0 && column >= 0) {
				changeSelection(row, column, false, false);
				showCellPopup(getValueAt(row, column), e);
			}
		}
	}
	private static void showCellPopup(Object value, MouseEvent e) {
		buildCellPopup(value).show(e.getComponent(), e.getX(), e.getY());
	}
	private static JPopupMenu buildCellPopup(Object value) {
		JPopupMenu cellPopup = new JPopupMenu();
		
		JMenuItem valueItem = new JMenuItem(value.toString()),
							copyItem = new JMenuItem(Strings.get(COPY_TEXT));
		valueItem.setEnabled(false);
		copyItem.addActionListener(e -> copyToClipboard(value));
		
		cellPopup.add(valueItem);
		cellPopup.addSeparator();
		cellPopup.add(copyItem);
		
		return cellPopup;
	}
	private static void copyToClipboard(Object value) {
		StringSelection selection = new StringSelection(value.toString());
		
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
	}
	
	private void tryShowHeaderPopup(MouseEvent e) {
		if (e.isPopupTrigger())
			showHeaderPopup(e);
	}
	private void showHeaderPopup(MouseEvent e) {
		int column = columnAtPoint(e.getPoint());
		
		buildHeaderPopup(column).show(e.getComponent(), e.getX(), e.getY());
	}
	private JPopupMenu buildHeaderPopup(int column) {
		Window frame = SwingUtilities.getWindowAncestor(this);
		int popupHeight = (frame == null) ? DEFAULT_POPUP_HEIGHT : frame.getHeight() / DEFAULT_POPUP_HEIGHT;

		JPopupMenu headerPopup = new JScrollablePopupMenu(popupHeight);
		
		JMenuItem removeFilterItem = new JMenuItem(Strings.get(REMOVE_FILTER_TEXT));
		removeFilterItem.addActionListener(e -> removeFilter(column));
		
		headerPopup.add(removeFilterItem);
		headerPopup.addSeparator();
		
		for (Object value : getUniqueValues(column)) {
			JMenuItem currentFilterItem = new JMenuItem(value.toString());
			currentFilterItem.addActionListener(e -> addFilter(value.toString(), column));
			
			headerPopup.add(currentFilterItem);
		}
		return headerPopup;
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
		if (getEditorComponent() != null && (getEditorComponent() instanceof JTextComponent)) {
			if (e instanceof MouseEvent)
				SwingUtilities.invokeLater(() -> ((JTextComponent) getEditorComponent()).selectAll());
			else
				((JTextComponent) getEditorComponent()).selectAll();
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
