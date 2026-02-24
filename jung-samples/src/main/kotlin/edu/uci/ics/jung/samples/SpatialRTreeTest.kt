/*
 * Copyright (c) 2008, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.samples

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.google.common.graph.MutableNetwork
import com.google.common.graph.NetworkBuilder
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.StaticLayoutAlgorithm
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.util.NetworkNodeAccessor
import edu.uci.ics.jung.layout.util.RadiusNetworkNodeAccessor
import edu.uci.ics.jung.visualization.BaseVisualizationModel
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse
import edu.uci.ics.jung.visualization.control.ModalGraphMouse
import edu.uci.ics.jung.visualization.renderers.Renderer
import edu.uci.ics.jung.visualization.spatial.Spatial
import edu.uci.ics.jung.visualization.spatial.SpatialQuadTree
import edu.uci.ics.jung.visualization.spatial.SpatialRTree
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ItemEvent
import java.util.function.Supplier
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.WindowConstants

/**
 * A test that puts a lot of nodes on the screen with a visible quadtree. When the button is pushed,
 * 1000 random points are generated in order to find the closest node for each point. The search is
 * done both with the SpatialQuadTree and with the RadiusNetworkElementAccessor. If they don't find
 * the same node, the testing halts after highlighting the problem nodes along with the search
 * point.
 *
 * A mouse click at a location will highlight the closest node to the pick point.
 *
 * A toggle button will turn on/off the display of the spatialquadtree features, including the
 * expansion of the search target (red circle) in order to find the closest node.
 *
 * @author Tom Nelson
 */
class SpatialRTreeTest : JPanel() {

    init {
        layout = BorderLayout()
        val g: MutableNetwork<String, Number> =
            NetworkBuilder.directed().allowsParallelEdges(true).build()
        val viewPreferredSize = Dimension(600, 600)
        val layoutPreferredSize = Dimension(600, 600)
        val layoutAlgorithm: LayoutAlgorithm<String> = StaticLayoutAlgorithm()

        val scaler = CrossoverScalingControl()
        val model = BaseVisualizationModel(g, layoutAlgorithm, null, layoutPreferredSize)
        val vv = VisualizationViewer(model, viewPreferredSize)

        vv.getRenderContext().setNodeLabelFunction { it.toString() }
        vv.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.CNTR)

        val nodeFactory: Supplier<String> = NodeFactory()
        val edgeFactory: Supplier<Number> = EdgeFactory()

        val graphMouse = EditingModalGraphMouse<String, Number>(
            vv.getRenderContext(), nodeFactory, edgeFactory
        )

        // the EditingGraphMouse will pass mouse event coordinates to the
        // nodeLocations function to set the locations of the nodes as
        // they are created
        vv.setGraphMouse(graphMouse)
        vv.addKeyListener(graphMouse.modeKeyListener!!)

        val showSpatialEffects = JRadioButton("Show Spatial Structure")
        showSpatialEffects.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                System.err.println("TURNED ON LOGGING")
                // turn on the logging
                // programmatically set the log level so that the spatial grid is drawn for this demo
                // and the SpatialGrid logging is output
                val ctx = LoggerFactory.getILoggerFactory() as LoggerContext
                ctx.getLogger("edu.uci.ics.jung.visualization.spatial").level = Level.DEBUG
                ctx.getLogger("edu.uci.ics.jung.visualization.spatial.rtree").level = Level.DEBUG
                ctx.getLogger("edu.uci.ics.jung.visualization.BasicVisualizationServer").level =
                    Level.TRACE
                ctx.getLogger("edu.uci.ics.jung.visualization.picking").level = Level.TRACE
                repaint()
            } else if (e.stateChange == ItemEvent.DESELECTED) {
                System.err.println("TURNED OFF LOGGING")
                // turn off the logging
                // programmatically set the log level so that the spatial grid is drawn for this demo
                // and the SpatialGrid logging is output
                val ctx = LoggerFactory.getILoggerFactory() as LoggerContext
                ctx.getLogger("edu.uci.ics.jung.visualization.spatial").level = Level.INFO
                ctx.getLogger("edu.uci.ics.jung.visualization.spatial.rtree").level = Level.INFO
                ctx.getLogger("edu.uci.ics.jung.visualization.BasicVisualizationServer").level =
                    Level.INFO
                ctx.getLogger("edu.uci.ics.jung.visualization.picking").level = Level.INFO
                repaint()
            }
        }

        val modeBox: JComboBox<ModalGraphMouse.Mode> = graphMouse.getModeComboBox()

        val recalculate = JButton("Recalculate")
        recalculate.addActionListener { vv.getNodeSpatial().recalculate() }
        vv.scaleToLayout(scaler)
        this.add(vv)
        val buttons = JPanel()
        val search = JButton("Test 1000 Searches")
        buttons.add(search)
        buttons.add(showSpatialEffects)
        buttons.add(recalculate)
        buttons.add(modeBox)

        val spatial: Spatial<String> = vv.getNodeSpatial()
        when (spatial) {
            is SpatialQuadTree<String> -> {
                search.addActionListener {
                    testClosestNodes(vv, g, model.getLayoutModel(), spatial)
                }
            }
            is SpatialRTree.Nodes<String> -> {
                search.addActionListener {
                    testClosestNodes(vv, g, model.getLayoutModel(), spatial)
                }
            }
        }

        this.add(buttons, BorderLayout.SOUTH)
    }

    fun testClosestNodes(
        vv: VisualizationViewer<String, Number>,
        graph: MutableNetwork<String, Number>,
        layoutModel: LayoutModel<String>,
        tree: SpatialQuadTree<String>
    ) {
        vv.getPickedNodeState().clear()
        val slowWay: NetworkNodeAccessor<String> = RadiusNetworkNodeAccessor(Double.MAX_VALUE)

        // look for nodes closest to 1000 random locations
        for (i in 0 until 1000) {
            val x = Math.random() * layoutModel.width
            val y = Math.random() * layoutModel.height
            // use the slowWay
            val winnerOne = slowWay.getNode(layoutModel, x, y) ?: continue
            // use the quadtree
            val winnerTwo = tree.getClosestElement(x, y) ?: continue

            log.trace("{} and {} should be the same...", winnerOne, winnerTwo)

            if (winnerOne != winnerTwo) {
                log.info(
                    "the radius distanceSq from winnerOne {} at {} to {},{} is {}",
                    winnerOne,
                    layoutModel.apply(winnerOne),
                    x,
                    y,
                    layoutModel.apply(winnerOne).distanceSquared(x, y)
                )
                log.info(
                    "the radius distanceSq from winnerTwo {} at {} to {},{} is {}",
                    winnerTwo,
                    layoutModel.apply(winnerTwo),
                    x,
                    y,
                    layoutModel.apply(winnerTwo).distanceSquared(x, y)
                )

                log.info(
                    "the cell for winnerOne {} is {}",
                    winnerOne,
                    tree.getContainingQuadTreeLeaf(winnerOne)
                )
                log.info(
                    "the cell for winnerTwo {} is {}",
                    winnerTwo,
                    tree.getContainingQuadTreeLeaf(winnerTwo)
                )
                log.info(
                    "the cell for the search point {},{} is {}",
                    x,
                    y,
                    tree.getContainingQuadTreeLeaf(x, y)
                )
                vv.getPickedNodeState().pick(winnerOne, true)
                vv.getPickedNodeState().pick(winnerTwo, true)
                graph.addNode("P")
                layoutModel.set("P", x, y)
                vv.getRenderContext().getPickedNodeState().pick("P", true)
                break
            }
        }
    }

    fun testClosestNodes(
        vv: VisualizationViewer<String, Number>,
        graph: MutableNetwork<String, Number>,
        layoutModel: LayoutModel<String>,
        tree: SpatialRTree.Nodes<String>
    ) {
        vv.getPickedNodeState().clear()
        val slowWay: NetworkNodeAccessor<String> = RadiusNetworkNodeAccessor(Double.MAX_VALUE)

        // look for nodes closest to 1000 random locations
        for (i in 0 until 1000) {
            val x = Math.random() * layoutModel.width
            val y = Math.random() * layoutModel.height
            // use the slowWay
            val winnerOne = slowWay.getNode(layoutModel, x, y) ?: continue
            // use the quadtree
            val winnerTwo = tree.getClosestElement(x, y) ?: continue

            log.trace("{} and {} should be the same...", winnerOne, winnerTwo)

            if (winnerOne != winnerTwo) {
                log.info(
                    "the radius distanceSq from winnerOne {} at {} to {},{} is {}",
                    winnerOne,
                    layoutModel.apply(winnerOne),
                    x,
                    y,
                    layoutModel.apply(winnerOne).distanceSquared(x, y)
                )
                log.info(
                    "the radius distanceSq from winnerTwo {} at {} to {},{} is {}",
                    winnerTwo,
                    layoutModel.apply(winnerTwo),
                    x,
                    y,
                    layoutModel.apply(winnerTwo).distanceSquared(x, y)
                )

                log.info(
                    "the cell for winnerOne {} is {}",
                    winnerOne,
                    tree.getContainingLeaf(winnerOne)
                )
                log.info(
                    "the cell for winnerTwo {} is {}",
                    winnerTwo,
                    tree.getContainingLeaf(winnerTwo)
                )
                log.info(
                    "the cell for the search point {},{} is {}",
                    x,
                    y,
                    tree.getContainingLeafs(x, y)
                )
                vv.getPickedNodeState().pick(winnerOne, true)
                vv.getPickedNodeState().pick(winnerTwo, true)
                graph.addNode("P")
                layoutModel.set("P", x, y)
                vv.getRenderContext().getPickedNodeState().pick("P", true)
                break
            }
        }
    }

    inner class NodeFactory : Supplier<String> {
        var i = 0
        override fun get(): String = "N${i++}"
    }

    inner class EdgeFactory : Supplier<Number> {
        var i = 0
        override fun get(): Number = i++
    }

    companion object {
        private val log = LoggerFactory.getLogger(SpatialRTreeTest::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            val jf = JFrame()
            jf.contentPane.add(SpatialRTreeTest())
            jf.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            jf.pack()
            jf.isVisible = true
        }
    }
}
