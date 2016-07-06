package dev.kkorolyov.sqlobviewer.gui;

import java.awt.Color;
import java.awt.event.*;

import javax.swing.JTextField;

/**
 * A {@code JTextField} with placeholder text.
 */
public class JPlaceholderTextField extends JTextField {
	private static final long serialVersionUID = 2079811371568110689L;
	
	private String placeholderText;
	private boolean placeholderActive;
	private Color trueColor;
	
	/**
	 * Constructs a new placeholder text field.
	 * @param text placeholder text
	 * @param columns number of columns
	 */
	public JPlaceholderTextField(String text, int columns) {
		super(columns);
		trueColor = getForeground();
		
		setPlaceholderText(text);
		applyPlaceholder();
		
		addFocusListener();
		addKeyListener();
	}
	@SuppressWarnings("synthetic-access")
	private void addFocusListener() {
		addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				tryApplyPlaceholder();
			}
			@Override
			public void focusGained(FocusEvent e) {
				tryApplyPlaceholder();
			}
		});
	}
	private void addKeyListener() {
		addKeyListener(new KeyAdapter() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() != KeyEvent.CHAR_UNDEFINED)
					tryRemovePlaceholder();
			}
		});
	}
	
	private void tryApplyPlaceholder() {
		if (getText().length() <= 0)
			applyPlaceholder();
	}
	private void applyPlaceholder() {
		placeholderActive = true;
		
		super.setForeground(Color.GRAY);
		setText(placeholderText);
		setCaretPosition(0);
	}
	
	private void tryRemovePlaceholder() {
		if (placeholderActive)
			removePlaceholder();
	}
	private void removePlaceholder() {
		placeholderActive = false;
		
		super.setForeground(trueColor);
		setText("");
	}
	
	/** @param newPlaceholderText new placeholder text */
	public void setPlaceholderText(String newPlaceholderText) {
		placeholderText = newPlaceholderText;
	}
	
	@Override
	public String getText() {
		return placeholderActive ? "" : super.getText();
	}
	
	@Override
	public void setForeground(Color fg) {
		trueColor = fg;
		
		if (!placeholderActive)
			super.setForeground(fg);
	}
}
