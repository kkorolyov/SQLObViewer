package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.REMOVE_COLUMN_TEXT;

import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

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

	private JButton deleteButton;
	private JTextField columnNameField;
	private JComboBox<SqlType> columnTypeComboBox;
	
	/**
	 * Constructs a new column panel.
	 */
	public ColumnPanel() {
		//BoxLayout columnLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
		GridLayout columnLayout = new GridLayout(3, 1);
		setLayout(columnLayout);
		
		initComponents();
		
		Standardizer.standardize(Property.WIDTH, Extreme.MAXIMUM, deleteButton, columnNameField, columnTypeComboBox);
		add(deleteButton);
		add(columnNameField);
		add(columnTypeComboBox);
		
		repaint();
		revalidate();
	}
	private void initComponents() {
		deleteButton = new JButton(Strings.get(REMOVE_COLUMN_TEXT));
		columnNameField = new JTextField();
		columnTypeComboBox = new JComboBox<>(SqlType.values());
	}
	
	/** @param e action which this panel's delete button invokes */
	public void addDeleteButtonListener(ActionListener e) {
		deleteButton.addActionListener(e);
	}
	
	/** @return	a {@code Column} matching the data specified in this panel, or {@code null} if not enough data */
	public Column getColumn() {
		if (columnNameField.getText().length() < 1)	// Name not set
			return null;
		
		return new Column(columnNameField.getText(), (SqlType) columnTypeComboBox.getSelectedItem());
	}
}
