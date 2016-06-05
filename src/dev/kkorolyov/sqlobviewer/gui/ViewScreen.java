package dev.kkorolyov.sqlobviewer.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlob.construct.RowEntry;
import dev.kkorolyov.sqlob.construct.SqlType;
import dev.kkorolyov.sqlob.exceptions.MismatchedTypeException;
import dev.kkorolyov.sqlobviewer.gui.event.GuiListener;
import dev.kkorolyov.sqlobviewer.gui.event.GuiSubject;

/**
 * A prebuilt database view screen.
 */
public class ViewScreen extends JPanel implements GuiSubject {
	private static final long serialVersionUID = -7570749964472465310L;
	private static final String ADD_ROW_TITLE = "Add Row";
	private static final String REFRESH_TABLE_BUTTON_TEXT = "R",
															NEW_TABLE_BUTTON_TEXT = "+",
															ADD_ROW_BUTTON_TEXT = "+",
															DELETE_ROW_BUTTON_TEXT = "-",
															UNDO_STATEMENT_BUTTON_TEXT = "Undo",
															BACK_BUTTON_TEXT = "Log Out";

	private JComboBox<String> tableComboBox;
	private JButton refreshTableButton,
									newTableButton,
									addRowButton,
									deleteRowButton,
									undoStatementButton,
									backButton;
	
	private Column[] columns;
	private RowEntry[][] data;
	private JTable databaseTable;
	
	private JLabel lastStatementLabel;
	private Set<GuiListener> 	listeners = new HashSet<>(),
														listenersToRemove = new HashSet<>();
	
	/**
	 * Constructs a new view screen.
	 * @see #rebuild(String[], String[], Object[][])
	 */
	public ViewScreen(String[] tables, Column[] columnNames, Object[][] data) {
		BorderLayout viewLayout = new BorderLayout();
		setLayout(viewLayout);
		
		rebuild(tables, columnNames, data);
	}
	/**
	 * Rebuilds this screen using specified properties.
	 * @param tables table names to display
	 * @param columnNames displayed table's column names
	 * @param data displayed table's data
	 */
	public void rebuild(String[] tables, Column[] columnNames, Object[][] data) {
		removeAll();
		
		setTables(tables);
		setRefreshTableButtonText(REFRESH_TABLE_BUTTON_TEXT);
		setNewTableButtonText(NEW_TABLE_BUTTON_TEXT);
		setAddRowButtonText(ADD_ROW_BUTTON_TEXT);
		setDeleteRowButtonText(DELETE_ROW_BUTTON_TEXT);
		setUndoStatementButtonText(UNDO_STATEMENT_BUTTON_TEXT);
		setBackButtonText(BACK_BUTTON_TEXT);
		setViewedTable(columnNames, data);
		setLastStatement("LAST STATEMENT GOES HERE");
		
		add(buildTablesPanel(), BorderLayout.NORTH);
		add(buildDatabaseTableScrollPane(), BorderLayout.CENTER);
		add(buildAddDeletePanel(), BorderLayout.EAST);
		add(buildStatementPanel(), BorderLayout.SOUTH);
		
		revalidate();
		repaint();
	}
	private JPanel buildTablesPanel() {
		JPanel tablesPanel = new JPanel();
		BorderLayout tablesLayout = new BorderLayout();
		tablesPanel.setLayout(tablesLayout);
		
		tablesPanel.add(refreshTableButton, BorderLayout.WEST);
		tablesPanel.add(tableComboBox, BorderLayout.CENTER);
		tablesPanel.add(newTableButton, BorderLayout.EAST);
		
		return tablesPanel;
	}
	private JScrollPane buildDatabaseTableScrollPane() {
		JScrollPane databaseTableScrollPane = new JScrollPane(databaseTable);
		
		return databaseTableScrollPane;
	}
	private JPanel buildAddDeletePanel() {
		JPanel addDeletePanel = new JPanel();
		BoxLayout addDeleteLayout = new BoxLayout(addDeletePanel, BoxLayout.Y_AXIS);
		addDeletePanel.setLayout(addDeleteLayout);
		
		addDeletePanel.add(addRowButton);
		addDeletePanel.add(deleteRowButton);
		
		return addDeletePanel;
	}
	private JPanel buildStatementPanel() {
		JPanel statementPanel = new JPanel();
		BorderLayout statementLayout = new BorderLayout();
		statementPanel.setLayout(statementLayout);
		
		statementPanel.add(lastStatementLabel, BorderLayout.CENTER);
		statementPanel.add(undoStatementButton, BorderLayout.EAST);
		
		// TODO Placeholder location for back button
		statementPanel.add(backButton, BorderLayout.SOUTH);
		
		return statementPanel;
	}
	
	/** @param tables table names to display */
	public void setTables(String[] tables) {
		if (tableComboBox == null)
			tableComboBox = new JComboBox<>();
		
		tableComboBox.removeAllItems();
		for (String table : tables)
			tableComboBox.addItem(table);
		
		tableComboBox.addActionListener(e -> notifyTableSelected());
	}
	
	/** @param text new refresh table button text */
	public void setRefreshTableButtonText(String text) {
		if (refreshTableButton == null) {
			refreshTableButton = new JButton();
			
			refreshTableButton.addActionListener(e -> notifyRefreshTableButtonPressed());
		}
		refreshTableButton.setText(text);
	}
	/** @param text new new table button text */
	public void setNewTableButtonText(String text) {
		if (newTableButton == null) {
			newTableButton = new JButton();
			
			newTableButton.addActionListener(e -> notifyNewTableButtonPressed());
		}
		newTableButton.setText(text);
	}
	/** @param text new add row button text */
	public void setAddRowButtonText(String text) {
		if (addRowButton == null) {
			addRowButton = new JButton();
			
			addRowButton.addActionListener(e -> displayAddRowDialog());
		}
		addRowButton.setText(text);
	}
	/** @param text new delete row button text */
	public void setDeleteRowButtonText(String text) {
		if (deleteRowButton == null) {
			deleteRowButton = new JButton();
			
			deleteRowButton.addActionListener(e -> deleteSelected());
		}
		deleteRowButton.setText(text);
	}
	/** @param text new undo statement button text */
	public void setUndoStatementButtonText(String text) {
		if (undoStatementButton == null) {
			undoStatementButton = new JButton();
			
			undoStatementButton.addActionListener(e -> notifyUndoStatementButtonPressed());
		}
		undoStatementButton.setText(text);
	}
	/** @param text new back button text */
	public void setBackButtonText(String text) {
		if (backButton == null) {
			backButton = new JButton();
			
			backButton.addActionListener(e -> notifyBackButtonPressed());
		}
		backButton.setText(text);
	}
	
	/**
	 * @param columnNames table column names
	 * @param data table data
	 */
	public void setViewedTable(Column[] columnNames, Object[][] data) {					
		if (databaseTable == null) {
			databaseTable = new JTable();
			databaseTable.setFillsViewportHeight(true);
		}
		if (columnNames == null || data == null)
			return;
		
		TableModel databaseTableModel = new AbstractTableModel() {
			private static final long serialVersionUID = 5281540525032945988L;
			
			private Column[] modelColumnNames = columnNames;
			private Object[][] modelRowData = data;
			
			@Override
			public int getColumnCount() {
				return modelColumnNames.length;
			}
			@Override
			public int getRowCount() {
				return modelRowData.length;
			}
			
			@Override
			public String getColumnName(int column) {
				return modelColumnNames[column].getName();
			}
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return modelColumnNames[columnIndex].getType().getTypeClass();
			}
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				return modelRowData[rowIndex][columnIndex];
			}
			@SuppressWarnings("synthetic-access")
			@Override
			public void setValueAt(Object value, int rowIndex, int columnIndex) {
				RowEntry[] criteria = buildCriteria(rowIndex, databaseTable);	// Build criteria before updating table value
				
				modelRowData[rowIndex][columnIndex] = value;
				
				RowEntry[] newValues = buildValues(rowIndex, columnIndex, databaseTable);	// Build new values after updating table value
				
				notifyUpdateRows(newValues, criteria);
			}
			
			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return true;
			}
		};		
		databaseTable.setModel(databaseTableModel);
	}
	
	/** @param statement new statement to display */
	public void setLastStatement(String statement) {
		if (lastStatementLabel == null)
			lastStatementLabel = new JLabel();
		
		lastStatementLabel.setText(statement);
	}
	
	private void displayAddRowDialog() {
		JTable addRowTable = buildAddRowTable(buildColumnNames(), buildEmptyData());
		JPanel addRowPanel = buildAddRowPanel(addRowTable);
		
		int selectedOption = JOptionPane.showOptionDialog(this, addRowPanel, ADD_ROW_TITLE, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
		
		if (selectedOption == JOptionPane.OK_OPTION)
			notifyInsertRow(buildCriteria(0, addRowTable));
	}
	private static JPanel buildAddRowPanel(JTable addRowTable) {		
		JPanel addRowPanel = new JPanel();
		BoxLayout addRowLayout = new BoxLayout(addRowPanel, BoxLayout.Y_AXIS);
		addRowPanel.setLayout(addRowLayout);
		
		JScrollPane tableScrollPane = new JScrollPane(addRowTable);
		tableScrollPane.setPreferredSize(new Dimension((int) addRowTable.getPreferredSize().getWidth(), addRowTable.getRowHeight() + 23));
		
		addRowPanel.add(tableScrollPane);
		
		return addRowPanel;
	}
	private static JTable buildAddRowTable(String[] columnNames, Object[][] data) {
		TableModel databaseTableModel = new AbstractTableModel() {
			private static final long serialVersionUID = 5281540525032945988L;
			
			private String[] modelColumnNames = columnNames;
			private Object[][] modelRowData = data;
			
			@Override
			public int getColumnCount() {
				return modelColumnNames.length;
			}
			@Override
			public int getRowCount() {
				return modelRowData.length;
			}
			
			@Override
			public String getColumnName(int column) {
				return modelColumnNames[column];
			}
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return getValueAt(0, columnIndex).getClass();
			}
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				return modelRowData[rowIndex][columnIndex];
			}
			@Override
			public void setValueAt(Object value, int rowIndex, int columnIndex) {				
				modelRowData[rowIndex][columnIndex] = value;
			}
			
			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return true;
			}
		};
		JTable addRowTable = new JTable(databaseTableModel);
		addRowTable.setFillsViewportHeight(true);
		
		return addRowTable;
	}
	
	private String[] buildColumnNames() {
		TableModel model = databaseTable.getModel();
		String[] columnNames = new String[model.getColumnCount()];
		
		for (int i = 0; i < columnNames.length; i++)
			columnNames[i] = model.getColumnName(i);
		
		return columnNames;
	}
	private Object[][] buildEmptyData() {
		TableModel model = databaseTable.getModel();
		int numColumns = model.getColumnCount();
		Object[][] emptyData = new Object[1][numColumns];
		
		for (int i = 0; i < numColumns; i++) {
			Class<?> currentColumnClass = model.getColumnClass(i);
			Object currentEmptyData = null;
			
			if (currentColumnClass == Boolean.class)
				currentEmptyData = false;
			else if (currentColumnClass == Short.class || currentColumnClass == Integer.class || currentColumnClass == Long.class || currentColumnClass == Float.class || currentColumnClass == Double.class)
				currentEmptyData = 0;
			else if (currentColumnClass == Character.class)
				currentEmptyData = ' ';
			else
				currentEmptyData = "";
			
			emptyData[0][i] = currentEmptyData;
		}
		return emptyData;
	}
	
	private void notifyBackButtonPressed() {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.backButtonPressed(this);
	}
	private void notifyRefreshTableButtonPressed() {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.refreshTableButtonPressed(this);
	}
	private void notifyNewTableButtonPressed() {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.newTableButtonPressed(this);
	}
	private void notifyUndoStatementButtonPressed() {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.undoStatementButtonPressed(this);
	}
	
	private void notifyTableSelected() {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.tableSelected((String) tableComboBox.getSelectedItem(), this);
	}
	
	private void notifyInsertRow(RowEntry[] rowValues) {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.insertRow(rowValues, this);
	}
	private void notifyUpdateRows(RowEntry[] newValues, RowEntry[] criteria) {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.updateRows(newValues, criteria, this);
	}
	private void notifyDeleteRows(RowEntry[] criteria) {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.deleteRows(criteria, this);
	}
	
	private static RowEntry[] buildValues(int row, int column, JTable table) {
		TableModel model = table.getModel();
		Column currentColumn = new Column(model.getColumnName(column), selectType(model.getColumnClass(column)));
		RowEntry currentEntry;
		try {
			currentEntry = new RowEntry(currentColumn, model.getValueAt(row, column));
		} catch (MismatchedTypeException e) {
			throw new RuntimeException(e);
		}
		return new RowEntry[]{currentEntry}; 
	}
	private static RowEntry[] buildCriteria(int row, JTable table) {	// Criteria for entire changed row
		TableModel model = table.getModel();
		RowEntry[] entries = new RowEntry[model.getColumnCount()];
		
		for (int i = 0; i < entries.length; i++) {
			Column currentColumn = new Column(model.getColumnName(i), selectType(model.getColumnClass(i)));
			try {
				entries[i] = new RowEntry(currentColumn, model.getValueAt(row, i));
			} catch (MismatchedTypeException e) {
				throw new RuntimeException(e);
			}
		}
		return entries;
	}
	private static SqlType selectType(Class<?> c) {
		for (SqlType type : SqlType.values()) {
			if (type.getTypeClass() == c)
				return type;
		}
		return null;
	}
	
	private void deleteSelected() {
		int[] selectedRows = databaseTable.getSelectedRows();
		RowEntry[][] toDelete = new RowEntry[selectedRows.length][];
		
		for (int i = 0; i < toDelete.length; i++)
			toDelete[i] = buildCriteria(selectedRows[i], databaseTable);
		
		for (RowEntry[] toDel : toDelete)
			notifyDeleteRows(toDel);
	}
	
	@Override
	public void addListener(GuiListener listener) {
		listeners.add(listener);
	}
	@Override
	public void removeListener(GuiListener listener) {
		listenersToRemove.add(listener);
	}
	private void removeQueuedListeners() {
		for (GuiListener listener : listenersToRemove)
			listeners.remove(listener);
		
		listenersToRemove.clear();
	}
}
