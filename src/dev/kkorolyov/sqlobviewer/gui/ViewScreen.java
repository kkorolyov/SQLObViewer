package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.*;

import dev.kkorolyov.sqlob.construct.RowEntry;
import dev.kkorolyov.sqlobviewer.assets.Assets.Strings;
import dev.kkorolyov.sqlobviewer.gui.event.GuiListener;
import dev.kkorolyov.sqlobviewer.gui.event.GuiSubject;
import dev.kkorolyov.sqlobviewer.gui.table.SQLObTable;
import dev.kkorolyov.sqlobviewer.gui.table.SQLObTableModel;
import dev.kkorolyov.swingplus.JHoverButtonPanel;
import dev.kkorolyov.swingplus.JHoverButtonPanel.ExpandTrigger;
import dev.kkorolyov.swingplus.JHoverButtonPanel.Orientation;
import net.miginfocom.swing.MigLayout;

/**
 * A prebuilt database view screen.
 */
public class ViewScreen extends JPanel implements GuiSubject {
	private static final long serialVersionUID = -7570749964472465310L;
	
	private TablesScreen tables;
	private JComboBox<String> tableComboBox;
	private JHoverButtonPanel 	tableButtonPanel,
															rowButtonPanel;
	private JButton refreshTableButton,
									backButton;
	private JLabel lastStatementLabel;
	private JPopupMenu lastStatementPopup;
		
	private Set<GuiListener> 	listeners = new HashSet<>(),
														listenersToRemove = new HashSet<>();
	
	/**
	 * Constructs a new view screen.
	 */
	public ViewScreen() {
		MigLayout viewLayout = new MigLayout("insets 0, wrap 3, gap 4px", "[fill]0px[fill, grow]0px[fill]", "[fill][grow][fill]");
		setLayout(viewLayout);
		
		addTableDeselectionListener();
		
		initComponents();
		buildComponents();
	}
	private void addTableDeselectionListener() {
		addMouseListener(new MouseAdapter() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void mouseClicked(MouseEvent e) {				
				if (!tables.getPanel().contains(e.getPoint())) {
					for (SQLObTable table : tables.getTables())
						table.deselect();
				}
			}
		});
	}
	
	@SuppressWarnings("synthetic-access")
	private void initComponents() {
		tables = new TablesScreen(null);
		
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
		
		tableButtonPanel = new JHoverButtonPanel(Strings.get(TABLE_OPTIONS_TEXT), Orientation.X, ExpandTrigger.HOVER);
		for (JButton tableButton : initTableButtons())
			tableButtonPanel.addButton(tableButton);
		
		rowButtonPanel = new JHoverButtonPanel(Strings.get(ROW_OPTIONS_TEXT), Orientation.X, ExpandTrigger.HOVER);
		for (JButton rowButton : initRowButtons())
			rowButtonPanel.addButton(rowButton);
		
		refreshTableButton = new JButton(Strings.get(REFRESH_TABLE_TEXT));
		refreshTableButton.addActionListener(e -> notifyRefreshTableButtonPressed());
		
		tableComboBox = new JComboBox<String>();
		tableComboBox.addActionListener(e -> notifyTableSelected());
		
		backButton = new JButton(Strings.get(LOG_OUT_TEXT));
		backButton.addActionListener(e -> notifyBackButtonPressed());
	}
	private JButton[] initTableButtons() {
		JButton addTableButton = new JButton(Strings.get(ADD_TABLE_TEXT));		
		addTableButton.setToolTipText(Strings.get(ADD_TABLE_TIP));
		addTableButton.addActionListener(e -> notifyAddTableButtonPressed());

		JButton removeTableButton = new JButton(Strings.get(REMOVE_TABLE_TEXT));
		removeTableButton.setToolTipText(Strings.get(REMOVE_TABLE_TIP));
		removeTableButton.addActionListener(e -> notifyRemoveTable());
		
		return new JButton[]{addTableButton, removeTableButton};
	}
	private JButton[] initRowButtons() {
		JButton addRowButton = new JButton(Strings.get(ADD_ROW_TEXT));
		addRowButton.setToolTipText(Strings.get(ADD_ROW_TIP));
		addRowButton.addActionListener(e -> displayAddRowDialog());
		
		JButton removeRowButton = new JButton(Strings.get(REMOVE_ROW_TEXT));
		removeRowButton.setToolTipText(Strings.get(REMOVE_ROW_TIP));
		removeRowButton.addActionListener(e -> deleteSelected());
		
		return new JButton[]{addRowButton, removeRowButton};
	}
	
	private void buildComponents() {
		add(refreshTableButton);
		add(tableComboBox);
		add(tableButtonPanel, "gap 0");
		add(tables.getPanel(), "spanx 2, grow");
		add(rowButtonPanel, "top, gap 0");
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
	
	/** @param newModel new table model */
	public void setTableModel(SQLObTableModel newModel) {
		tables.setModel(newModel);
	}
	
	/**
	 * Spawns a new table matching this screen's current table model.
	 */
	public void spawnTable() {
		tables.spawnTable();
	}
	
	/** @param statement new statement to display */
	public void setLastStatement(String statement) {		
		lastStatementLabel.setText(statement);
	}
	
	private void displayAddRowDialog() {
		SQLObTable addRowTable = new SQLObTable(tables.getModel().getEmptyTableModel());
		
		int selectedOption = JOptionPane.showOptionDialog(this, addRowTable.getScrollPane(), Strings.get(ADD_ROW_TEXT), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
		
		if (selectedOption == JOptionPane.OK_OPTION) {
			if (addRowTable.getCellEditor() != null)
				addRowTable.getCellEditor().stopCellEditing();
			
			tables.getModel().insertRow(addRowTable.getRow(0));
		}
	}
	
	private void deleteSelected() {
		List<RowEntry[]> toDelete = new LinkedList<>();
		
		for (SQLObTable table : tables.getTables()) {
			for (int index : table.getSelectedRows())
				toDelete.add(table.getRow(index));
		}
		for (RowEntry[] toDel : toDelete)
			tables.getModel().deleteRow(toDel);
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
