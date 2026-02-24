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
import edu.uci.ics.jung.graph.CTreeNetwork
import edu.uci.ics.jung.graph.MutableCTreeNetwork
import edu.uci.ics.jung.graph.TreeNetworkBuilder
import edu.uci.ics.jung.layout.algorithms.BalloonLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.RadialTreeLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.TreeLayoutAlgorithm
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.layout.model.PolarPoint
import edu.uci.ics.jung.samples.util.ControlHelpers
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer
import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.ModalGraphMouse
import edu.uci.ics.jung.visualization.decorators.EdgeShape
import edu.uci.ics.jung.visualization.decorators.EllipseNodeShapeFunction
import edu.uci.ics.jung.visualization.layout.LayoutAlgorithmTransition
import edu.uci.ics.jung.visualization.subLayout.TreeCollapser
import edu.uci.ics.jung.visualization.transform.MutableTransformer
import edu.uci.ics.jung.visualization.transform.MutableTransformerDecorator
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Container
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.event.ItemEvent
import java.awt.geom.AffineTransform
import java.awt.geom.Ellipse2D
import java.util.function.Function
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

/**
 * Demonstrates "collapsing"/"expanding" of a tree's subtrees.
 *
 * @author Tom Nelson
 */
@Suppress("serial")
class TreeCollapseDemo : JPanel() {

    enum class Layouts {
        TREE,
        RADIAL,
        BALLOON
    }

    /** the original graph */
    var graph: MutableCTreeNetwork<Any, Any>

    /** the visual component and renderer for the graph */
    val vv: VisualizationViewer<Any, Any>

    var rings: VisualizationServer.Paintable? = null

    var balloonRings: VisualizationServer.Paintable? = null

    val layoutAlgorithm: TreeLayoutAlgorithm<Any>

    val radialLayoutAlgorithm: RadialTreeLayoutAlgorithm<Any>

    val balloonLayoutAlgorithm: BalloonLayoutAlgorithm<Any>

    init {
        layout = BorderLayout()
        // create a simple graph for the demo
        graph = createTree()

        layoutAlgorithm = TreeLayoutAlgorithm()

        radialLayoutAlgorithm = RadialTreeLayoutAlgorithm()

        balloonLayoutAlgorithm = BalloonLayoutAlgorithm()

        vv = VisualizationViewer(graph, layoutAlgorithm, Dimension(600, 600))
        vv.background = Color.white
        vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line())
        vv.getRenderContext().setNodeLabelFunction { it.toString() }
        vv.getRenderContext().setNodeShapeFunction(ClusterNodeShapeFunction<Any>())
        // add a listener for ToolTips
        vv.setNodeToolTipFunction { it.toString() }
        vv.getRenderContext().setArrowFillPaintFunction { Color.lightGray }

        val panel = GraphZoomScrollPane(vv)
        add(panel)

        val graphMouse = DefaultModalGraphMouse<String, Int>()

        vv.setGraphMouse(graphMouse)

        val modeBox = graphMouse.getModeComboBox()
        modeBox.addItemListener(graphMouse.getModeListener())
        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING)

        val layoutComboBox = JComboBox(Layouts.values())
        layoutComboBox.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                when (e.item) {
                    Layouts.RADIAL -> {
                        LayoutAlgorithmTransition.animate(vv, radialLayoutAlgorithm)

                        balloonRings?.let { vv.removePreRenderPaintable(it) }
                        vv.getRenderContext().getMultiLayerTransformer().setToIdentity()
                        if (rings == null) {
                            rings = Rings(vv.getModel().getLayoutModel())
                        }
                        vv.addPreRenderPaintable(rings!!)
                    }
                    Layouts.BALLOON -> {
                        LayoutAlgorithmTransition.animate(vv, balloonLayoutAlgorithm)

                        rings?.let { vv.removePreRenderPaintable(it) }
                        vv.getRenderContext().getMultiLayerTransformer().setToIdentity()
                        if (balloonRings == null) {
                            balloonRings = BalloonRings(balloonLayoutAlgorithm)
                        }

                        vv.addPreRenderPaintable(balloonRings!!)
                    }
                    else -> {
                        LayoutAlgorithmTransition.animate(vv, layoutAlgorithm)

                        rings?.let { vv.removePreRenderPaintable(it) }
                        balloonRings?.let { vv.removePreRenderPaintable(it) }
                        vv.getRenderContext().getMultiLayerTransformer().setToIdentity()
                    }
                }
                vv.repaint()
            }
        }

        val collapse = JButton("Collapse")
        collapse.addActionListener {
            val picked: Set<Any> = vv.getPickedNodeState().getPicked()
            if (picked.size == 1) {
                val root = picked.first()
                val subTree: CTreeNetwork<*, *> = TreeCollapser.collapse(graph, root)
                val objectLayoutModel = vv.getModel().getLayoutModel()
                objectLayoutModel.set(subTree, objectLayoutModel.apply(root))
                vv.getModel().setNetwork(graph, true)
                vv.getPickedNodeState().clear()
                vv.repaint()
            }
        }

        val expand = JButton("Expand")
        expand.addActionListener {
            for (v in vv.getPickedNodeState().getPicked().toSet()) {
                if (v is MutableCTreeNetwork<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    graph = TreeCollapser.expand(graph, v as MutableCTreeNetwork<Any, Any>) as MutableCTreeNetwork<Any, Any>
                    val objectLayoutModel = vv.getModel().getLayoutModel()
                    objectLayoutModel.set(graph, objectLayoutModel.apply(v))
                    vv.getModel().setNetwork(graph, true)
                }
                vv.getPickedNodeState().clear()
                vv.repaint()
            }
        }

        val controls = JPanel()
        controls.add(layoutComboBox)
        controls.add(ControlHelpers.getZoomControls(vv, "Zoom"))
        controls.add(modeBox)
        controls.add(collapse)
        controls.add(expand)
        add(controls, BorderLayout.SOUTH)
    }

    inner class Rings(val layoutModel: LayoutModel<Any>) : VisualizationServer.Paintable {

        val depths: Collection<Double> = getDepths()

        private fun getDepths(): Set<Double> {
            val depths = HashSet<Double>()
            val polarLocations: Map<Any, PolarPoint> = radialLayoutAlgorithm.polarLocations
            for (v in graph.nodes().toSet()) {
                val pp = polarLocations[v]
                depths.add(pp!!.radius)
            }
            return depths
        }

        override fun paint(g: Graphics) {
            g.color = Color.lightGray

            val g2d = g as Graphics2D
            val center: Point = radialLayoutAlgorithm.getCenter(layoutModel)

            val ellipse = Ellipse2D.Double()
            for (d in depths) {
                ellipse.setFrameFromDiagonal(
                    center.x - d, center.y - d, center.x + d, center.y + d
                )
                val shape: Shape = vv.getRenderContext()
                    .getMultiLayerTransformer()
                    .getTransformer(Layer.LAYOUT)
                    .transform(ellipse)
                g2d.draw(shape)
            }
        }

        override fun useTransform(): Boolean = true
    }

    inner class BalloonRings(
        private val layoutAlgorithm: BalloonLayoutAlgorithm<*>
    ) : VisualizationServer.Paintable {

        override fun paint(g: Graphics) {
            g.color = Color.gray

            val g2d = g as Graphics2D

            val ellipse = Ellipse2D.Double()
            for (v in vv.getModel().getNetwork().nodes().toSet()) {
                val radius = layoutAlgorithm.radii[v] ?: continue
                val p: Point = vv.getModel().getLayoutModel().apply(v)
                ellipse.setFrame(-radius, -radius, 2 * radius, 2 * radius)
                val at = AffineTransform.getTranslateInstance(p.x, p.y)
                var shape: Shape = at.createTransformedShape(ellipse)

                val viewTransformer: MutableTransformer =
                    vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)

                shape = if (viewTransformer is MutableTransformerDecorator) {
                    vv.getRenderContext().getMultiLayerTransformer().transform(shape)!!
                } else {
                    vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, shape)!!
                }

                g2d.draw(shape)
            }
        }

        override fun useTransform(): Boolean = true
    }

    private fun createTree(): MutableCTreeNetwork<Any, Any> {
        val tree: MutableCTreeNetwork<Any, Any> =
            TreeNetworkBuilder.builder().expectedNodeCount(27).build()

        tree.addNode("root")

        var edgeId = 0
        tree.addEdge("root", "V0", edgeId++)
        tree.addEdge("V0", "V1", edgeId++)
        tree.addEdge("V0", "V2", edgeId++)
        tree.addEdge("V1", "V4", edgeId++)
        tree.addEdge("V2", "V3", edgeId++)
        tree.addEdge("V2", "V5", edgeId++)
        tree.addEdge("V4", "V6", edgeId++)
        tree.addEdge("V4", "V7", edgeId++)
        tree.addEdge("V3", "V8", edgeId++)
        tree.addEdge("V6", "V9", edgeId++)
        tree.addEdge("V4", "V10", edgeId++)

        tree.addEdge("root", "A0", edgeId++)
        tree.addEdge("A0", "A1", edgeId++)
        tree.addEdge("A0", "A2", edgeId++)
        tree.addEdge("A0", "A3", edgeId++)

        tree.addEdge("root", "B0", edgeId++)
        tree.addEdge("B0", "B1", edgeId++)
        tree.addEdge("B0", "B2", edgeId++)
        tree.addEdge("B1", "B4", edgeId++)
        tree.addEdge("B2", "B3", edgeId++)
        tree.addEdge("B2", "B5", edgeId++)
        tree.addEdge("B4", "B6", edgeId++)
        tree.addEdge("B4", "B7", edgeId++)
        tree.addEdge("B3", "B8", edgeId++)
        tree.addEdge("B6", "B9", edgeId++)

        return tree
    }

    /**
     * A demo class that will create a node shape that is either a polygon or star. The number of
     * sides corresponds to the number of nodes that were collapsed into the node represented by this
     * shape.
     *
     * @author Tom Nelson
     * @param N the node type
     */
    inner class ClusterNodeShapeFunction<N> : EllipseNodeShapeFunction<N>() {

        init {
            setSizeTransformer(ClusterNodeSizeFunction(20))
        }

        override fun apply(v: N): Shape {
            if (v is Network<*, *>) {
                val size = v.nodes().size
                return if (size < 8) {
                    val sides = maxOf(size, 3)
                    factory.getRegularPolygon(v, sides)
                } else {
                    factory.getRegularStar(v, size)
                }
            }
            return super.apply(v)
        }
    }

    /**
     * A demo class that will make nodes larger if they represent a collapsed collection of original
     * nodes
     *
     * @author Tom Nelson
     * @param N the node type
     */
    inner class ClusterNodeSizeFunction<N>(private val size: Int) : Function<N, Int> {

        override fun apply(v: N): Int {
            return if (v is Network<*, *>) 30 else size
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val frame = JFrame()
            val content: Container = frame.contentPane
            frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE

            content.add(TreeCollapseDemo())
            frame.pack()
            frame.isVisible = true
        }
    }
}
