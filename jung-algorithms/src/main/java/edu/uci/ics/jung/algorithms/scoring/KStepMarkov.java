/**
 * Copyright (c) 2008, The JUNG Authors
 *
 * <p>All rights reserved.
 *
 * <p>This software is open-source under the BSD license; see either "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description. Created on Aug 22, 2008
 */
package edu.uci.ics.jung.algorithms.scoring;

import com.google.common.base.Preconditions;
import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.scoring.util.ScoringUtils;
import java.util.function.Function;

/**
 * A special case of {@code PageRankWithPriors} in which the final scores represent a probability
 * distribution over position assuming a random (Markovian) walk of exactly k steps, based on the
 * initial distribution specified by the priors.
 *
 * <p><b>NOTE</b>: The version of {@code KStepMarkov} in {@code algorithms.importance} (and in JUNG
 * 1.x) is believed to be incorrect: rather than returning a score which represents a probability
 * distribution over position assuming a k-step random walk, it returns a score which represents the
 * sum over all steps of the probability for each step. If you want that behavior, set the
 * 'cumulative' flag as follows <i>before calling {@code evaluate()}</i>:
 *
 * <pre>
 *     KStepMarkov ksm = new KStepMarkov(...);
 *     ksm.setCumulative(true);
 *     ksm.evaluate();
 * </pre>
 *
 * By default, the 'cumulative' flag is set to false.
 *
 * <p>NOTE: THIS CLASS IS NOT YET COMPLETE. USE AT YOUR OWN RISK. (The original behavior is captured
 * by the version still available in {@code algorithms.importance}.)
 *
 * @see "Algorithms for Estimating Relative Importance in Graphs by Scott White and Padhraic Smyth,
 *     2003"
 * @see PageRank
 * @see PageRankWithPriors
 */
public class KStepMarkov<N, E> extends PageRankWithPriors<N, E> {
  private boolean cumulative;

  /**
   * Creates an instance based on the specified graph, edge weights, node priors (initial scores),
   * and number of steps to take.
   *
   * @param graph the input graph
   * @param edge_weights the edge weights (transition probabilities)
   * @param node_priors the initial probability distribution (score assignment)
   * @param steps the number of times that {@code step()} will be called by {@code evaluate}
   */
  public KStepMarkov(
      Network<N, E> graph,
      Function<E, ? extends Number> edge_weights,
      Function<N, Double> node_priors,
      int steps) {
    super(graph, edge_weights, node_priors, 0);
    initialize(steps);
  }

  /**
   * Creates an instance based on the specified graph, node priors (initial scores), and number of
   * steps to take. The edge weights (transition probabilities) are set to default values (a uniform
   * distribution over all outgoing edges).
   *
   * @param graph the input graph
   * @param node_priors the initial probability distribution (score assignment)
   * @param steps the number of times that {@code step()} will be called by {@code evaluate}
   */
  public KStepMarkov(Network<N, E> graph, Function<N, Double> node_priors, int steps) {
    super(graph, node_priors, 0);
    initialize(steps);
  }

  /**
   * Creates an instance based on the specified graph and number of steps to take. The edge weights
   * (transition probabilities) and node initial scores (prior probabilities) are set to default
   * values (a uniform distribution over all outgoing edges, and a uniform distribution over all
   * nodes, respectively).
   *
   * @param graph the input graph
   * @param steps the number of times that {@code step()} will be called by {@code evaluate}
   */
  public KStepMarkov(Network<N, E> graph, int steps) {
    super(graph, ScoringUtils.getUniformRootPrior(graph.nodes()), 0);
    initialize(steps);
  }

  private void initialize(int steps) {
    Preconditions.checkArgument(steps >= 0, "Number of steps must be > 0");
    this.acceptDisconnectedGraph(false);
    this.max_iterations = steps;
    this.tolerance = -1.0;

    this.cumulative = false;
  }

  /**
   * Specifies whether this instance should assign a score to each node based on the sum over all
   * steps of the probability for each step. See the class-level documentation for details.
   *
   * @param cumulative true if this instance should assign a cumulative score to each node
   */
  public void setCumulative(boolean cumulative) {
    this.cumulative = cumulative;
  }

  /** Updates the value for this node. Called by <code>step()</code>. */
  @Override
  public double update(N v) {
    if (!cumulative) {
      return super.update(v);
    }

    collectDisappearingPotential(v);

    double v_input = 0;
    for (N u : graph.predecessors(v)) {
      for (E e : graph.edgesConnecting(u, v)) {
        v_input += (getCurrentValue(u) * getEdgeWeight(u, e).doubleValue());
      }
    }

    // modify total_input according to alpha
    double new_value = alpha > 0 ? v_input * (1 - alpha) + getNodePrior(v) * alpha : v_input;
    setOutputValue(v, new_value + getCurrentValue(v));

    // FIXME: DO WE NEED TO CHANGE HOW DISAPPEARING IS COUNTED?  NORMALIZE?

    return Math.abs(getCurrentValue(v) - new_value);
  }
}
