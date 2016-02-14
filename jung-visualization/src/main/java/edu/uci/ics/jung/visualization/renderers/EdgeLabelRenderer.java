/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Apr 14, 2005
 */

package edu.uci.ics.jung.visualization.renderers;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JComponent;

/**
 * @author Tom Nelson
 *
 * 
 */
public interface EdgeLabelRenderer {
	/**
     * Returns the component used for drawing the label.  This method is
     * used to configure the renderer appropriately before drawing.
	 * 
	 * @param component the component that is asking the renderer to draw
	 * @param value the value of the cell to be rendered; the details of how to
	 * 		render the value are up to the renderer implementation.  For example,
	 * 		if {@code value} is the string "true", it could be rendered as the
	 * 		string or as a checked checkbox.  
	 * @param font the font to use in rendering the label
	 * @param isSelected whether the edge is currently selected
	 * @param edge the edge whose label is being drawn
	 * @param <E> the edge type
	 * @return the component used for drawing the label
	 */
    <E> Component getEdgeLabelRendererComponent(JComponent component,
    		Object value, Font font, boolean isSelected, E edge);
    
    boolean isRotateEdgeLabels();
    
    void setRotateEdgeLabels(boolean state);
}
