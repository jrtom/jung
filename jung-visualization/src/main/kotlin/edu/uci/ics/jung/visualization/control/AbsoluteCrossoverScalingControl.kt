/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.visualization.control

import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer
import edu.uci.ics.jung.visualization.VisualizationServer
import java.awt.geom.Point2D

/**
 * Scales to the absolute value passed as an argument. It first resets the scaling Functions, then
 * uses the relative CrossoverScalingControl to achieve the absolute value.
 *
 * @author Tom Nelson
 */
class AbsoluteCrossoverScalingControl : CrossoverScalingControl(), ScalingControl {

    /**
     * Scale to the absolute value passed as 'amount'.
     *
     * @param vv the VisualizationServer used for rendering; provides the layout and view
     *     transformers.
     * @param amount the amount by which to scale
     * @param at the point of reference for scaling
     */
    override fun scale(vv: VisualizationServer<*, *>, amount: Float, at: Point2D) {
        val layoutTransformer =
            vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)
        val viewTransformer =
            vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)
        val modelScale = layoutTransformer.getScale()
        val viewScale = viewTransformer.getScale()
        val inverseModelScale = Math.sqrt(crossover) / modelScale
        val inverseViewScale = Math.sqrt(crossover) / viewScale

        val transformedAt =
            vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, at)!!

        // return the Functions to 1.0
        layoutTransformer.scale(inverseModelScale, inverseModelScale, transformedAt)
        viewTransformer.scale(inverseViewScale, inverseViewScale, at)

        super.scale(vv, amount, at)
    }
}
