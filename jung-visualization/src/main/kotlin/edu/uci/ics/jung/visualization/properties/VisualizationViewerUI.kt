package edu.uci.ics.jung.visualization.properties

import com.google.common.graph.Network
import edu.uci.ics.jung.visualization.RenderContext
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.decorators.EdgeShape
import edu.uci.ics.jung.visualization.util.Context
import java.util.function.Function
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction
import edu.uci.ics.jung.visualization.layout.BoundingRectangleCollector
import edu.uci.ics.jung.visualization.renderers.Renderer
import edu.uci.ics.jung.visualization.spatial.Spatial
import edu.uci.ics.jung.visualization.spatial.SpatialGrid
import edu.uci.ics.jung.visualization.spatial.SpatialQuadTree
import edu.uci.ics.jung.visualization.spatial.SpatialRTree
import edu.uci.ics.jung.visualization.spatial.rtree.QuadraticLeafSplitter
import edu.uci.ics.jung.visualization.spatial.rtree.QuadraticSplitter
import edu.uci.ics.jung.visualization.spatial.rtree.RStarLeafSplitter
import edu.uci.ics.jung.visualization.spatial.rtree.RStarSplitter
import edu.uci.ics.jung.visualization.spatial.rtree.SplitterContext
import java.awt.Color
import java.awt.Shape
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D
import java.io.IOException
import org.slf4j.LoggerFactory

open class VisualizationViewerUI<N : Any, E : Any> internal constructor(
    private val vv: VisualizationServer<N, E>
) {

    private fun loadFromAppName(): Boolean {
        try {
            val launchProgram = System.getProperty("sun.java.command")
            if (launchProgram != null && launchProgram.isNotEmpty()) {
                val resourceName = "/" + launchProgram.substring(launchProgram.lastIndexOf('.') + 1) + ".properties"
                val stream = javaClass.getResourceAsStream(resourceName)
                System.getProperties().load(stream)
                return true
            }
        } catch (ex: Exception) {
        }
        return false
    }

    private fun loadFromDefault(): Boolean {
        try {
            val stream = javaClass.getResourceAsStream("/$PROPERTIES_FILE_NAME")
            System.getProperties().load(stream)
            return true
        } catch (ex: Exception) {
        }
        return false
    }

    /**
     * Parse the properties file and set values or defaults.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun parse() {
        if (loadFromAppName() || loadFromDefault()) {
            val rc = vv.getRenderContext()

            setEdgeShape(System.getProperty(EDGE_SHAPE, "QUAD_CURVE"))

            rc.setNodeFillPaintFunction(
                PickableNodePaintFunction(
                    vv.getPickedNodeState(),
                    Color(Integer.getInteger(NODE_COLOR, 0xFF0000)),
                    Color(Integer.getInteger(PICKED_NODE_COLOR, 0x00FFFF))
                )
            )

            rc.setEdgeDrawPaintFunction(
                PickableEdgePaintFunction(
                    vv.getPickedEdgeState(),
                    Color(Integer.getInteger(EDGE_COLOR, 0xFF0000)),
                    Color(Integer.getInteger(PICKED_EDGE_COLOR, 0x00FFFF))
                )
            )

            rc.setNodeLabelDrawPaintFunction { _: N -> Color(Integer.getInteger(NODE_LABEL_COLOR, 0x000000)) }

            val size = Integer.getInteger(NODE_SIZE, 12)

            vv.getRenderContext().setNodeShapeFunction { _: N -> getNodeShape(System.getProperty(NODE_SHAPE, "CIRCLE"), size) }

            // only set if the property is requested
            if (System.getProperty(NODE_SPATIAL_SUPPORT) != null) {
                val spatial = createNodeSpatial(vv)
                if (spatial != null) {
                    vv.setNodeSpatial(spatial)
                }
            }
            // only set if the property is requested
            if (System.getProperty(EDGE_SPATIAL_SUPPORT) != null) {
                val spatial = createEdgeSpatial(vv)
                if (spatial != null) {
                    vv.setEdgeSpatial(spatial)
                }
            }

            vv.getRenderer()
                .getNodeLabelRenderer()
                .setPosition(getPosition(System.getProperty(NODE_LABEL_POSITION, "SE")))
        }
    }

    private fun getNodeShape(shape: String, size: Int): Shape {
        return when (shape) {
            "SQUARE" -> Rectangle2D.Float(-size / 2.0f, -size / 2.0f, size.toFloat(), size.toFloat())
            else -> Ellipse2D.Float(-size / 2.0f, -size / 2.0f, size.toFloat(), size.toFloat())
        }
    }

    /**
     * Parse out the node label position.
     */
    private fun getPosition(position: String): Renderer.NodeLabel.Position {
        try {
            return Renderer.NodeLabel.Position.valueOf(position)
        } catch (e: Exception) {
        }
        return Renderer.NodeLabel.Position.SE
    }

    /**
     * Parse out the edge shape.
     */
    @Suppress("UNCHECKED_CAST")
    private fun setEdgeShape(edgeShape: String) {
        val rc = vv.getRenderContext()
        when (edgeShape) {
            "LINE" -> rc.setEdgeShapeFunction(EdgeShape.line<N, E>())
            "CUBIC_CURVE" -> rc.setEdgeShapeFunction(EdgeShape.cubicCurve<E>() as Function<Context<Network<N, E>, E>, Shape>)
            "ORTHOGONAL" -> rc.setEdgeShapeFunction(EdgeShape.orthogonal<E>() as Function<Context<Network<N, E>, E>, Shape>)
            "WEDGE" -> rc.setEdgeShapeFunction(EdgeShape.wedge<E>(10) as Function<Context<Network<N, E>, E>, Shape>)
            else -> rc.setEdgeShapeFunction(EdgeShape.quadCurve<E>() as Function<Context<Network<N, E>, E>, Shape>)
        }
    }

    private fun getNodeSpatialSupportPreference(): VisualizationModel.SpatialSupport {
        val spatialSupportProperty = System.getProperty(NODE_SPATIAL_SUPPORT, "RTREE")
        try {
            return VisualizationModel.SpatialSupport.valueOf(spatialSupportProperty)
        } catch (ex: IllegalArgumentException) {
            // the user set an unknown name
            // issue a warning because unlike colors and shapes, it is not immediately obvious what
            // spatial support is being used
            log.warn("Unknown ModelStructure type {} ignored.", spatialSupportProperty)
        }
        return VisualizationModel.SpatialSupport.QUADTREE
    }

    private fun getEdgeSpatialSupportPreference(): VisualizationModel.SpatialSupport {
        val spatialSupportProperty = System.getProperty(EDGE_SPATIAL_SUPPORT, "RTREE")
        try {
            return VisualizationModel.SpatialSupport.valueOf(spatialSupportProperty)
        } catch (ex: IllegalArgumentException) {
            // the user set an unknown name
            // issue a warning because unlike colors and shapes, it is not immediately obvious what
            // spatial support is being used
            log.warn("Unknown ModelStructure type {} ignored.", spatialSupportProperty)
        }
        return VisualizationModel.SpatialSupport.NONE
    }

    private fun createNodeSpatial(visualizationServer: VisualizationServer<N, E>): Spatial<N>? {
        return when (getNodeSpatialSupportPreference()) {
            VisualizationModel.SpatialSupport.RTREE -> SpatialRTree.Nodes(
                visualizationServer.getModel(),
                BoundingRectangleCollector.Nodes(
                    visualizationServer.getRenderContext(), visualizationServer.getModel()
                ),
                SplitterContext.of(RStarLeafSplitter(), RStarSplitter())
            )
            VisualizationModel.SpatialSupport.GRID ->
                SpatialGrid(visualizationServer.getModel().getLayoutModel())
            VisualizationModel.SpatialSupport.QUADTREE ->
                SpatialQuadTree(visualizationServer.getModel().getLayoutModel())
            else ->
                Spatial.NoOp.Node(visualizationServer.getModel().getLayoutModel())
        }
    }

    private fun createEdgeSpatial(visualizationServer: VisualizationServer<N, E>): Spatial<E>? {
        return when (getEdgeSpatialSupportPreference()) {
            VisualizationModel.SpatialSupport.RTREE -> SpatialRTree.Edges(
                visualizationServer.getModel(),
                BoundingRectangleCollector.Edges(
                    visualizationServer.getRenderContext(), visualizationServer.getModel()
                ),
                SplitterContext.of(QuadraticLeafSplitter(), QuadraticSplitter())
            )
            else ->
                Spatial.NoOp.Edge(visualizationServer.getModel())
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(VisualizationViewerUI::class.java)

        private const val PROPERTIES_FILE_NAME = "jung.properties"

        private const val PREFIX = "jung."
        private const val NODE_SHAPE = PREFIX + "nodeShape"
        private const val NODE_SIZE = PREFIX + "nodeSize"
        private const val EDGE_SHAPE = PREFIX + "edgeShape"
        private const val NODE_COLOR = PREFIX + "nodeColor"
        private const val PICKED_NODE_COLOR = PREFIX + "pickedNodeColor"
        private const val EDGE_COLOR = PREFIX + "edgeColor"
        private const val PICKED_EDGE_COLOR = PREFIX + "pickedEdgeColor"
        private const val ARROW_STYLE = PREFIX + "arrowStyle"
        private const val NODE_SPATIAL_SUPPORT = PREFIX + "nodeSpatialSupport"
        private const val EDGE_SPATIAL_SUPPORT = PREFIX + "edgeSpatialSupport"
        private const val NODE_LABEL_POSITION = PREFIX + "nodeLabelPosition"
        private const val NODE_LABEL_COLOR = PREFIX + "nodeLabelColor"

        @JvmStatic
        fun <N : Any, E : Any> getInstance(vv: VisualizationServer<N, E>): VisualizationViewerUI<N, E> =
            VisualizationViewerUI(vv)
    }
}
