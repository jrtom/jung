/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 1, 2005
 */
package edu.uci.ics.jung.visualization.decorators

import edu.uci.ics.jung.visualization.util.ImageShapeUtils
import java.awt.Image
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.util.function.Function
import javax.swing.Icon
import javax.swing.ImageIcon

/**
 * A default implementation that stores images in a Map keyed on the node. Also applies a shaping
 * function to images to extract the shape of the opaque part of a transparent image.
 *
 * @author Tom Nelson
 */
open class NodeIconShapeFunction<N>(
    /**
     * The delegate node-to-shape function to use if no image is present for the node.
     */
    var delegate: Function<N, Shape>
) : Function<N, Shape> {

    var shapeMap: MutableMap<Image, Shape> = HashMap()

    var iconMap: MutableMap<N, Icon>? = null

    /**
     * Get the shape from the image. If not available, get the shape from the delegate
     * NodeShapeFunction.
     */
    override fun apply(v: N): Shape {
        val icon = iconMap?.get(v)
        if (icon != null && icon is ImageIcon) {
            val image = icon.image
            var shape = shapeMap[image]
            if (shape == null) {
                shape = ImageShapeUtils.getShape(image, 30)
                if (shape.bounds.getWidth() > 0 && shape.bounds.getHeight() > 0) {
                    // don't cache a zero-sized shape, wait for the image
                    // to be ready
                    val width = image.getWidth(null)
                    val height = image.getHeight(null)
                    val transform = AffineTransform.getTranslateInstance(
                        (-width / 2).toDouble(),
                        (-height / 2).toDouble()
                    )
                    shape = transform.createTransformedShape(shape)
                    shapeMap[image] = shape
                }
            }
            return shape
        } else {
            return delegate.apply(v)
        }
    }
}
