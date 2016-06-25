package dev.kkorolyov.sqlobviewer.gui;

import java.awt.Component;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * A scrollable popup menu.
 */
public class JScrollablePopupMenu extends JPopupMenu {
	private static final long serialVersionUID = -5908433174380937634L;

	private List<JMenuItem> menuList = new ArrayList<>();
	private int base = 0,
							height;
	
	/**
	 * Constructs a new scrollable popup menu with a specified height.
	 * @param height maximum number of simultaneous visible menu items
	 */
	public JScrollablePopupMenu(int height) {
		super();
		this.height = height;
		
		addMouseWheelListener(this::actOnWheel);
	}
	
	private void actOnWheel(MouseWheelEvent e) {
		e.consume();
		
		if (e.getWheelRotation() < 0)
			scrollUp();
		else if (e.getWheelRotation() > 0)
			scrollDown();
	}
	private void scrollUp() {
		if (base > 0) {
			base--;
			
			reloadDisplayedItems();
		}
	}
	private void scrollDown() {
		if ((menuList.size() > height) && (base < (menuList.size() - height))) {
			base++;
			
			reloadDisplayedItems();
		}
	}
	
	private void reloadDisplayedItems() {
		super.removeAll();
		
		int lastShown = lastShown();
		for (int i = base; i <= lastShown; i++) {
			if (menuList.get(i) == null) {
				super.addSeparator();
				
				if (lastShown < menuList.size() - 1)
					lastShown++;
			}
			else
				super.add(menuList.get(i));
		}
		revalidate();
		repaint();
	}
	
	private int lastShown() {
		return (base + (height - 1) >= menuList.size()) ? menuList.size() - 1 : base + (height - 1);
	}
	
	@Override
	public JMenuItem add(JMenuItem menuItem) {
		menuList.add(menuItem);
		
		return menuItem;
	}
	@Override
	public void addSeparator() {
		menuList.add(null);
	}
	
	@Override
	public void show(Component invoker, int x, int y) {
		reloadDisplayedItems();
		super.show(invoker, x, y);
	}
}
