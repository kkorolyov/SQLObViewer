package dev.kkorolyov.sqlobviewer.gui.table;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.*;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;

import dev.kkorolyov.simplelogs.Logger;
import dev.kkorolyov.sqlob.construct.RowEntry;
import dev.kkorolyov.sqlobviewer.assets.Assets.Strings;
import dev.kkorolyov.swingplus.JScrollablePopupMenu;

/**
 * A {@code JTable} displaying database information. 
 */
public class SQLObTable extends JTable implements ChangeListener {
	private static final long serialVersionUID = 899876032885503098L;
	private static final int DEFAULT_POPUP_HEIGHT = 32;
	private static final String FILTER_MARKER = "*";
	private static final Logger log = Logger.getLogger(SQLObTable.class.getName());

	private int lastSelectedRow,
							lastSelectedColumn;
	private boolean selectionListenerActive = true;
	private Map<Integer, RowFilter<SQLObTableModel, Integer>> filters = new HashMap<>();
	private Map<Integer, String> filterStrings = new HashMap<>();
	
	private JScrollPane scrollPane;
	
	/**
	 * Constructs a new database table.
	 * @param model the model backing this table
	 */
	public SQLObTable(SQLObTableModel model) {		
		setAutoCreateRowSorter(true);
		
		addListSelectionListener();
		addDeselectionListeners();
		addHeaderPopupListener();
		addCellPopupListener();
		
		setModel(model);
		
		scrollPane = new JScrollPane(this);
	}
	private void addListSelectionListener() {
		if (getSelectionModel() != null) {
			getSelectionModel().addListSelectionListener(e -> {
				if (selectionListenerActive) {
					lastSelectedRow = getSelectedRow();
					lastSelectedColumn = getSelectedColumn();
				}
			});
		}
	}
	private void addDeselectionListeners() {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {				
				if (rowAtPoint(e.getPoint()) < 0)
					deselect();
			}
		});
		addKeyListener(new KeyAdapter() {
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
			boolean oldSelectionListenerActive = selectionListenerActive;
			selectionListenerActive = false;
			
			changeSelection(getRowCount(), getColumnCount(), false, false);	// Moves focus to nonexistent cell
			clearSelection();
			
			selectionListenerActive = oldSelectionListenerActive;
			
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
		
		JMenuItem valueItem = new JMenuItem(getColumnName(column));
		valueItem.setEnabled(false);
		
		headerPopup.add(valueItem);
		
		String filterValue = getFilterValue(column);
		if (filterValue != null) {
			JMenuItem removeFilterItem = new JMenuItem(filterValue);
			removeFilterItem.setToolTipText(Strings.get(REMOVE_FILTER_TIP) + ": " + filterValue);
			removeFilterItem.addActionListener(e -> removeFilter(column));
			
			headerPopup.add(removeFilterItem);
		}
		headerPopup.addSeparator();
		
		for (Object value : getCastedModel().getUniqueValues(column)) {
			if (!value.toString().equals(filterValue)) {
				JMenuItem currentFilterItem = new JMenuItem(value.toString());
				currentFilterItem.setToolTipText(Strings.get(ADD_FILTER_TIP) + ": " + value);
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
		super.setModel(dataModel);
		
		if (dataModel instanceof SQLObTableModel) {
			SQLObTableModel castedModel = (SQLObTableModel) dataModel;
			castedModel.addChangeListener(this);
			
			if (!castedModel.isEditable()) {
				setFocusable(false);
				setCellSelectionEnabled(false);
			}
		}
	}
	
	/** @return	casted table model */
	private SQLObTableModel getCastedModel() {
		return (SQLObTableModel) super.getModel();
	}
	@SuppressWarnings("unchecked")
	private TableRowSorter<SQLObTableModel> getCastedRowSorter() {
		return (TableRowSorter<SQLObTableModel>) super.getRowSorter();
	}
	@Override
	public void stateChanged(ChangeEvent e) {
		deselect();
		sort();
		
		changeSelection(lastSelectedRow, lastSelectedColumn, false, false);
		
		revalidate();
		repaint();
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
				return filterString == null ? filterString : (Strings.get(CURRENT_FILTER_TIP) + ": " + filterString);
			};
		};
	}
}
