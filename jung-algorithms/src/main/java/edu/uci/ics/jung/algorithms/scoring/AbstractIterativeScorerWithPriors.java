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
package edu.uci.ics.jung.algorithms.scoring;

import com.google.common.graph.Network;
import java.util.function.Function;

/**
 * An abstract class for iterative random-walk-based node scoring algorithms that have a fixed
 * probability, for each node, of 'jumping' to that node at each step in the algorithm (rather than
 * following a link out of that node).
 *
 * @param <N> the node type
 * @param <E> the edge type
 * @param <S> the score type
 */
public abstract class AbstractIterativeScorerWithPriors<N, E, S>
    extends AbstractIterativeScorer<N, E, S> implements NodeScorer<N, S> {
  /**
   * The prior probability of each node being visited on a given 'jump' (non-link-following) step.
   */
  protected Function<? super N, ? extends S> node_priors;

  /** The probability of making a 'jump' at each step. */
  protected double alpha;

  /**
   * Creates an instance for the specified graph, edge weights, node priors, and jump probability.
   *
   * @param g the graph whose nodes are to be assigned scores
   * @param edge_weights the edge weights to use in the score assignment
   * @param node_priors the prior probabilities of each node being 'jumped' to
   * @param alpha the probability of making a 'jump' at each step
   */
  public AbstractIterativeScorerWithPriors(
      Network<N, E> g,
      Function<? super E, ? extends Number> edge_weights,
      Function<? super N, ? extends S> node_priors,
      double alpha) {
    super(g, edge_weights);
    this.node_priors = node_priors;
    this.alpha = alpha;
    initialize();
  }

  /**
   * Creates an instance for the specified graph, node priors, and jump probability, with edge
   * weights specified by the subclass.
   *
   * @param g the graph whose nodes are to be assigned scores
   * @param node_priors the prior probabilities of each node being 'jumped' to
   * @param alpha the probability of making a 'jump' at each step
   */
  public AbstractIterativeScorerWithPriors(
      Network<N, E> g, Function<N, ? extends S> node_priors, double alpha) {
    super(g);
    this.node_priors = node_priors;
    this.alpha = alpha;
    initialize();
  }

  /** Initializes the state of this instance. */
  @Override
  public void initialize() {
    super.initialize();
    // initialize output values to priors
    // (output and current are swapped before each step(), so current will
    // have priors when update()s start happening)
    for (N v : graph.nodes()) {
      setOutputValue(v, getNodePrior(v));
    }
  }

  /**
   * Returns the prior probability for <code>v</code>.
   *
   * @param v the node whose prior probability is being queried
   * @return the prior probability for <code>v</code>
   */
  protected S getNodePrior(N v) {
    return node_priors.apply(v);
  }

  /**
   * Returns a Function which maps each node to its prior probability.
   *
   * @return a Function which maps each node to its prior probability
   */
  public Function<? super N, ? extends S> getNodePriors() {
    return node_priors;
  }

  /**
   * Returns the probability of making a 'jump' (non-link-following step).
   *
   * @return the probability of making a 'jump' (non-link-following step)
   */
  public double getAlpha() {
    return alpha;
  }
}
