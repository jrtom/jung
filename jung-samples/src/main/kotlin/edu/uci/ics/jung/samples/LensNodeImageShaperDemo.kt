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
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.LayeredIcon
import edu.uci.ics.jung.visualization.MultiLayerTransformer
import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.LensMagnificationGraphMousePlugin
import edu.uci.ics.jung.visualization.control.ModalGraphMouse
import edu.uci.ics.jung.visualization.control.ModalLensGraphMouse
import edu.uci.ics.jung.visualization.control.ScalingControl
import edu.uci.ics.jung.visualization.decorators.EllipseNodeShapeFunction
import edu.uci.ics.jung.visualization.decorators.NodeIconShapeFunction
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction
import edu.uci.ics.jung.visualization.picking.PickedState
import edu.uci.ics.jung.visualization.renderers.Checkmark
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer
import edu.uci.ics.jung.visualization.renderers.DefaultNodeLabelRenderer
import edu.uci.ics.jung.visualization.transform.LayoutLensSupport
import edu.uci.ics.jung.visualization.transform.Lens
import edu.uci.ics.jung.visualization.transform.LensSupport
import edu.uci.ics.jung.visualization.transform.MagnifyTransformer
import edu.uci.ics.jung.visualization.transform.shape.MagnifyImageLensSupport
import edu.uci.ics.jung.visualization.transform.shape.MagnifyShapeTransformer
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Container
import java.awt.Dimension
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.GridLayout
import java.awt.Paint
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.util.function.Function
import javax.swing.BorderFactory
import javax.swing.ButtonGroup
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.WindowConstants

/**
 * Demonstrates the use of images to represent graph nodes. The images are added to the
 * DefaultGraphLabelRenderer and can either be offset from the node, or centered on the node.
 * Additionally, the relative positioning of the label and image is controlled by subclassing the
 * DefaultGraphLabelRenderer and setting the appropriate properties on its JLabel superclass
 * FancyGraphLabelRenderer
 *
 * The images used in this demo (courtesy of slashdot.org) are rectangular but with a transparent
 * background. When nodes are represented by these images, it looks better if the actual shape of
 * the opaque part of the image is computed so that the edge arrowheads follow the visual shape of
 * the image. This demo uses the FourPassImageShaper class to compute the Shape from an image with
 * transparent background.
 *
 * @author Tom Nelson
 */
class LensNodeImageShaperDemo : JPanel() {

    /** the graph */
    val graph: Network<Number, Number>

    /** the visual component and renderer for the graph */
    val vv: VisualizationViewer<Number, Number>

    /** some icon names to use */
    val iconNames = arrayOf(
        "apple", "os", "x", "linux", "inputdevices", "wireless",
        "graphics3", "gamespcgames", "humor", "music", "privacy"
    )

    val magnifyLayoutSupport: LensSupport
    val magnifyViewSupport: LensSupport

    /** create an instance of a simple graph with controls to demo the zoom features. */
    init {
        layout = BorderLayout()
        // create a simple graph for the demo
        graph = createGraph()

        // Maps for the labels and icons
        val map = HashMap<Number, String>()
        val iconMap = HashMap<Number, Icon>()
        for (node in graph.nodes()) {
            val i = node.toInt()
            map[node] = iconNames[i % iconNames.size]

            val name = "/images/topic${iconNames[i]}.gif"
            try {
                val icon: Icon =
                    LayeredIcon(ImageIcon(LensNodeImageShaperDemo::class.java.getResource(name)).image)
                iconMap[node] = icon
            } catch (ex: Exception) {
                System.err.println("You need slashdoticons.jar in your classpath to see the image $name")
            }
        }

        val layoutAlgorithm = FRLayoutAlgorithm<Number>()
        layoutAlgorithm.setMaxIterations(100)
        vv = VisualizationViewer(graph, layoutAlgorithm, Dimension(600, 600))

        val vpf: Function<Number, Paint> =
            PickableNodePaintFunction(vv.getPickedNodeState(), Color.white, Color.yellow)
        vv.getRenderContext().setNodeFillPaintFunction(vpf)
        vv.getRenderContext()
            .setEdgeDrawPaintFunction(
                PickableEdgePaintFunction(vv.getPickedEdgeState(), Color.black, Color.cyan)
            )

        vv.background = Color.white

        vv.getRenderContext().setNodeLabelFunction(Function { map[it] ?: "" })
        vv.getRenderContext().setNodeLabelRenderer(DefaultNodeLabelRenderer(Color.cyan))
        vv.getRenderContext().setEdgeLabelRenderer(DefaultEdgeLabelRenderer(Color.cyan))

        val nodeImageShapeFunction =
            NodeIconShapeFunction<Number>(EllipseNodeShapeFunction())

        val nodeIconFunction: Function<Number, Icon> = Function { iconMap[it]!! }

        nodeImageShapeFunction.iconMap = iconMap

        vv.getRenderContext().setNodeShapeFunction(nodeImageShapeFunction)
        vv.getRenderContext().setNodeIconFunction(nodeIconFunction)

        // Get the pickedState and add a listener that will decorate the
        // Node images with a checkmark icon when they are picked
        val ps: PickedState<Number> = vv.getPickedNodeState()
        ps.addItemListener(PickWithIconListener(nodeIconFunction))

        vv.addPostRenderPaintable(object : VisualizationServer.Paintable {
            var x = 0
            var y = 0
            var font: Font? = null
            var metrics: FontMetrics? = null
            var swidth = 0
            var sheight = 0
            val str = "Thank You, slashdot.org, for the images!"

            override fun paint(g: Graphics) {
                val d = vv.size
                if (font == null) {
                    font = Font(g.font.name, Font.BOLD, 20)
                    metrics = g.getFontMetrics(font)
                    swidth = metrics!!.stringWidth(str)
                    sheight = metrics!!.maxAscent + metrics!!.maxDescent
                    x = (d.width - swidth) / 2
                    y = (d.height - sheight * 1.5).toInt()
                }
                g.font = font
                val oldColor = g.color
                g.color = Color.lightGray
                g.drawString(str, x, y)
                g.color = oldColor
            }

            override fun useTransform(): Boolean = false
        })

        // add a listener for ToolTips
        vv.setNodeToolTipFunction { it.toString() }

        val panel = GraphZoomScrollPane(vv)
        add(panel)

        val graphMouse = DefaultModalGraphMouse<Number, Number>()
        vv.setGraphMouse(graphMouse)

        val scaler: ScalingControl = CrossoverScalingControl()

        val plus = JButton("+")
        plus.addActionListener { scaler.scale(vv, 1.1f, vv.getCenter()) }

        val minus = JButton("-")
        minus.addActionListener { scaler.scale(vv, 1 / 1.1f, vv.getCenter()) }

        val modeBox: JComboBox<ModalGraphMouse.Mode> = graphMouse.getModeComboBox()
        val modePanel = JPanel()
        modePanel.border = BorderFactory.createTitledBorder("Mouse Mode")
        modePanel.add(modeBox)

        val scaleGrid = JPanel(GridLayout(1, 0))
        scaleGrid.border = BorderFactory.createTitledBorder("Zoom")
        val controls = JPanel()
        scaleGrid.add(plus)
        scaleGrid.add(minus)
        controls.add(scaleGrid)

        controls.add(modePanel)
        add(controls, BorderLayout.SOUTH)

        val layoutModel: LayoutModel<Number> = vv.getModel().getLayoutModel()
        val d = Dimension(layoutModel.width, layoutModel.height)

        val lens = Lens(d)
        lens.magnification = 2.0f
        magnifyViewSupport = MagnifyImageLensSupport<Number, Number>(
            vv,
            MagnifyShapeTransformer(
                lens, vv.getRenderContext().getMultiLayerTransformer().getTransformer(MultiLayerTransformer.Layer.VIEW)
            ),
            ModalLensGraphMouse(magnificationPlugin = LensMagnificationGraphMousePlugin(floor = 1.0f, ceiling = 6.0f, delta = 0.2f))
        )
        magnifyLayoutSupport = LayoutLensSupport<Number, Number>(
            vv,
            MagnifyTransformer(
                lens,
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(MultiLayerTransformer.Layer.LAYOUT)
            ),
            ModalLensGraphMouse(magnificationPlugin = LensMagnificationGraphMousePlugin(floor = 1.0f, ceiling = 6.0f, delta = 0.2f))
        )

        graphMouse.addItemListener(magnifyLayoutSupport.getGraphMouse().getModeListener())
        graphMouse.addItemListener(magnifyViewSupport.getGraphMouse().getModeListener())

        val radio = ButtonGroup()
        val none = JRadioButton("None")
        none.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                magnifyViewSupport.deactivate()
                magnifyLayoutSupport.deactivate()
            }
        }

        val magnifyView = JRadioButton("Magnified View")
        magnifyView.addItemListener { e ->
            magnifyViewSupport.activate(e.stateChange == ItemEvent.SELECTED)
        }

        val magnifyModel = JRadioButton("Magnified Layout")
        magnifyModel.addItemListener { e ->
            magnifyLayoutSupport.activate(e.stateChange == ItemEvent.SELECTED)
        }

        radio.add(none)
        radio.add(magnifyView)
        radio.add(magnifyModel)

        val menubar = JMenuBar()
        val modeMenu: JMenu = graphMouse.getModeMenu()
        menubar.add(modeMenu)

        val lensPanel = JPanel(GridLayout(2, 0))
        lensPanel.border = BorderFactory.createTitledBorder("Lens")
        lensPanel.add(none)
        lensPanel.add(magnifyView)
        lensPanel.add(magnifyModel)
        controls.add(lensPanel)
    }

    fun createGraph(): Network<Number, Number> {
        val graph: MutableNetwork<Number, Number> = NetworkBuilder.directed().build()
        graph.addEdge(0, 1, Math.random())
        graph.addEdge(3, 0, Math.random())
        graph.addEdge(0, 4, Math.random())
        graph.addEdge(4, 5, Math.random())
        graph.addEdge(5, 3, Math.random())
        graph.addEdge(2, 1, Math.random())
        graph.addEdge(4, 1, Math.random())
        graph.addEdge(8, 2, Math.random())
        graph.addEdge(3, 8, Math.random())
        graph.addEdge(6, 7, Math.random())
        graph.addEdge(7, 5, Math.random())
        graph.addEdge(0, 9, Math.random())
        graph.addEdge(9, 8, Math.random())
        graph.addEdge(7, 6, Math.random())
        graph.addEdge(6, 5, Math.random())
        graph.addEdge(4, 2, Math.random())
        graph.addEdge(5, 4, Math.random())
        graph.addEdge(4, 10, Math.random())
        graph.addEdge(10, 4, Math.random())

        return graph
    }

    class PickWithIconListener(val imager: Function<Number, Icon>) : ItemListener {
        val checked: Icon = Checkmark(Color.red)

        override fun itemStateChanged(e: ItemEvent) {
            val icon = imager.apply(e.item as Number)
            if (icon != null && icon is LayeredIcon) {
                if (e.stateChange == ItemEvent.SELECTED) {
                    icon.add(checked)
                } else {
                    icon.remove(checked)
                }
            }
        }
    }

    companion object {
        private const val serialVersionUID = 5432239991020505763L

        @JvmStatic
        fun main(args: Array<String>) {
            val frame = JFrame()
            val content: Container = frame.contentPane
            frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE

            content.add(LensNodeImageShaperDemo())
            frame.pack()
            frame.isVisible = true
        }
    }
}
