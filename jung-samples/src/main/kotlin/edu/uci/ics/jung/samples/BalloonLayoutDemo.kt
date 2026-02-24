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
import edu.uci.ics.jung.layout.algorithms.BalloonLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.TreeLayoutAlgorithm
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.MultiLayerTransformer
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer
import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.ModalGraphMouse
import edu.uci.ics.jung.visualization.control.ModalLensGraphMouse
import edu.uci.ics.jung.visualization.decorators.EdgeShape
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
import java.awt.Container
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.event.ItemEvent
import java.awt.geom.AffineTransform
import java.awt.geom.Ellipse2D
import javax.swing.BorderFactory
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JToggleButton
import javax.swing.WindowConstants

/**
 * Demonstrates the visualization of a Tree using TreeLayout and BalloonLayout. An examiner lens
 * performing a hyperbolic transformation of the view is also included.
 *
 * @author Tom Nelson
 */
class BalloonLayoutDemo : JPanel() {

    private val graph: CTreeNetwork<String, Int>
    private var rings: VisualizationServer.Paintable
    private val treeLayoutAlgorithm: TreeLayoutAlgorithm<String>
    private val radialLayoutAlgorithm: BalloonLayoutAlgorithm<String>

    /** the visual component and renderer for the graph */
    private val vv: VisualizationViewer<String, Int>

    /** provides a Hyperbolic lens for the view */
    private val hyperbolicViewSupport: LensSupport
    private val hyperbolicSupport: LensSupport

    init {
        layout = BorderLayout()
        // create a simple graph for the demo
        graph = createTree()

        treeLayoutAlgorithm = TreeLayoutAlgorithm()
        radialLayoutAlgorithm = BalloonLayoutAlgorithm()

        vv = VisualizationViewer(
            graph, treeLayoutAlgorithm, Dimension(900, 900), Dimension(600, 600)
        )
        vv.background = Color.white
        vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line())
        vv.getRenderContext().setNodeLabelFunction { it.toString() }
        // add a listener for ToolTips
        vv.setNodeToolTipFunction { it.toString() }
        vv.getRenderContext().setArrowFillPaintFunction { Color.lightGray }
        rings = Rings(radialLayoutAlgorithm)

        val panel = GraphZoomScrollPane(vv)
        add(panel)

        val graphMouse = DefaultModalGraphMouse<String, Int>()

        vv.setGraphMouse(graphMouse)
        vv.addKeyListener(graphMouse.modeKeyListener!!)
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
        hyperbolicSupport = LayoutLensSupport<String, Int>(
            vv,
            HyperbolicTransformer(
                lens, vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)
            ),
            ModalLensGraphMouse()
        )

        graphMouse.addItemListener(hyperbolicViewSupport.getGraphMouse().getModeListener())
        graphMouse.addItemListener(hyperbolicSupport.getGraphMouse().getModeListener())

        val modeBox = graphMouse.getModeComboBox()
        modeBox.addItemListener(graphMouse.getModeListener())
        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING)

        val scaler = CrossoverScalingControl()

        vv.scaleToLayout(scaler)

        val plus = JButton("+")
        plus.addActionListener { scaler.scale(vv, 1.1f, vv.getCenter()) }

        val minus = JButton("-")
        minus.addActionListener { scaler.scale(vv, 1 / 1.1f, vv.getCenter()) }

        val radial = JToggleButton("Balloon")
        val animateTransition = JRadioButton("Animate Transition")

        radial.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
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
            } else {
                (e.source as JToggleButton).text = "Balloon"
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
            }
            vv.repaint()
        }

        val hyperView = JRadioButton("Hyperbolic View")
        hyperView.addItemListener { e ->
            hyperbolicViewSupport.activate(e.stateChange == ItemEvent.SELECTED)
        }
        val hyperLayout = JRadioButton("Hyperbolic Layout")
        hyperLayout.addItemListener { e ->
            hyperbolicSupport.activate(e.stateChange == ItemEvent.SELECTED)
        }
        val noLens = JRadioButton("No Lens")
        noLens.isSelected = true

        val radio = ButtonGroup()
        radio.add(hyperView)
        radio.add(hyperLayout)
        radio.add(noLens)

        val scaleGrid = JPanel(java.awt.GridLayout(1, 0))
        scaleGrid.border = BorderFactory.createTitledBorder("Zoom")
        val viewControls = JPanel()
        viewControls.layout = java.awt.GridLayout(0, 1)

        val controls = JPanel()
        scaleGrid.add(plus)
        scaleGrid.add(minus)
        val layoutControls = JPanel()
        layoutControls.add(radial)
        layoutControls.add(animateTransition)
        controls.add(layoutControls)
        controls.add(scaleGrid)
        controls.add(modeBox)
        viewControls.add(hyperView)
        viewControls.add(hyperLayout)
        viewControls.add(noLens)
        controls.add(viewControls)
        add(controls, BorderLayout.SOUTH)
    }

    internal inner class Rings(private val layoutAlgorithm: BalloonLayoutAlgorithm<String>) :
        VisualizationServer.Paintable {

        override fun paint(g: Graphics) {
            g.color = Color.gray

            val g2d = g as Graphics2D

            val ellipse = Ellipse2D.Double()
            for (v in vv.getModel().getNetwork().nodes()) {
                val radius = layoutAlgorithm.radii[v] ?: continue
                val p: Point = vv.getModel().getLayoutModel().apply(v)
                ellipse.setFrame(-radius, -radius, 2 * radius, 2 * radius)
                val at = AffineTransform.getTranslateInstance(p.x, p.y)
                var shape: Shape = at.createTransformedShape(ellipse)

                val multiLayerTransformer: MultiLayerTransformer =
                    vv.getRenderContext().getMultiLayerTransformer()

                val viewTransformer = multiLayerTransformer.getTransformer(Layer.VIEW)
                val layoutTransformer = multiLayerTransformer.getTransformer(Layer.LAYOUT)

                if (viewTransformer is LensTransformer) {
                    shape = multiLayerTransformer.transform(shape)
                } else if (layoutTransformer is LensTransformer) {
                    val layoutModel: LayoutModel<String> = vv.getModel().getLayoutModel()
                    val d = Dimension(layoutModel.width, layoutModel.height)

                    val shapeChanger = HyperbolicShapeTransformer(d, viewTransformer)
                    val lensTransformer = layoutTransformer
                    shapeChanger.lens.lensShape = lensTransformer.lens.lensShape
                    val layoutDelegate =
                        (layoutTransformer as MutableTransformerDecorator).delegate
                    shape = shapeChanger.transform(layoutDelegate.transform(shape))
                } else {
                    shape = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, shape)!!
                }

                g2d.draw(shape)
            }
        }

        override fun useTransform(): Boolean = true
    }

    private fun createTree(): CTreeNetwork<String, Int> {
        val tree: MutableCTreeNetwork<String, Int> =
            TreeNetworkBuilder.builder().expectedNodeCount(27).build()

        var edgeId = 0
        tree.addNode("A0")
        tree.addEdge("A0", "B0", edgeId++)
        tree.addEdge("A0", "B1", edgeId++)
        tree.addEdge("A0", "B2", edgeId++)

        tree.addEdge("B0", "C0", edgeId++)
        tree.addEdge("B0", "C1", edgeId++)
        tree.addEdge("B0", "C2", edgeId++)
        tree.addEdge("B0", "C3", edgeId++)

        tree.addEdge("C2", "H0", edgeId++)
        tree.addEdge("C2", "H1", edgeId++)

        tree.addEdge("B1", "D0", edgeId++)
        tree.addEdge("B1", "D1", edgeId++)
        tree.addEdge("B1", "D2", edgeId++)

        tree.addEdge("B2", "E0", edgeId++)
        tree.addEdge("B2", "E1", edgeId++)
        tree.addEdge("B2", "E2", edgeId++)

        tree.addEdge("D0", "F0", edgeId++)
        tree.addEdge("D0", "F1", edgeId++)
        tree.addEdge("D0", "F2", edgeId++)

        tree.addEdge("D1", "G0", edgeId++)
        tree.addEdge("D1", "G1", edgeId++)
        tree.addEdge("D1", "G2", edgeId++)
        tree.addEdge("D1", "G3", edgeId++)
        tree.addEdge("D1", "G4", edgeId++)
        tree.addEdge("D1", "G5", edgeId++)
        tree.addEdge("D1", "G6", edgeId++)
        tree.addEdge("D1", "G7", edgeId++)

        return tree
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val frame = JFrame()
            val content: Container = frame.contentPane
            frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE

            content.add(BalloonLayoutDemo())
            frame.pack()
            frame.isVisible = true
        }
    }
}
