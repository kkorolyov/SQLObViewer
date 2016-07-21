package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.CURRENT_TABLES_X;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.CURRENT_TABLES_Y;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.*;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import dev.kkorolyov.sqlobviewer.assets.Assets.Config;

/**
 * Selects a span of cells in a grid.
 */
public class GridSelector implements Screen {
	private JPanel panel;
	private JButton[][] gridButtons;
	
	private Set<ChangeListener> changeListeners = new CopyOnWriteArraySet<>();
	
	/**
	 * Constructs a new grid selector.
	 * @param maxX number of grid cells along the X-axis
	 * @param maxY number of grid cell along the Y-axis
	 */
	public GridSelector(int maxX, int maxY) {
		gridButtons = new JButton[maxY][maxX];
		
		initComponents();
		buildComponents();
	}
	@SuppressWarnings("synthetic-access")
	private void initComponents() {
		panel = new JPanel(new GridLayout(gridButtons.length, gridButtons[0].length, 0, 0)) {
			private static final long serialVersionUID = 7892116516791763651L;
			
			@Override
			public Dimension getPreferredSize() {
				Dimension preferred = super.getSize();
				int length = (int) (preferred.getWidth() > preferred.getHeight() ? preferred.getWidth() : preferred.getHeight());
				return new Dimension(length, length);
			}
			@Override
			public void setPreferredSize(Dimension preferredSize) {
				super.setPreferredSize(preferredSize);
				super.setPreferredSize(getPreferredSize());
			}
		};
		panel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				panel.revalidate();
				panel.repaint();
			}
		});
	}
	private void buildComponents() {
		buildGridButtons();
	}
	private void buildGridButtons() {
		for (int i = 0; i < gridButtons.length; i++) {
			for (int j = 0; j < gridButtons[i].length; j++) {
				gridButtons[i][j] = buildGridButton(i, j);
				panel.add(gridButtons[i][j]);
			}
		}
		setGridSpanEnabled(Config.getInt(CURRENT_TABLES_X) - 1, Config.getInt(CURRENT_TABLES_Y) - 1, true);
	}
	@SuppressWarnings("synthetic-access")
	private JButton buildGridButton(int x, int y) {
		JButton gridButton = new JButton();
		gridButton.setToolTipText((x + 1) + "x" + (y + 1));
		gridButton.setMargin(new Insets(0, 0, 0, 0));
		gridButton.setEnabled(false);
		gridButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				setGridEnabled(false);
				setGridSpanEnabled(x, y, true);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				setGridEnabled(false);
				setGridSpanEnabled(Config.getInt(CURRENT_TABLES_X) - 1, Config.getInt(CURRENT_TABLES_Y) - 1, true);
			}
		});
		gridButton.addActionListener(e -> {
			Config.set(CURRENT_TABLES_X, String.valueOf(x + 1));
			Config.set(CURRENT_TABLES_Y, String.valueOf(y + 1));
			Config.save();
			
			fireStateChanged();
		});
		return gridButton;
	}
	
	private void setGridSpanEnabled(int endX, int endY, boolean enabled) {
		for (int i = 0; i <= endX; i++) {
			for (int j = 0; j <= endY; j++)
				gridButtons[i][j].setEnabled(enabled);
		}
	}
	private void setGridEnabled(boolean enabled) {
		setGridSpanEnabled(gridButtons.length - 1, gridButtons[0].length - 1, enabled);
	}
	
	@Override
	public boolean focusDefaultComponent() {
		return false;
	}
	@Override
	public JPanel getPanel() {
		return panel;
	}
	
	private void fireStateChanged() {
		for (ChangeListener listener : changeListeners)
			listener.stateChanged(new ChangeEvent(this));
	}
	
	/** @param listener change listener to add */
	public void addChangeListener(ChangeListener listener) {
		changeListeners.add(listener);
	}
	/** @param listener change listener to remove */
	public void removeChangeListener(ChangeListener listener) {
		changeListeners.remove(listener);
	}
}
