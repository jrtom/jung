/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.generators.random

import junit.framework.Assert
import junit.framework.Test
import junit.framework.TestCase
import junit.framework.TestSuite
import java.util.Random
import java.util.function.Supplier

/**
 * @author Scott White
 */
class TestEppsteinPowerLawGenerator : TestCase() {
  lateinit var nodeFactory: Supplier<Integer>

  override fun setUp() {
    nodeFactory = object : Supplier<Integer> {
      var count = 0
      override fun get(): Integer = count++ as Integer
    }
  }

  fun testSimpleDirectedCase() {
    for (r in 0 until 10) {
      val generator = EppsteinPowerLawGenerator<Integer>(nodeFactory, 10, 40, r)
      generator.setRandom(Random(2))

      val graph = generator.get()
      Assert.assertEquals(graph.nodes().size, 10)
      Assert.assertEquals(graph.edges().size, 40)
    }
  }

  companion object {
    fun suite(): Test {
      return TestSuite(TestEppsteinPowerLawGenerator::class.java)
    }
  }
}
