/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.renderers

import edu.uci.ics.jung.visualization.RenderContext
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.spatial.Spatial
import java.awt.Dimension

/**
 * The interface for drawing nodes, edges, and their labels. Implementations of this class can set
 * specific renderers for each element, allowing custom control of each.
 */
interface Renderer<N : Any, E : Any> {

    fun render(
        rc: RenderContext<N, E>,
        visualizationModel: VisualizationModel<N, E>,
        nodeSpatial: Spatial<N>,
        edgeSpatial: Spatial<E>
    )

    fun render(renderContext: RenderContext<N, E>, visualizationModel: VisualizationModel<N, E>)

    fun renderNode(
        renderContext: RenderContext<N, E>,
        visualizationModel: VisualizationModel<N, E>,
        v: N
    )

    fun renderNodeLabel(
        renderContext: RenderContext<N, E>,
        visualizationModel: VisualizationModel<N, E>,
        v: N
    )

    fun renderEdge(
        renderContext: RenderContext<N, E>,
        visualizationModel: VisualizationModel<N, E>,
        e: E
    )

    fun renderEdgeLabel(
        renderContext: RenderContext<N, E>,
        visualizationModel: VisualizationModel<N, E>,
        e: E
    )

    fun setNodeRenderer(r: Node<N, E>)

    fun setEdgeRenderer(r: Edge<N, E>)

    fun setNodeLabelRenderer(r: NodeLabel<N, E>)

    fun setEdgeLabelRenderer(r: EdgeLabel<N, E>)

    fun getNodeLabelRenderer(): NodeLabel<N, E>

    fun getNodeRenderer(): Node<N, E>

    fun getEdgeRenderer(): Edge<N, E>

    fun getEdgeLabelRenderer(): EdgeLabel<N, E>

    interface Node<N : Any, E : Any> {
        fun paintNode(
            renderContext: RenderContext<N, E>,
            visualizationModel: VisualizationModel<N, E>,
            v: N
        )

        class NOOP<N : Any, E : Any> : Node<N, E> {
            override fun paintNode(
                renderContext: RenderContext<N, E>,
                visualizationModel: VisualizationModel<N, E>,
                v: N
            ) {
            }
        }
    }

    interface Edge<N : Any, E : Any> {
        fun paintEdge(
            renderContext: RenderContext<N, E>,
            visualizationModel: VisualizationModel<N, E>,
            e: E
        )

        fun getEdgeArrowRenderingSupport(): EdgeArrowRenderingSupport<N, E>

        fun setEdgeArrowRenderingSupport(edgeArrowRenderingSupport: EdgeArrowRenderingSupport<N, E>)

        class NOOP<N : Any, E : Any> : Edge<N, E> {
            override fun paintEdge(
                renderContext: RenderContext<N, E>,
                visualizationModel: VisualizationModel<N, E>,
                e: E
            ) {
            }

            override fun getEdgeArrowRenderingSupport(): EdgeArrowRenderingSupport<N, E> =
                throw UnsupportedOperationException("NOOP renderer has no edge arrow rendering support")

            override fun setEdgeArrowRenderingSupport(
                edgeArrowRenderingSupport: EdgeArrowRenderingSupport<N, E>
            ) {
            }
        }
    }

    interface NodeLabel<N : Any, E : Any> {
        fun labelNode(
            renderContext: RenderContext<N, E>,
            visualizationModel: VisualizationModel<N, E>,
            v: N,
            label: String
        )

        fun getPosition(): Position

        fun setPosition(position: Position)

        fun setPositioner(positioner: Positioner)

        fun getPositioner(): Positioner

        class NOOP<N : Any, E : Any> : NodeLabel<N, E> {
            override fun labelNode(
                renderContext: RenderContext<N, E>,
                visualizationModel: VisualizationModel<N, E>,
                v: N,
                label: String
            ) {
            }

            override fun getPosition(): Position = Position.CNTR

            override fun setPosition(position: Position) {}

            override fun getPositioner(): Positioner = object : Positioner {
                override fun getPosition(x: Float, y: Float, d: Dimension): Position = Position.CNTR
            }

            override fun setPositioner(positioner: Positioner) {}
        }

        enum class Position {
            N, NE, E, SE, S, SW, W, NW, CNTR, AUTO
        }

        interface Positioner {
            fun getPosition(x: Float, y: Float, d: Dimension): Position
        }
    }

    interface EdgeLabel<N : Any, E : Any> {
        fun labelEdge(
            renderContext: RenderContext<N, E>,
            visualizationModel: VisualizationModel<N, E>,
            e: E,
            label: String
        )

        class NOOP<N : Any, E : Any> : EdgeLabel<N, E> {
            override fun labelEdge(
                renderContext: RenderContext<N, E>,
                visualizationModel: VisualizationModel<N, E>,
                e: E,
                label: String
            ) {
            }
        }
    }
}
