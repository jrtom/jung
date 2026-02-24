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
import org.slf4j.LoggerFactory
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D

open class BasicNodeRenderer<N : Any, E : Any> : Renderer.Node<N, E> {

    companion object {
        private val log = LoggerFactory.getLogger(BasicNodeRenderer::class.java)
    }

    override fun paintNode(
        renderContext: RenderContext<N, E>,
        visualizationModel: VisualizationModel<N, E>,
        v: N
    ) {
        if (renderContext.getNodeIncludePredicate().test(v)) {
            paintIconForNode(renderContext, visualizationModel, v)
        }
    }

    /**
     * Returns the node shape in layout coordinates.
     *
     * @param v the node whose shape is to be returned
     * @param coords the x and y view coordinates
     * @return the node shape in view coordinates
     */
    protected open fun prepareFinalNodeShape(
        renderContext: RenderContext<N, E>,
        visualizationModel: VisualizationModel<N, E>,
        v: N,
        coords: IntArray
    ): Shape {
        // get the shape to be rendered
        var shape = renderContext.getNodeShapeFunction().apply(v)
        val p = visualizationModel.getLayoutModel().apply(v)
        // p is the node location in layout coordinates
        log.trace("prepared a shape for {} to go at {}", v, p)
        val p2d = renderContext
            .getMultiLayerTransformer()
            .transform(Layer.LAYOUT, Point2D.Double(p.x, p.y))!!
        // now p is in view coordinates, ready to be further transformed by any transform in the
        // graphics context
        val x = p2d.x.toFloat()
        val y = p2d.y.toFloat()
        coords[0] = x.toInt()
        coords[1] = y.toInt()
        // create a transform that translates to the location of
        // the node to be rendered
        val xform = AffineTransform.getTranslateInstance(x.toDouble(), y.toDouble())
        // transform the node shape with xtransform
        shape = xform.createTransformedShape(shape)
        return shape
    }

    /**
     * Paint `v`'s icon on `g` at `(x,y)`.
     *
     * @param v the node to be painted
     */
    protected open fun paintIconForNode(
        renderContext: RenderContext<N, E>,
        visualizationModel: VisualizationModel<N, E>,
        v: N
    ) {
        val g = renderContext.getGraphicsContext()!!
        val coords = IntArray(2)
        val shape = prepareFinalNodeShape(renderContext, visualizationModel, v, coords)

        if (renderContext.getNodeIconFunction() != null) {
            val icon = renderContext.getNodeIconFunction().apply(v)
            if (icon != null) {
                g.draw(icon, renderContext.getScreenDevice(), shape, coords[0], coords[1])
            } else {
                paintShapeForNode(renderContext, visualizationModel, v, shape)
            }
        } else {
            paintShapeForNode(renderContext, visualizationModel, v, shape)
        }
    }

    protected open fun paintShapeForNode(
        renderContext: RenderContext<N, E>,
        visualizationModel: VisualizationModel<N, E>,
        v: N,
        shape: Shape
    ) {
        val g = renderContext.getGraphicsContext()!!
        val oldPaint = g.getPaint()
        val fillPaint = renderContext.getNodeFillPaintFunction().apply(v)
        if (fillPaint != null) {
            g.setPaint(fillPaint)
            g.fill(shape)
            g.setPaint(oldPaint)
        }
        val drawPaint = renderContext.getNodeDrawPaintFunction().apply(v)
        if (drawPaint != null) {
            g.setPaint(drawPaint)
            val oldStroke = g.getStroke()
            val stroke = renderContext.getNodeStrokeFunction().apply(v)
            if (stroke != null) {
                g.setStroke(stroke)
            }
            g.draw(shape)
            g.setPaint(oldPaint)
            g.setStroke(oldStroke)
        }
    }
}
