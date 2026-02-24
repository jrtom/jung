package edu.uci.ics.jung.graph

import com.google.common.base.Preconditions.checkArgument
import com.google.common.base.Preconditions.checkNotNull
import com.google.common.base.Preconditions.checkState
import com.google.common.collect.Iterables
import com.google.common.graph.AbstractNetwork
import com.google.common.graph.ElementOrder
import com.google.common.graph.EndpointPair
import com.google.common.graph.Graph
import com.google.common.graph.MutableNetwork
import com.google.common.graph.Traverser
import java.util.Optional

// TODO: Add tests in similar fashion to CTreeTest and AbstractCTreeTest
internal class DelegateCTreeNetwork<N : Any, E : Any>(
  graph: MutableNetwork<N, E>,
  root: Optional<N>,
) : AbstractNetwork<N, E>(), MutableCTreeNetwork<N, E> {

  private val delegate: MutableNetwork<N, E> = checkNotNull(graph, "graph")
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

  override fun inEdge(node: N): Optional<E> {
    val inEdges = delegate.inEdges(node)
    checkState(inEdges.size <= 1)
    return if (inEdges.isEmpty()) Optional.empty() else Optional.of(Iterables.getOnlyElement(inEdges))
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

  override fun edges(): Set<E> = delegate.edges()

  override fun adjacentEdges(edge: E): Set<E> {
    checkNotNull(edge, "edge")
    return delegate.adjacentEdges(edge)
  }

  override fun allowsParallelEdges(): Boolean = false

  override fun asGraph(): Graph<N> = delegate.asGraph()

  override fun edgeOrder(): ElementOrder<E> = delegate.edgeOrder()

  override fun edgesConnecting(nodeU: N, nodeV: N): Set<E> {
    checkNotNull(nodeU, "nodeU")
    checkNotNull(nodeV, "nodeV")
    checkArgument(delegate.nodes().contains(nodeU), GraphConstants.NODE_NOT_IN_TREE, nodeU)
    checkArgument(delegate.nodes().contains(nodeV), GraphConstants.NODE_NOT_IN_TREE, nodeV)
    return delegate.edgesConnecting(nodeU, nodeV)
  }

  override fun edgeConnecting(nodeU: N, nodeV: N): Optional<E> =
    delegate.edgeConnecting(nodeU, nodeV)

  override fun edgeConnectingOrNull(nodeU: N, nodeV: N): E? =
    delegate.edgeConnectingOrNull(nodeU, nodeV)

  override fun hasEdgeConnecting(nodeU: N, nodeV: N): Boolean =
    delegate.hasEdgeConnecting(nodeU, nodeV)

  override fun inEdges(node: N): Set<E> {
    checkNotNull(node, "node")
    checkArgument(delegate.nodes().contains(node), GraphConstants.NODE_NOT_IN_TREE, node)
    return delegate.inEdges(node)
  }

  override fun incidentEdges(node: N): Set<E> {
    checkNotNull(node, "node")
    checkArgument(delegate.nodes().contains(node), GraphConstants.NODE_NOT_IN_TREE, node)
    return delegate.incidentEdges(node)
  }

  override fun incidentNodes(edge: E): EndpointPair<N> {
    checkNotNull(edge, "edge")
    checkArgument(delegate.edges().contains(edge), GraphConstants.EDGE_NOT_IN_TREE, edge)
    return delegate.incidentNodes(edge)
  }

  override fun outEdges(node: N): Set<E> {
    checkNotNull(node, "node")
    checkArgument(delegate.nodes().contains(node), GraphConstants.NODE_NOT_IN_TREE, node)
    return delegate.outEdges(node)
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

  override fun addEdge(nodeU: N, nodeV: N, edge: E): Boolean {
    checkNotNull(nodeU, "nodeU")
    checkNotNull(nodeV, "nodeV")
    checkNotNull(edge, "edge")
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
    return delegate.addEdge(nodeU, nodeV, edge)
  }

  override fun addEdge(endpoints: EndpointPair<N>, edge: E): Boolean {
    checkNotNull(endpoints, "endpoints")
    return addEdge(endpoints.nodeU(), endpoints.nodeV(), edge)
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
    this.height = Optional.empty()
    return true
  }

  private fun setRoot(root: Optional<N>) {
    this.root = root
    this.root.ifPresent { node ->
      delegate.addNode(node)
      setDepth(node, null)
    }
  }

  override fun removeEdge(edge: E): Boolean {
    checkNotNull(edge, "edge")
    // remove the subtree rooted at this edge's target
    val target = delegate.incidentNodes(edge).target()
    delegate.removeEdge(edge)
    return removeNode(target)
  }
}
