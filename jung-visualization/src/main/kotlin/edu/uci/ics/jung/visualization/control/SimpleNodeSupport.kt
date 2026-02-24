package edu.uci.ics.jung.visualization.control

import com.google.common.base.Preconditions
import com.google.common.graph.MutableNetwork
import edu.uci.ics.jung.visualization.BasicVisualizationServer
import java.awt.geom.Point2D
import java.util.function.Supplier

/**
 * sample implementation showing how to use the NodeSupport interface member of the
 * EditingGraphMousePlugin. override midNodeCreate and endNodeCreate for more elaborate
 * implementations
 *
 * @author Tom Nelson
 * @param N the node type
 * @param E the edge type
 */
open class SimpleNodeSupport<N : Any, E : Any>(
    var nodeFactory: Supplier<N>
) : NodeSupport<N, E> {

    @Suppress("UNCHECKED_CAST")
    override fun startNodeCreate(vv: BasicVisualizationServer<N, E>, point: Point2D) {
        Preconditions.checkState(
            vv.getModel().getNetwork() is MutableNetwork<*, *>, "graph must be mutable"
        )
        val newNode = nodeFactory.get()
        val visualizationModel = vv.getModel()
        val graph = visualizationModel.getNetwork() as MutableNetwork<N, E>
        graph.addNode(newNode)
        val p2d = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(point)
        visualizationModel.getLayoutModel().set(newNode, p2d.x, p2d.y)
        vv.repaint()
    }

    override fun midNodeCreate(vv: BasicVisualizationServer<N, E>, point: Point2D) {
        // noop
    }

    override fun endNodeCreate(vv: BasicVisualizationServer<N, E>, point: Point2D) {
        // noop
    }
}
