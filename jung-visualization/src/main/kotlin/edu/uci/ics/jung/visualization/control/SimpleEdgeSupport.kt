package edu.uci.ics.jung.visualization.control

import com.google.common.base.Preconditions
import com.google.common.graph.MutableNetwork
import edu.uci.ics.jung.visualization.BasicVisualizationServer
import java.awt.geom.Point2D
import java.util.function.Supplier

open class SimpleEdgeSupport<N : Any, E : Any>(
    var edgeFactory: Supplier<E>
) : EdgeSupport<N, E> {

    protected var down: Point2D? = null
    var edgeEffects: EdgeEffects<N, E> = CubicCurveEdgeEffects()
    protected var startNode: N? = null

    override fun startEdgeCreate(vv: BasicVisualizationServer<N, E>, startNode: N, startPoint: Point2D) {
        this.startNode = startNode
        this.down = startPoint
        this.edgeEffects.startEdgeEffects(vv, startPoint, startPoint)
        if (vv.getModel().getNetwork().isDirected) {
            this.edgeEffects.startArrowEffects(vv, startPoint, startPoint)
        }
        vv.repaint()
    }

    override fun midEdgeCreate(vv: BasicVisualizationServer<N, E>, midPoint: Point2D) {
        if (startNode != null) {
            this.edgeEffects.midEdgeEffects(vv, down!!, midPoint)
            if (vv.getModel().getNetwork().isDirected) {
                this.edgeEffects.midArrowEffects(vv, down!!, midPoint)
            }
            vv.repaint()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun endEdgeCreate(vv: BasicVisualizationServer<N, E>, endNode: N) {
        Preconditions.checkState(
            vv.getModel().getNetwork() is MutableNetwork<*, *>, "graph must be mutable"
        )
        if (startNode != null) {
            val graph = vv.getModel().getNetwork() as MutableNetwork<N, E>
            graph.addEdge(startNode!!, endNode, edgeFactory.get())
            vv.getEdgeSpatial().recalculate()
            vv.repaint()
        }
        startNode = null
        edgeEffects.endEdgeEffects(vv)
        edgeEffects.endArrowEffects(vv)
    }

    override fun abort(vv: BasicVisualizationServer<N, E>) {
        startNode = null
        edgeEffects.endEdgeEffects(vv)
        edgeEffects.endArrowEffects(vv)
        vv.repaint()
    }
}
