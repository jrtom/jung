/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * 
 */

package edu.uci.ics.jung.visualization.annotations;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.io.Serializable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * AnnotationRenderer is similar to the cell renderers
 * used by the JTable and JTree JFC classes.
 * 
 * @author Tom Nelson 
 *
 * 
 */
@SuppressWarnings("serial")
public class AnnotationRenderer extends JLabel implements
        Serializable {

     protected static Border noFocusBorder = new EmptyBorder(0,0,0,0); 
    
    /**
     * Creates a default table cell renderer.
     */
    public AnnotationRenderer() {
        setOpaque(true);
        setBorder(noFocusBorder);
    }

    /**
     * Overrides <code>JComponent.setForeground</code> to assign
     * the unselected-foreground color to the specified color.
     * 
     * @param c set the foreground color to this value
     */
    @Override
    public void setForeground(Color c) {
        super.setForeground(c); 
    }
    
    /**
     * Overrides <code>JComponent.setBackground</code> to assign
     * the unselected-background color to the specified color.
     *
     * @param c set the background color to this value
     */
    @Override
    public void setBackground(Color c) {
        super.setBackground(c); 
    }

    /**
     * Notification from the <code>UIManager</code> that the look and feel
     * has changed.
     * Replaces the current UI object with the latest version from the 
     * <code>UIManager</code>.
     *
     * @see JComponent#updateUI
     */
    @Override
    public void updateUI() {
        super.updateUI(); 
        setForeground(null);
        setBackground(null);
    }
    
    /**
     * Returns the default label renderer.
     *
     * @param vv  the <code>VisualizationViewer</code> to render on
     * @param value  the value to assign to the label
     * @return the default label renderer
     */
    public Component getAnnotationRendererComponent(JComponent vv, Object value) {
        
        super.setForeground(vv.getForeground());
        super.setBackground(vv.getBackground());
        
        setFont(vv.getFont());
        setIcon(null);
        setBorder(noFocusBorder);
        setValue(value); 
        return this;
    }
    
    /*
     * The following methods are overridden as a performance measure to 
     * to prune code-paths are often called in the case of renders
     * but which we know are unnecessary.  Great care should be taken
     * when writing your own renderer to weigh the benefits and 
     * drawbacks of overriding methods like these.
     */

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    @Override
    public boolean isOpaque() { 
        Color back = getBackground();
        Component p = getParent(); 
        if (p != null) { 
            p = p.getParent(); 
        }
        boolean colorMatch = (back != null) && (p != null) && 
        back.equals(p.getBackground()) && 
        p.isOpaque();
        return !colorMatch && super.isOpaque(); 
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    @Override
    public void validate() {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    @Override
    public void revalidate() {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    @Override
    public void repaint(long tm, int x, int y, int width, int height) {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    @Override
    public void repaint(Rectangle r) { }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {	
        // Strings get interned...
        if (propertyName=="text") {
            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { }

    /**
     * Sets the <code>String</code> object for the cell being rendered to
     * <code>value</code>.
     * 
     * @param value  the string value for this cell; if value is
     *		<code>null</code> it sets the text value to an empty string
     * @see JLabel#setText
     * 
     */
    protected void setValue(Object value) {
        setText((value == null) ? "" : value.toString());
    }
}
