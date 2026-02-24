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
import edu.uci.ics.jung.algorithms.scoring.util.ScoringUtils
import junit.framework.Assert
import junit.framework.Test
import junit.framework.TestCase
import junit.framework.TestSuite

/** Tests HITSWithPriors. */
class TestHITSWithPriors : TestCase() {
  lateinit var graph: MutableNetwork<Number, Number>
  lateinit var roots: Set<Number>

  companion object {
    fun suite(): Test {
      return TestSuite(TestHITSWithPriors::class.java)
    }
  }

  override fun setUp() {
    graph = NetworkBuilder.directed().build()
    for (i in 0 until 4) {
      graph.addNode(i)
    }
    var j = 0
    graph.addEdge(0, 1, j++)
    graph.addEdge(1, 2, j++)
    graph.addEdge(2, 3, j++)
    graph.addEdge(3, 0, j++)
    graph.addEdge(2, 1, j++)

    roots = HashSet<Number>().apply { add(2) }
  }

  fun testRankings() {

    val ranker =
        HITSWithPriors<Number, Number>(graph, ScoringUtils.getHITSUniformRootPrior(roots), 0.3)
    ranker.evaluate()

    val expectedAuth = doubleArrayOf(0.0, 0.765, 0.365, 0.530)
    val expectedHub = doubleArrayOf(0.398, 0.190, 0.897, 0.0)

    var hubSum = 0.0
    var authSum = 0.0
    for (n in graph.nodes()) {
      val i = n.toInt()
      val auth = ranker.getNodeScore(i).authority
      val hub = ranker.getNodeScore(i).hub
      Assert.assertEquals(auth, expectedAuth[i], 0.001)
      Assert.assertEquals(hub, expectedHub[i], 0.001)
      hubSum += hub * hub
      authSum += auth * auth
    }
    Assert.assertEquals(1.0, hubSum, 0.001)
    Assert.assertEquals(1.0, authSum, 0.001)
  }
}
