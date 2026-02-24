/*
 * Copyright (c) 2004, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 *
 * Created on Nov 7, 2004
 */
package edu.uci.ics.jung.samples

import com.google.common.graph.MutableNetwork
import com.google.common.graph.Network
import com.google.common.graph.NetworkBuilder
import edu.uci.ics.jung.algorithms.generators.random.BarabasiAlbertGenerator
import edu.uci.ics.jung.algorithms.scoring.VoltageScorer
import edu.uci.ics.jung.algorithms.scoring.util.NodeScoreTransformer
import edu.uci.ics.jung.graph.util.Graphs
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.RenderContext
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.decorators.AbstractNodeShapeFunction
import edu.uci.ics.jung.visualization.decorators.EdgeShape
import edu.uci.ics.jung.visualization.decorators.GradientEdgePaintFunction
import edu.uci.ics.jung.visualization.decorators.NumberFormattingFunction
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction
import edu.uci.ics.jung.visualization.picking.PickedInfo
import edu.uci.ics.jung.visualization.renderers.BasicEdgeArrowRenderingSupport
import edu.uci.ics.jung.visualization.renderers.CenterEdgeArrowRenderingSupport
import edu.uci.ics.jung.visualization.renderers.Renderer
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.GradientPaint
import java.awt.GridLayout
import java.awt.Paint
import java.awt.Shape
import java.awt.Stroke
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier
import javax.swing.AbstractAction
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JRadioButton
import javax.swing.WindowConstants

/**
 * Shows off some of the capabilities of `PluggableRenderer`. This code provides examples
 * of different ways to provide and change the various functions that provide property information
 * to the renderer.
 *
 * This demo creates a random graph with random edge weights. It then runs `VoltageRanker`
 * on this graph, using half of the "seed" nodes from the random graph generation as voltage
 * sources, and half of them as voltage sinks.
 *
 * @author Danyel Fisher, Joshua O'Madadhain, Tom Nelson
 */
class PluggableRendererDemo : JPanel(), ActionListener {

    protected lateinit var v_color: JCheckBox
    protected lateinit var e_color: JCheckBox
    protected lateinit var v_stroke: JCheckBox
    protected lateinit var e_arrow_centered: JCheckBox
    protected lateinit var v_shape: JCheckBox
    protected lateinit var v_size: JCheckBox
    protected lateinit var v_aspect: JCheckBox
    protected lateinit var v_labels: JCheckBox
    protected lateinit var e_line: JRadioButton
    protected lateinit var e_bent: JRadioButton
    protected lateinit var e_wedge: JRadioButton
    protected lateinit var e_quad: JRadioButton
    protected lateinit var e_ortho: JRadioButton
    protected lateinit var e_cubic: JRadioButton
    protected lateinit var e_labels: JCheckBox
    protected lateinit var font: JCheckBox
    protected lateinit var e_filter_small: JCheckBox
    protected lateinit var e_show_u: JCheckBox
    protected lateinit var v_small: JCheckBox
    protected lateinit var zoom_at_mouse: JCheckBox
    protected lateinit var fill_edges: JCheckBox

    protected lateinit var no_gradient: JRadioButton
    protected lateinit var gradient_relative: JRadioButton

    private lateinit var seedFillColor: SeedFillColor<Int>
    private lateinit var seedDrawColor: SeedDrawColor<Int>
    private lateinit var ewcs: EdgeWeightStrokeFunction<Number>
    private lateinit var vsh: NodeStrokeHighlight<Int, Number>
    protected lateinit var vs: Function<in Int, String>
    protected lateinit var vs_none: Function<in Int, String>
    protected lateinit var es: Function<in Number, String>
    protected lateinit var es_none: Function<in Number, String>
    private lateinit var vff: NodeFontTransformer<Int>
    private lateinit var eff: EdgeFontTransformer<Number>
    private lateinit var vssa: NodeShapeSizeAspect<Int, Number>
    private lateinit var showNode: NodeDisplayPredicate<Int>
    private lateinit var showEdge: EdgeDisplayPredicate<Number>
    protected lateinit var self_loop: Predicate<Number>
    protected lateinit var edgeDrawPaint: GradientPickedEdgePaintFunction<Int, Number>
    protected lateinit var edgeFillPaint: GradientPickedEdgePaintFunction<Int, Number>

    protected val edge_weight: MutableMap<Number, Number> = HashMap()
    protected lateinit var voltages: Function<Int, Double>
    protected val transparency: MutableMap<Int, Number> = HashMap()

    protected lateinit var vv: VisualizationViewer<Int, Number>
    protected lateinit var gm: DefaultModalGraphMouse<Int, Number>
    protected var seedNodes: MutableSet<Int> = HashSet()

    private lateinit var graph: Network<Int, Number>

    fun startFunction(): JPanel {
        this.graph = buildGraph()

        val layoutAlgorithm: LayoutAlgorithm<Int> = FRLayoutAlgorithm()
        vv = VisualizationViewer(graph, layoutAlgorithm, Dimension(1000, 800))

        val picked_state = vv.getPickedNodeState()

        self_loop = Predicate { e -> Graphs.isSelfLoop(graph, e) }

        // create decorators
        seedFillColor = SeedFillColor(picked_state)
        seedDrawColor = SeedDrawColor()
        ewcs = EdgeWeightStrokeFunction(edge_weight)
        vsh = NodeStrokeHighlight(graph, picked_state)
        vff = NodeFontTransformer()
        eff = EdgeFontTransformer()
        vs_none = Function { _ -> "" }
        es_none = Function { _ -> "" }
        vssa = NodeShapeSizeAspect(graph, voltages)
        showNode = NodeDisplayPredicate(graph, false)
        showEdge = EdgeDisplayPredicate(edge_weight, false)

        // uses a gradient edge if unpicked, otherwise uses picked selection
        edgeDrawPaint = GradientPickedEdgePaintFunction(
            PickableEdgePaintFunction(vv.getPickedEdgeState(), Color.black, Color.cyan), vv
        )
        edgeFillPaint = GradientPickedEdgePaintFunction(
            PickableEdgePaintFunction(vv.getPickedEdgeState(), Color.black, Color.cyan), vv
        )

        vv.getRenderContext().setNodeFillPaintFunction(seedFillColor)
        vv.getRenderContext().setNodeDrawPaintFunction(seedDrawColor)
        vv.getRenderContext().setNodeStrokeFunction(vsh)
        vv.getRenderContext().setNodeLabelFunction(vs_none)
        vv.getRenderContext().setNodeFontFunction(vff)
        vv.getRenderContext().setNodeShapeFunction(vssa)
        vv.getRenderContext().setNodeIncludePredicate(showNode)

        vv.getRenderContext().setEdgeDrawPaintFunction(edgeDrawPaint)
        vv.getRenderContext().setEdgeLabelFunction(es_none)
        vv.getRenderContext().setEdgeFontFunction(eff)
        vv.getRenderContext().setEdgeStrokeFunction(ewcs)
        vv.getRenderContext().setEdgeIncludePredicate(showEdge)
        vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line())

        vv.getRenderContext().setArrowFillPaintFunction { Color.lightGray }
        vv.getRenderContext().setArrowDrawPaintFunction { Color.black }
        val jp = JPanel()
        jp.preferredSize = Dimension(800, 800)
        jp.layout = BorderLayout()

        vv.background = Color.white
        val scrollPane = GraphZoomScrollPane(vv)
        jp.add(scrollPane)
        gm = DefaultModalGraphMouse()
        vv.setGraphMouse(gm)
        gm.add(PopupGraphMousePlugin())

        addBottomControls(jp)
        vssa.setScaling(true)

        vv.setNodeToolTipFunction(VoltageTips<Number>())
        vv.toolTipText =
            "<html><center>Use the mouse wheel to zoom<p>Click and Drag the mouse to pan<p>Shift-click and Drag to Rotate</center></html>"

        return jp
    }

    /**
     * Generates a random graph, runs VoltageRanker on it, and returns the resultant graph.
     *
     * @return the generated graph
     */
    fun buildGraph(): Network<Int, Number> {
        val nodeFactory = object : Supplier<Int> {
            var count = 0
            override fun get(): Int = count++
        }
        val edgeFactory = object : Supplier<Number> {
            var count = 0
            override fun get(): Number = count++
        }
        val generator = BarabasiAlbertGenerator(
            NetworkBuilder.directed().allowsSelfLoops(true).allowsParallelEdges(true),
            nodeFactory,
            edgeFactory,
            4,
            3
        )
        generator.evolveGraph(200)
        val g: MutableNetwork<Int, Number> = generator.get()
        for (e in g.edges()) {
            edge_weight[e] = Math.random()
        }
        es = NumberFormattingFunction(Function { edge_weight[it] })

        // collect the seeds used to define the random graph
        seedNodes = HashSet(generator.seedNodes)

        if (seedNodes.size < 2) {
            println("need at least 2 seeds (one source, one sink)")
        }

        // use these seeds as source and sink nodes, run VoltageRanker
        var source = true
        val sources: MutableSet<Int> = HashSet()
        val sinks: MutableSet<Int> = HashSet()
        for (v in seedNodes) {
            if (source) {
                sources.add(v)
            } else {
                sinks.add(v)
            }
            source = !source
        }
        val voltage_scores = VoltageScorer(g, Function { edge_weight[it] }, sources, sinks)
        voltage_scores.evaluate()
        voltages = NodeScoreTransformer(voltage_scores)
        vs = NumberFormattingFunction(voltages)

        val verts = g.nodes()

        // assign a transparency value of 0.9 to all nodes
        for (v in verts) {
            transparency[v] = 0.9
        }

        // add a couple of self-loops (sanity check on rendering)
        val v = verts.iterator().next()
        var e: Number = Math.random()
        edge_weight[e] = e
        g.addEdge(v, v, e)
        e = Math.random()
        edge_weight[e] = e
        g.addEdge(v, v, e)
        return g
    }

    /**
     * @param jp panel to which controls will be added
     */
    protected fun addBottomControls(jp: JPanel) {
        val control_panel = JPanel()
        jp.add(control_panel, BorderLayout.EAST)
        control_panel.layout = BorderLayout()
        val nodePanel = Box.createVerticalBox()
        nodePanel.border = BorderFactory.createTitledBorder("Nodes")
        val edgePanel = Box.createVerticalBox()
        edgePanel.border = BorderFactory.createTitledBorder("Edges")
        val bothPanel = Box.createVerticalBox()

        control_panel.add(nodePanel, BorderLayout.NORTH)
        control_panel.add(edgePanel, BorderLayout.SOUTH)
        control_panel.add(bothPanel, BorderLayout.CENTER)

        // set up node controls
        v_color = JCheckBox("seed highlight")
        v_color.addActionListener(this)
        v_stroke = JCheckBox("stroke highlight on selection")
        v_stroke.addActionListener(this)
        v_labels = JCheckBox("show voltage values")
        v_labels.addActionListener(this)
        v_shape = JCheckBox("shape by degree")
        v_shape.addActionListener(this)
        v_size = JCheckBox("layoutSize by voltage")
        v_size.addActionListener(this)
        v_size.isSelected = true
        v_aspect = JCheckBox("stretch by degree ratio")
        v_aspect.addActionListener(this)
        v_small = JCheckBox("filter when degree < ${NodeDisplayPredicate.MIN_DEGREE}")
        v_small.addActionListener(this)

        nodePanel.add(v_color)
        nodePanel.add(v_stroke)
        nodePanel.add(v_labels)
        nodePanel.add(v_shape)
        nodePanel.add(v_size)
        nodePanel.add(v_aspect)
        nodePanel.add(v_small)

        // set up edge controls
        val gradient_panel = JPanel(GridLayout(1, 0))
        gradient_panel.border = BorderFactory.createTitledBorder("Edge paint")
        no_gradient = JRadioButton("Solid color")
        no_gradient.addActionListener(this)
        no_gradient.isSelected = true
        gradient_relative = JRadioButton("Gradient")
        gradient_relative.addActionListener(this)
        val bg_grad = ButtonGroup()
        bg_grad.add(no_gradient)
        bg_grad.add(gradient_relative)
        gradient_panel.add(no_gradient)
        gradient_panel.add(gradient_relative)

        val shape_panel = JPanel(GridLayout(3, 2))
        shape_panel.border = BorderFactory.createTitledBorder("Edge shape")
        e_line = JRadioButton("line")
        e_line.addActionListener(this)
        e_line.isSelected = true
        e_wedge = JRadioButton("wedge")
        e_wedge.addActionListener(this)
        e_quad = JRadioButton("quad curve")
        e_quad.addActionListener(this)
        e_cubic = JRadioButton("cubic curve")
        e_cubic.addActionListener(this)
        e_ortho = JRadioButton("orthogonal")
        e_ortho.addActionListener(this)
        val bg_shape = ButtonGroup()
        bg_shape.add(e_line)
        bg_shape.add(e_wedge)
        bg_shape.add(e_quad)
        bg_shape.add(e_ortho)
        bg_shape.add(e_cubic)
        shape_panel.add(e_line)
        shape_panel.add(e_wedge)
        shape_panel.add(e_quad)
        shape_panel.add(e_cubic)
        shape_panel.add(e_ortho)
        fill_edges = JCheckBox("fill edge shapes")
        fill_edges.isSelected = false
        fill_edges.addActionListener(this)
        shape_panel.add(fill_edges)
        shape_panel.isOpaque = true
        e_color = JCheckBox("highlight edge weights")
        e_color.addActionListener(this)
        e_labels = JCheckBox("show edge weight values")
        e_labels.addActionListener(this)
        e_arrow_centered = JCheckBox("centered")
        e_arrow_centered.addActionListener(this)
        val arrow_panel = JPanel(GridLayout(1, 0))
        arrow_panel.border = BorderFactory.createTitledBorder("Show arrows")
        arrow_panel.add(e_arrow_centered)

        e_filter_small = JCheckBox("filter small-weight edges")
        e_filter_small.addActionListener(this)
        e_filter_small.isSelected = true
        val show_edge_panel = JPanel(GridLayout(1, 0))
        show_edge_panel.border = BorderFactory.createTitledBorder("Show edges")
        show_edge_panel.add(e_filter_small)

        shape_panel.alignmentX = Component.LEFT_ALIGNMENT
        edgePanel.add(shape_panel)
        gradient_panel.alignmentX = Component.LEFT_ALIGNMENT
        edgePanel.add(gradient_panel)
        show_edge_panel.alignmentX = Component.LEFT_ALIGNMENT
        edgePanel.add(show_edge_panel)
        arrow_panel.alignmentX = Component.LEFT_ALIGNMENT
        edgePanel.add(arrow_panel)

        e_color.alignmentX = Component.LEFT_ALIGNMENT
        edgePanel.add(e_color)
        e_labels.alignmentX = Component.LEFT_ALIGNMENT
        edgePanel.add(e_labels)

        // set up zoom controls
        zoom_at_mouse = JCheckBox("<html><center>zoom at mouse<p>(wheel only)</center></html>")
        zoom_at_mouse.addActionListener(this)
        zoom_at_mouse.isSelected = true

        val scaler = CrossoverScalingControl()

        val plus = JButton("+")
        plus.addActionListener { scaler.scale(vv, 1.1f, vv.getCenter()) }

        val minus = JButton("-")
        minus.addActionListener { scaler.scale(vv, 1 / 1.1f, vv.getCenter()) }

        val zoomPanel = JPanel()
        zoomPanel.border = BorderFactory.createTitledBorder("Zoom")
        plus.alignmentX = Component.CENTER_ALIGNMENT
        zoomPanel.add(plus)
        minus.alignmentX = Component.CENTER_ALIGNMENT
        zoomPanel.add(minus)
        zoom_at_mouse.alignmentX = Component.CENTER_ALIGNMENT
        zoomPanel.add(zoom_at_mouse)

        val fontPanel = JPanel()
        // add font and zoom controls to center panel
        font = JCheckBox("bold text")
        font.addActionListener(this)
        font.alignmentX = Component.CENTER_ALIGNMENT
        fontPanel.add(font)

        bothPanel.add(zoomPanel)
        bothPanel.add(fontPanel)

        val modeBox = gm.getModeComboBox()
        modeBox.alignmentX = Component.CENTER_ALIGNMENT
        val modePanel = object : JPanel(BorderLayout()) {
            override fun getMaximumSize(): Dimension = preferredSize
        }
        modePanel.border = BorderFactory.createTitledBorder("Mouse Mode")
        modePanel.add(modeBox)
        val comboGrid = JPanel(GridLayout(0, 1))
        comboGrid.add(modePanel)
        fontPanel.add(comboGrid)

        val cb = JComboBox<Renderer.NodeLabel.Position>()
        cb.addItem(Renderer.NodeLabel.Position.N)
        cb.addItem(Renderer.NodeLabel.Position.NE)
        cb.addItem(Renderer.NodeLabel.Position.E)
        cb.addItem(Renderer.NodeLabel.Position.SE)
        cb.addItem(Renderer.NodeLabel.Position.S)
        cb.addItem(Renderer.NodeLabel.Position.SW)
        cb.addItem(Renderer.NodeLabel.Position.W)
        cb.addItem(Renderer.NodeLabel.Position.NW)
        cb.addItem(Renderer.NodeLabel.Position.N)
        cb.addItem(Renderer.NodeLabel.Position.CNTR)
        cb.addItem(Renderer.NodeLabel.Position.AUTO)
        cb.addItemListener { e ->
            val position = e.item as Renderer.NodeLabel.Position
            vv.getRenderer().getNodeLabelRenderer().setPosition(position)
            vv.repaint()
        }
        cb.selectedItem = Renderer.NodeLabel.Position.SE
        val positionPanel = JPanel()
        positionPanel.border = BorderFactory.createTitledBorder("Label Position")
        positionPanel.add(cb)

        comboGrid.add(positionPanel)
    }

    override fun actionPerformed(e: ActionEvent) {
        val source = e.source as javax.swing.AbstractButton
        when {
            source == v_color -> seedFillColor.setSeedColoring(source.isSelected)
            source == e_color -> ewcs.setWeighted(source.isSelected)
            source == v_stroke -> vsh.setHighlight(source.isSelected)
            source == v_labels -> {
                if (source.isSelected) {
                    vv.getRenderContext().setNodeLabelFunction(vs)
                } else {
                    vv.getRenderContext().setNodeLabelFunction(vs_none)
                }
            }
            source == e_labels -> {
                if (source.isSelected) {
                    vv.getRenderContext().setEdgeLabelFunction(es)
                } else {
                    vv.getRenderContext().setEdgeLabelFunction(es_none)
                }
            }
            source == e_arrow_centered -> {
                if (source.isSelected) {
                    vv.getRenderer().getEdgeRenderer().setEdgeArrowRenderingSupport(CenterEdgeArrowRenderingSupport())
                } else {
                    vv.getRenderer().getEdgeRenderer().setEdgeArrowRenderingSupport(BasicEdgeArrowRenderingSupport())
                }
            }
            source == font -> {
                vff.setBold(source.isSelected)
                eff.setBold(source.isSelected)
            }
            source == v_shape -> vssa.useFunnyShapes(source.isSelected)
            source == v_size -> vssa.setScaling(source.isSelected)
            source == v_aspect -> vssa.setStretching(source.isSelected)
            source == e_line -> {
                if (source.isSelected) {
                    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line())
                }
            }
            source == e_ortho -> {
                if (source.isSelected) {
                    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.Orthogonal<Int, Number>())
                }
            }
            source == e_wedge -> {
                if (source.isSelected) {
                    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.Wedge<Int, Number>(10))
                }
            }
            source == e_quad -> {
                if (source.isSelected) {
                    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.QuadCurve<Int, Number>())
                }
            }
            source == e_cubic -> {
                if (source.isSelected) {
                    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.CubicCurve<Int, Number>())
                }
            }
            source == e_filter_small -> showEdge.filterSmall(source.isSelected)
            source == v_small -> showNode.filterSmall(source.isSelected)
            source == zoom_at_mouse -> gm.setZoomAtMouse(source.isSelected)
            source == no_gradient -> {
                if (source.isSelected) {
                    gradient_level = GRADIENT_NONE
                }
            }
            source == gradient_relative -> {
                if (source.isSelected) {
                    gradient_level = GRADIENT_RELATIVE
                }
            }
            source == fill_edges -> {
                vv.getRenderContext().setEdgeFillPaintFunction(
                    if (source.isSelected) edgeFillPaint else Function { Color(0, 0, 0, 0) }
                )
            }
        }
        vv.repaint()
    }

    private inner class SeedDrawColor<N> : Function<N, Paint> {
        override fun apply(v: N): Paint = Color.BLACK
    }

    private inner class SeedFillColor<N>(
        private val pi: PickedInfo<N>
    ) : Function<N, Paint> {

        private var seed_coloring: Boolean = false

        fun setSeedColoring(b: Boolean) {
            this.seed_coloring = b
        }

        override fun apply(v: N): Paint {
            @Suppress("UNCHECKED_CAST")
            val alpha = (transparency as Map<N, Number>)[v]!!.toFloat()
            return when {
                pi.isPicked(v) -> Color(1f, 1f, 0f, alpha)
                @Suppress("UNCHECKED_CAST")
                seed_coloring && (seedNodes as Set<N>).contains(v) -> {
                    val dark = Color(0f, 0f, SEED_FILL_DARK_VALUE, alpha)
                    val light = Color(0f, 0f, SEED_FILL_LIGHT_VALUE, alpha)
                    GradientPaint(0f, 0f, dark, 10f, 0f, light, true)
                }
                else -> Color(1f, 0f, 0f, alpha)
            }
        }
    }

    private class EdgeWeightStrokeFunction<E>(
        private val edge_weight: Map<E, Number>
    ) : Function<E, Stroke> {

        private var weighted = false

        fun setWeighted(weighted: Boolean) {
            this.weighted = weighted
        }

        override fun apply(e: E): Stroke {
            return if (weighted) {
                if (drawHeavy(e)) heavy else dotted
            } else {
                basic
            }
        }

        private fun drawHeavy(e: E): Boolean {
            val value = edge_weight[e]!!.toDouble()
            return value > 0.7
        }

        companion object {
            val basic: Stroke = BasicStroke(1f)
            val heavy: Stroke = BasicStroke(2f)
            val dotted: Stroke = RenderContext.DOTTED
        }
    }

    private class NodeStrokeHighlight<N, E>(
        private val graph: Network<N, E>,
        private val pi: PickedInfo<N>
    ) : Function<N, Stroke> {

        private var highlight = false
        private val heavy: Stroke = BasicStroke(5f)
        private val medium: Stroke = BasicStroke(3f)
        private val light: Stroke = BasicStroke(1f)

        fun setHighlight(highlight: Boolean) {
            this.highlight = highlight
        }

        override fun apply(v: N): Stroke {
            return if (highlight) {
                when {
                    pi.isPicked(v) -> heavy
                    graph.adjacentNodes(v).any { pi.isPicked(it) } -> medium
                    else -> light
                }
            } else {
                light
            }
        }
    }

    private class NodeFontTransformer<N> : Function<N, Font> {
        private var bold = false
        private val f = Font("Helvetica", Font.PLAIN, 12)
        private val b = Font("Helvetica", Font.BOLD, 12)

        fun setBold(bold: Boolean) {
            this.bold = bold
        }

        override fun apply(v: N): Font = if (bold) b else f
    }

    private class EdgeFontTransformer<E> : Function<E, Font> {
        private var bold = false
        private val f = Font("Helvetica", Font.PLAIN, 12)
        private val b = Font("Helvetica", Font.BOLD, 12)

        fun setBold(bold: Boolean) {
            this.bold = bold
        }

        override fun apply(e: E): Font = if (bold) b else f
    }

    private class NodeDisplayPredicate<N>(
        private val graph: Network<N, *>,
        private var filter_small: Boolean
    ) : Predicate<N> {

        fun filterSmall(b: Boolean) {
            filter_small = b
        }

        override fun test(node: N): Boolean =
            if (filter_small) graph.degree(node) >= MIN_DEGREE else true

        companion object {
            const val MIN_DEGREE = 4
        }
    }

    private class EdgeDisplayPredicate<E>(
        private val edge_weights: Map<E, Number>,
        private var filter_small: Boolean
    ) : Predicate<E> {

        private val MIN_WEIGHT = 0.5

        fun filterSmall(b: Boolean) {
            filter_small = b
        }

        override fun test(edge: E): Boolean =
            if (filter_small) edge_weights[edge]!!.toDouble() >= MIN_WEIGHT else true
    }

    /**
     * Controls the shape, layoutSize, and aspect ratio for each node.
     *
     * @author Joshua O'Madadhain
     */
    private class NodeShapeSizeAspect<N, E>(
        private val graph: Network<N, E>,
        private val voltages: Function<N, Double>
    ) : AbstractNodeShapeFunction<N>(), Function<N, Shape> {

        private var stretch = false
        private var scale = false
        private var funny_shapes = false

        init {
            setSizeTransformer { n ->
                if (scale) (voltages.apply(n) * 15).toInt() + 10 else 10
            }
            setAspectRatioTransformer { n ->
                if (stretch) (graph.inDegree(n) + 1).toFloat() / (graph.outDegree(n) + 1).toFloat() else 1.0f
            }
        }

        fun setStretching(stretch: Boolean) {
            this.stretch = stretch
        }

        fun setScaling(scale: Boolean) {
            this.scale = scale
        }

        fun useFunnyShapes(use: Boolean) {
            this.funny_shapes = use
        }

        override fun apply(v: N): Shape {
            return if (funny_shapes) {
                if (graph.degree(v) < 5) {
                    val sides = Math.max(graph.degree(v), 3)
                    factory.getRegularPolygon(v, sides)
                } else {
                    factory.getRegularStar(v, graph.degree(v))
                }
            } else {
                factory.getEllipse(v)
            }
        }
    }

    /** a GraphMousePlugin that offers popup menu support */
    protected inner class PopupGraphMousePlugin @JvmOverloads constructor(
        modifiers: Int = MouseEvent.BUTTON3_MASK
    ) : AbstractPopupGraphMousePlugin(modifiers), MouseListener {

        @Suppress("UNCHECKED_CAST")
        override fun handlePopup(e: MouseEvent) {
            val vv = e.source as VisualizationViewer<Int, Number>
            val layoutModel = vv.getModel().getLayoutModel()
            val p = e.point

            val pickSupport = vv.getPickSupport()
            if (pickSupport != null) {
                val v = pickSupport.getNode(layoutModel, p.getX(), p.getY())
                if (v != null) {
                    val popup = JPopupMenu()
                    popup.add(object : AbstractAction("Decrease Transparency") {
                        override fun actionPerformed(e: ActionEvent) {
                            val value = Math.min(1.0, transparency[v]!!.toDouble() + 0.1)
                            transparency[v] = value
                            vv.repaint()
                        }
                    })
                    popup.add(object : AbstractAction("Increase Transparency") {
                        override fun actionPerformed(e: ActionEvent) {
                            val value = Math.max(0.0, transparency[v]!!.toDouble() - 0.1)
                            transparency[v] = value
                            vv.repaint()
                        }
                    })
                    popup.show(vv, e.x, e.y)
                } else {
                    val edge = pickSupport.getEdge(layoutModel, p.getX(), p.getY())
                    if (edge != null) {
                        val popup = JPopupMenu()
                        popup.add(object : AbstractAction(edge.toString()) {
                            override fun actionPerformed(e: ActionEvent) {
                                System.err.println("got $edge")
                            }
                        })
                        popup.show(vv, e.x, e.y)
                    }
                }
            }
        }
    }

    inner class VoltageTips<E> : Function<Int, String> {
        override fun apply(node: Int): String = "Voltage:${voltages.apply(node)}"
    }

    inner class GradientPickedEdgePaintFunction<N : Any, E : Any>(
        private val defaultFunc: Function<E, Paint>,
        private val vv: VisualizationViewer<N, E>
    ) : GradientEdgePaintFunction<N, E>(Color.WHITE, Color.BLACK, vv) {

        var fill_edge = false

        fun useFill(b: Boolean) {
            fill_edge = b
        }

        override fun apply(e: E): Paint {
            return if (gradient_level == GRADIENT_NONE) {
                defaultFunc.apply(e)
            } else {
                super.apply(e)
            }
        }

        override fun getColor2(e: E): Color {
            return if (this.vv.getPickedEdgeState().isPicked(e)) Color.CYAN else c2
        }
    }

    companion object {
        const val SEED_FILL_DARK_VALUE = 0.8f
        const val SEED_FILL_LIGHT_VALUE = 0.2f
        const val GRADIENT_NONE = 0
        const val GRADIENT_RELATIVE = 1
        @JvmStatic
        var gradient_level = GRADIENT_NONE

        val VOLTAGE_KEY: Any = "voltages"
        val TRANSPARENCY: Any = "transparency"

        @JvmStatic
        fun main(s: Array<String>) {
            val jf = JFrame()
            jf.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            val jp = PluggableRendererDemo().startFunction()
            jf.contentPane.add(jp)
            jf.pack()
            jf.isVisible = true
        }
    }
}
