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

/**
 * An interface for algorithms that assign scores to nodes.
 *
 * @param N the node type
 * @param S the score type
 */
interface NodeScorer<N : Any, S> {
  /**
   * @param v the node whose score is requested
   * @return the algorithm's score for this node
   */
  fun getNodeScore(v: N): S

  fun nodeScores(): Map<N, S>
}
