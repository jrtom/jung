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

import java.awt.geom.Point2D;

import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;

/** 
 * LayoutScalingControl applies a scaling transformation to the graph layout.
 * The Vertices get closer or farther apart, but do not themselves change
 * size. ScalingGraphMouse uses MouseWheelEvents to apply the scaling.
 * 
 * @author Tom Nelson
 */
public class LayoutScalingControl implements ScalingControl {

    /**
	 * zoom the display in or out, depending on the direction of the
	 * mouse wheel motion.
	 */
    public void scale(VisualizationServer vv, float amount, Point2D from) {
        
        Point2D ivtfrom = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, from);
        MutableTransformer modelTransformer = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
        modelTransformer.scale(amount, amount, ivtfrom);
        vv.repaint();
    }
}
