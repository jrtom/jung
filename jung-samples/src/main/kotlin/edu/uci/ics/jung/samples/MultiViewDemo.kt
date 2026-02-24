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
import edu.uci.ics.jung.visualization.BaseVisualizationModel
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.AnimatedPickingGraphMousePlugin
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.LayoutScalingControl
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin
import edu.uci.ics.jung.visualization.control.RotatingGraphMousePlugin
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin
import edu.uci.ics.jung.visualization.control.ShearingGraphMousePlugin
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin
import edu.uci.ics.jung.visualization.control.ModalGraphMouse
import edu.uci.ics.jung.visualization.control.ViewScalingControl
import edu.uci.ics.jung.visualization.decorators.EdgeShape
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor
import edu.uci.ics.jung.visualization.picking.MultiPickedState
import edu.uci.ics.jung.visualization.picking.PickedState
import edu.uci.ics.jung.visualization.picking.ShapePickSupport
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.GridLayout
import java.awt.event.InputEvent
import java.awt.geom.Rectangle2D
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.WindowConstants

/**
 * Demonstrates 3 views of one graph in one model with one layout. Each view uses a different
 * scaling graph mouse.
 *
 * @author Tom Nelson
 */
class MultiViewDemo : JPanel() {

    /** the graph */
    val graph: Network<String, Number>

    /** the visual components and renderers for the graph */
    val vv1: VisualizationViewer<String, Number>

    val vv2: VisualizationViewer<String, Number>
    val vv3: VisualizationViewer<String, Number>

    val preferredLayoutSize = Dimension(300, 300)

    val messageOne =
        "The mouse wheel will scale the model's layout when activated" +
            " in View 1. Since all three views share the same layout Function, all three views will" +
            " show the same scaling of the layout."

    val messageTwo =
        "The mouse wheel will scale the view when activated in" +
            " View 2. Since all three views share the same view Function, all three views will be affected."

    val messageThree =
        "   The mouse wheel uses a 'crossover' feature in View 3." +
            " When the combined layout and view scale is greater than '1', the model's layout will be scaled." +
            " Since all three views share the same layout Function, all three views will show the same " +
            " scaling of the layout.\n   When the combined scale is less than '1', the scaling function" +
            " crosses over to the view, and then, since all three views share the same view Function," +
            " all three views will show the same scaling."

    val textArea: JTextArea
    val scrollPane: JScrollPane

    /** create an instance of a simple graph in two views with controls to demo the zoom features. */
    init {
        layout = BorderLayout()
        // create a simple graph for the demo
        graph = TestGraphs.getOneComponentGraph()

        // create one layout for the graph
        val layoutAlgorithm = FRLayoutAlgorithm<String>()
        layoutAlgorithm.setMaxIterations(1000)

        // create one model that all 3 views will share
        val visualizationModel: VisualizationModel<String, Number> =
            BaseVisualizationModel(graph, layoutAlgorithm, preferredLayoutSize)

        // create 3 views that share the same model
        vv1 = VisualizationViewer(visualizationModel, preferredLayoutSize)
        vv2 = VisualizationViewer(visualizationModel, preferredLayoutSize)
        vv3 = VisualizationViewer(visualizationModel, preferredLayoutSize)

        vv1.getRenderContext().setEdgeShapeFunction(EdgeShape.line())
        vv2.getRenderContext().setNodeShapeFunction { Rectangle2D.Float(-6f, -6f, 12f, 12f) }

        vv2.getRenderContext().setEdgeShapeFunction(EdgeShape.QuadCurve<String, Number>())

        vv3.getRenderContext().setEdgeShapeFunction(EdgeShape.CubicCurve<String, Number>())

        vv2.getRenderContext()
            .setMultiLayerTransformer(vv1.getRenderContext().getMultiLayerTransformer())
        vv3.getRenderContext()
            .setMultiLayerTransformer(vv1.getRenderContext().getMultiLayerTransformer())

        vv1.getRenderContext().getMultiLayerTransformer().addChangeListener(vv1)
        vv2.getRenderContext().getMultiLayerTransformer().addChangeListener(vv2)
        vv3.getRenderContext().getMultiLayerTransformer().addChangeListener(vv3)

        vv1.background = Color.white
        vv2.background = Color.white
        vv3.background = Color.white

        // create one pick support for all 3 views to share
        val pickSupport: NetworkElementAccessor<String, Number> = ShapePickSupport(vv1)
        vv1.setPickSupport(pickSupport)
        vv2.setPickSupport(pickSupport)
        vv3.setPickSupport(pickSupport)

        // create one picked state for all 3 views to share
        val pes: PickedState<Number> = MultiPickedState()
        val pvs: PickedState<String> = MultiPickedState()
        vv1.setPickedNodeState(pvs)
        vv2.setPickedNodeState(pvs)
        vv3.setPickedNodeState(pvs)
        vv1.setPickedEdgeState(pes)
        vv2.setPickedEdgeState(pes)
        vv3.setPickedEdgeState(pes)

        // set an edge paint function that shows picked edges
        vv1.getRenderContext()
            .setEdgeDrawPaintFunction(PickableEdgePaintFunction(pes, Color.black, Color.red))
        vv2.getRenderContext()
            .setEdgeDrawPaintFunction(PickableEdgePaintFunction(pes, Color.black, Color.red))
        vv3.getRenderContext()
            .setEdgeDrawPaintFunction(PickableEdgePaintFunction(pes, Color.black, Color.red))
        vv1.getRenderContext()
            .setNodeFillPaintFunction(PickableNodePaintFunction(pvs, Color.red, Color.yellow))
        vv2.getRenderContext()
            .setNodeFillPaintFunction(PickableNodePaintFunction(pvs, Color.blue, Color.cyan))
        vv3.getRenderContext()
            .setNodeFillPaintFunction(PickableNodePaintFunction(pvs, Color.red, Color.yellow))

        // add default listener for ToolTips
        vv1.setNodeToolTipFunction { it.toString() }
        vv2.setNodeToolTipFunction { it.toString() }
        vv3.setNodeToolTipFunction { it.toString() }

        val panel = JPanel(GridLayout(1, 0))

        val p1 = JPanel(BorderLayout())
        val p2 = JPanel(BorderLayout())
        val p3 = JPanel(BorderLayout())

        p1.add(GraphZoomScrollPane(vv1))
        p2.add(GraphZoomScrollPane(vv2))
        p3.add(GraphZoomScrollPane(vv3))

        val h1 = JButton("?")
        h1.addActionListener {
            textArea.text = messageOne
            JOptionPane.showMessageDialog(p1, scrollPane, "View 1", JOptionPane.PLAIN_MESSAGE)
        }

        val h2 = JButton("?")
        h2.addActionListener {
            textArea.text = messageTwo
            JOptionPane.showMessageDialog(p2, scrollPane, "View 2", JOptionPane.PLAIN_MESSAGE)
        }

        val h3 = JButton("?")
        h3.addActionListener {
            textArea.text = messageThree
            textArea.caretPosition = 0
            JOptionPane.showMessageDialog(p3, scrollPane, "View 3", JOptionPane.PLAIN_MESSAGE)
        }

        // create a GraphMouse for each view
        // each one has a different scaling plugin
        val gm1 = object : DefaultModalGraphMouse<String, Number>() {
            override fun loadPlugins() {
                pickingPlugin = PickingGraphMousePlugin<String, Number>()
                animatedPickingPlugin = AnimatedPickingGraphMousePlugin<String, Number>()
                translatingPlugin = TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK)
                scalingPlugin = ScalingGraphMousePlugin(LayoutScalingControl(), 0)
                rotatingPlugin = RotatingGraphMousePlugin()
                shearingPlugin = ShearingGraphMousePlugin()

                add(scalingPlugin)
                mode = ModalGraphMouse.Mode.TRANSFORMING
            }
        }

        val gm2 = object : DefaultModalGraphMouse<String, Number>() {
            override fun loadPlugins() {
                pickingPlugin = PickingGraphMousePlugin<String, Number>()
                animatedPickingPlugin = AnimatedPickingGraphMousePlugin<String, Number>()
                translatingPlugin = TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK)
                scalingPlugin = ScalingGraphMousePlugin(ViewScalingControl(), 0)
                rotatingPlugin = RotatingGraphMousePlugin()
                shearingPlugin = ShearingGraphMousePlugin()

                add(scalingPlugin)
                mode = ModalGraphMouse.Mode.TRANSFORMING
            }
        }

        val gm3 = object : DefaultModalGraphMouse<String, Number>() {}

        vv1.setGraphMouse(gm1)
        vv2.setGraphMouse(gm2)
        vv3.setGraphMouse(gm3)

        vv1.toolTipText = "<html><center>MouseWheel Scales Layout</center></html>"
        vv2.toolTipText = "<html><center>MouseWheel Scales View</center></html>"
        vv3.toolTipText =
            "<html><center>MouseWheel Scales Layout and<p>crosses over to view<p>ctrl+MouseWheel scales view</center></html>"

        vv1.addPostRenderPaintable(BannerLabel(vv1, "View 1"))
        vv2.addPostRenderPaintable(BannerLabel(vv2, "View 2"))
        vv3.addPostRenderPaintable(BannerLabel(vv3, "View 3"))

        textArea = JTextArea(6, 30)
        scrollPane = JScrollPane(
            textArea,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        )
        textArea.lineWrap = true
        textArea.wrapStyleWord = true
        textArea.isEditable = false

        var flow = JPanel()
        flow.add(h1)
        flow.add(gm1.getModeComboBox())
        p1.add(flow, BorderLayout.SOUTH)
        flow = JPanel()
        flow.add(h2)
        flow.add(gm2.getModeComboBox())
        p2.add(flow, BorderLayout.SOUTH)
        flow = JPanel()
        flow.add(h3)
        flow.add(gm3.getModeComboBox())
        p3.add(flow, BorderLayout.SOUTH)

        panel.add(p1)
        panel.add(p2)
        panel.add(p3)
        add(panel)
    }

    inner class BannerLabel(
        private val vv: VisualizationViewer<String, Number>,
        private val str: String
    ) : VisualizationServer.Paintable {

        var x = 0
        var y = 0
        var font: Font? = null
        var metrics: FontMetrics? = null
        var swidth = 0
        var sheight = 0

        override fun paint(g: Graphics) {
            val d = vv.size
            if (font == null) {
                font = Font(g.font.name, Font.BOLD, 30)
                metrics = g.getFontMetrics(font)
                swidth = metrics!!.stringWidth(str)
                sheight = metrics!!.maxAscent + metrics!!.maxDescent
                x = (3 * d.width / 2 - swidth) / 2
                y = d.height - sheight
            }
            g.font = font
            val oldColor = g.color
            g.color = Color.gray
            g.drawString(str, x, y)
            g.color = oldColor
        }

        override fun useTransform(): Boolean = false
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val f = JFrame()
            f.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            f.contentPane.add(MultiViewDemo())
            f.pack()
            f.isVisible = true
        }
    }
}
