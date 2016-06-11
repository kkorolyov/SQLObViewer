package dev.kkorolyov.sqlobviewer.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;

import dev.kkorolyov.sqlob.construct.RowEntry;
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
	private DatabaseTable databaseTable;
	
	private JLabel lastStatementLabel;
	private Set<GuiListener> 	listeners = new HashSet<>(),
														listenersToRemove = new HashSet<>();
	
	/**
	 * Constructs a new view screen.
	 * @see #rebuild(String[], DatabaseTable)
	 */
	public ViewScreen(String[] tables, DatabaseTable table) {
		BorderLayout viewLayout = new BorderLayout();
		setLayout(viewLayout);
		
		rebuild(tables, table);
	}
	/**
	 * Rebuilds this screen using specified properties.
	 * @param tables table names to display
	 * @param table database table to display
	 */
	public void rebuild(String[] tables, DatabaseTable table) {
		removeAll();
		
		setTables(tables);
		setRefreshTableButtonText(REFRESH_TABLE_BUTTON_TEXT);
		setNewTableButtonText(NEW_TABLE_BUTTON_TEXT);
		setAddRowButtonText(ADD_ROW_BUTTON_TEXT);
		setDeleteRowButtonText(DELETE_ROW_BUTTON_TEXT);
		setUndoStatementButtonText(UNDO_STATEMENT_BUTTON_TEXT);
		setBackButtonText(BACK_BUTTON_TEXT);
		setViewedTable(table);
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
	
	/** @param newTable new database table */
	public void setViewedTable(DatabaseTable newTable) {
		if (databaseTable != null)
			databaseTable.clearListeners();
		
		databaseTable = newTable;
		
		forwardListeners(databaseTable);
	}
	
	/** @param statement new statement to display */
	public void setLastStatement(String statement) {
		if (lastStatementLabel == null)
			lastStatementLabel = new JLabel();
		
		lastStatementLabel.setText(statement);
	}
	
	private void displayAddRowDialog() {
		DatabaseTable addRowTable = databaseTable.getEmptyTable();
		
		int selectedOption = JOptionPane.showOptionDialog(this, buildAddRowScrollPane(addRowTable), ADD_ROW_TITLE, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
		
		if (selectedOption == JOptionPane.OK_OPTION)
			notifyInsertRow(addRowTable.getSelectedRow(0));
	}
	private static JScrollPane buildAddRowScrollPane(JTable addRowTable) {		
		JScrollPane addRowScrollPane = new JScrollPane(addRowTable);
		addRowScrollPane.setPreferredSize(new Dimension((int) addRowTable.getPreferredSize().getWidth(), addRowTable.getRowHeight() + 23));
				
		return addRowScrollPane;
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
	private void notifyDeleteRows(RowEntry[] criteria) {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.deleteRows(criteria, this);
	}
	
	private void deleteSelected() {
		int[] selectedRows = databaseTable.getSelectedRows();
		RowEntry[][] toDelete = new RowEntry[selectedRows.length][];
		
		for (int i = 0; i < toDelete.length; i++)
			toDelete[i] = databaseTable.getSelectedRow(selectedRows[i]);
		
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
	
	@Override
	public void clearListeners() {
		listeners.clear();
	}
	
	private void removeQueuedListeners() {
		for (GuiListener listener : listenersToRemove)
			listeners.remove(listener);
		
		listenersToRemove.clear();
	}
	
	private void forwardListeners(GuiSubject subject) {
		for (GuiListener listener : listeners)
			subject.addListener(listener);
	}
}
