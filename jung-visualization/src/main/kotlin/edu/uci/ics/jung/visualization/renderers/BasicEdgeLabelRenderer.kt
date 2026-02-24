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

import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer
import edu.uci.ics.jung.visualization.RenderContext
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.util.Context
import java.awt.Component
import java.awt.geom.AffineTransform
import java.awt.geom.Ellipse2D
import java.awt.geom.Point2D

open class BasicEdgeLabelRenderer<N : Any, E : Any> : Renderer.EdgeLabel<N, E> {

    open fun prepareRenderer(
        renderContext: RenderContext<N, E>,
        layoutModel: LayoutModel<N>,
        graphLabelRenderer: EdgeLabelRenderer,
        value: Any?,
        isSelected: Boolean,
        edge: E
    ): Component {
        return renderContext
            .getEdgeLabelRenderer()
            .getEdgeLabelRendererComponent(
                renderContext.getScreenDevice(),
                value,
                renderContext.getEdgeFontFunction().apply(edge),
                isSelected,
                edge
            )
    }

    override fun labelEdge(
        renderContext: RenderContext<N, E>,
        visualizationModel: VisualizationModel<N, E>,
        e: E,
        label: String
    ) {
        if (label.isEmpty()) {
            return
        }

        // don't draw edge if either incident node is not drawn
        val endpoints = visualizationModel.getNetwork().incidentNodes(e)
        val v1 = endpoints.nodeU()
        val v2 = endpoints.nodeV()
        val nodeIncludePredicate = renderContext.getNodeIncludePredicate()
        if (!nodeIncludePredicate.test(v1) || !nodeIncludePredicate.test(v2)) {
            return
        }

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

        val g = renderContext.getGraphicsContext()
        val distX = x2 - x1
        val distY = y2 - y1
        val totalLength = Math.sqrt((distX * distX + distY * distY).toDouble())

        val closeness = renderContext.getEdgeLabelCloseness()

        val posX = (x1 + closeness * distX).toInt()
        val posY = (y1 + closeness * distY).toInt()

        val xDisplacement = (renderContext.getLabelOffset() * (distY / totalLength)).toInt()
        val yDisplacement = (renderContext.getLabelOffset() * (-distX / totalLength)).toInt()

        val component = prepareRenderer(
            renderContext,
            visualizationModel.getLayoutModel(),
            renderContext.getEdgeLabelRenderer(),
            label,
            renderContext.getPickedEdgeState().isPicked(e),
            e
        )

        val d = component.preferredSize

        val edgeShape = renderContext
            .getEdgeShapeFunction()
            .apply(Context.getInstance(visualizationModel.getNetwork(), e))

        var parallelOffset = 1.0

        parallelOffset += renderContext
            .getParallelEdgeIndexFunction()
            .getIndex(Context.getInstance(visualizationModel.getNetwork(), e)).toDouble()

        parallelOffset *= d.height.toDouble()
        if (edgeShape is Ellipse2D) {
            parallelOffset += edgeShape.bounds.getHeight()
            parallelOffset = -parallelOffset
        }

        val gd = g!!
        val old = gd.getTransform()
        val xform = AffineTransform(old)
        xform.translate((posX + xDisplacement).toDouble(), (posY + yDisplacement).toDouble())
        val dx = (x2 - x1).toDouble()
        val dy = (y2 - y1).toDouble()
        if (renderContext.getEdgeLabelRenderer().isRotateEdgeLabels()) {
            var theta = Math.atan2(dy, dx)
            if (dx < 0) {
                theta += Math.PI
            }
            xform.rotate(theta)
        }
        if (dx < 0) {
            parallelOffset = -parallelOffset
        }

        xform.translate((-d.width / 2).toDouble(), (-(d.height / 2 - parallelOffset)))
        gd.setTransform(xform)
        gd.draw(component, renderContext.getRendererPane(), 0, 0, d.width, d.height, true)

        gd.setTransform(old)
    }
}
