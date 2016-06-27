package dev.kkorolyov.sqlobviewer.utility;

import java.awt.Component;
import java.awt.Dimension;

/**
 * Provides methods to standardize groups of components.
 */
public class Standardizer {

	/**
	 * Standardizes a group of components based on their preferred sizes.
	 * @param property property to standardize, if {@code null}, will standardize both width and height
	 * @param extreme extreme to standardize toward
	 * @param components components to standardize
	 */
	public static void standardize(Property property, Extreme extreme, Component... components) {
		int finalWidth = (extreme == Extreme.MINIMUM) ? Integer.MAX_VALUE : 0,
				finalHeight = (extreme == Extreme.MINIMUM) ? Integer.MAX_VALUE : 0;
	
		for (Component component : components) {
			Dimension currentDimension = component.getPreferredSize();
			
			int currentWidth = (int) currentDimension.getWidth(),
					currentHeight = (int) currentDimension.getHeight();
			
			if (extreme == Extreme.MINIMUM) {
				if (currentWidth < finalWidth)
					finalWidth = currentWidth;
				if (currentHeight < finalHeight)
					finalHeight = currentHeight;
			}
			else if (extreme == Extreme.MAXIMUM) {
				if (currentWidth > finalWidth)
					finalWidth = currentWidth;
				if (currentHeight > finalHeight)
					finalHeight = currentHeight;
			}
		}
		for (Component component : components) {
			int newWidth = (property == null || property == Property.WIDTH) ? finalWidth : (int) component.getPreferredSize().getWidth(),
					newHeight = (property == null || property == Property.HEIGHT) ? finalHeight : (int) component.getPreferredSize().getHeight();
			Dimension newSize = new Dimension(newWidth, newHeight);
			
			component.setMinimumSize(newSize);
			component.setMaximumSize(newSize);
			component.setPreferredSize(newSize);
		}
	}
	
	@SuppressWarnings("javadoc")
	public static enum Property {
		WIDTH,
		HEIGHT;
	}
	@SuppressWarnings("javadoc")
	public static enum Extreme {
		MINIMUM,
		MAXIMUM;
	}
}
