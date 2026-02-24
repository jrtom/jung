/**
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description. Created on Aug 22, 2008
 */
package edu.uci.ics.jung.algorithms.scoring

import com.google.common.base.Preconditions
import com.google.common.graph.Network
import edu.uci.ics.jung.algorithms.scoring.util.ScoringUtils
import java.util.function.Function

/**
 * A special case of [PageRankWithPriors] in which the final scores represent a probability
 * distribution over position assuming a random (Markovian) walk of exactly k steps, based on the
 * initial distribution specified by the priors.
 *
 * **NOTE**: The version of `KStepMarkov` in `algorithms.importance` (and in JUNG
 * 1.x) is believed to be incorrect: rather than returning a score which represents a probability
 * distribution over position assuming a k-step random walk, it returns a score which represents the
 * sum over all steps of the probability for each step. If you want that behavior, set the
 * 'cumulative' flag as follows *before calling `evaluate()`*:
 *
 * ```
 *     val ksm = KStepMarkov(...)
 *     ksm.setCumulative(true)
 *     ksm.evaluate()
 * ```
 *
 * By default, the 'cumulative' flag is set to false.
 *
 * NOTE: THIS CLASS IS NOT YET COMPLETE. USE AT YOUR OWN RISK. (The original behavior is captured
 * by the version still available in `algorithms.importance`.)
 *
 * @see "Algorithms for Estimating Relative Importance in Graphs by Scott White and Padhraic Smyth,
 *     2003"
 * @see PageRank
 * @see PageRankWithPriors
 */
class KStepMarkov<N : Any, E : Any> : PageRankWithPriors<N, E> {
  private var cumulative: Boolean = false

  /**
   * Creates an instance based on the specified graph, edge weights, node priors (initial scores),
   * and number of steps to take.
   *
   * @param graph the input graph
   * @param edge_weights the edge weights (transition probabilities)
   * @param node_priors the initial probability distribution (score assignment)
   * @param steps the number of times that `step()` will be called by `evaluate`
   */
  constructor(
    graph: Network<N, E>,
    edge_weights: Function<E, out Number>,
    node_priors: Function<N, Double>,
    steps: Int
  ) : super(graph, edge_weights, node_priors, 0.0) {
    initialize(steps)
  }

  /**
   * Creates an instance based on the specified graph, node priors (initial scores), and number of
   * steps to take. The edge weights (transition probabilities) are set to default values (a uniform
   * distribution over all outgoing edges).
   *
   * @param graph the input graph
   * @param node_priors the initial probability distribution (score assignment)
   * @param steps the number of times that `step()` will be called by `evaluate`
   */
  constructor(
    graph: Network<N, E>,
    node_priors: Function<N, Double>,
    steps: Int
  ) : super(graph, node_priors, 0.0) {
    initialize(steps)
  }

  /**
   * Creates an instance based on the specified graph and number of steps to take. The edge weights
   * (transition probabilities) and node initial scores (prior probabilities) are set to default
   * values (a uniform distribution over all outgoing edges, and a uniform distribution over all
   * nodes, respectively).
   *
   * @param graph the input graph
   * @param steps the number of times that `step()` will be called by `evaluate`
   */
  constructor(graph: Network<N, E>, steps: Int)
      : super(graph, ScoringUtils.getUniformRootPrior(graph.nodes()), 0.0) {
    initialize(steps)
  }

  private fun initialize(steps: Int) {
    Preconditions.checkArgument(steps >= 0, "Number of steps must be > 0")
    this.acceptDisconnectedGraph(false)
    this.max_iterations = steps
    this.tolerance = -1.0

    this.cumulative = false
  }

  /**
   * Specifies whether this instance should assign a score to each node based on the sum over all
   * steps of the probability for each step. See the class-level documentation for details.
   *
   * @param cumulative true if this instance should assign a cumulative score to each node
   */
  fun setCumulative(cumulative: Boolean) {
    this.cumulative = cumulative
  }

  /** Updates the value for this node. Called by `step()`. */
  override fun update(v: N): Double {
    if (!cumulative) {
      return super.update(v)
    }

    collectDisappearingPotential(v)

    var v_input = 0.0
    for (u in graph.predecessors(v)) {
      for (e in graph.edgesConnecting(u, v)) {
        v_input += getCurrentValue(u) * getEdgeWeight(u, e).toDouble()
      }
    }

    // modify total_input according to alpha
    val new_value = if (alpha > 0) v_input * (1 - alpha) + getNodePrior(v) * alpha else v_input
    setOutputValue(v, new_value + getCurrentValue(v))

    // FIXME: DO WE NEED TO CHANGE HOW DISAPPEARING IS COUNTED?  NORMALIZE?

    return Math.abs(getCurrentValue(v) - new_value)
  }
}
