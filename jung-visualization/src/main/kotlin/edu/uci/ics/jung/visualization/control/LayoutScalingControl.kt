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
package edu.uci.ics.jung.visualization.control

import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer
import edu.uci.ics.jung.visualization.VisualizationServer
import java.awt.geom.Point2D

/**
 * LayoutScalingControl applies a scaling transformation to the graph layout. The Nodes get closer
 * or farther apart, but do not themselves change layoutSize. ScalingGraphMouse uses
 * MouseWheelEvents to apply the scaling.
 *
 * @author Tom Nelson
 */
class LayoutScalingControl : ScalingControl {

    /** zoom the display in or out, depending on the direction of the mouse wheel motion. */
    override fun scale(vv: VisualizationServer<*, *>, amount: Float, from: Point2D) {
        val ivtfrom =
            vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, from)!!
        val modelTransformer =
            vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)
        modelTransformer.scale(amount.toDouble(), amount.toDouble(), ivtfrom)
        vv.repaint()
    }
}
