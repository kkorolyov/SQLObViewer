package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;

import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlob.construct.RowEntry;
import dev.kkorolyov.sqlobviewer.assets.Assets.Strings;
import dev.kkorolyov.sqlobviewer.gui.event.GuiListener;
import dev.kkorolyov.sqlobviewer.gui.event.GuiSubject;
import net.miginfocom.swing.MigLayout;

/**
 * A prebuilt database view screen.
 */
public class ViewScreen extends JPanel implements GuiSubject {
	private static final long serialVersionUID = -7570749964472465310L;

	private JComboBox<String> tableComboBox;
	private DynamicButtonPanel tableButtonPanel;
	private JButton refreshTableButton,
									addRowButton,
									removeRowButton,
									backButton;
	private DatabaseTable databaseTable;
	private JLabel lastStatementLabel;
	private JPopupMenu lastStatementPopup;
		
	private Set<GuiListener> 	listeners = new HashSet<>(),
														listenersToRemove = new HashSet<>();
	
	/**
	 * Constructs a new view screen.
	 * @param tables table names to display
	 */
	public ViewScreen(String[] tables) {
		MigLayout viewLayout = new MigLayout("insets 0, wrap 3, gap 4px", "[fill]0px[fill, grow]0px[fill]", "[fill][grow][fill]");
		setLayout(viewLayout);
		
		addTableDeselectionListener();
		
		initComponents();
		buildComponents();
		
		setTables(tables);
	}
	private void addTableDeselectionListener() {
		addMouseListener(new MouseAdapter() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void mouseClicked(MouseEvent e) {				
				if (databaseTable != null) {
					if (databaseTable.rowAtPoint(e.getPoint()) < 0) {
						databaseTable.deselect();
					}
				}
			}
		});
	}
	
	@SuppressWarnings("synthetic-access")
	private void initComponents() {
		databaseTable = new DatabaseTable();
		
		lastStatementLabel = new JLabel();
		lastStatementLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				tryShowLastStatementPopup(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				tryShowLastStatementPopup(e);
			}
		});
		lastStatementPopup = new JPopupMenu();
		JMenuItem undoItem = new JMenuItem(Strings.get(UNDO_STATEMENT_TEXT));
		undoItem.addActionListener(e -> notifyUndoStatementButtonPressed());
		lastStatementPopup.add(undoItem);
		
		tableButtonPanel = new DynamicButtonPanel(Strings.get(TABLE_OPTIONS_TEXT));
		JButton addTableButton = new JButton(Strings.get(ADD_TABLE_TEXT)),
						removeTableButton = new JButton(Strings.get(REMOVE_TABLE_TEXT));
		addTableButton.setToolTipText(Strings.get(ADD_TABLE_TIP));
		removeTableButton.setToolTipText(Strings.get(REMOVE_TABLE_TIP));
		addTableButton.addActionListener(e -> notifyAddTableButtonPressed());
		removeTableButton.addActionListener(e -> notifyRemoveTable());
		tableButtonPanel.addButton(addTableButton);
		tableButtonPanel.addButton(removeTableButton);
		
		refreshTableButton = new JButton(Strings.get(REFRESH_TABLE_TEXT));
		refreshTableButton.addActionListener(e -> notifyRefreshTableButtonPressed());
		
		tableComboBox = new JComboBox<String>();
		tableComboBox.addActionListener(e -> notifyTableSelected());
		
		addRowButton = new JButton(Strings.get(ADD_ROW_TEXT));
		addRowButton.addActionListener(e -> displayAddRowDialog());
		
		removeRowButton = new JButton(Strings.get(REMOVE_ROW_TEXT));
		removeRowButton.addActionListener(e -> deleteSelected());
		
		backButton = new JButton(Strings.get(LOG_OUT_TEXT));
		backButton.addActionListener(e -> notifyBackButtonPressed());
	}
	private void buildComponents() {
		add(refreshTableButton);
		add(tableComboBox);
		add(tableButtonPanel, "gap 0");
		add(databaseTable.getScrollPane(), "spanx 2, grow");
		add(addRowButton, "split 2, flowy, top, gapy 0");
		add(removeRowButton, "gapy 0");
		add(lastStatementLabel, "spanx");
		add(backButton, "span, center, grow 0");
	}
	
	private void tryShowLastStatementPopup(MouseEvent e) {
		if (e.isPopupTrigger())
			showLastStatementPopup(e);
	}
	private void showLastStatementPopup(MouseEvent e) {
		lastStatementPopup.show(e.getComponent(), e.getX(), e.getY());
	}
	
	/** @param tables table names to display */
	public void setTables(String[] tables) {
		tableComboBox.removeAllItems();
		for (String table : tables)
			tableComboBox.addItem(table);
		
		tableComboBox.revalidate();
		tableComboBox.repaint();
	}
	
	/**
	 * @param newColumns new viewed columns
	 * @param newData new viewed data
	 */
	public void setViewedData(Column[] newColumns, RowEntry[][] newData) {
		databaseTable.setData(newColumns, newData);
		
		revalidate();
		repaint();
	}
	
	/** @param statement new statement to display */
	public void setLastStatement(String statement) {		
		lastStatementLabel.setText(statement);
	}
	
	private void displayAddRowDialog() {
		DatabaseTable addRowTable = databaseTable.getEmptyTable();
		
		int selectedOption = JOptionPane.showOptionDialog(this, addRowTable.getScrollPane(), Strings.get(ADD_ROW_TEXT), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
		
		if (selectedOption == JOptionPane.OK_OPTION) {
			if (addRowTable.getCellEditor() != null)
				addRowTable.getCellEditor().stopCellEditing();
			
			notifyInsertRow(addRowTable.getSelectedRow(0));
		}
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
	private void notifyAddTableButtonPressed() {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.addTableButtonPressed(this);
	}
	private void notifyUndoStatementButtonPressed() {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.undoStatementButtonPressed(this);
	}
	
	private void notifyTableSelected() {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners) {
			if (tableComboBox.getSelectedItem() != null)
				listener.tableSelected((String) tableComboBox.getSelectedItem(), this);
		}
	}
	
	private void notifyRemoveTable() {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.removeTable((String) tableComboBox.getSelectedItem(), this);
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
		databaseTable.addListener(listener);
	}
	@Override
	public void removeListener(GuiListener listener) {
		listenersToRemove.add(listener);
		databaseTable.removeListener(listener);
	}
	
	@Override
	public void clearListeners() {
		listeners.clear();
		databaseTable.clearListeners();
	}
	
	private void removeQueuedListeners() {
		for (GuiListener listener : listenersToRemove)
			listeners.remove(listener);
		
		listenersToRemove.clear();
	}
}
