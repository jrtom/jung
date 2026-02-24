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

import com.google.common.graph.MutableNetwork
import com.google.common.graph.NetworkBuilder
import junit.framework.Assert
import junit.framework.Test
import junit.framework.TestCase
import junit.framework.TestSuite

/**
 * @author Scott White
 * @author Tom Nelson - adapted to jung2
 */
class TestHITS : TestCase() {

  lateinit var graph: MutableNetwork<Number, Number>

  companion object {
    fun suite(): Test {
      return TestSuite(TestHITS::class.java)
    }
  }

  override fun setUp() {
    graph = NetworkBuilder.directed().allowsParallelEdges(true).build()
    for (i in 0 until 5) {
      graph.addNode(i)
    }

    var j = 0
    graph.addEdge(0, 1, j++)
    graph.addEdge(1, 2, j++)
    graph.addEdge(2, 3, j++)
    graph.addEdge(3, 0, j++)
    graph.addEdge(2, 1, j++)
  }

  // TODO(jrtom): add tests for
  // * undirected graph
  // * self-loops
  // * parallel edges
  // * weighted edges
  fun testRanker() {

    val ranker = HITS<Number, Number>(graph)
    for (i in 0 until 10) {
      ranker.step()
      // verify that sums of each scores are 1.0
      var authSum = 0.0
      var hubSum = 0.0
      for (j in 0 until 5) {
        val score = ranker.getNodeScore(j)
        authSum += score.authority * score.authority
        hubSum += score.hub * score.hub
      }
      Assert.assertEquals(authSum, 1.0, 0.0001)
      Assert.assertEquals(hubSum, 1.0, 0.0001)
    }

    ranker.evaluate()

    Assert.assertEquals(ranker.getNodeScore(0).authority, 0.0, .0001)
    Assert.assertEquals(ranker.getNodeScore(1).authority, 0.8507, .001)
    Assert.assertEquals(ranker.getNodeScore(2).authority, 0.0, .0001)
    Assert.assertEquals(ranker.getNodeScore(3).authority, 0.5257, .001)

    Assert.assertEquals(ranker.getNodeScore(0).hub, 0.5257, .001)
    Assert.assertEquals(ranker.getNodeScore(1).hub, 0.0, .0001)
    Assert.assertEquals(ranker.getNodeScore(2).hub, 0.8507, .0001)
    Assert.assertEquals(ranker.getNodeScore(3).hub, 0.0, .0001)
  }
}
