package edu.uci.ics.jung.algorithms.generators.random

import junit.framework.Assert
import junit.framework.Test
import junit.framework.TestCase
import junit.framework.TestSuite
import java.util.Random
import java.util.function.Supplier

/**
 * @author W. Giordano, Scott White
 */
class TestErdosRenyi : TestCase() {

  lateinit var nodeFactory: Supplier<String>

  override fun setUp() {
    nodeFactory = object : Supplier<String> {
      var count = 0
      override fun get(): String = ('A' + count++).toChar().toString()
    }
  }

  fun test() {
    val numNodes = 100
    var total = 0
    for (i in 1..10) {
      val generator = ErdosRenyiGenerator<String>(nodeFactory, numNodes, 0.1)
      generator.setRandom(Random(0))

      val graph = generator.get()
      Assert.assertTrue(graph.nodes().size == numNodes)
      total += graph.edges().size
    }
    total = (total / 10.0).toInt()
    Assert.assertTrue(total > 495 - 50 && total < 495 + 50)
  }

  companion object {
    fun suite(): Test {
      return TestSuite(TestErdosRenyi::class.java)
    }
  }
}
