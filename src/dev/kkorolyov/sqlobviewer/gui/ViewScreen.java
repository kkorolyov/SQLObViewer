package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Strings.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;

import dev.kkorolyov.sqlob.construct.RowEntry;
import dev.kkorolyov.sqlobviewer.assets.Strings;
import dev.kkorolyov.sqlobviewer.gui.event.GuiListener;
import dev.kkorolyov.sqlobviewer.gui.event.GuiSubject;

/**
 * A prebuilt database view screen.
 */
public class ViewScreen extends JPanel implements GuiSubject {
	private static final long serialVersionUID = -7570749964472465310L;

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
		
		initComponents();
		
		rebuild(tables, table);
	}
	private void initComponents() {
		refreshTableButton = new JButton(Strings.get(REFRESH_TABLE));
		refreshTableButton.addActionListener(e -> notifyRefreshTableButtonPressed());
		
		newTableButton = new JButton(Strings.get(NEW_TABLE));
		newTableButton.addActionListener(e -> notifyNewTableButtonPressed());
		
		addRowButton = new JButton(Strings.get(ADD_ROW));
		addRowButton.addActionListener(e -> displayAddRowDialog());
		
		deleteRowButton = new JButton(Strings.get(DELETE_ROW));
		deleteRowButton.addActionListener(e -> deleteSelected());
		
		undoStatementButton = new JButton(Strings.get(UNDO_STATEMENT));
		undoStatementButton.addActionListener(e -> notifyUndoStatementButtonPressed());
		
		backButton = new JButton(Strings.get(LOG_OUT));
		backButton.addActionListener(e -> notifyBackButtonPressed());
		
		lastStatementLabel = new JLabel();
	}
	
	/**
	 * Rebuilds this screen using specified properties.
	 * @param tables table names to display
	 * @param table database table to display
	 */
	public void rebuild(String[] tables, DatabaseTable table) {
		removeAll();
		
		setTables(tables);
		setViewedTable(table);
		
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
	
	/** @param newTable new database table */
	public void setViewedTable(DatabaseTable newTable) {
		if (databaseTable != null)
			databaseTable.clearListeners();
		
		databaseTable = newTable;
		
		forwardListeners(databaseTable);
	}
	
	/** @param statement new statement to display */
	public void setLastStatement(String statement) {		
		lastStatementLabel.setText(statement);
	}
	
	private void displayAddRowDialog() {
		DatabaseTable addRowTable = databaseTable.getEmptyTable();
		
		int selectedOption = JOptionPane.showOptionDialog(this, buildAddRowScrollPane(addRowTable), Strings.get(ADD_ROW), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
		
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
