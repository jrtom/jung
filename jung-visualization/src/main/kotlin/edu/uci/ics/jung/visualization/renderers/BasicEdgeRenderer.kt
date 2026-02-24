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
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer
import edu.uci.ics.jung.visualization.decorators.EdgeShape
import edu.uci.ics.jung.visualization.decorators.ParallelEdgeShapeFunction
import edu.uci.ics.jung.visualization.util.Context
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.GeneralPath
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D

open class BasicEdgeRenderer<N : Any, E : Any> : Renderer.Edge<N, E> {

    protected var _edgeArrowRenderingSupport: EdgeArrowRenderingSupport<N, E> =
        BasicEdgeArrowRenderingSupport()

    override fun paintEdge(
        renderContext: RenderContext<N, E>,
        visualizationModel: VisualizationModel<N, E>,
        e: E
    ) {
        val g2d = renderContext.getGraphicsContext()!!
        if (!renderContext.getEdgeIncludePredicate().test(e)) {
            return
        }

        // don't draw edge if either incident node is not drawn
        val endpoints = visualizationModel.getNetwork().incidentNodes(e)
        val u = endpoints.nodeU()
        val v = endpoints.nodeV()
        val nodeIncludePredicate = renderContext.getNodeIncludePredicate()
        if (!nodeIncludePredicate.test(u) || !nodeIncludePredicate.test(v)) {
            return
        }

        val newStroke = renderContext.edgeStrokeFunction().apply(e)
        val oldStroke = g2d.getStroke()
        if (newStroke != null) {
            g2d.setStroke(newStroke)
        }

        drawSimpleEdge(renderContext, visualizationModel, e)

        // restore paint and stroke
        if (newStroke != null) {
            g2d.setStroke(oldStroke)
        }
    }

    protected open fun prepareFinalEdgeShape(
        renderContext: RenderContext<N, E>,
        visualizationModel: VisualizationModel<N, E>,
        e: E,
        coords: IntArray,
        loop: BooleanArray
    ): Shape {
        val endpoints = visualizationModel.getNetwork().incidentNodes(e)
        val v1 = endpoints.nodeU()
        val v2 = endpoints.nodeV()

        val p1 = visualizationModel.getLayoutModel().apply(v1)
        val p2 = visualizationModel.getLayoutModel().apply(v2)
        val p2d1 = renderContext
            .getMultiLayerTransformer()
            .transform(Layer.LAYOUT, Point2D.Double(p1.x, p1.y))!!
        val p2d2 = renderContext
            .getMultiLayerTransformer()
            .transform(Layer.LAYOUT, Point2D.Double(p2.x, p2.y))!!
        val x1 = p2d1.x.toFloat()
        val y1 = p2d1.y.toFloat()
        val x2 = p2d2.x.toFloat()
        val y2 = p2d2.y.toFloat()
        coords[0] = x1.toInt()
        coords[1] = y1.toInt()
        coords[2] = x2.toInt()
        coords[3] = y2.toInt()

        val isLoop = v1 == v2
        loop[0] = isLoop
        val s2 = renderContext.getNodeShapeFunction().apply(v2)
        var edgeShape = renderContext
            .getEdgeShapeFunction()
            .apply(Context.getInstance(visualizationModel.getNetwork(), e))

        val xform = AffineTransform.getTranslateInstance(x1.toDouble(), y1.toDouble())

        if (isLoop) {
            // this is a self-loop. scale it is larger than the node
            // it decorates and translate it so that its nadir is
            // at the center of the node.
            val s2Bounds = s2.getBounds2D()
            xform.scale(s2Bounds.width, s2Bounds.height)
            xform.translate(0.0, -edgeShape.getBounds2D().width / 2)
        } else if (renderContext.getEdgeShapeFunction() is EdgeShape.Orthogonal<*, *>) {
            val dx = x2 - x1
            val dy = y2 - y1
            var index = 0
            if (renderContext.getEdgeShapeFunction() is ParallelEdgeShapeFunction<*, *>) {
                @Suppress("UNCHECKED_CAST")
                val peif = (renderContext.getEdgeShapeFunction() as ParallelEdgeShapeFunction<N, E>)
                    .getEdgeIndexFunction()
                index = peif!!.getIndex(Context.getInstance(visualizationModel.getNetwork(), e))
                index *= 20
            }
            val gp = GeneralPath()
            gp.moveTo(0f, 0f) // the xform will do the translation to x1,y1
            if (x1 > x2) {
                if (y1 > y2) {
                    gp.lineTo(0f, index.toFloat())
                    gp.lineTo(dx - index, index.toFloat())
                    gp.lineTo(dx - index, dy)
                    gp.lineTo(dx, dy)
                } else {
                    gp.lineTo(0f, -index.toFloat())
                    gp.lineTo(dx - index, -index.toFloat())
                    gp.lineTo(dx - index, dy)
                    gp.lineTo(dx, dy)
                }
            } else {
                if (y1 > y2) {
                    gp.lineTo(0f, index.toFloat())
                    gp.lineTo(dx + index, index.toFloat())
                    gp.lineTo(dx + index, dy)
                    gp.lineTo(dx, dy)
                } else {
                    gp.lineTo(0f, -index.toFloat())
                    gp.lineTo(dx + index, -index.toFloat())
                    gp.lineTo(dx + index, dy)
                    gp.lineTo(dx, dy)
                }
            }
            edgeShape = gp
        } else {
            // this is a normal edge. Rotate it to the angle between
            // node endpoints, then scale it to the distance between
            // the nodes
            val dx = x2 - x1
            val dy = y2 - y1
            val thetaRadians = Math.atan2(dy.toDouble(), dx.toDouble()).toFloat()
            xform.rotate(thetaRadians.toDouble())
            val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
            xform.scale(dist.toDouble(), 1.0)
        }

        return xform.createTransformedShape(edgeShape)
    }

    /**
     * Draws the edge `e`, whose endpoints are at `(x1,y1)` and `(x2,y2)`,
     * on the graphics context `g`. The `Shape` provided by the
     * `EdgeShapeFunction` instance is scaled in the x-direction so that its width is equal to
     * the distance between `(x1,y1)` and `(x2,y2)`.
     *
     * @param e the edge to be drawn
     */
    protected open fun drawSimpleEdge(
        renderContext: RenderContext<N, E>,
        visualizationModel: VisualizationModel<N, E>,
        e: E
    ) {
        val coords = IntArray(4)
        val loop = BooleanArray(1)
        val edgeShape = prepareFinalEdgeShape(renderContext, visualizationModel, e, coords, loop)

        val x1 = coords[0]
        val y1 = coords[1]
        val x2 = coords[2]
        val y2 = coords[3]
        val isLoop = loop[0]

        val g = renderContext.getGraphicsContext()!!
        val network = visualizationModel.getNetwork()

        val oldPaint = g.getPaint()

        // get Paints for filling and drawing
        // (filling is done first so that drawing and label use same Paint)
        val fillPaint = renderContext.getEdgeFillPaintFunction().apply(e)
        if (fillPaint != null) {
            g.setPaint(fillPaint)
            g.fill(edgeShape)
        }
        val drawPaint = renderContext.getEdgeDrawPaintFunction().apply(e)
        if (drawPaint != null) {
            g.setPaint(drawPaint)
            g.draw(edgeShape)
        }

        val scalex = g.getTransform().scaleX.toFloat()
        val scaley = g.getTransform().scaleY.toFloat()
        // see if arrows are too small to bother drawing
        if (scalex < .3f || scaley < .3f) {
            return
        }

        if (renderContext.renderEdgeArrow()) {
            val newStroke = renderContext.getEdgeArrowStrokeFunction().apply(e)
            val oldStroke = g.getStroke()
            if (newStroke != null) {
                g.setStroke(newStroke)
            }

            val destNodeShape = renderContext.getNodeShapeFunction().apply(network.incidentNodes(e).nodeV())

            var xf = AffineTransform.getTranslateInstance(x2.toDouble(), y2.toDouble())
            val transformedDestNodeShape = xf.createTransformedShape(destNodeShape)

            val at = _edgeArrowRenderingSupport.getArrowTransform(renderContext, edgeShape, transformedDestNodeShape)
                ?: return
            var arrow = renderContext.getEdgeArrow()
            arrow = at.createTransformedShape(arrow)
            g.setPaint(renderContext.getArrowFillPaintFunction().apply(e))
            g.fill(arrow)
            g.setPaint(renderContext.getArrowDrawPaintFunction().apply(e))
            g.draw(arrow)

            if (!network.isDirected) {
                val nodeShape = renderContext.getNodeShapeFunction().apply(network.incidentNodes(e).nodeU())
                xf = AffineTransform.getTranslateInstance(x1.toDouble(), y1.toDouble())
                val transformedNodeShape = xf.createTransformedShape(nodeShape)
                val reverseAt = _edgeArrowRenderingSupport.getReverseArrowTransform(
                    renderContext, edgeShape, transformedNodeShape, !isLoop
                ) ?: return
                arrow = renderContext.getEdgeArrow()
                arrow = reverseAt.createTransformedShape(arrow)
                g.setPaint(renderContext.getArrowFillPaintFunction().apply(e))
                g.fill(arrow)
                g.setPaint(renderContext.getArrowDrawPaintFunction().apply(e))
                g.draw(arrow)
            }
            // restore paint and stroke
            if (newStroke != null) {
                g.setStroke(oldStroke)
            }
        }

        // restore old paint
        g.setPaint(oldPaint)
    }

    override fun getEdgeArrowRenderingSupport(): EdgeArrowRenderingSupport<N, E> {
        return _edgeArrowRenderingSupport
    }

    override fun setEdgeArrowRenderingSupport(
        _edgeArrowRenderingSupport: EdgeArrowRenderingSupport<N, E>
    ) {
        this._edgeArrowRenderingSupport = _edgeArrowRenderingSupport
    }
}
