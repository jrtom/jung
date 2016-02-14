/*
 * Copyright (c) 2005, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * Created on Mar 8, 2005
 *
 */
package edu.uci.ics.jung.visualization.control;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.transform.LensTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;

/** 
 * HyperbolicMagnificationGraphMousePlugin changes the magnification
 * within the Hyperbolic projection of the HyperbolicTransformer.
 * 
 * @author Tom Nelson
 */
public class LensMagnificationGraphMousePlugin extends AbstractGraphMousePlugin
    implements MouseWheelListener {

    protected final float floor;
    protected final float ceiling;
    protected final float delta;
    
	/**
	 * Creates an instance with modifier of CTRL_MASK, and default min/max/delta zoom values
	 * of 1/4/0.2.
	 */
	public LensMagnificationGraphMousePlugin() {
	    this(MouseEvent.CTRL_MASK);
	}
    
	/**
	 * Creates an instance with modifier of CTRL_MASK, and the specified zoom parameters.
	 * @param floor the minimum zoom value
	 * @param ceiling the maximum zoom value
	 * @param delta the change in zoom value caused by each mouse event
	 */
    public LensMagnificationGraphMousePlugin(float floor, float ceiling, float delta) {
        this(MouseEvent.CTRL_MASK, floor, ceiling, delta);
    }
    
    /**
     * Creates an instance with the specified modifiers and the default min/max/delta zoom values
     * of 1/4/0.2.
     * @param modifiers the mouse event modifiers to specify
     */
    public LensMagnificationGraphMousePlugin(int modifiers) {
        this(modifiers, 1.0f, 4.0f, .2f);
    }
    
	/**
	 * Creates an instance with the specified mouse event modifiers and zoom parameters.
     * @param modifiers the mouse event modifiers to specify
	 * @param floor the minimum zoom value
	 * @param ceiling the maximum zoom value
	 * @param delta the change in zoom value caused by each mouse event
	 */
    public LensMagnificationGraphMousePlugin(int modifiers, float floor, float ceiling, float delta) {
        super(modifiers);
        this.floor = floor;
        this.ceiling = ceiling;
        this.delta = delta;
    }

    /**
     * override to check equality with a mask
     */
    public boolean checkModifiers(MouseEvent e) {
        return (e.getModifiers() & modifiers) != 0;
    }

    private void changeMagnification(MutableTransformer transformer, float delta) {
        if(transformer instanceof LensTransformer) {
            LensTransformer ht = (LensTransformer)transformer;
            float magnification = ht.getMagnification() + delta;
            magnification = Math.max(floor, magnification);
            magnification = Math.min(magnification, ceiling);
            ht.setMagnification(magnification);
        }
    }
	/**
	 * zoom the display in or out, depending on the direction of the
	 * mouse wheel motion.
	 */
    public void mouseWheelMoved(MouseWheelEvent e) {
        boolean accepted = checkModifiers(e);
        float delta = this.delta;
        if(accepted == true) {
            VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>)e.getSource();
            MutableTransformer modelTransformer = vv.getRenderContext().getMultiLayerTransformer()
            	.getTransformer(Layer.LAYOUT);
            MutableTransformer viewTransformer = vv.getRenderContext().getMultiLayerTransformer()
            	.getTransformer(Layer.VIEW);
            int amount = e.getWheelRotation();
            if(amount < 0) {
                delta = -delta;
            }
            changeMagnification(modelTransformer, delta);
            changeMagnification(viewTransformer, delta);
            vv.repaint();
            e.consume();
        }
    }
}
