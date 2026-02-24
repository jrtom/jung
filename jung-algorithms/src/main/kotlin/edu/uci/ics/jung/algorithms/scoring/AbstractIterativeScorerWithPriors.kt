/*
 * Created on Jul 14, 2007
 *
 * Copyright (c) 2007, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.scoring

import com.google.common.graph.Network
import java.util.function.Function

/**
 * An abstract class for iterative random-walk-based node scoring algorithms that have a fixed
 * probability, for each node, of 'jumping' to that node at each step in the algorithm (rather than
 * following a link out of that node).
 *
 * @param N the node type
 * @param E the edge type
 * @param S the score type
 */
abstract class AbstractIterativeScorerWithPriors<N : Any, E : Any, S>
    : AbstractIterativeScorer<N, E, S>, NodeScorer<N, S> {

  /**
   * The prior probability of each node being visited on a given 'jump' (non-link-following) step.
   */
  protected val node_priors: Function<in N, out S>

  /** The probability of making a 'jump' at each step. */
  protected val alpha: Double

  /**
   * Creates an instance for the specified graph, edge weights, node priors, and jump probability.
   *
   * @param g the graph whose nodes are to be assigned scores
   * @param edge_weights the edge weights to use in the score assignment
   * @param node_priors the prior probabilities of each node being 'jumped' to
   * @param alpha the probability of making a 'jump' at each step
   */
  constructor(
    g: Network<N, E>,
    edge_weights: Function<in E, out Number>,
    node_priors: Function<in N, out S>,
    alpha: Double
  ) : super(g, edge_weights) {
    this.node_priors = node_priors
    this.alpha = alpha
    initialize()
  }

  /**
   * Creates an instance for the specified graph, node priors, and jump probability, with edge
   * weights specified by the subclass.
   *
   * @param g the graph whose nodes are to be assigned scores
   * @param node_priors the prior probabilities of each node being 'jumped' to
   * @param alpha the probability of making a 'jump' at each step
   */
  constructor(
    g: Network<N, E>,
    node_priors: Function<N, out S>,
    alpha: Double
  ) : super(g) {
    this.node_priors = node_priors
    this.alpha = alpha
    initialize()
  }

  /** Initializes the state of this instance. */
  override fun initialize() {
    super.initialize()
    // initialize output values to priors
    // (output and current are swapped before each step(), so current will
    // have priors when update()s start happening)
    for (v in graph.nodes()) {
      setOutputValue(v, getNodePrior(v))
    }
  }

  /**
   * Returns the prior probability for `v`.
   *
   * @param v the node whose prior probability is being queried
   * @return the prior probability for `v`
   */
  protected fun getNodePrior(v: N): S = node_priors.apply(v)

  /**
   * Returns a Function which maps each node to its prior probability.
   *
   * @return a Function which maps each node to its prior probability
   */
  fun getNodePriors(): Function<in N, out S> = node_priors

}
