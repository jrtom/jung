/*
 * Created on Jul 11, 2008
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

import java.util.function.Function

/**
 * A `Function<VEPair, Number>` that delegates its operation to a `Function<E, Number>`.
 * Mainly useful for technical reasons inside AbstractIterativeScorer; in essence it allows the
 * edge weight instance variable to be of type `VEPair, W` even if the edge weight `Function`
 * only operates on edges.
 */
class DelegateToEdgeTransformer<N : Any, E : Any>(
  /** The Function to which this instance delegates its function. */
  protected val delegate: Function<in E, out Number>
) : Function<VEPair<N, E>, Number> {

  /** @see Function.apply */
  override fun apply(arg0: VEPair<N, E>): Number = delegate.apply(arg0.e)
}
