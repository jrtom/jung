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

import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import java.awt.geom.Point2D;

/**
 * LayoutScalingControl applies a scaling transformation to the graph layout. The Vertices get
 * closer or farther apart, but do not themselves change layoutSize. ScalingGraphMouse uses
 * MouseWheelEvents to animate the scaling.
 *
 * @author Tom Nelson
 */
public class LayoutScalingControl implements ScalingControl {

  /** zoom the display in or out, depending on the direction of the mouse wheel motion. */
  public void scale(VisualizationServer<?, ?> vv, float amount, Point2D from) {

    Point2D ivtfrom =
        vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, from);
    MutableTransformer modelTransformer =
        vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
    modelTransformer.scale(amount, amount, ivtfrom);
    vv.repaint();
  }
}
