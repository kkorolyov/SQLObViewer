package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.ApplicationProperties.Keys.*;

import java.awt.Component;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.*;
import javax.swing.Box.Filler;

import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlobviewer.assets.ApplicationProperties.Lang;
import dev.kkorolyov.sqlobviewer.gui.event.CancelListener;
import dev.kkorolyov.sqlobviewer.gui.event.CancelSubject;
import dev.kkorolyov.sqlobviewer.gui.event.SubmitListener;
import dev.kkorolyov.sqlobviewer.gui.event.SubmitSubject;
import dev.kkorolyov.swingplus.JPlaceholderTextField;
import net.miginfocom.swing.MigLayout;

/**
 * A prebuilt create table screen.
 */
public class CreateTableScreen implements Screen, SubmitSubject, CancelSubject {
	private static final byte NUM_FILLER_COLUMNS = 4;
	
	private boolean standalone;
	
	private JPanel panel;
	private JTextField nameField;
	private JButton submitButton,
									backButton,
									addColumnButton;
	private JPanel columnsPanel;
	private JScrollPane columnsScrollPane;
	private Set<ColumnInfo> columns = new HashSet<>();
	
	private Set<SubmitListener> submitListeners = new CopyOnWriteArraySet<>();
	private Set<CancelListener> cancelListeners = new CopyOnWriteArraySet<>();
	
	/**
	 * Constructs a new create table screen.
	 * @param isStandalone if {@code true}, this screen will have its own, native OK-CANCEL buttons
	 */
	public CreateTableScreen(boolean isStandalone) {
		standalone = isStandalone;
		
		initComponents();
		buildComponents();
		
		addColumn();
	}
	private void initComponents() {
		panel = new JPanel(new MigLayout("insets 0, gap 4px, flowy", "[grow]", "[]push[]push[]"));
		
		columnsPanel = new JPanel();
		int strutWidth = (int) new ColumnInfo().getPanel().getPreferredSize().getWidth();
		for (int i = 0; i < NUM_FILLER_COLUMNS; i++)
			columnsPanel.add(Box.createHorizontalStrut(strutWidth));
		
		columnsScrollPane = new JScrollPane(columnsPanel);
		columnsScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, (standalone ? 4 : 0), 0));
		columnsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		
		columnsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, (int) columnsScrollPane.getHorizontalScrollBar().getPreferredSize().getHeight(), 0));
		
		nameField = new JPlaceholderTextField(Lang.get(MESSAGE_TIP_TABLE_NAME), 0);
		nameField.setToolTipText(Lang.get(MESSAGE_TIP_TABLE_NAME));
		
		if (standalone) {	// Not required if in a dialog
			submitButton = new JButton(Lang.get(ACTION_SUBMIT));
			submitButton.addActionListener(e -> fireSubmitted());
			
			backButton = new JButton(Lang.get(ACTION_CANCEL));
			backButton.addActionListener(e -> fireCanceled());
		}
		addColumnButton = new JButton(Lang.get(ACTION_ADD_COLUMN));
		addColumnButton.setToolTipText(Lang.get(ACTION_TIP_ADD_COLUMN));
		addColumnButton.addActionListener(e -> addColumn());
	}
	private void buildComponents() {		panel.add(nameField, "grow, split 2, flowx, gapx 0");
		panel.add(addColumnButton, "gapx 0");
		panel.add(columnsScrollPane, "grow");
		
		if (standalone) {	// Not required if in a dialog
			panel.add(submitButton, "split 2, flowx, center, sgx");
			panel.add(backButton, "center, sgx");
		}
	}
	
	private void addColumn() {
		ColumnInfo newColumn = new ColumnInfo();
		newColumn.addDeletionListener(e -> removeColumn(newColumn));
		columns.add(newColumn);
		
		Component[] components = columnsPanel.getComponents();
		columnsPanel.removeAll();
		boolean foundFiller = false;
		for (int i = 0; i < components.length; i++) {
			if (!foundFiller && components[i] instanceof Filler) {
				components[i] = newColumn.getPanel();
				foundFiller = true;
			}
			columnsPanel.add(components[i]);
		}
		if (!foundFiller)
			columnsPanel.add(newColumn.getPanel());
		
		columnsPanel.revalidate();
		columnsPanel.repaint();
	}
	private void removeColumn(ColumnInfo column) {
		columns.remove(column);
		columnsPanel.remove(column.getPanel());
		
		columnsPanel.revalidate();
		columnsPanel.repaint();
	}
	
	/** @return current text in table name field */
	public String getName() {
		return nameField.getText();
	}
	
	/** @return	an array of {@code Column} objects matching all the {@code ColumnInfo} objects displayed on this screen */
	public Column[] getColumns() {
		List<Column> allColumns = new LinkedList<>();
		
		for (ColumnInfo column : columns) {
			Column currentColumn = column.getColumn();
			
			if (currentColumn != null)
				allColumns.add(currentColumn);
		}
		return allColumns.toArray(new Column[allColumns.size()]);
	}
	
	@Override
	public boolean focusDefaultComponent() {
		return nameField.requestFocusInWindow();
	}
	
	@Override
	public JPanel getPanel() {
		return panel;
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
