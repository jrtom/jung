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
import java.util.function.Supplier

/**
 * @author Scott White
 */
class TestPageRankWithPriors : TestCase() {
  private lateinit var graph: MutableNetwork<Int, Int>
  private lateinit var edgeFactory: Supplier<Int>

  companion object {
    fun suite(): Test {
      return TestSuite(TestPageRankWithPriors::class.java)
    }
  }

  override fun setUp() {
    edgeFactory = object : Supplier<Int> {
      var i = 0
      override fun get(): Int {
        return i++
      }
    }
  }

  private fun addEdge(v1: Int, v2: Int) {
    val edge = edgeFactory.get()
    graph.addEdge(v1, v2, edge)
  }

  fun testGraphScoring() {
    graph = NetworkBuilder.directed().allowsParallelEdges(true).build()

    val expectedScore = doubleArrayOf(0.1157, 0.2463, 0.4724, 0.1653)

    for (i in 0 until 4) {
      graph.addNode(i)
    }
    addEdge(0, 1)
    addEdge(1, 2)
    addEdge(2, 3)
    addEdge(3, 0)
    addEdge(2, 1)

    val priors = HashSet<Int>()
    priors.add(2)

    val pr = PageRankWithPriors<Int, Int>(
        graph, ScoringUtils.getUniformRootPrior(priors), 0.3)
    pr.evaluate()

    var scoreSum = 0.0
    for (i in 0 until graph.nodes().size) {
      val score = pr.getNodeScore(i)
      Assert.assertEquals(expectedScore[i], score, pr.tolerance)
      scoreSum += score
    }
    Assert.assertEquals(1.0, scoreSum, pr.tolerance * graph.nodes().size)
  }
}
