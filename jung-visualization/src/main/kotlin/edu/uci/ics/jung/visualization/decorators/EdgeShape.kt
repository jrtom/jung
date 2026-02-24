/*
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on March 10, 2005
 */
package edu.uci.ics.jung.visualization.decorators

import com.google.common.base.Preconditions.checkNotNull
import com.google.common.graph.Network
import edu.uci.ics.jung.visualization.util.ArrowFactory
import edu.uci.ics.jung.visualization.util.Context
import edu.uci.ics.jung.visualization.util.EdgeIndexFunction
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.CubicCurve2D
import java.awt.geom.Ellipse2D
import java.awt.geom.GeneralPath
import java.awt.geom.Line2D
import java.awt.geom.QuadCurve2D
import java.awt.geom.Rectangle2D
import java.awt.geom.RectangularShape
import java.util.function.Function

/**
 * An interface for decorators that return a `Shape` for a specified edge.
 *
 * All edge shapes must be defined so that their endpoints are at (0,0) and (1,0). They will be
 * scaled, rotated and translated into position by the PluggableRenderer.
 *
 * @author Joshua O'Madadhain
 * @author Tom Nelson
 * @param E the edge type
 */
open class EdgeShape<E> {

    companion object {
        private val LINE = Line2D.Float(0.0f, 0.0f, 1.0f, 0.0f)
        private val BENT_LINE = GeneralPath()
        private val QUAD_CURVE = QuadCurve2D.Float()
        private val CUBIC_CURVE = CubicCurve2D.Float()
        private val ELLIPSE = Ellipse2D.Float(-0.5f, -0.5f, 1f, 1f)
        private val BOX = Rectangle2D.Float()
        private val BOW_TIE = GeneralPath(GeneralPath.WIND_EVEN_ODD)

        private var triangle: GeneralPath? = null

        /**
         * A convenience instance for other edge shapes to use for self-loop edges where parallel
         * instances will not overlay each other.
         */
        @JvmStatic
        protected val loop: Loop<Any, Any> = Loop()

        private fun <E> isLoop(graph: Network<*, E>, edge: E): Boolean {
            val endpoints = graph.incidentNodes(edge)
            checkNotNull(endpoints)
            return endpoints.nodeU() == endpoints.nodeV()
        }

        @JvmStatic
        fun <N, E> line(): Line<N, E> = Line()

        @JvmStatic
        fun <E> quadCurve(): QuadCurve<Any, E> = QuadCurve()

        @JvmStatic
        fun <E> cubicCurve(): CubicCurve<Any, E> = CubicCurve()

        @JvmStatic
        fun <E> orthogonal(): Orthogonal<Any, E> = Orthogonal()

        @JvmStatic
        fun <E> wedge(width: Int): Wedge<Any, E> = Wedge(width)

        private fun <N, E> getIndex(
            context: Context<Network<N, E>, E>,
            edgeIndexFunction: EdgeIndexFunction<N, E>?
        ): Int = edgeIndexFunction?.getIndex(context) ?: 1

        private fun buildFrame(shape: RectangularShape, index: Int): Shape {
            var x = -0.5f
            var y = -0.5f
            var diam = 1.0f
            diam += diam * index / 2
            x += x * index / 2
            y += y * index / 2
            shape.setFrame(x.toDouble(), y.toDouble(), diam.toDouble(), diam.toDouble())
            return shape
        }
    }

    /** An edge shape that renders as a straight line between the node endpoints. */
    open class Line<N, E> : EdgeShape<E>(),
        Function<Context<Network<N, E>, E>, Shape> {

        override fun apply(context: Context<Network<N, E>, E>): Shape {
            val graph = context.graph
            val e = context.element
            return if (isLoop(graph, e)) ELLIPSE else LINE
        }
    }

    /** An edge shape that renders as a bent-line between the node endpoints. */
    open class BentLine<N, E> : ParallelEdgeShapeFunction<N, E>() {

        override fun setEdgeIndexFunction(edgeIndexFunction: EdgeIndexFunction<N, E>?) {
            this._edgeIndexFunction = edgeIndexFunction
            @Suppress("UNCHECKED_CAST")
            (loop as Loop<N, E>).setEdgeIndexFunction(edgeIndexFunction)
        }

        /**
         * Get the shape for this edge, returning either the shared instance or, in the case of
         * self-loop edges, the Loop shared instance.
         */
        override fun apply(context: Context<Network<N, E>, E>): Shape {
            val graph = context.graph
            val e = context.element
            if (isLoop(graph, e)) {
                @Suppress("UNCHECKED_CAST")
                return (loop as Loop<N, E>).apply(context)
            }

            val index = getIndex(context, _edgeIndexFunction)
            val controlY = control_offset_increment + control_offset_increment * index
            BENT_LINE.reset()
            BENT_LINE.moveTo(0.0f, 0.0f)
            BENT_LINE.lineTo(0.5f, controlY)
            BENT_LINE.lineTo(1.0f, 1.0f)
            return BENT_LINE
        }
    }

    /** An edge shape that renders as a QuadCurve between node endpoints. */
    open class QuadCurve<N, E> : ParallelEdgeShapeFunction<N, E>() {

        override fun setEdgeIndexFunction(edgeIndexFunction: EdgeIndexFunction<N, E>?) {
            this._edgeIndexFunction = edgeIndexFunction
            @Suppress("UNCHECKED_CAST")
            (loop as Loop<N, E>).setEdgeIndexFunction(edgeIndexFunction)
        }

        /**
         * Get the shape for this edge, returning either the shared instance or, in the case of
         * self-loop edges, the Loop shared instance.
         */
        override fun apply(context: Context<Network<N, E>, E>): Shape {
            val graph = context.graph
            val e = context.element
            if (isLoop(graph, e)) {
                @Suppress("UNCHECKED_CAST")
                return (loop as Loop<N, E>).apply(context)
            }

            val index = getIndex(context, _edgeIndexFunction)
            val controlY = control_offset_increment + control_offset_increment * index
            QUAD_CURVE.setCurve(0.0f, 0.0f, 0.5f, controlY, 1.0f, 0.0f)
            return QUAD_CURVE
        }
    }

    /**
     * An edge shape that renders as a CubicCurve between node endpoints. The two control points are
     * at (1/3*length, 2*controlY) and (2/3*length, controlY) giving a 'spiral' effect.
     */
    open class CubicCurve<N, E> : ParallelEdgeShapeFunction<N, E>() {

        override fun setEdgeIndexFunction(edgeIndexFunction: EdgeIndexFunction<N, E>?) {
            this._edgeIndexFunction = edgeIndexFunction
            @Suppress("UNCHECKED_CAST")
            (loop as Loop<N, E>).setEdgeIndexFunction(edgeIndexFunction)
        }

        /**
         * Get the shape for this edge, returning either the shared instance or, in the case of
         * self-loop edges, the Loop shared instance.
         */
        override fun apply(context: Context<Network<N, E>, E>): Shape {
            val graph = context.graph
            val e = context.element
            if (isLoop(graph, e)) {
                @Suppress("UNCHECKED_CAST")
                return (loop as Loop<N, E>).apply(context)
            }

            val index = getIndex(context, _edgeIndexFunction)
            val controlY = control_offset_increment + control_offset_increment * index
            CUBIC_CURVE.setCurve(0.0f, 0.0f, 0.33f, 2 * controlY, 0.66f, -controlY, 1.0f, 0.0f)
            return CUBIC_CURVE
        }
    }

    /**
     * An edge shape that renders as a loop with its nadir at the center of the node. Parallel
     * instances will overlap.
     *
     * @author Tom Nelson
     */
    open class SimpleLoop<E> : Function<E, Shape> {
        override fun apply(e: E): Shape = ELLIPSE
    }

    /**
     * An edge shape that renders as a loop with its nadir at the center of the node. Parallel
     * instances will not overlap.
     */
    open class Loop<N, E> : ParallelEdgeShapeFunction<N, E>() {
        override fun apply(context: Context<Network<N, E>, E>): Shape {
            val graph = context.graph
            val e = context.element
            return buildFrame(ELLIPSE, getIndex(context, _edgeIndexFunction))
        }
    }

    /**
     * An edge shape that renders as an isosceles triangle whose apex is at the destination node for
     * directed edges, and as a "bowtie" shape for undirected edges.
     *
     * @author Joshua O'Madadhain
     */
    open class Wedge<N, E>(width: Int) : ParallelEdgeShapeFunction<N, E>() {

        init {
            triangle = ArrowFactory.getWedgeArrow(width.toFloat(), 1f)
            triangle!!.transform(AffineTransform.getTranslateInstance(1.0, 0.0))
            BOW_TIE.moveTo(0f, width / 2f)
            BOW_TIE.lineTo(1f, -width / 2f)
            BOW_TIE.lineTo(1f, width / 2f)
            BOW_TIE.lineTo(0f, -width / 2f)
            BOW_TIE.closePath()
        }

        override fun apply(context: Context<Network<N, E>, E>): Shape {
            val graph = context.graph
            val e = context.element
            if (isLoop(graph, e)) {
                @Suppress("UNCHECKED_CAST")
                return (loop as Loop<N, E>).apply(context)
            }
            return if (graph.isDirected) triangle!! else BOW_TIE
        }
    }

    /**
     * An edge shape that renders as a diamond with its nadir at the center of the node. Parallel
     * instances will not overlap.
     */
    open class Box<N, E> : ParallelEdgeShapeFunction<N, E>() {
        override fun apply(context: Context<Network<N, E>, E>): Shape {
            val graph = context.graph
            val e = context.element
            return buildFrame(BOX, getIndex(context, _edgeIndexFunction))
        }
    }

    /** An edge shape that renders as a bent-line between the node endpoints. */
    open class Orthogonal<N, E> : ParallelEdgeShapeFunction<N, E>() {
        private val box: Box<N, E> = Box()

        override fun apply(context: Context<Network<N, E>, E>): Shape {
            val graph = context.graph
            val e = context.element
            return if (isLoop(graph, e)) box.apply(context) else LINE
        }
    }
}
