package edu.uci.ics.jung.visualization;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * An icon that is made up of a collection of Icons.
 * They are rendered in layers starting with the first
 * Icon added (from the constructor).
 * 
 * @author Tom Nelson
 *
 */
@SuppressWarnings("serial")
public class LayeredIcon extends ImageIcon {

	Set<Icon> iconSet = new LinkedHashSet<Icon>();

	public LayeredIcon(Image image) {
	    super(image);
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
        super.paintIcon(c, g, x, y);
        Dimension d = new Dimension(getIconWidth(), getIconHeight());
		for (Icon icon : iconSet) {
			Dimension id = new Dimension(icon.getIconWidth(), icon.getIconHeight());
			int dx = (d.width - id.width)/2;
			int dy = (d.height - id.height)/2;
			icon.paintIcon(c, g, x+dx, y+dy);
		}
	}

	public void add(Icon icon) {
		iconSet.add(icon);
	}

	public boolean remove(Icon icon) {
		return iconSet.remove(icon);
	}
}