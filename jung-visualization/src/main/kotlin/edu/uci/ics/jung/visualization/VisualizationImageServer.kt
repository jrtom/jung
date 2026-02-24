/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization

import com.google.common.graph.Network
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import java.awt.Dimension
import java.awt.Image
import java.awt.RenderingHints
import java.awt.geom.Point2D
import java.awt.image.BufferedImage

/**
 * A class that could be used on the server side of a thin-client application. It creates the jung
 * visualization, then produces an image of it.
 *
 * @author tom
 * @param N the node type
 * @param E the edge type
 */
@Suppress("serial")
open class VisualizationImageServer<N : Any, E : Any>(
    network: Network<N, E>,
    layoutAlgorithm: LayoutAlgorithm<N>,
    preferredSize: Dimension
) : BasicVisualizationServer<N, E>(network, layoutAlgorithm, preferredSize) {

    private val imageRenderingHints: MutableMap<RenderingHints.Key, Any> = HashMap()

    init {
        setSize(preferredSize)
        imageRenderingHints[RenderingHints.KEY_ANTIALIASING] = RenderingHints.VALUE_ANTIALIAS_ON
        addNotify()
    }

    fun getImage(center: Point2D, d: Dimension): Image {
        val width = getWidth()
        val height = getHeight()

        val scalex = width.toFloat() / d.width
        val scaley = height.toFloat() / d.height
        try {
            getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(MultiLayerTransformer.Layer.VIEW)
                .scale(scalex.toDouble(), scaley.toDouble(), center)

            val bi = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            val graphics = bi.createGraphics()
            graphics.setRenderingHints(imageRenderingHints)
            paint(graphics)
            graphics.dispose()
            return bi
        } finally {
            getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(MultiLayerTransformer.Layer.VIEW)
                .setToIdentity()
        }
    }
}
