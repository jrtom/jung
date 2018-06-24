package edu.uci.ics.jung.layout.algorithms;

import edu.uci.ics.jung.algorithms.util.IterativeContext;

/**
 * A LayoutAlgorithm that may utilize a pre-relax phase, which is a loop of calls to <code>step
 * </code> that occurr in the current thread instead of in a new Thread. The purpose of <code>
 * preRelax()</code> is to rapidly reach an initial state before spawning a new Thread to perform a
 * more lengthy relax operation.
 *
 * @param <N> the Node type
 */
public interface IterativeLayoutAlgorithm<N> extends LayoutAlgorithm<N>, IterativeContext {
  /**
   * may be a no-op depending on how the algorithm instance is created
   *
   * @return true if a prerelax was done, false otherwise
   */
  boolean preRelax();
}
