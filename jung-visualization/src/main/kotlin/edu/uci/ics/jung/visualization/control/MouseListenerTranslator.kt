/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
/*
 * Created on Feb 17, 2004
 */
package edu.uci.ics.jung.visualization.control

import edu.uci.ics.jung.visualization.VisualizationViewer
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.Point2D

/**
 * This class translates mouse clicks into node clicks
 *
 * @author danyelf
 */
open class MouseListenerTranslator<N : Any, E : Any>(
    private val gel: GraphMouseListener<N>,
    private val vv: VisualizationViewer<N, E>
) : MouseAdapter() {

    /**
     * Transform the point to the coordinate system in the VisualizationViewer, then use either
     * PickSuuport (if available) or Layout to find a Node
     *
     * @param point
     * @return
     */
    private fun getNode(point: Point2D): N? {
        // adjust for scale and offset in the VisualizationViewer
        val p = point
        val pickSupport = vv.getPickSupport()
        val layoutModel = vv.getModel().getLayoutModel()
        var v: N? = null
        if (pickSupport != null) {
            v = pickSupport.getNode(layoutModel, p.x, p.y)
        }
        return v
    }

    override fun mouseClicked(e: MouseEvent) {
        val v = getNode(e.point)
        if (v != null) {
            gel.graphClicked(v, e)
        }
    }

    override fun mousePressed(e: MouseEvent) {
        val v = getNode(e.point)
        if (v != null) {
            gel.graphPressed(v, e)
        }
    }

    override fun mouseReleased(e: MouseEvent) {
        val v = getNode(e.point)
        if (v != null) {
            gel.graphReleased(v, e)
        }
    }
}
