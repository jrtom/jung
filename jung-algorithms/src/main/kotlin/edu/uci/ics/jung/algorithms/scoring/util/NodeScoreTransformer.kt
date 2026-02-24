/*
 * Created on Jul 18, 2008
 *
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.scoring.util

import edu.uci.ics.jung.algorithms.scoring.NodeScorer
import java.util.function.Function

/**
 * A Function convenience wrapper around NodeScorer.
 */
class NodeScoreTransformer<N : Any, S>(
  /** The NodeScorer instance that provides the values returned by `apply`. */
  protected val vs: NodeScorer<N, S>
) : Function<N, S> {

  /**
   * @param v the node whose score is being returned
   * @return the score for this node.
   */
  override fun apply(v: N): S = vs.getNodeScore(v)
}
