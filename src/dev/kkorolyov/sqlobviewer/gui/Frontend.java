package dev.kkorolyov.sqlobviewer.gui;

import javax.swing.JFrame;

/**
 * SQLObViewer frontend.
 */
public class Frontend {	
	private JFrame frame;
	private Backend backend;
	
	/**
	 * Constructs a new frontend.
	 * @param title frame title
	 * @param width initial width
	 * @param height initial height
	 */
	public Frontend(String title, int width, int height) {
		buildFrame(title, width, height);
	}
	private void buildFrame(String title, int width, int height) {
		frame = new JFrame(title);
		frame.setSize(width, height);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	/** @return main GUI frame */
	public JFrame getFrame() {
		return frame;
	}
	
	/** @param newBackend new backend */
	public void setBackend(Backend newBackend) {
		backend = newBackend;
	}
}
