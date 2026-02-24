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
import edu.uci.ics.jung.graph.util.TestGraphs
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.samples.util.ControlHelpers
import edu.uci.ics.jung.visualization.BaseVisualizationModel
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.ModalGraphMouse
import edu.uci.ics.jung.visualization.decorators.EllipseNodeShapeFunction
import edu.uci.ics.jung.visualization.subLayout.GraphCollapser
import edu.uci.ics.jung.visualization.util.PredicatedParallelEdgeIndexFunction
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.Shape
import java.util.function.Function
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.WindowConstants
import org.slf4j.LoggerFactory

/**
 * A demo that shows how collections of nodes can be collapsed into a single node. In this demo, the
 * nodes that are collapsed are those mouse-picked by the user. Any criteria could be used to form
 * the node collections to be collapsed, perhaps some common characteristic of those node objects.
 *
 * Note that the collection types don't use generics in this demo, because the nodes are of two
 * types: String for plain nodes, and `Network<String,Number>` for the collapsed nodes.
 *
 * @author Tom Nelson
 */
@Suppress("UNCHECKED_CAST", "rawtypes")
class NodeCollapseDemo : JPanel() {

    val instructions =
        "<html>Use the mouse to select multiple nodes" +
            "<p>either by dragging a region, or by shift-clicking" +
            "<p>on multiple nodes." +
            "<p>After you select nodes, use the Collapse button" +
            "<p>to combine them into a single node." +
            "<p>Select a 'collapsed' node and use the Expand button" +
            "<p>to restore the collapsed nodes." +
            "<p>The Restore button will restore the original graph." +
            "<p>If you select 2 (and only 2) nodes, then press" +
            "<p>the Compress Edges button, parallel edges between" +
            "<p>those two nodes will no longer be expanded." +
            "<p>If you select 2 (and only 2) nodes, then press" +
            "<p>the Expand Edges button, parallel edges between" +
            "<p>those two nodes will be expanded." +
            "<p>You can drag the nodes with the mouse." +
            "<p>Use the 'Picking'/'Transforming' combo-box to switch" +
            "<p>between picking and transforming mode.</html>"

    /** the graph */
    val graph: Network<*, *>

    /** the visual component and renderer for the graph */
    val vv: VisualizationViewer<Any, Any>

    val layoutAlgorithm: LayoutAlgorithm<Any>

    val collapser: GraphCollapser

    init {
        layout = BorderLayout()

        // create a simple graph for the demo
        graph = TestGraphs.getOneComponentGraph()

        collapser = GraphCollapser(graph)

        layoutAlgorithm = FRLayoutAlgorithm()

        val preferredSize = Dimension(400, 400)

        val visualizationModel: VisualizationModel<Any, Any> =
            BaseVisualizationModel(graph as Network<Any, Any>, layoutAlgorithm, preferredSize)
        vv = VisualizationViewer(visualizationModel, preferredSize)

        vv.getRenderContext().setNodeShapeFunction(ClusterNodeShapeFunction())

        val exclusions = HashSet<Any>()
        val eif = PredicatedParallelEdgeIndexFunction<Any, Any>(exclusions::contains)

        vv.getRenderContext().setParallelEdgeIndexFunction(eif)

        vv.background = Color.white

        // add a listener for ToolTips
        vv.setNodeToolTipFunction { v ->
            if (v is Network<*, *>) {
                v.nodes().toString()
            } else {
                v.toString()
            }
        }

        /** the regular graph mouse for the normal view */
        val graphMouse = DefaultModalGraphMouse<Any, Any>()

        vv.setGraphMouse(graphMouse)

        val gzsp = GraphZoomScrollPane(vv)
        add(gzsp)

        val modeBox: JComboBox<*> = graphMouse.getModeComboBox()
        modeBox.addItemListener(graphMouse.getModeListener())
        graphMouse.setMode(ModalGraphMouse.Mode.PICKING)

        val collapse = JButton("Collapse")
        collapse.addActionListener {
            val picked = HashSet<Any>(vv.getPickedNodeState().getPicked())
            if (picked.size > 1) {
                @Suppress("UNCHECKED_CAST")
                val inGraph: Network<Any, Any> = vv.getModel().getNetwork() as Network<Any, Any>
                val layoutModel: LayoutModel<Any> = vv.getModel().getLayoutModel() as LayoutModel<Any>
                val clusterGraph = collapser.getClusterGraph(inGraph, picked)
                log.info("clusterGraph:$clusterGraph")
                val g = collapser.collapse(inGraph, clusterGraph)
                log.info("g:$g")

                var sumx = 0.0
                var sumy = 0.0
                for (v in picked) {
                    val p = layoutModel.apply(v) as Point
                    sumx += p.x
                    sumy += p.y
                }
                val cp = Point.of(sumx / picked.size, sumy / picked.size)
                layoutModel.lock(false)
                layoutModel.set(clusterGraph, cp)
                log.info("put the cluster at $cp")
                layoutModel.lock(clusterGraph, true)
                layoutModel.lock(true)
                @Suppress("UNCHECKED_CAST")
                vv.getModel().setNetwork(g as Network<Any, Any>)

                vv.getRenderContext().getParallelEdgeIndexFunction().reset()
                layoutModel.accept(vv.getModel().getLayoutAlgorithm())
                vv.getPickedNodeState().clear()
                vv.repaint()
            }
        }

        val expand = JButton("Expand")
        expand.addActionListener {
            val picked = HashSet<Any>(vv.getPickedNodeState().getPicked())
            for (v in picked) {
                if (v is Network<*, *>) {
                    val inGraph: Network<Any, Any> = vv.getModel().getNetwork() as Network<Any, Any>
                    val layoutModel: LayoutModel<Any> = vv.getModel().getLayoutModel() as LayoutModel<Any>
                    val g = collapser.expand(graph, inGraph, v as Network<*, *>)

                    layoutModel.lock(false)
                    @Suppress("UNCHECKED_CAST")
                    vv.getModel().setNetwork(g as Network<Any, Any>)

                    vv.getRenderContext().getParallelEdgeIndexFunction().reset()
                }
                vv.getPickedNodeState().clear()
                vv.repaint()
            }
        }

        val compressEdges = JButton("Compress Edges")
        compressEdges.addActionListener {
            val picked = vv.getPickedNodeState().getPicked()
            if (picked.size == 2) {
                val pickedIter = picked.iterator()
                val nodeU = pickedIter.next()
                val nodeV = pickedIter.next()
                val graph: Network<Any, Any> = vv.getModel().getNetwork() as Network<Any, Any>
                val edges = HashSet<Any>(graph.incidentEdges(nodeU))
                edges.retainAll(graph.incidentEdges(nodeV))
                exclusions.addAll(edges)
                vv.repaint()
            }
        }

        val expandEdges = JButton("Expand Edges")
        expandEdges.addActionListener {
            val picked = vv.getPickedNodeState().getPicked()
            if (picked.size == 2) {
                val pickedIter = picked.iterator()
                val nodeU = pickedIter.next()
                val nodeV = pickedIter.next()
                val graph: Network<Any, Any> = vv.getModel().getNetwork() as Network<Any, Any>
                val edges = HashSet<Any>(graph.incidentEdges(nodeU))
                edges.retainAll(graph.incidentEdges(nodeV))
                exclusions.removeAll(edges)
                vv.repaint()
            }
        }

        val reset = JButton("Reset")
        reset.addActionListener {
            vv.getModel().setNetwork(graph)
            exclusions.clear()
            vv.repaint()
        }

        val help = JButton("Help")
        help.addActionListener { e ->
            JOptionPane.showMessageDialog(
                e.source as JComponent, instructions, "Help", JOptionPane.PLAIN_MESSAGE
            )
        }

        val controls = JPanel()
        controls.add(ControlHelpers.getZoomControls(vv, "Zoom"))
        val collapseControls = JPanel(GridLayout(3, 1))
        collapseControls.border = BorderFactory.createTitledBorder("Picked")
        collapseControls.add(collapse)
        collapseControls.add(expand)
        collapseControls.add(compressEdges)
        collapseControls.add(expandEdges)
        collapseControls.add(reset)
        controls.add(collapseControls)
        controls.add(modeBox)
        controls.add(help)
        add(controls, BorderLayout.SOUTH)
    }

    /**
     * a demo class that will create a node shape that is either a polygon or star. The number of
     * sides corresponds to the number of nodes that were collapsed into the node represented by this
     * shape.
     *
     * @author Tom Nelson
     */
    inner class ClusterNodeShapeFunction<N> : EllipseNodeShapeFunction<N>() {

        init {
            setSizeTransformer(ClusterNodeSizeFunction(20))
        }

        override fun apply(v: N): Shape {
            if (v is Network<*, *>) {
                val size = v.nodes().size
                return if (size < 8) {
                    val sides = Math.max(size, 3)
                    factory.getRegularPolygon(v, sides)
                } else {
                    factory.getRegularStar(v, size)
                }
            }
            return super.apply(v)
        }
    }

    /**
     * A demo class that will make nodes larger if they represent a collapsed collection of original
     * nodes
     *
     * @author Tom Nelson
     */
    inner class ClusterNodeSizeFunction<N>(val size: Int) : Function<N, Int> {

        override fun apply(v: N): Int {
            return if (v is Network<*, *>) {
                30
            } else {
                size
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(NodeCollapseDemo::class.java)

        fun getSmallGraph(): Network<String, Number> {
            val g: MutableNetwork<Any, Any> = NetworkBuilder.undirected().allowsParallelEdges(true).build()

            for (nodeIt in 1..3) {
                for (current in nodeIt + 1..3) {
                    val i = "$nodeIt"
                    val next = "$current"
                    g.addEdge(i, next, Math.pow((nodeIt + 2).toDouble(), current.toDouble()))
                }
            }

            for (nodeIt in 11..4) {
                for (current in nodeIt + 1..4) {
                    if (Math.random() <= 0.6) {
                        val i = "$nodeIt"
                        val next = "$current"
                        g.addEdge(i, next, Math.pow((nodeIt + 2).toDouble(), current.toDouble()))
                    }
                }
            }

            @Suppress("UNCHECKED_CAST")
            return g as Network<String, Number>
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val f = JFrame()
            f.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            f.contentPane.add(NodeCollapseDemo())
            f.pack()
            f.isVisible = true
        }
    }
}
