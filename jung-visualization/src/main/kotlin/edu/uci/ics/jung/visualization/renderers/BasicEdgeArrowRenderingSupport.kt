/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 23, 2005
 */
package edu.uci.ics.jung.visualization.renderers

import com.google.common.base.Preconditions
import edu.uci.ics.jung.visualization.RenderContext
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.GeneralPath
import java.awt.geom.Line2D
import java.awt.geom.PathIterator
import java.awt.geom.Point2D

open class BasicEdgeArrowRenderingSupport<N : Any, E : Any> : EdgeArrowRenderingSupport<N, E> {

    override fun getArrowTransform(
        rc: RenderContext<N, E>,
        edgeShape: Shape,
        nodeShape: Shape
    ): AffineTransform {
        val path = GeneralPath(edgeShape)
        val seg = FloatArray(6)
        var p1: Point2D? = null
        var p2: Point2D? = null
        var at = AffineTransform()
        // when the PathIterator is done, switch to the line-subdivide
        // method to get the arrowhead closer.
        val i = path.getPathIterator(null, 1.0)
        while (!i.isDone) {
            val ret = i.currentSegment(seg)
            if (ret == PathIterator.SEG_MOVETO) {
                p2 = Point2D.Float(seg[0], seg[1])
            } else if (ret == PathIterator.SEG_LINETO) {
                p1 = p2
                p2 = Point2D.Float(seg[0], seg[1])
                if (nodeShape.contains(p2)) {
                    at = getArrowTransform(rc, Line2D.Float(p1, p2), nodeShape)!!
                    break
                }
            }
            i.next()
        }
        return at
    }

    override fun getReverseArrowTransform(
        rc: RenderContext<N, E>,
        edgeShape: Shape,
        nodeShape: Shape
    ): AffineTransform {
        return getReverseArrowTransform(rc, edgeShape, nodeShape, true)!!
    }

    override fun getReverseArrowTransform(
        rc: RenderContext<N, E>,
        edgeShape: Shape,
        nodeShape: Shape,
        passedGo: Boolean
    ): AffineTransform {
        val path = GeneralPath(edgeShape)
        val seg = FloatArray(6)
        var p1: Point2D? = null
        var p2: Point2D? = null
        var currentPassedGo = passedGo

        var at = AffineTransform()
        val i = path.getPathIterator(null, 1.0)
        while (!i.isDone) {
            val ret = i.currentSegment(seg)
            if (ret == PathIterator.SEG_MOVETO) {
                p2 = Point2D.Float(seg[0], seg[1])
            } else if (ret == PathIterator.SEG_LINETO) {
                p1 = p2
                p2 = Point2D.Float(seg[0], seg[1])
                if (!currentPassedGo && nodeShape.contains(p2)) {
                    currentPassedGo = true
                } else if (currentPassedGo && !nodeShape.contains(p2)) {
                    at = getReverseArrowTransform(rc, Line2D.Float(p1, p2), nodeShape)!!
                    break
                }
            }
            i.next()
        }
        return at
    }

    override fun getArrowTransform(
        rc: RenderContext<N, E>,
        edgeShape: Line2D,
        nodeShape: Shape
    ): AffineTransform? {
        var currentEdgeShape = edgeShape
        var dx = (currentEdgeShape.x1 - currentEdgeShape.x2).toFloat()
        var dy = (currentEdgeShape.y1 - currentEdgeShape.y2).toFloat()
        // iterate over the line until the edge shape will place the
        // arrowhead closer than 'arrowGap' to the node shape boundary
        while ((dx * dx + dy * dy) > rc.getArrowPlacementTolerance()) {
            try {
                currentEdgeShape = getLastOutsideSegment(currentEdgeShape, nodeShape)
            } catch (e: IllegalArgumentException) {
                System.err.println(e.toString())
                return null
            }
            dx = (currentEdgeShape.x1 - currentEdgeShape.x2).toFloat()
            dy = (currentEdgeShape.y1 - currentEdgeShape.y2).toFloat()
        }
        val atheta = Math.atan2(dx.toDouble(), dy.toDouble()) + Math.PI / 2
        val at = AffineTransform.getTranslateInstance(currentEdgeShape.x1, currentEdgeShape.y1)
        at.rotate(-atheta)
        return at
    }

    protected open fun getReverseArrowTransform(
        rc: RenderContext<N, E>,
        edgeShape: Line2D,
        nodeShape: Shape
    ): AffineTransform? {
        var currentEdgeShape = edgeShape
        var dx = (currentEdgeShape.x1 - currentEdgeShape.x2).toFloat()
        var dy = (currentEdgeShape.y1 - currentEdgeShape.y2).toFloat()
        // iterate over the line until the edge shape will place the
        // arrowhead closer than 'arrowGap' to the node shape boundary
        while ((dx * dx + dy * dy) > rc.getArrowPlacementTolerance()) {
            try {
                currentEdgeShape = getFirstOutsideSegment(currentEdgeShape, nodeShape)
            } catch (e: IllegalArgumentException) {
                System.err.println(e.toString())
                return null
            }
            dx = (currentEdgeShape.x1 - currentEdgeShape.x2).toFloat()
            dy = (currentEdgeShape.y1 - currentEdgeShape.y2).toFloat()
        }
        // calculate the angle for the arrowhead
        val atheta = Math.atan2(dx.toDouble(), dy.toDouble()) - Math.PI / 2
        val at = AffineTransform.getTranslateInstance(currentEdgeShape.x1, currentEdgeShape.y1)
        at.rotate(-atheta)
        return at
    }

    /**
     * Returns a line that intersects `shape`'s boundary.
     *
     * @param line line to subdivide
     * @param shape shape to compare with line
     * @return a line that intersects the shape boundary
     * @throws IllegalArgumentException if the passed line's point2 is not inside the shape
     */
    protected open fun getLastOutsideSegment(line: Line2D, shape: Shape): Line2D {
        Preconditions.checkArgument(
            shape.contains(line.p2),
            "line end point: " + line.p2 + " is not contained in shape: " + shape.bounds2D
        )
        val left = Line2D.Double()
        val right = Line2D.Double()
        var currentLine = line
        // subdivide the line until its left segment intersects
        // the shape boundary
        do {
            subdivide(currentLine, left, right)
            currentLine = right
        } while (!shape.contains(currentLine.p1))
        // now that right is completely inside shape,
        // return left, which must be partially outside
        return left
    }

    /**
     * Returns a line that intersects `shape`'s boundary.
     *
     * @param line line to subdivide
     * @param shape shape to compare with line
     * @return a line that intersects the shape boundary
     * @throws IllegalArgumentException if the passed line's point1 is not inside the shape
     */
    protected open fun getFirstOutsideSegment(line: Line2D, shape: Shape): Line2D {
        Preconditions.checkArgument(
            shape.contains(line.p1),
            "line start point: " + line.p1 + " is not contained in shape: " + shape.bounds2D
        )
        val left = Line2D.Float()
        val right = Line2D.Float()
        var currentLine = line
        // subdivide the line until its right side intersects the
        // shape boundary
        do {
            subdivide(currentLine, left, right)
            currentLine = left
        } while (!shape.contains(currentLine.p2))
        // now that left is completely inside shape,
        // return right, which must be partially outside
        return right
    }

    /**
     * divide a Line2D into 2 new Line2Ds that are returned in the passed left and right instances, if
     * non-null
     *
     * @param src the line to divide
     * @param left the left side, or null
     * @param right the right side, or null
     */
    protected open fun subdivide(src: Line2D, left: Line2D?, right: Line2D?) {
        val x1 = src.x1
        val y1 = src.y1
        val x2 = src.x2
        val y2 = src.y2

        val mx = x1 + (x2 - x1) / 2.0
        val my = y1 + (y2 - y1) / 2.0
        left?.setLine(x1, y1, mx, my)
        right?.setLine(mx, my, x2, y2)
    }
}
