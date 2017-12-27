/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.flows;

import com.google.common.base.Preconditions;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.util.IterativeProcess;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Implements the Edmonds-Karp maximum flow algorithm for solving the maximum flow problem. After
 * the algorithm is executed, the input {@code Map} is populated with a {@code Integer} for each
 * edge that indicates the flow along that edge.
 *
 * <p>An example of using this algorithm is as follows:
 *
 * <pre>
 * EdmondsKarpMaxFlow ek = new EdmondsKarpMaxFlow(graph, source, sink, edge_capacities, edge_flows,
 * edge_factory);
 * ek.evaluate(); // This instructs the class to compute the max flow
 * </pre>
 *
 * @see "Introduction to Algorithms by Cormen, Leiserson, Rivest, and Stein."
 * @see "Network Flows by Ahuja, Magnanti, and Orlin."
 * @see "Theoretical improvements in algorithmic efficiency for network flow problems by Edmonds and
 *     Karp, 1972."
 * @author Scott White, adapted to jung2 by Tom Nelson
 */
// TODO: this should work for input ValueGraphs also
// TODO: use a ValueGraph for the flow graph (take max of available parallel edge capacities)
// TODO: this currently works on Integers; can probably be generalized at least somewhat
// TODO: does this algorithm in fact actually fail for undirected graphs?
// TODO: no reason why the user should have to supply the edgeFlowMap
public class EdmondsKarpMaxFlow<N, E> extends IterativeProcess {

  private MutableNetwork<N, E> flowNetwork;
  private Network<N, E> network;
  private N source;
  private N target;
  private int maxFlow;
  private Set<N> sourcePartitionNodes;
  private Set<N> sinkPartitionNodes;
  private Set<E> minCutEdges;

  private Map<E, Integer> residualCapacityMap = new HashMap<E, Integer>();
  private Map<N, N> parentMap = new HashMap<N, N>();
  private Map<N, Integer> parentCapacityMap = new HashMap<N, Integer>();
  private Function<E, Integer> edgeCapacityTransformer;
  private Map<E, Integer> edgeFlowMap;
  private Supplier<E> edgeFactory;

  /**
   * Constructs a new instance of the algorithm solver for a given graph, source, and sink. Source
   * and sink nodes must be elements of the specified graph, and must be distinct.
   *
   * @param network the flow graph
   * @param source the source node
   * @param sink the sink node
   * @param edgeCapacityTransformer the Function that gets the capacity for each edge.
   * @param edgeFlowMap the map where the solver will place the value of the flow for each edge
   * @param edgeFactory used to create new edge instances for backEdges
   */
  public EdmondsKarpMaxFlow(
      Network<N, E> network,
      N source,
      N sink,
      Function<E, Integer> edgeCapacityTransformer,
      Map<E, Integer> edgeFlowMap,
      Supplier<E> edgeFactory) {
    Preconditions.checkArgument(network.isDirected(), "input graph must be directed");
    Preconditions.checkArgument(
        network.nodes().contains(source), "input graph must contain source node");
    Preconditions.checkArgument(
        network.nodes().contains(sink), "input graph must contain sink node");
    Preconditions.checkArgument(!source.equals(sink), "source and sink nodes must be distinct");

    this.network = network;

    this.source = source;
    this.target = sink;
    this.edgeFlowMap = edgeFlowMap;
    this.edgeCapacityTransformer = edgeCapacityTransformer;
    this.edgeFactory = edgeFactory;
    this.flowNetwork = Graphs.copyOf(network);
    maxFlow = 0;
    sinkPartitionNodes = new HashSet<N>();
    sourcePartitionNodes = new HashSet<N>();
    minCutEdges = new HashSet<E>();
  }

  private void clearParentValues() {
    parentMap.clear();
    parentCapacityMap.clear();
    parentCapacityMap.put(source, Integer.MAX_VALUE);
    parentMap.put(source, source);
  }

  protected boolean hasAugmentingPath() {
    sinkPartitionNodes.clear();
    sourcePartitionNodes.clear();
    sinkPartitionNodes.addAll(flowNetwork.nodes());

    Set<E> visitedEdgesMap = new HashSet<E>();
    Queue<N> queue = new LinkedList<N>();
    queue.add(source);

    while (!queue.isEmpty()) {
      N currentNode = queue.remove();
      sinkPartitionNodes.remove(currentNode);
      sourcePartitionNodes.add(currentNode);
      Integer currentCapacity = parentCapacityMap.get(currentNode);

      for (E neighboringEdge : flowNetwork.outEdges(currentNode)) {

        N neighboringNode = flowNetwork.incidentNodes(neighboringEdge).target();

        Integer residualCapacity = residualCapacityMap.get(neighboringEdge);
        if (residualCapacity <= 0 || visitedEdgesMap.contains(neighboringEdge)) {
          continue;
        }

        N neighborsParent = parentMap.get(neighboringNode);
        Integer neighborCapacity = parentCapacityMap.get(neighboringNode);
        int newCapacity = Math.min(residualCapacity, currentCapacity);

        if ((neighborsParent == null) || newCapacity > neighborCapacity) {
          parentMap.put(neighboringNode, currentNode);
          parentCapacityMap.put(neighboringNode, newCapacity);
          visitedEdgesMap.add(neighboringEdge);
          if (neighboringNode != target) {
            queue.add(neighboringNode);
          }
        }
      }
    }

    boolean hasAugmentingPath = false;
    Integer targetsParentCapacity = parentCapacityMap.get(target);
    if (targetsParentCapacity != null && targetsParentCapacity > 0) {
      updateResidualCapacities();
      hasAugmentingPath = true;
    }
    clearParentValues();
    return hasAugmentingPath;
  }

  @Override
  public void step() {
    while (hasAugmentingPath()) {}
    computeMinCut();
  }

  private void computeMinCut() {

    for (E e : network.edges()) {
      EndpointPair<N> endpoints = network.incidentNodes(e);
      N source = endpoints.source();
      N destination = endpoints.target();
      if (sinkPartitionNodes.contains(source) && sinkPartitionNodes.contains(destination)) {
        continue;
      }
      if (sourcePartitionNodes.contains(source) && sourcePartitionNodes.contains(destination)) {
        continue;
      }
      if (sinkPartitionNodes.contains(source) && sourcePartitionNodes.contains(destination)) {
        continue;
      }
      minCutEdges.add(e);
    }
  }

  /** @return the value of the maximum flow from the source to the sink. */
  public int getMaxFlow() {
    return maxFlow;
  }

  /**
   * @return the nodes which share the same partition (as defined by the min-cut edges) as the sink
   *     node.
   */
  public Set<N> getNodesInSinkPartition() {
    return sinkPartitionNodes;
  }

  /**
   * @return the nodes which share the same partition (as defined by the min-cut edges) as the
   *     source node.
   */
  public Set<N> getNodesInSourcePartition() {
    return sourcePartitionNodes;
  }

  /** @return the edges in the minimum cut. */
  public Set<E> getMinCutEdges() {
    return minCutEdges;
  }

  /** @return the graph for which the maximum flow is calculated. */
  public Network<N, E> getFlowGraph() {
    return flowNetwork;
  }

  @Override
  protected void initializeIterations() {
    parentCapacityMap.put(source, Integer.MAX_VALUE);
    parentMap.put(source, source);

    Set<EndpointPair<N>> backEdges = new HashSet<>();
    for (E edge : flowNetwork.edges()) {
      Integer capacity = edgeCapacityTransformer.apply(edge);
      Preconditions.checkNotNull(capacity, "Edge capacities must exist for all edges");

      residualCapacityMap.put(edge, capacity);
      EndpointPair<N> endpoints = flowNetwork.incidentNodes(edge);
      N source = endpoints.source();
      N destination = endpoints.target();

      if (!flowNetwork.successors(destination).contains(source)) {
        backEdges.add(EndpointPair.ordered(destination, source));
      }
    }

    for (EndpointPair<N> endpoints : backEdges) {
      E backEdge = edgeFactory.get();
      flowNetwork.addEdge(endpoints.source(), endpoints.target(), backEdge);
      residualCapacityMap.put(backEdge, 0);
    }
  }

  @Override
  protected void finalizeIterations() {

    for (E currentEdge : flowNetwork.edges()) {
      Integer capacity = edgeCapacityTransformer.apply(currentEdge);

      Integer residualCapacity = residualCapacityMap.get(currentEdge);
      if (capacity != null) {
        Integer flowValue = capacity - residualCapacity;
        this.edgeFlowMap.put(currentEdge, flowValue);
      }
    }

    Set<E> backEdges = new HashSet<E>();
    for (E currentEdge : flowNetwork.edges()) {

      if (edgeCapacityTransformer.apply(currentEdge) == null) {
        backEdges.add(currentEdge);
      } else {
        residualCapacityMap.remove(currentEdge);
      }
    }
    for (E e : backEdges) {
      flowNetwork.removeEdge(e);
    }
  }

  private void updateResidualCapacities() {

    Integer augmentingPathCapacity = parentCapacityMap.get(target);
    maxFlow += augmentingPathCapacity;
    N currentNode = target;
    N parentNode = null;
    while ((parentNode = parentMap.get(currentNode)) != currentNode) {
      // TODO: change this to edgeConnecting() once we are using Guava 22.0+
      E currentEdge = flowNetwork.edgesConnecting(parentNode, currentNode).iterator().next();

      Integer residualCapacity = residualCapacityMap.get(currentEdge);

      residualCapacity = residualCapacity - augmentingPathCapacity;
      residualCapacityMap.put(currentEdge, residualCapacity);

      E backEdge = flowNetwork.edgesConnecting(currentNode, parentNode).iterator().next();
      residualCapacity = residualCapacityMap.get(backEdge);
      residualCapacity = residualCapacity + augmentingPathCapacity;
      residualCapacityMap.put(backEdge, residualCapacity);
      currentNode = parentNode;
    }
  }
}
