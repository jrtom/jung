/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.flows

import com.google.common.graph.NetworkBuilder
import junit.framework.Assert
import junit.framework.Test
import junit.framework.TestCase
import junit.framework.TestSuite
import java.util.function.Supplier

/**
 * @author Scott White, Joshua O'Madadhain, Tom Nelson
 */
class TestEdmondsKarpMaxFlow : TestCase() {

  override fun setUp() {}

  fun testSanityChecks() {
    val g = NetworkBuilder.directed().build<Number, Number>()
    val source: Number = Integer(1)
    val sink: Number = Integer(2)
    g.addNode(source)
    g.addNode(sink)

    val v: Number = Integer(3)

    val h = NetworkBuilder.directed().build<Number, Number>()
    val w: Number = Integer(4)
    g.addNode(w)

    val dummyCapacity = java.util.function.Function<Number, Int?> { 0 }
    val dummyFlowMap = HashMap<Number, Int>()
    val dummyEdgeFactory = object : Supplier<Number> {
      var count = 100
      override fun get(): Number = count++
    }

    try {
      EdmondsKarpMaxFlow<Number, Number>(g, source, source, dummyCapacity, dummyFlowMap, dummyEdgeFactory)
      fail("source and sink nodes not distinct")
    } catch (iae: IllegalArgumentException) {
    }

    try {
      EdmondsKarpMaxFlow<Number, Number>(h, source, w, dummyCapacity, dummyFlowMap, dummyEdgeFactory)
      fail("source and sink nodes not both part of specified graph")
    } catch (iae: IllegalArgumentException) {
    }

    try {
      EdmondsKarpMaxFlow<Number, Number>(g, source, v, dummyCapacity, dummyFlowMap, dummyEdgeFactory)
      fail("source and sink nodes not both part of specified graph")
    } catch (iae: IllegalArgumentException) {
    }
  }

  fun testSimpleFlow() {
    val graph = NetworkBuilder.directed().build<Number, Number>()
    val edgeFactory = object : Supplier<Number> {
      var count = 0
      override fun get(): Number = count++
    }

    val edgeCapacityMap = HashMap<Number, Int>()
    for (i in 0 until 6) {
      graph.addNode(i)
    }

    val edgeFlowMap = HashMap<Number, Int>()

    graph.addEdge(0, 1, edgeFactory.get())
    edgeCapacityMap[0] = 16

    graph.addEdge(0, 2, edgeFactory.get())
    edgeCapacityMap[1] = 13

    graph.addEdge(1, 2, edgeFactory.get())
    edgeCapacityMap[2] = 6

    graph.addEdge(1, 3, edgeFactory.get())
    edgeCapacityMap[3] = 12

    graph.addEdge(2, 4, edgeFactory.get())
    edgeCapacityMap[4] = 14

    graph.addEdge(3, 2, edgeFactory.get())
    edgeCapacityMap[5] = 9

    graph.addEdge(3, 5, edgeFactory.get())
    edgeCapacityMap[6] = 20

    graph.addEdge(4, 3, edgeFactory.get())
    edgeCapacityMap[7] = 7

    graph.addEdge(4, 5, edgeFactory.get())
    edgeCapacityMap[8] = 4

    val ek = EdmondsKarpMaxFlow<Number, Number>(
      graph, 0, 5, { n -> edgeCapacityMap[n] }, edgeFlowMap, edgeFactory
    )
    ek.evaluate()

    assertTrue(ek.maxFlow == 23)
    val nodesInS = ek.getNodesInSourcePartition()
    assertEquals(4, nodesInS.size)

    for (v in nodesInS) {
      Assert.assertTrue(v.toInt() != 3 && v.toInt() != 5)
    }

    val nodesInT = ek.getNodesInSinkPartition()
    assertEquals(2, nodesInT.size)

    for (v in nodesInT) {
      Assert.assertTrue(v.toInt() == 3 || v.toInt() == 5)
    }

    val minCutEdges = ek.getMinCutEdges()
    var maxFlow = 0
    for (e in minCutEdges) {
      val flow = edgeFlowMap[e]
      maxFlow += flow!!.toInt()
    }
    Assert.assertEquals(23, maxFlow)
    Assert.assertEquals(3, minCutEdges.size)
  }

  fun testAnotherSimpleFlow() {
    val graph = NetworkBuilder.directed().build<Number, Number>()
    val edgeFactory = object : Supplier<Number> {
      var count = 0
      override fun get(): Number = count++
    }

    val edgeCapacityMap = HashMap<Number, Int>()
    for (i in 0 until 6) {
      graph.addNode(i)
    }

    val edgeFlowMap = HashMap<Number, Int>()

    graph.addEdge(0, 1, edgeFactory.get())
    edgeCapacityMap[0] = 5

    graph.addEdge(0, 2, edgeFactory.get())
    edgeCapacityMap[1] = 3

    graph.addEdge(1, 5, edgeFactory.get())
    edgeCapacityMap[2] = 2

    graph.addEdge(1, 2, edgeFactory.get())
    edgeCapacityMap[3] = 8

    graph.addEdge(2, 3, edgeFactory.get())
    edgeCapacityMap[4] = 4

    graph.addEdge(2, 4, edgeFactory.get())
    edgeCapacityMap[5] = 2

    graph.addEdge(3, 4, edgeFactory.get())
    edgeCapacityMap[6] = 3

    graph.addEdge(3, 5, edgeFactory.get())
    edgeCapacityMap[7] = 6

    graph.addEdge(4, 5, edgeFactory.get())
    edgeCapacityMap[8] = 1

    val ek = EdmondsKarpMaxFlow<Number, Number>(
      graph, 0, 5, { n -> edgeCapacityMap[n] }, edgeFlowMap, edgeFactory
    )
    ek.evaluate()

    assertTrue(ek.maxFlow == 7)
  }

  companion object {
    fun suite(): Test {
      return TestSuite(TestEdmondsKarpMaxFlow::class.java)
    }
  }
}
