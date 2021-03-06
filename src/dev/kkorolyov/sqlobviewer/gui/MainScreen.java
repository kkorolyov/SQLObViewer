package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.ApplicationProperties.Keys.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import dev.kkorolyov.simplelogs.Logger;
import dev.kkorolyov.simplelogs.Logger.Level;
import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlob.construct.RowEntry;
import dev.kkorolyov.sqlob.construct.statement.StatementCommand;
import dev.kkorolyov.sqlobviewer.assets.ApplicationProperties.Config;
import dev.kkorolyov.sqlobviewer.assets.ApplicationProperties.Lang;
import dev.kkorolyov.sqlobviewer.assets.Asset;
import dev.kkorolyov.sqlobviewer.gui.event.CancelListener;
import dev.kkorolyov.sqlobviewer.gui.event.CancelSubject;
import dev.kkorolyov.sqlobviewer.gui.event.SqlRequestListener;
import dev.kkorolyov.sqlobviewer.gui.event.SqlRequestSubject;
import dev.kkorolyov.sqlobviewer.gui.table.SQLObTable;
import dev.kkorolyov.sqlobviewer.gui.table.SQLObTableModel;
import dev.kkorolyov.sqlobviewer.model.DatabaseModel;
import dev.kkorolyov.swingplus.JHoverButtonPanel;
import dev.kkorolyov.swingplus.JHoverButtonPanel.ExpandTrigger;
import dev.kkorolyov.swingplus.JHoverButtonPanel.Orientation;
import net.miginfocom.swing.MigLayout;

/**
 * The main application screen.
 */
public class MainScreen implements Screen, CancelSubject, SqlRequestSubject, ChangeListener {
	private static final Logger log = Logger.getLogger(MainScreen.class.getName(), Level.DEBUG, (PrintWriter[]) null);
	
	private DatabaseModel dbModel;
	
	private JPanel panel;
	private TableGrid tableGrid;
	private GridSelector tableGridSelector;
	private JComboBox<String> tableSelector;
	private boolean tableSelectorEnabled;
	private JHoverButtonPanel 	tableButtonPanel,
															rowButtonPanel;
	private JButton	backButton,
									refreshTableButton,
									addTableButton,
									removeTableButton,
									addRowButton,
									removeRowButton;
	private JLabel selectedRowsCounter;
	private JTextArea lastStatementText;
	private JPopupMenu lastStatementPopup;
	
	private Set<CancelListener> cancelListeners = new CopyOnWriteArraySet<>();
	private Set<SqlRequestListener> sqlRequestListeners = new CopyOnWriteArraySet<>();
	
	/**
	 * Constructs a new view screen.
	 * @param dbModel backing database model
	 */
	public MainScreen(DatabaseModel dbModel) {
		this.dbModel = dbModel;
		this.dbModel.addChangeListener(this);
		
		initComponents();
		buildComponents();
		
		update();
	}
	@SuppressWarnings("synthetic-access")
	private void initComponents() {
		panel = new JPanel(new MigLayout("insets 0, wrap 3, gap 4px", "[fill]0px[fill, grow]0px[fill]", "[fill][grow][fill]"));
		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!tableGrid.contains(e.getPoint()))
					tableGrid.deselect();
			}
		});
		tableGrid = new TableGrid(new SQLObTableModel(dbModel.getTableColumns(), dbModel.getTableData(), true), Config.getInt(CURRENT_TABLES_X), Config.getInt(CURRENT_TABLES_Y));
		tableGrid.addChangeListener(e -> syncSelectedRowsCounter());
		
		tableGridSelector = new GridSelector(Config.getInt(MAX_TABLES_X), Config.getInt(MAX_TABLES_Y));
		tableGridSelector.addChangeListener(e -> syncTableGrid());
		
		selectedRowsCounter = new JLabel();
		syncSelectedRowsCounter();
		
		lastStatementText = new JTextArea();
		lastStatementText.setOpaque(false);
		lastStatementText.setEditable(false);
		lastStatementText.setLineWrap(true);
		lastStatementText.setWrapStyleWord(true);
		lastStatementText.setToolTipText(Lang.get(MESSAGE_TIP_LAST_STATEMENT));
		lastStatementText.addMouseListener(new MouseAdapter() {
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
		undoItem.addActionListener(e ->  {
			if (lastStatementText.getText().length() > 0)	// Check for some statement
				fireRevertStatement(dbModel.getLastStatement());
		});
		lastStatementPopup.add(undoItem);
		
		tableSelector = new JComboBox<String>();
		tableSelector.addActionListener(e -> {
			if (tableSelectorEnabled) {
				String selectedTable = (String) tableSelector.getSelectedItem();
				
				if (selectedTable != null)
					fireSelectTable(selectedTable);
			}
		});
		refreshTableButton = new JButton(Asset.REFRESH_ICON.asIcon());
		refreshTableButton.setToolTipText(Lang.get(ACTION_TIP_REFRESH_TABLE));
		refreshTableButton.addActionListener(e -> fireUpdate());
		
		initTableButtons();
		initRowButtons();
		
		backButton = new JButton(Lang.get(ACTION_LOG_OUT));
		backButton.addActionListener(e -> fireCanceled());
	}
	private void initTableButtons() {
		tableButtonPanel = new JHoverButtonPanel(Lang.get(DYNAMIC_ACTION_TABLE), Orientation.X, ExpandTrigger.HOVER);
		
		addTableButton = new JButton(Lang.get(ACTION_ADD_TABLE));
		addTableButton.setToolTipText(Lang.get(ACTION_TIP_ADD_TABLE));
		addTableButton.addActionListener(e -> displayAddTableDialog());

		removeTableButton = new JButton(Lang.get(ACTION_REMOVE_TABLE));
		removeTableButton.setToolTipText(Lang.get(ACTION_TIP_REMOVE_TABLE));
		removeTableButton.addActionListener(e -> displayConfirmRemoveTableDialog());
		
		tableButtonPanel.addButton(addTableButton);
		tableButtonPanel.addButton(removeTableButton);
	}
	private void initRowButtons() {
		rowButtonPanel = new JHoverButtonPanel(Lang.get(DYNAMIC_ACTION_ROW), Orientation.X, ExpandTrigger.HOVER);

		addRowButton = new JButton(Lang.get(ACTION_ADD_ROW));
		addRowButton.setToolTipText(Lang.get(ACTION_TIP_ADD_ROW));
		addRowButton.addActionListener(e -> displayAddRowDialog());
		
		removeRowButton = new JButton(Lang.get(ACTION_REMOVE_ROW));
		removeRowButton.setToolTipText(Lang.get(ACTION_TIP_REMOVE_ROW));
		removeRowButton.addActionListener(e -> displayConfirmRemoveRowDialog());
		
		rowButtonPanel.addButton(addRowButton);
		rowButtonPanel.addButton(removeRowButton);
	}
	private void buildComponents() {
		panel.add(refreshTableButton);
		panel.add(tableSelector);
		panel.add(tableButtonPanel, "gap 0");
		panel.add(tableGrid.getPanel(), "spanx 2, grow");
		panel.add(rowButtonPanel, "split 2, flowy, top, gap 0");
		panel.add(tableGridSelector.getPanel(), "gap 0");
		panel.add(selectedRowsCounter, "spanx");
		panel.add(lastStatementText, "spanx 2, wmin 0, wrap");
		panel.add(backButton, "span, center, grow 0");
	}
	
	private void update() {
		setTables(dbModel.getTables());
		syncTable();
		
		getTableModel().setData(dbModel.getTableColumns(), dbModel.getTableData());
		
		StatementCommand lastStatement = dbModel.getLastStatement();
		lastStatementText.setText(lastStatement != null ? lastStatement.toString() : null);
	}
	private void syncTable() {
		tableSelectorEnabled = false;
		
		tableSelector.setSelectedItem(dbModel.getTable());
		
		tableSelectorEnabled = true;
	}
	private void syncTableGrid() {
		tableGrid.setTables(Config.getInt(CURRENT_TABLES_X), Config.getInt(CURRENT_TABLES_Y));
	}
	private void syncSelectedRowsCounter() {
		selectedRowsCounter.setText(getNumSelectedRows() + " " + Lang.get(MESSAGE_ROWS_SELECTED));
	}
	
	/** @return name of currently-selected table */
	public String getTable() {
		return (String) tableSelector.getSelectedItem();
	}
	/** @param name name of table to add to table selector */
	public void addTable(String name) {
		tableSelector.addItem(name);
	}
	/** @param name name of table to remove from table selector */
	public void removeTable(String name) {
		tableSelector.removeItem(name);
	}
	
	/** @return names of all tables in table selector */
	public String[] getTables() {
		String[] tables = new String[tableSelector.getItemCount()];
		
		for (int i = 0; i < tables.length; i++)
			tables[i] = tableSelector.getItemAt(i);
		
		return tables;
	}
	/** @param tableNames new table names to display in table selector */
	public void setTables(String[] tableNames) {
		log.debug("Setting new table names...");
		
		Arrays.sort(tableNames);
		
		if (Arrays.equals(getTables(), tableNames)) {
			log.debug("No change in table names, aborting");
			return;
		}
		tableSelectorEnabled = false;	// Avoid selection event

		String lastSelectedTable = (String) tableSelector.getSelectedItem();

		clearTables();
		
		for (String table : tableNames)
			addTable(table);
		
		if (lastSelectedTable != null)
			tableSelector.setSelectedItem(lastSelectedTable);

		tableSelectorEnabled = true;
		
		log.debug("Set new tables of length = " + tableSelector.getItemCount());
	}
	/**
	 * Clears all tables from table selector.
	 */
	public void clearTables() {
		tableSelector.removeAllItems();
	}
	
	/** @return table model backing all displayed tables */
	public SQLObTableModel getTableModel() {
		return tableGrid.getModel();
	}
	/** @param newModel new table model */
	public void setTableModel(SQLObTableModel newModel) {
		tableGrid.setModel(newModel);
	}
		
	private int getNumSelectedRows() {
		int totalSelected = 0;
		
		for (SQLObTable table : tableGrid.getTables())
			totalSelected += table.getSelectedRowCount();
		
		return totalSelected;
	}
	private RowEntry[][] getSelectedRows() {
		List<RowEntry[]> selectedRows = new LinkedList<>();
		
		for (SQLObTable table : tableGrid.getTables()) {
			for (int index : table.getSelectedRows())
				selectedRows.add(table.getRow(index));
		}
		return selectedRows.toArray(new RowEntry[selectedRows.size()][]);
	}
	
	private void deleteRows(RowEntry[][] toDelete) {
		for (RowEntry[] toDel : toDelete)
			getTableModel().deleteRow(toDel);
	}
	
	private void tryShowLastStatementPopup(MouseEvent e) {
		if (e.isPopupTrigger())
			showLastStatementPopup(e);
	}
	private void showLastStatementPopup(MouseEvent e) {
		lastStatementPopup.show(e.getComponent(), e.getX(), e.getY());
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
			
			tableGrid.getModel().insertRow(message.getRow(0));
		}
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
	
	@Override
	public boolean focusDefaultComponent() {
		return false;
	}
	@Override
	public JPanel getPanel() {
		return panel;
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == dbModel)
			update();
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
	
	private void fireRevertStatement(StatementCommand statement) {
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
		getTableModel().addSqlRequestListener(listener);
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
