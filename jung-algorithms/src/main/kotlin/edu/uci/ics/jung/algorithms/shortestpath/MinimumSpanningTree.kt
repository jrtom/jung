package edu.uci.ics.jung.algorithms.shortestpath

import com.google.common.collect.Sets
import com.google.common.graph.MutableNetwork
import com.google.common.graph.MutableValueGraph
import com.google.common.graph.Network
import com.google.common.graph.NetworkBuilder
import com.google.common.graph.ValueGraph
import com.google.common.graph.ValueGraphBuilder
import edu.uci.ics.jung.algorithms.util.MapBinaryHeap
import java.util.LinkedHashMap
import java.util.LinkedHashSet
import java.util.function.Function

/**
 * Creates a minimum spanning tree of a specified graph using a variation of Prim's algorithm.
 *
 * The input graph is treated as though it were undirected, and the generated spanning tree is
 * undirected.
 *
 * @author Tom Nelson - tomnelson@dev.java.net
 * @author Joshua O'Madadhain
 * @param N the node type
 * @param E the edge type
 */
object MinimumSpanningTree {
  // TODO: consider providing a separate mechanism for extracting a spanning tree from an unweighted
  // graph.

  /**
   * Extracts a minimum spanning forest from [graph] based on the specified edge weights. (If
   * [graph] is connected, then the graph returned will be a tree.)
   *
   * Uses Prim's algorithm with a binary heap, for a run time of O(|E| log |V|).
   *
   * @param graph the graph from which to extract the minimum spanning forest
   * @param edgeWeights a mapping from edges to weights
   */
  @JvmStatic
  fun <N : Any, E : Any> extractFrom(
    graph: Network<N, E>, edgeWeights: Function<in E, Double>
  ): Network<N, E> {
    val remainingNodes = LinkedHashSet(graph.nodes())
    val nodeData = LinkedHashMap<N, NodeData<E>>()
    // initialize node data
    for (node in remainingNodes) {
      nodeData[node] = NodeData()
    }
    val heap = MapBinaryHeap<N>(Comparator { a, b ->
      java.lang.Double.compare(nodeData[a]!!.cost, nodeData[b]!!.cost)
    })
    heap.addAll(remainingNodes)

    // TODO: it seems unfortunate that this is a directed graph, but our libraries
    // (e.g. TreeLayout) assume that it is one.  Consider other options:
    // * let the user specify whether to create a directed or undirected graph
    // * let TreeLayout (etc.) handle undirected graphs (given a root set)
    val tree: MutableNetwork<N, E> =
      NetworkBuilder.undirected().build() // no self-loops or parallel edges

    while (remainingNodes.isNotEmpty()) {
      val node = heap.poll()!! // remove the node with the minimum incident edge cost
      remainingNodes.remove(node)
      val edge = nodeData[node]!!.connection
      if (edge == null) {
        tree.addNode(node)
      } else {
        tree.addEdge(graph.incidentNodes(edge).adjacentNode(node), node, edge)
      }
      for (adjacentNode in graph.adjacentNodes(node)) {
        if (!remainingNodes.contains(adjacentNode)) {
          continue
        }
        val adjacentNodeData = nodeData[adjacentNode]!!
        // edgesConnecting() respects direction, so since the input graph may be directed
        // (and we want to treat it as undirected), get all connecting edges in both
        // directions.
        for (connectingEdge in Sets.union(
          graph.edgesConnecting(node, adjacentNode),
          graph.edgesConnecting(adjacentNode, node)
        )) {
          val connectingEdgeWeight = edgeWeights.apply(connectingEdge)
          if (connectingEdgeWeight < adjacentNodeData.cost) {
            adjacentNodeData.update(connectingEdgeWeight, connectingEdge)
            heap.update(adjacentNode)
          }
        }
      }
    }
    return tree
  }

  /**
   * Extracts a minimum spanning forest from [graph] using its edge values (interpreted as
   * doubles). If [graph] is connected, then the graph returned will be a tree; otherwise it
   * will be a forest of trees.
   *
   * Uses Prim's algorithm with a binary heap, for a run time of `O(|E| log |V|)`.
   *
   * @param graph the graph from which to extract the minimum spanning forest
   */
  @JvmStatic
  fun <N : Any, V : Number> extractFrom(graph: ValueGraph<N, V>): ValueGraph<N, V> {
    val remainingNodes = LinkedHashSet(graph.nodes())
    val nodeData = LinkedHashMap<N, NodeData<N>>()
    // initialize node data
    for (node in remainingNodes) {
      nodeData[node] = NodeData()
    }
    val heap = MapBinaryHeap<N>(Comparator { a, b ->
      java.lang.Double.compare(nodeData[a]!!.cost, nodeData[b]!!.cost)
    })
    heap.addAll(remainingNodes)

    val tree: MutableValueGraph<N, V> =
      ValueGraphBuilder.directed().build() // no self-loops or parallel edges

    while (remainingNodes.isNotEmpty()) {
      val node = heap.peek()!! // get the node with the minimum incident edge cost
      remainingNodes.remove(node)
      val connectedNode = nodeData[node]!!.connection
      if (connectedNode == null) {
        tree.addNode(node)
      } else {
        tree.putEdgeValue(
          node,
          connectedNode,
          graph.edgeValue(node, connectedNode).orElseThrow {
            IllegalStateException("unexpected exception caused by bug in graph data structures")
          }
        )
      }
      for (adjacentNode in graph.adjacentNodes(node)) {
        if (!remainingNodes.contains(adjacentNode)) {
          continue
        }
        val adjacentNodeData = nodeData[adjacentNode]!!
        val connectingEdgeWeight = graph.edgeValue(node, adjacentNode).orElseThrow {
          IllegalStateException("unexpected exception caused by bug in graph data structures")
        }.toDouble()
        if (connectingEdgeWeight < adjacentNodeData.cost) {
          adjacentNodeData.update(connectingEdgeWeight, node)
          heap.update(adjacentNode)
        }
      }
    }
    return tree
  }

  // TODO: make this an AutoValue?
  private class NodeData<T> {
    var cost: Double = Double.POSITIVE_INFINITY
    var connection: T? = null

    fun update(cost: Double, connection: T) {
      this.cost = cost
      this.connection = connection
    }

    override fun toString(): String = "cost: $cost, connection: $connection"
  }
}
