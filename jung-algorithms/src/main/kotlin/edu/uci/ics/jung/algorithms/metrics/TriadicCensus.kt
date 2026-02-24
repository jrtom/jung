/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.metrics

import com.google.common.base.Preconditions
import com.google.common.graph.Graph

/**
 * TriadicCensus is a standard social network tool that counts, for each of the different possible
 * configurations of three nodes, the number of times that that configuration occurs in the given
 * graph. This may then be compared to the set of expected counts for this particular graph or to an
 * expected sample. This is often used in p* modeling.
 *
 * To use this class:
 *
 * ```
 * long[] triad_counts = TriadicCensus.getCounts(dg);
 * ```
 *
 * You can also access it through `Metrics.triadicCensus(dg)`.
 *
 * where `dg` is a directed `Graph`. ith element of the array (for i in [1,16])
 * is the number of occurrences of the corresponding triad type. (The 0th element is not meaningful;
 * this array is effectively 1-based.) The names of the triads are encoded in this class's constant
 * array `TRIAD_NAMES`.
 *
 * Triads are named as (number of pairs that are mutually tied) (number of pairs that are one-way
 * tied) (number of non-tied pairs) in the triple. Since there are be only three pairs, there is a
 * finite set of these possible triads.
 *
 * In fact, there are exactly 16, conventionally sorted by the number of realized edges in the
 * triad:
 *
 * | Number | Configuration | Notes |
 * |--------|---------------|-------|
 * | 1 | 003 | The empty triad |
 * | 2 | 012 | |
 * | 3 | 102 | |
 * | 4 | 021D | "Down": the directed edges point away |
 * | 5 | 021U | "Up": the directed edges meet |
 * | 6 | 021C | "Circle": one in, one out |
 * | 7 | 111D | "Down": 021D but one edge is mutual |
 * | 8 | 111U | "Up": 021U but one edge is mutual |
 * | 9 | 030T | "Transitive": two point to the same node |
 * | 10 | 030C | "Circle": A->B->C->A |
 * | 11 | 201 | |
 * | 12 | 120D | "Down": 021D but the third edge is mutual |
 * | 13 | 120U | "Up": 021U but the third edge is mutual |
 * | 14 | 120C | "Circle": 021C but the third edge is mutual |
 * | 15 | 210 | |
 * | 16 | 300 | The complete triad |
 *
 * This implementation takes O(m) time, where `m` is the number of edges in the graph.
 * It is based on the paper published in Social Networks (23 (3), 237-243, 2001) by Vladimir
 * Batagelj and Andrej Mrvar, University of Ljubljana.
 *
 * @author Danyel Fisher
 * @author Tom Nelson - converted to jung2
 * @see [A subquadratic triad census algorithm for large sparse networks with small maximum degree](https://www.sciencedirect.com/science/article/abs/pii/S0378873301000351)
 */
object TriadicCensus {

    // and their types
    @JvmField
    val TRIAD_NAMES = arrayOf(
        "N/A", "003", "012", "102", "021D", "021U", "021C", "111D", "111U", "030T", "030C", "201",
        "120D", "120U", "120C", "210", "300",
    )

    @JvmField
    val MAX_TRIADS = TRIAD_NAMES.size

    /**
     * Returns an array whose ith element (for i in [1,16]) is the number of occurrences of the
     * corresponding triad type in `g`. (The 0th element is not meaningful; this array is
     * effectively 1-based.)
     *
     * @param g the graph whose properties are being measured
     * @param N the node type
     * @return an array encoding the number of occurrences of each triad type
     */
    @JvmStatic
    fun <N : Any> getCounts(g: Graph<N>): LongArray {
        Preconditions.checkArgument(g.isDirected, "input graph must be directed")
        val count = LongArray(MAX_TRIADS)

        val id = ArrayList(g.nodes())

        // apply algorithm to each edge, one at at time
        for (iV in id.indices) {
            val v = id[iV]
            for (u in g.adjacentNodes(v)) {
                val triType: Int
                if (id.indexOf(u) <= iV) {
                    continue
                }
                val neighbors = HashSet(g.adjacentNodes(u))
                neighbors.addAll(g.adjacentNodes(v))
                neighbors.remove(u)
                neighbors.remove(v)
                triType = if (g.hasEdgeConnecting(v, u) && g.hasEdgeConnecting(u, v)) {
                    3
                } else {
                    2
                }
                count[triType] += (id.size - neighbors.size - 2).toLong()
                for (w in neighbors) {
                    if (shouldCount(g, id, u, v, w)) {
                        count[triType(triCode(g, u, v, w))]++
                    }
                }
            }
        }
        var sum = 0L
        for (i in 2..16) {
            sum += count[i]
        }
        val n = id.size
        count[1] = n.toLong() * (n - 1) * (n - 2) / 6 - sum
        return count
    }

    /**
     * This is the core of the technique in the paper. Returns an int from 0 to 63 which encodes the
     * presence of all possible links between u, v, and w as bit flags: WU = 32, UW = 16, WV = 8,
     * VW = 4, UV = 2, VU = 1
     *
     * @param g the graph for which the calculation is being made
     * @param u a node in g
     * @param v a node in g
     * @param w a node in g
     * @param N the node type
     * @return an int encoding the presence of all links between u, v, and w
     */
    @JvmStatic
    fun <N : Any> triCode(g: Graph<N>, u: N, v: N, w: N): Int {
        var i = 0
        i += if (link(g, v, u)) 1 else 0
        i += if (link(g, u, v)) 2 else 0
        i += if (link(g, v, w)) 4 else 0
        i += if (link(g, w, v)) 8 else 0
        i += if (link(g, u, w)) 16 else 0
        i += if (link(g, w, u)) 32 else 0
        return i
    }

    @JvmStatic
    internal fun <N : Any> link(g: Graph<N>, a: N, b: N): Boolean =
        g.predecessors(b).contains(a)

    /**
     * @param triCode the code returned by `triCode()`
     * @return the string code associated with the numeric type
     */
    @JvmStatic
    fun triType(triCode: Int): Int = codeToType[triCode]

    /**
     * For debugging purposes, this is copied straight out of the paper which means that they refer to
     * triad types 1-16.
     */
    @JvmStatic
    internal val codeToType = intArrayOf(
        1, 2, 2, 3, 2, 4, 6, 8, 2, 6, 5, 7, 3, 8, 7, 11, 2, 6, 4, 8, 5, 9, 9, 13, 6, 10, 9, 14,
        7, 14, 12, 15, 2, 5, 6, 7, 6, 9, 10, 14, 4, 9, 9, 12, 8, 13, 14, 15, 3, 7, 8, 11, 7, 12,
        14, 15, 8, 14, 13, 15, 11, 15, 15, 16,
    )

    /**
     * Return true iff this ordering is canonical and therefore we should build statistics for it.
     *
     * @param g the graph whose properties are being examined
     * @param id a list of the nodes in g; used to assign an index to each
     * @param u a node in g
     * @param v a node in g
     * @param w a node in g
     * @param N the node type
     * @return true if index(u) < index(w), or if index(v) < index(w) < index(u) and v
     *     doesn't link to w; false otherwise
     */
    @JvmStatic
    internal fun <N : Any> shouldCount(g: Graph<N>, id: List<N>, u: N, v: N, w: N): Boolean {
        val iU = id.indexOf(u)
        val iW = id.indexOf(w)
        if (iU < iW) {
            return true
        }
        val iV = id.indexOf(v)
        if (iV < iW && iW < iU && !g.adjacentNodes(w).contains(v)) {
            return true
        }
        return false
    }
}
