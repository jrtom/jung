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

import com.google.common.graph.Network
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.util.RadiusNetworkNodeAccessor
import java.awt.Shape
import java.awt.geom.Point2D
import java.util.ConcurrentModificationException

/**
 * Simple implementation of PickSupport that returns the node or edge that is closest to the
 * specified location. This implementation provides the same picking options that were available in
 * previous versions of
 *
 * No element will be returned that is farther away than the specified maximum distance.
 *
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */
open class RadiusNetworkElementAccessor<N : Any, E> : RadiusNetworkNodeAccessor<N>,
    NetworkElementAccessor<N, E> {

    private val network: Network<N, E>

    /** Creates an instance with an effectively infinite default maximum distance. */
    constructor(network: Network<N, E>) : this(network, Math.sqrt(Double.MAX_VALUE - 1000))

    /**
     * Creates an instance with the specified default maximum distance.
     *
     * @param maxDistance the maximum distance at which any element can be from a specified location
     *     and still be returned
     */
    constructor(network: Network<N, E>, maxDistance: Double) : super(maxDistance) {
        this.network = network
    }

    /**
     * Gets the edge nearest to the point location selected, whose endpoints are < `maxDistance`.
     * Iterates through all visible edges and checks their distance from the location.
     * Override this method to provide a more efficient implementation.
     */
    override fun getEdge(layoutModel: LayoutModel<N>, p: Point2D): E? =
        getEdge(layoutModel, p.x, p.y)

    /**
     * Gets the edge nearest to the location of the (x,y) location selected, whose endpoints are <
     * `maxDistance`. Iterates through all visible nodes and checks their distance from the
     * location. Override this method to provide a more efficient implementation.
     *
     * @param x the x coordinate of the location
     * @param y the y coordinate of the location
     * @return an edge which is associated with the location `(x,y)` as given by `layout`
     */
    override fun getEdge(layoutModel: LayoutModel<N>, x: Double, y: Double): E? {
        var minDistance = maxDistance * maxDistance
        var closest: E? = null
        while (true) {
            try {
                for (edge in network.edges()) {
                    val endpoints = network.incidentNodes(edge)
                    val node1 = endpoints.nodeU()
                    val node2 = endpoints.nodeV()
                    // Get coords
                    val p1 = layoutModel.apply(node1)
                    val p2 = layoutModel.apply(node2)
                    val x1 = p1.x
                    val y1 = p1.y
                    val x2 = p2.x
                    val y2 = p2.y
                    // Calculate location on line closest to (x,y)
                    // First, check that v1 and v2 are not coincident.
                    if (x1 == x2 && y1 == y2) {
                        continue
                    }
                    val b = ((y - y1) * (y2 - y1) + (x - x1) * (x2 - x1)) /
                        ((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))

                    val distance2: Double // square of the distance
                    if (b <= 0) {
                        distance2 = (x - x1) * (x - x1) + (y - y1) * (y - y1)
                    } else if (b >= 1) {
                        distance2 = (x - x2) * (x - x2) + (y - y2) * (y - y2)
                    } else {
                        val x3 = x1 + b * (x2 - x1)
                        val y3 = y1 + b * (y2 - y1)
                        distance2 = (x - x3) * (x - x3) + (y - y3) * (y - y3)
                    }

                    if (distance2 < minDistance) {
                        minDistance = distance2
                        closest = edge
                    }
                }
                break
            } catch (cme: ConcurrentModificationException) {
                // retry
            }
        }
        return closest
    }

    override fun getNodes(layoutModel: LayoutModel<N>, rectangle: Shape): Set<N> {
        val pickedNodes = HashSet<N>()
        while (true) {
            try {
                for (node in layoutModel.graph.nodes()) {
                    val p = layoutModel.apply(node)
                    if (rectangle.contains(p.x, p.y)) {
                        pickedNodes.add(node)
                    }
                }
                break
            } catch (cme: ConcurrentModificationException) {
                // retry
            }
        }
        return pickedNodes
    }
}
