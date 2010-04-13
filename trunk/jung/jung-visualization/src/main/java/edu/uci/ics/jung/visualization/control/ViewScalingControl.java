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
 * ViewScalingGraphMouse applies a scaling transform to the View
 * of the graph. This causes all elements of the graph to grow
 * larger or smaller. ViewScalingGraphMouse, by default, is activated
 * by the MouseWheel when the control key is pressed. The control
 * key modifier can be overridden in the contstructor.
 * 
 * @author Tom Nelson
 */
public class ViewScalingControl implements ScalingControl {

	/**
	 * zoom the display in or out, depending on the direction of the
	 * mouse wheel motion.
	 */
    public void scale(VisualizationServer vv, float amount, Point2D from) {
        MutableTransformer viewTransformer = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW);
        viewTransformer.scale(amount, amount, from);
        vv.repaint();
    }
}
