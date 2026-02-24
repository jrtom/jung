/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.samples

import edu.uci.ics.jung.graph.CTreeNetwork
import edu.uci.ics.jung.graph.MutableCTreeNetwork
import edu.uci.ics.jung.graph.TreeNetworkBuilder
import edu.uci.ics.jung.layout.algorithms.RadialTreeLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.TreeLayoutAlgorithm
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.layout.model.PolarPoint
import edu.uci.ics.jung.samples.util.ControlHelpers
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer
import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.ModalGraphMouse
import edu.uci.ics.jung.visualization.decorators.EdgeShape
import edu.uci.ics.jung.visualization.layout.LayoutAlgorithmTransition
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Container
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridLayout
import java.awt.Shape
import java.awt.event.ItemEvent
import java.awt.geom.Ellipse2D
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JToggleButton
import javax.swing.WindowConstants

/**
 * Demonstrates TreeLayout and RadialTreeLayout.
 *
 * @author Tom Nelson
 */
@Suppress("serial")
class TreeLayoutDemo : JPanel() {

    val graph: CTreeNetwork<String, Int>

    /** the visual component and renderer for the graph */
    val vv: VisualizationViewer<String, Int>

    var rings: VisualizationServer.Paintable? = null

    var root: String? = null

    val treeLayoutAlgorithm: TreeLayoutAlgorithm<String>

    val radialLayoutAlgorithm: RadialTreeLayoutAlgorithm<String>

    init {
        layout = BorderLayout()
        // create a simple graph for the demo
        graph = createTree()

        treeLayoutAlgorithm = TreeLayoutAlgorithm()
        radialLayoutAlgorithm = RadialTreeLayoutAlgorithm()
        vv = VisualizationViewer(graph, treeLayoutAlgorithm, Dimension(600, 600))
        vv.background = Color.white
        vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line())
        vv.getRenderContext().setNodeLabelFunction { it.toString() }
        // add a listener for ToolTips
        vv.setNodeToolTipFunction { it.toString() }
        vv.getRenderContext().setArrowFillPaintFunction { Color.lightGray }

        val panel = GraphZoomScrollPane(vv)
        add(panel)

        val graphMouse = DefaultModalGraphMouse<String, Int>()

        vv.setGraphMouse(graphMouse)

        val modeBox: JComboBox<ModalGraphMouse.Mode> = graphMouse.getModeComboBox()
        modeBox.addItemListener(graphMouse.getModeListener())
        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING)

        val animate = JRadioButton("Animate Transition")
        val radial = JToggleButton("Radial")
        radial.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                if (animate.isSelected) {
                    LayoutAlgorithmTransition.animate(vv, radialLayoutAlgorithm)
                } else {
                    LayoutAlgorithmTransition.apply(vv, radialLayoutAlgorithm)
                }
                vv.getRenderContext().getMultiLayerTransformer().setToIdentity()
                if (rings == null) {
                    rings = Rings(vv.getModel().getLayoutModel())
                }
                vv.addPreRenderPaintable(rings!!)
            } else {
                if (animate.isSelected) {
                    LayoutAlgorithmTransition.animate(vv, treeLayoutAlgorithm)
                } else {
                    LayoutAlgorithmTransition.apply(vv, treeLayoutAlgorithm)
                }
                vv.getRenderContext().getMultiLayerTransformer().setToIdentity()
                rings?.let { vv.removePreRenderPaintable(it) }
            }
            vv.repaint()
        }

        val layoutPanel = JPanel(GridLayout(2, 1))
        layoutPanel.add(radial)
        layoutPanel.add(animate)
        val controls = JPanel()
        controls.add(layoutPanel)
        controls.add(ControlHelpers.getZoomControls(vv, "Zoom"))
        controls.add(modeBox)

        add(controls, BorderLayout.SOUTH)
    }

    inner class Rings(val layoutModel: LayoutModel<String>) : VisualizationServer.Paintable {

        val depths: Collection<Double> = getDepths()

        private fun getDepths(): Set<Double> {
            val depths = HashSet<Double>()
            val polarLocations: Map<String, PolarPoint> = radialLayoutAlgorithm.polarLocations
            for (v in graph.nodes().toSet()) {
                val pp = polarLocations[v]
                depths.add(pp!!.radius)
            }
            return depths
        }

        override fun paint(g: Graphics) {
            g.color = Color.lightGray

            val g2d = g as Graphics2D
            val center: Point = radialLayoutAlgorithm.getCenter(layoutModel)

            val ellipse = Ellipse2D.Double()
            for (d in depths) {
                ellipse.setFrameFromDiagonal(
                    center.x - d, center.y - d, center.x + d, center.y + d
                )
                val shape: Shape = vv.getRenderContext()
                    .getMultiLayerTransformer()
                    .getTransformer(Layer.LAYOUT)
                    .transform(ellipse)
                g2d.draw(shape)
            }
        }

        override fun useTransform(): Boolean = true
    }

    private fun createTree(): CTreeNetwork<String, Int> {
        val tree: MutableCTreeNetwork<String, Int> =
            TreeNetworkBuilder.builder().expectedNodeCount(27).build()

        tree.addNode("root")

        var edgeId = 0
        tree.addEdge("root", "V0", edgeId++)
        tree.addEdge("V0", "V1", edgeId++)
        tree.addEdge("V0", "V2", edgeId++)
        tree.addEdge("V1", "V4", edgeId++)
        tree.addEdge("V2", "V3", edgeId++)
        tree.addEdge("V2", "V5", edgeId++)
        tree.addEdge("V4", "V6", edgeId++)
        tree.addEdge("V4", "V7", edgeId++)
        tree.addEdge("V3", "V8", edgeId++)
        tree.addEdge("V6", "V9", edgeId++)
        tree.addEdge("V4", "V10", edgeId++)

        tree.addEdge("root", "A0", edgeId++)
        tree.addEdge("A0", "A1", edgeId++)
        tree.addEdge("A0", "A2", edgeId++)
        tree.addEdge("A0", "A3", edgeId++)

        tree.addEdge("root", "B0", edgeId++)
        tree.addEdge("B0", "B1", edgeId++)
        tree.addEdge("B0", "B2", edgeId++)
        tree.addEdge("B1", "B4", edgeId++)
        tree.addEdge("B2", "B3", edgeId++)
        tree.addEdge("B2", "B5", edgeId++)
        tree.addEdge("B4", "B6", edgeId++)
        tree.addEdge("B4", "B7", edgeId++)
        tree.addEdge("B3", "B8", edgeId++)
        tree.addEdge("B6", "B9", edgeId++)

        return tree
    }

    companion object {
        private val log = LoggerFactory.getLogger(TreeLayoutDemo::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            val frame = JFrame()
            val content: Container = frame.contentPane
            frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE

            content.add(TreeLayoutDemo())
            frame.pack()
            frame.isVisible = true
        }
    }
}
