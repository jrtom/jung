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
import com.google.common.graph.Network
import com.google.common.graph.NetworkBuilder
import edu.uci.ics.jung.graph.util.TestGraphs
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.StaticLayoutAlgorithm
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.layout.util.RandomLocationTransformer
import edu.uci.ics.jung.visualization.BaseVisualizationModel
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.MultiLayerTransformer
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.LensMagnificationGraphMousePlugin
import edu.uci.ics.jung.visualization.control.ModalLensGraphMouse
import edu.uci.ics.jung.visualization.control.ScalingControl
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction
import edu.uci.ics.jung.visualization.layout.LayoutAlgorithmTransition
import edu.uci.ics.jung.visualization.picking.PickedState
import edu.uci.ics.jung.visualization.transform.HyperbolicTransformer
import edu.uci.ics.jung.visualization.transform.LayoutLensSupport
import edu.uci.ics.jung.visualization.transform.Lens
import edu.uci.ics.jung.visualization.transform.LensSupport
import edu.uci.ics.jung.visualization.transform.MagnifyTransformer
import edu.uci.ics.jung.visualization.transform.shape.HyperbolicShapeTransformer
import edu.uci.ics.jung.visualization.transform.shape.MagnifyShapeTransformer
import edu.uci.ics.jung.visualization.transform.shape.ViewLensSupport
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridLayout
import java.awt.Insets
import java.awt.Rectangle
import java.awt.Shape
import java.awt.event.ItemEvent
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D
import java.util.HashMap
import java.util.function.Function
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.ButtonGroup
import javax.swing.JButton
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
 * applied to either the model (graph layout) or the view (VisualizationViewer) The hyperbolic
 * transform is applied in an elliptical lens that affects that part of the visualization.
 *
 * @author Tom Nelson
 */
class LensDemo : JPanel() {

    /** the graph */
    val graph: Network<String, Number>

    val graphLayoutAlgorithm: FRLayoutAlgorithm<String>

    /** a grid shaped graph */
    val grid: Network<String, Number>

    val gridLayoutAlgorithm: LayoutAlgorithm<String>

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

    /** create an instance of a simple graph with controls to demo the zoom and hyperbolic features. */
    init {
        layout = BorderLayout()
        // create a simple graph for the demo
        graph = TestGraphs.getOneComponentGraph()

        graphLayoutAlgorithm = FRLayoutAlgorithm()
        graphLayoutAlgorithm.setMaxIterations(1000)

        val preferredSize = Dimension(600, 600)
        val map = HashMap<String, Point>()
        val vlf: Function<String, Point> = Function { map[it]!! }
        grid = generateNodeGrid(map, preferredSize, 25)
        gridLayoutAlgorithm = StaticLayoutAlgorithm()

        val visualizationModel: VisualizationModel<String, Number> =
            BaseVisualizationModel(graph, graphLayoutAlgorithm, preferredSize)
        vv = VisualizationViewer(visualizationModel, preferredSize)

        val ps: PickedState<String> = vv.getPickedNodeState()
        val pes: PickedState<Number> = vv.getPickedEdgeState()
        vv.getRenderContext()
            .setNodeFillPaintFunction(PickableNodePaintFunction(ps, Color.red, Color.yellow))
        vv.getRenderContext()
            .setEdgeDrawPaintFunction(PickableEdgePaintFunction(pes, Color.black, Color.cyan))
        vv.background = Color.white

        vv.getRenderContext().setNodeLabelFunction { it.toString() }

        val ovals: Function<in String, Shape> = vv.getRenderContext().getNodeShapeFunction()
        val squares: Function<in String, Shape> = Function { Rectangle2D.Float(-10f, -10f, 20f, 20f) }

        // add a listener for ToolTips
        vv.setNodeToolTipFunction { n -> n }

        val gzsp = GraphZoomScrollPane(vv)
        add(gzsp)

        // the regular graph mouse for the normal view
        val graphMouse = DefaultModalGraphMouse<String, Number>()

        vv.setGraphMouse(graphMouse)
        vv.addKeyListener(graphMouse.modeKeyListener!!)

        // create a lens to share between the two hyperbolic transformers
        val layoutModel: LayoutModel<String> = vv.getModel().getLayoutModel()
        val d = Dimension(layoutModel.width, layoutModel.height)

        var lens = Lens(d)
        hyperbolicViewSupport = ViewLensSupport<String, Number>(
            vv,
            HyperbolicShapeTransformer(
                lens, vv.getRenderContext().getMultiLayerTransformer().getTransformer(MultiLayerTransformer.Layer.VIEW)
            ),
            ModalLensGraphMouse()
        )
        hyperbolicLayoutSupport = LayoutLensSupport<String, Number>(
            vv,
            HyperbolicTransformer(
                lens,
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(MultiLayerTransformer.Layer.LAYOUT)
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
                lens, vv.getRenderContext().getMultiLayerTransformer().getTransformer(MultiLayerTransformer.Layer.VIEW)
            ),
            ModalLensGraphMouse(magnificationPlugin = LensMagnificationGraphMousePlugin(floor = 1.0f, ceiling = 6.0f, delta = 0.2f))
        )
        magnifyLayoutSupport = LayoutLensSupport<String, Number>(
            vv,
            MagnifyTransformer(
                lens,
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(MultiLayerTransformer.Layer.LAYOUT)
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

        val scaler: ScalingControl = CrossoverScalingControl()

        val plus = JButton("+")
        plus.addActionListener { scaler.scale(vv, 1.1f, vv.getCenter()) }

        val minus = JButton("-")
        minus.addActionListener { scaler.scale(vv, 1 / 1.1f, vv.getCenter()) }

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

        val graphRadio = ButtonGroup()
        val graphButton = JRadioButton("Graph")
        graphButton.isSelected = true
        graphButton.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                layoutModel.setInitializer(
                    RandomLocationTransformer(layoutModel.width.toDouble(), layoutModel.height.toDouble())
                )
                visualizationModel.setNetwork(graph, false)
                LayoutAlgorithmTransition.apply(vv, graphLayoutAlgorithm)
                vv.getRenderContext().setNodeShapeFunction(ovals)
                vv.getRenderContext().setNodeLabelFunction { it.toString() }
                vv.repaint()
            }
        }

        val gridButton = JRadioButton("Grid")
        gridButton.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                layoutModel.setInitializer(vlf)
                // so it won't start running the old layout algorithm on the new graph
                visualizationModel.setNetwork(grid, false)
                LayoutAlgorithmTransition.apply(vv, gridLayoutAlgorithm)
                vv.getRenderContext().setNodeShapeFunction(squares)
                vv.getRenderContext().setNodeLabelFunction { "" }
                vv.repaint()
            }
        }

        graphRadio.add(graphButton)
        graphRadio.add(gridButton)

        val modePanel = JPanel(GridLayout(3, 1))
        modePanel.border = BorderFactory.createTitledBorder("Display")
        modePanel.add(graphButton)
        modePanel.add(gridButton)

        val menubar = JMenuBar()
        menubar.add(graphMouse.getModeMenu())
        gzsp.setCorner(menubar)

        val controls = Box.createHorizontalBox()
        val zoomControls = JPanel(GridLayout(2, 1))
        zoomControls.border = BorderFactory.createTitledBorder("Zoom")
        val hyperControls = JPanel(GridLayout(3, 2))
        hyperControls.border = BorderFactory.createTitledBorder("Examiner Lens")
        zoomControls.add(plus)
        zoomControls.add(minus)

        hyperControls.add(normal)
        hyperControls.add(JLabel())

        hyperControls.add(hyperModel)
        hyperControls.add(magnifyModel)

        hyperControls.add(hyperView)
        hyperControls.add(magnifyView)

        controls.add(zoomControls)
        controls.add(hyperControls)
        controls.add(modePanel)
        controls.add(modeLabel)
        add(controls, BorderLayout.SOUTH)
    }

    private fun generateNodeGrid(
        vlf: MutableMap<String, Point>,
        d: Dimension,
        interval: Int
    ): Network<String, Number> {
        val count = d.width / interval * d.height / interval
        val graph: MutableNetwork<String, Number> = NetworkBuilder.directed().build()
        for (i in 0 until count) {
            var x = interval * i
            val y = x / d.width * interval
            x %= d.width

            val location = Point.of(x.toDouble(), y.toDouble())
            val node = "v$i"
            vlf[node] = location
            graph.addNode(node)
        }
        return graph
    }

    class VerticalLabelUI(private val clockwise: Boolean) : BasicLabelUI() {

        companion object {
            init {
                labelUI = VerticalLabelUI(false)
            }

            private val paintIconR = Rectangle()
            private val paintTextR = Rectangle()
            private val paintViewR = Rectangle()
            private var paintViewInsets = Insets(0, 0, 0, 0)
        }

        override fun getPreferredSize(c: JComponent): Dimension {
            val dim = super.getPreferredSize(c)
            return Dimension(dim.height, dim.width)
        }

        override fun paint(g: Graphics, c: JComponent) {
            val label = c as JLabel
            val text = label.text
            val icon = if (label.isEnabled) label.icon else label.disabledIcon

            if (icon == null && text == null) {
                return
            }

            val fm = g.fontMetrics
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

            if (icon != null) {
                icon.paintIcon(c, g, paintIconR.x, paintIconR.y)
            }

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
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val f = JFrame()
            f.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            f.contentPane.add(LensDemo())
            f.pack()
            f.isVisible = true
        }
    }
}
