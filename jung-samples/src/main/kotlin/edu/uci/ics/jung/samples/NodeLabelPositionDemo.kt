/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.samples

import com.google.common.graph.Network
import edu.uci.ics.jung.graph.util.TestGraphs
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm
import edu.uci.ics.jung.visualization.BaseVisualizationModel
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.ScalingControl
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction
import edu.uci.ics.jung.visualization.picking.PickedState
import edu.uci.ics.jung.visualization.renderers.Renderer
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JMenuBar
import javax.swing.JPanel
import javax.swing.WindowConstants

/**
 * Demonstrates node label positioning controlled by the user. In the AUTO setting, labels are
 * placed according to which quadrant the node is in
 *
 * @author Tom Nelson
 */
class NodeLabelPositionDemo : JPanel() {

    /** the graph */
    val graph: Network<String, Number>

    val graphLayoutAlgorithm: FRLayoutAlgorithm<String>

    /** the visual component and renderer for the graph */
    val vv: VisualizationViewer<String, Number>

    var scaler: ScalingControl? = null

    /** create an instance of a simple graph with controls to demo the zoom and hyperbolic features. */
    init {
        layout = BorderLayout()
        // create a simple graph for the demo
        graph = TestGraphs.getOneComponentGraph()

        graphLayoutAlgorithm = FRLayoutAlgorithm()
        graphLayoutAlgorithm.setMaxIterations(1000)

        val preferredSize = Dimension(600, 600)

        val visualizationModel: VisualizationModel<String, Number> =
            BaseVisualizationModel(graph, graphLayoutAlgorithm, preferredSize)
        vv = VisualizationViewer(visualizationModel, preferredSize)

        val ps: PickedState<String> = vv.getPickedNodeState()
        val pes: PickedState<Number> = vv.getPickedEdgeState()
        vv.getRenderContext()
            .setNodeFillPaintFunction(PickableNodePaintFunction(ps, Color.red, Color.yellow))
        vv.getRenderContext()
            .setEdgeDrawPaintFunction(PickableEdgePaintFunction(pes, Color.black, Color.cyan))
        vv.background = Color.white
        vv.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.W)

        vv.getRenderContext().setNodeLabelFunction { n -> n }

        // add a listener for ToolTips
        vv.setNodeToolTipFunction { n -> n }

        val gzsp = GraphZoomScrollPane(vv)
        add(gzsp)

        // the regular graph mouse for the normal view
        val graphMouse: AbstractModalGraphMouse = DefaultModalGraphMouse<String, Number>()

        vv.setGraphMouse(graphMouse)
        vv.addKeyListener(graphMouse.modeKeyListener!!)

        val scaler: ScalingControl = CrossoverScalingControl()

        val plus = JButton("+")
        plus.addActionListener { scaler.scale(vv, 1.1f, vv.getCenter()) }

        val minus = JButton("-")
        minus.addActionListener { scaler.scale(vv, 1 / 1.1f, vv.getCenter()) }

        val positionPanel = JPanel()
        positionPanel.border = BorderFactory.createTitledBorder("Label Position")
        val menubar = JMenuBar()
        menubar.add(graphMouse.getModeMenu())
        gzsp.setCorner(menubar)
        val cb = JComboBox<Renderer.NodeLabel.Position>()
        cb.addItem(Renderer.NodeLabel.Position.N)
        cb.addItem(Renderer.NodeLabel.Position.NE)
        cb.addItem(Renderer.NodeLabel.Position.E)
        cb.addItem(Renderer.NodeLabel.Position.SE)
        cb.addItem(Renderer.NodeLabel.Position.S)
        cb.addItem(Renderer.NodeLabel.Position.SW)
        cb.addItem(Renderer.NodeLabel.Position.W)
        cb.addItem(Renderer.NodeLabel.Position.NW)
        cb.addItem(Renderer.NodeLabel.Position.N)
        cb.addItem(Renderer.NodeLabel.Position.CNTR)
        cb.addItem(Renderer.NodeLabel.Position.AUTO)
        cb.addItemListener { e ->
            val position = e.item as Renderer.NodeLabel.Position
            vv.getRenderer().getNodeLabelRenderer().setPosition(position)
            vv.repaint()
        }

        cb.selectedItem = Renderer.NodeLabel.Position.SE
        positionPanel.add(cb)
        val controls = JPanel()
        val zoomControls = JPanel(GridLayout(2, 1))
        zoomControls.border = BorderFactory.createTitledBorder("Zoom")
        zoomControls.add(plus)
        zoomControls.add(minus)

        controls.add(zoomControls)
        controls.add(positionPanel)
        add(controls, BorderLayout.SOUTH)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val f = JFrame()
            f.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            f.contentPane.add(NodeLabelPositionDemo())
            f.pack()
            f.isVisible = true
        }
    }
}
