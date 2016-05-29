package dev.kkorolyov.sqlobviewer.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import dev.kkorolyov.sqlob.connection.TableConnection;
import dev.kkorolyov.sqlobviewer.gui.event.GuiListener;
import dev.kkorolyov.sqlobviewer.gui.event.GuiSubject;

/**
 * A prebuilt database view screen.
 */
public class ViewScreen extends JPanel implements GuiSubject {
	private static final long serialVersionUID = -7570749964472465310L;
	private static final String NEW_TABLE_BUTTON_TEXT = "+";

	private JComboBox<String> tableComboBox;
	private JButton newTableButton;
	private TableViewScreen tableViewScreen;
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
		setNewTableButton(NEW_TABLE_BUTTON_TEXT);
		setViewedTable(null);
						
		add(buildTablesPanel(), BorderLayout.NORTH);
		add(tableViewScreen, BorderLayout.CENTER);
		
		if (tables.length > 0)
			notifyTableSelected();
		
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
	
	/** @param text text of new table button */
	public void setNewTableButton(String text) {
		if (newTableButton == null)
			newTableButton = new JButton();
		
		newTableButton.setText(text);
		
		newTableButton.addActionListener(new ActionListener() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void actionPerformed(ActionEvent e) {
				notifyNewTableButtonPressed();
			}
		});
	}
	
	/** @param newTable table to view */
	public void setViewedTable(TableConnection newTable) {
		if (tableViewScreen == null)
			tableViewScreen = new TableViewScreen(newTable);
		
		else
			tableViewScreen.rebuild(newTable);
	}
	
	private void notifyTableSelected() {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.tableSelected((String) tableComboBox.getSelectedItem(), this);
	}
	private void notifyNewTableButtonPressed() {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.newTableButtonPressed(this);
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
