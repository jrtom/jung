/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 11, 2005
 */

package edu.uci.ics.jung.visualization.transform.shape;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Shape;

import javax.swing.CellRendererPane;
import javax.swing.Icon;


/**
 * an extendion of Graphics2DWrapper that adds enhanced
 * methods for drawing icons and components
 * 
 * @see TransformingGraphics as an example subclass
 * 
 * @author Tom Nelson 
 *
 *
 */
public class GraphicsDecorator extends Graphics2DWrapper {
    
    public GraphicsDecorator() {
        this(null);
    }
    public GraphicsDecorator(Graphics2D delegate) {
        super(delegate);
    }
    
    public void draw(Icon icon, Component c, Shape clip, int x, int y) {
    	int w = icon.getIconWidth();
    	int h = icon.getIconHeight();
    	icon.paintIcon(c, delegate, x-w/2, y-h/2);
    }
    
    public void draw(Component c, CellRendererPane rendererPane, 
    		int x, int y, int w, int h, boolean shouldValidate) {
    	rendererPane.paintComponent(delegate, c, c.getParent(), x, y, w, h, shouldValidate);
    }
}
