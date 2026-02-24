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
import edu.uci.ics.jung.graph.util.TestGraphs
import edu.uci.ics.jung.layout.algorithms.StaticLayoutAlgorithm
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.util.RadiusNetworkNodeAccessor
import edu.uci.ics.jung.layout.util.RandomLocationTransformer
import edu.uci.ics.jung.visualization.BaseVisualizationModel
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import edu.uci.ics.jung.visualization.renderers.Renderer
import edu.uci.ics.jung.visualization.spatial.Spatial
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ItemEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.WindowConstants
import org.slf4j.LoggerFactory

/**
 * A test that puts a lot of nodes on the screen with a visible R-Tree. When the button is pushed,
 * 1000 random points are generated in order to find the closest node for each point. The search is
 * done both with the R-Tree and with the RadiusNetworkElementAccessor. If they don't find the same
 * node, the testing halts after highlighting the problem nodes along with the search point.
 *
 * A mouse click at a location will highlight the closest node to the pick point.
 *
 * A toggle button will turn on/off the display of the R-Tree features, including the expansion
 * of the search target (red circle) in order to find the closest node.
 *
 * @author Tom Nelson
 */
class SimpleGraphSpatialSearchTest : JPanel() {

    init {
        layout = BorderLayout()

        @Suppress("UNCHECKED_CAST")
        val g = TestGraphs.createChainPlusIsolates(0, 100) as MutableNetwork<String, Number>
        val viewPreferredSize = Dimension(600, 600)
        val layoutPreferredSize = Dimension(600, 600)
        val layoutAlgorithm = StaticLayoutAlgorithm<Any>()

        val scaler = CrossoverScalingControl()
        val model = BaseVisualizationModel<Any, Any>(
            g as MutableNetwork<Any, Any>,
            layoutAlgorithm,
            RandomLocationTransformer<Any>(600.0, 600.0, System.currentTimeMillis()),
            layoutPreferredSize
        )
        val vv = VisualizationViewer<Any, Any>(model, viewPreferredSize)

        vv.getRenderContext().setNodeLabelFunction { it.toString() }
        vv.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.CNTR)

        vv.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer()
                val layoutPoint = multiLayerTransformer.inverseTransform(e.x.toDouble(), e.y.toDouble())
                val node = vv.getNodeSpatial().getClosestElement(layoutPoint)
                if (node != null) {
                    vv.getPickedNodeState().clear()
                    vv.getPickedNodeState().pick(node, true)
                }
            }
        })

        val showSpatialEffects = JRadioButton("Show Spatial Structure")
        showSpatialEffects.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                System.err.println("TURNED ON LOGGING")
                val ctx = LoggerFactory.getILoggerFactory() as LoggerContext
                ctx.getLogger("edu.uci.ics.jung.visualization.spatial").level = Level.DEBUG
                ctx.getLogger("edu.uci.ics.jung.visualization.BasicVisualizationServer").level = Level.TRACE
                ctx.getLogger("edu.uci.ics.jung.visualization.picking").level = Level.TRACE
                repaint()
            } else if (e.stateChange == ItemEvent.DESELECTED) {
                System.err.println("TURNED OFF LOGGING")
                val ctx = LoggerFactory.getILoggerFactory() as LoggerContext
                ctx.getLogger("edu.uci.ics.jung.visualization.spatial").level = Level.INFO
                ctx.getLogger("edu.uci.ics.jung.visualization.BasicVisualizationServer").level = Level.INFO
                ctx.getLogger("edu.uci.ics.jung.visualization.picking").level = Level.INFO
                repaint()
            }
        }

        vv.scaleToLayout(scaler)
        this.add(vv)
        val buttons = JPanel()
        val search = JButton("Test 1000 Searches")
        buttons.add(search)
        buttons.add(showSpatialEffects)

        @Suppress("UNCHECKED_CAST")
        search.addActionListener {
            testClosestNodes(
                vv as VisualizationViewer<String, String>,
                g as MutableNetwork<String, Number>,
                model.getLayoutModel() as LayoutModel<String>,
                vv.getNodeSpatial() as Spatial<String>
            )
        }
        this.add(buttons, BorderLayout.SOUTH)
    }

    fun testClosestNodes(
        vv: VisualizationViewer<String, String>,
        graph: MutableNetwork<String, Number>,
        layoutModel: LayoutModel<String>,
        tree: Spatial<String>
    ) {
        vv.getPickedNodeState().clear()
        val slowWay = RadiusNetworkNodeAccessor<String>(java.lang.Double.MAX_VALUE)

        // look for nodes closest to 1000 random locations
        for (i in 0 until 1000) {
            val x = Math.random() * layoutModel.width
            val y = Math.random() * layoutModel.height
            // use the slowWay
            val winnerOne = slowWay.getNode(layoutModel, x, y)
            // use the quadtree
            val winnerTwo = tree.getClosestElement(x, y)

            log.trace("{} and {} should be the same...", winnerOne, winnerTwo)

            if (winnerOne != winnerTwo) {
                log.info(
                    "the radius distanceSq from winnerOne {} at {} to {},{} is {}",
                    winnerOne, layoutModel.apply(winnerOne!!), x, y,
                    layoutModel.apply(winnerOne).distanceSquared(x, y)
                )
                log.info(
                    "the radius distanceSq from winnerTwo {} at {} to {},{} is {}",
                    winnerTwo, layoutModel.apply(winnerTwo!!), x, y,
                    layoutModel.apply(winnerTwo).distanceSquared(x, y)
                )

                log.info("the cell for winnerOne {} is {}", winnerOne, tree.getContainingLeaf(winnerOne))
                log.info("the cell for winnerTwo {} is {}", winnerTwo, tree.getContainingLeaf(winnerTwo))
                log.info("the cell for the search point {},{} is {}", x, y, tree.getContainingLeafs(x, y))
                vv.getPickedNodeState().pick(winnerOne, true)
                vv.getPickedNodeState().pick(winnerTwo, true)
                graph.addNode("P")
                layoutModel.set("P", x, y)
                vv.getRenderContext().getPickedNodeState().pick("P", true)
                break
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SimpleGraphSpatialSearchTest::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            val jf = JFrame()
            jf.contentPane.add(SimpleGraphSpatialSearchTest())
            jf.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            jf.pack()
            jf.isVisible = true
        }
    }
}
