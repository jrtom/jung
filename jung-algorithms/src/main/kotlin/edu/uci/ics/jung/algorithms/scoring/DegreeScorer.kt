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

import com.google.common.collect.Maps
import com.google.common.graph.Network

/**
 * Assigns a score to each node equal to its degree.
 *
 * @param N the node type
 */
class DegreeScorer<N : Any>(
  // TODO: Graph and Network degree are different, so we either need to
  // provide separate constructors or separate classes
  /** The graph for which scores are to be generated. */
  protected val graph: Network<N, *>
) : NodeScorer<N, Int> {

  /**
   * Returns the degree of the node.
   *
   * @return the degree of the node
   */
  override fun getNodeScore(node: N): Int = graph.degree(node)

  override fun nodeScores(): Map<N, Int> =
    Maps.asMap(graph.nodes()) { node -> graph.degree(node!!) }
}
