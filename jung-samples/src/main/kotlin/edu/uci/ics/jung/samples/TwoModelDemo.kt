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
import edu.uci.ics.jung.graph.util.TestGraphs
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.ISOMLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.samples.util.ControlHelpers
import edu.uci.ics.jung.visualization.BaseVisualizationModel
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction
import edu.uci.ics.jung.visualization.picking.MultiPickedState
import edu.uci.ics.jung.visualization.picking.PickedState
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

/**
 * Demonstrates a single graph with 2 layouts in 2 views. They share picking, transforms, and a
 * pluggable renderer
 *
 * @author Tom Nelson
 */
@Suppress("serial")
class TwoModelDemo : JPanel() {

    /** the graph */
    val graph: Network<String, Number>

    /** the visual components and renderers for the graph */
    val vv1: VisualizationViewer<String, Number>

    val vv2: VisualizationViewer<String, Number>

    val preferredLayoutSize = Dimension(300, 300)

    /** create an instance of a simple graph in two views with controls to demo the zoom features. */
    init {
        layout = BorderLayout()
        // create a simple graph for the demo
        // both models will share one graph
        graph = TestGraphs.getOneComponentGraph()

        // create two layouts for the one graph, one layout for each model
        val layoutAlgorithm1: LayoutAlgorithm<String> = FRLayoutAlgorithm()
        val layoutAlgorithm2: LayoutAlgorithm<String> = ISOMLayoutAlgorithm()

        // create the two models, each with a different layout
        val vm1: VisualizationModel<String, Number> =
            BaseVisualizationModel(graph, layoutAlgorithm1, preferredLayoutSize)
        val vm2: VisualizationModel<String, Number> =
            BaseVisualizationModel(graph, layoutAlgorithm2, preferredLayoutSize)

        // create the two views, one for each model
        // they share the same renderer
        vv1 = VisualizationViewer(vm1, preferredLayoutSize)
        vv2 = VisualizationViewer(vm2, preferredLayoutSize)
        vv1.setRenderContext(vv2.getRenderContext())

        // share the model Function between the two models
        vv2.getRenderContext().setMultiLayerTransformer(vv1.getRenderContext().getMultiLayerTransformer())
        vv2.getRenderContext().getMultiLayerTransformer().addChangeListener(vv1)

        vv1.background = Color.white
        vv2.background = Color.white

        // share one PickedState between the two views
        val ps: PickedState<String> = MultiPickedState()
        vv1.setPickedNodeState(ps)
        vv2.setPickedNodeState(ps)
        val pes: PickedState<Number> = MultiPickedState()
        vv1.setPickedEdgeState(pes)
        vv2.setPickedEdgeState(pes)

        // set an edge paint function that will show picking for edges
        vv1.getRenderContext().setEdgeDrawPaintFunction(
            PickableEdgePaintFunction(vv1.getPickedEdgeState(), Color.black, Color.red)
        )
        vv1.getRenderContext().setNodeFillPaintFunction(
            PickableNodePaintFunction(vv1.getPickedNodeState(), Color.red, Color.yellow)
        )
        // add default listeners for ToolTips
        vv1.setNodeToolTipFunction { it.toString() }
        vv2.setNodeToolTipFunction { it.toString() }

        val panel = JPanel(GridLayout(1, 0))
        panel.add(GraphZoomScrollPane(vv1))
        panel.add(GraphZoomScrollPane(vv2))

        add(panel)

        // create a GraphMouse for each view
        val gm1 = DefaultModalGraphMouse<String, Number>()

        val gm2 = DefaultModalGraphMouse<String, Number>()

        vv1.setGraphMouse(gm1)
        vv2.setGraphMouse(gm2)

        val modePanel = JPanel()
        modePanel.border = BorderFactory.createTitledBorder("Mouse Mode")
        gm1.getModeComboBox().addItemListener(gm2.getModeListener())
        modePanel.add(gm1.getModeComboBox())

        val controls = JPanel()
        controls.add(ControlHelpers.getZoomControls(vv1, "Zoom"))
        controls.add(modePanel)
        add(controls, BorderLayout.SOUTH)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val f = JFrame()
            f.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            f.contentPane.add(TwoModelDemo())
            f.pack()
            f.isVisible = true
        }
    }
}
