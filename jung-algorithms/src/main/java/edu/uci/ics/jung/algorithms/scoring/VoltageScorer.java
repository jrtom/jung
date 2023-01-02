/*
 * Created on Jul 15, 2007
 *
 * Copyright (c) 2007, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.scoring;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.scoring.util.UniformDegreeWeight;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Assigns scores to nodes according to their 'voltage' in an approximate solution to the Kirchoff
 * equations. This is accomplished by tying "source" nodes to specified positive voltages, "sink"
 * nodes to 0 N, and iteratively updating the voltage of each other node to the (weighted) average
 * of the voltages of its neighbors.
 *
 * <p>The resultant voltages will all be in the range <code>[0, max]</code> where <code>max</code>
 * is the largest voltage of any source node (in the absence of negative source voltages; see
 * below).
 *
 * <p>A few notes about this algorithm's interpretation of the graph data:
 *
 * <ul>
 *   <li>Higher edge weights are interpreted as indicative of greater influence/effect than lower
 *       edge weights.
 *   <li>Negative edge weights (and negative "source" voltages) invalidate the interpretation of the
 *       resultant values as voltages. However, this algorithm will not reject graphs with negative
 *       edge weights or source voltages.
 *   <li>Parallel edges are equivalent to a single edge whose weight is the sum of the weights on
 *       the parallel edges.
 *   <li>Current flows along undirected edges in both directions, but only flows along directed
 *       edges in the direction of the edge.
 * </ul>
 */
public class VoltageScorer<N, E> extends AbstractIterativeScorer<N, E, Double>
    implements NodeScorer<N, Double> {
  protected Map<N, ? extends Number> source_voltages;
  protected Set<N> sinks;

  /**
   * Creates an instance with the specified graph, edge weights, source voltages, and sinks.
   *
   * @param g the input graph
   * @param edge_weights the edge weights, representing conductivity
   * @param source_voltages the (fixed) voltage for each source
   * @param sinks the nodes whose voltages are tied to 0
   */
  public VoltageScorer(
      Network<N, E> g,
      Function<? super E, ? extends Number> edge_weights,
      Map<N, ? extends Number> source_voltages,
      Set<N> sinks) {
    super(g, edge_weights);
    this.source_voltages = source_voltages;
    this.sinks = sinks;
    initialize();
  }

  /**
   * Creates an instance with the specified graph, edge weights, source nodes (each of whose
   * 'voltages' are tied to 1), and sinks.
   *
   * @param g the input graph
   * @param edge_weights the edge weights, representing conductivity
   * @param sources the nodes whose voltages are tied to 1
   * @param sinks the nodes whose voltages are tied to 0
   */
  public VoltageScorer(
      Network<N, E> g,
      Function<? super E, ? extends Number> edge_weights,
      Set<N> sources,
      Set<N> sinks) {
    super(g, edge_weights);

    Map<N, Double> unit_voltages = new HashMap<N, Double>();
    for (N v : sources) {
      unit_voltages.put(v, new Double(1.0));
    }
    this.source_voltages = unit_voltages;
    this.sinks = sinks;
    initialize();
  }

  /**
   * Creates an instance with the specified graph, source nodes (each of whose 'voltages' are tied
   * to 1), and sinks. The outgoing edges for each node are assigned weights that sum to 1.
   *
   * @param g the input graph
   * @param sources the nodes whose voltages are tied to 1
   * @param sinks the nodes whose voltages are tied to 0
   */
  public VoltageScorer(Network<N, E> g, Set<N> sources, Set<N> sinks) {
    super(g);

    Map<N, Double> unit_voltages = new HashMap<N, Double>();
    for (N v : sources) {
      unit_voltages.put(v, new Double(1.0));
    }
    this.source_voltages = unit_voltages;
    this.sinks = sinks;
    initialize();
  }

  /**
   * Creates an instance with the specified graph, source voltages, and sinks. The outgoing edges
   * for each node are assigned weights that sum to 1.
   *
   * @param g the input graph
   * @param source_voltages the (fixed) voltage for each source
   * @param sinks the nodes whose voltages are tied to 0
   */
  public VoltageScorer(Network<N, E> g, Map<N, ? extends Number> source_voltages, Set<N> sinks) {
    super(g);
    this.source_voltages = source_voltages;
    this.sinks = sinks;
    this.edge_weights = new UniformDegreeWeight<N, E>(g);
    initialize();
  }

  /**
   * Creates an instance with the specified graph, edge weights, source, and sink. The source node
   * voltage is tied to 1.
   *
   * @param g the input graph
   * @param edge_weights the edge weights, representing conductivity
   * @param source the node whose voltage is tied to 1
   * @param sink the node whose voltage is tied to 0
   */
  public VoltageScorer(
      Network<N, E> g, Function<? super E, ? extends Number> edge_weights, N source, N sink) {
    this(g, edge_weights, ImmutableMap.of(source, 1.0), ImmutableSet.of(sink));
    initialize();
  }

  /**
   * Creates an instance with the specified graph, edge weights, source, and sink. The source node
   * voltage is tied to 1. The outgoing edges for each node are assigned weights that sum to 1.
   *
   * @param g the input graph
   * @param source the node whose voltage is tied to 1
   * @param sink the node whose voltage is tied to 0
   */
  public VoltageScorer(Network<N, E> g, N source, N sink) {
    this(g, ImmutableMap.of(source, 1.0), ImmutableSet.of(sink));
    initialize();
  }

  /** Initializes the state of this instance. */
  @Override
  public void initialize() {
    super.initialize();

    // sanity check
    Preconditions.checkArgument(!source_voltages.isEmpty(), "Source voltages must be non-empty");
    Preconditions.checkArgument(!sinks.isEmpty(), "Sinks must be non-empty");

    Preconditions.checkArgument(
        Sets.intersection(source_voltages.keySet(), sinks).isEmpty(),
        "Sources and sinks must be disjoint");
    Preconditions.checkArgument(
        graph.nodes().containsAll(source_voltages.keySet()),
        "Sources must all be elements of the graph");
    Preconditions.checkArgument(
        graph.nodes().containsAll(sinks), "Sinks must all be elements of the graph");

    for (Map.Entry<N, ? extends Number> entry : source_voltages.entrySet()) {
      N v = entry.getKey();
      Preconditions.checkArgument(
          !sinks.contains(v), "Node " + v + " is incorrectly specified as both source and sink");
      double value = entry.getValue().doubleValue();
      Preconditions.checkArgument(
          value > 0, "Source node " + v + " has non-positive voltage " + value);
    }

    // set up initial voltages
    for (N v : graph.nodes()) {
      if (source_voltages.containsKey(v)) {
        setOutputValue(v, source_voltages.get(v).doubleValue());
      } else {
        setOutputValue(v, 0.0);
      }
    }
  }

  /**
   * @see edu.uci.ics.jung.algorithms.scoring.AbstractIterativeScorer#update(Object)
   */
  @Override
  public double update(N v) {
    // if it's a voltage source or sink, we're done
    Number source_volts = source_voltages.get(v);
    if (source_volts != null) {
      setOutputValue(v, source_volts.doubleValue());
      return 0.0;
    }
    if (sinks.contains(v)) {
      setOutputValue(v, 0.0);
      return 0.0;
    }

    double voltage_sum = 0;
    double weight_sum = 0;
    for (N u : graph.predecessors(v)) {
      for (E e : graph.edgesConnecting(u, v)) {
        double weight = getEdgeWeight(u, e).doubleValue();
        voltage_sum += getCurrentValue(u).doubleValue() * weight;
        weight_sum += weight;
      }
    }

    // if either is 0, new value is 0
    if (voltage_sum == 0 || weight_sum == 0) {
      setOutputValue(v, 0.0);
      return getCurrentValue(v).doubleValue();
    }

    double outputValue = voltage_sum / weight_sum;
    setOutputValue(v, outputValue);
    return Math.abs(getCurrentValue(v).doubleValue() - outputValue);
  }
}
