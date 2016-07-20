package dev.kkorolyov.sqlobviewer.gui;

import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.ACTION_OPTIONS_BACK;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.ACTION_OPTIONS_DISCARD;
import static dev.kkorolyov.sqlobviewer.assets.Assets.Keys.ACTION_OPTIONS_SAVE;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.JButton;
import javax.swing.JPanel;

import dev.kkorolyov.simplepropseditor.model.PropsModel;
import dev.kkorolyov.simplepropseditor.view.PropsScreen;
import dev.kkorolyov.sqlobviewer.assets.Assets;
import dev.kkorolyov.sqlobviewer.assets.Assets.Lang;
import dev.kkorolyov.sqlobviewer.gui.event.CancelListener;
import dev.kkorolyov.sqlobviewer.gui.event.CancelSubject;
import net.miginfocom.swing.MigLayout;

/**
 * A prebuilt options screen.
 */
public class OptionsScreen implements Screen, CancelSubject {
	private JPanel panel;
	private PropsScreen propsEditor;
	private JButton cancelButton;
	
	private Set<CancelListener> cancelListeners = new CopyOnWriteArraySet<>();
	
	/**
	 * Constructs a new options screen.
	 */
	public OptionsScreen() {
		initComponents();
		buildComponents();
	}
	private void initComponents() {
		panel = new JPanel(new MigLayout("insets 0, gap 4px, flowy", "grow", "[fill, grow][]"));
		
		propsEditor = new PropsScreen(new PropsModel(Assets.getConfig()), null, null, null, Lang.get(ACTION_OPTIONS_SAVE), Lang.get(ACTION_OPTIONS_DISCARD), false);
		
		cancelButton = new JButton(Lang.get(ACTION_OPTIONS_BACK));
		cancelButton.addActionListener(e -> fireCanceled());
	}
	private void buildComponents() {
		panel.add(propsEditor.getPanel(), "grow");
		panel.add(cancelButton, "center");
	}
	
	@Override
	public boolean focusDefaultComponent() {
		return propsEditor.getPanel().requestFocusInWindow();
	}
	
	@Override
	public JPanel getPanel() {
		return panel;
	}
	
	private void fireCanceled() {
		for (CancelListener listener : cancelListeners)
			listener.canceled(this);
	}
	
	@Override
	public void addCancelListener(CancelListener listener) {
		cancelListeners.add(listener);
	}
	@Override
	public void removeCancelListener(CancelListener listener) {
		cancelListeners.remove(listener);
	}
	
	@Override
	public void clearListeners() {
		cancelListeners.clear();
		
		if (propsEditor != null)
			propsEditor.setModel(null);
	}
}
