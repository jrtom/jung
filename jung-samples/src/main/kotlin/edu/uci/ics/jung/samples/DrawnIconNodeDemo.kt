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
import edu.uci.ics.jung.samples.util.ControlHelpers
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer
import edu.uci.ics.jung.visualization.renderers.DefaultNodeLabelRenderer
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.Icon
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

/**
 * A demo that shows drawn Icons as nodes
 *
 * @author Tom Nelson
 */
class DrawnIconNodeDemo {

    /** the graph */
    private val graph: Network<Int, Number>

    /** the visual component and renderer for the graph */
    private val vv: VisualizationViewer<Int, Number>

    init {
        // create a simple graph for the demo
        graph = createGraph()

        vv = VisualizationViewer(graph, FRLayoutAlgorithm(), Dimension(700, 700))
        vv.getRenderContext().setNodeLabelFunction { v -> "Node $v" }

        vv.getRenderContext().setNodeLabelRenderer(DefaultNodeLabelRenderer(Color.cyan))
        vv.getRenderContext().setEdgeLabelRenderer(DefaultEdgeLabelRenderer(Color.cyan))

        vv.getRenderContext().setNodeIconFunction { v ->
            object : Icon {
                override fun getIconHeight(): Int = 20

                override fun getIconWidth(): Int = 20

                override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
                    if (vv.getPickedNodeState().isPicked(v)) {
                        g.color = Color.yellow
                    } else {
                        g.color = Color.red
                    }
                    g.fillOval(x, y, 20, 20)
                    if (vv.getPickedNodeState().isPicked(v)) {
                        g.color = Color.black
                    } else {
                        g.color = Color.white
                    }
                    g.drawString("$v", x + 6, y + 15)
                }
            }
        }

        vv.getRenderContext().setNodeFillPaintFunction(
            PickableNodePaintFunction(vv.getPickedNodeState(), Color.white, Color.yellow)
        )
        vv.getRenderContext().setEdgeDrawPaintFunction(
            PickableEdgePaintFunction(vv.getPickedEdgeState(), Color.black, Color.lightGray)
        )

        vv.background = Color.white

        // add my listener for ToolTips
        vv.setNodeToolTipFunction { it.toString() }

        // create a frame to hold the graph
        val frame = JFrame()
        val content: Container = frame.contentPane
        val panel = GraphZoomScrollPane(vv)
        content.add(panel)
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE

        val gm = DefaultModalGraphMouse<Int, Number>()
        vv.setGraphMouse(gm)

        val controls = JPanel()
        controls.add(ControlHelpers.getZoomControls(vv, ""))
        controls.add(gm.getModeComboBox())
        content.add(controls, BorderLayout.SOUTH)

        frame.pack()
        frame.isVisible = true
    }

    private fun createGraph(): Network<Int, Number> {
        val graph: MutableNetwork<Int, Number> = NetworkBuilder.directed().build()
        graph.addEdge(0, 1, Math.random())
        graph.addEdge(3, 0, Math.random())
        graph.addEdge(0, 4, Math.random())
        graph.addEdge(4, 5, Math.random())
        graph.addEdge(5, 3, Math.random())
        graph.addEdge(2, 1, Math.random())
        graph.addEdge(4, 1, Math.random())
        graph.addEdge(8, 2, Math.random())
        graph.addEdge(3, 8, Math.random())
        graph.addEdge(6, 7, Math.random())
        graph.addEdge(7, 5, Math.random())
        graph.addEdge(0, 9, Math.random())
        graph.addEdge(9, 8, Math.random())
        graph.addEdge(7, 6, Math.random())
        graph.addEdge(6, 5, Math.random())
        graph.addEdge(4, 2, Math.random())
        graph.addEdge(5, 4, Math.random())
        graph.addEdge(4, 10, Math.random())
        graph.addEdge(10, 4, Math.random())

        return graph
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            DrawnIconNodeDemo()
        }
    }
}
