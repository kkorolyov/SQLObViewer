package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.MAX_TABLES_X;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.MAX_TABLES_Y;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import dev.kkorolyov.sqlobviewer.assets.Assets.Config;
import net.miginfocom.swing.MigLayout;

/**
 * Provides access to table-specific settings.
 */
public class TableOptionsScreen implements Screen {
	private static final byte SQUARE_LENGTH = 4;
	
	private int xTables = 1,
							yTables = 1;
	
	private JPanel panel;
		
	/**
	 * Constructs a new table options screen.
	 */
	public TableOptionsScreen() {
		initComponents();
		buildComponents();
	}
	private void initComponents() {
		panel = new JPanel(new MigLayout("insets 0, gap 0, wrap " + SQUARE_LENGTH));		
	}
	private void buildComponents() {
		buildButtons(Integer.parseInt(Config.get(MAX_TABLES_X)), Integer.parseInt(Config.get(MAX_TABLES_Y)));
	}
	private void buildButtons(int maxX, int maxY) {
		int numButtons = maxX * maxY;
		
		for (int i = 0; i < numButtons; i++) {
			JButton currentButton = new JButton();
			currentButton.addActionListener(buildListener((i % SQUARE_LENGTH) + 1, (i / SQUARE_LENGTH) + 1));
			
			panel.add(currentButton);
		}
	}
	private ActionListener buildListener(final int newXTables, final int newYTables) {
		return new ActionListener() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void actionPerformed(ActionEvent e) {
				setNumTables(newXTables, newYTables);
			}
		};
	}
	
	/** @return number of displayed tables along X-axis */
	public int getXTables() {
		return xTables;
	}
	/** @return number of displayed tables along Y-axis */
	public int getYTables() {
		return yTables;
	}
	private void setNumTables(int newXTables, int newYTables) {
		xTables = newXTables;
		yTables = newYTables;
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
