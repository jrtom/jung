package edu.uci.ics.jung.visualization.control

import edu.uci.ics.jung.visualization.BasicVisualizationServer
import java.awt.geom.Point2D

/**
 * interface to support the creation of new edges by the EditingGraphMousePlugin SimpleEdgeSupport
 * is a sample implementation
 *
 * @author Tom Nelson
 * @param N the node type
 * @param E the edge type
 */
interface EdgeSupport<N : Any, E : Any> {

    fun startEdgeCreate(vv: BasicVisualizationServer<N, E>, startNode: N, startPoint: Point2D)

    fun midEdgeCreate(vv: BasicVisualizationServer<N, E>, midPoint: Point2D)

    fun endEdgeCreate(vv: BasicVisualizationServer<N, E>, endNode: N)

    fun abort(vv: BasicVisualizationServer<N, E>)
}
