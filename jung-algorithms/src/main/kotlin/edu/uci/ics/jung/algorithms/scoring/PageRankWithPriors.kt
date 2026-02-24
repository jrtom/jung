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
package edu.uci.ics.jung.algorithms.scoring

import com.google.common.base.Preconditions
import com.google.common.graph.Network
import edu.uci.ics.jung.algorithms.scoring.util.UniformDegreeWeight
import java.util.function.Function

/**
 * A generalization of PageRank that permits non-uniformly-distributed random jumps. The
 * 'node_priors' (that is, prior probabilities for each node) may be thought of as the fraction of
 * the total 'potential' that is assigned to that node at each step out of the portion that is
 * assigned according to random jumps (this portion is specified by 'alpha').
 *
 * @see "Algorithms for Estimating Relative Importance in Graphs by Scott White and Padhraic Smyth,
 *     2003"
 * @see PageRank
 */
open class PageRankWithPriors<N : Any, E : Any>
    : AbstractIterativeScorerWithPriors<N, E, Double> {

  /** Maintains the amount of potential associated with nodes with no out-edges. */
  protected var disappearing_potential: Double = 0.0

  /**
   * Creates an instance with the specified graph, edge weights, node priors, and 'random jump'
   * probability (alpha).
   *
   * @param graph the input graph
   * @param edge_weights the edge weights, denoting transition probabilities from source to destination
   * @param node_priors the prior probabilities for each node
   * @param alpha the probability of executing a 'random jump' at each step
   */
  constructor(
    graph: Network<N, E>,
    edge_weights: Function<E, out Number>,
    node_priors: Function<N, Double>,
    alpha: Double
  ) : super(graph, edge_weights, node_priors, alpha)

  /**
   * Creates an instance with the specified graph, node priors, and 'random jump' probability
   * (alpha). The outgoing edge weights for each node will be equal and sum to 1.
   *
   * @param graph the input graph
   * @param node_priors the prior probabilities for each node
   * @param alpha the probability of executing a 'random jump' at each step
   */
  constructor(
    graph: Network<N, E>,
    node_priors: Function<N, Double>,
    alpha: Double
  ) : super(graph, node_priors, alpha) {
    this.edge_weights = UniformDegreeWeight(graph)
  }

  /** Updates the value for this node. Called by `step()`. */
  override fun update(v: N): Double {
    collectDisappearingPotential(v)

    var v_input = 0.0
    for (u in graph.predecessors(v)) {
      for (e in graph.edgesConnecting(u, v)) {
        v_input += getCurrentValue(u) * getEdgeWeight(u, e).toDouble()
      }
    }

    // modify total_input according to alpha
    val new_value = if (alpha > 0) v_input * (1 - alpha) + getNodePrior(v) * alpha else v_input
    setOutputValue(v, new_value)

    return Math.abs(getCurrentValue(v) - new_value)
  }

  /**
   * Cleans up after each step. In this case that involves allocating the disappearing potential
   * (thus maintaining normalization of the scores) according to the node probability priors, and
   * then calling `super.afterStep`.
   */
  override fun afterStep() {
    // distribute disappearing potential according to priors
    if (disappearing_potential > 0) {
      for (v in graph.nodes()) {
        setOutputValue(
          v, getOutputValue(v) + (1 - alpha) * (disappearing_potential * getNodePrior(v))
        )
      }
      disappearing_potential = 0.0
    }

    super.afterStep()
  }

  /**
   * Collects the "disappearing potential" associated with nodes that have no outgoing edges. Nodes
   * that have no outgoing edges do not directly contribute to the scores of other nodes. These
   * values are collected at each step and then distributed across all nodes as a part of the
   * normalization process.
   */
  override fun collectDisappearingPotential(v: N) {
    if (graph.outDegree(v) == 0) {
      Preconditions.checkState(isDisconnectedGraphOK(), "Outdegree of $v must be > 0")
      disappearing_potential += getCurrentValue(v)
    }
  }
}
