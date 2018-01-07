/*
 * Created on Jul 6, 2007
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
import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.scoring.util.DelegateToEdgeTransformer;
import edu.uci.ics.jung.algorithms.scoring.util.VEPair;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * An abstract class for algorithms that assign scores to nodes based on iterative methods.
 * Generally, any (concrete) subclass will function by creating an instance, and then either calling
 * <code>evaluate</code> (if the user wants to iterate until the algorithms is 'done') or repeatedly
 * call <code>step</code> (if the user wants to observe the values at each step).
 */
public abstract class AbstractIterativeScorer<N, E, T>
    implements IterativeContext, NodeScorer<N, T> {
  /** Maximum number of iterations to use before terminating. Defaults to 100. */
  protected int max_iterations;

  /**
   * Minimum change from one step to the next; if all changes are &le; tolerance, no further updates
   * will occur. Defaults to 0.001.
   */
  protected double tolerance;

  /** The graph on which the calculations are to be made. */
  protected Network<N, E> graph;

  /** The total number of iterations used so far. */
  protected int total_iterations;

  /** The edge weights used by this algorithm. */
  protected Function<VEPair<N, E>, ? extends Number> edge_weights;

  /** The map in which the output values are stored. */
  private Map<N, T> output;

  /** The map in which the current values are stored. */
  private Map<N, T> current_values;

  /**
   * A flag representing whether this instance tolerates disconnected graphs. Instances that do not
   * accept disconnected graphs may have unexpected behavior on disconnected graphs; they are not
   * guaranteed to do an explicit check. Defaults to true.
   */
  private boolean accept_disconnected_graph;

  /**
   * Sets the output value for this node.
   *
   * @param v the node whose output value is to be set
   * @param value the value to set
   */
  protected void setOutputValue(N v, T value) {
    output.put(v, value);
  }

  /**
   * Gets the output value for this node.
   *
   * @param v the node whose output value is to be retrieved
   * @return the output value for this node
   */
  protected T getOutputValue(N v) {
    return output.get(v);
  }

  /**
   * Gets the current value for this node
   *
   * @param v the node whose current value is to be retrieved
   * @return the current value for this node
   */
  protected T getCurrentValue(N v) {
    return current_values.get(v);
  }

  /**
   * Sets the current value for this node.
   *
   * @param v the node whose current value is to be set
   * @param value the current value to set
   */
  protected void setCurrentValue(N v, T value) {
    current_values.put(v, value);
  }

  /** The largest change seen so far among all node scores. */
  protected double max_delta;

  /**
   * Creates an instance for the specified graph and edge weights.
   *
   * @param g the graph for which the instance is to be created
   * @param edge_weights the edge weights for this instance
   */
  public AbstractIterativeScorer(
      Network<N, E> g, Function<? super E, ? extends Number> edge_weights) {
    this.graph = g;
    this.max_iterations = 100;
    this.tolerance = 0.001;
    this.accept_disconnected_graph = true;
    setEdgeWeights(edge_weights);
  }

  /**
   * Creates an instance for the specified graph <code>g</code>. NOTE: This constructor does not set
   * the internal <code>edge_weights</code> variable. If this variable is used by the subclass which
   * invoked this constructor, it must be initialized by that subclass.
   *
   * @param g the graph for which the instance is to be created
   */
  public AbstractIterativeScorer(Network<N, E> g) {
    this.graph = g;
    this.max_iterations = 100;
    this.tolerance = 0.001;
    this.accept_disconnected_graph = true;
  }

  /** Initializes the internal state for this instance. */
  protected void initialize() {
    this.total_iterations = 0;
    this.max_delta = Double.MIN_VALUE;
    this.current_values = new HashMap<N, T>();
    this.output = new HashMap<N, T>();
  }

  /** Steps through this scoring algorithm until a termination condition is reached. */
  public void evaluate() {
    do {
      step();
    } while (!done());
  }

  /**
   * Returns true if the total number of iterations is greater than or equal to <code>max_iterations
   * </code> or if the maximum value change observed is less than <code>tolerance</code>.
   */
  public boolean done() {
    return total_iterations >= max_iterations || max_delta < tolerance;
  }

  /** Performs one step of this algorithm; updates the state (value) for each node. */
  public void step() {
    swapOutputForCurrent();
    max_delta = 0;

    for (N v : graph.nodes()) {
      double diff = update(v);
      updateMaxDelta(v, diff);
    }
    total_iterations++;
    afterStep();
  }

  /** */
  protected void swapOutputForCurrent() {
    Map<N, T> tmp = output;
    output = current_values;
    current_values = tmp;
  }

  /**
   * Updates the value for <code>v</code>.
   *
   * @param v the node whose value is to be updated
   * @return the updated value
   */
  protected abstract double update(N v);

  protected void updateMaxDelta(N v, double diff) {
    max_delta = Math.max(max_delta, diff);
  }

  protected void afterStep() {}

  @Override
  public T getNodeScore(N v) {
    Preconditions.checkArgument(
        graph.nodes().contains(v), "Node %s not an element of this graph", v.toString());

    return output.get(v);
  }

  @Override
  public Map<N, T> nodeScores() {
    return Collections.unmodifiableMap(output);
  }

  /**
   * Returns the maximum number of iterations that this instance will use.
   *
   * @return the maximum number of iterations that <code>evaluate</code> will use prior to
   *     terminating
   */
  public int getMaxIterations() {
    return max_iterations;
  }

  /**
   * Returns the number of iterations that this instance has used so far.
   *
   * @return the number of iterations that this instance has used so far
   */
  public int getIterations() {
    return total_iterations;
  }

  /**
   * Sets the maximum number of times that <code>evaluate</code> will call <code>step</code>.
   *
   * @param max_iterations the maximum
   */
  public void setMaxIterations(int max_iterations) {
    this.max_iterations = max_iterations;
  }

  /**
   * Gets the size of the largest change (difference between the current and previous values) for
   * any node that can be tolerated. Once all changes are less than this value, <code>evaluate
   * </code> will terminate.
   *
   * @return the size of the largest change that evaluate() will permit
   */
  public double getTolerance() {
    return tolerance;
  }

  /**
   * Sets the size of the largest change (difference between the current and previous values) for
   * any node that can be tolerated.
   *
   * @param tolerance the size of the largest change that evaluate() will permit
   */
  public void setTolerance(double tolerance) {
    this.tolerance = tolerance;
  }

  /**
   * Returns the Function that this instance uses to associate edge weights with each edge.
   *
   * @return the Function that associates an edge weight with each edge
   */
  public Function<VEPair<N, E>, ? extends Number> getEdgeWeights() {
    return edge_weights;
  }

  /**
   * Sets the Function that this instance uses to associate edge weights with each edge
   *
   * @param edge_weights the Function to use to associate an edge weight with each edge
   * @see edu.uci.ics.jung.algorithms.scoring.util.UniformDegreeWeight
   */
  public void setEdgeWeights(Function<? super E, ? extends Number> edge_weights) {
    this.edge_weights = new DelegateToEdgeTransformer<N, E>(edge_weights);
  }

  /**
   * Gets the edge weight for <code>e</code> in the context of its (incident) node <code>v</code>.
   *
   * @param v the node incident to e as a context in which the edge weight is to be calculated
   * @param e the edge whose weight is to be returned
   * @return the edge weight for <code>e</code> in the context of its (incident) node <code>v
   *     </code>
   */
  protected Number getEdgeWeight(N v, E e) {
    return edge_weights.apply(new VEPair<N, E>(v, e));
  }

  /**
   * Collects the 'potential' from v (its current value) if it has no outgoing edges; this can then
   * be redistributed among the other nodes as a means of normalization.
   *
   * @param v the node whose potential is being collected
   */
  protected void collectDisappearingPotential(N v) {}

  /**
   * Specifies whether this instance should accept nodes with no outgoing edges.
   *
   * @param accept true if this instance should accept nodes with no outgoing edges, false otherwise
   */
  public void acceptDisconnectedGraph(boolean accept) {
    this.accept_disconnected_graph = accept;
  }

  /**
   * Returns true if this instance accepts nodes with no outgoing edges, and false otherwise.
   *
   * @return true if this instance accepts nodes with no outgoing edges, otherwise false
   */
  public boolean isDisconnectedGraphOK() {
    return this.accept_disconnected_graph;
  }
}
