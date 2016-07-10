package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.*;

import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.*;

import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlob.construct.RowEntry;
import dev.kkorolyov.sqlobviewer.assets.Assets.Strings;
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
	private JPanel panel;
	private TablesScreen tablesScreen;
	private FilterInfoScreen tableFiltersScreen;
	private JComboBox<String> tableComboBox;
	private JHoverButtonPanel 	tableButtonPanel,
															rowButtonPanel;
	private JButton refreshTableButton,
									backButton;
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
		
		tablesScreen = new TablesScreen(null);
		
		tableFiltersScreen = new FilterInfoScreen(null);
		
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
		undoItem.addActionListener(e -> fireRevertStatement(""));
		lastStatementPopup.add(undoItem);
		
		tableButtonPanel = new JHoverButtonPanel(Strings.get(TABLE_OPTIONS_TEXT), Orientation.X, ExpandTrigger.HOVER);
		for (JButton tableButton : initTableButtons())
			tableButtonPanel.addButton(tableButton);
		
		rowButtonPanel = new JHoverButtonPanel(Strings.get(ROW_OPTIONS_TEXT), Orientation.X, ExpandTrigger.HOVER);
		for (JButton rowButton : initRowButtons())
			rowButtonPanel.addButton(rowButton);
		
		refreshTableButton = new JButton(Strings.get(REFRESH_TABLE_TEXT));
		refreshTableButton.addActionListener(e -> fireUpdate());
		
		tableComboBox = new JComboBox<String>();
		tableComboBox.addActionListener(e -> {
			String selectedTable = (String) tableComboBox.getSelectedItem();
			
			if (selectedTable != null)
				fireSelectTable(selectedTable);
		});
		
		backButton = new JButton(Strings.get(LOG_OUT_TEXT));
		backButton.addActionListener(e -> fireCanceled());
	}
	private JButton[] initTableButtons() {
		JButton addTableButton = new JButton(Strings.get(ADD_TABLE_TEXT));		
		addTableButton.setToolTipText(Strings.get(ADD_TABLE_TIP));
		addTableButton.addActionListener(e -> displayAddTableDialog());

		JButton removeTableButton = new JButton(Strings.get(REMOVE_TABLE_TEXT));
		removeTableButton.setToolTipText(Strings.get(REMOVE_TABLE_TIP));
		removeTableButton.addActionListener(e -> displayConfirmRemoveTableDialog());
		
		return new JButton[]{addTableButton, removeTableButton};
	}
	private JButton[] initRowButtons() {
		JButton addRowButton = new JButton(Strings.get(ADD_ROW_TEXT));
		addRowButton.setToolTipText(Strings.get(ADD_ROW_TIP));
		addRowButton.addActionListener(e -> displayAddRowDialog());
		
		JButton removeRowButton = new JButton(Strings.get(REMOVE_ROW_TEXT));
		removeRowButton.setToolTipText(Strings.get(REMOVE_ROW_TIP));
		removeRowButton.addActionListener(e -> displayConfirmRemoveRowDialog());
		
		return new JButton[]{addRowButton, removeRowButton};
	}
	
	private void buildComponents() {
		panel.add(refreshTableButton);
		panel.add(tableComboBox);
		panel.add(tableButtonPanel, "gap 0");
		panel.add(tablesScreen.getPanel(), "spanx 2, grow");
		panel.add(rowButtonPanel, "top, gap 0");
		panel.add(tableFiltersScreen.getPanel(), "spanx");
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
	
	/** @param name name of table to add to combo box */
	public void addTable(String name) {
		tableComboBox.addItem(name);
	}
	/** @param name name of table to remove from combo box */
	public void removeTable(String name) {
		tableComboBox.removeItem(name);
	}
	
	/** @return names of all tables in combo box */
	public String[] getTables() {
		String[] tables = new String[tableComboBox.getItemCount()];
		
		for (int i = 0; i < tables.length; i++)
			tables[i] = tableComboBox.getItemAt(i);
		
		return tables;
	}
	/** @param tableNames new table names to display in combo box */
	public void setTables(String[] tableNames) {
		tableComboBox.removeAllItems();
		
		for (String table : tableNames)
			tableComboBox.addItem(table);
	}
	
	/** @param newModel new table model */
	public void setTableModel(SQLObTableModel newModel) {
		tablesScreen.setModel(newModel);
		
		tableFiltersScreen.setTables(tablesScreen.getTables().toArray(new SQLObTable[tablesScreen.getTables().size()]));
	}
	
	/** @param statement new statement to display */
	public void setLastStatement(String statement) {		
		lastStatementLabel.setText(statement);
	}
	
	private void displayAddTableDialog() {		
		String title = Strings.get(ADD_TABLE_TIP);
		CreateTableScreen message = new CreateTableScreen(false);

		int selectedOption = displayOkCancelDialog(title, message.getPanel());
		
		if (selectedOption == JOptionPane.OK_OPTION) {
			String name = message.getName();
			Column[] columns = message.getColumns();
			
			if (name.length() > 0 && columns.length > 0)
				fireCreateTable(name, columns);
		}
	}
	private void displayAddRowDialog() {		
		String title = Strings.get(ADD_ROW_TIP);
		SQLObTable message = new SQLObTable(tablesScreen.getModel().getEmptyTableModel());

		int selectedOption = displayOkCancelDialog(title, message.getScrollPane());
		
		if (selectedOption == JOptionPane.OK_OPTION) {
			if (message.getCellEditor() != null)
				message.getCellEditor().stopCellEditing();
			
			tablesScreen.getModel().insertRow(message.getRow(0));
		}
	}
	private int displayOkCancelDialog(String title, Object message) {
		return JOptionPane.showOptionDialog(getPanel(), message, Strings.get(title), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
	}
	
	private void displayConfirmRemoveTableDialog() {
		String selectedTableName = (String) tableComboBox.getSelectedItem();
		
		String 	title = Strings.get(REMOVE_TABLE_TIP),
						message = Strings.get(CONFIRM_REMOVE_TABLE_TEXT) + System.lineSeparator() + selectedTableName;
		
		if (displayConfirmDialog(title, message) == JOptionPane.YES_OPTION)
			fireDropTable(selectedTableName);
	}
	private void displayConfirmRemoveRowDialog() {
		RowEntry[][] selectedRows = getSelectedRows();

		String title = Strings.get(REMOVE_ROW_TIP);
		JPanel message = new JPanel(new GridLayout(0, 1));
		
		JLabel selectedRowsLabel = new JLabel(Strings.get(CONFIRM_REMOVE_ROW_TEXT));
		SQLObTable selectedRowsTable = new SQLObTable(new SQLObTableModel(tablesScreen.getModel().getColumns(), selectedRows, false));
		
		message.add(selectedRowsLabel);
		message.add(selectedRowsTable.getScrollPane());
		
		if (displayConfirmDialog(title, message) == JOptionPane.YES_OPTION)
			deleteRows(selectedRows);
	}
	private int displayConfirmDialog(String title, Object message) {
		return JOptionPane.showConfirmDialog(getPanel(), message, title, JOptionPane.YES_NO_OPTION);
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
