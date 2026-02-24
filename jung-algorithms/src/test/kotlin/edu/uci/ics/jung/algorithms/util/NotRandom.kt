package edu.uci.ics.jung.algorithms.util

import java.util.Random

/**
 * A decidedly non-random extension of [Random] that may be useful for testing random
 * algorithms that accept an instance of [Random] as a parameter. This algorithm maintains
 * internal counters which are incremented after each call, and returns values which are functions
 * of those counter values. Thus the output is not only deterministic (as is necessarily true of all
 * software with no externalities) but precisely predictable in distribution.
 *
 * @author Joshua O'Madadhain
 */
@Suppress("serial")
class NotRandom(private val size: Int = 100) : Random() {
  private var i = 0
  private var d = 0

  /** Returns the post-incremented value of the internal counter modulo n. */
  override fun nextInt(n: Int): Int = i++ % n

  /**
   * Returns the post-incremented value of the internal counter modulo [size], divided by
   * [size].
   */
  override fun nextDouble(): Double = (d++ % size) / size.toDouble()

  /**
   * Returns the post-incremented value of the internal counter modulo [size], divided by
   * [size].
   */
  override fun nextFloat(): Float = (d++ % size) / size.toFloat()
}
