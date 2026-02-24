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
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.annotations.AnnotatingGraphMousePlugin
import edu.uci.ics.jung.visualization.annotations.AnnotatingModalGraphMouse
import edu.uci.ics.jung.visualization.annotations.AnnotationControls
import edu.uci.ics.jung.visualization.control.ModalGraphMouse
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction
import edu.uci.ics.jung.visualization.renderers.Renderer
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Container
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.WindowConstants

/**
 * Demonstrates annotation of graph elements.
 *
 * @author Tom Nelson
 */
class AnnotationsDemo : JPanel() {

    private val helpDialog: JDialog

    /** create an instance of a simple graph in two views with controls to demo the features. */
    init {
        layout = BorderLayout()
        // create a simple graph for the demo
        val graph: Network<String, Number> = TestGraphs.getOneComponentGraph()

        // the preferred sizes for the two views
        val preferredSize1 = Dimension(600, 600)

        // create one layout for the graph
        val layoutAlgorithm = FRLayoutAlgorithm<String>()
        layoutAlgorithm.setMaxIterations(500)

        val vm = BaseVisualizationModel(graph, layoutAlgorithm, preferredSize1)

        // create 2 views that share the same model
        val vv = VisualizationViewer(vm, preferredSize1)
        vv.background = Color.white
        vv.getRenderContext().setEdgeDrawPaintFunction(
            PickableEdgePaintFunction(vv.getPickedEdgeState(), Color.black, Color.cyan)
        )
        vv.getRenderContext().setNodeFillPaintFunction(
            PickableNodePaintFunction(vv.getPickedNodeState(), Color.red, Color.yellow)
        )
        vv.getRenderContext().setNodeLabelFunction { it.toString() }
        vv.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.CNTR)

        // add default listener for ToolTips
        vv.setNodeToolTipFunction { n -> n }

        val panel: Container = JPanel(BorderLayout())

        val gzsp = GraphZoomScrollPane(vv)
        panel.add(gzsp)

        helpDialog = JDialog()
        helpDialog.contentPane.add(JLabel(instructions))

        val rc = vv.getRenderContext()
        val annotatingPlugin = AnnotatingGraphMousePlugin<String, Number>(rc)
        // create a GraphMouse for the main view
        val graphMouse = AnnotatingModalGraphMouse<String, Number>(rc, annotatingPlugin)
        vv.setGraphMouse(graphMouse)
        vv.addKeyListener(graphMouse.modeKeyListener!!)

        val modeBox = graphMouse.getModeComboBox()
        modeBox.selectedItem = ModalGraphMouse.Mode.ANNOTATING

        val help = JButton("Help")
        help.addActionListener {
            helpDialog.pack()
            helpDialog.isVisible = true
        }

        val controls = JPanel()
        controls.add(ControlHelpers.getZoomControls(vv, "Zoom"))

        val modeControls = JPanel()
        modeControls.border = BorderFactory.createTitledBorder("Mouse Mode")
        modeControls.add(graphMouse.getModeComboBox())
        controls.add(modeControls)

        val annotationControlPanel = JPanel()
        annotationControlPanel.border = BorderFactory.createTitledBorder("Annotation Controls")

        val annotationControls = AnnotationControls<String, Number>(annotatingPlugin)

        annotationControlPanel.add(annotationControls.getAnnotationsToolBar())
        controls.add(annotationControlPanel)

        val helpControls = JPanel()
        helpControls.border = BorderFactory.createTitledBorder("Help")
        helpControls.add(help)
        controls.add(helpControls)
        add(panel)
        add(controls, BorderLayout.SOUTH)
    }

    companion object {
        val instructions =
            "<html>" +
                "<b><h2><center>Instructions for Annotations</center></h2></b>" +
                "<p>The Annotation Controls allow you to select:" +
                "<ul>" +
                "<li>Shape" +
                "<li>Color" +
                "<li>Fill (or outline)" +
                "<li>Above or below (UPPER/LOWER) the graph display" +
                "</ul>" +
                "<p>Mouse Button one press starts a Shape," +
                "<p>drag and release to complete." +
                "<p>Mouse Button three pops up an input dialog" +
                "<p>for text. This will create a text annotation." +
                "<p>You may use html for multi-line, etc." +
                "<p>You may even use an image tag and image url" +
                "<p>to put an image in the annotation." +
                "<p><p>" +
                "<p>To remove an annotation, shift-click on it" +
                "<p>in the Annotations mode." +
                "<p>If there is overlap, the Annotation with center" +
                "<p>closest to the mouse point will be removed."

        @JvmStatic
        fun main(args: Array<String>) {
            val f = JFrame()
            f.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            f.contentPane.add(AnnotationsDemo())
            f.pack()
            f.isVisible = true
        }
    }
}
