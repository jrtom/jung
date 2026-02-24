/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.shortestpath

import com.google.common.base.Preconditions
import com.google.common.graph.Graph
import java.util.Collections

/**
 * Labels each node in the graph according to the BFS distance from the start node(s). If nodes are
 * unreachable, then they are assigned a distance of -1. All nodes traversed at step k are marked as
 * predecessors of their successors traversed at step k+1.
 *
 * Running time is: O(m)
 *
 * @author Scott White
 */
// TODO: update or replace
class BFSDistanceLabeler<N : Any> {

  private val distanceDecorator = HashMap<N, Int>()
  private lateinit var mCurrentList: MutableList<N>
  private lateinit var mUnvisitedNodes: MutableSet<N>
  private lateinit var mNodesInOrderVisited: MutableList<N>
  private val mPredecessorMap = HashMap<N, HashSet<N>>()

  /**
   * Returns the list of nodes visited in order of traversal
   *
   * @return the list of nodes
   */
  val nodesInOrderVisited: List<N>
    get() = mNodesInOrderVisited

  /**
   * Returns the set of all nodes that were not visited
   *
   * @return the list of unvisited nodes
   */
  val unvisitedNodes: Set<N>
    get() = mUnvisitedNodes

  /**
   * Given a node, returns the shortest distance from any node in the root set to v
   *
   * @param g the graph in which the distances are to be measured
   * @param v the node whose distance is to be retrieved
   * @return the shortest distance from any node in the root set to v
   */
  fun getDistance(g: Graph<N>, v: N): Int {
    Preconditions.checkArgument(
      g.nodes().contains(v), "Node %s is not contained in the graph %s", v, g
    )
    return distanceDecorator[v]!!
  }

  /**
   * Returns set of predecessors of the given node
   *
   * @param v the node whose predecessors are to be retrieved
   * @return the set of predecessors
   */
  fun getPredecessors(v: N): Set<N>? = mPredecessorMap[v]

  protected fun initialize(g: Graph<N>, rootSet: Set<N>) {
    mNodesInOrderVisited = ArrayList()
    mUnvisitedNodes = HashSet()
    for (currentNode in g.nodes()) {
      mUnvisitedNodes.add(currentNode)
      mPredecessorMap[currentNode] = HashSet()
    }

    mCurrentList = ArrayList()
    for (v in rootSet) {
      distanceDecorator[v] = 0
      mCurrentList.add(v)
      mUnvisitedNodes.remove(v)
      mNodesInOrderVisited.add(v)
    }
  }

  private fun addPredecessor(predecessor: N, successor: N) {
    val predecessors = mPredecessorMap[successor]!!
    predecessors.add(predecessor)
  }

  /**
   * Computes the distances of all the node from the starting root nodes. If there is more than one
   * root node the minimum distance from each root node is used as the designated distance to a
   * given node. Also keeps track of the predecessors of each node traversed as well as the order of
   * nodes traversed.
   *
   * @param graph the graph to label
   * @param rootSet the set of starting nodes to traverse from
   */
  fun labelDistances(graph: Graph<N>, rootSet: Set<N>) {
    initialize(graph, rootSet)

    var distance = 1
    while (true) {
      val newList = ArrayList<N>()
      for (currentNode in mCurrentList) {
        if (graph.nodes().contains(currentNode)) {
          for (next in graph.successors(currentNode)) {
            visitNewNode(currentNode, next, distance, newList)
          }
        }
      }
      if (newList.isEmpty()) {
        break
      }
      mCurrentList = newList
      distance++
    }

    for (v in mUnvisitedNodes) {
      distanceDecorator[v] = -1
    }
  }

  /**
   * Computes the distances of all the node from the specified root node. Also keeps track of the
   * predecessors of each node traversed as well as the order of nodes traversed.
   *
   * @param graph the graph to label
   * @param root the single starting node to traverse from
   */
  fun labelDistances(graph: Graph<N>, root: N) {
    labelDistances(graph, Collections.singleton(root))
  }

  private fun visitNewNode(predecessor: N, neighbor: N, distance: Int, newList: MutableList<N>) {
    if (mUnvisitedNodes.contains(neighbor)) {
      distanceDecorator[neighbor] = distance
      newList.add(neighbor)
      mNodesInOrderVisited.add(neighbor)
      mUnvisitedNodes.remove(neighbor)
    }
    val predecessorDistance = distanceDecorator[predecessor]!!
    val successorDistance = distanceDecorator[neighbor]!!
    if (predecessorDistance < successorDistance) {
      addPredecessor(predecessor, neighbor)
    }
  }

  /**
   * Must be called after `labelDistances` in order to contain valid data.
   *
   * @return a map from nodes to minimum distances from the original source(s)
   */
  fun getDistanceDecorator(): Map<N, Int> = distanceDecorator
}
