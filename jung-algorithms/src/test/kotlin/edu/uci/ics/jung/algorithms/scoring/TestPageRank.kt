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
import java.util.function.Supplier

/**
 * @author Joshua O'Madadhain
 */
class TestPageRank : TestCase() {

  private lateinit var edgeWeights: MutableMap<Int, Number>
  private lateinit var graph: MutableNetwork<Int, Int>
  private lateinit var edgeFactory: Supplier<Int>

  companion object {
    fun suite(): Test {
      return TestSuite(TestPageRank::class.java)
    }
  }

  override fun setUp() {
    edgeWeights = HashMap()
    edgeFactory = object : Supplier<Int> {
      var i = 0
      override fun get(): Int {
        return i++
      }
    }
  }

  private fun addEdge(v1: Int, v2: Int, weight: Double) {
    val edge = edgeFactory.get()
    graph.addEdge(v1, v2, edge)
    edgeWeights[edge] = weight
  }

  fun testRanker() {
    graph = NetworkBuilder.directed().build()

    addEdge(0, 1, 1.0)
    addEdge(1, 2, 1.0)
    addEdge(2, 3, 0.5)
    addEdge(3, 1, 1.0)
    addEdge(2, 1, 0.5)

    val pr = PageRank<Int, Int>(graph, { edgeWeights[it]!! }, 0.0)
    pr.evaluate()

    Assert.assertEquals(pr.getNodeScore(0), 0.0, pr.tolerance)
    Assert.assertEquals(pr.getNodeScore(1), 0.4, pr.tolerance)
    Assert.assertEquals(pr.getNodeScore(2), 0.4, pr.tolerance)
    Assert.assertEquals(pr.getNodeScore(3), 0.2, pr.tolerance)
  }
}
