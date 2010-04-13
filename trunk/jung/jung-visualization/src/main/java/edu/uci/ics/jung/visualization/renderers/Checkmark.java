package edu.uci.ics.jung.visualization.renderers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.Collections;

import javax.swing.Icon;

/**
 * a simple Icon that draws a checkmark in the lower-right quadrant of its
 * area. Used to draw a checkmark on Picked Vertices.
 * @author Tom Nelson
 */
public class Checkmark implements Icon {

	GeneralPath path = new GeneralPath();
	AffineTransform highlight = AffineTransform.getTranslateInstance(-1,-1);
	AffineTransform lowlight = AffineTransform.getTranslateInstance(1,1);
	AffineTransform shadow = AffineTransform.getTranslateInstance(2,2);
	Color color;
	public Checkmark() {
		this(Color.green);
	}
	public Checkmark(Color color) {
		this.color = color;
		path.moveTo(10,17);
		path.lineTo(13,20);
		path.lineTo(20,13);
	}
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Shape shape = AffineTransform.getTranslateInstance(x, y).createTransformedShape(path);
		Graphics2D g2d = (Graphics2D)g;
		g2d.addRenderingHints(Collections.singletonMap(RenderingHints.KEY_ANTIALIASING, 
				RenderingHints.VALUE_ANTIALIAS_ON));
		Stroke stroke = g2d.getStroke();
		g2d.setStroke(new BasicStroke(4));
		g2d.setColor(Color.darkGray);
		g2d.draw(shadow.createTransformedShape(shape));
		g2d.setColor(Color.black);
		g2d.draw(lowlight.createTransformedShape(shape));
		g2d.setColor(Color.white);
		g2d.draw(highlight.createTransformedShape(shape));
		g2d.setColor(color);
		g2d.draw(shape);
		g2d.setStroke(stroke);
	}

	public int getIconWidth() {
		return 20;
	}

	public int getIconHeight() {
		return 20;
	}
}