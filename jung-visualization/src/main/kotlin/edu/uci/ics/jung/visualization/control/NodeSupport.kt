package edu.uci.ics.jung.visualization.control

import edu.uci.ics.jung.visualization.BasicVisualizationServer
import java.awt.geom.Point2D

/**
 * interface to support the creation of new nodes by the EditingGraphMousePlugin. SimpleNodeSupport
 * is a sample implementation.
 *
 * @author Tom Nelson
 * @param N the node type
 */
interface NodeSupport<N : Any, E : Any> {

    fun startNodeCreate(vv: BasicVisualizationServer<N, E>, point: Point2D)

    fun midNodeCreate(vv: BasicVisualizationServer<N, E>, point: Point2D)

    fun endNodeCreate(vv: BasicVisualizationServer<N, E>, point: Point2D)
}
