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
package edu.uci.ics.jung.algorithms.scoring.util;

import edu.uci.ics.jung.algorithms.scoring.NodeScorer;
import java.util.function.Function;

/** A Function convenience wrapper around NodeScorer. */
public class NodeScoreTransformer<N, S> implements Function<N, S> {
  /** The NodeScorer instance that provides the values returned by <code>transform</code>. */
  protected NodeScorer<N, S> vs;

  /**
   * Creates an instance based on the specified NodeScorer.
   *
   * @param vs the NodeScorer which will retrieve the score for each node
   */
  public NodeScoreTransformer(NodeScorer<N, S> vs) {
    this.vs = vs;
  }

  /**
   * @param v the node whose score is being returned
   * @return the score for this node.
   */
  public S apply(N v) {
    return vs.getNodeScore(v);
  }
}
