package dev.kkorolyov.sqlobviewer.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import dev.kkorolyov.sqlob.connection.TableConnection;
import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlob.construct.Results;
import dev.kkorolyov.sqlob.construct.RowEntry;
import dev.kkorolyov.sqlobviewer.gui.event.GuiListener;
import dev.kkorolyov.sqlobviewer.gui.event.GuiSubject;

/**
 * A prebuilt database view screen.
 */
public class ViewScreen extends JPanel implements GuiSubject {
	private static final long serialVersionUID = -7570749964472465310L;
	private static final String NEW_TABLE_BUTTON_TEXT = "+",
															BACK_BUTTON_TEXT = "Log Out";

	private JComboBox<String> tableComboBox;
	private JButton newTableButton,
									backButton;
	private JTable databaseTable;
	private Set<GuiListener> 	listeners = new HashSet<>(),
														listenersToRemove = new HashSet<>();
	
	/**
	 * Constructs a new view screen.
	 * @see #rebuild(String[])
	 */
	public ViewScreen(String[] tables) {
		BorderLayout viewLayout = new BorderLayout();
		setLayout(viewLayout);
		
		rebuild(tables);
	}
	/**
	 * Rebuilds this screen using specified properties.
	 * @param tables table names to display
	 */
	public void rebuild(String[] tables) {
		removeAll();
		
		setTables(tables);
		setNewTableButtonText(NEW_TABLE_BUTTON_TEXT);
		setBackButtonText(BACK_BUTTON_TEXT);
		setViewedTable(null);
						
		add(buildTablesPanel(), BorderLayout.NORTH);
		add(buildDatabaseTableScrollPane(), BorderLayout.CENTER);
		add(backButton, BorderLayout.SOUTH);
		
		revalidate();
		repaint();
	}
	private JPanel buildTablesPanel() {
		JPanel tablesPanel = new JPanel();
		BorderLayout tablesLayout = new BorderLayout();
		tablesPanel.setLayout(tablesLayout);
		
		tablesPanel.add(tableComboBox, BorderLayout.CENTER);
		tablesPanel.add(newTableButton, BorderLayout.EAST);
		
		return tablesPanel;
	}
	private JScrollPane buildDatabaseTableScrollPane() {
		JScrollPane databaseTableScrollPane = new JScrollPane(databaseTable);
		
		return databaseTableScrollPane;
	}
	
	/** @param tables table names to display */
	public void setTables(String[] tables) {
		if (tableComboBox == null)
			tableComboBox = new JComboBox<>();
		
		tableComboBox.removeAllItems();
		for (String table : tables)
			tableComboBox.addItem(table);
		
		tableComboBox.addActionListener(new ActionListener() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void actionPerformed(ActionEvent e) {
				notifyTableSelected();
			}
		});
	}
	
	/** @param text new new table button text */
	public void setNewTableButtonText(String text) {
		if (newTableButton == null) {
			newTableButton = new JButton();
			
			newTableButton.addActionListener(new ActionListener() {
				@SuppressWarnings("synthetic-access")
				@Override
				public void actionPerformed(ActionEvent e) {
					notifyNewTableButtonPressed();
				}
			});
		}
		newTableButton.setText(text);
	}
	/** @param text new back button text */
	public void setBackButtonText(String text) {
		if (backButton == null) {
			backButton = new JButton();
			
			backButton.addActionListener(new ActionListener() {
				@SuppressWarnings("synthetic-access")
				@Override
				public void actionPerformed(ActionEvent e) {
					notifyBackButtonPressed();
				}
			});
		}
		backButton.setText(text);
	}
	
	/** @param newTable table to view */
	public void setViewedTable(TableConnection newTable) {					
		if (databaseTable == null) {
			databaseTable = new JTable();
			databaseTable.setFillsViewportHeight(true);
		}
		if (newTable == null)
			return;
		
		String[] columnNames = extractColumnNames(newTable);
		Object[][] data = extractData(newTable);
		
		TableModel resultsModel = new AbstractTableModel() {
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
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return true;
			}
		};
		databaseTable.setModel(resultsModel);
	}
	private static String[] extractColumnNames(TableConnection table) {
		Column[] columns = table.getColumns();
		String[] columnNames = new String[columns.length];
		
		for (int i = 0; i < columnNames.length; i++)
			columnNames[i] = columns[i].getName();
		
		return columnNames;
	}
	private static Object[][] extractData(TableConnection table) {
		List<Object[]> data = new LinkedList<>();

		try {
			Results allResults = table.select(null);
			
			RowEntry[] currentRow;
			while ((currentRow = allResults.getNextRow()) != null) {
				Object[] currentRowData = new Object[currentRow.length];
				
				for (int i = 0; i < currentRowData.length; i++)
					currentRowData[i] = currentRow[i].getValue();
				
				data.add(currentRowData);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return data.toArray(new Object[data.size()][]);
	}
	
	private void notifyBackButtonPressed() {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.backButtonPressed(this);
	}
	private void notifyNewTableButtonPressed() {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.newTableButtonPressed(this);
	}
	private void notifyTableSelected() {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.tableSelected((String) tableComboBox.getSelectedItem(), this);
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
