package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.ACTION_REMOVE_COLUMN;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.MESSAGE_TIP_COLUMN_NAME;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.MESSAGE_TIP_COLUMN_TYPE;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;

import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlob.construct.SqlType;
import dev.kkorolyov.sqlobviewer.assets.Assets.Lang;
import dev.kkorolyov.swingplus.JPlaceholderTextField;

/**
 * Contains editable info for a single column.
 */
public class ColumnInfo implements Screen {
	private JPanel panel;
	private JTextField columnNameField;
	private JComboBox<SqlType> columnTypeComboBox;
	private JPopupMenu deleteMenu;
	private JMenuItem deleteItem;
	
	/**
	 * Constructs a new column panel.
	 */
	public ColumnInfo() {
		initComponents();
		buildComponents();
	}
	@SuppressWarnings("synthetic-access")
	private void initComponents() {
		panel = new JPanel(new GridLayout(2, 1));
		
		columnNameField = new JPlaceholderTextField(Lang.get(MESSAGE_TIP_COLUMN_NAME), 0);
		columnNameField.setToolTipText(Lang.get(MESSAGE_TIP_COLUMN_NAME));
		
		columnTypeComboBox = new JComboBox<>(SqlType.values());
		columnTypeComboBox.setToolTipText(Lang.get(MESSAGE_TIP_COLUMN_TYPE));
		
		deleteMenu = new JPopupMenu();
		deleteItem = new JMenuItem(Lang.get(ACTION_REMOVE_COLUMN));
		deleteMenu.add(deleteItem);
		
		MouseListener popupListener = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				tryShowDeletePopup(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				tryShowDeletePopup(e);
			}
		};
		panel.addMouseListener(popupListener);
		columnNameField.addMouseListener(popupListener);
		columnTypeComboBox.addMouseListener(popupListener);
	}
	private void buildComponents() {
		panel.add(columnNameField);
		panel.add(columnTypeComboBox);
	}
	
	/** @param listener listener to notify when a deletion event is fired by this component */
	public void addDeletionListener(ActionListener listener) {
		deleteItem.addActionListener(listener);
	}
	
	/** @return	a {@code Column} matching the data specified in this panel, or {@code null} if not enough data */
	public Column getColumn() {
		if (columnNameField.getText().length() < 1)	// Name not set
			return null;
		
		return new Column(columnNameField.getText(), (SqlType) columnTypeComboBox.getSelectedItem());
	}
	
	private void tryShowDeletePopup(MouseEvent e) {
		if (e.isPopupTrigger())
			showDeletePopup(e);
	}
	private void showDeletePopup(MouseEvent e) {
		deleteMenu.show(e.getComponent(), e.getX(), e.getY());
	}
	
	@Override
	public boolean focusDefaultComponent() {
		return false;
	}
	@Override
	public JPanel getPanel() {
		return panel;
	}
}
