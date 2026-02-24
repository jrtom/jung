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

import com.google.common.base.Preconditions
import com.google.common.graph.Network
import java.util.function.Function

/**
 * A generalization of HITS that permits non-uniformly-distributed random jumps. The 'node_priors'
 * (that is, prior probabilities for each node) may be thought of as the fraction of the total
 * 'potential' (hub or authority score) that is assigned to that node out of the portion that is
 * assigned according to random jumps.
 *
 * @see "Algorithms for Estimating Relative Importance in Graphs by Scott White and Padhraic Smyth,
 *     2003"
 */
open class HITSWithPriors<N : Any, E : Any>
    : AbstractIterativeScorerWithPriors<N, E, HITS.Scores> {

  /**
   * The sum of the potential, at each step, associated with nodes with no outedges (authority) or
   * no inedges (hub).
   */
  protected var disappearing_potential: HITS.Scores

  /**
   * Creates an instance for the specified graph, edge weights, node prior probabilities, and random
   * jump probability (alpha).
   *
   * @param g the input graph
   * @param edge_weights the edge weights
   * @param node_priors the prior probability for each node
   * @param alpha the probability of a random jump at each step
   */
  constructor(
    g: Network<N, E>,
    edge_weights: Function<E, out Number>,
    node_priors: Function<N, HITS.Scores>,
    alpha: Double
  ) : super(g, edge_weights, node_priors, alpha) {
    disappearing_potential = HITS.Scores(0.0, 0.0)
  }

  /**
   * Creates an instance for the specified graph, node priors, and random jump probability (alpha).
   * The edge weights default to 1.0.
   *
   * @param g the input graph
   * @param node_priors the prior probability for each node
   * @param alpha the probability of a random jump at each step
   */
  constructor(
    g: Network<N, E>,
    node_priors: Function<N, HITS.Scores>,
    alpha: Double
  ) : super(g, Function { 1.0 }, node_priors, alpha) {
    disappearing_potential = HITS.Scores(0.0, 0.0)
  }

  /** Updates the value for this node. */
  override fun update(v: N): Double {
    collectDisappearingPotential(v)

    var v_auth = 0.0
    for (u in graph.predecessors(v)) {
      for (e in graph.edgesConnecting(u, v)) {
        v_auth += getCurrentValue(u).hub * getEdgeWeight(u, e).toDouble()
      }
    }

    var v_hub = 0.0
    for (w in graph.successors(v)) {
      for (e in graph.edgesConnecting(v, w)) {
        v_hub += getCurrentValue(w).authority * getEdgeWeight(w, e).toDouble()
      }
    }

    // modify total_input according to alpha
    if (alpha > 0) {
      v_auth = v_auth * (1 - alpha) + getNodePrior(v).authority * alpha
      v_hub = v_hub * (1 - alpha) + getNodePrior(v).hub * alpha
    }
    setOutputValue(v, HITS.Scores(v_hub, v_auth))

    return Math.max(
      Math.abs(getCurrentValue(v).hub - v_hub),
      Math.abs(getCurrentValue(v).authority - v_auth)
    )
  }

  /**
   * Code which is executed after each step. In this case, deals with the 'disappearing potential',
   * normalizes the scores, and then calls `super.afterStep()`.
   *
   * @see .collectDisappearingPotential
   */
  override fun afterStep() {
    if (disappearing_potential.hub > 0 || disappearing_potential.authority > 0) {
      for (v in graph.nodes()) {
        val new_hub =
          getOutputValue(v).hub +
              (1 - alpha) * (disappearing_potential.hub * getNodePrior(v).hub)
        val new_auth =
          getOutputValue(v).authority +
              (1 - alpha) * (disappearing_potential.authority * getNodePrior(v).authority)
        setOutputValue(v, HITS.Scores(new_hub, new_auth))
      }
      disappearing_potential.hub = 0.0
      disappearing_potential.authority = 0.0
    }

    normalizeScores()

    super.afterStep()
  }

  /**
   * Normalizes scores so that sum of their squares = 1. This method may be overridden so as to
   * yield different normalizations.
   */
  protected open fun normalizeScores() {
    var hub_ssum = 0.0
    var auth_ssum = 0.0
    for (v in graph.nodes()) {
      val hub_val = getOutputValue(v).hub
      val auth_val = getOutputValue(v).authority
      hub_ssum += hub_val * hub_val
      auth_ssum += auth_val * auth_val
    }

    hub_ssum = Math.sqrt(hub_ssum)
    auth_ssum = Math.sqrt(auth_ssum)

    for (v in graph.nodes()) {
      val values = getOutputValue(v)
      setOutputValue(v, HITS.Scores(values.hub / hub_ssum, values.authority / auth_ssum))
    }
  }

  /**
   * Collects the "disappearing potential" associated with nodes that have either no incoming edges,
   * no outgoing edges, or both. Nodes that have no incoming edges do not directly contribute to the
   * hub scores of other nodes; similarly, nodes that have no outgoing edges do not directly
   * contribute to the authority scores of other nodes. These values are collected at each step and
   * then distributed across all nodes as a part of the normalization process. (This process is not
   * required for, and does not affect, the 'sum-of-squares'-style normalization.)
   */
  override fun collectDisappearingPotential(v: N) {
    if (graph.outDegree(v) == 0) {
      Preconditions.checkArgument(isDisconnectedGraphOK(), "Outdegree of $v must be > 0")
      disappearing_potential.hub += getCurrentValue(v).authority
    }
    if (graph.inDegree(v) == 0) {
      Preconditions.checkArgument(isDisconnectedGraphOK(), "Indegree of $v must be > 0")
      disappearing_potential.authority += getCurrentValue(v).hub
    }
  }
}
