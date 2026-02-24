/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.visualization.control

import edu.uci.ics.jung.visualization.VisualizationServer
import java.awt.geom.Point2D

interface ScalingControl {

    /**
     * zoom the display in or out
     *
     * @param vv the VisualizationViewer
     * @param amount how much to adjust scale by
     * @param at where to adjust scale from
     */
    fun scale(vv: VisualizationServer<*, *>, amount: Float, at: Point2D)
}
