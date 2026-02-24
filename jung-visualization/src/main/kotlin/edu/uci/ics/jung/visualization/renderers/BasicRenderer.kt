/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.renderers

import edu.uci.ics.jung.visualization.RenderContext
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.spatial.Spatial
import org.slf4j.LoggerFactory
import java.util.ConcurrentModificationException

/**
 * The default implementation of the Renderer used by the VisualizationViewer. Default Node and Edge
 * Renderers are supplied, or the user may set custom values. The Node and Edge renderers are used
 * in the renderNode and renderEdge methods, which are called in the render loop of the
 * VisualizationViewer.
 *
 * @author Tom Nelson
 */
open class BasicRenderer<N : Any, E : Any> : Renderer<N, E> {

    companion object {
        private val log = LoggerFactory.getLogger(BasicRenderer::class.java)
    }

    protected var _nodeRenderer: Renderer.Node<N, E> = BasicNodeRenderer()
    protected var _nodeLabelRenderer: Renderer.NodeLabel<N, E> = BasicNodeLabelRenderer()
    protected var _edgeRenderer: Renderer.Edge<N, E> = BasicEdgeRenderer()
    protected var _edgeLabelRenderer: Renderer.EdgeLabel<N, E> = BasicEdgeLabelRenderer()

    override fun render(
        rc: RenderContext<N, E>,
        visualizationModel: VisualizationModel<N, E>,
        nodeSpatial: Spatial<N>,
        edgeSpatial: Spatial<E>
    ) {
        val visibleNodes: Iterable<N>
        val visibleEdges: Iterable<E>

        try {
            visibleNodes = nodeSpatial.getVisibleElements(
                (rc.getScreenDevice() as VisualizationServer<*, *>).viewOnLayout()
            )

            visibleEdges = if (edgeSpatial != null) {
                edgeSpatial.getVisibleElements(
                    (rc.getScreenDevice() as VisualizationServer<*, *>).viewOnLayout()
                )
            } else {
                visualizationModel.getNetwork().edges()
            }
        } catch (ex: ConcurrentModificationException) {
            // skip rendering until graph node index is stable,
            // this can happen if the layout relax thread is changing locations while the
            // visualization is rendering
            log.info("got {} so returning", ex)
            return
        }

        try {
            val network = visualizationModel.getNetwork()
            // paint all the edges
            log.trace("the visibleEdges are {}", visibleEdges)
            for (e in visibleEdges) {
                if (network.edges().contains(e)) {
                    renderEdge(rc, visualizationModel, e)
                    renderEdgeLabel(rc, visualizationModel, e)
                }
            }
        } catch (cme: ConcurrentModificationException) {
            rc.getScreenDevice().repaint()
        }

        // paint all the nodes
        try {
            log.trace("the visibleNodes are {}", visibleNodes)
            for (v in visibleNodes) {
                renderNode(rc, visualizationModel, v)
                renderNodeLabel(rc, visualizationModel, v)
            }
        } catch (cme: ConcurrentModificationException) {
            rc.getScreenDevice().repaint()
        }
    }

    override fun render(
        renderContext: RenderContext<N, E>,
        visualizationModel: VisualizationModel<N, E>
    ) {
        val network = visualizationModel.getNetwork()
        // paint all the edges
        try {
            for (e in network.edges() as Iterable<E>) {
                renderEdge(renderContext, visualizationModel, e)
                renderEdgeLabel(renderContext, visualizationModel, e)
            }
        } catch (cme: ConcurrentModificationException) {
            renderContext.getScreenDevice().repaint()
        }

        // paint all the nodes
        try {
            for (v in network.nodes() as Iterable<N>) {
                renderNode(renderContext, visualizationModel, v)
                renderNodeLabel(renderContext, visualizationModel, v)
            }
        } catch (cme: ConcurrentModificationException) {
            renderContext.getScreenDevice().repaint()
        }
    }

    override fun renderNode(
        renderContext: RenderContext<N, E>,
        visualizationModel: VisualizationModel<N, E>,
        v: N
    ) {
        _nodeRenderer.paintNode(renderContext, visualizationModel, v)
    }

    override fun renderNodeLabel(
        renderContext: RenderContext<N, E>,
        visualizationModel: VisualizationModel<N, E>,
        v: N
    ) {
        _nodeLabelRenderer.labelNode(
            renderContext, visualizationModel, v, renderContext.getNodeLabelFunction().apply(v)
        )
    }

    override fun renderEdge(
        renderContext: RenderContext<N, E>,
        visualizationModel: VisualizationModel<N, E>,
        e: E
    ) {
        _edgeRenderer.paintEdge(renderContext, visualizationModel, e)
    }

    override fun renderEdgeLabel(
        renderContext: RenderContext<N, E>,
        visualizationModel: VisualizationModel<N, E>,
        e: E
    ) {
        _edgeLabelRenderer.labelEdge(
            renderContext, visualizationModel, e, renderContext.getEdgeLabelFunction().apply(e)
        )
    }

    override fun setNodeRenderer(r: Renderer.Node<N, E>) {
        this._nodeRenderer = r
    }

    override fun setEdgeRenderer(r: Renderer.Edge<N, E>) {
        this._edgeRenderer = r
    }

    override fun getEdgeLabelRenderer(): Renderer.EdgeLabel<N, E> = _edgeLabelRenderer

    override fun setEdgeLabelRenderer(r: Renderer.EdgeLabel<N, E>) {
        this._edgeLabelRenderer = r
    }

    override fun getNodeLabelRenderer(): Renderer.NodeLabel<N, E> = _nodeLabelRenderer

    override fun setNodeLabelRenderer(r: Renderer.NodeLabel<N, E>) {
        this._nodeLabelRenderer = r
    }

    override fun getEdgeRenderer(): Renderer.Edge<N, E> = _edgeRenderer

    override fun getNodeRenderer(): Renderer.Node<N, E> = _nodeRenderer
}
