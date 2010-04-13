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

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;

/**
 * Mouse events in the SatelliteView that match the modifiers
 * will cause the Main view to rotate
 * @see RotatingGraphMousePlugin
 * @author Tom Nelson 
 *
 */
public class SatelliteRotatingGraphMousePlugin extends RotatingGraphMousePlugin {

    public SatelliteRotatingGraphMousePlugin() {
        super();
    }

    public SatelliteRotatingGraphMousePlugin(int modifiers) {
        super(modifiers);
    }
    /**
     * check the modifiers. If accepted, use the mouse drag motion
     * to rotate the graph in the master view
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

                // rotate
                vv.setCursor(cursor);
                // I want to compute rotation based on the view coordinates of the
                // lens center in the satellite view.
                // translate the master view center to layout coords, then translate
                // that point to the satellite view's view coordinate system....
                Point2D center = vv.getRenderContext().getMultiLayerTransformer().transform(vvMaster.getRenderContext().getMultiLayerTransformer().inverseTransform(vvMaster.getCenter()));
                Point2D q = down;
                Point2D p = e.getPoint();
                Point2D v1 = new Point2D.Double(center.getX()-p.getX(), center.getY()-p.getY());
                Point2D v2 = new Point2D.Double(center.getX()-q.getX(), center.getY()-q.getY());
                double theta = angleBetween(v1, v2);
                modelTransformerMaster.rotate(-theta, 
                        vvMaster.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, vvMaster.getCenter()));
                down.x = e.getX();
                down.y = e.getY();
            } 
            e.consume();
        }
    }

}
