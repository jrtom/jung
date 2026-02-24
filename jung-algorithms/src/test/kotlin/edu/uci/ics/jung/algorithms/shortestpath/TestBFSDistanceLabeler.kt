/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.shortestpath

import com.google.common.graph.GraphBuilder
import junit.framework.Assert
import junit.framework.Test
import junit.framework.TestCase
import junit.framework.TestSuite

/**
 * @author Scott White, adapted to jung2 by Tom Nelson
 */
class TestBFSDistanceLabeler : TestCase() {
  companion object {
    fun suite(): Test {
      return TestSuite(TestBFSDistanceLabeler::class.java)
    }
  }

  override fun setUp() {}

  fun test() {
    val graph = GraphBuilder.undirected().build<Number>()
    for (i in 0 until 6) {
      graph.addNode(i)
    }
    graph.putEdge(0, 1)
    graph.putEdge(0, 5)
    graph.putEdge(0, 3)
    graph.putEdge(0, 4)
    graph.putEdge(1, 5)
    graph.putEdge(3, 4)
    graph.putEdge(3, 2)
    graph.putEdge(5, 2)
    val root: Number = 0

    val labeler = BFSDistanceLabeler<Number>()
    labeler.labelDistances(graph, root)

    Assert.assertEquals(labeler.getPredecessors(root)!!.size, 0)
    Assert.assertEquals(labeler.getPredecessors(1)!!.size, 1)
    Assert.assertEquals(labeler.getPredecessors(2)!!.size, 2)
    Assert.assertEquals(labeler.getPredecessors(3)!!.size, 1)
    Assert.assertEquals(labeler.getPredecessors(4)!!.size, 1)
    Assert.assertEquals(labeler.getPredecessors(5)!!.size, 1)

    Assert.assertEquals(labeler.getDistance(graph, 0), 0)
    Assert.assertEquals(labeler.getDistance(graph, 1), 1)
    Assert.assertEquals(labeler.getDistance(graph, 2), 2)
    Assert.assertEquals(labeler.getDistance(graph, 3), 1)
    Assert.assertEquals(labeler.getDistance(graph, 4), 1)
    Assert.assertEquals(labeler.getDistance(graph, 5), 1)
  }
}
