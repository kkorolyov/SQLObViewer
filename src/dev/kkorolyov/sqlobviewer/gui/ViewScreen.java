package dev.kkorolyov.sqlobviewer.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import dev.kkorolyov.sqlob.connection.TableConnection;
import dev.kkorolyov.sqlobviewer.gui.event.GuiListener;
import dev.kkorolyov.sqlobviewer.gui.event.GuiSubject;

/**
 * A prebuilt view of a database.
 */
public class ViewScreen extends JPanel implements GuiSubject {
	private static final long serialVersionUID = -7570749964472465310L;

	private JComboBox<String> tableComboBox;
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
		setViewedTable(null);
		
		add(tableComboBox, BorderLayout.NORTH);
		add(tableViewScreen, BorderLayout.CENTER);
		
		if (tables.length > 0)
			notifyTableSelected(tables[0]);
		
		revalidate();
		repaint();
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
				@SuppressWarnings("unchecked")
				String selection = (String) ((JComboBox<String>) e.getSource()).getSelectedItem();
				
				notifyTableSelected(selection);
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
	
	private void notifyTableSelected(String table) {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.tableSelected(table);
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
