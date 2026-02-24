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
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode
import edu.uci.ics.jung.visualization.control.ScalingControl
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer
import edu.uci.ics.jung.visualization.renderers.DefaultNodeLabelRenderer
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Container
import java.awt.Dimension
import java.awt.GridLayout
import java.net.URL
import java.util.function.Function
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

/**
 * Demonstrates the use of images on graph edge labels.
 *
 * @author Tom Nelson
 */
class ImageEdgeLabelDemo : JPanel() {

    /** the graph */
    private val graph: Network<Number, Number>

    /** the visual component and renderer for the graph */
    private val vv: VisualizationViewer<Number, Number>

    init {
        layout = BorderLayout()
        // create a simple graph for the demo
        graph = createGraph(NODE_COUNT)

        val layoutAlgorithm = FRLayoutAlgorithm<Number>()
        layoutAlgorithm.setMaxIterations(100)
        vv = VisualizationViewer(graph, layoutAlgorithm, Dimension(400, 400))

        vv.getRenderContext().setEdgeDrawPaintFunction(
            PickableEdgePaintFunction(vv.getPickedEdgeState(), Color.black, Color.cyan)
        )

        vv.background = Color.white

        vv.getRenderContext().setNodeLabelRenderer(DefaultNodeLabelRenderer(Color.cyan))
        vv.getRenderContext().setEdgeLabelRenderer(DefaultEdgeLabelRenderer(Color.cyan))
        vv.getRenderContext().setEdgeLabelFunction(object : Function<Number, String> {
            val url: URL = javaClass.getResource("/images/lightning-s.gif")

            override fun apply(input: Number): String {
                return "<html><img src=$url height=10 width=21>"
            }
        })

        // add a listener for ToolTips
        vv.setNodeToolTipFunction { it.toString() }
        vv.setEdgeToolTipFunction { it.toString() }
        val panel = GraphZoomScrollPane(vv)
        add(panel)

        val graphMouse = DefaultModalGraphMouse<Number, Number>()
        vv.setGraphMouse(graphMouse)
        vv.addKeyListener(graphMouse.modeKeyListener!!)
        val scaler: ScalingControl = CrossoverScalingControl()

        val plus = JButton("+")
        plus.addActionListener { scaler.scale(vv, 1.1f, vv.getCenter()) }
        val minus = JButton("-")
        minus.addActionListener { scaler.scale(vv, 1 / 1.1f, vv.getCenter()) }

        val modeBox: JComboBox<Mode> = graphMouse.getModeComboBox()
        val modePanel = JPanel()
        modePanel.border = BorderFactory.createTitledBorder("Mouse Mode")
        modePanel.add(modeBox)

        val scaleGrid = JPanel(GridLayout(1, 0))
        scaleGrid.border = BorderFactory.createTitledBorder("Zoom")
        val controls = JPanel()
        scaleGrid.add(plus)
        scaleGrid.add(minus)
        controls.add(scaleGrid)
        controls.add(modePanel)
        add(controls, BorderLayout.SOUTH)
    }

    /**
     * create some nodes
     *
     * @param nodeCount how many to create
     * @return the Nodes in an array
     */
    private fun createGraph(nodeCount: Int): Network<Number, Number> {
        val graph: MutableNetwork<Number, Number> = NetworkBuilder.directed().build()
        for (i in 0 until nodeCount) {
            graph.addNode(i)
        }
        var j = 0
        graph.addEdge(0, 1, j++)
        graph.addEdge(3, 0, j++)
        graph.addEdge(0, 4, j++)
        graph.addEdge(4, 5, j++)
        graph.addEdge(5, 3, j++)
        graph.addEdge(2, 1, j++)
        graph.addEdge(4, 1, j++)
        graph.addEdge(8, 2, j++)
        graph.addEdge(3, 8, j++)
        graph.addEdge(6, 7, j++)
        graph.addEdge(7, 5, j++)
        graph.addEdge(0, 9, j++)
        graph.addEdge(9, 8, j++)
        graph.addEdge(7, 6, j++)
        graph.addEdge(6, 5, j++)
        graph.addEdge(4, 2, j++)
        graph.addEdge(5, 4, j++)
        graph.addEdge(4, 10, j++)
        graph.addEdge(10, 4, j++)

        return graph
    }

    companion object {
        private const val serialVersionUID = -4332663871914930864L
        private const val NODE_COUNT = 11

        @JvmStatic
        fun main(args: Array<String>) {
            val frame = JFrame()
            val content: Container = frame.contentPane
            frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE

            content.add(ImageEdgeLabelDemo())
            frame.pack()
            frame.isVisible = true
        }
    }
}
