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
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.MultiLayerTransformer
import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.ModalGraphMouse
import edu.uci.ics.jung.visualization.control.ScalingControl
import edu.uci.ics.jung.visualization.decorators.EdgeShape
import edu.uci.ics.jung.visualization.layout.LayoutAlgorithmTransition
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
import java.awt.geom.Point2D
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JToggleButton
import javax.swing.WindowConstants

/**
 * A variant of TreeLayoutDemo that rotates the view by 90 degrees from the default orientation.
 *
 * @author Tom Nelson
 */
class L2RTreeLayoutDemo : JPanel() {

    /** the graph */
    val graph: CTreeNetwork<String, Int>

    /** the visual component and renderer for the graph */
    val vv: VisualizationViewer<String, Int>

    var rings: VisualizationServer.Paintable? = null

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

        setLtoR(vv)

        val panel = GraphZoomScrollPane(vv)
        add(panel)

        val graphMouse = DefaultModalGraphMouse<String, Int>()

        vv.setGraphMouse(graphMouse)

        val modeBox: JComboBox<ModalGraphMouse.Mode> = graphMouse.getModeComboBox()
        modeBox.addItemListener(graphMouse.getModeListener())
        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING)

        val scaler: ScalingControl = CrossoverScalingControl()

        val plus = JButton("+")
        plus.addActionListener { scaler.scale(vv, 1.1f, vv.getCenter()) }

        val minus = JButton("-")
        minus.addActionListener { scaler.scale(vv, 1 / 1.1f, vv.getCenter()) }

        val radial = JToggleButton("Radial")
        radial.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                LayoutAlgorithmTransition.animate(vv, radialLayoutAlgorithm)
                vv.getRenderContext().getMultiLayerTransformer().setToIdentity()
                if (rings == null) {
                    rings = Rings(vv.getModel().getLayoutModel())
                }
                vv.addPreRenderPaintable(rings!!)
            } else {
                LayoutAlgorithmTransition.animate(vv, treeLayoutAlgorithm)
                vv.getRenderContext().getMultiLayerTransformer().setToIdentity()
                setLtoR(vv)
                vv.removePreRenderPaintable(rings!!)
            }

            vv.repaint()
        }

        val scaleGrid = JPanel(GridLayout(1, 0))
        scaleGrid.border = BorderFactory.createTitledBorder("Zoom")

        val controls = JPanel()
        scaleGrid.add(plus)
        scaleGrid.add(minus)
        controls.add(radial)
        controls.add(scaleGrid)
        controls.add(modeBox)

        add(controls, BorderLayout.SOUTH)
    }

    private fun setLtoR(vv: VisualizationViewer<String, Int>) {
        val d = vv.getModel().getLayoutSize()
        val center = Point2D.Double(d.width / 2.0, d.height / 2.0)
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .getTransformer(MultiLayerTransformer.Layer.LAYOUT)
            .rotate(-Math.PI / 2, center)
    }

    inner class Rings(val layoutModel: LayoutModel<String>) : VisualizationServer.Paintable {

        val depths: Collection<Double> = computeDepths()

        private fun computeDepths(): Collection<Double> {
            val depths = HashSet<Double>()
            val polarLocations: Map<String, PolarPoint> = radialLayoutAlgorithm.polarLocations
            for (v in graph.nodes()) {
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
                ellipse.setFrameFromDiagonal(center.x - d, center.y - d, center.x + d, center.y + d)
                val shape: Shape = vv.getRenderContext()
                    .getMultiLayerTransformer()
                    .getTransformer(MultiLayerTransformer.Layer.LAYOUT)
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
        @JvmStatic
        fun main(args: Array<String>) {
            val frame = JFrame()
            val content: Container = frame.contentPane
            frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE

            content.add(L2RTreeLayoutDemo())
            frame.pack()
            frame.isVisible = true
        }
    }
}
