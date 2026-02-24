/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.scoring

import com.google.common.collect.ImmutableMap
import com.google.common.graph.NetworkBuilder
import junit.framework.Assert
import junit.framework.Test
import junit.framework.TestCase
import junit.framework.TestSuite
import java.util.function.Supplier

/**
 * @author Scott White, adapted to jung2 by Tom Nelson
 */
class TestWeightedNIPaths : TestCase() {

  lateinit var nodeFactory: Supplier<String>
  lateinit var edgeFactory: Supplier<Number>

  companion object {
    fun suite(): Test {
      return TestSuite(TestWeightedNIPaths::class.java)
    }
  }

  override fun setUp() {
    nodeFactory = object : Supplier<String> {
      var a = 'A'
      override fun get(): String {
        return (a++).toString()
      }
    }
    edgeFactory = object : Supplier<Number> {
      var count = 0
      override fun get(): Number {
        return count++
      }
    }
  }

  fun testRanker() {

    val graph = NetworkBuilder.directed().build<String, Number>()
    for (i in 0 until 5) {
      graph.addNode(nodeFactory.get())
    }

    graph.addEdge("A", "B", edgeFactory.get())
    graph.addEdge("A", "C", edgeFactory.get())
    graph.addEdge("A", "D", edgeFactory.get())
    graph.addEdge("B", "A", edgeFactory.get())
    graph.addEdge("B", "E", edgeFactory.get())
    graph.addEdge("B", "D", edgeFactory.get())
    graph.addEdge("C", "A", edgeFactory.get())
    graph.addEdge("C", "E", edgeFactory.get())
    graph.addEdge("C", "D", edgeFactory.get())
    graph.addEdge("D", "A", edgeFactory.get())
    graph.addEdge("D", "B", edgeFactory.get())
    graph.addEdge("D", "C", edgeFactory.get())
    graph.addEdge("D", "E", edgeFactory.get())

    val priors = HashSet<String>()
    priors.add("A")

    val ranker = WeightedNIPaths<String, Number>(graph, nodeFactory, edgeFactory, 2.0, 3, priors)

    val expectedScores = ImmutableMap.of(
        "A", 0.277787,
        "B", 0.166676,
        "C", 0.166676,
        "D", 0.222222,
        "E", 0.166676)
    for (node in graph.nodes()) {
      Assert.assertEquals(expectedScores[node]!!, ranker.getNodeScore(node), 0.0001)
    }
  }
}
