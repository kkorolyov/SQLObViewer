package dev.kkorolyov.sqlobviewer.gui.table;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.ACTION_COPY;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.ACTION_TIP_ADD_FILTER;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.ACTION_TIP_REMOVE_FILTER;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.MESSAGE_TIP_CURRENT_FILTER;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;

import dev.kkorolyov.simplelogs.Logger;
import dev.kkorolyov.simplelogs.Logger.Level;
import dev.kkorolyov.sqlob.construct.RowEntry;
import dev.kkorolyov.sqlobviewer.assets.Assets.Lang;
import dev.kkorolyov.swingplus.JScrollablePopupMenu;

/**
 * A {@code JTable} displaying database information. 
 */
public class SQLObTable extends JTable implements ChangeListener {
	private static final long serialVersionUID = 899876032885503098L;
	private static final int DEFAULT_POPUP_HEIGHT = 32;
	private static final String FILTER_MARKER = "*";
	private static final Logger log = Logger.getLogger(SQLObTable.class.getName(), Level.DEBUG, (PrintWriter[]) null);

	private int lastSelectedRow,
							lastSelectedColumn;
	private boolean selectionListenerActive = true;
	private Map<Integer, RowFilter<SQLObTableModel, Integer>> filters = new HashMap<>();
	private Map<Integer, String> filterStrings = new HashMap<>();
	
	private JScrollPane scrollPane;
	
	private Set<ChangeListener> changeListeners = new CopyOnWriteArraySet<>();
	
	/**
	 * Constructs a new database table.
	 * @param model the model backing this table
	 */
	@SuppressWarnings("synthetic-access")
	public SQLObTable(SQLObTableModel model) {		
		setAutoCreateRowSorter(true);
		
		if (getSelectionModel() != null) {
			getSelectionModel().addListSelectionListener(e -> {
				if (selectionListenerActive) {
					lastSelectedRow = getSelectedRow();
					if (lastSelectedRow >= 0)
						lastSelectedRow = lastSelectedRow < getRowCount() ? convertRowIndexToModel(lastSelectedRow) : -1;
					
					lastSelectedColumn = getSelectedColumn();
					if (lastSelectedColumn >= 0)
						lastSelectedColumn = lastSelectedColumn < getColumnCount() ? convertColumnIndexToModel(lastSelectedColumn) : -1;
				}
			});
		}
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (rowAtPoint(e.getPoint()) < 0)
					deselect();
			}
		});
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					deselect();
			}
		});
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
		getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				tryShowHeaderPopup(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				tryShowHeaderPopup(e);
			}
		});
		setModel(model);
		
		scrollPane = new JScrollPane(this);
	}
	
	/**
	 * Deselects any currently-selected data in this table.
	 * Cancels any pending cell editing.
	 */
	public void deselect() {
		int selectedRow = getSelectedRow(),
				selectedColumn = getSelectedColumn();
		
		if (selectedRow >= 0 || selectedColumn >= 0) {	// Something is selected
			changeSelection(getRowCount(), getColumnCount(), false, false);	// Moves focus to nonexistent cell
			clearSelection();
			
			if (getCellEditor() != null)
				getCellEditor().cancelCellEditing();
		}
	}
	
	/**
	 * Returns the filter value of a column.
	 * @param column index of column
	 * @return column's filter value, or {@code null} if not filtered
	 */
	public String getFilterValue(int column) {
		return filterStrings.get(column);
	}
	
	/**
	 * Adds a filter to this table.
	 * @param filter value to filter by
	 * @param column index of column to apply filter on
	 */
	public void addFilter(String filter, int column) {
		String exactFilter = '^' + filter + '$';
		
		filters.put(column, RowFilter.regexFilter(exactFilter, column));
		filterStrings.put(column, filter);
		
		applyFilterMarker(column, true);
		
		log.debug("Added filter=" + exactFilter + " for column=" + getModel().getColumnName(column).toUpperCase());
		
		applyFilters();
	}
	/**
	 * Removes the filter for a specified column.
	 * @param column index of column to remove filter of
	 */
	public void removeFilter(int column) {
		RowFilter<SQLObTableModel, Integer> removedFilter = filters.remove(column);
		String removedFilterString = filterStrings.remove(column);
		
		applyFilterMarker(column, false);
		
		if (removedFilter == null)
			log.debug("No filter to remove for column=" + getModel().getColumnName(column).toUpperCase());
		else
			log.debug("Removed filter=" + removedFilterString + " for column=" + getModel().getColumnName(column).toUpperCase());
		
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
	
	private void applyFilterMarker(int column, boolean enabled) {
		TableColumn currentColumn = getColumnModel().getColumn(column);
		String currentColumnValue = (String) currentColumn.getHeaderValue();
		
		boolean hasMarker = currentColumnValue.regionMatches((currentColumnValue.length() - FILTER_MARKER.length()), FILTER_MARKER, 0, FILTER_MARKER.length());
		if (enabled ? !hasMarker : hasMarker)
			currentColumn.setHeaderValue(enabled ? currentColumnValue + FILTER_MARKER : currentColumnValue.substring(0, currentColumnValue.length() - 1));
		
		getTableHeader().repaint();
	}
	
	/**
	 * Sorts this table based on its sorter's current sort keys.
	 */
	public void sort() {
		getCastedRowSorter().sort();
	}
	
	/** @return row at the specified view index */
	public RowEntry[] getRow(int index) {
		return getCastedModel().getRow(convertRowIndexToModel(index));
	}
	
	/** @return	a table with a single row of data matching this table's data types */
	public SQLObTable getEmptyTable() {
		return new SQLObTable(getCastedModel().getEmptyTableModel());
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
							copyItem = new JMenuItem(Lang.get(ACTION_COPY));
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
		
		JMenuItem valueItem = new JMenuItem(getColumnName(column));
		valueItem.setEnabled(false);
		
		headerPopup.add(valueItem);
		
		String filterValue = getFilterValue(column);
		if (filterValue != null) {
			JMenuItem removeFilterItem = new JMenuItem(filterValue);
			removeFilterItem.setToolTipText(Lang.get(ACTION_TIP_REMOVE_FILTER) + ": " + filterValue);
			removeFilterItem.addActionListener(e -> removeFilter(column));
			
			headerPopup.add(removeFilterItem);
		}
		headerPopup.addSeparator();
		
		for (Object value : getCastedModel().getUniqueValues(column)) {
			if (!value.toString().equals(filterValue)) {
				JMenuItem currentFilterItem = new JMenuItem(value.toString());
				currentFilterItem.setToolTipText(Lang.get(ACTION_TIP_ADD_FILTER) + ": " + value);
				currentFilterItem.addActionListener(e -> addFilter(value.toString(), column));
				
				headerPopup.add(currentFilterItem);
			}
		}
		return headerPopup;
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
	public Dimension getPreferredScrollableViewportSize() {
		return new Dimension((int) super.getPreferredScrollableViewportSize().getWidth(), (getRowHeight() * getRowCount()));
	}
	@Override
	public boolean getFillsViewportHeight() {
		return true;
	}
	
	@Override
	public void setModel(TableModel dataModel) {
		if (getCastedModel() != null)
			getCastedModel().removeChangeListener(this);
		
		super.setModel(dataModel != null ? dataModel : new DefaultTableModel());
		
		if (dataModel instanceof SQLObTableModel) {
			SQLObTableModel castedModel = (SQLObTableModel) dataModel;
			castedModel.addChangeListener(this);
			
			if (!castedModel.isEditable()) {
				setFocusable(false);
				setCellSelectionEnabled(false);
			}
		}
	}
	
	/** @return	table model as a {@code SQLObTableModel}, or {@code null} if no model or model is not a {@code SQLObTableModel} */
	private SQLObTableModel getCastedModel() {
		return ((super.getModel() != null) && (super.getModel() instanceof SQLObTableModel)) ? (SQLObTableModel) super.getModel() : null;
	}
	@SuppressWarnings("unchecked")
	private TableRowSorter<SQLObTableModel> getCastedRowSorter() {
		return (TableRowSorter<SQLObTableModel>) super.getRowSorter();
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {	// Invoked by backing model
		boolean oldSelectionListenerActive = selectionListenerActive;
		selectionListenerActive = false;	// Ignore selection changes during update
		
		deselect();
		sort();
		
		selectionListenerActive = oldSelectionListenerActive;	// Ok to listen to selection changes again

		if (lastSelectedRow >= 0 && lastSelectedColumn >= 0)
			changeSelection(convertRowIndexToView(lastSelectedRow), convertColumnIndexToView(lastSelectedColumn), false, false);
		
		revalidate();
		repaint();
	}
	
	@Override
	public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
		super.changeSelection(rowIndex, columnIndex, toggle, extend);
		
		fireStateChanged();
	}
	@Override
	public void clearSelection() {
		super.clearSelection();
		
		if (changeListeners != null)
			fireStateChanged();
	}
	
	@Override
	protected JTableHeader createDefaultTableHeader() {
		return new JTableHeader(getColumnModel()) {
			private static final long serialVersionUID = -4913911112753388076L;

			@Override
			public String getToolTipText(MouseEvent event) {
				int viewColumnIndex = columnAtPoint(event.getPoint()),
						modelColumnIndex = (viewColumnIndex < 0) ? -1 : convertColumnIndexToModel(viewColumnIndex);
								
				String filterString = (modelColumnIndex < 0) ? null : getFilterValue(modelColumnIndex);
				return filterString == null ? filterString : (Lang.get(MESSAGE_TIP_CURRENT_FILTER) + ": " + filterString);
			};
		};
	}
	
	private void fireStateChanged() {
		for (ChangeListener listener : changeListeners)
			listener.stateChanged(new ChangeEvent(this));
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
		changeListeners.clear();
	}
}
