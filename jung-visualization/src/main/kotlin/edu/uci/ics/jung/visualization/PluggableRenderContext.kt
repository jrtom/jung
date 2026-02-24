/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization

import com.google.common.graph.Network
import edu.uci.ics.jung.visualization.decorators.EdgeShape
import edu.uci.ics.jung.visualization.decorators.ParallelEdgeShapeFunction
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor
import edu.uci.ics.jung.visualization.picking.PickedState
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer
import edu.uci.ics.jung.visualization.renderers.DefaultNodeLabelRenderer
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer
import edu.uci.ics.jung.visualization.renderers.NodeLabelRenderer
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator
import edu.uci.ics.jung.visualization.util.ArrowFactory
import edu.uci.ics.jung.visualization.util.Context
import edu.uci.ics.jung.visualization.util.EdgeIndexFunction
import edu.uci.ics.jung.visualization.util.ParallelEdgeIndexFunction
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Paint
import java.awt.Shape
import java.awt.Stroke
import java.awt.geom.Ellipse2D
import java.util.function.Function
import java.util.function.Predicate
import javax.swing.CellRendererPane
import javax.swing.Icon
import javax.swing.JComponent

open class PluggableRenderContext<N : Any, E : Any> internal constructor(graph: Network<N, E>) : RenderContext<N, E> {

    private var arrowPlacementTolerance: Float = 1f
    private var nodeIncludePredicate: Predicate<N> = Predicate { true }
    private var nodeStrokeFunction: Function<in N, Stroke> = Function { BasicStroke(1.0f) }

    private var nodeShapeFunction: Function<in N, Shape> =
        Function { Ellipse2D.Float(-10f, -10f, 20f, 20f) }

    private var nodeLabelFunction: Function<in N, String> = Function { "" }
    private var nodeIconFunction: Function<N, Icon>? = null
    private var nodeFontFunction: Function<in N, Font> = Function { Font("Helvetica", Font.PLAIN, 12) }

    private var nodeDrawPaintFunction: Function<in N, Paint> = Function { Color.BLACK }
    private var nodeFillPaintFunction: Function<in N, Paint> = Function { Color.RED }
    private var nodeLabelDrawPaintFunction: Function<in N, Paint> = Function { Color.BLACK }

    private var edgeLabelFunction: Function<in E, String> = Function { "" }
    private var edgeStrokeFunction: Function<in E, Stroke> = Function { BasicStroke(1.0f) }
    private var edgeArrowStrokeFunction: Function<in E, Stroke> = Function { BasicStroke(1.0f) }

    private var edgeArrow: Shape
    private var renderEdgeArrow: Boolean

    private var edgeIncludePredicate: Predicate<E> = Predicate { true }
    private var edgeFontFunction: Function<in E, Font> = Function { Font("Helvetica", Font.PLAIN, 12) }

    private var edgeLabelCloseness: Float

    private var edgeShapeFunction: Function<Context<Network<N, E>, E>, Shape>
    private var edgeFillPaintFunction: Function<in E, Paint> = Function { Color.black as Paint }
    private var edgeDrawPaintFunction: Function<in E, Paint> = Function { Color.black }
    private var arrowFillPaintFunction: Function<in E, Paint> = Function { Color.black }
    private var arrowDrawPaintFunction: Function<in E, Paint> = Function { Color.black }

    private var parallelEdgeIndexFunction: EdgeIndexFunction<N, E>

    private var multiLayerTransformer: MultiLayerTransformer = BasicTransformer()

    /** pluggable support for picking graph elements by finding them based on their coordinates. */
    private var pickSupport: NetworkElementAccessor<N, E>? = null

    private var labelOffset: Int = RenderContext.LABEL_OFFSET

    /** the JComponent that this Renderer will display the graph on */
    private var screenDevice: JComponent? = null

    private var pickedNodeState: PickedState<N>? = null
    private var pickedEdgeState: PickedState<E>? = null

    /**
     * The CellRendererPane is used here just as it is in JTree and JTable, to allow a pluggable
     * JLabel-based renderer for Node and Edge label strings and icons.
     */
    private var rendererPane: CellRendererPane = CellRendererPane()

    /** A default GraphLabelRenderer - picked Node labels are blue, picked edge labels are cyan */
    private var nodeLabelRenderer: NodeLabelRenderer = DefaultNodeLabelRenderer(Color.blue)

    private var edgeLabelRenderer: EdgeLabelRenderer = DefaultEdgeLabelRenderer(Color.cyan)

    private var graphicsContext: GraphicsDecorator? = null

    init {
        this.edgeShapeFunction = EdgeShape.QuadCurve<N, E>()
        this.parallelEdgeIndexFunction = ParallelEdgeIndexFunction()
        if (graph.isDirected) {
            this.edgeArrow = ArrowFactory.getNotchedArrow(EDGE_ARROW_WIDTH, EDGE_ARROW_LENGTH, EDGE_ARROW_NOTCH_DEPTH)
            this.renderEdgeArrow = true
            this.edgeLabelCloseness = DIRECTED_EDGE_LABEL_CLOSENESS
        } else {
            this.edgeArrow = ArrowFactory.getWedgeArrow(EDGE_ARROW_WIDTH, EDGE_ARROW_LENGTH)
            this.renderEdgeArrow = false
            this.edgeLabelCloseness = UNDIRECTED_EDGE_LABEL_CLOSENESS
        }
    }

    override fun getNodeShapeFunction(): Function<in N, Shape> = nodeShapeFunction

    override fun setNodeShapeFunction(nodeShapeFunction: Function<in N, Shape>) {
        this.nodeShapeFunction = nodeShapeFunction
    }

    override fun getNodeStrokeFunction(): Function<in N, Stroke> = nodeStrokeFunction

    override fun setNodeStrokeFunction(nodeStrokeFunction: Function<in N, Stroke>) {
        this.nodeStrokeFunction = nodeStrokeFunction
    }

    override fun getArrowPlacementTolerance(): Float = arrowPlacementTolerance

    override fun setArrowPlacementTolerance(arrow_placement_tolerance: Float) {
        this.arrowPlacementTolerance = arrow_placement_tolerance
    }

    override fun getEdgeArrow(): Shape = edgeArrow

    override fun setEdgeArrow(shape: Shape) {
        this.edgeArrow = shape
    }

    override fun renderEdgeArrow(): Boolean = this.renderEdgeArrow

    override fun setRenderEdgeArrow(render: Boolean) {
        this.renderEdgeArrow = render
    }

    override fun getEdgeFontFunction(): Function<in E, Font> = edgeFontFunction

    override fun setEdgeFontFunction(edgeFontFunction: Function<in E, Font>) {
        this.edgeFontFunction = edgeFontFunction
    }

    override fun getEdgeIncludePredicate(): Predicate<E> = edgeIncludePredicate

    override fun setEdgeIncludePredicate(edgeIncludePredicate: Predicate<E>) {
        this.edgeIncludePredicate = edgeIncludePredicate
    }

    override fun getEdgeLabelCloseness(): Float = edgeLabelCloseness

    override fun setEdgeLabelCloseness(closeness: Float) {
        this.edgeLabelCloseness = closeness
    }

    override fun getEdgeLabelRenderer(): EdgeLabelRenderer = edgeLabelRenderer

    override fun setEdgeLabelRenderer(edgeLabelRenderer: EdgeLabelRenderer) {
        this.edgeLabelRenderer = edgeLabelRenderer
    }

    override fun getEdgeFillPaintFunction(): Function<in E, Paint> = edgeFillPaintFunction

    override fun setEdgeDrawPaintFunction(edgeDrawPaintFunction: Function<in E, Paint>) {
        this.edgeDrawPaintFunction = edgeDrawPaintFunction
    }

    override fun getEdgeDrawPaintFunction(): Function<in E, Paint> = edgeDrawPaintFunction

    override fun setEdgeFillPaintFunction(edgeFillPaintFunction: Function<in E, Paint>) {
        this.edgeFillPaintFunction = edgeFillPaintFunction
    }

    override fun getEdgeShapeFunction(): Function<Context<Network<N, E>, E>, Shape> = edgeShapeFunction

    override fun setEdgeShapeFunction(edgeShapeFunction: Function<Context<Network<N, E>, E>, Shape>) {
        this.edgeShapeFunction = edgeShapeFunction
        if (edgeShapeFunction is ParallelEdgeShapeFunction<*, *>) {
            @Suppress("UNCHECKED_CAST")
            val function = edgeShapeFunction as ParallelEdgeShapeFunction<N, E>
            function.setEdgeIndexFunction(this.parallelEdgeIndexFunction)
        }
    }

    override fun getEdgeLabelFunction(): Function<in E, String> = edgeLabelFunction

    override fun setEdgeLabelFunction(edgeLabelFunction: Function<in E, String>) {
        this.edgeLabelFunction = edgeLabelFunction
    }

    override fun edgeStrokeFunction(): Function<in E, Stroke> = edgeStrokeFunction

    override fun setEdgeStrokeFunction(edgeStrokeFunction: Function<in E, Stroke>) {
        this.edgeStrokeFunction = edgeStrokeFunction
    }

    override fun getEdgeArrowStrokeFunction(): Function<in E, Stroke> = edgeArrowStrokeFunction

    override fun setEdgeArrowStrokeFunction(edgeArrowStrokeFunction: Function<in E, Stroke>) {
        this.edgeArrowStrokeFunction = edgeArrowStrokeFunction
    }

    override fun getGraphicsContext(): GraphicsDecorator? = graphicsContext

    override fun setGraphicsContext(graphicsContext: GraphicsDecorator) {
        this.graphicsContext = graphicsContext
    }

    override fun getLabelOffset(): Int = labelOffset

    override fun setLabelOffset(labelOffset: Int) {
        this.labelOffset = labelOffset
    }

    override fun getParallelEdgeIndexFunction(): EdgeIndexFunction<N, E> = parallelEdgeIndexFunction

    override fun setParallelEdgeIndexFunction(parallelEdgeIndexFunction: EdgeIndexFunction<N, E>) {
        this.parallelEdgeIndexFunction = parallelEdgeIndexFunction
        // reset the edge shape Function, as the parallel edge index function
        // is used by it
        this.setEdgeShapeFunction(getEdgeShapeFunction())
    }

    override fun getPickedEdgeState(): PickedState<E> = pickedEdgeState!!

    override fun setPickedEdgeState(pickedEdgeState: PickedState<E>) {
        this.pickedEdgeState = pickedEdgeState
    }

    override fun getPickedNodeState(): PickedState<N> = pickedNodeState!!

    override fun setPickedNodeState(pickedNodeState: PickedState<N>) {
        this.pickedNodeState = pickedNodeState
    }

    override fun getRendererPane(): CellRendererPane = rendererPane

    override fun setRendererPane(rendererPane: CellRendererPane) {
        this.rendererPane = rendererPane
    }

    override fun getScreenDevice(): JComponent = screenDevice!!

    override fun setScreenDevice(screenDevice: JComponent) {
        this.screenDevice = screenDevice
        screenDevice.add(rendererPane)
    }

    override fun getNodeFontFunction(): Function<in N, Font> = nodeFontFunction

    override fun setNodeFontFunction(nodeFontFunction: Function<in N, Font>) {
        this.nodeFontFunction = nodeFontFunction
    }

    override fun getNodeIconFunction(): Function<N, Icon> = nodeIconFunction!!

    override fun setNodeIconFunction(nodeIconFunction: Function<N, Icon>) {
        this.nodeIconFunction = nodeIconFunction
    }

    override fun getNodeIncludePredicate(): Predicate<N> = nodeIncludePredicate

    override fun setNodeIncludePredicate(nodeIncludePredicate: Predicate<N>) {
        this.nodeIncludePredicate = nodeIncludePredicate
    }

    override fun getNodeLabelRenderer(): NodeLabelRenderer = nodeLabelRenderer

    override fun setNodeLabelRenderer(nodeLabelRenderer: NodeLabelRenderer) {
        this.nodeLabelRenderer = nodeLabelRenderer
    }

    override fun getNodeFillPaintFunction(): Function<in N, Paint> = nodeFillPaintFunction

    override fun setNodeFillPaintFunction(nodeFillPaintFunction: Function<in N, Paint>) {
        this.nodeFillPaintFunction = nodeFillPaintFunction
    }

    override fun getNodeDrawPaintFunction(): Function<in N, Paint> = nodeDrawPaintFunction

    override fun setNodeDrawPaintFunction(nodeDrawPaintFunction: Function<in N, Paint>) {
        this.nodeDrawPaintFunction = nodeDrawPaintFunction
    }

    override fun getNodeLabelFunction(): Function<in N, String> = nodeLabelFunction

    override fun setNodeLabelFunction(nodeLabelFunction: Function<in N, String>) {
        this.nodeLabelFunction = nodeLabelFunction
    }

    override fun setNodeLabelDrawPaintFunction(nodeLabelDrawPaintFunction: Function<in N, Paint>) {
        this.nodeLabelDrawPaintFunction = nodeLabelDrawPaintFunction
    }

    override fun getNodeLabelDrawPaintFunction(): Function<in N, Paint> = nodeLabelDrawPaintFunction

    override fun getPickSupport(): NetworkElementAccessor<N, E> = pickSupport!!

    override fun setPickSupport(pickSupport: NetworkElementAccessor<N, E>) {
        this.pickSupport = pickSupport
    }

    override fun getMultiLayerTransformer(): MultiLayerTransformer = multiLayerTransformer

    override fun setMultiLayerTransformer(basicTransformer: MultiLayerTransformer) {
        this.multiLayerTransformer = basicTransformer
    }

    override fun getArrowDrawPaintFunction(): Function<in E, Paint> = arrowDrawPaintFunction

    override fun getArrowFillPaintFunction(): Function<in E, Paint> = arrowFillPaintFunction

    override fun setArrowDrawPaintFunction(arrowDrawPaintFunction: Function<in E, Paint>) {
        this.arrowDrawPaintFunction = arrowDrawPaintFunction
    }

    override fun setArrowFillPaintFunction(arrowFillPaintFunction: Function<in E, Paint>) {
        this.arrowFillPaintFunction = arrowFillPaintFunction
    }

    companion object {
        private const val EDGE_ARROW_LENGTH = 10f
        private const val EDGE_ARROW_WIDTH = 8f
        private const val EDGE_ARROW_NOTCH_DEPTH = 4f
        private const val DIRECTED_EDGE_LABEL_CLOSENESS = 0.65f
        private const val UNDIRECTED_EDGE_LABEL_CLOSENESS = 0.65f

        @JvmStatic
        fun getDashing(): FloatArray = RenderContext.dashing

        @JvmStatic
        fun getDotting(): FloatArray = RenderContext.dotting
    }
}
