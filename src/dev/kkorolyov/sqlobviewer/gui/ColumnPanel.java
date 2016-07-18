package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.MESSAGE_TIP_COLUMN_NAME;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.MESSAGE_TIP_COLUMN_TYPE;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.ACTION_REMOVE_COLUMN;

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
public class ColumnPanel extends JPanel {	// TODO Encapsulate instead of extend
	private static final long serialVersionUID = 3309134424336942020L;

	private JTextField columnNameField;
	private JComboBox<SqlType> columnTypeComboBox;
	private JPopupMenu deleteMenu;
	private JMenuItem deleteMenuItem;
	
	/**
	 * Constructs a new column panel.
	 */
	public ColumnPanel() {
		GridLayout columnLayout = new GridLayout(2, 1);
		setLayout(columnLayout);
				
		initComponents();
		addDeletePopupListeners();

		add(columnNameField);
		add(columnTypeComboBox);
		
		repaint();
		revalidate();
	}
	private void addDeletePopupListeners() {
		MouseListener popupListener = buildPopupListener();
		
		addMouseListener(popupListener);
		columnNameField.addMouseListener(popupListener);
		columnTypeComboBox.addMouseListener(popupListener);
	}
	@SuppressWarnings("synthetic-access")
	private MouseListener buildPopupListener() {
		return new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				tryShowDeletePopup(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				tryShowDeletePopup(e);
			}
		};
	}
	
	private void initComponents() {
		columnNameField = new JPlaceholderTextField(Lang.get(MESSAGE_TIP_COLUMN_NAME), 0);
		columnNameField.setToolTipText(Lang.get(MESSAGE_TIP_COLUMN_NAME));
		
		columnTypeComboBox = new JComboBox<>(SqlType.values());
		columnTypeComboBox.setToolTipText(Lang.get(MESSAGE_TIP_COLUMN_TYPE));
		
		deleteMenu = new JPopupMenu();
		deleteMenuItem = new JMenuItem(Lang.get(ACTION_REMOVE_COLUMN));
		deleteMenu.add(deleteMenuItem);
	}
	
	/** @param listener listener to notify when a deletion event is fired by this component */
	public void addDeletionListener(ActionListener listener) {
		deleteMenuItem.addActionListener(listener);
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
}
