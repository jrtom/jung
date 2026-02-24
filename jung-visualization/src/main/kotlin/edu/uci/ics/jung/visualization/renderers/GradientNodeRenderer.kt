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
import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.picking.PickedState
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.GradientPaint
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D

/**
 * A renderer that will fill node shapes with a GradientPaint
 *
 * @author Tom Nelson
 * @param N the node type
 * @param E the edge type
 */
open class GradientNodeRenderer<N : Any, E : Any> : Renderer.Node<N, E> {

    companion object {
        private val log = LoggerFactory.getLogger(GradientNodeRenderer::class.java)
    }

    private val colorOne: Color
    private val colorTwo: Color
    private val pickedColorOne: Color?
    private val pickedColorTwo: Color?
    private val pickedState: PickedState<N>?
    private val cyclic: Boolean

    constructor(vv: VisualizationServer<N, *>, colorOne: Color, colorTwo: Color, cyclic: Boolean) {
        this.colorOne = colorOne
        this.colorTwo = colorTwo
        this.pickedColorOne = null
        this.pickedColorTwo = null
        this.pickedState = null
        this.cyclic = cyclic
    }

    constructor(
        vv: VisualizationServer<N, *>,
        colorOne: Color,
        colorTwo: Color,
        pickedColorOne: Color,
        pickedColorTwo: Color,
        cyclic: Boolean
    ) {
        this.colorOne = colorOne
        this.colorTwo = colorTwo
        this.pickedColorOne = pickedColorOne
        this.pickedColorTwo = pickedColorTwo
        this.pickedState = vv.getPickedNodeState()
        this.cyclic = cyclic
    }

    override fun paintNode(
        renderContext: RenderContext<N, E>,
        visualizationModel: VisualizationModel<N, E>,
        v: N
    ) {
        if (renderContext.getNodeIncludePredicate().test(v)) {
            // get the shape to be rendered
            var shape = renderContext.getNodeShapeFunction().apply(v)
            val layoutModel = visualizationModel.getLayoutModel()
            val p = layoutModel.apply(v)
            val p2d = renderContext
                .getMultiLayerTransformer()
                .transform(Layer.LAYOUT, Point2D.Double(p.x, p.y))!!

            val x = p2d.x.toFloat()
            val y = p2d.y.toFloat()

            // create a transform that translates to the location of
            // the node to be rendered
            val xform = AffineTransform.getTranslateInstance(x.toDouble(), y.toDouble())
            // transform the node shape with xtransform
            shape = xform.createTransformedShape(shape)
            log.trace("prepared a shape for {} to go at {}", v, p)

            paintShapeForNode(renderContext, v, shape)
        }
    }

    protected open fun paintShapeForNode(renderContext: RenderContext<N, E>, v: N, shape: Shape) {
        val g = renderContext.getGraphicsContext()!!
        val oldPaint = g.getPaint()
        val r = shape.bounds
        var y2 = r.maxY.toFloat()
        if (cyclic) {
            y2 = (r.minY + r.height / 2).toFloat()
        }

        val fillPaint = if (pickedState != null && pickedState.isPicked(v)) {
            GradientPaint(
                r.minX.toFloat(), r.minY.toFloat(), pickedColorOne!!,
                r.minX.toFloat(), y2, pickedColorTwo!!,
                cyclic
            )
        } else {
            GradientPaint(
                r.minX.toFloat(), r.minY.toFloat(), colorOne,
                r.minX.toFloat(), y2, colorTwo,
                cyclic
            )
        }

        g.setPaint(fillPaint)
        g.fill(shape)
        g.setPaint(oldPaint)

        val drawPaint = renderContext.getNodeDrawPaintFunction().apply(v)
        if (drawPaint != null) {
            g.setPaint(drawPaint)
        }
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
