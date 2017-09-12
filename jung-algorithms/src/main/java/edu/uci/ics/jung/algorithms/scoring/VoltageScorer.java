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
 * Assigns scores to vertices according to their 'voltage' in an approximate solution to the
 * Kirchoff equations. This is accomplished by tying "source" vertices to specified positive
 * voltages, "sink" vertices to 0 V, and iteratively updating the voltage of each other vertex to
 * the (weighted) average of the voltages of its neighbors.
 *
 * <p>The resultant voltages will all be in the range <code>[0, max]</code> where <code>max</code>
 * is the largest voltage of any source vertex (in the absence of negative source voltages; see
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
public class VoltageScorer<V, E> extends AbstractIterativeScorer<V, E, Double>
    implements VertexScorer<V, Double> {
  protected Map<V, ? extends Number> source_voltages;
  protected Set<V> sinks;

  /**
   * Creates an instance with the specified graph, edge weights, source voltages, and sinks.
   *
   * @param g the input graph
   * @param edge_weights the edge weights, representing conductivity
   * @param source_voltages the (fixed) voltage for each source
   * @param sinks the vertices whose voltages are tied to 0
   */
  public VoltageScorer(
      Network<V, E> g,
      Function<? super E, ? extends Number> edge_weights,
      Map<V, ? extends Number> source_voltages,
      Set<V> sinks) {
    super(g, edge_weights);
    this.source_voltages = source_voltages;
    this.sinks = sinks;
    initialize();
  }

  /**
   * Creates an instance with the specified graph, edge weights, source vertices (each of whose
   * 'voltages' are tied to 1), and sinks.
   *
   * @param g the input graph
   * @param edge_weights the edge weights, representing conductivity
   * @param sources the vertices whose voltages are tied to 1
   * @param sinks the vertices whose voltages are tied to 0
   */
  public VoltageScorer(
      Network<V, E> g,
      Function<? super E, ? extends Number> edge_weights,
      Set<V> sources,
      Set<V> sinks) {
    super(g, edge_weights);

    Map<V, Double> unit_voltages = new HashMap<V, Double>();
    for (V v : sources) {
      unit_voltages.put(v, new Double(1.0));
    }
    this.source_voltages = unit_voltages;
    this.sinks = sinks;
    initialize();
  }

  /**
   * Creates an instance with the specified graph, source vertices (each of whose 'voltages' are
   * tied to 1), and sinks. The outgoing edges for each vertex are assigned weights that sum to 1.
   *
   * @param g the input graph
   * @param sources the vertices whose voltages are tied to 1
   * @param sinks the vertices whose voltages are tied to 0
   */
  public VoltageScorer(Network<V, E> g, Set<V> sources, Set<V> sinks) {
    super(g);

    Map<V, Double> unit_voltages = new HashMap<V, Double>();
    for (V v : sources) {
      unit_voltages.put(v, new Double(1.0));
    }
    this.source_voltages = unit_voltages;
    this.sinks = sinks;
    initialize();
  }

  /**
   * Creates an instance with the specified graph, source voltages, and sinks. The outgoing edges
   * for each vertex are assigned weights that sum to 1.
   *
   * @param g the input graph
   * @param source_voltages the (fixed) voltage for each source
   * @param sinks the vertices whose voltages are tied to 0
   */
  public VoltageScorer(Network<V, E> g, Map<V, ? extends Number> source_voltages, Set<V> sinks) {
    super(g);
    this.source_voltages = source_voltages;
    this.sinks = sinks;
    this.edge_weights = new UniformDegreeWeight<V, E>(g);
    initialize();
  }

  /**
   * Creates an instance with the specified graph, edge weights, source, and sink. The source vertex
   * voltage is tied to 1.
   *
   * @param g the input graph
   * @param edge_weights the edge weights, representing conductivity
   * @param source the vertex whose voltage is tied to 1
   * @param sink the vertex whose voltage is tied to 0
   */
  public VoltageScorer(
      Network<V, E> g, Function<? super E, ? extends Number> edge_weights, V source, V sink) {
    this(g, edge_weights, ImmutableMap.of(source, 1.0), ImmutableSet.of(sink));
    initialize();
  }

  /**
   * Creates an instance with the specified graph, edge weights, source, and sink. The source vertex
   * voltage is tied to 1. The outgoing edges for each vertex are assigned weights that sum to 1.
   *
   * @param g the input graph
   * @param source the vertex whose voltage is tied to 1
   * @param sink the vertex whose voltage is tied to 0
   */
  public VoltageScorer(Network<V, E> g, V source, V sink) {
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

    for (Map.Entry<V, ? extends Number> entry : source_voltages.entrySet()) {
      V v = entry.getKey();
      if (sinks.contains(v)) {
        throw new IllegalArgumentException(
            "Vertex " + v + " is incorrectly specified as both source and sink");
      }
      double value = entry.getValue().doubleValue();
      if (value <= 0) {
        throw new IllegalArgumentException("Source vertex " + v + " has negative voltage");
      }
    }

    // set up initial voltages
    for (V v : graph.nodes()) {
      if (source_voltages.containsKey(v)) {
        setOutputValue(v, source_voltages.get(v).doubleValue());
      } else {
        setOutputValue(v, 0.0);
      }
    }
  }

  /** @see edu.uci.ics.jung.algorithms.scoring.AbstractIterativeScorer#update(Object) */
  @Override
  public double update(V v) {
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
    for (V u : graph.predecessors(v)) {
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
