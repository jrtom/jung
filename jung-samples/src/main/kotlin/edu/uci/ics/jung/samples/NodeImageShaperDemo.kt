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
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.layout.util.RandomLocationTransformer
import edu.uci.ics.jung.visualization.BaseVisualizationModel
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.LayeredIcon
import edu.uci.ics.jung.visualization.MultiLayerTransformer
import edu.uci.ics.jung.visualization.RenderContext
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.ScalingControl
import edu.uci.ics.jung.visualization.decorators.EllipseNodeShapeFunction
import edu.uci.ics.jung.visualization.decorators.NodeIconShapeFunction
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction
import edu.uci.ics.jung.visualization.picking.PickedState
import edu.uci.ics.jung.visualization.renderers.BasicNodeRenderer
import edu.uci.ics.jung.visualization.renderers.Checkmark
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer
import edu.uci.ics.jung.visualization.renderers.DefaultNodeLabelRenderer
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator
import edu.uci.ics.jung.visualization.util.ImageShapeUtils
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Container
import java.awt.Dimension
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.GridLayout
import java.awt.Image
import java.awt.Paint
import java.awt.Shape
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.util.HashMap
import java.util.function.Function
import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

/**
 * Demonstrates the use of images to represent graph nodes. The images are supplied via the
 * NodeShapeFunction so that both the image and its shape can be utilized.
 *
 * The images used in this demo (courtesy of slashdot.org) are rectangular but with a transparent
 * background. When nodes are represented by these images, it looks better if the actual shape of
 * the opaque part of the image is computed so that the edge arrowheads follow the visual shape of
 * the image. This demo uses the FourPassImageShaper class to compute the Shape from an image with
 * transparent background.
 *
 * @author Tom Nelson
 */
class NodeImageShaperDemo : JPanel() {

    /** the graph */
    val graph: Network<Number, Number>

    /** the visual component and renderer for the graph */
    val vv: VisualizationViewer<Number, Number>

    /** some icon names to use */
    val iconNames = arrayOf(
        "apple", "os", "x", "linux", "inputdevices", "wireless",
        "graphics3", "gamespcgames", "humor", "music", "privacy"
    )

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
                    LayeredIcon(ImageIcon(NodeImageShaperDemo::class.java.getResource(name)).image)
                iconMap[node] = icon
            } catch (ex: Exception) {
                System.err.println("You need slashdoticons.jar in your classpath to see the image $name")
            }
        }

        val layoutAlgorithm = FRLayoutAlgorithm<Number>()
        layoutAlgorithm.setMaxIterations(100)

        vv = VisualizationViewer(
            BaseVisualizationModel(
                graph,
                layoutAlgorithm,
                RandomLocationTransformer(400.0, 400.0, 0L),
                Dimension(400, 400)
            ),
            Dimension(400, 400)
        )

        // This demo uses a special renderer to turn outlines on and off.
        // you do not need to do this in a real application.
        // Instead, just let vv use the Renderer it already has
        vv.getRenderer().setNodeRenderer(DemoRenderer())

        val vpf: Function<Number, Paint> =
            PickableNodePaintFunction(vv.getPickedNodeState(), Color.white, Color.yellow)
        vv.getRenderContext().setNodeFillPaintFunction(vpf)
        vv.getRenderContext()
            .setEdgeDrawPaintFunction(
                PickableEdgePaintFunction(vv.getPickedEdgeState(), Color.black, Color.cyan)
            )

        vv.background = Color.white

        val nodeStringerImpl: Function<Number, String> = NodeStringerImpl(map)
        vv.getRenderContext().setNodeLabelFunction(nodeStringerImpl)
        vv.getRenderContext().setNodeLabelRenderer(DefaultNodeLabelRenderer(Color.cyan))
        vv.getRenderContext().setEdgeLabelRenderer(DefaultEdgeLabelRenderer(Color.cyan))

        // For this demo only, I use a special class that lets me turn various
        // features on and off. For a real application, use NodeIconShapeTransformer instead.
        val nodeIconShapeTransformer = DemoNodeIconShapeFunction<Number>(EllipseNodeShapeFunction())
        nodeIconShapeTransformer.iconMap = iconMap

        val nodeIconTransformer = DemoNodeIconTransformer(iconMap)

        vv.getRenderContext().setNodeShapeFunction(nodeIconShapeTransformer)
        @Suppress("UNCHECKED_CAST")
        vv.getRenderContext().setNodeIconFunction(nodeIconTransformer as Function<Number, Icon>)

        // Get the pickedState and add a listener that will decorate the
        // Node images with a checkmark icon when they are picked
        val ps: PickedState<Number> = vv.getPickedNodeState()
        @Suppress("UNCHECKED_CAST")
        ps.addItemListener(PickWithIconListener<Number>(nodeIconTransformer as Function<Number, Icon>))

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
        vv.addKeyListener(graphMouse.modeKeyListener!!)
        val scaler: ScalingControl = CrossoverScalingControl()

        val plus = JButton("+")
        plus.addActionListener { scaler.scale(vv, 1.1f, vv.getCenter()) }

        val minus = JButton("-")
        minus.addActionListener { scaler.scale(vv, 1 / 1.1f, vv.getCenter()) }

        val shape = JCheckBox("Shape")
        shape.addItemListener { e ->
            nodeIconShapeTransformer.setShapeImages(e.stateChange == ItemEvent.SELECTED)
            vv.repaint()
        }

        shape.isSelected = true

        val fill = JCheckBox("Fill")
        fill.addItemListener { e ->
            nodeIconTransformer.fillImages = e.stateChange == ItemEvent.SELECTED
            vv.repaint()
        }

        fill.isSelected = true

        val drawOutlines = JCheckBox("Outline")
        drawOutlines.addItemListener { e ->
            nodeIconTransformer.outlineImages = e.stateChange == ItemEvent.SELECTED
            vv.repaint()
        }

        val modeBox = graphMouse.getModeComboBox()
        val modePanel = JPanel()
        modePanel.border = BorderFactory.createTitledBorder("Mouse Mode")
        modePanel.add(modeBox)

        val scaleGrid = JPanel(GridLayout(1, 0))
        scaleGrid.border = BorderFactory.createTitledBorder("Zoom")
        val labelFeatures = JPanel(GridLayout(1, 0))
        labelFeatures.border = BorderFactory.createTitledBorder("Image Effects")
        val controls = JPanel()
        scaleGrid.add(plus)
        scaleGrid.add(minus)
        controls.add(scaleGrid)
        labelFeatures.add(shape)
        labelFeatures.add(fill)
        labelFeatures.add(drawOutlines)

        controls.add(labelFeatures)
        controls.add(modePanel)
        add(controls, BorderLayout.SOUTH)
    }

    /**
     * When Nodes are picked, add a checkmark icon to the imager. Remove the icon when a Node is
     * unpicked
     *
     * @author Tom Nelson
     */
    class PickWithIconListener<N>(val imager: Function<N, Icon>) : ItemListener {
        val checked: Icon = Checkmark()

        override fun itemStateChanged(e: ItemEvent) {
            @Suppress("UNCHECKED_CAST")
            val icon = imager.apply(e.item as N)
            if (icon != null && icon is LayeredIcon) {
                if (e.stateChange == ItemEvent.SELECTED) {
                    icon.add(checked)
                } else {
                    icon.remove(checked)
                }
            }
        }
    }

    /**
     * A simple implementation of Function that gets Node labels from a Map
     *
     * @author Tom Nelson
     */
    class NodeStringerImpl<N>(var map: Map<N, String>) : Function<N, String> {

        private var _enabled = true

        override fun apply(v: N): String {
            return if (isEnabled()) {
                map[v] ?: ""
            } else {
                ""
            }
        }

        fun isEnabled(): Boolean = _enabled

        fun setEnabled(enabled: Boolean) {
            this._enabled = enabled
        }
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

    /**
     * This class exists only to provide settings to turn on/off shapes and image fill in this demo.
     *
     * For a real application, just use `Functions.forMap(iconMap)` to provide a
     * `Function<N, Icon>`.
     */
    class DemoNodeIconTransformer<N>(var iconMap: Map<N, Icon> = HashMap()) : Function<N, Icon?> {
        var fillImages = true
        var outlineImages = false

        override fun apply(v: N): Icon? {
            return if (fillImages) {
                iconMap[v]
            } else {
                null
            }
        }
    }

    /**
     * this class exists only to provide settings to turn on/off shapes and image fill in this demo.
     * In a real application, use NodeIconShapeTransformer instead.
     */
    class DemoNodeIconShapeFunction<N>(delegate: Function<N, Shape>) : NodeIconShapeFunction<N>(delegate) {

        private var _shapeImages = true

        fun isShapeImages(): Boolean = _shapeImages

        fun setShapeImages(shapeImages: Boolean) {
            shapeMap.clear()
            this._shapeImages = shapeImages
        }

        override fun apply(v: N): Shape {
            val icon = iconMap?.get(v)

            if (icon != null && icon is ImageIcon) {
                val image = icon.image

                var shape = shapeMap[image]
                if (shape == null) {
                    shape = if (_shapeImages) {
                        ImageShapeUtils.getShape(image, 30)
                    } else {
                        Rectangle2D.Float(0f, 0f, image.getWidth(null).toFloat(), image.getHeight(null).toFloat())
                    }
                    if (shape.bounds.getWidth() > 0 && shape.bounds.getHeight() > 0) {
                        val width = image.getWidth(null)
                        val height = image.getHeight(null)
                        val transform = AffineTransform.getTranslateInstance(
                            (-width / 2).toDouble(), (-height / 2).toDouble()
                        )
                        shape = transform.createTransformedShape(shape)
                        shapeMap[image] = shape
                    }
                }
                return shape
            } else {
                return delegate.apply(v)
            }
        }
    }

    /**
     * a special renderer that can turn outlines on and off in this demo. You won't need this for a
     * real application. Use BasicNodeRenderer instead
     *
     * @author Tom Nelson
     */
    inner class DemoRenderer<N : Any, E : Any> : BasicNodeRenderer<N, E>() {

        override fun paintIconForNode(
            renderContext: RenderContext<N, E>,
            model: VisualizationModel<N, E>,
            v: N
        ) {
            val p: Point = model.getLayoutModel().apply(v)
            val p2d: Point2D = renderContext
                .getMultiLayerTransformer()
                .transform(MultiLayerTransformer.Layer.LAYOUT, Point2D.Double(p.x, p.y))!!
            val x = p2d.x.toFloat()
            val y = p2d.y.toFloat()

            val g: GraphicsDecorator = renderContext.getGraphicsContext()!!
            var outlineImages = false
            val nodeIconFunction = renderContext.getNodeIconFunction()

            if (nodeIconFunction is DemoNodeIconTransformer<*>) {
                outlineImages = (nodeIconFunction as DemoNodeIconTransformer<N>).outlineImages
            }
            val icon = nodeIconFunction.apply(v)
            if (icon == null || outlineImages) {
                val s = AffineTransform.getTranslateInstance(x.toDouble(), y.toDouble())
                    .createTransformedShape(renderContext.getNodeShapeFunction().apply(v))
                paintShapeForNode(renderContext, model, v, s)
            }
            if (icon != null) {
                val xLoc = (x - icon.iconWidth / 2).toInt()
                val yLoc = (y - icon.iconHeight / 2).toInt()
                icon.paintIcon(renderContext.getScreenDevice(), g.getDelegate(), xLoc, yLoc)
            }
        }
    }

    companion object {
        private const val serialVersionUID = -4332663871914930864L

        @JvmStatic
        fun main(args: Array<String>) {
            val frame = JFrame()
            val content: Container = frame.contentPane
            frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE

            content.add(NodeImageShaperDemo())
            frame.pack()
            frame.isVisible = true
        }
    }
}
