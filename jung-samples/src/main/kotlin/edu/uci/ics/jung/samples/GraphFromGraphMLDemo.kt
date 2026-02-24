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
import com.google.common.graph.NetworkBuilder
import edu.uci.ics.jung.io.GraphMLReader
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
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
import java.awt.event.MouseEvent
import java.io.IOException
import java.io.InputStreamReader
import java.util.function.Supplier
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants
import javax.xml.parsers.ParserConfigurationException
import org.xml.sax.SAXException

/**
 * Demonstrates loading (and visualizing) a graph from a GraphML file.
 *
 * @author Tom Nelson
 */
class GraphFromGraphMLDemo
/**
 * Creates an instance showing a simple graph with controls to demonstrate the zoom features.
 *
 * @param filename the file containing the graph data we're reading
 * @throws ParserConfigurationException if a SAX parser cannot be constructed
 * @throws SAXException if the SAX parser factory cannot be constructed
 * @throws IOException if the file cannot be read
 */
@Throws(ParserConfigurationException::class, SAXException::class, IOException::class)
constructor(filename: String) {

    /** the visual component and renderer for the graph */
    private val vv: VisualizationViewer<Number, Number>

    init {
        val nodeFactory = object : Supplier<Number> {
            var n = 0
            override fun get(): Number = n++
        }
        val edgeFactory = object : Supplier<Number> {
            var n = 0
            override fun get(): Number = n++
        }

        val gmlr = GraphMLReader<MutableNetwork<Number, Number>, Number, Number>(nodeFactory, edgeFactory)
        val graph: MutableNetwork<Number, Number> =
            NetworkBuilder.directed().allowsSelfLoops(true).build()
        gmlr.load(InputStreamReader(this.javaClass.getResourceAsStream(filename)), graph)

        // create a simple graph for the demo
        val layoutAlgorithm: LayoutAlgorithm<Number> = FRLayoutAlgorithm()
        vv = VisualizationViewer(graph, layoutAlgorithm, Dimension(800, 800))

        vv.addGraphMouseListener(TestGraphMouseListener())
        vv.getRenderer().setNodeRenderer(
            GradientNodeRenderer(vv, Color.white, Color.red, Color.white, Color.blue, false)
        )

        // add my listeners for ToolTips
        vv.setNodeToolTipFunction { it.toString() }
        vv.setEdgeToolTipFunction { edge -> "E${graph.incidentNodes(edge)}" }

        vv.getRenderContext().setNodeLabelFunction { it.toString() }
        vv.getRenderer()
            .getNodeLabelRenderer()
            .setPositioner(BasicNodeLabelRenderer.InsidePositioner())
        vv.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.AUTO)

        // create a frame to hold the graph
        val frame = JFrame()
        val content: Container = frame.contentPane
        val panel = GraphZoomScrollPane(vv)
        content.add(panel)
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        val graphMouse: AbstractModalGraphMouse = DefaultModalGraphMouse<Number, Number>()
        vv.setGraphMouse(graphMouse)
        vv.addKeyListener(graphMouse.modeKeyListener!!)

        val menubar = javax.swing.JMenuBar()
        menubar.add(graphMouse.getModeMenu())
        panel.setCorner(menubar)

        vv.addKeyListener(graphMouse.modeKeyListener!!)
        vv.toolTipText = "<html><center>Type 'p' for Pick mode<p>Type 't' for Transform mode"

        val scaler: ScalingControl = CrossoverScalingControl()

        val plus = JButton("+")
        plus.addActionListener { scaler.scale(vv, 1.1f, vv.getCenter()) }

        val minus = JButton("-")
        minus.addActionListener { scaler.scale(vv, 1 / 1.1f, vv.getCenter()) }

        val controls = JPanel()
        controls.add(plus)
        controls.add(minus)
        content.add(controls, BorderLayout.SOUTH)

        frame.pack()
        frame.isVisible = true
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
        /**
         * @param args if this contains at least one element, the first will be used as the file to read
         * @throws ParserConfigurationException if a SAX parser cannot be constructed
         * @throws SAXException if the SAX parser factory cannot be constructed
         * @throws IOException if the file cannot be read
         */
        @JvmStatic
        @Throws(ParserConfigurationException::class, SAXException::class, IOException::class)
        fun main(args: Array<String>) {
            var filePath = "/datasets/simple.graphml"
            if (args.isNotEmpty()) {
                filePath = args[0]
            }
            GraphFromGraphMLDemo(filePath)
        }
    }
}
