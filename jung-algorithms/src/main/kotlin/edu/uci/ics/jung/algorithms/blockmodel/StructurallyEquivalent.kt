/*
 * Copyright (c) 2004, The JUNG Authors
 *
 * All rights reserved.
 * Created on Jan 28, 2004
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.blockmodel

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.google.common.graph.Graph
import java.util.function.Function

/**
 * Identifies sets of structurally equivalent nodes in a graph. Nodes *i* and *j* are
 * structurally equivalent iff the set of *i*'s neighbors is identical to the set of *j*'s
 * neighbors, with the exception of *i* and *j* themselves. This algorithm finds all sets
 * of equivalent nodes in O(V^2) time.
 *
 * You can extend this class to have a different definition of equivalence (by overriding
 * `isStructurallyEquivalent`), and may give it hints for accelerating the process by
 * overriding `canBeEquivalent`. (For example, in a bipartite graph,
 * `canBeEquivalent` may return `false` for nodes in different partitions. This
 * function should be fast.)
 *
 * @author Danyel Fisher
 */
open class StructurallyEquivalent<N : Any> : Function<Graph<N>, NodePartition<N>> {

    override fun apply(g: Graph<N>): NodePartition<N> {
        val nodePairs = getEquivalentPairs(g)

        val rv = HashSet<Set<N>>()
        val intermediate = HashMap<N, MutableSet<N>>()
        for (pair in nodePairs) {
            var res = intermediate[pair[0]]
            if (res == null) {
                res = intermediate[pair[1]]
            }
            if (res == null) { // we haven't seen this one before
                res = HashSet()
            }
            res.add(pair[0])
            res.add(pair[1])
            intermediate[pair[0]] = res
            intermediate[pair[1]] = res
        }
        rv.addAll(intermediate.values)

        // pick up the nodes which don't appear in intermediate; they are
        // singletons (equivalence classes of size 1)
        val singletons = ArrayList(g.nodes())
        singletons.removeAll(intermediate.keys)
        for (v in singletons) {
            val vSet = setOf(v)
            intermediate[v] = vSet.toMutableSet()
            rv.add(vSet)
        }

        return NodePartition(g, intermediate.mapValues { it.value as Set<N> }, rv)
    }

    /**
     * For each node pair v, v1 in G, checks whether v and v1 are fully equivalent: meaning that they
     * connect to the exact same nodes. (Is this regular equivalence, or whathaveyou?)
     *
     * @param g the graph whose equivalent pairs are to be generated
     * @return an immutable set of pairs of nodes, where all pairs are represented as immutable lists,
     *     and the nodes in the inner pairs are equivalent.
     */
    protected open fun getEquivalentPairs(g: Graph<N>): ImmutableSet<ImmutableList<N>> {
        val rv = ImmutableSet.builder<ImmutableList<N>>()
        val alreadyEquivalent = HashSet<N>()

        val l = ArrayList(g.nodes())

        for (v1 in l) {
            if (alreadyEquivalent.contains(v1)) {
                continue
            }

            val iterator = l.listIterator(l.indexOf(v1) + 1)
            while (iterator.hasNext()) {
                val v2 = iterator.next()

                if (alreadyEquivalent.contains(v2)) {
                    continue
                }

                if (!canBeEquivalent(v1, v2)) {
                    continue
                }

                if (isStructurallyEquivalent(g, v1, v2)) {
                    val pair = ImmutableList.of(v1, v2)
                    alreadyEquivalent.add(v2)
                    rv.add(pair)
                }
            }
        }

        return rv.build()
    }

    /**
     * @param g the graph in which the structural equivalence comparison is to take place
     * @param v1 the node to check for structural equivalence to v2
     * @param v2 the node to check for structural equivalence to v1
     * @return `true` if `v1`'s predecessors/successors are equal to `v2`'s
     *     predecessors/successors
     */
    protected open fun isStructurallyEquivalent(g: Graph<N>, v1: N, v2: N): Boolean {
        if (g.degree(v1) != g.degree(v2)) {
            return false
        }

        val n1 = HashSet(g.predecessors(v1))
        n1.remove(v2)
        n1.remove(v1)
        val n2 = HashSet(g.predecessors(v2))
        n2.remove(v1)
        n2.remove(v2)

        val o1 = HashSet(g.successors(v1))
        val o2 = HashSet(g.successors(v2))
        o1.remove(v1)
        o1.remove(v2)
        o2.remove(v1)
        o2.remove(v2)

        // this neglects self-loops and directed edges from 1 to other
        var b = n1 == n2 && o1 == o2
        if (!b) {
            return b
        }

        // if there's a directed edge v1->v2 then there's a directed edge v2->v1
        b = b && (g.successors(v1).contains(v2) == g.successors(v2).contains(v1))

        // self-loop check
        b = b && (g.successors(v1).contains(v1) == g.successors(v2).contains(v2))

        return b
    }

    /**
     * This is a space for optimizations. For example, for a bipartite graph, nodes from different
     * partitions cannot possibly be equivalent.
     *
     * @param v1 the first node to compare
     * @param v2 the second node to compare
     * @return `true` if the nodes can be equivalent
     */
    protected open fun canBeEquivalent(v1: N, v2: N): Boolean = true
}
