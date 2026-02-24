/*
 * Created on Feb 12, 2017
 *
 * Copyright (c) 2017, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.graph

import com.google.common.base.Preconditions.checkArgument
import com.google.common.base.Preconditions.checkNotNull
import com.google.common.base.Preconditions.checkState
import com.google.common.collect.Iterables
import com.google.common.graph.AbstractGraph
import com.google.common.graph.ElementOrder
import com.google.common.graph.EndpointPair
import com.google.common.graph.MutableGraph
import com.google.common.graph.Traverser
import java.util.Optional

internal class DelegateCTree<N : Any>(
  graph: MutableGraph<N>,
  root: Optional<N>,
) : AbstractGraph<N>(), MutableCTree<N> {

  private val delegate: MutableGraph<N> = checkNotNull(graph, "graph")
  private val depths: MutableMap<N, Int> = HashMap()
  private var height: Optional<Int> = Optional.empty()
  private var root: Optional<N> = Optional.empty()

  init {
    setRoot(checkNotNull(root, "root"))
  }

  override fun root(): Optional<N> = root

  override fun predecessor(node: N): Optional<N> {
    checkNotNull(node, "node")
    val predecessors = delegate.predecessors(node)
    checkState(predecessors.size <= 1)
    return if (predecessors.isEmpty()) {
      Optional.empty()
    } else {
      Optional.of(Iterables.getOnlyElement(predecessors))
    }
  }

  override fun depth(node: N): Int {
    checkNotNull(node, "node")
    checkArgument(delegate.nodes().contains(node), GraphConstants.NODE_NOT_IN_TREE, node)
    return depths[node]!!
  }

  override fun height(): Optional<Int> {
    if (!root.isPresent) {
      return Optional.empty()
    }
    // Calculate the height
    if (!height.isPresent) {
      calculateHeight()
    }
    return height
  }

  override fun isDirected(): Boolean = true

  override fun allowsSelfLoops(): Boolean = false

  override fun adjacentNodes(node: N): Set<N> {
    checkNotNull(node, "node")
    checkArgument(delegate.nodes().contains(node), GraphConstants.NODE_NOT_IN_TREE, node)
    return delegate.adjacentNodes(node)
  }

  override fun degree(node: N): Int {
    checkNotNull(node, "node")
    checkArgument(delegate.nodes().contains(node), GraphConstants.NODE_NOT_IN_TREE, node)
    return delegate.degree(node)
  }

  override fun inDegree(node: N): Int {
    checkNotNull(node, "node")
    checkArgument(delegate.nodes().contains(node), GraphConstants.NODE_NOT_IN_TREE, node)
    return delegate.inDegree(node)
  }

  override fun nodeOrder(): ElementOrder<N> = delegate.nodeOrder()

  override fun nodes(): Set<N> = delegate.nodes()

  override fun outDegree(node: N): Int {
    checkNotNull(node, "node")
    checkArgument(delegate.nodes().contains(node), GraphConstants.NODE_NOT_IN_TREE, node)
    return delegate.outDegree(node)
  }

  override fun predecessors(node: N): Set<N> {
    checkNotNull(node, "node")
    checkArgument(delegate.nodes().contains(node), GraphConstants.NODE_NOT_IN_TREE, node)
    return delegate.predecessors(node)
  }

  override fun successors(node: N): Set<N> {
    checkNotNull(node, "node")
    checkArgument(delegate.nodes().contains(node), GraphConstants.NODE_NOT_IN_TREE, node)
    return delegate.successors(node)
  }

  override fun edges(): Set<EndpointPair<N>> = delegate.edges()

  override fun incidentEdges(node: N): Set<EndpointPair<N>> {
    checkNotNull(node, "node")
    checkArgument(delegate.nodes().contains(node), GraphConstants.NODE_NOT_IN_TREE, node)
    return delegate.incidentEdges(node)
  }

  override fun hasEdgeConnecting(nodeU: N, nodeV: N): Boolean {
    checkNotNull(nodeU, "nodeU")
    checkNotNull(nodeV, "nodeV")
    checkArgument(delegate.nodes().contains(nodeU), GraphConstants.NODE_NOT_IN_TREE, nodeU)
    checkArgument(delegate.nodes().contains(nodeV), GraphConstants.NODE_NOT_IN_TREE, nodeV)
    return delegate.hasEdgeConnecting(nodeU, nodeV)
  }

  override fun addNode(node: N): Boolean {
    checkNotNull(node, "node")
    if (root.isPresent) {
      val rootValue = root.get()
      checkArgument(rootValue == node, GraphConstants.NODE_ROOT_OF_TREE, node, rootValue)
      return false
    }
    setRoot(Optional.of(node))
    return true
  }

  override fun putEdge(nodeU: N, nodeV: N): Boolean {
    checkNotNull(nodeU, "nodeU")
    checkNotNull(nodeV, "nodeV")
    checkArgument(nodeU != nodeV, GraphConstants.SELF_LOOP_NOT_ALLOWED, nodeU)
    if (!root.isPresent) {
      setRoot(Optional.of(nodeU))
    } else {
      checkArgument(nodes().contains(nodeU), GraphConstants.NODEU_NOT_IN_TREE, nodeU, nodeV, nodeU)
      if (successors(nodeU).contains(nodeV)) {
        return false // edge is already present; no-op
      }
      // verify that nodeV is not in the tree
      checkArgument(!nodes().contains(nodeV), GraphConstants.NODEV_IN_TREE, nodeU, nodeV, nodeV)
    }
    setDepth(nodeV, nodeU)
    return delegate.putEdge(nodeU, nodeV)
  }

  override fun putEdge(endpoints: EndpointPair<N>): Boolean {
    checkNotNull(endpoints, "endpoints")
    return putEdge(endpoints.nodeU(), endpoints.nodeV())
  }

  private fun setDepth(node: N, parent: N?) {
    if (parent == null) { // root: both depth and height are 0
      depths[node] = 0
      height = Optional.of(0)
    } else {
      depths.putIfAbsent(parent, 0)
      val nodeDepth = maxOf(depths[parent]!! + 1, height.orElse(0))
      depths[node] = nodeDepth
      height = Optional.of(nodeDepth)
    }
  }

  private fun calculateHeight() {
    // This method is only called when the root is present, so we don't need to check for that.
    var currentHeight = 0
    var currentLevel = ArrayList(successors(root.orElseThrow { AssertionError() }))
    while (currentLevel.isNotEmpty()) {
      val nextLevel = ArrayList<N>()
      currentHeight++ // there's at least one node in the current level
      for (node in currentLevel) {
        nextLevel.addAll(successors(node))
      }
      currentLevel = nextLevel
    }
    height = Optional.of(currentHeight)
  }

  override fun removeNode(node: N): Boolean {
    checkNotNull(node, "node")
    if (!nodes().contains(node)) {
      return false
    }
    // Collect all nodes to remove first to avoid ConcurrentModificationException
    val nodesToRemove = Traverser.forTree(delegate).breadthFirst(node).toList()
    for (nodeToRemove in nodesToRemove) {
      delegate.removeNode(nodeToRemove)
      depths.remove(nodeToRemove)
    }
    if (root.isPresent && root.get() == node) {
      setRoot(Optional.empty())
    }
    // Reset the height, since we don't know how it was affected by removing the subtree.
    height = Optional.empty()
    return true
  }

  private fun setRoot(root: Optional<N>) {
    this.root = root
    this.root.ifPresent { node ->
      delegate.addNode(node)
      setDepth(node, null)
    }
  }

  override fun removeEdge(nodeU: N, nodeV: N): Boolean {
    checkNotNull(nodeU, "nodeU")
    checkNotNull(nodeV, "nodeV")
    delegate.removeEdge(nodeU, nodeV)
    return removeNode(nodeV)
  }

  override fun removeEdge(endpoints: EndpointPair<N>): Boolean {
    checkNotNull(endpoints, "endpoints")
    return removeEdge(endpoints.nodeU(), endpoints.nodeV())
  }
}
