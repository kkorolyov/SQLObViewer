package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;

import dev.kkorolyov.sqlob.construct.RowEntry;
import dev.kkorolyov.sqlobviewer.assets.Assets.Strings;
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
	
	private JPanel 	northPanel,
									eastPanel,
									southPanel;
	private JScrollPane scrollPane;
	
	private Set<GuiListener> 	listeners = new HashSet<>(),
														listenersToRemove = new HashSet<>();
	
	/**
	 * Constructs a new view screen.
	 * @param tables table names to display
	 * @param table database table to display
	 */
	public ViewScreen(String[] tables, DatabaseTable table) {
		BorderLayout viewLayout = new BorderLayout();
		setLayout(viewLayout);
		
		initComponents();
		initPanels();
		
		setTables(tables);
		setViewedTable(table);
		
		add(northPanel, BorderLayout.NORTH);
		add(eastPanel, BorderLayout.EAST);
		add(southPanel, BorderLayout.SOUTH);
		add(scrollPane, BorderLayout.CENTER);
		
		revalidate();
		repaint();
	}
	
	private void initComponents() {
		refreshTableButton = new JButton(Strings.get(REFRESH_TABLE_TEXT));
		refreshTableButton.addActionListener(e -> notifyRefreshTableButtonPressed());
		
		tableComboBox = new JComboBox<String>();
		tableComboBox.addActionListener(e -> notifyTableSelected());
		
		newTableButton = new JButton(Strings.get(NEW_TABLE_TEXT));
		newTableButton.addActionListener(e -> notifyNewTableButtonPressed());
		
		addRowButton = new JButton(Strings.get(ADD_ROW_TEXT));
		addRowButton.addActionListener(e -> displayAddRowDialog());
		
		deleteRowButton = new JButton(Strings.get(DELETE_ROW_TEXT));
		deleteRowButton.addActionListener(e -> deleteSelected());
		
		undoStatementButton = new JButton(Strings.get(UNDO_STATEMENT_TEXT));
		undoStatementButton.addActionListener(e -> notifyUndoStatementButtonPressed());
		
		backButton = new JButton(Strings.get(LOG_OUT_TEXT));
		backButton.addActionListener(e -> notifyBackButtonPressed());
		
		lastStatementLabel = new JLabel();
	}
	private void initPanels() {
		northPanel = new JPanel();
		BorderLayout northLayout = new BorderLayout();
		northPanel.setLayout(northLayout);
		
		northPanel.add(refreshTableButton, BorderLayout.WEST);
		northPanel.add(tableComboBox, BorderLayout.CENTER);
		northPanel.add(newTableButton, BorderLayout.EAST);
		
		eastPanel = new JPanel();
		BoxLayout eastLayout = new BoxLayout(eastPanel, BoxLayout.Y_AXIS);
		eastPanel.setLayout(eastLayout);
		
		standardizeComponents(addRowButton, deleteRowButton);
		eastPanel.add(addRowButton);
		eastPanel.add(deleteRowButton);
		
		southPanel = new JPanel();
		BorderLayout southLayout = new BorderLayout();
		southPanel.setLayout(southLayout);
		
		southPanel.add(lastStatementLabel, BorderLayout.CENTER);
		southPanel.add(undoStatementButton, BorderLayout.EAST);
		southPanel.add(backButton, BorderLayout.SOUTH);
		
		scrollPane = new JScrollPane();
	}
	
	private static void standardizeComponents(Component... components) {	// Inflates smaller components' max sizes to max size of largest component
		int maxWidth = 0,
				maxHeight = 0;
		
		for (Component component : components) {
			int currentWidth = (int) component.getMaximumSize().getWidth(),
					currentHeight = (int) component.getMaximumSize().getHeight();
			
			if (currentWidth > maxWidth)
				maxWidth = currentWidth;
			if (currentHeight > maxHeight)
				maxHeight = currentHeight;
		}
		for (Component component : components)
			component.setMaximumSize(new Dimension(maxWidth, maxHeight));
	}
	
	/** @param tables table names to display */
	public void setTables(String[] tables) {
		tableComboBox.removeAllItems();
		for (String table : tables)
			tableComboBox.addItem(table);
		
		tableComboBox.revalidate();
		tableComboBox.repaint();
	}
	
	/** @param newTable new database table */
	public void setViewedTable(DatabaseTable newTable) {
		if (databaseTable != null)
			databaseTable.clearListeners();
		
		databaseTable = newTable;
		forwardListeners(databaseTable);
		
		scrollPane.setViewportView(databaseTable);
		scrollPane.revalidate();
		scrollPane.repaint();
	}
	
	/** @param statement new statement to display */
	public void setLastStatement(String statement) {		
		lastStatementLabel.setText(statement);
	}
	
	private void displayAddRowDialog() {
		DatabaseTable addRowTable = databaseTable.getEmptyTable();
		
		int selectedOption = JOptionPane.showOptionDialog(this, buildAddRowScrollPane(addRowTable), Strings.get(ADD_ROW_TEXT), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
		
		if (selectedOption == JOptionPane.OK_OPTION) {
			if (addRowTable.getCellEditor() != null)
				addRowTable.getCellEditor().stopCellEditing();
			
			notifyInsertRow(addRowTable.getSelectedRow(0));
		}
	}
	private static JScrollPane buildAddRowScrollPane(JTable addRowTable) {		
		JScrollPane addRowScrollPane = new JScrollPane(addRowTable);
		addRowScrollPane.setPreferredSize(new Dimension((int) addRowTable.getPreferredSize().getWidth(), addRowTable.getRowHeight() + 23));
				
		return addRowScrollPane;
	}
	
	private void deleteSelected() {
		int[] selectedRows = databaseTable.getSelectedRows();
		RowEntry[][] toDelete = new RowEntry[selectedRows.length][];
		
		for (int i = 0; i < toDelete.length; i++)
			toDelete[i] = databaseTable.getSelectedRow(selectedRows[i]);
		
		for (RowEntry[] toDel : toDelete)
			notifyDeleteRows(toDel);
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
