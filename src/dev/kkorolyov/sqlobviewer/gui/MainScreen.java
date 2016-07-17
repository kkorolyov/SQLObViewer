package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.*;

import dev.kkorolyov.simplelogs.Logger;
import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlob.construct.RowEntry;
import dev.kkorolyov.sqlobviewer.assets.Assets.Config;
import dev.kkorolyov.sqlobviewer.assets.Assets.Lang;
import dev.kkorolyov.sqlobviewer.gui.event.CancelListener;
import dev.kkorolyov.sqlobviewer.gui.event.CancelSubject;
import dev.kkorolyov.sqlobviewer.gui.event.SqlRequestListener;
import dev.kkorolyov.sqlobviewer.gui.event.SqlRequestSubject;
import dev.kkorolyov.sqlobviewer.gui.table.SQLObTable;
import dev.kkorolyov.sqlobviewer.gui.table.SQLObTableModel;
import dev.kkorolyov.swingplus.JHoverButtonPanel;
import dev.kkorolyov.swingplus.JHoverButtonPanel.ExpandTrigger;
import dev.kkorolyov.swingplus.JHoverButtonPanel.Orientation;
import net.miginfocom.swing.MigLayout;

/**
 * The main application screen.
 */
public class MainScreen implements Screen, CancelSubject, SqlRequestSubject {
	private static final Logger log = Logger.getLogger(MainScreen.class.getName());
	
	private JPanel panel;
	private TableGrid tablesScreen;
	private JComboBox<String> tableComboBox;
	private JHoverButtonPanel 	tableButtonPanel,
															rowButtonPanel;
	private JButton refreshTableButton,
									backButton;
	private GridSelector tableGridSelector;
	private JLabel lastStatementLabel;
	private JPopupMenu lastStatementPopup;
	
	private Set<CancelListener> cancelListeners = new CopyOnWriteArraySet<>();
	private Set<SqlRequestListener> sqlRequestListeners = new CopyOnWriteArraySet<>();
	
	/**
	 * Constructs a new view screen.
	 */
	public MainScreen() {
		initComponents();
		addTableDeselectionListener();
		
		buildComponents();
	}
	private void addTableDeselectionListener() {
		panel.addMouseListener(new MouseAdapter() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void mouseClicked(MouseEvent e) {				
				if (!tablesScreen.getPanel().contains(e.getPoint())) {
					for (SQLObTable table : tablesScreen.getTables())
						table.deselect();
				}
			}
		});
	}
	
	@SuppressWarnings("synthetic-access")
	private void initComponents() {
		panel = new JPanel(new MigLayout("insets 0, wrap 3, gap 4px", "[fill]0px[fill, grow]0px[fill]", "[fill][grow][fill]"));
		
		tablesScreen = new TableGrid(null, Config.getInt(CURRENT_TABLES_X), Config.getInt(CURRENT_TABLES_Y));
				
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
		JMenuItem undoItem = new JMenuItem(Lang.get(ACTION_UNDO_STATEMENT));
		undoItem.addActionListener(e -> fireRevertStatement(""));
		lastStatementPopup.add(undoItem);
		
		tableButtonPanel = new JHoverButtonPanel(Lang.get(DYNAMIC_ACTION_TABLE), Orientation.X, ExpandTrigger.HOVER);
		for (JButton tableButton : initTableButtons())
			tableButtonPanel.addButton(tableButton);
		
		rowButtonPanel = new JHoverButtonPanel(Lang.get(DYNAMIC_ACTION_ROW), Orientation.X, ExpandTrigger.HOVER);
		for (JButton rowButton : initRowButtons())
			rowButtonPanel.addButton(rowButton);
		
		tableGridSelector = new GridSelector(Config.getInt(MAX_TABLES_X), Config.getInt(MAX_TABLES_Y));
		tableGridSelector.addChangeListener(e -> tablesScreen.setTables(Config.getInt(CURRENT_TABLES_X), Config.getInt(CURRENT_TABLES_Y)));	// TODO Move to syncTables()
		
		refreshTableButton = new JButton(Lang.get(ACTION_REFRESH_TABLE));
		refreshTableButton.addActionListener(e -> fireUpdate());
		refreshTableButton.setToolTipText(Lang.get(ACTION_TIP_REFRESH_TABLE));
		
		tableComboBox = new JComboBox<String>();
		tableComboBox.addActionListener(e -> {
			String selectedTable = (String) tableComboBox.getSelectedItem();
			
			if (selectedTable != null)
				fireSelectTable(selectedTable);
		});
		backButton = new JButton(Lang.get(ACTION_LOG_OUT));
		backButton.addActionListener(e -> fireCanceled());
	}
	private JButton[] initTableButtons() {
		JButton addTableButton = new JButton(Lang.get(ACTION_ADD_TABLE));		
		addTableButton.setToolTipText(Lang.get(ACTION_TIP_ADD_TABLE));
		addTableButton.addActionListener(e -> displayAddTableDialog());

		JButton removeTableButton = new JButton(Lang.get(ACTION_REMOVE_TABLE));
		removeTableButton.setToolTipText(Lang.get(ACTION_TIP_REMOVE_TABLE));
		removeTableButton.addActionListener(e -> displayConfirmRemoveTableDialog());
		
		return new JButton[]{addTableButton, removeTableButton};
	}
	private JButton[] initRowButtons() {
		JButton addRowButton = new JButton(Lang.get(ACTION_ADD_ROW));
		addRowButton.setToolTipText(Lang.get(ACTION_TIP_ADD_ROW));
		addRowButton.addActionListener(e -> displayAddRowDialog());
		
		JButton removeRowButton = new JButton(Lang.get(ACTION_REMOVE_ROW));
		removeRowButton.setToolTipText(Lang.get(ACTION_TIP_REMOVE_ROW));
		removeRowButton.addActionListener(e -> displayConfirmRemoveRowDialog());
		
		return new JButton[]{addRowButton, removeRowButton};
	}
	
	private void buildComponents() {
		panel.add(refreshTableButton);
		panel.add(tableComboBox);
		panel.add(tableButtonPanel, "gap 0");
		panel.add(tablesScreen.getPanel(), "spanx 2, grow");
		panel.add(rowButtonPanel, "split 2, flowy, top, gap 0");
		panel.add(tableGridSelector, "gap 0");
		panel.add(lastStatementLabel, "spanx");
		panel.add(backButton, "span, center, grow 0");
	}
	
	private void tryShowLastStatementPopup(MouseEvent e) {
		if (e.isPopupTrigger())
			showLastStatementPopup(e);
	}
	private void showLastStatementPopup(MouseEvent e) {
		lastStatementPopup.show(e.getComponent(), e.getX(), e.getY());
	}
	
	/** @return name of currently-selected table */
	public String getTable() {
		return (String) tableComboBox.getSelectedItem();
	}
	/** @param name name of table to add to table selector */
	public void addTable(String name) {
		tableComboBox.addItem(name);
	}
	/** @param name name of table to remove from table selector */
	public void removeTable(String name) {
		tableComboBox.removeItem(name);
	}
	
	/** @return names of all tables in table selector */
	public String[] getTables() {
		String[] tables = new String[tableComboBox.getItemCount()];
		
		for (int i = 0; i < tables.length; i++)
			tables[i] = tableComboBox.getItemAt(i);
		
		return tables;
	}
	/** @param tableNames new table names to display in table selector */
	public void setTables(String[] tableNames) {
		clearTables();
		
		for (String table : tableNames)
			addTable(table);
	}
	/**
	 * Clears all tables from table selector.
	 */
	public void clearTables() {
		tableComboBox.removeAllItems();
	}
	
	/** @return table model backing all displayed tables */
	public SQLObTableModel getTableModel() {
		return tablesScreen.getModel();
	}
	/** @param newModel new table model */
	public void setTableModel(SQLObTableModel newModel) {
		tablesScreen.setModel(newModel);
	}
	
	/** @param statement new statement to display */
	public void setLastStatement(String statement) {		
		lastStatementLabel.setText(statement);
	}
	
	private void displayAddTableDialog() {
		String title = Lang.get(ACTION_TIP_ADD_TABLE);
		CreateTableScreen message = new CreateTableScreen(false);
		
		if (displayDialog(title, message.getPanel(), Lang.get(OPTION_SUBMIT), Lang.get(OPTION_CANCEL)) == 0) {
			String name = message.getName();
			Column[] columns = message.getColumns();
			
			if (name.length() > 0 && columns.length > 0)
				fireCreateTable(name, columns);
		}
	}
	private void displayAddRowDialog() {
		if (getTableModel() == null) {
			log.warning("No table model set, aborting AddRowDialog creation");
			return;
		}
		String title = Lang.get(ACTION_TIP_ADD_ROW);
		SQLObTable message = new SQLObTable(getTableModel().getEmptyTableModel());
		
		if (displayDialog(title, message.getScrollPane(), Lang.get(OPTION_SUBMIT), Lang.get(OPTION_CANCEL)) == 0) {
			if (message.getCellEditor() != null)
				message.getCellEditor().stopCellEditing();
			
			tablesScreen.getModel().insertRow(message.getRow(0));
		}
	}
	
	private void displayConfirmRemoveTableDialog() {
		String selectedTableName = getTable();
		if (selectedTableName == null) {
			log.warning("No table selected, aborting RemoveTableDialog creation");
			return;
		}
		String 	title = Lang.get(ACTION_TIP_REMOVE_TABLE),
						message = Lang.get(MESSAGE_CONFIRM_REMOVE_TABLE) + System.lineSeparator() + selectedTableName;
		
		if (displayDialog(title, message, Lang.get(OPTION_YES), Lang.get(OPTION_NO)) == 0)
			fireDropTable(selectedTableName);
	}
	private void displayConfirmRemoveRowDialog() {
		if (getTableModel() == null) {
			log.warning("No table model set, aborting RemoveRowDialog creation");
			return;
		}		
		RowEntry[][] selectedRows = getSelectedRows();
		
		if (selectedRows.length > 0) {
			String title = Lang.get(ACTION_TIP_REMOVE_ROW);
			JPanel message = new JPanel(new MigLayout("insets 0, gap 4px, flowy"));
			
			JLabel selectedRowsLabel = new JLabel(Lang.get(MESSAGE_CONFIRM_REMOVE_ROW));
			SQLObTable selectedRowsTable = new SQLObTable(new SQLObTableModel(getTableModel().getColumns(), selectedRows, false));
			
			message.add(selectedRowsLabel);
			message.add(selectedRowsTable.getScrollPane());
			
			if (displayDialog(title, message, Lang.get(OPTION_YES), Lang.get(OPTION_NO)) == 0)
				deleteRows(selectedRows);
		}
	}
	
	private int displayDialog(String title, Object message, Object... options) {
		return JOptionPane.showOptionDialog(getPanel(), message, Lang.get(title), JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, null);
	}
	
	private RowEntry[][] getSelectedRows() {
		List<RowEntry[]> selectedRows = new LinkedList<>();
		
		for (SQLObTable table : tablesScreen.getTables()) {
			for (int index : table.getSelectedRows())
				selectedRows.add(table.getRow(index));
		}
		return selectedRows.toArray(new RowEntry[selectedRows.size()][]);
	}
	
	private void deleteRows(RowEntry[][] toDelete) {
		for (RowEntry[] toDel : toDelete) {
			System.out.println("Deleting " + Arrays.toString(toDel));
			tablesScreen.getModel().deleteRow(toDel);
		}
	}
	
	@Override
	public boolean focusDefaultComponent() {
		return false;
	}
	@Override
	public JPanel getPanel() {
		return panel;
	}
	
	private void fireCanceled() {
		for (CancelListener listener : cancelListeners)
			listener.canceled(this);
	}
	
	private void fireUpdate() {		
		for (SqlRequestListener listener : sqlRequestListeners)
			listener.update(this);
	}
	
	private void fireSelectTable(String name) {		
		for (SqlRequestListener listener : sqlRequestListeners)
			listener.selectTable(name, this);
	}
	
	private void fireCreateTable(String name, Column[] columns) {
		for (SqlRequestListener listener : sqlRequestListeners)
			listener.createTable(name, columns, this);
	}
	private void fireDropTable(String name) {
		for (SqlRequestListener listener : sqlRequestListeners)
			listener.dropTable(name, this);
	}
	
	private void fireRevertStatement(String statement) {		
		for (SqlRequestListener listener : sqlRequestListeners)
			listener.revertStatement(statement, this);
	}
	
	@Override
	public void addCancelListener(CancelListener listener) {
		cancelListeners.add(listener);
	}
	@Override
	public void removeCancelListener(CancelListener listener) {
		cancelListeners.remove(listener);
	}
	
	@Override
	public void addSqlRequestListener(SqlRequestListener listener) {
		sqlRequestListeners.add(listener);
	}
	@Override
	public void removeSqlRequestListener(SqlRequestListener listener) {
		sqlRequestListeners.remove(listener);
	}
	
	@Override
	public void clearListeners() {
		cancelListeners.clear();
		sqlRequestListeners.clear();
	}
}
