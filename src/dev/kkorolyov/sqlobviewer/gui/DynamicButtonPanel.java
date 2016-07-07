package dev.kkorolyov.sqlobviewer.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

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
		setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
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
				System.out.println(getBounds());
				System.out.println(e.getPoint());
				if (getBounds().contains(e.getPoint())) {
					System.out.println("aborting");
					return;
				}
				else {
					System.out.println("shrinking");
					shrink();
				}
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
		if (!buttons.contains(toAdd)) {
			toAdd.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(MouseEvent e) {
					e.getComponent().getParent().dispatchEvent(e);
				}
			});
			buttons.add(toAdd);
		}
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
