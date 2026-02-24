/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.cluster

import com.google.common.graph.NetworkBuilder
import junit.framework.Assert
import junit.framework.Test
import junit.framework.TestCase
import junit.framework.TestSuite

/**
 * @author Scott White
 */
class TestEdgeBetweennessClusterer : TestCase() {

  override fun setUp() {}

  fun testRanker() {
    val graph = NetworkBuilder.directed().build<Number, Number>()
    for (i in 0 until 10) {
      graph.addNode(i + 1)
    }
    var j = 0
    graph.addEdge(1, 2, j++)
    graph.addEdge(1, 3, j++)
    graph.addEdge(2, 3, j++)
    graph.addEdge(5, 6, j++)
    graph.addEdge(5, 7, j++)
    graph.addEdge(6, 7, j++)
    graph.addEdge(8, 10, j++)
    graph.addEdge(7, 8, j++)
    graph.addEdge(7, 10, j++)
    graph.addEdge(3, 4, j++)
    graph.addEdge(4, 6, j++)
    graph.addEdge(4, 8, j++)

    Assert.assertEquals(graph.nodes().size, 10)
    Assert.assertEquals(graph.edges().size, 12)

    val clusterer = EdgeBetweennessClusterer<Number, Number>(3)
    val clusters = clusterer.apply(graph)

    Assert.assertEquals(clusters.size, 3)
  }

  companion object {
    fun suite(): Test {
      return TestSuite(TestEdgeBetweennessClusterer::class.java)
    }
  }
}
