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
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer
import edu.uci.ics.jung.visualization.RenderContext
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.transform.BidirectionalTransformer
import edu.uci.ics.jung.visualization.transform.shape.ShapeTransformer
import edu.uci.ics.jung.visualization.transform.shape.TransformingGraphics
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Paint
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D

open class BasicNodeLabelRenderer<N : Any, E : Any> : Renderer.NodeLabel<N, E> {

    protected var _position: Renderer.NodeLabel.Position = Renderer.NodeLabel.Position.SE
    private var positioner: Renderer.NodeLabel.Positioner = OutsidePositioner()

    override fun getPosition(): Renderer.NodeLabel.Position = _position

    override fun setPosition(position: Renderer.NodeLabel.Position) {
        this._position = position
    }

    open fun prepareRenderer(
        renderContext: RenderContext<N, E>,
        layoutModel: LayoutModel<N>,
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
     * context is used.) If node label centering is active, the label is centered on the _position of
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
        val layoutModel = visualizationModel.getLayoutModel()
        val pt = layoutModel.apply(v)
        val pt2d = renderContext
            .getMultiLayerTransformer()
            .transform(Layer.LAYOUT, Point2D.Double(pt.x, pt.y))!!

        val x = pt2d.x.toFloat()
        val y = pt2d.y.toFloat()

        val component = prepareRenderer(
            renderContext,
            layoutModel,
            renderContext.getNodeLabelRenderer(),
            label,
            renderContext.getPickedNodeState().isPicked(v),
            v
        )
        val g = renderContext.getGraphicsContext()
        val d = component.preferredSize
        val xform = AffineTransform.getTranslateInstance(x.toDouble(), y.toDouble())

        var shape = renderContext.getNodeShapeFunction().apply(v)
        shape = xform.createTransformedShape(shape)
        val gc = renderContext.getGraphicsContext()
        if (gc is TransformingGraphics) {
            val transformer = gc.getTransformer()
            if (transformer is ShapeTransformer) {
                shape = transformer.transform(shape)
            }
        }
        val bounds = shape.bounds2D

        val p: Point = if (_position == Renderer.NodeLabel.Position.AUTO) {
            var vvd = renderContext.getScreenDevice().size
            if (vvd.width == 0 || vvd.height == 0) {
                vvd = renderContext.getScreenDevice().preferredSize
            }
            getAnchorPoint(bounds, d, positioner.getPosition(x, y, vvd))
        } else {
            getAnchorPoint(bounds, d, _position)
        }

        val fillPaint: Paint? = renderContext.getNodeLabelDrawPaintFunction().apply(v)
        if (fillPaint != null) {
            val oldPaint = component.foreground
            component.foreground = fillPaint as Color
            g!!.draw(
                component,
                renderContext.getRendererPane(),
                p.x.toInt(),
                p.y.toInt(),
                d.width,
                d.height,
                true
            )
            component.foreground = oldPaint
        } else {
            g!!.draw(
                component,
                renderContext.getRendererPane(),
                p.x.toInt(),
                p.y.toInt(),
                d.width,
                d.height,
                true
            )
        }
    }

    protected open fun getAnchorPoint(
        nodeBounds: Rectangle2D,
        labelSize: Dimension,
        _position: Renderer.NodeLabel.Position
    ): Point {
        val offset = 5
        return when (_position) {
            Renderer.NodeLabel.Position.N -> {
                val x = nodeBounds.centerX - labelSize.width / 2
                val y = nodeBounds.minY - offset - labelSize.height
                Point.of(x, y)
            }
            Renderer.NodeLabel.Position.NE -> {
                val x = nodeBounds.maxX + offset
                val y = nodeBounds.minY - offset - labelSize.height
                Point.of(x, y)
            }
            Renderer.NodeLabel.Position.E -> {
                val x = nodeBounds.maxX + offset
                val y = nodeBounds.centerY - labelSize.height / 2
                Point.of(x, y)
            }
            Renderer.NodeLabel.Position.SE -> {
                val x = nodeBounds.maxX + offset
                val y = nodeBounds.maxY + offset
                Point.of(x, y)
            }
            Renderer.NodeLabel.Position.S -> {
                val x = nodeBounds.centerX - labelSize.width / 2
                val y = nodeBounds.maxY + offset
                Point.of(x, y)
            }
            Renderer.NodeLabel.Position.SW -> {
                val x = nodeBounds.minX - offset - labelSize.width
                val y = nodeBounds.maxY + offset
                Point.of(x, y)
            }
            Renderer.NodeLabel.Position.W -> {
                val x = nodeBounds.minX - offset - labelSize.width
                val y = nodeBounds.centerY - labelSize.height / 2
                Point.of(x, y)
            }
            Renderer.NodeLabel.Position.NW -> {
                val x = nodeBounds.minX - offset - labelSize.width
                val y = nodeBounds.minY - offset - labelSize.height
                Point.of(x, y)
            }
            Renderer.NodeLabel.Position.CNTR -> {
                val x = nodeBounds.centerX - labelSize.width / 2
                val y = nodeBounds.centerY - labelSize.height / 2
                Point.of(x, y)
            }
            else -> Point.ORIGIN
        }
    }

    class InsidePositioner : Renderer.NodeLabel.Positioner {
        override fun getPosition(x: Float, y: Float, d: Dimension): Renderer.NodeLabel.Position {
            val cx = d.width / 2
            val cy = d.height / 2
            if (x > cx && y > cy) return Renderer.NodeLabel.Position.NW
            if (x > cx && y < cy) return Renderer.NodeLabel.Position.SW
            if (x < cx && y > cy) return Renderer.NodeLabel.Position.NE
            return Renderer.NodeLabel.Position.SE
        }
    }

    class OutsidePositioner : Renderer.NodeLabel.Positioner {
        override fun getPosition(x: Float, y: Float, d: Dimension): Renderer.NodeLabel.Position {
            val cx = d.width / 2
            val cy = d.height / 2
            if (x > cx && y > cy) return Renderer.NodeLabel.Position.SE
            if (x > cx && y < cy) return Renderer.NodeLabel.Position.NE
            if (x < cx && y > cy) return Renderer.NodeLabel.Position.SW
            return Renderer.NodeLabel.Position.NW
        }
    }

    override fun getPositioner(): Renderer.NodeLabel.Positioner = positioner

    override fun setPositioner(positioner: Renderer.NodeLabel.Positioner) {
        this.positioner = positioner
    }
}
