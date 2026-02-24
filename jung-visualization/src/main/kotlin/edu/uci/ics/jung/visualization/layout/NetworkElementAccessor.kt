/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 *
 * Created on Apr 12, 2005
 */
package edu.uci.ics.jung.visualization.layout

import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.util.NetworkNodeAccessor
import java.awt.Shape
import java.awt.geom.Point2D

/**
 * Interface for coordinate-based selection of graph components.
 *
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */

/**
 * interface for support for node information about Networks (nodes and edges).
 *
 * @param N
 * @param E
 */
interface NetworkElementAccessor<N : Any, E> : NetworkNodeAccessor<N> {

    /**
     * @param rectangle the region in which the returned nodes are located
     * @return the nodes whose locations given by `layout` are contained within `rectangle`
     */
    fun getNodes(layoutModel: LayoutModel<N>, rectangle: Shape): Collection<N>

    /**
     * @param x the x coordinate of the location
     * @param y the y coordinate of the location
     * @return an edge which is associated with the location `(x,y)` as given by `layout`,
     *     generally by reference to the edge's endpoints
     */
    fun getEdge(layoutModel: LayoutModel<N>, x: Double, y: Double): E?

    /**
     * @param layoutModel
     * @param p the pick location
     * @return an edge associated with the pick location
     */
    fun getEdge(layoutModel: LayoutModel<N>, p: Point2D): E?
}
