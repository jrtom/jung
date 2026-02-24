/*
 * Created on Sep 24, 2005
 *
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.util

import com.google.common.graph.Network

/**
 * An interface for a service to access the index of a given edge (in a given `Network`) into
 * the set formed by the given edge and all the other edges it is parallel to.
 *
 * This index is assumed to be an integer value in the interval [0,n-1], where n-1 is the number
 * of edges parallel to `e`.
 *
 * @author Tom Nelson
 */
interface EdgeIndexFunction<N, E> {

    /**
     * The index of `e` is defined as its position in some consistent ordering of `e`
     * and all edges parallel to `e`.
     *
     * @param context the network and the edge whose index is to be queried
     * @return `edge`'s index in this instance's `Network`.
     */
    fun getIndex(context: Context<Network<N, E>, E>): Int

    /**
     * Resets the indices for `edge` and its parallel edges. Should be invoked when an edge
     * parallel to `edge` has been added or removed.
     *
     * @param context the network and the edge whose index is to be reset
     */
    fun reset(context: Context<Network<N, E>, E>)

    /** Clears all edge indices for all edges. Does not recalculate the indices. */
    fun reset()
}
