/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * Created on Feb 2, 2005
 *
 */
package edu.uci.ics.jung.visualization

import edu.uci.ics.jung.visualization.transform.BidirectionalTransformer
import edu.uci.ics.jung.visualization.transform.shape.Intersector
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Rectangle
import java.awt.event.AdjustmentEvent
import java.awt.event.AdjustmentListener
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.geom.AffineTransform
import java.awt.geom.Line2D
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollBar
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

/**
 * GraphZoomScrollPane is a Container for the Graph's VisualizationViewer and includes custom
 * horizontal and vertical scrollbars. GraphZoomScrollPane listens for changes in the scale and
 * translation of the VisualizationViewer, and will update the scrollbar positions and sizes
 * accordingly. Changes in the scrollbar positions will cause the corresponding change in the
 * translation component (offset) of the VisualizationViewer. The scrollbars are modified so that
 * they will allow panning of the graph when the scale has been changed (e.g. zoomed-in or
 * zoomed-out).
 *
 * The lower-right corner of this component is available to use as a small button or menu.
 *
 * samples.graph.GraphZoomScrollPaneDemo shows the use of this component.
 *
 * @author Tom Nelson
 */
@Suppress("serial")
open class GraphZoomScrollPane(protected val vv: VisualizationViewer<*, *>) : JPanel(BorderLayout()) {

    private val _horizontalScrollBar: JScrollBar
    private val _verticalScrollBar: JScrollBar
    private var _corner: JComponent = JPanel()
    protected var scrollBarsMayControlAdjusting: Boolean = true
    protected val south: JPanel

    /**
     * Create an instance of the GraphZoomScrollPane to contain the VisualizationViewer
     */
    init {
        addComponentListener(ResizeListener())
        val d = vv.getModel().getLayoutSize()
        _verticalScrollBar = JScrollBar(JScrollBar.VERTICAL, 0, d.height, 0, d.height)
        _horizontalScrollBar = JScrollBar(JScrollBar.HORIZONTAL, 0, d.width, 0, d.width)
        _verticalScrollBar.addAdjustmentListener(VerticalAdjustmentListenerImpl())
        _horizontalScrollBar.addAdjustmentListener(HorizontalAdjustmentListenerImpl())
        _verticalScrollBar.unitIncrement = 20
        _horizontalScrollBar.unitIncrement = 20
        // respond to changes in the VisualizationViewer's transform
        // and set the scroll bar parameters appropriately
        vv.addChangeListener(ChangeListener { evt ->
            val source = (evt as ChangeEvent).source as VisualizationViewer<*, *>
            setScrollBars(source)
        })
        add(vv as JComponent)
        add(_verticalScrollBar, BorderLayout.EAST)
        south = JPanel(BorderLayout())
        south.add(_horizontalScrollBar)
        setCorner(JPanel())
        add(south, BorderLayout.SOUTH)
    }

    /**
     * listener for adjustment of the horizontal scroll bar. Sets the translation of the
     * VisualizationViewer
     */
    private inner class HorizontalAdjustmentListenerImpl : AdjustmentListener {
        var previous: Int = 0

        override fun adjustmentValueChanged(e: AdjustmentEvent) {
            val hval = e.value
            var dh = (previous - hval).toFloat()
            previous = hval
            if (dh != 0f && scrollBarsMayControlAdjusting) {
                // get the uniform scale of all transforms
                val layoutScale = vv.getRenderContext()
                    .getMultiLayerTransformer()
                    .getTransformer(MultiLayerTransformer.Layer.LAYOUT)
                    .getScale().toFloat()
                dh *= layoutScale
                val at = AffineTransform.getTranslateInstance(dh.toDouble(), 0.0)
                vv.getRenderContext()
                    .getMultiLayerTransformer()
                    .getTransformer(MultiLayerTransformer.Layer.LAYOUT)
                    .preConcatenate(at)
            }
        }
    }

    /**
     * Listener for adjustment of the vertical scroll bar. Sets the translation of the
     * VisualizationViewer
     */
    private inner class VerticalAdjustmentListenerImpl : AdjustmentListener {
        var previous: Int = 0

        override fun adjustmentValueChanged(e: AdjustmentEvent) {
            val sb = e.source as JScrollBar
            val m = sb.model
            val vval = m.value
            var dv = (previous - vval).toFloat()
            previous = vval
            if (dv != 0f && scrollBarsMayControlAdjusting) {
                // get the uniform scale of all transforms
                val layoutScale = vv.getRenderContext()
                    .getMultiLayerTransformer()
                    .getTransformer(MultiLayerTransformer.Layer.LAYOUT)
                    .getScale().toFloat()
                dv *= layoutScale
                val at = AffineTransform.getTranslateInstance(0.0, dv.toDouble())
                vv.getRenderContext()
                    .getMultiLayerTransformer()
                    .getTransformer(MultiLayerTransformer.Layer.LAYOUT)
                    .preConcatenate(at)
            }
        }
    }

    /**
     * use the supplied vv characteristics to set the position and dimensions of the scroll bars.
     * Called in response to a ChangeEvent from the VisualizationViewer
     */
    private fun setScrollBars(vv: VisualizationViewer<*, *>) {
        val d = vv.getModel().getLayoutSize()
        val vvBounds = vv.bounds

        // a rectangle representing the layout
        val layoutRectangle = Rectangle(0, 0, d.width, d.height)

        val viewTransformer: BidirectionalTransformer =
            vv.getRenderContext().getMultiLayerTransformer().getTransformer(MultiLayerTransformer.Layer.VIEW)
        val layoutTransformer: BidirectionalTransformer =
            vv.getRenderContext().getMultiLayerTransformer().getTransformer(MultiLayerTransformer.Layer.LAYOUT)

        var h0: Point2D = Point2D.Double(vvBounds.minX, vvBounds.centerY)
        var h1: Point2D = Point2D.Double(vvBounds.maxX, vvBounds.centerY)
        var v0: Point2D = Point2D.Double(vvBounds.centerX, vvBounds.minY)
        var v1: Point2D = Point2D.Double(vvBounds.centerX, vvBounds.maxY)

        h0 = viewTransformer.inverseTransform(h0)
        h0 = layoutTransformer.inverseTransform(h0)
        h1 = viewTransformer.inverseTransform(h1)
        h1 = layoutTransformer.inverseTransform(h1)
        v0 = viewTransformer.inverseTransform(v0)
        v0 = layoutTransformer.inverseTransform(v0)
        v1 = viewTransformer.inverseTransform(v1)
        v1 = layoutTransformer.inverseTransform(v1)

        scrollBarsMayControlAdjusting = false
        setScrollBarValues(layoutRectangle, h0, h1, v0, v1)
        scrollBarsMayControlAdjusting = true
    }

    protected open fun setScrollBarValues(
        rectangle: Rectangle, h0: Point2D, h1: Point2D, v0: Point2D, v1: Point2D
    ) {
        val containsH0 = rectangle.contains(h0)
        val containsH1 = rectangle.contains(h1)
        val containsV0 = rectangle.contains(v0)
        val containsV1 = rectangle.contains(v1)

        // horizontal scrollbar:
        val intersector = Intersector(rectangle, Line2D.Double(h0, h1))

        var min = 0
        var ext: Int
        var valH = 0
        var max: Int

        var points = intersector.points
        var first: Point2D? = null
        var second: Point2D? = null

        var pointArray = points.toTypedArray()
        if (pointArray.size > 1) {
            first = pointArray[0]
            second = pointArray[1]
        } else if (pointArray.isNotEmpty()) {
            first = pointArray[0]
            second = pointArray[0]
        }

        if (first != null && second != null) {
            // correct direction of intersect points
            if ((h0.x - h1.x) * (first.x - second.x) < 0) {
                val temp = first
                first = second
                second = temp
            }

            if (containsH0 && containsH1) {
                max = first.distance(second).toInt()
                valH = first.distance(h0).toInt()
                ext = h0.distance(h1).toInt()
            } else if (containsH0) {
                max = first.distance(second).toInt()
                valH = first.distance(h0).toInt()
                ext = h0.distance(second).toInt()
            } else if (containsH1) {
                max = first.distance(second).toInt()
                valH = 0
                ext = first.distance(h1).toInt()
            } else {
                ext = rectangle.width
                max = ext
                valH = min
            }
            _horizontalScrollBar.setValues(valH, ext + 1, min, max)
        }

        // vertical scroll bar
        var valV = 0
        min = 0

        intersector.intersectLine(Line2D.Double(v0, v1))
        points = intersector.points

        pointArray = points.toTypedArray()
        if (pointArray.size > 1) {
            first = pointArray[0]
            second = pointArray[1]
        } else if (pointArray.isNotEmpty()) {
            first = pointArray[0]
            second = pointArray[0]
        }

        if (first != null && second != null) {
            // arrange for direction
            if ((v0.y - v1.y) * (first.y - second.y) < 0) {
                val temp = first
                first = second
                second = temp
            }

            if (containsV0 && containsV1) {
                max = first.distance(second).toInt()
                valV = first.distance(v0).toInt()
                ext = v0.distance(v1).toInt()
            } else if (containsV0) {
                max = first.distance(second).toInt()
                valV = first.distance(v0).toInt()
                ext = v0.distance(second).toInt()
            } else if (containsV1) {
                max = first.distance(second).toInt()
                valV = 0
                ext = first.distance(v1).toInt()
            } else {
                ext = rectangle.height
                max = ext
                valV = min
            }
            _verticalScrollBar.setValues(valV, ext + 1, min, max)
        }
    }

    /** Listener to adjust the scroll bar parameters when the window is resized */
    protected inner class ResizeListener : ComponentAdapter() {
        override fun componentResized(e: ComponentEvent) {
            setScrollBars(vv)
        }
    }

    /**
     * @return Returns the corner component.
     */
    fun getCorner(): JComponent = _corner

    /**
     * @param corner The cornerButton to set.
     */
    fun setCorner(corner: JComponent) {
        this._corner = corner
        corner.preferredSize = Dimension(
            _verticalScrollBar.preferredSize.width,
            _horizontalScrollBar.preferredSize.height
        )
        south.add(this._corner, BorderLayout.EAST)
    }

    fun getHorizontalScrollBar(): JScrollBar = _horizontalScrollBar

    fun getVerticalScrollBar(): JScrollBar = _verticalScrollBar
}
