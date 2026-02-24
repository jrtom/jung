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
 * A scaling control that has a crossover point. When the overall scale of the view and model is
 * less than the crossover point, the scaling is applied to the view's transform and the graph
 * nodes, labels, etc grow smaller. This preserves the overall shape of the graph. When the scale is
 * larger than the crossover, the scaling is applied to the graph layout. The graph spreads out, but
 * the nodes and labels grow no larger than their original layoutSize.
 *
 * @author Tom Nelson
 */
open class CrossoverScalingControl : ScalingControl {

    /** Point where scale crosses over from view to layout. */
    var crossover: Double = 1.0

    override fun scale(vv: VisualizationServer<*, *>, amount: Float, at: Point2D) {
        val layoutTransformer =
            vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)
        val viewTransformer =
            vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)
        val modelScale = layoutTransformer.getScale()
        val viewScale = viewTransformer.getScale()
        val inverseModelScale = Math.sqrt(crossover) / modelScale
        val inverseViewScale = Math.sqrt(crossover) / viewScale
        val scale = modelScale * viewScale

        val transformedAt =
            vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, at)!!

        if ((scale * amount.toDouble() - crossover) * (scale * amount.toDouble() - crossover) < 0.001) {
            // close to the control point, return both Functions to a scale of sqrt crossover value
            layoutTransformer.scale(inverseModelScale, inverseModelScale, transformedAt)
            viewTransformer.scale(inverseViewScale, inverseViewScale, at)
        } else if (scale * amount.toDouble() < crossover) {
            // scale the viewTransformer, return the layoutTransformer to sqrt crossover value
            viewTransformer.scale(amount.toDouble(), amount.toDouble(), at)
            layoutTransformer.scale(inverseModelScale, inverseModelScale, transformedAt)
        } else {
            // scale the layoutTransformer, return the viewTransformer to crossover value
            layoutTransformer.scale(amount.toDouble(), amount.toDouble(), transformedAt)
            viewTransformer.scale(inverseViewScale, inverseViewScale, at)
        }
        vv.repaint()
    }
}
