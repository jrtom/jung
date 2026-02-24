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

import edu.uci.ics.jung.visualization.RenderContext
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.GeneralPath
import java.awt.geom.Line2D
import java.awt.geom.PathIterator
import java.awt.geom.Point2D

open class CenterEdgeArrowRenderingSupport<N : Any, E : Any> : EdgeArrowRenderingSupport<N, E> {

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
        // count the segments.
        var current = 0
        val countIterator = path.getPathIterator(null, 1.0)
        while (!countIterator.isDone) {
            current++
            countIterator.next()
        }
        val middleSegment = current / 2
        // find the middle segment
        current = 0
        val i = path.getPathIterator(null, 1.0)
        while (!i.isDone) {
            current++
            val ret = i.currentSegment(seg)
            if (ret == PathIterator.SEG_MOVETO) {
                p2 = Point2D.Float(seg[0], seg[1])
            } else if (ret == PathIterator.SEG_LINETO) {
                p1 = p2
                p2 = Point2D.Float(seg[0], seg[1])
            }
            if (current > middleSegment) { // done
                at = getArrowTransform(rc, Line2D.Float(p1, p2), nodeShape)!!
                break
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

    /**
     * Returns a transform to position the arrowhead on this edge shape at the point where it
     * intersects the passed node shape.
     *
     * @param rc the rendering context used for rendering the arrow
     * @param edgeShape the shape used to draw the edge
     * @param nodeShape the shape used to draw the node
     * @param passedGo (ignored in this implementation)
     */
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
        var at = AffineTransform()
        // count the segments.
        var current = 0
        val countIterator = path.getPathIterator(null, 1.0)
        while (!countIterator.isDone) {
            current++
            countIterator.next()
        }
        val middleSegment = current / 2
        // find the middle segment
        current = 0
        val i = path.getPathIterator(null, 1.0)
        while (!i.isDone) {
            current++
            val ret = i.currentSegment(seg)
            if (ret == PathIterator.SEG_MOVETO) {
                p2 = Point2D.Float(seg[0], seg[1])
            } else if (ret == PathIterator.SEG_LINETO) {
                p1 = p2
                p2 = Point2D.Float(seg[0], seg[1])
            }
            if (current > middleSegment) { // done
                at = getReverseArrowTransform(rc, Line2D.Float(p1, p2), nodeShape)!!
                break
            }
            i.next()
        }
        return at
    }

    override fun getArrowTransform(
        rc: RenderContext<N, E>,
        edgeShape: Line2D,
        nodeShape: Shape
    ): AffineTransform {
        // find the midpoint of the edgeShape line, and use it to make the transform
        val left = Line2D.Float()
        val right = Line2D.Float()
        subdivide(edgeShape, left, right)
        val midEdge = right
        val dx = (midEdge.x1 - midEdge.x2).toFloat()
        val dy = (midEdge.y1 - midEdge.y2).toFloat()
        val atheta = Math.atan2(dx.toDouble(), dy.toDouble()) + Math.PI / 2
        val at = AffineTransform.getTranslateInstance(midEdge.x1.toDouble(), midEdge.y1.toDouble())
        at.rotate(-atheta)
        return at
    }

    protected open fun getReverseArrowTransform(
        rc: RenderContext<N, E>,
        edgeShape: Line2D,
        nodeShape: Shape
    ): AffineTransform {
        // find the midpoint of the edgeShape line, and use it to make the transform
        val left = Line2D.Float()
        val right = Line2D.Float()
        subdivide(edgeShape, left, right)
        val midEdge = right
        val dx = (midEdge.x1 - midEdge.x2).toFloat()
        val dy = (midEdge.y1 - midEdge.y2).toFloat()
        // calculate the angle for the arrowhead
        val atheta = Math.atan2(dx.toDouble(), dy.toDouble()) - Math.PI / 2
        val at = AffineTransform.getTranslateInstance(midEdge.x1.toDouble(), midEdge.y1.toDouble())
        at.rotate(-atheta)
        return at
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
