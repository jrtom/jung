/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.generators.random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.algorithms.util.WeightedChoice;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Simple evolving scale-free random graph generator. At each time step, a new node is created and
 * is connected to existing nodes according to the principle of "preferential attachment", whereby
 * nodes with higher degree have a higher probability of being selected for attachment.
 *
 * <p>At a given timestep, the probability <code>p</code> of creating an edge between an existing
 * node <code>v</code> and the newly added node is
 *
 * <pre>
 * p = (degree(v) + 1) / (|E| + |V|);
 * </pre>
 *
 * <p>where <code>|E|</code> and <code>|V|</code> are, respectively, the number of edges and nodes
 * currently in the network (counting neither the new node nor the other edges that are being
 * attached to it).
 *
 * <p>Note that the formula specified in the original paper (cited below) was
 *
 * <pre>
 * p = degree(v) / |E|
 * </pre>
 *
 * <p>However, this would have meant that the probability of attachment for any existing isolated
 * node would be 0. This version uses Lagrangian smoothing to give each existing node a positive
 * attachment probability.
 *
 * <p>The graph created may be either directed or undirected (controlled by a constructor
 * parameter); the default is undirected. If the graph is specified to be directed, then the edges
 * added will be directed from the newly added node u to the existing node v, with probability
 * proportional to the indegree of v (number of edges directed towards v). If the graph is specified
 * to be undirected, then the (undirected) edges added will connect u to v, with probability
 * proportional to the degree of v.
 *
 * <p>The <code>parallel</code> constructor parameter specifies whether parallel edges may be
 * created.
 *
 * @see "A.-L. Barabasi and R. Albert, Emergence of scaling in random networks, Science 286, 1999."
 * @author Scott White
 * @author Joshua O'Madadhain
 * @author Tom Nelson - adapted to jung2
 * @author James Marchant
 */
// TODO(jrtom): decide whether EvolvingGraphGenerator is actually necessary for this to extend
public class BarabasiAlbertGenerator<N, E> {
  private int mNumEdgesToAttachPerStep;
  private int mElapsedTimeSteps;
  private Random mRandom;
  protected Supplier<N> nodeFactory;
  protected Supplier<E> edgeFactory;
  protected ImmutableSet<N> seedNodes;
  private MutableNetwork<N, E> graph;

  /**
   * Constructs a new instance of the generator.
   *
   * @param graphBuilder builder for graph instances
   * @param nodeFactory factory for nodes of the appropriate type
   * @param edgeFactory factory for edges of the appropriate type
   * @param init_nodes number of unconnected 'seed' nodes that the graph should start with
   * @param numEdgesToAttach the number of edges that should be attached from the new node to
   *     pre-existing nodes at each time step
   * @param seed random number seed
   */
  // TODO(jrtom): consider using a Builder pattern here
  public BarabasiAlbertGenerator(
      NetworkBuilder<Object, Object> graphBuilder,
      Supplier<N> nodeFactory,
      Supplier<E> edgeFactory,
      int init_nodes,
      int numEdgesToAttach,
      int seed) {
    this.nodeFactory = checkNotNull(nodeFactory);
    this.edgeFactory = checkNotNull(edgeFactory);
    checkArgument(init_nodes > 0, "Number of initial unconnected 'seed' nodes must be positive");
    checkArgument(
        numEdgesToAttach > 0, "Number of edges to attach at each time step must be positive");
    checkArgument(
        numEdgesToAttach <= init_nodes,
        "Number of edges to attach at each time step must be <= the number of initial nodes");
    this.graph = graphBuilder.build();

    mNumEdgesToAttachPerStep = numEdgesToAttach;
    mRandom = new Random(seed);
    this.nodeFactory = nodeFactory;
    initialize(init_nodes);
  }

  /**
   * Constructs a new instance of the generator, whose output will be an undirected graph, and which
   * will use the current time as a seed for the random number generation.
   *
   * @param nodeFactory factory for nodes of the appropriate type
   * @param edgeFactory factory for edges of the appropriate type
   * @param init_nodes number of nodes that the graph should start with
   * @param numEdgesToAttach the number of edges that should be attached from the new node to
   *     pre-existing nodes at each time step
   */
  public BarabasiAlbertGenerator(
      NetworkBuilder<Object, Object> graphBuilder,
      Supplier<N> nodeFactory,
      Supplier<E> edgeFactory,
      int init_nodes,
      int numEdgesToAttach) {
    this(
        graphBuilder,
        nodeFactory,
        edgeFactory,
        init_nodes,
        numEdgesToAttach,
        (int) System.currentTimeMillis());
  }

  private void initialize(int init_nodes) {
    ImmutableSet.Builder<N> seedBuilder = ImmutableSet.builder();

    for (int i = 0; i < init_nodes; i++) {
      N v = nodeFactory.get();
      seedBuilder.add(v);
      graph.addNode(v);
    }

    seedNodes = seedBuilder.build();
    mElapsedTimeSteps = 0;
  }

  private WeightedChoice<N> buildNodeProbabilities() {
    Map<N, Double> item_weights = new HashMap<N, Double>();
    for (N v : graph.nodes()) {
      double degree;
      double denominator;

      // Attachment probability is dependent on whether the graph is
      // directed or undirected.
      if (graph.isDirected()) {
        degree = graph.inDegree(v);
        denominator = graph.edges().size() + graph.nodes().size();
      } else {
        degree = graph.degree(v);
        denominator = (2 * graph.edges().size()) + graph.nodes().size();
      }

      double prob = (degree + 1) / denominator;
      item_weights.put(v, prob);
    }
    WeightedChoice<N> nodeProbabilities = new WeightedChoice<N>(item_weights, mRandom);

    return nodeProbabilities;
  }

  private List<N> generateAdjacentNodes(int edgesToAdd) {
    Preconditions.checkArgument(edgesToAdd >= 1);
    WeightedChoice<N> nodeChooser = buildNodeProbabilities();
    List<N> adjacentNodes = new ArrayList<N>(edgesToAdd);
    while (adjacentNodes.size() < edgesToAdd) {
      N attach_point = nodeChooser.nextItem();

      // if parallel edges are not allowed, skip this node if already present
      if (!graph.allowsParallelEdges() && adjacentNodes.contains(attach_point)) {
        continue;
      }

      adjacentNodes.add(attach_point);
    }
    return adjacentNodes;
  }

  public void evolveGraph(int numTimeSteps) {
    for (int i = 0; i < numTimeSteps; i++) {
      N newNode = nodeFactory.get();

      // determine the nodes to connect to newNode before connecting anything, because
      // we don't want to bias the degree calculations
      // note: because we don't add newNode to the graph until after identifying the
      // adjacent nodes, we don't need to worry about creating a self-loop
      List<N> adjacentNodes = generateAdjacentNodes(mNumEdgesToAttachPerStep);
      graph.addNode(newNode);

      for (N node : adjacentNodes) {
        graph.addEdge(newNode, node, edgeFactory.get());
      }

      mElapsedTimeSteps++;
    }
  }

  public int numIterations() {
    return mElapsedTimeSteps;
  }

  public MutableNetwork<N, E> get() {
    return graph;
  }

  public ImmutableSet<N> seedNodes() {
    return seedNodes;
  }
}
