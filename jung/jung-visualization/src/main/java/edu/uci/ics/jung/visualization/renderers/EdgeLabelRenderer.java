/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
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
     *  Returns the component used for drawing the label.  This method is
     *  used to configure the renderer appropriately before drawing.
     *
     * @param	vv		the <code>JComponent</code> that is asking the 
     *				renderer to draw; can be <code>null</code>
     * @param	value		the value of the cell to be rendered.  It is
     *				up to the specific renderer to interpret
     *				and draw the value.  For example, if
     *				<code>value</code>
     *				is the string "true", it could be rendered as a
     *				string or it could be rendered as a check
     *				box that is checked.  <code>null</code> is a
     *				valid value
     * @param	vertex  the vertex for the label being drawn.
     */
    <T> Component getEdgeLabelRendererComponent(JComponent vv, Object value,
					   Font font, boolean isSelected, T edge);
    
    boolean isRotateEdgeLabels();
    
    void setRotateEdgeLabels(boolean state);
}
