package dev.kkorolyov.sqlobviewer.gui;

import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import dev.kkorolyov.sqlob.connection.TableConnection;
import dev.kkorolyov.sqlobviewer.gui.event.GuiListener;
import dev.kkorolyov.sqlobviewer.gui.event.GuiSubject;

/**
 * A prebuilt view of a database table.
 */
public class TableViewScreen extends JPanel implements GuiSubject {
	private static final long serialVersionUID = -7921939710945709683L;
	
	private Set<GuiListener> 	listeners = new HashSet<>(),
														listenersToRemove = new HashSet<>();
	
	/**
	 * Constructs a new table view screen.
	 * @see #rebuild(TableConnection)
	 */
	public TableViewScreen(TableConnection table) {
		BoxLayout tableViewLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(tableViewLayout);
		
		rebuild(table);
	}
	
	/**
	 * Rebuilds this screen using the specified properties.
	 * @param table table to display
	 */
	public void rebuild(TableConnection table) {
		
	}
	
	@Override
	public void addListener(GuiListener listener) {
		listeners.add(listener);
	}
	@Override
	public void removeListener(GuiListener listener) {
		listenersToRemove.add(listener);
	}
	private void removeQueuedListeners() {
		for (GuiListener listener : listenersToRemove)
			listeners.remove(listener);
		
		listenersToRemove.clear();
	}
}
