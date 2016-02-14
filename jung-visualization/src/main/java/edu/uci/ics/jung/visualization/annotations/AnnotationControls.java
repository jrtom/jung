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
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 * a collection of controls for annotations.
 * allows selection of colors, shapes, etc
 * @author Tom Nelson - tomnelson@dev.java.net
 *
 */
public class AnnotationControls<V,E> {
	
	protected AnnotatingGraphMousePlugin<V,E> annotatingPlugin;

	public AnnotationControls(AnnotatingGraphMousePlugin<V,E> annotatingPlugin) {
		this.annotatingPlugin = annotatingPlugin;
	}
	
    @SuppressWarnings("serial")
    public JComboBox<Shape> getShapeBox() {
    	JComboBox<Shape> shapeBox = new JComboBox<Shape>(
    			new Shape[] {
    					new Rectangle2D.Double(),
    					new RoundRectangle2D.Double(0,0,0,0,50,50),
    					new Ellipse2D.Double()
    			});
    	shapeBox.setRenderer(new DefaultListCellRenderer() {
    		@Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
    			int index, boolean isSelected, boolean hasFocus) {
    			String valueString = value.toString();
    			valueString = valueString.substring(0,valueString.indexOf("2D"));
    			valueString = valueString.substring(valueString.lastIndexOf('.')+1);
    			return super.getListCellRendererComponent(list, valueString, index,
    					isSelected, hasFocus);
    		}
    	});
    	shapeBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					annotatingPlugin.setRectangularShape((RectangularShape)e.getItem());
				}
				
			}});
    	return shapeBox;
    }
    
    public JButton getColorChooserButton() {
    	final JButton colorChooser = new JButton("Color");
    	colorChooser.setForeground(annotatingPlugin.getAnnotationColor());
    	colorChooser.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Color color = JColorChooser.showDialog(colorChooser, "Annotation Color", 
						colorChooser.getForeground());
				annotatingPlugin.setAnnotationColor(color);
				colorChooser.setForeground(color);
			}});
    	return colorChooser;
    }
    
    public JComboBox<Annotation.Layer> getLayerBox() {
    	final JComboBox<Annotation.Layer> layerBox = new JComboBox<Annotation.Layer>(
    			new Annotation.Layer[] {
    			Annotation.Layer.LOWER, Annotation.Layer.UPPER
    			});
    	layerBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					annotatingPlugin.setLayer((Annotation.Layer)e.getItem());
				}
				
			}});

    	return layerBox;
    }

    public JToggleButton getFillButton() {
    	JToggleButton fillButton = new JToggleButton("Fill");
    	fillButton.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				annotatingPlugin.setFill(e.getStateChange() == ItemEvent.SELECTED);
				
			}});
    	return fillButton;
    }
    
    public JToolBar getAnnotationsToolBar() {
    	JToolBar toolBar = new JToolBar();
    	toolBar.add(this.getShapeBox());
    	toolBar.add(this.getColorChooserButton());
    	toolBar.add(this.getFillButton());
    	toolBar.add(this.getLayerBox());
    	return toolBar;
    	
    }

	

}
