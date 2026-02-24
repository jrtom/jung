/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.samples

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.google.common.graph.MutableNetwork
import com.google.common.graph.Network
import com.google.common.graph.NetworkBuilder
import edu.uci.ics.jung.layout.algorithms.FRBHVisitorLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.visualization.BaseVisualizationModel
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.LensMagnificationGraphMousePlugin
import edu.uci.ics.jung.visualization.control.ModalLensGraphMouse
import edu.uci.ics.jung.visualization.control.ScalingControl
import edu.uci.ics.jung.visualization.transform.HyperbolicTransformer
import edu.uci.ics.jung.visualization.transform.LayoutLensSupport
import edu.uci.ics.jung.visualization.transform.Lens
import edu.uci.ics.jung.visualization.transform.LensSupport
import edu.uci.ics.jung.visualization.transform.MagnifyTransformer
import edu.uci.ics.jung.visualization.transform.shape.HyperbolicShapeTransformer
import edu.uci.ics.jung.visualization.transform.shape.MagnifyShapeTransformer
import edu.uci.ics.jung.visualization.transform.shape.ViewLensSupport
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridLayout
import java.awt.Insets
import java.awt.Rectangle
import java.awt.event.ItemEvent
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D
import javax.swing.Box
import javax.swing.BorderFactory
import javax.swing.ButtonGroup
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JMenuBar
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.WindowConstants
import javax.swing.plaf.basic.BasicLabelUI

/**
 * Demonstrates the use of `HyperbolicTransform` and `MagnifyTransform`
 * applied to either the model (graph layout) or the view (VisualizationViewer). The hyperbolic
 * transform is applied in an elliptical lens that affects that part of the visualization.
 *
 * @author Tom Nelson
 */
@Suppress("serial")
class SpatialLensLargeGraphDemo : JPanel() {

    /** the graph */
    val _graph: Network<String, Number>

    val graphLayoutAlgorithm: LayoutAlgorithm<String>

    /** the visual component and renderer for the graph */
    val vv: VisualizationViewer<String, Number>

    /** provides a Hyperbolic lens for the view */
    val hyperbolicViewSupport: LensSupport

    /** provides a magnification lens for the view */
    val magnifyViewSupport: LensSupport

    /** provides a Hyperbolic lens for the model */
    val hyperbolicLayoutSupport: LensSupport

    /** provides a magnification lens for the model */
    val magnifyLayoutSupport: LensSupport

    var scaler: ScalingControl? = null

    init {
        layout = BorderLayout()
        _graph = getGraph()

        graphLayoutAlgorithm = FRBHVisitorLayoutAlgorithm<String>()

        val preferredSize = Dimension(800, 800)
        val viewPreferredSize = Dimension(800, 800)

        val visualizationModel = BaseVisualizationModel(_graph, graphLayoutAlgorithm, preferredSize)
        vv = VisualizationViewer(visualizationModel, viewPreferredSize)
        vv.getRenderContext().setNodeLabelFunction { it.toString() }
        vv.getRenderContext().setNodeShapeFunction { Rectangle2D.Float(-8f, -8f, 16f, 16f) }
        vv.background = Color.white

        vv.getRenderContext().setNodeLabelFunction { it.toString() }

        val gzsp = GraphZoomScrollPane(vv)
        add(gzsp)

        // the regular graph mouse for the normal view
        val graphMouse = DefaultModalGraphMouse<String, Number>()

        vv.setGraphMouse(graphMouse)
        vv.addKeyListener(graphMouse.modeKeyListener!!)

        // create a lens to share between the two hyperbolic transformers
        val layoutModel = vv.getModel().getLayoutModel()
        val d = Dimension(layoutModel.width, layoutModel.height)
        var lens = Lens(d)
        hyperbolicViewSupport = ViewLensSupport<String, Number>(
            vv,
            HyperbolicShapeTransformer(
                lens, vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)
            ),
            ModalLensGraphMouse()
        )
        hyperbolicLayoutSupport = LayoutLensSupport<String, Number>(
            vv,
            HyperbolicTransformer(
                lens,
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)
            ),
            ModalLensGraphMouse()
        )

        // the magnification lens uses a different magnification than the hyperbolic lens
        // create a new one to share between the two magnify transformers
        lens = Lens(d)
        lens.magnification = 3.0f
        magnifyViewSupport = ViewLensSupport<String, Number>(
            vv,
            MagnifyShapeTransformer(
                lens, vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)
            ),
            ModalLensGraphMouse(magnificationPlugin = LensMagnificationGraphMousePlugin(floor = 1.0f, ceiling = 6.0f, delta = 0.2f))
        )
        magnifyLayoutSupport = LayoutLensSupport<String, Number>(
            vv,
            MagnifyTransformer(
                lens,
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)
            ),
            ModalLensGraphMouse(magnificationPlugin = LensMagnificationGraphMousePlugin(floor = 1.0f, ceiling = 6.0f, delta = 0.2f))
        )
        hyperbolicLayoutSupport
            .getLensTransformer()
            .lens
            .lensShape = hyperbolicViewSupport.getLensTransformer().lens.lensShape
        magnifyViewSupport
            .getLensTransformer()
            .lens
            .lensShape = hyperbolicLayoutSupport.getLensTransformer().lens.lensShape
        magnifyLayoutSupport
            .getLensTransformer()
            .lens
            .lensShape = magnifyViewSupport.getLensTransformer().lens.lensShape

        val scaler = CrossoverScalingControl()

        val plus = JButton("+")
        plus.addActionListener { scaler.scale(vv, 1.1f, vv.getCenter()) }

        val minus = JButton("-")
        minus.addActionListener { scaler.scale(vv, 1.0f / 1.1f, vv.getCenter()) }

        val radio = ButtonGroup()
        val normal = JRadioButton("None")
        normal.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                hyperbolicViewSupport.deactivate()
                hyperbolicLayoutSupport.deactivate()
                magnifyViewSupport.deactivate()
                magnifyLayoutSupport.deactivate()
            }
        }

        val hyperView = JRadioButton("Hyperbolic View")
        hyperView.addItemListener { e ->
            hyperbolicViewSupport.activate(e.stateChange == ItemEvent.SELECTED)
        }

        val hyperModel = JRadioButton("Hyperbolic Layout")
        hyperModel.addItemListener { e ->
            hyperbolicLayoutSupport.activate(e.stateChange == ItemEvent.SELECTED)
        }

        val magnifyView = JRadioButton("Magnified View")
        magnifyView.addItemListener { e ->
            magnifyViewSupport.activate(e.stateChange == ItemEvent.SELECTED)
        }

        val magnifyModel = JRadioButton("Magnified Layout")
        magnifyModel.addItemListener { e ->
            magnifyLayoutSupport.activate(e.stateChange == ItemEvent.SELECTED)
        }

        val modeLabel = JLabel("     Mode Menu >>")
        modeLabel.setUI(VerticalLabelUI(false))
        radio.add(normal)
        radio.add(hyperModel)
        radio.add(hyperView)
        radio.add(magnifyModel)
        radio.add(magnifyView)
        normal.isSelected = true

        graphMouse.addItemListener(hyperbolicLayoutSupport.getGraphMouse().getModeListener())
        graphMouse.addItemListener(hyperbolicViewSupport.getGraphMouse().getModeListener())
        graphMouse.addItemListener(magnifyLayoutSupport.getGraphMouse().getModeListener())
        graphMouse.addItemListener(magnifyViewSupport.getGraphMouse().getModeListener())

        val menubar = JMenuBar()
        menubar.add(graphMouse.getModeMenu())
        gzsp.setCorner(menubar)

        val modeBox: JComboBox<*> = graphMouse.getModeComboBox()
        @Suppress("UNCHECKED_CAST")
        modeBox.addItemListener(
            (vv.getGraphMouse() as DefaultModalGraphMouse<Int, Number>).getModeListener()
        )

        val showSpatialEffects = JRadioButton("Spatial Structure")
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

        val controls = Box.createHorizontalBox()
        val zoomControls = JPanel(GridLayout(2, 1))
        val modeControls = JPanel(GridLayout(2, 1))
        val leftControls = JPanel()
        val hyperControls = JPanel(GridLayout(3, 2))
        hyperControls.border = BorderFactory.createTitledBorder("Examiner Lens")
        zoomControls.add(plus)
        zoomControls.add(minus)
        modeControls.add(showSpatialEffects)
        modeControls.add(modeBox)
        leftControls.add(zoomControls)
        leftControls.add(modeControls)

        hyperControls.add(normal)
        hyperControls.add(JLabel())

        hyperControls.add(hyperModel)
        hyperControls.add(magnifyModel)

        hyperControls.add(hyperView)
        hyperControls.add(magnifyView)

        controls.add(leftControls)
        controls.add(hyperControls)
        controls.add(modeLabel)
        add(controls, BorderLayout.SOUTH)
        vv.setNodeToolTipFunction { it.toString() }
    }

    class VerticalLabelUI(private val clockwise: Boolean) : BasicLabelUI() {

        override fun getPreferredSize(c: JComponent): Dimension {
            val dim = super.getPreferredSize(c)
            return Dimension(dim.height, dim.width)
        }

        override fun paint(g: Graphics, c: JComponent) {
            val label = c as JLabel
            val text = label.text
            val icon: Icon? = if (label.isEnabled) label.icon else label.disabledIcon

            if (icon == null && text == null) {
                return
            }

            val fm: FontMetrics = g.fontMetrics
            paintViewInsets = c.getInsets(paintViewInsets)

            paintViewR.x = paintViewInsets.left
            paintViewR.y = paintViewInsets.top

            // Use inverted height & width
            paintViewR.height = c.getWidth() - (paintViewInsets.left + paintViewInsets.right)
            paintViewR.width = c.getHeight() - (paintViewInsets.top + paintViewInsets.bottom)

            paintIconR.x = 0
            paintIconR.y = 0
            paintIconR.width = 0
            paintIconR.height = 0
            paintTextR.x = 0
            paintTextR.y = 0
            paintTextR.width = 0
            paintTextR.height = 0

            val clippedText = layoutCL(label, fm, text, icon, paintViewR, paintIconR, paintTextR)

            val g2 = g as Graphics2D
            val tr = g2.transform
            if (clockwise) {
                g2.rotate(Math.PI / 2)
                g2.translate(0, -c.getWidth())
            } else {
                g2.rotate(-Math.PI / 2)
                g2.translate(-c.getHeight(), 0)
            }

            icon?.paintIcon(c, g, paintIconR.x, paintIconR.y)

            if (text != null) {
                val textX = paintTextR.x
                val textY = paintTextR.y + fm.ascent

                if (label.isEnabled) {
                    paintEnabledText(label, g, clippedText, textX, textY)
                } else {
                    paintDisabledText(label, g, clippedText, textX, textY)
                }
            }

            g2.transform = tr
        }

        companion object {
            init {
                labelUI = VerticalLabelUI(false)
            }

            private val paintIconR = Rectangle()
            private val paintTextR = Rectangle()
            private val paintViewR = Rectangle()
            private var paintViewInsets = Insets(0, 0, 0, 0)
        }
    }

    fun buildOneNode(): Network<String, Number> {
        val graph: MutableNetwork<String, Number> =
            NetworkBuilder.directed().allowsParallelEdges(true).build()
        graph.addNode("A")
        return graph
    }

    companion object {
        private val log = LoggerFactory.getLogger(SpatialLensLargeGraphDemo::class.java)

        private fun createEdge(
            g: MutableNetwork<String, Number>, v1Label: String, v2Label: String, weight: Int
        ) {
            g.addEdge(v1Label, v2Label, Math.random())
        }

        @JvmField
        val pairs = arrayOf(
            arrayOf("a", "b", "3"),
            arrayOf("a", "c", "4"),
            arrayOf("a", "d", "5"),
            arrayOf("d", "c", "6"),
            arrayOf("d", "e", "7"),
            arrayOf("e", "f", "8"),
            arrayOf("f", "g", "9"),
            arrayOf("h", "i", "1"),
            arrayOf("h", "g", "2")
        )

        @JvmStatic
        fun getGraph(): Network<String, Number> {
            val g: MutableNetwork<String, Number> =
                NetworkBuilder.undirected().allowsParallelEdges(true).build()

            for (pair in pairs) {
                createEdge(g, pair[0], pair[1], pair[2].toInt())
            }
            var edge = 10
            for (i in 1..10) {
                for (j in i + 1..10) {
                    val i1 = "c$i"
                    val i2 = "c$j"
                    g.addEdge(i1, i2, edge++)
                }
            }

            for (i in 1..10) {
                for (j in i + 1..10) {
                    val i1 = "d$i"
                    val i2 = "d$j"
                    g.addEdge(i1, i2, edge++)
                }
            }

            // and, last, a partial clique
            for (i in 1..20) {
                for (j in i + 1..20) {
                    if (Math.random() > 0.6) {
                        continue
                    }
                    val i1 = "q$i"
                    val i2 = "q$j"
                    g.addEdge(i1, i2, edge++)
                }
            }

            // and, last, a partial clique
            for (i in 1..20) {
                for (j in i + 1..20) {
                    if (Math.random() > 0.6) {
                        continue
                    }
                    val i1 = "p$i"
                    val i2 = "p$j"
                    g.addEdge(i1, i2, edge++)
                }
            }
            val nodeIt = g.nodes().iterator()
            val current = nodeIt.next()
            while (nodeIt.hasNext()) {
                val next = nodeIt.next()
                g.addEdge(current, next, edge++)
            }
            return g
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val f = JFrame()
            f.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            f.contentPane.add(SpatialLensLargeGraphDemo())
            f.pack()
            f.isVisible = true
        }
    }
}
