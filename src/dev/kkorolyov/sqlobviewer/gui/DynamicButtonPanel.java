package dev.kkorolyov.sqlobviewer.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * A panel with a single button which splits into multiple buttons when hovered over.
 */
public class DynamicButtonPanel extends JPanel {
	private static final long serialVersionUID = -7838929165247651542L;

	private JButton mainButton;
	private List<JButton> buttons = new LinkedList<>();
	
	/**
	 * Constructs a new panel.
	 * @param text main button text
	 */
	public DynamicButtonPanel(String text) {
		setLayout(new GridLayout(1, 0));
		addHoverListener();
		
		initComponents();
		setMainButtonText(text);
		
		shrink();
	}
	@SuppressWarnings("synthetic-access")
	private void addHoverListener() {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				expand();
			}
			@Override
			public void mouseExited(MouseEvent e) {				
				if (!getBounds().contains(SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), getParent())))	// Mouse point converted to same reference as base panel
					shrink();
			}
		});
	}
	private void initComponents() {
		mainButton = new JButton();
		mainButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				e.getComponent().getParent().dispatchEvent(e);
			}
		});
	}
	
	/** @param toAdd button to add */
	public void addButton(JButton toAdd) {
		if (buttons.contains(toAdd))
			return;
		
		buttons.add(prepareButton(toAdd));
		
		fitButtons();
	}
	private JButton prepareButton(JButton toAdd) {
		toAdd.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				e.getComponent().getParent().dispatchEvent(e);
			}
		});
		toAdd.addActionListener(e -> shrink());
		
		toAdd.setMargin(new Insets(0, 0, 0, 0));
		
		return toAdd;
	}
	private void fitButtons() {
		for (JButton button : buttons)
			button.setPreferredSize(new Dimension((int) (mainButton.getPreferredSize().getWidth() / buttons.size()), (int) mainButton.getPreferredSize().getHeight()));
	}
	
	/** @param toRemove button to remove */
	public void removeButton(JButton toRemove) {
		buttons.remove(toRemove);
	}
	
	/** @param text new main button text */
	public void setMainButtonText(String text) {
		mainButton.setText(text);
	}
	
	private void shrink() {
		removeAll();
		
		add(mainButton);
		setPreferredSize(mainButton.getPreferredSize());
		
		revalidate();
		repaint();
	}
	private void expand() {
		removeAll();
		
		for (JButton button : buttons)
			add(button);
		
		revalidate();
		repaint();
	}
}
