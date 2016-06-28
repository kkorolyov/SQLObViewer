package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.REMOVE_COLUMN_TEXT;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;

import dev.kkorolyov.sqlob.construct.Column;
import dev.kkorolyov.sqlob.construct.SqlType;
import dev.kkorolyov.sqlobviewer.assets.Assets.Strings;
import dev.kkorolyov.sqlobviewer.utility.Standardizer;
import dev.kkorolyov.sqlobviewer.utility.Standardizer.Extreme;
import dev.kkorolyov.sqlobviewer.utility.Standardizer.Property;

/**
 * Contains editable info for a single column.
 */
public class ColumnPanel extends JPanel {
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

		Standardizer.standardize(Property.WIDTH, Extreme.MAXIMUM, columnNameField, columnTypeComboBox);
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
		columnNameField = new JTextField();
		
		columnTypeComboBox = new JComboBox<>(SqlType.values());
		
		deleteMenu = new JPopupMenu();
		deleteMenuItem = new JMenuItem(Strings.get(REMOVE_COLUMN_TEXT));
		deleteMenu.add(deleteMenuItem);
	}
	
	/** @param e action invoked when a deletion event occurs on this component */
	public void addDeletionListener(ActionListener e) {
		deleteMenuItem.addActionListener(e);
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
