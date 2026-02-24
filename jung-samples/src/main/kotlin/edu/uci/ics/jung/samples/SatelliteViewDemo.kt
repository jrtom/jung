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
import edu.uci.ics.jung.samples.util.ControlHelpers
import edu.uci.ics.jung.visualization.BaseVisualizationModel
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer
import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.SatelliteVisualizationViewer
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction
import edu.uci.ics.jung.visualization.renderers.GradientNodeRenderer
import edu.uci.ics.jung.visualization.renderers.Renderer
import edu.uci.ics.jung.visualization.transform.shape.ShapeTransformer
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridLayout
import java.awt.event.ItemEvent
import java.awt.geom.GeneralPath
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.ToolTipManager
import javax.swing.WindowConstants

/**
 * Demonstrates the construction of a graph visualization with a main and a satellite view. The
 * satellite view is smaller, always contains the entire graph, and contains a lens shape that shows
 * the boundaries of the visible part of the graph in the main view.
 *
 * @author Tom Nelson
 */
class SatelliteViewDemo : JPanel() {

    private val helpDialog: JDialog
    private val viewGrid: VisualizationServer.Paintable

    init {
        layout = BorderLayout()
        // create a simple graph for the demo
        val graph: Network<String, Number> = TestGraphs.getOneComponentGraph()

        // the preferred sizes for the two views
        val preferredSize1 = Dimension(600, 600)
        val preferredSize2 = Dimension(300, 300)

        // create one layout for the graph
        val layoutAlgorithm = FRLayoutAlgorithm<String>()
        layoutAlgorithm.setMaxIterations(500)

        // create one model that both views will share
        val vm = BaseVisualizationModel(graph, layoutAlgorithm, preferredSize1)

        // create 2 views that share the same model
        val vv1 = VisualizationViewer(vm, preferredSize1)
        val vv2 = SatelliteVisualizationViewer(vv1, preferredSize2)
        vv1.background = Color.white
        vv1.getRenderContext().setEdgeDrawPaintFunction(
            PickableEdgePaintFunction(vv1.getPickedEdgeState(), Color.black, Color.cyan)
        )
        vv1.getRenderContext().setNodeFillPaintFunction(
            PickableNodePaintFunction(vv1.getPickedNodeState(), Color.red, Color.yellow)
        )
        vv2.getRenderContext().setEdgeDrawPaintFunction(
            PickableEdgePaintFunction(vv2.getPickedEdgeState(), Color.black, Color.cyan)
        )
        vv2.getRenderContext().setNodeFillPaintFunction(
            PickableNodePaintFunction(vv2.getPickedNodeState(), Color.red, Color.yellow)
        )
        vv1.getRenderer().setNodeRenderer(GradientNodeRenderer(vv1, Color.red, Color.white, true))
        vv1.getRenderContext().setNodeLabelFunction { it.toString() }
        vv1.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.CNTR)

        val vv2Scaler = CrossoverScalingControl()
        vv2.scaleToLayout(vv2Scaler)

        viewGrid = ViewGrid(vv2, vv1)

        // add default listener for ToolTips
        vv1.setNodeToolTipFunction { it.toString() }
        vv2.setNodeToolTipFunction { it.toString() }

        vv2.getRenderContext().setNodeLabelFunction(vv1.getRenderContext().getNodeLabelFunction())

        ToolTipManager.sharedInstance().dismissDelay = 10000

        val panel = JPanel(BorderLayout())
        val rightPanel = JPanel(GridLayout(2, 1))

        val gzsp = GraphZoomScrollPane(vv1)
        panel.add(gzsp)
        rightPanel.add(JPanel())
        rightPanel.add(vv2)
        panel.add(rightPanel, BorderLayout.EAST)

        helpDialog = JDialog()
        helpDialog.contentPane.add(JLabel(instructions))

        // create a GraphMouse for the main view
        val graphMouse = DefaultModalGraphMouse<String, Number>()
        vv1.setGraphMouse(graphMouse)

        val modeBox = graphMouse.getModeComboBox()
        modeBox.addItemListener((vv2.getGraphMouse() as DefaultModalGraphMouse<*, *>).getModeListener())

        val gridBox = JCheckBox("Show Grid")
        gridBox.addItemListener { e -> showGrid(vv2, e.stateChange == ItemEvent.SELECTED) }

        val help = JButton("Help")
        help.addActionListener {
            helpDialog.pack()
            helpDialog.isVisible = true
        }

        val controls = JPanel()
        controls.add(ControlHelpers.getZoomControls(vv1, ""))
        controls.add(modeBox)
        controls.add(gridBox)
        controls.add(help)
        add(panel)
        add(controls, BorderLayout.SOUTH)
    }

    private fun showGrid(vv: VisualizationViewer<*, *>, state: Boolean) {
        if (state) {
            vv.addPreRenderPaintable(viewGrid)
        } else {
            vv.removePreRenderPaintable(viewGrid)
        }
        vv.repaint()
    }

    /**
     * Draws a grid on the SatelliteViewer's lens.
     *
     * @author Tom Nelson
     */
    private class ViewGrid(
        private val vv: VisualizationViewer<*, *>,
        private val master: VisualizationViewer<*, *>
    ) : VisualizationServer.Paintable {

        override fun paint(g: Graphics) {
            val masterViewTransformer: ShapeTransformer =
                master.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)
            val masterLayoutTransformer: ShapeTransformer =
                master.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)
            val vvLayoutTransformer: ShapeTransformer =
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)

            val rect = master.bounds
            var path = GeneralPath()
            path.moveTo(rect.x.toFloat(), rect.y.toFloat())
            path.lineTo(rect.width.toFloat(), rect.y.toFloat())
            path.lineTo(rect.width.toFloat(), rect.height.toFloat())
            path.lineTo(rect.x.toFloat(), rect.height.toFloat())
            path.lineTo(rect.x.toFloat(), rect.y.toFloat())

            var i = 0
            while (i <= rect.width) {
                path.moveTo((rect.x + i).toFloat(), rect.y.toFloat())
                path.lineTo((rect.x + i).toFloat(), rect.height.toFloat())
                i += rect.width / 10
            }
            i = 0
            while (i <= rect.height) {
                path.moveTo(rect.x.toFloat(), (rect.y + i).toFloat())
                path.lineTo(rect.width.toFloat(), (rect.y + i).toFloat())
                i += rect.height / 10
            }
            var lens = masterViewTransformer.inverseTransform(path)
            lens = masterLayoutTransformer.inverseTransform(lens)
            lens = vvLayoutTransformer.transform(lens)
            val g2d = g as Graphics2D
            val old = g.getColor()
            g.setColor(Color.cyan)
            g2d.draw(lens)

            path = GeneralPath()
            path.moveTo(rect.minX.toFloat(), rect.centerY.toFloat())
            path.lineTo(rect.maxX.toFloat(), rect.centerY.toFloat())
            path.moveTo(rect.centerX.toFloat(), rect.minY.toFloat())
            path.lineTo(rect.centerX.toFloat(), rect.maxY.toFloat())
            var crosshairShape = masterViewTransformer.inverseTransform(path)
            crosshairShape = masterLayoutTransformer.inverseTransform(crosshairShape)
            crosshairShape = vvLayoutTransformer.transform(crosshairShape)
            g.setColor(Color.black)
            g2d.stroke = BasicStroke(3f)
            g2d.draw(crosshairShape)

            g.setColor(old)
        }

        override fun useTransform(): Boolean = true
    }

    companion object {
        val instructions: String =
            "<html>" +
                "<b><h2><center>Instructions for Mouse Listeners</center></h2></b>" +
                "<p>There are two modes, Transforming and Picking." +
                "<p>The modes are selected with a combo box." +
                "<p><p><b>Transforming Mode:</b>" +
                "<ul>" +
                "<li>Mouse1+drag pans the graph" +
                "<li>Mouse1+Shift+drag rotates the graph" +
                "<li>Mouse1+CTRL(or Command)+drag shears the graph" +
                "</ul>" +
                "<b>Picking Mode:</b>" +
                "<ul>" +
                "<li>Mouse1 on a Node selects the node" +
                "<li>Mouse1 elsewhere unselects all Nodes" +
                "<li>Mouse1+Shift on a Node adds/removes Node selection" +
                "<li>Mouse1+drag on a Node moves all selected Nodes" +
                "<li>Mouse1+drag elsewhere selects Nodes in a region" +
                "<li>Mouse1+Shift+drag adds selection of Nodes in a new region" +
                "<li>Mouse1+CTRL on a Node selects the node and centers the display on it" +
                "</ul>" +
                "<b>Both Modes:</b>" +
                "<ul>" +
                "<li>Mousewheel scales with a crossover value of 1.0.<p>" +
                "     - scales the graph layout when the combined scale is greater than 1<p>" +
                "     - scales the graph view when the combined scale is less than 1"

        @JvmStatic
        fun main(args: Array<String>) {
            val f = JFrame()
            f.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            f.contentPane.add(SatelliteViewDemo())
            f.pack()
            f.isVisible = true
        }
    }
}
