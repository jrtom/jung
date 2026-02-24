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
import edu.uci.ics.jung.layout.algorithms.ISOMLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.ModalGraphMouse
import edu.uci.ics.jung.visualization.control.SatelliteVisualizationViewer
import edu.uci.ics.jung.visualization.control.ScalingControl
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Container
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JDesktopPane
import javax.swing.JFrame
import javax.swing.JInternalFrame
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.WindowConstants

/**
 * Similar to the SatelliteViewDemo, but using JInternalFrame.
 *
 * @author Tom Nelson
 */
class InternalFrameSatelliteViewDemo {

    /** the graph */
    val graph: Network<String, Number>

    /** the visual component and renderer for the graph */
    val vv: VisualizationViewer<String, Number>

    val satellite: VisualizationViewer<String, Number>

    val dialog: JInternalFrame

    val desktop: JDesktopPane

    /** create an instance of a simple graph with controls to demo the zoom features. */
    init {
        // create a simple graph for the demo
        graph = TestGraphs.getOneComponentGraph()

        val layout: LayoutAlgorithm<String> = ISOMLayoutAlgorithm()

        vv = VisualizationViewer(graph, layout, Dimension(600, 600))
        vv.getRenderContext()
            .setEdgeDrawPaintFunction(
                PickableEdgePaintFunction(vv.getPickedEdgeState(), Color.black, Color.cyan)
            )
        vv.getRenderContext()
            .setNodeFillPaintFunction(
                PickableNodePaintFunction(vv.getPickedNodeState(), Color.red, Color.yellow)
            )

        // add my listener for ToolTips
        vv.setNodeToolTipFunction { it.toString() }
        val graphMouse = DefaultModalGraphMouse<String, Number>()
        vv.setGraphMouse(graphMouse)

        satellite = SatelliteVisualizationViewer(vv, Dimension(200, 200))
        satellite
            .getRenderContext()
            .setEdgeDrawPaintFunction(
                PickableEdgePaintFunction(satellite.getPickedEdgeState(), Color.black, Color.cyan)
            )
        satellite
            .getRenderContext()
            .setNodeFillPaintFunction(
                PickableNodePaintFunction(satellite.getPickedNodeState(), Color.red, Color.yellow)
            )

        val satelliteScaler: ScalingControl = CrossoverScalingControl()
        satellite.scaleToLayout(satelliteScaler)

        val frame = JFrame()
        desktop = JDesktopPane()
        var content: Container = frame.contentPane
        val panel = JPanel(BorderLayout())
        panel.add(desktop)
        content.add(panel)
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE

        val vvFrame = JInternalFrame()
        vvFrame.contentPane.add(vv)
        vvFrame.pack()
        vvFrame.isVisible = true // necessary as of 1.3
        desktop.add(vvFrame)
        try {
            vvFrame.isSelected = true
        } catch (e: java.beans.PropertyVetoException) {
        }

        dialog = JInternalFrame()
        desktop.add(dialog)
        content = dialog.contentPane

        val scaler: ScalingControl = CrossoverScalingControl()

        val plus = JButton("+")
        plus.addActionListener { scaler.scale(vv, 1.1f, vv.getCenter()) }
        val minus = JButton("-")
        minus.addActionListener { scaler.scale(vv, 1 / 1.1f, vv.getCenter()) }

        val dismiss = JButton("Dismiss")
        dismiss.addActionListener { dialog.isVisible = false }

        val help = JButton("Help")
        help.addActionListener {
            JOptionPane.showInternalMessageDialog(
                dialog, instructions, "Instructions", JOptionPane.PLAIN_MESSAGE
            )
        }
        val controls = JPanel(GridLayout(2, 2))
        controls.add(plus)
        controls.add(minus)
        controls.add(dismiss)
        controls.add(help)
        content.add(satellite)
        content.add(controls, BorderLayout.SOUTH)

        val zoomer = JButton("Show Satellite View")
        zoomer.addActionListener {
            dialog.pack()
            dialog.setLocation(desktop.width - dialog.width, 0)
            @Suppress("DEPRECATION")
            dialog.show()
            try {
                dialog.isSelected = true
            } catch (ex: java.beans.PropertyVetoException) {
            }
        }

        val modeBox: JComboBox<ModalGraphMouse.Mode> = graphMouse.getModeComboBox()
        modeBox.addItemListener((satellite.getGraphMouse()!! as ModalGraphMouse).getModeListener())
        val p = JPanel()
        p.add(zoomer)
        p.add(modeBox)

        frame.contentPane.add(p, BorderLayout.SOUTH)
        frame.setSize(800, 800)
        frame.isVisible = true
    }

    companion object {
        val instructions =
            "<html>" +
                "<b><h2><center>Instructions for Mouse Listeners</center></h2></b>" +
                "<p>There are two modes, Transforming and Picking." +
                "<p>The modes are selected with a toggle button." +
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
                "<li>Mousewheel scales the layout &gt; 1 and scales the view &lt; 1"

        @JvmStatic
        fun main(args: Array<String>) {
            InternalFrameSatelliteViewDemo()
        }
    }
}
