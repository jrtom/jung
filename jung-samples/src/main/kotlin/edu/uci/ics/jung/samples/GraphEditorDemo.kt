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
import com.google.common.graph.NetworkBuilder
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.StaticLayoutAlgorithm
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.annotations.AnnotationControls
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse
import edu.uci.ics.jung.visualization.control.ModalGraphMouse
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode
import edu.uci.ics.jung.visualization.control.ScalingControl
import edu.uci.ics.jung.visualization.util.ParallelEdgeIndexFunction
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.event.ActionEvent
import java.awt.image.BufferedImage
import java.awt.print.PageFormat
import java.awt.print.Printable
import java.awt.print.PrinterJob
import java.io.File
import java.util.function.Function
import java.util.function.Supplier
import javax.imageio.ImageIO
import javax.swing.AbstractAction
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.WindowConstants

/**
 * Shows how to create a graph editor with JUNG. Mouse modes and actions are explained in the help
 * text. The application version of GraphEditorDemo provides a File menu with an option to save the
 * visible graph as a jpeg file.
 *
 * @author Tom Nelson
 */
class GraphEditorDemo : JPanel(), Printable {

    /** the graph */
    private val graph: MutableNetwork<Number, Number>
    private val layoutAlgorithm: LayoutAlgorithm<Number>

    /** the visual component and renderer for the graph */
    private val vv: VisualizationViewer<Number, Number>

    private val instructions =
        "<html>" +
            "<h3>All Modes:</h3>" +
            "<ul>" +
            "<li>Right-click an empty area for <b>Create Node</b> popup" +
            "<li>Right-click on a Node for <b>Delete Node</b> popup" +
            "<li>Right-click on a Node for <b>Add Edge</b> menus <br>(if there are selected Nodes)" +
            "<li>Right-click on an Edge for <b>Delete Edge</b> popup" +
            "<li>Mousewheel scales with a crossover value of 1.0.<p>" +
            "     - scales the graph layout when the combined scale is greater than 1<p>" +
            "     - scales the graph view when the combined scale is less than 1" +
            "</ul>" +
            "<h3>Editing Mode:</h3>" +
            "<ul>" +
            "<li>Left-click an empty area to create a new Node" +
            "<li>Left-click on a Node and drag to another Node to create an Undirected Edge" +
            "<li>Shift+Left-click on a Node and drag to another Node to create a Directed Edge" +
            "</ul>" +
            "<h3>Picking Mode:</h3>" +
            "<ul>" +
            "<li>Mouse1 on a Node selects the node" +
            "<li>Mouse1 elsewhere unselects all Nodes" +
            "<li>Mouse1+Shift on a Node adds/removes Node selection" +
            "<li>Mouse1+drag on a Node moves all selected Nodes" +
            "<li>Mouse1+drag elsewhere selects Nodes in a region" +
            "<li>Mouse1+Shift+drag adds selection of Nodes in a new region" +
            "<li>Mouse1+CTRL on a Node selects the node and centers the display on it" +
            "<li>Mouse1 double-click on a node or edge allows you to edit the label" +
            "</ul>" +
            "<h3>Transforming Mode:</h3>" +
            "<ul>" +
            "<li>Mouse1+drag pans the graph" +
            "<li>Mouse1+Shift+drag rotates the graph" +
            "<li>Mouse1+CTRL(or Command)+drag shears the graph" +
            "<li>Mouse1 double-click on a node or edge allows you to edit the label" +
            "</ul>" +
            "<h3>Annotation Mode:</h3>" +
            "<ul>" +
            "<li>Mouse1 begins drawing of a Rectangle" +
            "<li>Mouse1+drag defines the Rectangle shape" +
            "<li>Mouse1 release adds the Rectangle as an annotation" +
            "<li>Mouse1+Shift begins drawing of an Ellipse" +
            "<li>Mouse1+Shift+drag defines the Ellipse shape" +
            "<li>Mouse1+Shift release adds the Ellipse as an annotation" +
            "<li>Mouse3 shows a popup to input text, which will become" +
            "<li>a text annotation on the graph at the mouse location" +
            "</ul>" +
            "</html>"

    /** create an instance of a simple graph with popup controls to create a graph. */
    init {
        layout = BorderLayout()
        // create a simple graph for the demo
        graph = NetworkBuilder.directed().allowsParallelEdges(true).allowsSelfLoops(true).build()

        layoutAlgorithm = StaticLayoutAlgorithm()

        vv = VisualizationViewer(graph, layoutAlgorithm, Dimension(600, 600))
        vv.background = Color.white

        val labeller = Function<Any, String> { it.toString() }
        vv.getRenderContext().setNodeLabelFunction(labeller)
        vv.getRenderContext().setEdgeLabelFunction(labeller)
        vv.getRenderContext().setParallelEdgeIndexFunction(ParallelEdgeIndexFunction())

        vv.setNodeToolTipFunction(vv.getRenderContext().getNodeLabelFunction())

        val panel = GraphZoomScrollPane(vv)
        add(panel)
        val nodeFactory: Supplier<Number> = NodeFactory()
        val edgeFactory: Supplier<Number> = EdgeFactory()

        val graphMouse = EditingModalGraphMouse<Number, Number>(
            vv.getRenderContext(), nodeFactory, edgeFactory
        )

        // the EditingGraphMouse will pass mouse event coordinates to the
        // nodeLocations function to set the locations of the nodes as
        // they are created
        vv.setGraphMouse(graphMouse)
        vv.addKeyListener(graphMouse.modeKeyListener!!)

        graphMouse.setMode(ModalGraphMouse.Mode.EDITING)

        val scaler: ScalingControl = CrossoverScalingControl()
        val plus = JButton("+")
        plus.addActionListener { scaler.scale(vv, 1.1f, vv.getCenter()) }

        val minus = JButton("-")
        minus.addActionListener { scaler.scale(vv, 1 / 1.1f, vv.getCenter()) }

        val help = JButton("Help")
        help.addActionListener { JOptionPane.showMessageDialog(vv, instructions) }

        val annotationControls = AnnotationControls<Number, Number>(graphMouse.getAnnotatingPlugin())
        val controls = JPanel()
        controls.add(plus)
        controls.add(minus)
        val modeBox: JComboBox<Mode> = graphMouse.getModeComboBox()
        controls.add(modeBox)
        controls.add(annotationControls.getAnnotationsToolBar())
        controls.add(help)
        add(controls, BorderLayout.SOUTH)
    }

    /**
     * copy the visible part of the graph to a file as a jpeg image
     *
     * @param file the file in which to save the graph image
     */
    fun writeJPEGImage(file: File) {
        val width = vv.width
        val height = vv.height

        val bi = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = bi.createGraphics()
        vv.paint(graphics)
        graphics.dispose()

        try {
            ImageIO.write(bi, "jpeg", file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun print(
        graphics: java.awt.Graphics,
        pageFormat: PageFormat,
        pageIndex: Int
    ): Int {
        return if (pageIndex > 0) {
            Printable.NO_SUCH_PAGE
        } else {
            val g2d = graphics as Graphics2D
            vv.isDoubleBuffered = false
            g2d.translate(pageFormat.imageableX, pageFormat.imageableY)

            vv.paint(g2d)
            vv.isDoubleBuffered = true

            Printable.PAGE_EXISTS
        }
    }

    internal inner class NodeFactory : Supplier<Number> {
        var i = 0
        override fun get(): Number = i++
    }

    internal inner class EdgeFactory : Supplier<Number> {
        var i = 0
        override fun get(): Number = i++
    }

    companion object {
        private const val serialVersionUID = -2023243689258876709L

        @JvmStatic
        fun main(args: Array<String>) {
            val frame = JFrame()
            frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            val demo = GraphEditorDemo()

            val menu = JMenu("File")
            menu.add(object : AbstractAction("Make Image") {
                override fun actionPerformed(e: ActionEvent) {
                    val chooser = JFileChooser()
                    val option = chooser.showSaveDialog(demo)
                    if (option == JFileChooser.APPROVE_OPTION) {
                        val file = chooser.selectedFile
                        demo.writeJPEGImage(file)
                    }
                }
            })
            menu.add(object : AbstractAction("Print") {
                override fun actionPerformed(e: ActionEvent) {
                    val printJob = PrinterJob.getPrinterJob()
                    printJob.setPrintable(demo)
                    if (printJob.printDialog()) {
                        try {
                            printJob.print()
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
            })
            JPopupMenu.setDefaultLightWeightPopupEnabled(false)
            val menuBar = JMenuBar()
            menuBar.add(menu)
            frame.jMenuBar = menuBar
            frame.contentPane.add(demo)
            frame.pack()
            frame.isVisible = true
        }
    }
}
