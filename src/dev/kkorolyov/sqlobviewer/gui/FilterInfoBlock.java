package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.*;

import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import dev.kkorolyov.sqlobviewer.assets.Assets.Strings;

/**
 * Displays info for a single filter.
 */
public class FilterInfoBlock implements Screen {	
	private JPanel panel;
	private JLabel text;
	private JButton removeButton;
	
	/**
	 * Constructs a new display
	 * @param column filter column index
	 * @param columnName filter column name
	 * @param filterText filter criteria text
	 */
	public FilterInfoBlock(int column, String columnName, String filterText) {
		panel = new JPanel();
		
		text = new JLabel("[" + column + "] " + columnName + ": " + filterText);
		removeButton = new JButton(Strings.get(MINIMAL_REMOVE_TEXT));	// TODO To popup
		removeButton.setToolTipText(Strings.get(REMOVE_FILTER_TEXT));
		removeButton.setMargin(new Insets(0, 0, 0, 0));
		
		buildComponents();
	}
	private void buildComponents() {
		panel.add(text);
		panel.add(removeButton);
	}
	
	/** @param listener listener to notify when a remove filter event is fired by this component */
	public void addRemovalListener(ActionListener listener) {
		removeButton.addActionListener(listener);
	}
	
	@Override
	public JPanel getPanel() {
		return panel;
	}
}
