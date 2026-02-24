package edu.uci.ics.jung.layout.algorithms

import edu.uci.ics.jung.algorithms.util.IterativeContext

/**
 * A LayoutAlgorithm that may utilize a pre-relax phase, which is a loop of calls to `step`
 * that occur in the current thread instead of in a new Thread. The purpose of `preRelax()`
 * is to rapidly reach an initial state before spawning a new Thread to perform a more lengthy
 * relax operation.
 *
 * @param N the Node type
 */
interface IterativeLayoutAlgorithm<N : Any> : LayoutAlgorithm<N>, IterativeContext {
  /**
   * may be a no-op depending on how the algorithm instance is created
   *
   * @return true if a prerelax was done, false otherwise
   */
  fun preRelax(): Boolean
}
