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
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer
import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.GraphMouseListener
import edu.uci.ics.jung.visualization.control.ScalingControl
import edu.uci.ics.jung.visualization.renderers.BasicNodeLabelRenderer
import edu.uci.ics.jung.visualization.renderers.GradientNodeRenderer
import edu.uci.ics.jung.visualization.renderers.Renderer
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Container
import java.awt.Dimension
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.event.MouseEvent
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

/**
 * Demonstrates the use of `GraphZoomScrollPane`. This class shows the
 * `VisualizationViewer` zooming and panning capabilities, using horizontal and vertical
 * scrollbars.
 *
 * This demo also shows ToolTips on graph nodes and edges, and a key listener to change graph
 * mouse modes.
 *
 * @author Tom Nelson
 */
class GraphZoomScrollPaneDemo {

    /** the graph */
    private val graph: Network<Int, Number>

    /** the visual component and renderer for the graph */
    private val vv: VisualizationViewer<Int, Number>

    /** create an instance of a simple graph with controls to demo the zoom features. */
    init {
        // create a simple graph for the demo
        graph = createGraph()

        var sandstoneIcon: ImageIcon? = null
        val imageLocation = "/images/Sandstone.jpg"
        try {
            sandstoneIcon = ImageIcon(javaClass.getResource(imageLocation))
        } catch (ex: Exception) {
            System.err.println("Can't load \"$imageLocation\"")
        }
        val icon = sandstoneIcon
        vv = VisualizationViewer(graph, KKLayoutAlgorithm(), Dimension(700, 700))

        if (icon != null) {
            vv.addPreRenderPaintable(object : VisualizationServer.Paintable {
                override fun paint(g: Graphics) {
                    val d = vv.size
                    g.drawImage(icon.image, 0, 0, d.width, d.height, vv)
                }

                override fun useTransform(): Boolean = false
            })
        }
        vv.addPostRenderPaintable(object : VisualizationServer.Paintable {
            var x = 0
            var y = 0
            var font: Font? = null
            var metrics: FontMetrics? = null
            var swidth = 0
            var sheight = 0
            val str = "GraphZoomScrollPane Demo"

            override fun paint(g: Graphics) {
                val d = vv.size
                if (font == null) {
                    font = Font(g.font.name, Font.BOLD, 30)
                    metrics = g.getFontMetrics(font)
                    swidth = metrics!!.stringWidth(str)
                    sheight = metrics!!.maxAscent + metrics!!.maxDescent
                    x = (d.width - swidth) / 2
                    y = (d.height - sheight * 1.5).toInt()
                }
                g.font = font
                val oldColor = g.color
                g.color = Color.lightGray
                g.drawString(str, x, y)
                g.color = oldColor
            }

            override fun useTransform(): Boolean = false
        })

        vv.addGraphMouseListener(TestGraphMouseListener())
        vv.getRenderer().setNodeRenderer(
            GradientNodeRenderer(vv, Color.white, Color.red, Color.white, Color.blue, false)
        )
        vv.getRenderContext().setEdgeDrawPaintFunction { Color.lightGray }
        vv.getRenderContext().setArrowFillPaintFunction { Color.lightGray }
        vv.getRenderContext().setArrowDrawPaintFunction { Color.lightGray }

        // add my listeners for ToolTips
        vv.setNodeToolTipFunction { it.toString() }
        vv.setEdgeToolTipFunction { edge -> "E${graph.incidentNodes(edge)}" }

        vv.getRenderContext().setNodeLabelFunction { it.toString() }
        vv.getRenderer()
            .getNodeLabelRenderer()
            .setPositioner(BasicNodeLabelRenderer.InsidePositioner())
        vv.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.AUTO)
        vv.foreground = Color.lightGray

        // create a frame to hold the graph
        val frame = JFrame()
        val content: Container = frame.contentPane
        val panel = GraphZoomScrollPane(vv)
        content.add(panel)
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        val graphMouse: AbstractModalGraphMouse = DefaultModalGraphMouse<Int, Number>()
        vv.setGraphMouse(graphMouse)

        vv.addKeyListener(graphMouse.modeKeyListener!!)
        vv.toolTipText = "<html><center>Type 'p' for Pick mode<p>Type 't' for Transform mode"

        val scaler: ScalingControl = CrossoverScalingControl()

        val plus = JButton("+")
        plus.addActionListener { scaler.scale(vv, 1.1f, vv.getCenter()) }

        val minus = JButton("-")
        minus.addActionListener { scaler.scale(vv, 1 / 1.1f, vv.getCenter()) }

        val reset = JButton("reset")
        reset.addActionListener {
            vv.getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(Layer.LAYOUT)
                .setToIdentity()
            vv.getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(Layer.VIEW)
                .setToIdentity()
        }

        val controls = JPanel()
        controls.add(plus)
        controls.add(minus)
        controls.add(reset)
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

    /** A nested class to demo the GraphMouseListener finding the right nodes after zoom/pan */
    private class TestGraphMouseListener<N> : GraphMouseListener<N> {
        override fun graphClicked(v: N, me: MouseEvent) {
            System.err.println("Node $v was clicked at (${me.x},${me.y})")
        }

        override fun graphPressed(v: N, me: MouseEvent) {
            System.err.println("Node $v was pressed at (${me.x},${me.y})")
        }

        override fun graphReleased(v: N, me: MouseEvent) {
            System.err.println("Node $v was released at (${me.x},${me.y})")
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            GraphZoomScrollPaneDemo()
        }
    }
}
