package edu.uci.ics.jung.algorithms.shortestpath;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import edu.uci.ics.jung.algorithms.util.MapBinaryHeap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Creates a minimum spanning tree of a specified graph using a variation of Prim's algorithm.
 *
 * <p>The input graph is treated as though it were undirected, and the generated spanning tree is
 * undirected.
 *
 * @author Tom Nelson - tomnelson@dev.java.net
 * @author Joshua O'Madadhain
 * @param <N> the node type
 * @param <E> the edge type
 */
public class MinimumSpanningTree<N, E> {
  // TODO: consider providing a separate mechanism for extracting a spanning tree from an unweighted graph.

  /**
   * Extracts a minimum spanning forest from {@code graph} based on the specified edge weights. (If
   * {@code graph} is connected, then the graph returned will be a tree.)
   *
   * <p>Uses Prim's algorithm with a binary heap, for a run time of O(|E| log |V|).
   *
   * @param graph the graph from which to extract the minimum spanning forest
   * @param edgeWeights a mapping from edges to weights
   */
  public static <N, E> Network<N, E> extractFrom(
      Network<N, E> graph, Function<? super E, Double> edgeWeights) {
    Set<N> remainingNodes = new HashSet<>(graph.nodes());
    Map<N, NodeData<E>> nodeData = new HashMap<>();
    // initialize node data
    for (N node : remainingNodes) {
      nodeData.put(node, new NodeData<>());
    }
    MapBinaryHeap<N> heap =
        new MapBinaryHeap<>((a, b) -> Double.compare(nodeData.get(a).cost, nodeData.get(b).cost));
    heap.addAll(remainingNodes);

    // TODO: it seems unfortunate that this is a directed graph, but our libraries
    // (e.g. TreeLayout) assume that it is one.  Consider other options:
    // * let the user specify whether to create a directed or undirected graph
    // * let TreeLayout (etc.) handle undirected graphs (given a root set)
    MutableNetwork<N, E> tree =
        NetworkBuilder.directed().build(); // no self-loops or parallel edges

    while (!remainingNodes.isEmpty()) {
      N node = heap.poll(); // remove the node with the minimum incident edge cost
      remainingNodes.remove(node);
      E edge = nodeData.get(node).connection;
      if (edge == null) {
        tree.addNode(node);
      } else {
        tree.addEdge(graph.incidentNodes(edge).adjacentNode(node), node, edge);
      }
      for (N adjacentNode : graph.adjacentNodes(node)) {
        if (!remainingNodes.contains(adjacentNode)) {
          continue;
        }
        NodeData<E> adjacentNodeData = nodeData.get(adjacentNode);
        for (E connectingEdge : graph.edgesConnecting(node, adjacentNode)) {
          double connectingEdgeWeight = edgeWeights.apply(connectingEdge);
          if (connectingEdgeWeight < adjacentNodeData.cost) {
            adjacentNodeData.update(connectingEdgeWeight, connectingEdge);
            heap.update(adjacentNode);
          }
        }
      }
    }
    return tree;
  }

  /**
   * Extracts a minimum spanning forest from {@code graph} using its edge values (interpreted as
   * doubles). If {@code graph} is connected, then the graph returned will be a tree; otherwise it
   * will be a forest of trees.
   *
   * <p>Uses Prim's algorithm with a binary heap, for a run time of {@code O(|E| log |V|)}.
   *
   * @param graph the graph from which to extract the minimum spanning forest
   */
  public static <N, V extends Number> ValueGraph<N, V> extractFrom(ValueGraph<N, V> graph) {
    Set<N> remainingNodes = new HashSet<>(graph.nodes());
    Map<N, NodeData<N>> nodeData = new HashMap<>();
    // initialize node data
    for (N node : remainingNodes) {
      nodeData.put(node, new NodeData<>());
    }
    MapBinaryHeap<N> heap =
        new MapBinaryHeap<>((a, b) -> Double.compare(nodeData.get(a).cost, nodeData.get(b).cost));
    heap.addAll(remainingNodes);

    MutableValueGraph<N, V> tree =
        ValueGraphBuilder.directed().build(); // no self-loops or parallel edges

    while (!remainingNodes.isEmpty()) {
      N node = heap.peek(); // get the node with the minimum incident edge cost
      remainingNodes.remove(node);
      N connectedNode = nodeData.get(node).connection;
      if (connectedNode == null) {
        tree.addNode(node);
      } else {
        tree.putEdgeValue(
            node,
            connectedNode,
            graph
                .edgeValue(node, connectedNode)
                .orElseThrow(
                    () ->
                        new IllegalStateException(
                            "unexpected exception caused by bug in graph data structures")));
      }
      for (N adjacentNode : graph.adjacentNodes(node)) {
        if (!remainingNodes.contains(adjacentNode)) {
          continue;
        }
        NodeData<N> adjacentNodeData = nodeData.get(adjacentNode);
        double connectingEdgeWeight =
            graph
                .edgeValue(node, adjacentNode)
                .orElseThrow(
                    () ->
                        new IllegalStateException(
                            "unexpected exception caused by bug in graph data structures"))
                .doubleValue();
        if (connectingEdgeWeight < adjacentNodeData.cost) {
          adjacentNodeData.update(connectingEdgeWeight, node);
          heap.update(adjacentNode);
        }
      }
    }
    return tree;
  }

  // TODO: make this an AutoValue?
  private static class NodeData<T> {
    private double cost = Double.POSITIVE_INFINITY;
    private T connection = null;

    private NodeData() {}

    private void update(double cost, T connection) {
      this.cost = cost;
      this.connection = connection;
    }
  }
}
