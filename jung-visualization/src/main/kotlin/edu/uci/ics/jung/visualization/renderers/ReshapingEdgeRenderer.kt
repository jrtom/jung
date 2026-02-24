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

import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer
import edu.uci.ics.jung.visualization.RenderContext
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.transform.LensTransformer
import edu.uci.ics.jung.visualization.transform.shape.TransformingGraphics
import edu.uci.ics.jung.visualization.util.Context
import java.awt.geom.AffineTransform
import java.awt.geom.GeneralPath
import java.awt.geom.Point2D

/**
 * uses a flatness argument to break edges into smaller segments. This produces a more detailed
 * transformation of the edge shape
 *
 * @author Tom Nelson
 * @param N the node type
 * @param E the edge type
 */
open class ReshapingEdgeRenderer<N : Any, E : Any> : BasicEdgeRenderer<N, E>(), Renderer.Edge<N, E> {

    /**
     * Draws the edge `e`, whose endpoints are at `(x1,y1)` and `(x2,y2)`,
     * on the graphics context `g`. The `Shape` provided by the
     * `EdgeShapeFunction` instance is scaled in the x-direction so that its width is equal to
     * the distance between `(x1,y1)` and `(x2,y2)`.
     */
    override fun drawSimpleEdge(
        renderContext: RenderContext<N, E>,
        visualizationModel: VisualizationModel<N, E>,
        e: E
    ) {
        val g = renderContext.getGraphicsContext() as TransformingGraphics
        val graph = visualizationModel.getNetwork()
        val endpoints = graph.incidentNodes(e)
        val v1 = endpoints.nodeU()
        val v2 = endpoints.nodeV()
        val p1 = visualizationModel.getLayoutModel().apply(v1)
        val p2 = visualizationModel.getLayoutModel().apply(v2)
        val p12d = renderContext
            .getMultiLayerTransformer()
            .transform(Layer.LAYOUT, Point2D.Double(p1.x, p1.y))!!
        val p22d = renderContext
            .getMultiLayerTransformer()
            .transform(Layer.LAYOUT, Point2D.Double(p2.x, p2.y))!!
        val x1 = p12d.x.toFloat()
        val y1 = p12d.y.toFloat()
        val x2 = p22d.x.toFloat()
        val y2 = p22d.y.toFloat()

        var flatness = 0f
        val transformer = renderContext.getMultiLayerTransformer().getTransformer(Layer.VIEW)
        if (transformer is LensTransformer) {
            val lensShape = transformer.lens.lensShape
            if (lensShape.contains(x1.toDouble(), y1.toDouble()) ||
                lensShape.contains(x2.toDouble(), y2.toDouble())
            ) {
                flatness = .05f
            }
        }

        val isLoop = v1 == v2
        val s2 = renderContext.getNodeShapeFunction().apply(v2)
        var edgeShape = renderContext.getEdgeShapeFunction().apply(Context.getInstance(graph, e))

        val xform = AffineTransform.getTranslateInstance(x1.toDouble(), y1.toDouble())

        if (isLoop) {
            // this is a self-loop. scale it is larger than the node
            // it decorates and translate it so that its nadir is
            // at the center of the node.
            val s2Bounds = s2.getBounds2D()
            xform.scale(s2Bounds.width, s2Bounds.height)
            xform.translate(0.0, -edgeShape.getBounds2D().width / 2)
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

        edgeShape = xform.createTransformedShape(edgeShape)

        val oldPaint = g.getPaint()

        // get Paints for filling and drawing
        // (filling is done first so that drawing and label use same Paint)
        val fillPaint = renderContext.getEdgeFillPaintFunction().apply(e)
        if (fillPaint != null) {
            g.setPaint(fillPaint)
            g.fill(edgeShape, flatness)
        }
        val drawPaint = renderContext.getEdgeDrawPaintFunction().apply(e)
        if (drawPaint != null) {
            g.setPaint(drawPaint)
            g.draw(edgeShape, flatness)
        }

        val scalex = g.getTransform().scaleX.toFloat()
        val scaley = g.getTransform().scaleY.toFloat()
        // see if arrows are too small to bother drawing
        if (scalex < .3f || scaley < .3f) {
            return
        }

        if (renderContext.renderEdgeArrow()) {
            val destNodeShape = renderContext.getNodeShapeFunction().apply(v2)

            var xf = AffineTransform.getTranslateInstance(x2.toDouble(), y2.toDouble())
            val transformedDestNodeShape = xf.createTransformedShape(destNodeShape)

            val at = _edgeArrowRenderingSupport.getArrowTransform(
                renderContext, GeneralPath(edgeShape), transformedDestNodeShape
            ) ?: return

            var arrow = renderContext.getEdgeArrow()
            arrow = at.createTransformedShape(arrow)
            g.setPaint(renderContext.getArrowFillPaintFunction().apply(e))
            g.fill(arrow)
            g.setPaint(renderContext.getArrowDrawPaintFunction().apply(e))
            g.draw(arrow)

            if (!graph.isDirected) {
                val nodeShape = renderContext.getNodeShapeFunction().apply(v1)
                xf = AffineTransform.getTranslateInstance(x1.toDouble(), y1.toDouble())
                val transformedNodeShape = xf.createTransformedShape(nodeShape)

                val reverseAt = _edgeArrowRenderingSupport.getReverseArrowTransform(
                    renderContext, GeneralPath(edgeShape), transformedNodeShape, !isLoop
                ) ?: return

                arrow = renderContext.getEdgeArrow()
                arrow = reverseAt.createTransformedShape(arrow)
                g.setPaint(renderContext.getArrowFillPaintFunction().apply(e))
                g.fill(arrow)
                g.setPaint(renderContext.getArrowDrawPaintFunction().apply(e))
                g.draw(arrow)
            }
        }
        // use existing paint for text if no draw paint specified
        if (drawPaint == null) {
            g.setPaint(oldPaint)
        }

        // restore old paint
        g.setPaint(oldPaint)
    }
}
