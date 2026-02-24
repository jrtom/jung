/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.samples

import edu.uci.ics.jung.graph.CTreeNetwork
import edu.uci.ics.jung.graph.MutableCTreeNetwork
import edu.uci.ics.jung.graph.TreeNetworkBuilder
import edu.uci.ics.jung.layout.algorithms.RadialTreeLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.TreeLayoutAlgorithm
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.PolarPoint
import edu.uci.ics.jung.samples.util.ControlHelpers
import edu.uci.ics.jung.visualization.BaseVisualizationModel
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.MultiLayerTransformer
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.ModalLensGraphMouse
import edu.uci.ics.jung.visualization.decorators.EdgeShape
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction
import edu.uci.ics.jung.visualization.layout.LayoutAlgorithmTransition
import edu.uci.ics.jung.visualization.transform.HyperbolicTransformer
import edu.uci.ics.jung.visualization.transform.LayoutLensSupport
import edu.uci.ics.jung.visualization.transform.Lens
import edu.uci.ics.jung.visualization.transform.LensSupport
import edu.uci.ics.jung.visualization.transform.LensTransformer
import edu.uci.ics.jung.visualization.transform.MutableTransformerDecorator
import edu.uci.ics.jung.visualization.transform.shape.HyperbolicShapeTransformer
import edu.uci.ics.jung.visualization.transform.shape.ViewLensSupport
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridLayout
import java.awt.Shape
import java.awt.event.ItemEvent
import java.awt.geom.Ellipse2D
import javax.swing.BorderFactory
import javax.swing.ButtonGroup
import javax.swing.JFrame
import javax.swing.JMenuBar
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JToggleButton
import javax.swing.WindowConstants

/**
 * Shows a RadialTreeLayout view of a Forest. A hyperbolic projection lens may also be applied to
 * the view.
 *
 * @author Tom Nelson
 */
class RadialTreeLensDemo : JPanel() {

    private val graph: CTreeNetwork<String, Int>

    private var rings: VisualizationServer.Paintable

    private val treeLayoutAlgorithm: TreeLayoutAlgorithm<String>

    private val radialLayoutAlgorithm: RadialTreeLayoutAlgorithm<String>

    /** the visual component and renderer for the graph */
    private val vv: VisualizationViewer<String, Int>

    /** provides a Hyperbolic lens for the view */
    private val hyperbolicViewSupport: LensSupport

    private val hyperbolicLayoutSupport: LensSupport

    init {
        layout = BorderLayout()
        // create a simple graph for the demo
        graph = createTree()

        radialLayoutAlgorithm = RadialTreeLayoutAlgorithm()
        treeLayoutAlgorithm = TreeLayoutAlgorithm()

        val preferredSize = Dimension(600, 600)

        val visualizationModel: VisualizationModel<String, Int> =
            BaseVisualizationModel(graph, radialLayoutAlgorithm, preferredSize)
        vv = VisualizationViewer(visualizationModel, preferredSize)

        val ps = vv.getPickedNodeState()
        val pes = vv.getPickedEdgeState()
        vv.getRenderContext().setNodeFillPaintFunction(PickableNodePaintFunction(ps, Color.red, Color.yellow))
        vv.getRenderContext().setNodeLabelFunction { it.toString() }
        vv.getRenderContext().setEdgeDrawPaintFunction(PickableEdgePaintFunction(pes, Color.black, Color.cyan))
        vv.background = Color.white

        vv.getRenderContext().setNodeLabelFunction { it.toString() }
        vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line())

        // add a listener for ToolTips
        vv.setNodeToolTipFunction { it.toString() }

        val gzsp = GraphZoomScrollPane(vv)
        add(gzsp)

        val graphMouse = DefaultModalGraphMouse<String, Int>()

        vv.setGraphMouse(graphMouse)
        vv.addKeyListener(graphMouse.modeKeyListener!!)
        rings = Rings(vv.getModel().getLayoutModel())
        vv.addPreRenderPaintable(rings)

        val radial = JToggleButton("Tree")
        val animateTransition = JRadioButton("Animate Transition")

        radial.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                (e.source as JToggleButton).text = "Radial"
                if (animateTransition.isSelected) {
                    LayoutAlgorithmTransition.animate(vv, treeLayoutAlgorithm)
                } else {
                    LayoutAlgorithmTransition.apply(vv, treeLayoutAlgorithm)
                }

                vv.getRenderContext()
                    .getMultiLayerTransformer()
                    .getTransformer(Layer.LAYOUT)
                    .setToIdentity()
                vv.removePreRenderPaintable(rings)
            } else {
                (e.source as JToggleButton).text = "Tree"
                if (animateTransition.isSelected) {
                    LayoutAlgorithmTransition.animate(vv, radialLayoutAlgorithm)
                } else {
                    LayoutAlgorithmTransition.apply(vv, radialLayoutAlgorithm)
                }

                vv.getRenderContext()
                    .getMultiLayerTransformer()
                    .getTransformer(Layer.LAYOUT)
                    .setToIdentity()
                vv.addPreRenderPaintable(rings)
            }
            vv.repaint()
        }

        val layoutModel = vv.getModel().getLayoutModel()
        val d = Dimension(layoutModel.width, layoutModel.height)

        val lens = Lens(d)
        hyperbolicViewSupport = ViewLensSupport<String, Int>(
            vv,
            HyperbolicShapeTransformer(
                lens, vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)
            ),
            ModalLensGraphMouse()
        )
        hyperbolicLayoutSupport = LayoutLensSupport<String, Int>(
            vv,
            HyperbolicTransformer(
                lens,
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)
            ),
            ModalLensGraphMouse()
        )

        val hyperView = JRadioButton("Hyperbolic View")
        hyperView.addItemListener { e ->
            hyperbolicViewSupport.activate(e.stateChange == ItemEvent.SELECTED)
        }
        val hyperLayout = JRadioButton("Hyperbolic Layout")
        hyperLayout.addItemListener { e ->
            hyperbolicLayoutSupport.activate(e.stateChange == ItemEvent.SELECTED)
        }
        val noLens = JRadioButton("No Lens")

        val radio = ButtonGroup()
        radio.add(hyperView)
        radio.add(hyperLayout)
        radio.add(noLens)

        graphMouse.addItemListener(hyperbolicViewSupport.getGraphMouse().getModeListener())
        graphMouse.addItemListener(hyperbolicLayoutSupport.getGraphMouse().getModeListener())

        val menubar = JMenuBar()
        menubar.add(graphMouse.getModeMenu())
        gzsp.setCorner(menubar)

        val controls = JPanel(GridLayout(1, 0))
        val hyperControls = JPanel(GridLayout(3, 2))
        hyperControls.border = BorderFactory.createTitledBorder("Examiner Lens")
        val modeControls = JPanel(BorderLayout())
        modeControls.border = BorderFactory.createTitledBorder("Mouse Mode")
        modeControls.add(graphMouse.getModeComboBox())
        hyperControls.add(hyperView)
        hyperControls.add(hyperLayout)
        hyperControls.add(noLens)

        controls.add(ControlHelpers.getZoomControls(vv, "Zoom"))
        controls.add(hyperControls)
        controls.add(modeControls)
        val layoutControls = JPanel(GridLayout(0, 1))
        layoutControls.border = BorderFactory.createTitledBorder("Layouts")
        val radialPanel = JPanel()
        radialPanel.add(radial)
        layoutControls.add(radialPanel)
        layoutControls.add(animateTransition)
        controls.add(layoutControls)
        add(controls, BorderLayout.SOUTH)
    }

    private fun createTree(): CTreeNetwork<String, Int> {
        val tree: MutableCTreeNetwork<String, Int> =
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

    private inner class Rings(private val layoutModel: LayoutModel<String>) : VisualizationServer.Paintable {

        private val depths: Collection<Double> = getDepths()

        private fun getDepths(): Set<Double> {
            val depths = HashSet<Double>()
            val polarLocations: Map<String, PolarPoint> = radialLayoutAlgorithm.polarLocations
            for (v in graph.nodes()) {
                val pp = polarLocations[v]
                depths.add(pp!!.radius)
            }
            return depths
        }

        override fun paint(g: Graphics) {
            g.color = Color.gray
            val g2d = g as Graphics2D
            val center = radialLayoutAlgorithm.getCenter(layoutModel)

            val ellipse = Ellipse2D.Double()
            for (d in depths) {
                ellipse.setFrameFromDiagonal(center.x - d, center.y - d, center.x + d, center.y + d)
                var shape: Shape = ellipse

                val multiLayerTransformer: MultiLayerTransformer =
                    vv.getRenderContext().getMultiLayerTransformer()

                val viewTransformer = multiLayerTransformer.getTransformer(Layer.VIEW)
                val layoutTransformer = multiLayerTransformer.getTransformer(Layer.LAYOUT)

                if (viewTransformer is MutableTransformerDecorator) {
                    shape = multiLayerTransformer.transform(shape)
                } else if (layoutTransformer is LensTransformer) {
                    val lm = vv.getModel().getLayoutModel()
                    val dimension = Dimension(lm.width, lm.height)

                    val shapeChanger = HyperbolicShapeTransformer(dimension, viewTransformer)
                    val lensTransformer = layoutTransformer
                    shapeChanger.lens.lensShape = lensTransformer.lens.lensShape
                    val layoutDelegate = (layoutTransformer as MutableTransformerDecorator).delegate
                    shape = shapeChanger.transform(layoutDelegate.transform(shape))
                } else {
                    shape = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, shape)!!
                }

                g2d.draw(shape)
            }
        }

        override fun useTransform(): Boolean = true
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val f = JFrame()
            f.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            f.contentPane.add(RadialTreeLensDemo())
            f.pack()
            f.isVisible = true
        }
    }
}
