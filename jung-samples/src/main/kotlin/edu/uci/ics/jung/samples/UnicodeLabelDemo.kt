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
import edu.uci.ics.jung.visualization.decorators.EllipseNodeShapeFunction
import edu.uci.ics.jung.visualization.decorators.NodeIconShapeFunction
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer
import edu.uci.ics.jung.visualization.renderers.DefaultNodeLabelRenderer
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Container
import java.awt.Dimension
import java.awt.event.ItemEvent
import java.util.function.Function
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JCheckBox
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

/**
 * A demo that shows flag images as nodes, and uses unicode to render node labels.
 *
 * @author Tom Nelson
 */
class UnicodeLabelDemo {

    val graph: Network<Int, Number>

    val vv: VisualizationViewer<Int, Number>

    var showLabels: Boolean = false

    init {
        // create a simple graph for the demo
        graph = createGraph()
        val iconMap = HashMap<Int, Icon>()

        vv = VisualizationViewer(graph, FRLayoutAlgorithm(), Dimension(700, 700))
        vv.getRenderContext().setNodeLabelFunction(UnicodeNodeStringer())
        vv.getRenderContext().setNodeLabelRenderer(DefaultNodeLabelRenderer(Color.cyan))
        vv.getRenderContext().setEdgeLabelRenderer(DefaultEdgeLabelRenderer(Color.cyan))
        val nodeIconShapeFunction = NodeIconShapeFunction<Int>(EllipseNodeShapeFunction())
        val nodeIconFunction: Function<Int, Icon> = Function { iconMap[it]!! }
        vv.getRenderContext().setNodeShapeFunction(nodeIconShapeFunction)
        vv.getRenderContext().setNodeIconFunction(nodeIconFunction)
        loadImages(iconMap)
        nodeIconShapeFunction.iconMap = iconMap
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

        val lo = JCheckBox("Show Labels")
        lo.addItemListener { e ->
            showLabels = e.stateChange == ItemEvent.SELECTED
            vv.repaint()
        }
        lo.isSelected = true

        val controls = JPanel()
        controls.add(ControlHelpers.getZoomControls(vv, ""))
        controls.add(lo)
        controls.add(gm.getModeComboBox())
        content.add(controls, BorderLayout.SOUTH)

        frame.pack()
        frame.isVisible = true
    }

    inner class UnicodeNodeStringer : Function<Int, String> {

        val map = HashMap<Int, String>()
        val labels = arrayOf(
            "\u0057\u0065\u006C\u0063\u006F\u006D\u0065\u0020\u0074\u006F\u0020JUNG\u0021",
            "\u6B22\u8FCE\u4F7F\u7528\u0020\u0020JUNG\u0021",
            "\u0414\u043E\u0431\u0440\u043E\u0020\u043F\u043E\u0436\u0430\u043B\u043E\u0432\u0430\u0422\u044A\u0020\u0432\u0020JUNG\u0021",
            "\u0042\u0069\u0065\u006E\u0076\u0065\u006E\u0075\u0065\u0020\u0061\u0075\u0020JUNG\u0021",
            "\u0057\u0069\u006C\u006B\u006F\u006D\u006D\u0065\u006E\u0020\u007A\u0075\u0020JUNG\u0021",
            "JUNG\u3078\u3087\u3045\u3053\u305D\u0021",
            "\u0042\u0069\u0065\u006E\u0076\u0065\u006E\u0069\u0064\u0061\u0020\u0061\u0020JUNG\u0021"
        )

        init {
            for (node in graph.nodes().toSet()) {
                map[node] = labels[node % labels.size]
            }
        }

        fun getLabel(v: Int): String {
            return if (showLabels) map[v] ?: "" else ""
        }

        override fun apply(input: Int): String = getLabel(input)
    }

    fun createGraph(): Network<Int, Number> {
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

    protected fun loadImages(imageMap: MutableMap<Int, Icon>) {
        var icons: Array<ImageIcon>? = null
        try {
            icons = arrayOf(
                ImageIcon(javaClass.getResource("/images/united-states.gif")),
                ImageIcon(javaClass.getResource("/images/china.gif")),
                ImageIcon(javaClass.getResource("/images/russia.gif")),
                ImageIcon(javaClass.getResource("/images/france.gif")),
                ImageIcon(javaClass.getResource("/images/germany.gif")),
                ImageIcon(javaClass.getResource("/images/japan.gif")),
                ImageIcon(javaClass.getResource("/images/spain.gif"))
            )
        } catch (ex: Exception) {
            System.err.println("You need flags.jar in your classpath to see the flag icons.")
        }
        for (node in graph.nodes().toSet()) {
            val i = node
            imageMap[node] = icons!![i % icons.size]
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            UnicodeLabelDemo()
        }
    }
}
