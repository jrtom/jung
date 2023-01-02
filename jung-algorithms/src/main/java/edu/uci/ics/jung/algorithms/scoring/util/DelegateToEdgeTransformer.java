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
package edu.uci.ics.jung.algorithms.scoring.util;

import java.util.function.Function;

/**
 * A {@code Transformer<VEPair,Number} that delegates its operation to a {@code
 * Transformer<E,Number>}. Mainly useful for technical reasons inside AbstractIterativeScorer; in
 * essence it allows the edge weight instance variable to be of type <code>VEPair,W</code> even if
 * the edge weight <code>Transformer</code> only operates on edges.
 */
public class DelegateToEdgeTransformer<N, E> implements Function<VEPair<N, E>, Number> {
  /** The Function to which this instance delegates its function. */
  protected Function<? super E, ? extends Number> delegate;

  /**
   * Creates an instance with the specified delegate Function.
   *
   * @param delegate the Function to which this instance will delegate
   */
  public DelegateToEdgeTransformer(Function<? super E, ? extends Number> delegate) {
    this.delegate = delegate;
  }

  /**
   * @see Function#apply(Object)
   */
  public Number apply(VEPair<N, E> arg0) {
    return delegate.apply(arg0.getE());
  }
}
