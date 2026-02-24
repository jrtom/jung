/**
 * Copyright (c) 2009, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description. Created on Jan 8, 2009
 */
package edu.uci.ics.jung.algorithms.util

import com.google.common.base.Preconditions
import java.util.LinkedList
import java.util.Random

/**
 * Selects items according to their probability in an arbitrary probability distribution. The
 * distribution is specified by a [Map] from items (of type [T]) to weights of type
 * [Number], supplied to the constructor; these weights are normalized internally to act as
 * probabilities.
 *
 * This implementation selects items in O(1) time, and requires O(n) space.
 *
 * @author Joshua O'Madadhain
 */
class WeightedChoice<T : Any>(
  item_weights: Map<T, out Number>,
  private var random: Random,
  threshold: Double
) {

  private val item_pairs: MutableList<ItemPair>

  /**
   * Equivalent to `this(item_weights, Random(), DEFAULT_THRESHOLD)`.
   *
   * @param item_weights a map from items to their weights
   */
  constructor(item_weights: Map<T, out Number>) : this(item_weights, Random(), DEFAULT_THRESHOLD)

  /**
   * Equivalent to `this(item_weights, Random(), threshold)`.
   *
   * @param item_weights a map from items to their weights
   * @param threshold the minimum value that is treated as a probability (anything smaller will be
   *     considered equivalent to a floating-point rounding error)
   */
  constructor(item_weights: Map<T, out Number>, threshold: Double) : this(item_weights, Random(), threshold)

  /**
   * Equivalent to `this(item_weights, random, DEFAULT_THRESHOLD)`.
   *
   * @param item_weights a map from items to their weights
   * @param random the Random instance to use for selection
   */
  constructor(item_weights: Map<T, out Number>, random: Random) : this(item_weights, random, DEFAULT_THRESHOLD)

  init {
    Preconditions.checkArgument(item_weights.isNotEmpty(), "Item weights must be non-empty")

    val itemCount = item_weights.size
    item_pairs = ArrayList(itemCount)

    var sum = 0.0
    for ((_, value) in item_weights) {
      val v = value.toDouble()
      Preconditions.checkArgument(v > 0, "Weights must be > 0")
      sum += v
    }
    val bucketWeight = 1.0 / item_weights.size

    val lightWeights = LinkedList<ItemPair>()
    val heavyWeights = LinkedList<ItemPair>()
    for ((key, value) in item_weights) {
      val v = value.toDouble() / sum
      enqueueItem(key, v, bucketWeight, lightWeights, heavyWeights)
    }

    // repeat until both queues empty
    while (heavyWeights.isNotEmpty() || lightWeights.isNotEmpty()) {
      val heavyItem = heavyWeights.poll()
      val lightItem = lightWeights.poll()
      var lightWeight = 0.0
      var light: T? = null
      var heavy: T? = null
      if (lightItem != null) {
        lightWeight = lightItem.weight
        light = lightItem.light
      }
      if (heavyItem != null) {
        heavy = heavyItem.heavy
        // put the 'left over' weight from the heavy item--what wasn't
        // needed to make up the difference between the light weight and
        // 1/n--back in the appropriate queue
        val newWeight = heavyItem.weight - (bucketWeight - lightWeight)
        if (newWeight > threshold) {
          enqueueItem(heavy!!, newWeight, bucketWeight, lightWeights, heavyWeights)
        }
      }
      lightWeight *= itemCount

      item_pairs.add(ItemPair(light, heavy, lightWeight))
    }
  }

  /**
   * Adds key/value to the appropriate queue. Keys with values less than the threshold get added to
   * [light_weights], all others get added to [heavy_weights].
   */
  private fun enqueueItem(
    key: T,
    value: Double,
    threshold: Double,
    light_weights: LinkedList<ItemPair>,
    heavy_weights: LinkedList<ItemPair>
  ) {
    if (value < threshold) {
      light_weights.offer(ItemPair(key, null, value))
    } else {
      heavy_weights.offer(ItemPair(null, key, value))
    }
  }

  fun setRandom(random: Random) {
    this.random = random
  }

  /**
   * Retrieves an item with probability proportional to its weight in the [Map] provided in
   * the input.
   *
   * @return an item chosen randomly based on its specified weight
   */
  fun nextItem(): T? {
    val itemPair = item_pairs[random.nextInt(item_pairs.size)]
    return if (random.nextDouble() < itemPair.weight) {
      itemPair.light
    } else {
      itemPair.heavy
    }
  }

  /** Manages light object/heavy object/light conditional probability tuples. */
  private inner class ItemPair(
    val light: T?,
    val heavy: T?,
    val weight: Double
  ) {
    override fun toString(): String = "[L:$light, H:$heavy, ${"%.3f".format(weight)}]"
  }

  companion object {
    /**
     * The default minimum value that is treated as a valid probability (as opposed to rounding error
     * from floating-point operations).
     */
    const val DEFAULT_THRESHOLD: Double = 0.00000000001
  }
}
