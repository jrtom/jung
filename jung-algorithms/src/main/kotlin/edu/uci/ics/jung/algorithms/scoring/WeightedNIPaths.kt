/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.scoring

import com.google.common.base.Preconditions
import com.google.common.graph.MutableNetwork
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedHashMap
import java.util.function.Supplier

/**
 * This algorithm measures the importance of nodes based upon both the number and length of disjoint
 * paths that lead to a given node from each of the nodes in the root set. Specifically the formula
 * for measuring the importance of a node is given by: I(t|R) = sum_i=1_|P(r,t)|_{alpha^|p_i|} where
 * alpha is the path decay coefficient, p_i is path i and P(r,t) is a set of maximum-sized
 * node-disjoint paths from r to t.
 *
 * This algorithm uses heuristic breadth-first search to try and find the maximum-sized set of
 * node-disjoint paths between two nodes. As such, it is not guaranteed to give exact answers.
 *
 * @author Scott White
 * @see "Algorithms for Estimating Relative Importance in Graphs by Scott White and Padhraic Smyth,
 *     2003"
 */
// TODO: versions for Graph/ValueGraph
// TODO: extend AbstractIterativeScorer and provide for iterating one step (depth) at a time?
// TODO: review this and make sure it's correctly implementing the algorithm
// TODO: this takes in a MutableNetwork and factories as a hack; there's got to be a better way;
// options include:
// (1) create a delegate class that pretends that the extra node/edge are there
// (2) refactor the internal logic so that we can emulate the presence of that node/edge
class WeightedNIPaths<N : Any, E : Any>(
  private val graph: MutableNetwork<N, E>,
  private val nodeFactory: Supplier<N>,
  private val edgeFactory: Supplier<E>,
  private val alpha: Double,
  private val maxDepth: Int,
  private val priors: Set<N>
) : NodeScorer<N, Double> {

  private val pathIndices: MutableMap<E, Number> = HashMap()
  private val roots: MutableMap<Any, N> = HashMap()
  private val pathsSeenMap: MutableMap<N, MutableSet<Number>> = HashMap()
  private val nodeScoresMap: MutableMap<N, Double> = LinkedHashMap()

  init {
    // TODO: is this actually restricted to only work on directed graphs?
    Preconditions.checkArgument(graph.isDirected, "Input graph must be directed")
    evaluate()
  }

  protected fun incrementRankScore(node: N, rankValue: Double) {
    nodeScoresMap.computeIfPresent(node) { _, value -> value + rankValue }
  }

  protected fun computeWeightedPathsFromSource(root: N, depth: Int) {
    var pathIdx = 1

    for (e in graph.outEdges(root)) {
      this.pathIndices[e] = pathIdx
      this.roots[e] = root
      newNodeEncountered(pathIdx, graph.incidentNodes(e).target(), root)
      pathIdx++
    }

    var edges: MutableList<E> = ArrayList()

    val virtualNode = nodeFactory.get()
    graph.addNode(virtualNode)
    val virtualSinkEdge = edgeFactory.get()

    graph.addEdge(virtualNode, root, virtualSinkEdge)
    edges.add(virtualSinkEdge)

    var currentDepth = 0
    while (currentDepth <= depth) {
      val currentWeight = Math.pow(alpha, -1.0 * currentDepth)
      for (currentEdge in edges) {
        incrementRankScore(graph.incidentNodes(currentEdge).target(), currentWeight)
      }

      if (currentDepth == depth || edges.size == 0) {
        break
      }

      val newEdges = ArrayList<E>()

      for (currentSourceEdge in edges) {
        val sourcePathIndex = this.pathIndices[currentSourceEdge]

        // from the currentSourceEdge, get its opposite end
        // then iterate over the out edges of that opposite end
        val newDestNode = graph.incidentNodes(currentSourceEdge).target()
        for (currentDestEdge in graph.outEdges(newDestNode)) {
          val destEdgeRoot = this.roots[currentDestEdge]
          val destEdgeDest = graph.incidentNodes(currentDestEdge).target()

          if (currentSourceEdge === virtualSinkEdge) {
            newEdges.add(currentDestEdge)
            continue
          }
          if (destEdgeRoot === root) {
            continue
          }
          if (destEdgeDest == graph.incidentNodes(currentSourceEdge).source()) {
            continue
          }
          val pathsSeen = this.pathsSeenMap[destEdgeDest]

          if (pathsSeen == null) {
            newNodeEncountered(sourcePathIndex!!.toInt(), destEdgeDest, root)
          } else if (roots[destEdgeDest] !== root) {
            roots[destEdgeDest] = root
            pathsSeen.clear()
            pathsSeen.add(sourcePathIndex!!)
          } else if (!pathsSeen.contains(sourcePathIndex)) {
            pathsSeen.add(sourcePathIndex!!)
          } else {
            continue
          }

          this.pathIndices[currentDestEdge] = sourcePathIndex!!
          this.roots[currentDestEdge] = root
          newEdges.add(currentDestEdge)
        }
      }

      edges = newEdges
      currentDepth++
    }

    graph.removeNode(virtualNode)
  }

  private fun newNodeEncountered(sourcePathIndex: Int, dest: N, root: N) {
    val pathsSeen = HashSet<Number>()
    pathsSeen.add(sourcePathIndex)
    this.pathsSeenMap[dest] = pathsSeen
    roots[dest] = root
  }

  private fun evaluate() {
    for (node in graph.nodes()) {
      nodeScoresMap[node] = 0.0
    }

    for (v in priors) {
      computeWeightedPathsFromSource(v, maxDepth)
    }

    var runningTotal = 0.0
    for (node in graph.nodes()) {
      runningTotal += nodeScoresMap[node]!!
    }

    val total = runningTotal
    for (node in graph.nodes()) {
      nodeScoresMap.computeIfPresent(node) { _, value -> value / total }
    }
  }

  override fun getNodeScore(v: N): Double = nodeScoresMap[v]!!

  override fun nodeScores(): Map<N, Double> = Collections.unmodifiableMap(nodeScoresMap)
}
