/*
 * Created on Jul 12, 2007
 *
 * Copyright (c) 2007, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.scoring.util

import edu.uci.ics.jung.algorithms.scoring.HITS
import java.util.function.Function

/**
 * Methods for assigning values (to be interpreted as prior probabilities) to nodes in the context
 * of random-walk-based scoring algorithms.
 */
object ScoringUtils {
  /**
   * Assigns a probability of 1/`roots.size()` to each of the elements of `roots`.
   *
   * @param N the node type
   * @param roots the nodes to be assigned nonzero prior probabilities
   * @return a Function assigning a uniform prior to each element in [roots]
   */
  @JvmStatic
  fun <N : Any> getUniformRootPrior(roots: Collection<N>): Function<N, Double> =
    Function { input ->
      if (roots.contains(input)) 1.0 / roots.size
      else 0.0
    }

  /**
   * Returns a Function that hub and authority values of 1/`roots.size()` to each element
   * of `roots`.
   *
   * @param N the node type
   * @param roots the nodes to be assigned nonzero scores
   * @return a Function that assigns uniform prior hub/authority probabilities to each root
   */
  @JvmStatic
  fun <N : Any> getHITSUniformRootPrior(roots: Collection<N>): Function<N, HITS.Scores> =
    Function { input ->
      if (roots.contains(input))
        HITS.Scores(1.0 / roots.size, 1.0 / roots.size)
      else
        HITS.Scores(0.0, 0.0)
    }
}
