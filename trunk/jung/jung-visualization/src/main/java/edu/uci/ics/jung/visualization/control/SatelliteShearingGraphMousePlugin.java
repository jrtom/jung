/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on Aug 15, 2005
 */

package edu.uci.ics.jung.visualization.control;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;

/**
 * Overrides ShearingGraphMousePlugin so that mouse events in the
 * satellite view cause shearing of the main view
 * 
 * @see ShearingGraphMousePlugin
 * @author Tom Nelson 
 *
 */
public class SatelliteShearingGraphMousePlugin extends ShearingGraphMousePlugin {

    public SatelliteShearingGraphMousePlugin() {
        super();
    }

    public SatelliteShearingGraphMousePlugin(int modifiers) {
        super(modifiers);
    }
    
    /**
     * overridden to shear the main view
     */
    public void mouseDragged(MouseEvent e) {
        if(down == null) return;
        VisualizationViewer vv = (VisualizationViewer)e.getSource();
        boolean accepted = checkModifiers(e);
        if(accepted) {
            if(vv instanceof SatelliteVisualizationViewer) {
                VisualizationViewer vvMaster = 
                    ((SatelliteVisualizationViewer)vv).getMaster();
                
                MutableTransformer modelTransformerMaster = 
                	vvMaster.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);

                vv.setCursor(cursor);
                Point2D q = down;
                Point2D p = e.getPoint();
                float dx = (float) (p.getX()-q.getX());
                float dy = (float) (p.getY()-q.getY());

                Dimension d = vv.getSize();
                float shx = 2.f*dx/d.height;
                float shy = 2.f*dy/d.width;
                // I want to compute shear based on the view coordinates of the
                // lens center in the satellite view.
                // translate the master view center to layout coords, then translate
                // that point to the satellite view's view coordinate system....
                Point2D center = vv.getRenderContext().getMultiLayerTransformer().transform(vvMaster.getRenderContext().getMultiLayerTransformer().inverseTransform(vvMaster.getCenter()));
                if(p.getX() < center.getX()) {
                    shy = -shy;
                }
                if(p.getY() < center.getY()) {
                    shx = -shx;
                }
                modelTransformerMaster.shear(-shx, -shy, vvMaster.getCenter());

                down.x = e.getX();
                down.y = e.getY();
            }
            e.consume();
        }
    }
}
