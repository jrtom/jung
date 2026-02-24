/**
 * Copyright (c) 2009, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description. Created on Jan 13, 2009
 */
package edu.uci.ics.jung.algorithms.util

import junit.framework.TestCase

/**
 * @author jrtom
 */
class TestWeightedChoice : TestCase() {
  private lateinit var weighted_choice: WeightedChoice<String>
  private val item_weights = HashMap<String, Double>()
  private val item_counts = HashMap<String, Int>()

  override fun tearDown() {
    item_weights.clear()
    item_counts.clear()
  }

  private fun initializeWeights(weights: DoubleArray) {
    item_weights["a"] = weights[0]
    item_weights["b"] = weights[1]
    item_weights["c"] = weights[2]
    item_weights["d"] = weights[3]

    for (key in item_weights.keys) {
      item_counts[key] = 0
    }
  }

  private fun runWeightedChoice() {
    weighted_choice = WeightedChoice(item_weights, NotRandom(100))

    val max_iterations = 10000
    for (i in 0 until max_iterations) {
      val item = weighted_choice.nextItem()
      val count = item_counts[item]!!
      item_counts[item!!] = count + 1
    }

    for (key in item_weights.keys) {
      assertEquals((item_weights[key]!! * max_iterations).toInt(), item_counts[key]!!)
    }
  }

  fun testUniform() {
    initializeWeights(doubleArrayOf(0.25, 0.25, 0.25, 0.25))

    runWeightedChoice()
  }

  fun testNonUniform() {
    initializeWeights(doubleArrayOf(0.45, 0.10, 0.13, 0.32))

    runWeightedChoice()
  }
}
