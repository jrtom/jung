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
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.visualization.BaseVisualizationModel
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.ModalGraphMouse
import edu.uci.ics.jung.visualization.control.ScalingControl
import edu.uci.ics.jung.visualization.renderers.DefaultNodeLabelRenderer
import edu.uci.ics.jung.visualization.renderers.GradientNodeRenderer
import edu.uci.ics.jung.visualization.renderers.NodeLabelAsShapeRenderer
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import java.util.function.Function
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

/**
 * This demo shows how to use the node labels themselves as the node shapes. Additionally, it shows
 * html labels so they are multi-line, and gradient painting of the node labels.
 *
 * @author Tom Nelson
 */
class NodeLabelAsShapeDemo : JPanel() {

    val graph: Network<String, Number>

    val vv: VisualizationViewer<String, Number>

    val layoutAlgorithm: LayoutAlgorithm<String>

    /** create an instance of a simple graph with basic controls */
    init {
        layout = BorderLayout()
        // create a simple graph for the demo
        graph = TestGraphs.getOneComponentGraph()

        layoutAlgorithm = FRLayoutAlgorithm()

        val preferredSize = Dimension(400, 400)
        val visualizationModel: VisualizationModel<String, Number> =
            BaseVisualizationModel(graph, layoutAlgorithm, preferredSize)
        vv = VisualizationViewer(visualizationModel, preferredSize)

        // this class will provide both label drawing and node shapes
        val vlasr = NodeLabelAsShapeRenderer<String, Number>(visualizationModel, vv.getRenderContext())

        // customize the render context
        vv.getRenderContext()
            .setNodeLabelFunction(
                (Function<String, String> { it.toString() })
                    .andThen { input -> "<html><center>Node<p>$input" }
            )
        vv.getRenderContext().setNodeShapeFunction(vlasr)
        vv.getRenderContext().setNodeLabelRenderer(DefaultNodeLabelRenderer(Color.red))
        vv.getRenderContext().setEdgeDrawPaintFunction { Color.yellow }
        vv.getRenderContext().setEdgeStrokeFunction { BasicStroke(2.5f) }

        // customize the renderer
        vv.getRenderer().setNodeRenderer(GradientNodeRenderer(vv, Color.gray, Color.white, true))
        vv.getRenderer().setNodeLabelRenderer(vlasr)

        vv.background = Color.black

        // add a listener for ToolTips
        vv.setNodeToolTipFunction { n -> n }

        val graphMouse = DefaultModalGraphMouse<String, Number>()

        vv.setGraphMouse(graphMouse)
        vv.addKeyListener(graphMouse.modeKeyListener!!)

        val gzsp = GraphZoomScrollPane(vv)
        add(gzsp)

        val modeBox: JComboBox<*> = graphMouse.getModeComboBox()
        modeBox.addItemListener(graphMouse.getModeListener())
        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING)

        val scaler: ScalingControl = CrossoverScalingControl()

        val plus = JButton("+")
        plus.addActionListener { scaler.scale(vv, 1.1f, vv.getCenter()) }

        val minus = JButton("-")
        minus.addActionListener { scaler.scale(vv, 1 / 1.1f, vv.getCenter()) }

        val controls = JPanel()
        val zoomControls = JPanel(GridLayout(2, 1))
        zoomControls.border = BorderFactory.createTitledBorder("Zoom")
        zoomControls.add(plus)
        zoomControls.add(minus)
        controls.add(zoomControls)
        controls.add(modeBox)
        add(controls, BorderLayout.SOUTH)
    }

    companion object {
        private const val serialVersionUID = 1017336668368978842L

        @JvmStatic
        fun main(args: Array<String>) {
            val f = JFrame()
            f.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            f.contentPane.add(NodeLabelAsShapeDemo())
            f.pack()
            f.isVisible = true
        }
    }
}
