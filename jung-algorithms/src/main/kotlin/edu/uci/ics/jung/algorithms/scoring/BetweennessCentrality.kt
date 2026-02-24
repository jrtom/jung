/*
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Sep 16, 2008
 */
package edu.uci.ics.jung.algorithms.scoring

import com.google.common.base.Preconditions
import com.google.common.graph.Network
import edu.uci.ics.jung.algorithms.util.MapBinaryHeap
import java.util.ArrayDeque
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.LinkedList
import java.util.Queue
import java.util.function.Function

/**
 * Computes betweenness centrality for each node and edge in the graph.
 *
 * @see "Ulrik Brandes: A Faster Algorithm for Betweenness Centrality. Journal of Mathematical
 *     Sociology 25(2):163-177, 2001."
 */
class BetweennessCentrality<N : Any, E : Any> : NodeScorer<N, Double>, EdgeScorer<E, Double> {
  protected lateinit var graph: Network<N, E>
  protected var node_scores: MutableMap<N, Double> = HashMap()
  protected var edge_scores: MutableMap<E, Double> = HashMap()
  internal var node_data: MutableMap<N, BetweennessData> = HashMap()

  /**
   * Calculates betweenness scores based on the all-pairs unweighted shortest paths in the graph.
   *
   * @param graph the graph for which the scores are to be calculated
   */
  constructor(graph: Network<N, E>) {
    node_scores = HashMap()
    edge_scores = HashMap()
    node_data = HashMap()
    initialize(graph)
    computeBetweenness(LinkedList(), Function { 1 })
  }

  /**
   * Calculates betweenness scores based on the all-pairs weighted shortest paths in the graph.
   *
   * NOTE: This version of the algorithm may not work correctly on all graphs; we're still
   * working out the bugs. Use at your own risk.
   *
   * @param graph the graph for which the scores are to be calculated
   * @param edge_weights the edge weights to be used in the path length calculations
   */
  constructor(graph: Network<N, E>, edge_weights: Function<in E, out Number>) {
    // reject negative-weight edges up front
    for (e in graph.edges()) {
      val e_weight = edge_weights.apply(e).toDouble()
      Preconditions.checkArgument(e_weight >= 0, "Weight for edge '%s' is < 0: %d", e, e_weight)
    }

    node_scores = HashMap()
    edge_scores = HashMap()
    node_data = HashMap()
    initialize(graph)
    computeBetweenness(
      MapBinaryHeap { v1, v2 ->
        java.lang.Double.compare(node_data[v1]!!.distance, node_data[v2]!!.distance)
      },
      edge_weights
    )
  }

  protected fun initialize(graph: Network<N, E>) {
    this.graph = graph
    this.node_scores = HashMap()
    this.edge_scores = HashMap()
    this.node_data = HashMap()

    for (v in graph.nodes()) {
      this.node_scores[v] = 0.0
    }

    for (e in graph.edges()) {
      this.edge_scores[e] = 0.0
    }
  }

  protected fun computeBetweenness(
    queue: Queue<N>,
    edge_weights: Function<in E, out Number>
  ) {
    for (v in graph.nodes()) {
      // initialize the betweenness data for this new node
      for (s in graph.nodes()) {
        this.node_data[s] = BetweennessData()
      }

      node_data[v]!!.numSPs = 1.0
      node_data[v]!!.distance = 0.0

      val stack = ArrayDeque<N>()
      queue.offer(v)

      while (!queue.isEmpty()) {
        val w = queue.poll()
        stack.push(w)
        val w_data = node_data[w]!!

        for (e in graph.outEdges(w)) {
          val x = graph.incidentNodes(e).adjacentNode(w)
          if (x == w) {
            continue
          }
          val wx_weight = edge_weights.apply(e).toDouble()

          val x_data = node_data[x]!!
          val x_potential_dist = w_data.distance + wx_weight

          if (x_data.distance < 0) {
            x_data.distance = x_potential_dist
            queue.offer(x)
          }

          // note:
          // (1) this can only happen with weighted edges
          // (2) x's SP count and incoming edges are updated below
          if (x_data.distance > x_potential_dist) {
            x_data.distance = x_potential_dist
            // invalidate previously identified incoming edges
            // (we have a new shortest path distance to x)
            x_data.incomingEdges.clear()
            // update x's position in queue
            @Suppress("UNCHECKED_CAST")
            (queue as MapBinaryHeap<N>).update(x)
          }
        }
        for (e in graph.outEdges(w)) {
          val x = graph.incidentNodes(e).adjacentNode(w)
          if (x == w) {
            continue
          }
          val e_weight = edge_weights.apply(e).toDouble()
          val x_data = node_data[x]!!
          val x_potential_dist = w_data.distance + e_weight
          if (x_data.distance == x_potential_dist) {
            x_data.numSPs += w_data.numSPs
            x_data.incomingEdges.add(e)
          }
        }
      }
      while (!stack.isEmpty()) {
        val x = stack.pop()

        for (e in node_data[x]!!.incomingEdges) {
          val w = graph.incidentNodes(e).adjacentNode(x)
          val partialDependency =
            node_data[w]!!.numSPs /
                node_data[x]!!.numSPs *
                (1.0 + node_data[x]!!.dependency)
          node_data[w]!!.dependency += partialDependency
          val e_score = edge_scores[e]!!
          edge_scores[e] = e_score + partialDependency
        }
        if (x != v) {
          val x_score = node_scores[x]!!
          node_scores[x] = x_score + node_data[x]!!.dependency
        }
      }
    }

    if (!graph.isDirected) {
      for (v in graph.nodes()) {
        val v_score = node_scores[v]!!
        node_scores[v] = v_score / 2.0
      }
      for (e in graph.edges()) {
        val e_score = edge_scores[e]!!
        edge_scores[e] = e_score / 2.0
      }
    }

    node_data.clear()
  }

  override fun getNodeScore(v: N): Double = node_scores[v]!!

  override fun getEdgeScore(e: E): Double = edge_scores[e]!!

  override fun nodeScores(): Map<N, Double> = Collections.unmodifiableMap(node_scores)

  override fun edgeScores(): Map<E, Double> = Collections.unmodifiableMap(edge_scores)

  internal inner class BetweennessData {
    var distance: Double = -1.0
    var numSPs: Double = 0.0
    val incomingEdges: MutableList<E> = ArrayList()
    var dependency: Double = 0.0

    override fun toString(): String =
      "[d:$distance, sp:$numSPs, p:$incomingEdges, d:$dependency]\n"
  }
}
