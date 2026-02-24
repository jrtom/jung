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
import java.awt.Component
import java.awt.Dimension
import java.awt.Rectangle
import java.awt.Shape
import java.awt.geom.Point2D
import java.util.HashMap
import java.util.function.Function

/**
 * Renders Node Labels, but can also supply Shapes for nodes. This has the effect of making the node
 * label the actual node shape. The user will probably want to center the node label on the node
 * location.
 *
 * @author Tom Nelson
 * @param N the node type
 * @param E the edge type
 */
open class NodeLabelAsShapeRenderer<N : Any, E : Any>(
    visualizationModel: VisualizationModel<N, E>,
    rc: RenderContext<N, *>
) : Renderer.NodeLabel<N, E>, Function<N, Shape> {

    protected val shapes: MutableMap<N, Shape> = HashMap()
    protected val layoutModel: LayoutModel<N> = visualizationModel.getLayoutModel()
    protected val renderContext: RenderContext<N, *> = rc

    open fun prepareRenderer(
        rc: RenderContext<N, *>,
        graphLabelRenderer: NodeLabelRenderer,
        value: Any?,
        isSelected: Boolean,
        node: N
    ): Component {
        return renderContext
            .getNodeLabelRenderer()
            .getNodeLabelRendererComponent(
                renderContext.getScreenDevice(),
                value,
                renderContext.getNodeFontFunction().apply(node),
                isSelected,
                node
            )
    }

    /**
     * Labels the specified node with the specified label. Uses the font specified by this instance's
     * `NodeFontFunction`. (If the font is unspecified, the existing font for the graphics
     * context is used.) If node label centering is active, the label is centered on the position of
     * the node; otherwise the label is offset slightly.
     */
    override fun labelNode(
        renderContext: RenderContext<N, E>,
        visualizationModel: VisualizationModel<N, E>,
        v: N,
        label: String
    ) {
        if (!renderContext.getNodeIncludePredicate().test(v)) {
            return
        }
        val g = renderContext.getGraphicsContext()!!
        val component = prepareRenderer(
            renderContext,
            renderContext.getNodeLabelRenderer(),
            label,
            renderContext.getPickedNodeState().isPicked(v),
            v
        )
        val d = component.preferredSize

        val hOffset = -d.width / 2
        val vOffset = -d.height / 2

        val p = layoutModel.apply(v)
        val p2d = renderContext
            .getMultiLayerTransformer()
            .transform(Layer.LAYOUT, Point2D.Double(p.x, p.y))!!

        val x = p2d.x.toInt()
        val y = p2d.y.toInt()

        g.draw(
            component,
            renderContext.getRendererPane(),
            x + hOffset,
            y + vOffset,
            d.width,
            d.height,
            true
        )

        val size = component.preferredSize
        val bounds = Rectangle(
            -size.width / 2 - 2,
            -size.height / 2 - 2,
            size.width + 4,
            size.height
        )
        shapes[v] = bounds
    }

    override fun apply(v: N): Shape {
        val component = prepareRenderer(
            renderContext,
            renderContext.getNodeLabelRenderer(),
            renderContext.getNodeLabelFunction().apply(v),
            renderContext.getPickedNodeState().isPicked(v),
            v
        )
        val size = component.preferredSize
        return Rectangle(
            -size.width / 2 - 2,
            -size.height / 2 - 2,
            size.width + 4,
            size.height
        )
    }

    override fun getPosition(): Renderer.NodeLabel.Position = Renderer.NodeLabel.Position.CNTR

    override fun getPositioner(): Renderer.NodeLabel.Positioner = object : Renderer.NodeLabel.Positioner {
        override fun getPosition(x: Float, y: Float, d: Dimension): Renderer.NodeLabel.Position =
            Renderer.NodeLabel.Position.CNTR
    }

    override fun setPosition(position: Renderer.NodeLabel.Position) {
        // noop
    }

    override fun setPositioner(positioner: Renderer.NodeLabel.Positioner) {
        // noop
    }
}
