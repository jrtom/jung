package edu.uci.ics.jung.visualization

import com.google.common.graph.Network
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor
import edu.uci.ics.jung.visualization.picking.PickedState
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer
import edu.uci.ics.jung.visualization.renderers.NodeLabelRenderer
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator
import edu.uci.ics.jung.visualization.util.Context
import edu.uci.ics.jung.visualization.util.EdgeIndexFunction
import java.awt.BasicStroke
import java.awt.Font
import java.awt.Paint
import java.awt.Shape
import java.awt.Stroke
import java.util.function.Function
import java.util.function.Predicate
import javax.swing.CellRendererPane
import javax.swing.Icon
import javax.swing.JComponent

interface RenderContext<N : Any, E : Any> {

    fun getLabelOffset(): Int

    fun setLabelOffset(labelOffset: Int)

    fun getArrowPlacementTolerance(): Float

    fun setArrowPlacementTolerance(arrow_placement_tolerance: Float)

    fun getEdgeArrow(): Shape

    fun setEdgeArrow(shape: Shape)

    fun renderEdgeArrow(): Boolean

    fun setRenderEdgeArrow(render: Boolean)

    fun getEdgeFontFunction(): Function<in E, Font>

    fun setEdgeFontFunction(edgeFontFunction: Function<in E, Font>)

    fun getEdgeIncludePredicate(): Predicate<E>

    fun setEdgeIncludePredicate(edgeIncludePredicate: Predicate<E>)

    fun getEdgeLabelCloseness(): Float

    fun setEdgeLabelCloseness(closeness: Float)

    fun getEdgeLabelRenderer(): EdgeLabelRenderer

    fun setEdgeLabelRenderer(edgeLabelRenderer: EdgeLabelRenderer)

    fun getEdgeFillPaintFunction(): Function<in E, Paint>

    fun setEdgeFillPaintFunction(edgePaintFunction: Function<in E, Paint>)

    fun getEdgeDrawPaintFunction(): Function<in E, Paint>

    fun setEdgeDrawPaintFunction(edgeDrawPaintFunction: Function<in E, Paint>)

    fun getArrowDrawPaintFunction(): Function<in E, Paint>

    fun setArrowDrawPaintFunction(arrowDrawPaintFunction: Function<in E, Paint>)

    fun getArrowFillPaintFunction(): Function<in E, Paint>

    fun setArrowFillPaintFunction(arrowFillPaintFunction: Function<in E, Paint>)

    fun getEdgeShapeFunction(): Function<Context<Network<N, E>, E>, Shape>

    fun setEdgeShapeFunction(edgeShapeFunction: Function<Context<Network<N, E>, E>, Shape>)

    fun getEdgeLabelFunction(): Function<in E, String>

    fun setEdgeLabelFunction(edgeStringer: Function<in E, String>)

    fun edgeStrokeFunction(): Function<in E, Stroke>

    fun setEdgeStrokeFunction(edgeStrokeFunction: Function<in E, Stroke>)

    fun getEdgeArrowStrokeFunction(): Function<in E, Stroke>

    fun setEdgeArrowStrokeFunction(edgeArrowStrokeFunction: Function<in E, Stroke>)

    fun getGraphicsContext(): GraphicsDecorator?

    fun setGraphicsContext(graphicsContext: GraphicsDecorator)

    fun getParallelEdgeIndexFunction(): EdgeIndexFunction<N, E>

    fun setParallelEdgeIndexFunction(parallelEdgeIndexFunction: EdgeIndexFunction<N, E>)

    fun getPickedEdgeState(): PickedState<E>

    fun setPickedEdgeState(pickedEdgeState: PickedState<E>)

    fun getPickedNodeState(): PickedState<N>

    fun setPickedNodeState(pickedNodeState: PickedState<N>)

    fun getRendererPane(): CellRendererPane

    fun setRendererPane(rendererPane: CellRendererPane)

    fun getScreenDevice(): JComponent

    fun setScreenDevice(screenDevice: JComponent)

    fun getNodeFontFunction(): Function<in N, Font>

    fun setNodeFontFunction(nodeFontFunction: Function<in N, Font>)

    fun getNodeIconFunction(): Function<N, Icon>

    fun setNodeIconFunction(nodeIconFunction: Function<N, Icon>)

    fun getNodeIncludePredicate(): Predicate<N>

    fun setNodeIncludePredicate(nodeIncludePredicate: Predicate<N>)

    fun getNodeLabelRenderer(): NodeLabelRenderer

    fun setNodeLabelRenderer(nodeLabelRenderer: NodeLabelRenderer)

    fun getNodeFillPaintFunction(): Function<in N, Paint>

    fun setNodeFillPaintFunction(nodeFillPaintFunction: Function<in N, Paint>)

    fun getNodeDrawPaintFunction(): Function<in N, Paint>

    fun setNodeDrawPaintFunction(nodeDrawPaintFunction: Function<in N, Paint>)

    fun getNodeShapeFunction(): Function<in N, Shape>

    fun setNodeShapeFunction(nodeShapeFunction: Function<in N, Shape>)

    fun getNodeLabelFunction(): Function<in N, String>

    fun setNodeLabelFunction(nodeStringer: Function<in N, String>)

    fun getNodeLabelDrawPaintFunction(): Function<in N, Paint>

    fun setNodeLabelDrawPaintFunction(nodeLabelDrawPaintFunction: Function<in N, Paint>)

    fun getNodeStrokeFunction(): Function<in N, Stroke>

    fun setNodeStrokeFunction(nodeStrokeFunction: Function<in N, Stroke>)

    class DirectedEdgeArrowPredicate : Predicate<Network<*, *>> {
        override fun test(graph: Network<*, *>): Boolean = graph.isDirected
    }

    class UndirectedEdgeArrowPredicate : Predicate<Network<*, *>> {
        override fun test(graph: Network<*, *>): Boolean = !graph.isDirected
    }

    fun getMultiLayerTransformer(): MultiLayerTransformer

    fun setMultiLayerTransformer(basicTransformer: MultiLayerTransformer)

    /**
     * @return the pickSupport
     */
    fun getPickSupport(): NetworkElementAccessor<N, E>

    /**
     * @param pickSupport the pickSupport to set
     */
    fun setPickSupport(pickSupport: NetworkElementAccessor<N, E>)

    companion object {
        val dotting = floatArrayOf(1.0f, 3.0f)
        val dashing = floatArrayOf(5.0f)

        /**
         * A stroke for a dotted line: 1 pixel width, round caps, round joins, and an array of {1.0f,
         * 3.0f}.
         */
        val DOTTED: Stroke =
            BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, dotting, 0f)

        /**
         * A stroke for a dashed line: 1 pixel width, square caps, beveled joins, and an array of {5.0f}.
         */
        val DASHED: Stroke =
            BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 1.0f, dashing, 0f)

        /** Specifies the offset for the edge labels. */
        const val LABEL_OFFSET: Int = 10
    }
}
