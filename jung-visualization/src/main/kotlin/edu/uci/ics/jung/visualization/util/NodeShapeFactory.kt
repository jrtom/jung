/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 20, 2004
 */
package edu.uci.ics.jung.visualization.util

import com.google.common.base.Preconditions
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Ellipse2D
import java.awt.geom.GeneralPath
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D
import java.util.function.Function
import org.slf4j.LoggerFactory

/**
 * A utility class for generating `Shape`s for drawing nodes. The available shapes
 * include rectangles, rounded rectangles, ellipses, regular polygons, and regular stars. The
 * dimensions of the requested shapes are defined by the specified node layoutSize function
 * (specified by a `Function<? super N, Integer>`) and node aspect ratio function (specified
 * by a `Function<? super N, Float>`) implementations: the width of the bounding box of the
 * shape is given by the node layoutSize, and the height is given by the layoutSize multiplied by
 * the node's aspect ratio.
 *
 * @author Joshua O'Madadhain
 */
open class NodeShapeFactory<N> @JvmOverloads constructor(
    protected val vsf: Function<in N, Int> = Function { 10 },
    protected val varf: Function<in N, Float> = Function { 1.0f }
) {

    /**
     * Returns a `Rectangle2D` whose width and height are defined by this instance's
     * layoutSize and aspect ratio functions for this node.
     *
     * @param v the node for which the shape will be drawn
     * @return a rectangle for this node
     */
    fun getRectangle(v: N): Rectangle2D {
        val width = vsf.apply(v).toFloat()
        val height = width * varf.apply(v)
        val h_offset = -(width / 2)
        val v_offset = -(height / 2)
        theRectangle.setFrame(
            h_offset.toDouble(), v_offset.toDouble(),
            width.toDouble(), height.toDouble()
        )
        return theRectangle
    }

    /**
     * Returns an `Ellipse2D` whose width and height are defined by this instance's
     * layoutSize and aspect ratio functions for this node.
     *
     * @param v the node for which the shape will be drawn
     * @return an ellipse for this node
     */
    fun getEllipse(v: N): Ellipse2D {
        theEllipse.setFrame(getRectangle(v))
        return theEllipse
    }

    /**
     * Returns a `RoundRectangle2D` whose width and height are defined by this instance's
     * layoutSize and aspect ratio functions for this node. The arc layoutSize is set to be half the
     * minimum of the height and width of the frame.
     *
     * @param v the node for which the shape will be drawn
     * @return a round rectangle for this node
     */
    fun getRoundRectangle(v: N): RoundRectangle2D {
        val frame = getRectangle(v)
        val arc_size = Math.min(frame.height, frame.width).toFloat() / 2
        theRoundRectangle.setRoundRect(
            frame.x, frame.y, frame.width, frame.height,
            arc_size.toDouble(), arc_size.toDouble()
        )
        return theRoundRectangle
    }

    /**
     * Returns a regular `num_sides`-sided `Polygon` whose bounding box's width
     * and height are defined by this instance's layoutSize and aspect ratio functions for this node.
     *
     * @param v the node for which the shape will be drawn
     * @param num_sides the number of sides of the polygon; must be >= 3.
     * @return a regular polygon for this node
     */
    fun getRegularPolygon(v: N, num_sides: Int): Shape {
        val thePolygon = GeneralPath()
        Preconditions.checkArgument(num_sides >= 3, "Number of sides must be >= 3")
        val frame = getRectangle(v)
        val width = frame.width.toFloat()
        val height = frame.height.toFloat()

        // generate coordinates
        var angle = 0.0
        thePolygon.reset()
        thePolygon.moveTo(0f, 0f)
        thePolygon.lineTo(width, 0f)
        val theta = (2 * Math.PI) / num_sides
        for (i in 2 until num_sides) {
            angle -= theta
            val delta_x = (width * Math.cos(angle)).toFloat()
            val delta_y = (width * Math.sin(angle)).toFloat()
            val prev = thePolygon.currentPoint
            thePolygon.lineTo(prev.x.toFloat() + delta_x, prev.y.toFloat() + delta_y)
        }
        thePolygon.closePath()

        // scale polygon to be right layoutSize, translate to center at (0,0)
        val r = thePolygon.bounds2D
        val scale_x = width / r.width
        val scale_y = height / r.height
        val translationX = (r.minX + r.width / 2).toFloat()
        val translationY = (r.minY + r.height / 2).toFloat()

        val at = AffineTransform.getScaleInstance(scale_x.toDouble(), scale_y.toDouble())
        at.translate(-translationX.toDouble(), -translationY.toDouble())

        return at.createTransformedShape(thePolygon)
    }

    /**
     * Returns a regular `Polygon` of `num_points` points whose bounding box's
     * width and height are defined by this instance's layoutSize and aspect ratio functions for this
     * node.
     *
     * @param v the node for which the shape will be drawn
     * @param num_points the number of points of the polygon; must be >= 5.
     * @return a star shape for this node
     */
    fun getRegularStar(v: N, num_points: Int): Shape {
        val thePolygon = GeneralPath()
        Preconditions.checkArgument(num_points >= 5, "Number of points must be >= 5")
        val frame = getRectangle(v)
        val width = frame.width.toFloat()
        val height = frame.height.toFloat()

        // generate coordinates
        val theta = (2 * Math.PI) / num_points
        var angle = -theta / 2
        thePolygon.reset()
        thePolygon.moveTo(0f, 0f)
        var delta_x = width * Math.cos(angle).toFloat()
        var delta_y = width * Math.sin(angle).toFloat()
        var prev = thePolygon.currentPoint
        thePolygon.lineTo(prev.x.toFloat() + delta_x, prev.y.toFloat() + delta_y)
        for (i in 1 until num_points) {
            angle += theta
            delta_x = width * Math.cos(angle).toFloat()
            delta_y = width * Math.sin(angle).toFloat()
            prev = thePolygon.currentPoint
            thePolygon.lineTo(prev.x.toFloat() + delta_x, prev.y.toFloat() + delta_y)
            angle -= theta * 2
            delta_x = width * Math.cos(angle).toFloat()
            delta_y = width * Math.sin(angle).toFloat()
            prev = thePolygon.currentPoint
            if (prev != null) {
                thePolygon.lineTo(prev.x.toFloat() + delta_x, prev.y.toFloat() + delta_y)
            } else {
                log.error("somehow, prev is null")
            }
        }
        thePolygon.closePath()

        // scale polygon to be right layoutSize, translate to center at (0,0)
        val r = thePolygon.bounds2D
        val scale_x = width / r.width
        val scale_y = height / r.height

        val translationX = (r.minX + r.width / 2).toFloat()
        val translationY = (r.minY + r.height / 2).toFloat()

        val at = AffineTransform.getScaleInstance(scale_x.toDouble(), scale_y.toDouble())
        at.translate(-translationX.toDouble(), -translationY.toDouble())

        return at.createTransformedShape(thePolygon)
    }

    companion object {
        private val log = LoggerFactory.getLogger(NodeShapeFactory::class.java)
        private val theRectangle = Rectangle2D.Float()
        private val theEllipse = Ellipse2D.Float()
        private val theRoundRectangle = RoundRectangle2D.Float()
    }
}
