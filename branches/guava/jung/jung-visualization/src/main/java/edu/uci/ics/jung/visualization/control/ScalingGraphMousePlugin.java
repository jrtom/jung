/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 * Created on Mar 8, 2005
 *
 */
package edu.uci.ics.jung.visualization.control;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;

import edu.uci.ics.jung.visualization.VisualizationViewer;

/** 
 * ScalingGraphMouse applies a scaling transformation to the graph layout.
 * The Vertices get closer or farther apart, but do not themselves change
 * size. ScalingGraphMouse uses MouseWheelEvents to apply the scaling.
 * 
 * @author Tom Nelson
 */
public class ScalingGraphMousePlugin extends AbstractGraphMousePlugin
    implements MouseWheelListener {

    /**
     * the amount to zoom in by
     */
	protected float in = 1.1f;
	/**
	 * the amount to zoom out by
	 */
	protected float out = 1/1.1f;
	
	/**
	 * whether to center the zoom at the current mouse position
	 */
	protected boolean zoomAtMouse = true;
    
    /**
     * controls scaling operations
     */
    protected ScalingControl scaler;
	
    public ScalingGraphMousePlugin(ScalingControl scaler, int modifiers) {
        this(scaler, modifiers, 1.1f, 1/1.1f);
    }
    
    public ScalingGraphMousePlugin(ScalingControl scaler, int modifiers, float in, float out) {
        super(modifiers);
        this.scaler = scaler;
        this.in = in;
        this.out = out;
    }
   /**
     * @param zoomAtMouse The zoomAtMouse to set.
     */
    public void setZoomAtMouse(boolean zoomAtMouse) {
        this.zoomAtMouse = zoomAtMouse;
    }
    
    public boolean checkModifiers(MouseEvent e) {
        return e.getModifiers() == modifiers || (e.getModifiers() & modifiers) != 0;
    }

    /**
	 * zoom the display in or out, depending on the direction of the
	 * mouse wheel motion.
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {
        boolean accepted = checkModifiers(e);
        if(accepted == true) {
            VisualizationViewer vv = (VisualizationViewer)e.getSource();
            Point2D mouse = e.getPoint();
            Point2D center = vv.getCenter();
            int amount = e.getWheelRotation();
            if(zoomAtMouse) {
                if(amount > 0) {
                    scaler.scale(vv, in, mouse);
                } else if(amount < 0) {
                    scaler.scale(vv, out, mouse);
                }
            } else {
                if(amount > 0) {
                    scaler.scale(vv, in, center);
                } else if(amount < 0) {
                    scaler.scale(vv, out, center);
                }
            }
            e.consume();
            vv.repaint();
        }
	}
    /**
     * @return Returns the zoom in value.
     */
    public float getIn() {
        return in;
    }
    /**
     * @param in The zoom in value to set.
     */
    public void setIn(float in) {
        this.in = in;
    }
    /**
     * @return Returns the zoom out value.
     */
    public float getOut() {
        return out;
    }
    /**
     * @param out The zoom out value to set.
     */
    public void setOut(float out) {
        this.out = out;
    }

    public ScalingControl getScaler() {
        return scaler;
    }

    public void setScaler(ScalingControl scaler) {
        this.scaler = scaler;
    }
}
