/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.samples

import com.google.common.graph.Network
import edu.uci.ics.jung.algorithms.shortestpath.MinimumSpanningTree
import edu.uci.ics.jung.graph.util.TestGraphs
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.KKLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.StaticLayoutAlgorithm
import edu.uci.ics.jung.visualization.BaseVisualizationModel
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.ScalingControl
import edu.uci.ics.jung.visualization.decorators.EdgeShape
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction
import edu.uci.ics.jung.visualization.picking.MultiPickedState
import edu.uci.ics.jung.visualization.picking.PickedState
import edu.uci.ics.jung.visualization.renderers.Renderer
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.WindowConstants
import org.slf4j.LoggerFactory

/**
 * Demonstrates a single graph with 3 layouts in 3 views. The first view is an undirected graph
 * using KKLayout The second view show a TreeLayout view of a MinimumSpanningTree of the first
 * graph. The third view shows the complete graph of the first view, using the layout positions of
 * the MinimumSpanningTree tree view.
 *
 * @author Tom Nelson
 */
class MinimumSpanningTreeDemo : JPanel() {

    /** the graph */
    val graph: Network<String, Number>

    val tree: Network<String, Number>

    /** the visual components and renderers for the graph */
    val vv0: VisualizationViewer<String, Number>

    val vv1: VisualizationViewer<String, Number>
    val vv2: VisualizationViewer<String, Number>

    val preferredLayoutSize = Dimension(300, 300)
    val preferredSizeRect = Dimension(800, 250)

    /** create an instance of a simple graph in two views with controls to demo the zoom features. */
    init {
        layout = BorderLayout()
        // create a simple graph for the demo
        // both models will share one graph
        graph = TestGraphs.getDemoGraph()

        tree = MinimumSpanningTree.extractFrom(graph) { 1.0 }

        val layout0: LayoutAlgorithm<String> = KKLayoutAlgorithm()
        val layout1: LayoutAlgorithm<String> = FRLayoutAlgorithm()
        val layout2: LayoutAlgorithm<String> = StaticLayoutAlgorithm()

        // create the two models, each with a different layout
        val vm0: VisualizationModel<String, Number> =
            BaseVisualizationModel(graph, layout0, preferredLayoutSize)
        val vm1: VisualizationModel<String, Number> =
            BaseVisualizationModel(tree, layout1, preferredSizeRect)
        // initializer is the layout model for vm1
        // and the size is also set to the same size required for the Tree in layout1
        val vm2: VisualizationModel<String, Number> =
            BaseVisualizationModel(graph, layout2, vm1.getLayoutModel(), vm1.getLayoutSize())

        // create the two views, one for each model
        // they share the same renderer
        vv0 = VisualizationViewer(vm0, preferredLayoutSize)
        vv1 = VisualizationViewer(vm1, preferredSizeRect)
        vv2 = VisualizationViewer(vm2, preferredSizeRect)

        vv1.getRenderContext().setMultiLayerTransformer(vv0.getRenderContext().getMultiLayerTransformer())
        vv2.getRenderContext().setMultiLayerTransformer(vv0.getRenderContext().getMultiLayerTransformer())

        vv1.getRenderContext().setEdgeShapeFunction(EdgeShape.line())

        vv0.addChangeListener(vv1)
        vv1.addChangeListener(vv2)

        vv0.getRenderContext().setNodeLabelFunction { it.toString() }
        vv2.getRenderContext().setNodeLabelFunction { it.toString() }

        val back = Color.decode("0xffffbb")
        vv0.background = back
        vv1.background = back
        vv2.background = back

        vv0.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.CNTR)
        vv0.foreground = Color.darkGray
        vv1.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.CNTR)
        vv1.foreground = Color.darkGray
        vv2.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.CNTR)
        vv2.foreground = Color.darkGray

        // share one PickedState between the two views
        val ps: PickedState<String> = MultiPickedState()
        vv0.setPickedNodeState(ps)
        vv1.setPickedNodeState(ps)
        vv2.setPickedNodeState(ps)

        val pes: PickedState<Number> = MultiPickedState()
        vv0.setPickedEdgeState(pes)
        vv1.setPickedEdgeState(pes)
        vv2.setPickedEdgeState(pes)

        // set an edge paint function that will show picking for edges
        vv0.getRenderContext()
            .setEdgeDrawPaintFunction(
                PickableEdgePaintFunction(vv0.getPickedEdgeState(), Color.black, Color.red)
            )
        vv0.getRenderContext()
            .setNodeFillPaintFunction(
                PickableNodePaintFunction(vv0.getPickedNodeState(), Color.red, Color.yellow)
            )
        vv1.getRenderContext()
            .setEdgeDrawPaintFunction(
                PickableEdgePaintFunction(vv1.getPickedEdgeState(), Color.black, Color.red)
            )
        vv1.getRenderContext()
            .setNodeFillPaintFunction(
                PickableNodePaintFunction(vv1.getPickedNodeState(), Color.red, Color.yellow)
            )

        // add default listeners for ToolTips
        vv0.setNodeToolTipFunction { it.toString() }
        vv1.setNodeToolTipFunction { it.toString() }
        vv2.setNodeToolTipFunction { it.toString() }

        vv0.setLayout(BorderLayout())
        vv1.setLayout(BorderLayout())
        vv2.setLayout(BorderLayout())

        val font = vv0.font.deriveFont(Font.BOLD, 16f)
        val vv0Label = JLabel("<html>Original Network<p>using KKLayout")
        vv0Label.font = font
        val vv1Label = JLabel("Minimum Spanning Trees")
        vv1Label.font = font
        val vv2Label = JLabel("Original Graph using TreeLayout")
        vv2Label.font = font
        val flow0 = JPanel()
        flow0.isOpaque = false
        val flow1 = JPanel()
        flow1.isOpaque = false
        val flow2 = JPanel()
        flow2.isOpaque = false
        flow0.add(vv0Label)
        flow1.add(vv1Label)
        flow2.add(vv2Label)
        vv0.add(flow0, BorderLayout.NORTH)
        vv1.add(flow1, BorderLayout.NORTH)
        vv2.add(flow2, BorderLayout.NORTH)

        val grid = JPanel(GridLayout(0, 1))
        val panel = JPanel(BorderLayout())
        panel.add(GraphZoomScrollPane(vv0), BorderLayout.WEST)
        grid.add(GraphZoomScrollPane(vv1))
        grid.add(GraphZoomScrollPane(vv2))
        panel.add(grid)

        add(panel)

        // create a GraphMouse for each view
        val gm0 = DefaultModalGraphMouse<String, Number>()
        val gm1 = DefaultModalGraphMouse<String, Number>()
        val gm2 = DefaultModalGraphMouse<String, Number>()

        vv0.setGraphMouse(gm0)
        vv1.setGraphMouse(gm1)
        vv2.setGraphMouse(gm2)

        // create zoom buttons for scaling the Function that is
        // shared between the two models.
        val scaler: ScalingControl = CrossoverScalingControl()
        vv0.scaleToLayout(scaler)

        val plus = JButton("+")
        plus.addActionListener { scaler.scale(vv1, 1.1f, vv1.getCenter()) }

        val minus = JButton("-")
        minus.addActionListener { scaler.scale(vv1, 1 / 1.1f, vv1.getCenter()) }

        val zoomPanel = JPanel(GridLayout(1, 2))
        zoomPanel.border = BorderFactory.createTitledBorder("Zoom")

        val modePanel = JPanel()
        modePanel.border = BorderFactory.createTitledBorder("Mouse Mode")
        gm1.getModeComboBox().addItemListener(gm2.getModeListener())
        gm1.getModeComboBox().addItemListener(gm0.getModeListener())
        modePanel.add(gm1.getModeComboBox())

        val controls = JPanel()
        zoomPanel.add(plus)
        zoomPanel.add(minus)
        controls.add(zoomPanel)
        controls.add(modePanel)
        add(controls, BorderLayout.SOUTH)
    }

    companion object {
        private val log = LoggerFactory.getLogger(MinimumSpanningTreeDemo::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            val f = JFrame()
            f.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            f.contentPane.add(MinimumSpanningTreeDemo())
            f.pack()
            f.isVisible = true
        }
    }
}
