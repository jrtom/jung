/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.samples

import com.google.common.graph.MutableNetwork
import com.google.common.graph.Network
import com.google.common.graph.NetworkBuilder
import edu.uci.ics.jung.layout.algorithms.CircleLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.ModalGraphMouse
import edu.uci.ics.jung.visualization.control.ScalingControl
import edu.uci.ics.jung.visualization.decorators.EdgeShape
import edu.uci.ics.jung.visualization.decorators.ParallelEdgeShapeFunction
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer
import edu.uci.ics.jung.visualization.renderers.NodeLabelRenderer
import edu.uci.ics.jung.visualization.util.Context
import javax.swing.AbstractButton
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Container
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.Shape
import java.awt.event.ItemEvent
import java.util.function.Function
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.ButtonGroup
import javax.swing.DefaultBoundedRangeModel
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JSlider
import javax.swing.WindowConstants

/**
 * Demonstrates jung support for drawing edge labels that can be positioned at any point along the
 * edge, and can be rotated to be parallel with the edge.
 *
 * @author Tom Nelson
 */
class EdgeLabelDemo : JPanel() {

    /** the graph */
    private val graph: Network<Int, Number>

    /** the visual component and renderer for the graph */
    private val vv: VisualizationViewer<Int, Number>

    private val nodeLabelRenderer: NodeLabelRenderer
    private val edgeLabelRenderer: EdgeLabelRenderer
    private val scaler: ScalingControl = CrossoverScalingControl()

    /** create an instance of a simple graph with controls to demo the label positioning features */
    init {
        layout = BorderLayout()
        // create a simple graph for the demo
        graph = buildGraph()

        val layoutAlgorithm: LayoutAlgorithm<Int> = CircleLayoutAlgorithm()
        vv = VisualizationViewer(graph, layoutAlgorithm, Dimension(600, 400))
        vv.background = Color.white

        nodeLabelRenderer = vv.getRenderContext().getNodeLabelRenderer()
        edgeLabelRenderer = vv.getRenderContext().getEdgeLabelRenderer()

        val stringer = Function<Number, String> { e -> "Edge:${graph.incidentNodes(e)}" }

        vv.getRenderContext().setEdgeLabelFunction(stringer)
        vv.getRenderContext().setEdgeDrawPaintFunction { v ->
            if (vv.getPickedEdgeState().isPicked(v)) Color.cyan else Color.black
        }
        vv.getRenderContext().setNodeFillPaintFunction { v ->
            if (vv.getPickedNodeState().isPicked(v)) Color.yellow else Color.red
        }

        // add my listener for ToolTips
        vv.setNodeToolTipFunction { o -> "$o ${vv.getModel().getLayoutModel().apply(o)}" }

        // create a frame to hold the graph
        val panel = GraphZoomScrollPane(vv)
        add(panel)

        val graphMouse = DefaultModalGraphMouse<Int, Number>()
        vv.setGraphMouse(graphMouse)

        val plus = JButton("+")
        plus.addActionListener { scaler.scale(vv, 1.1f, vv.getCenter()) }

        val minus = JButton("-")
        minus.addActionListener { scaler.scale(vv, 1 / 1.1f, vv.getCenter()) }

        val radio = ButtonGroup()
        val lineButton = JRadioButton("Line")
        lineButton.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line())
                vv.repaint()
            }
        }

        val quadButton = JRadioButton("QuadCurve")
        quadButton.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                vv.getRenderContext().setEdgeShapeFunction(EdgeShape.QuadCurve<Int, Number>())
                vv.repaint()
            }
        }

        val cubicButton = JRadioButton("CubicCurve")
        cubicButton.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                vv.getRenderContext().setEdgeShapeFunction(EdgeShape.CubicCurve<Int, Number>())
                vv.repaint()
            }
        }

        radio.add(lineButton)
        radio.add(quadButton)
        radio.add(cubicButton)

        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING)

        val rotate = JCheckBox("<html><center>EdgeType<p>Parallel</center></html>")
        rotate.addItemListener { e ->
            val b = e.source as AbstractButton
            edgeLabelRenderer.setRotateEdgeLabels(b.isSelected)
            vv.repaint()
        }

        rotate.isSelected = true
        val edgeClosenessUpdater = EdgeClosenessUpdater()
        val closenessSlider = object : JSlider(edgeClosenessUpdater.rangeModel) {
            override fun getPreferredSize(): Dimension {
                val d = super.getPreferredSize()
                d.width /= 2
                return d
            }
        }

        val edgeOffsetSlider = object : JSlider(0, 50) {
            override fun getPreferredSize(): Dimension {
                val d = super.getPreferredSize()
                d.width /= 2
                return d
            }
        }
        edgeOffsetSlider.addChangeListener { e ->
            val s = e.source as JSlider
            val edgeShapeFunction: Function<Context<Network<Int, Number>, Number>, Shape> =
                vv.getRenderContext().getEdgeShapeFunction()
            if (edgeShapeFunction is ParallelEdgeShapeFunction<*, *>) {
                (edgeShapeFunction as ParallelEdgeShapeFunction<*, *>).setControlOffsetIncrement(s.value.toFloat())
                vv.repaint()
            }
        }

        val controls = Box.createHorizontalBox()

        val zoomPanel = JPanel(GridLayout(0, 1))
        zoomPanel.border = BorderFactory.createTitledBorder("Scale")
        zoomPanel.add(plus)
        zoomPanel.add(minus)

        val edgePanel = JPanel(GridLayout(0, 1))
        edgePanel.border = BorderFactory.createTitledBorder("Edge Shape")
        edgePanel.add(lineButton)
        edgePanel.add(quadButton)
        edgePanel.add(cubicButton)

        val rotatePanel = JPanel()
        rotatePanel.border = BorderFactory.createTitledBorder("Alignment")
        rotatePanel.add(rotate)

        val labelPanel = JPanel(BorderLayout())
        val sliderPanel = JPanel(GridLayout(3, 1))
        val sliderLabelPanel = JPanel(GridLayout(3, 1))
        val offsetPanel = JPanel(BorderLayout())
        offsetPanel.border = BorderFactory.createTitledBorder("Offset")
        sliderPanel.add(closenessSlider)
        sliderPanel.add(edgeOffsetSlider)
        sliderLabelPanel.add(JLabel("Closeness", JLabel.RIGHT))
        sliderLabelPanel.add(JLabel("Edges", JLabel.RIGHT))
        offsetPanel.add(sliderLabelPanel, BorderLayout.WEST)
        offsetPanel.add(sliderPanel)
        labelPanel.add(offsetPanel)
        labelPanel.add(rotatePanel, BorderLayout.WEST)

        val modePanel = JPanel(GridLayout(2, 1))
        modePanel.border = BorderFactory.createTitledBorder("Mouse Mode")
        modePanel.add(graphMouse.getModeComboBox())

        controls.add(zoomPanel)
        controls.add(edgePanel)
        controls.add(labelPanel)
        controls.add(modePanel)
        add(controls, BorderLayout.SOUTH)
        quadButton.isSelected = true
    }

    /**
     * subclassed to hold two BoundedRangeModel instances that are used by JSliders to move the edge
     * label positions
     *
     * @author Tom Nelson
     */
    internal inner class EdgeClosenessUpdater {
        val rangeModel: javax.swing.BoundedRangeModel

        init {
            val initialValue = (vv.getRenderContext().getEdgeLabelCloseness() * 10).toInt() / 10
            rangeModel = DefaultBoundedRangeModel(initialValue, 0, 0, 10)

            rangeModel.addChangeListener {
                vv.getRenderContext().setEdgeLabelCloseness(rangeModel.value / 10f)
                vv.repaint()
            }
        }
    }

    private fun buildGraph(): Network<Int, Number> {
        val graph: MutableNetwork<Int, Number> =
            NetworkBuilder.directed().allowsParallelEdges(true).build()

        graph.addEdge(0, 1, Math.random())
        graph.addEdge(0, 1, Math.random())
        graph.addEdge(0, 1, Math.random())
        graph.addEdge(1, 0, Math.random())
        graph.addEdge(1, 0, Math.random())
        graph.addEdge(1, 2, Math.random())
        graph.addEdge(1, 2, Math.random())

        return graph
    }

    companion object {
        private const val serialVersionUID = -6077157664507049647L

        @JvmStatic
        fun main(args: Array<String>) {
            val frame = JFrame()
            frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            val content: Container = frame.contentPane
            content.add(EdgeLabelDemo())
            frame.pack()
            frame.isVisible = true
        }
    }
}
