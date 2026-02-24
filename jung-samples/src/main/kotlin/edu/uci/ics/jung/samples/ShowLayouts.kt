/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.samples

import com.google.common.graph.MutableNetwork
import com.google.common.graph.Network
import com.google.common.graph.NetworkBuilder
import edu.uci.ics.jung.algorithms.generators.random.BarabasiAlbertGenerator
import edu.uci.ics.jung.graph.util.TestGraphs
import edu.uci.ics.jung.layout.algorithms.CircleLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.FRBHVisitorLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.ISOMLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.KKLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.SpringBHVisitorLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.SpringLayoutAlgorithm
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction
import edu.uci.ics.jung.visualization.layout.LayoutAlgorithmTransition
import edu.uci.ics.jung.visualization.renderers.Renderer
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.function.Supplier
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.WindowConstants

/**
 * Demonstrates several of the graph layout algorithms. Allows the user to interactively select one
 * of several graphs, and one of several layouts, and visualizes the combination.
 *
 * @author Danyel Fisher
 * @author Joshua O'Madadhain
 * @author Tom Nelson - extensive modification
 */
class ShowLayouts : JPanel() {

    enum class Layouts(private val displayName: String) {
        KK("Kamada Kawai"),
        CIRCLE("Circle"),
        SELF_ORGANIZING_MAP("Self Organizing Map"),
        FR("Fruchterman Reingold (FR)"),
        FR_BH_VISITOR("FR with Barnes-Hut as Visitor"),
        SPRING("Spring"),
        SPRING_BH_VISITOR("Spring with Barnes-Hut as Visitor");

        override fun toString(): String = displayName
    }

    class GraphChooser(private val vv: VisualizationViewer<Int, Number>) : ActionListener {
        @Suppress("UNCHECKED_CAST")
        override fun actionPerformed(e: ActionEvent) {
            val cb = e.source as JComboBox<*>
            graph_index = cb.selectedIndex
            vv.getNodeSpatial().clear()
            vv.getEdgeSpatial().clear()
            vv.getModel().setNetwork(g_array[graph_index] as Network<Int, Number>)
        }
    }

    companion object {
        @JvmStatic
        var g_array: Array<Network<*, *>> = arrayOf()
        @JvmStatic
        var graph_index: Int = 0
        @JvmStatic
        val graph_names: Array<String> = arrayOf(
            "Two component graph",
            "Random mixed-mode graph",
            "Miscellaneous multicomponent graph",
            "One component graph",
            "Chain+isolate graph",
            "Trivial (disconnected) graph",
            "Little Graph"
        )

        @Suppress("UNCHECKED_CAST")
        private fun getGraphPanel(): JPanel {
            g_array = arrayOfNulls<Network<*, *>>(graph_names.size) as Array<Network<*, *>>

            val nodeFactory = object : Supplier<Int> {
                var count = 0
                override fun get(): Int = count++
            }
            val edgeFactory = object : Supplier<Number> {
                var count = 0
                override fun get(): Number = count++
            }

            g_array[0] = TestGraphs.createTestGraph(false)
            val generator = BarabasiAlbertGenerator(
                NetworkBuilder.directed().allowsParallelEdges(true), nodeFactory, edgeFactory, 4, 3
            )
            generator.evolveGraph(20)
            g_array[1] = generator.get()
            g_array[2] = TestGraphs.getDemoGraph()
            g_array[3] = TestGraphs.getOneComponentGraph()
            g_array[4] = TestGraphs.createChainPlusIsolates(18, 5)
            g_array[5] = TestGraphs.createChainPlusIsolates(0, 20)
            val network: MutableNetwork<Any, Any> = NetworkBuilder.directed().allowsParallelEdges(true).build()
            network.addEdge("A", "B", 1)
            network.addEdge("A", "C", 2)

            g_array[6] = network

            val g = g_array[3] // initial graph

            val vv = VisualizationViewer<Any, Any>(g as Network<Any, Any>, Dimension(600, 600))

            vv.getRenderContext().setNodeFillPaintFunction(
                PickableNodePaintFunction(vv.getPickedNodeState(), Color.red, Color.yellow)
            )

            vv.getRenderContext().setNodeLabelFunction { it.toString() }
            vv.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.CNTR)

            val graphMouse = DefaultModalGraphMouse<Int, Number>()
            vv.setGraphMouse(graphMouse)

            // this reinforces that the generics (or lack of) declarations are correct
            vv.setNodeToolTipFunction { node ->
                "$node. with neighbors:${vv.getModel().getNetwork().adjacentNodes(node)}"
            }

            val scaler = CrossoverScalingControl()

            val plus = JButton("+")
            plus.addActionListener { scaler.scale(vv, 1.1f, vv.getCenter()) }
            val minus = JButton("-")
            minus.addActionListener { scaler.scale(vv, 1 / 1.1f, vv.getCenter()) }

            val modeBox = graphMouse.getModeComboBox()
            modeBox.addItemListener(
                (vv.getGraphMouse() as DefaultModalGraphMouse<Int, Number>).getModeListener()
            )

            val jp = JPanel(BorderLayout())
            jp.background = Color.WHITE
            jp.layout = BorderLayout()
            jp.add(vv, BorderLayout.CENTER)
            val combos = Layouts.values()
            val animateLayoutTransition = JRadioButton("Animate Layout Transition")

            val jcb = JComboBox(combos)
            jcb.addActionListener {
                val layoutType = jcb.selectedItem as Layouts
                @Suppress("UNCHECKED_CAST")
                val layoutAlgorithm = createLayout(layoutType) as LayoutAlgorithm<Any>
                if (animateLayoutTransition.isSelected) {
                    LayoutAlgorithmTransition.animate(vv, layoutAlgorithm)
                } else {
                    LayoutAlgorithmTransition.apply(vv, layoutAlgorithm)
                }
            }

            jcb.selectedItem = Layouts.FR

            val control_panel = JPanel(GridLayout(2, 1))
            val topControls = JPanel()
            val bottomControls = JPanel()
            control_panel.add(topControls)
            control_panel.add(bottomControls)
            jp.add(control_panel, BorderLayout.NORTH)

            val graph_chooser = JComboBox(graph_names)
            // do this before adding the listener so there is no event fired
            graph_chooser.selectedIndex = 3

            graph_chooser.addActionListener(GraphChooser(vv as VisualizationViewer<Int, Number>))

            topControls.add(jcb)
            topControls.add(graph_chooser)
            bottomControls.add(animateLayoutTransition)
            bottomControls.add(plus)
            bottomControls.add(minus)
            bottomControls.add(modeBox)
            return jp
        }

        private fun createLayout(layoutType: Layouts): LayoutAlgorithm<*> {
            return when (layoutType) {
                Layouts.CIRCLE -> CircleLayoutAlgorithm<Any>()
                Layouts.FR -> FRLayoutAlgorithm<Any>()
                Layouts.KK -> KKLayoutAlgorithm<Any>()
                Layouts.SELF_ORGANIZING_MAP -> ISOMLayoutAlgorithm<Any>()
                Layouts.SPRING -> SpringLayoutAlgorithm<Any>()
                Layouts.FR_BH_VISITOR -> FRBHVisitorLayoutAlgorithm<Any>()
                Layouts.SPRING_BH_VISITOR -> SpringBHVisitorLayoutAlgorithm<Any>()
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val jp = getGraphPanel()

            val jf = JFrame()
            jf.contentPane.add(jp)
            jf.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            jf.pack()
            jf.isVisible = true
        }
    }
}
