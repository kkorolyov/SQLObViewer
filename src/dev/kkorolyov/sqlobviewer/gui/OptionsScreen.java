package dev.kkorolyov.sqlobviewer.gui;

import javax.swing.JPanel;

import dev.kkorolyov.simplepropseditor.model.PropsModel;
import dev.kkorolyov.simplepropseditor.view.PropsScreen;

/**
 * A prebuilt options screen.
 */
public class OptionsScreen implements Screen {
	PropsModel model;
	PropsScreen screen;
	
	@Override
	public JPanel getPanel() {
		return screen.getPanel();
	}
}
