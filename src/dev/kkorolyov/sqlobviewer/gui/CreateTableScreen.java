package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.ADD_COLUMN_TEXT;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.BACK_TEXT;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.SUBMIT_TEXT;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlobviewer.assets.Assets.Strings;
import dev.kkorolyov.sqlobviewer.gui.event.GuiListener;
import dev.kkorolyov.sqlobviewer.gui.event.GuiSubject;
import dev.kkorolyov.sqlobviewer.utility.Standardizer;
import dev.kkorolyov.sqlobviewer.utility.Standardizer.Extreme;

/**
 * A prebuilt create table screen.
 */
public class CreateTableScreen extends JPanel implements GuiSubject {
	private static final long serialVersionUID = 596434011928144752L;
	
	private JButton submitButton,
									backButton,
									addColumnButton;
	
	private JPanel 	columnsPanel,
									buttonPanel;
	private JScrollPane columnsScrollPane;
	
	private Set<GuiListener> 	listeners = new HashSet<>(),
														listenersToRemove = new HashSet<>();
	
	/**
	 * Constructs a new create table screen.
	 */
	public CreateTableScreen() {
		BorderLayout createTableLayout = new BorderLayout();
		setLayout(createTableLayout);
		
		initComponents();
		initPanels();
		
		add(columnsScrollPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		
		addColumn();

		revalidate();
		repaint();
	}
	private void initComponents() {
		submitButton = new JButton(Strings.get(SUBMIT_TEXT));
		submitButton.addActionListener(e -> notifySubmitButtonPressed());
		
		backButton = new JButton(Strings.get(BACK_TEXT));
		backButton.addActionListener(e -> notifyBackButtonPressed());
		
		addColumnButton = new JButton(Strings.get(ADD_COLUMN_TEXT));
		addColumnButton.addActionListener(e -> addColumn());
	}
	private void initPanels() {
		columnsPanel = new JPanel();
		BoxLayout buildLayout = new BoxLayout(columnsPanel, BoxLayout.X_AXIS);
		columnsPanel.setLayout(buildLayout);
		
		buttonPanel = new JPanel();
		buttonPanel.add(submitButton);
		buttonPanel.add(backButton);
		
		columnsScrollPane = new JScrollPane(columnsPanel);
	}
	
	private void addColumn() {
		ColumnPanel newColumn = new ColumnPanel();
		newColumn.addDeleteButtonListener(e -> removeColumn(newColumn));
		
		if (columnsPanel.getComponentCount() > 0)
			columnsPanel.remove(columnsPanel.getComponentCount() - 1);	// Remove add button from this column
		
		Standardizer.standardize(null, Extreme.MAXIMUM, newColumn, addColumnButton);
		columnsPanel.add(newColumn);
		columnsPanel.add(addColumnButton);	// Add to next column
		
		columnsPanel.repaint();
		columnsPanel.revalidate();
	}
	private void removeColumn(ColumnPanel column) {
		columnsPanel.remove(column);
		
		columnsPanel.repaint();
		columnsPanel.revalidate();
	}
	
	/** @return	an array of {@code Column} objects matching all the {@code ColumnPanel} objects currently on this screen */
	public Column[] getColumns() {
		Component[] columnComponents = columnsPanel.getComponents();
		List<Column> columns = new LinkedList<>();
		
		for (Component columnComponent : columnComponents) {
			if (columnComponent instanceof ColumnPanel)
				columns.add(((ColumnPanel) columnComponent).getColumn());
		}
		return columns.toArray(new Column[columns.size()]);
	}
	
	private void notifySubmitButtonPressed() {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.submitButtonPressed(this);
	}
	private void notifyBackButtonPressed() {
		removeQueuedListeners();
		
		for (GuiListener listener : listeners)
			listener.backButtonPressed(this);
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
