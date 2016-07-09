package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.*;

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.*;
import javax.swing.Box.Filler;

import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlobviewer.assets.Assets.Strings;
import dev.kkorolyov.swingplus.JPlaceholderTextField;
import net.miginfocom.swing.MigLayout;

/**
 * A prebuilt create table screen.
 */
public class CreateTableScreen implements Screen, SubmitSubject, CancelSubject {
	private static final byte NUM_FILLER_COLUMNS = 4;
		
	private JPanel panel;
	private JTextField nameField;
	private JButton submitButton,
									backButton,
									addColumnButton;
	private JPanel columnsPanel;
	private JScrollPane columnsScrollPane;
	
	private Set<SubmitListener> submitListeners = new CopyOnWriteArraySet<>();
	private Set<CancelListener> cancelListeners = new CopyOnWriteArraySet<>();
	
	/**
	 * Constructs a new create table screen.
	 */
	public CreateTableScreen() {
		initComponents();
		buildComponents();
		
		addColumn();
	}
	private void initComponents() {
		panel = new JPanel(new MigLayout("insets 0, gap 4px, flowy", "[grow]", "[]push[]push[]"));
		
		columnsPanel = new JPanel();
		int strutWidth = (int) new ColumnPanel().getPreferredSize().getWidth();
		for (int i = 0; i < NUM_FILLER_COLUMNS; i++)
			columnsPanel.add(Box.createHorizontalStrut(strutWidth));
		
		columnsScrollPane = new JScrollPane(columnsPanel);
		columnsScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
		columnsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		
		columnsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, (int) columnsScrollPane.getHorizontalScrollBar().getPreferredSize().getHeight(), 0));
		
		nameField = new JPlaceholderTextField(Strings.get(TABLE_NAME_TIP), 0);
		nameField.setToolTipText(Strings.get(TABLE_NAME_TIP));
		
		submitButton = new JButton(Strings.get(SUBMIT_TEXT));
		submitButton.addActionListener(e -> fireSubmitted());
		
		backButton = new JButton(Strings.get(CANCEL_TEXT));
		backButton.addActionListener(e -> fireCanceled());
		
		addColumnButton = new JButton(Strings.get(ADD_COLUMN_TEXT));
		addColumnButton.setToolTipText(Strings.get(ADD_COLUMN_TIP));
		addColumnButton.addActionListener(e -> addColumn());
	}
	private void buildComponents() {		panel.add(nameField, "grow, split 2, flowx, gapx 0");
		panel.add(addColumnButton, "gapx 0");
		panel.add(columnsScrollPane, "grow");
		panel.add(submitButton, "split 2, flowx, center, sgx");
		panel.add(backButton, "center, sgx");
	}
	
	private void addColumn() {
		ColumnPanel newColumn = new ColumnPanel();
		newColumn.addDeletionListener(e -> removeColumn(newColumn));
		
		Component[] components = columnsPanel.getComponents();
		columnsPanel.removeAll();
		boolean foundFiller = false;
		for (int i = 0; i < components.length; i++) {
			if (!foundFiller && components[i] instanceof Filler) {
				components[i] = newColumn;
				foundFiller = true;
			}
			columnsPanel.add(components[i]);
		}
		if (!foundFiller)
			columnsPanel.add(newColumn);
		
		columnsPanel.revalidate();
		columnsPanel.repaint();
	}
	private void removeColumn(ColumnPanel column) {
		columnsPanel.remove(column);
		
		columnsPanel.revalidate();
		columnsPanel.repaint();
	}
	
	/** @return current text in column name field */
	public String getName() {
		return nameField.getText();
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
	
	private void fireSubmitted() {
		for (SubmitListener listener : submitListeners)
			listener.submitted(this);
	}
	private void fireCanceled() {		
		for (CancelListener listener : cancelListeners)
			listener.canceled(this);
	}
	
	@Override
	public JPanel getPanel() {
		return panel;
	}
	
	@Override
	public void addSubmitListener(SubmitListener listener) {
		submitListeners.add(listener);
	}
	@Override
	public void removeSubmitListener(SubmitListener listener) {
		submitListeners.remove(listener);
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
	public void clearListeners() {
		submitListeners.clear();
		cancelListeners.clear();
	}
}
