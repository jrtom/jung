package edu.uci.ics.jung.visualization.control

import edu.uci.ics.jung.visualization.BasicVisualizationServer
import java.awt.geom.Point2D

interface EdgeEffects<N : Any, E : Any> {

    fun startEdgeEffects(vv: BasicVisualizationServer<N, E>, down: Point2D, out: Point2D)

    fun midEdgeEffects(vv: BasicVisualizationServer<N, E>, down: Point2D, out: Point2D)

    fun endEdgeEffects(vv: BasicVisualizationServer<N, E>)

    fun startArrowEffects(vv: BasicVisualizationServer<N, E>, down: Point2D, out: Point2D)

    fun midArrowEffects(vv: BasicVisualizationServer<N, E>, down: Point2D, out: Point2D)

    fun endArrowEffects(vv: BasicVisualizationServer<N, E>)
}
