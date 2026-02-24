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
import edu.uci.ics.jung.layout.algorithms.KKLayoutAlgorithm
import edu.uci.ics.jung.visualization.VisualizationImageServer
import edu.uci.ics.jung.visualization.renderers.BasicNodeLabelRenderer
import edu.uci.ics.jung.visualization.renderers.GradientNodeRenderer
import edu.uci.ics.jung.visualization.renderers.Renderer
import java.awt.Color
import java.awt.Container
import java.awt.Dimension
import java.awt.geom.Point2D
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.WindowConstants

/**
 * Demonstrates VisualizationImageServer.
 *
 * @author Tom Nelson
 */
class VisualizationImageServerDemo {

    /** the graph */
    val graph: Network<Int, Double>

    /** the visual component and renderer for the graph */
    val vv: VisualizationImageServer<Int, Double>

    init {
        // create a simple graph for the demo
        graph = createGraph()

        vv = VisualizationImageServer(graph, KKLayoutAlgorithm(), Dimension(600, 600))

        vv.getRenderer().setNodeRenderer(
            GradientNodeRenderer<Int, Double>(vv, Color.white, Color.red, Color.white, Color.blue, false))
        vv.getRenderContext().setEdgeDrawPaintFunction { Color.lightGray }
        vv.getRenderContext().setArrowFillPaintFunction { Color.lightGray }
        vv.getRenderContext().setArrowDrawPaintFunction { Color.lightGray }

        vv.getRenderContext().setNodeLabelFunction { it.toString() }
        vv.getRenderer()
            .getNodeLabelRenderer()
            .setPositioner(BasicNodeLabelRenderer.InsidePositioner())
        vv.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.AUTO)

        // create a frame to hold the graph
        val frame = JFrame()
        val content: Container = frame.contentPane

        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE

        val im = vv.getImage(Point2D.Double(300.0, 300.0), Dimension(600, 600))
        val icon = ImageIcon(im)
        val label = JLabel(icon)
        content.add(label)
        frame.pack()
        frame.isVisible = true
    }

    fun createGraph(): Network<Int, Double> {
        val graph: MutableNetwork<Int, Double> = NetworkBuilder.directed().build()
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
            VisualizationImageServerDemo()
        }
    }
}
