/*
 * Created on Jan 2, 2004
 */
package edu.uci.ics.jung.samples

import com.google.common.graph.ElementOrder
import com.google.common.graph.EndpointPair
import com.google.common.graph.MutableNetwork
import com.google.common.graph.Network
import com.google.common.graph.NetworkBuilder
import edu.uci.ics.jung.algorithms.generators.random.EppsteinPowerLawGenerator
import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer
import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.renderers.Renderer
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Paint
import java.awt.Stroke
import java.awt.geom.Point2D
import java.util.function.Function
import java.util.function.Supplier
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.WindowConstants

/**
 * Demonstrates use of the shortest path algorithm and visualization of the results.
 *
 * @author danyelf
 */
class ShortestPathDemo : JPanel() {

    /** Starting node */
    private var mFrom: String? = null

    /** Ending node */
    private var mTo: String? = null

    private val mGraph: Network<String, Number>
    private var mPred: Set<String>? = null

    init {
        this.mGraph = getGraph()
        background = Color.WHITE
        // show graph
        val layoutAlgorithm: LayoutAlgorithm<String> = FRLayoutAlgorithm()
        val vv = VisualizationViewer<String, Number>(mGraph, layoutAlgorithm, Dimension(1000, 1000))
        vv.background = Color.WHITE

        vv.getRenderContext().setNodeDrawPaintFunction(MyNodeDrawPaintFunction())
        vv.getRenderContext().setNodeFillPaintFunction(MyNodeFillPaintFunction())
        vv.getRenderContext().setEdgeDrawPaintFunction(MyEdgePaintFunction())
        vv.getRenderContext().setEdgeStrokeFunction(MyEdgeStrokeFunction())
        vv.getRenderContext().setNodeLabelFunction { it.toString() }
        vv.setGraphMouse(DefaultModalGraphMouse<String, Number>())
        val layoutModel = vv.getModel().getLayoutModel()
        vv.addPostRenderPaintable(object : VisualizationServer.Paintable {

            override fun useTransform(): Boolean = true

            override fun paint(g: Graphics) {
                if (mPred == null) {
                    return
                }

                // for all edges, paint edges that are in shortest path
                for (e in mGraph.edges()) {
                    if (isBlessed(e)) {
                        val endpoints: EndpointPair<String> = mGraph.incidentNodes(e)
                        val v1 = endpoints.nodeU()
                        val v2 = endpoints.nodeV()
                        val p1 = layoutModel.apply(v1)
                        val p2 = layoutModel.apply(v2)
                        val p2d1: Point2D = vv.getRenderContext()
                            .getMultiLayerTransformer()
                            .transform(Layer.LAYOUT, Point2D.Double(p1.x, p1.y))!!
                        val p2d2: Point2D = vv.getRenderContext()
                            .getMultiLayerTransformer()
                            .transform(Layer.LAYOUT, Point2D.Double(p2.x, p2.y))!!
                        val renderer: Renderer<String, Number> = vv.getRenderer()
                        renderer.renderEdge(vv.getRenderContext(), vv.getModel(), e)
                    }
                }
            }
        })

        layout = BorderLayout()
        add(vv, BorderLayout.CENTER)
        // set up controls
        add(setUpControls(), BorderLayout.SOUTH)
    }

    fun isBlessed(e: Number): Boolean {
        val endpoints: EndpointPair<String> = mGraph.incidentNodes(e)
        val v1 = endpoints.nodeU()
        val v2 = endpoints.nodeV()
        return v1 != v2 && mPred!!.contains(v1) && mPred!!.contains(v2)
    }

    inner class MyEdgePaintFunction : Function<Number, Paint> {
        override fun apply(e: Number): Paint {
            if (mPred == null || mPred!!.isEmpty()) {
                return Color.BLACK
            }
            return if (isBlessed(e)) {
                Color(0.0f, 0.0f, 1.0f, 0.5f)
            } else {
                Color.LIGHT_GRAY
            }
        }
    }

    inner class MyEdgeStrokeFunction : Function<Number, Stroke> {
        private val THIN: Stroke = BasicStroke(1f)
        private val THICK: Stroke = BasicStroke(1f)

        override fun apply(e: Number): Stroke {
            if (mPred == null || mPred!!.isEmpty()) {
                return THIN
            }
            return if (isBlessed(e)) THICK else THIN
        }
    }

    inner class MyNodeDrawPaintFunction<N> : Function<N, Paint> {
        override fun apply(v: N): Paint = Color.black
    }

    inner class MyNodeFillPaintFunction<N> : Function<N, Paint> {
        override fun apply(v: N): Paint {
            if (v == mFrom) {
                return Color.BLUE
            }
            if (v == mTo) {
                return Color.BLUE
            }
            return if (mPred == null) {
                Color.LIGHT_GRAY
            } else {
                if (mPred!!.contains(v as Any)) Color.RED else Color.LIGHT_GRAY
            }
        }
    }

    private fun setUpControls(): JPanel {
        val jp = JPanel()
        jp.background = Color.WHITE
        jp.layout = BoxLayout(jp, BoxLayout.PAGE_AXIS)
        jp.border = BorderFactory.createLineBorder(Color.black, 3)
        jp.add(JLabel("Select a pair of nodes for which a shortest path will be displayed"))
        val jp2 = JPanel()
        jp2.add(JLabel("node from", SwingConstants.LEFT))
        jp2.add(getSelectionBox(true))
        jp2.background = Color.white
        val jp3 = JPanel()
        jp3.add(JLabel("node to", SwingConstants.LEFT))
        jp3.add(getSelectionBox(false))
        jp3.background = Color.white
        jp.add(jp2)
        jp.add(jp3)
        return jp
    }

    private fun getSelectionBox(from: Boolean): Component {
        val nodes = Array(mGraph.nodes().size) { "" }
        var i = 0
        for (node in mGraph.nodes()) {
            nodes[i++] = node
        }
        val choices = JComboBox(nodes)
        choices.selectedIndex = -1
        choices.background = Color.WHITE
        choices.addActionListener {
            val v = choices.selectedItem as String

            if (from) {
                mFrom = v
            } else {
                mTo = v
            }
            drawShortest()
            repaint()
        }
        return choices
    }

    protected fun drawShortest() {
        if (mFrom == null || mTo == null) {
            return
        }
        val bdl = BFSDistanceLabeler<String>()
        val from = mFrom ?: return
        bdl.labelDistances(mGraph.asGraph(), from)
        mPred = HashSet()

        // grab a predecessor
        var v: String = mTo!!
        var prd: Set<String>? = bdl.getPredecessors(v)
        (mPred as HashSet<String>).add(mTo!!)
        while (prd != null && prd.isNotEmpty()) {
            v = prd.iterator().next()
            (mPred as HashSet<String>).add(v)
            if (v == mFrom) {
                return
            }
            prd = bdl.getPredecessors(v)
        }
    }

    fun getGraph(): Network<String, Number> {
        val g = EppsteinPowerLawGenerator(NodeFactory(), 26, 50, 50).get()
        // convert this graph into a Network because the visualization system can't handle Graphs (yet)
        val graph: MutableNetwork<String, Number> =
            NetworkBuilder.undirected().nodeOrder(ElementOrder.natural<String>()).build()
        val edgeFactory = EdgeFactory()
        // this implicitly removes any isolated nodes, as intended
        for (endpoints in g.edges()) {
            graph.addEdge(endpoints.nodeU(), endpoints.nodeV(), edgeFactory.get())
        }
        return graph
    }

    private class NodeFactory : Supplier<String> {
        var a = 'a'

        override fun get(): String = (a++).toString()
    }

    private class EdgeFactory : Supplier<Number> {
        var count = 0

        override fun get(): Number = count++
    }

    companion object {
        @JvmStatic
        fun main(s: Array<String>) {
            val jf = JFrame()
            jf.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            jf.contentPane.add(ShortestPathDemo())
            jf.pack()
            jf.isVisible = true
        }
    }
}
