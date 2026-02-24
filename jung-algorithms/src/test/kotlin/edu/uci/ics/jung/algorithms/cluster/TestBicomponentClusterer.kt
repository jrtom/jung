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

import com.google.common.graph.Graph
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import junit.framework.Test
import junit.framework.TestCase
import junit.framework.TestSuite

/**
 * @author Scott White
 */
class TestBicomponentClusterer : TestCase() {

  override fun setUp() {}

  fun testExtract0() {
    val graph: MutableGraph<String> = GraphBuilder.undirected().build()
    val v = arrayOf("0")
    graph.addNode(v[0])

    val c = ArrayList<Set<String>>()
    c.add(0, HashSet())
    (c[0] as MutableSet).add(v[0])

    testComponents(graph, v, c)
  }

  fun testExtractEdge() {
    val graph: MutableGraph<String> = GraphBuilder.undirected().build()
    val v = arrayOf("0", "1")
    graph.putEdge(v[0], v[1])

    val c = ArrayList<Set<String>>()
    c.add(0, HashSet())
    (c[0] as MutableSet).add(v[0])
    (c[0] as MutableSet).add(v[1])

    testComponents(graph, v, c)
  }

  fun testExtractV() {
    val graph: MutableGraph<String> = GraphBuilder.undirected().build()
    val v = arrayOf("0", "1", "2")
    graph.putEdge(v[0], v[1])
    graph.putEdge(v[0], v[2])

    val c = ArrayList<MutableSet<String>>()
    c.add(0, HashSet())
    c.add(1, HashSet())

    c[0].add(v[0])
    c[0].add(v[1])

    c[1].add(v[0])
    c[1].add(v[2])

    testComponents(graph, v, c.map { it as Set<String> })
  }

  fun createEdges(v: Array<String?>, edgeArray: Array<IntArray>, g: MutableGraph<String>) {
    for (k in edgeArray.indices) {
      val i = edgeArray[k][0]
      val j = edgeArray[k][1]
      val v1 = getNode(v, i, g)
      val v2 = getNode(v, j, g)
      g.putEdge(v1, v2)
    }
  }

  fun getNode(vArray: Array<String?>, i: Int, g: MutableGraph<String>): String {
    var v = vArray[i]
    if (v == null) {
      vArray[i] = ('0' + i).toChar().toString()
      g.addNode(vArray[i]!!)
      v = vArray[i]
    }
    return v!!
  }

  fun testExtract1() {
    val v = arrayOfNulls<String>(6)
    val edges1 = arrayOf(
      intArrayOf(0, 1), intArrayOf(0, 5), intArrayOf(0, 3),
      intArrayOf(0, 4), intArrayOf(1, 5), intArrayOf(3, 4), intArrayOf(2, 3)
    )
    val graph: MutableGraph<String> = GraphBuilder.undirected().build()
    createEdges(v, edges1, graph)

    val c = ArrayList<MutableSet<String>>()
    for (i in 0 until 3) {
      c.add(i, HashSet())
    }

    c[0].add(v[0]!!)
    c[0].add(v[1]!!)
    c[0].add(v[5]!!)

    c[1].add(v[0]!!)
    c[1].add(v[3]!!)
    c[1].add(v[4]!!)

    c[2].add(v[2]!!)
    c[2].add(v[3]!!)

    testComponents(graph, v.map { it!! }.toTypedArray(), c.map { it as Set<String> })
  }

  fun testExtract2() {
    val v = arrayOfNulls<String>(9)
    val edges1 = arrayOf(
      intArrayOf(0, 2), intArrayOf(0, 4), intArrayOf(1, 0), intArrayOf(2, 1),
      intArrayOf(3, 0), intArrayOf(4, 3), intArrayOf(5, 3), intArrayOf(6, 7),
      intArrayOf(6, 8), intArrayOf(8, 7)
    )
    val graph: MutableGraph<String> = GraphBuilder.undirected().build()
    createEdges(v, edges1, graph)

    val c = ArrayList<MutableSet<String>>()
    for (i in 0 until 4) {
      c.add(i, HashSet())
    }

    c[0].add(v[0]!!)
    c[0].add(v[1]!!)
    c[0].add(v[2]!!)

    c[1].add(v[0]!!)
    c[1].add(v[3]!!)
    c[1].add(v[4]!!)

    c[2].add(v[5]!!)
    c[2].add(v[3]!!)

    c[3].add(v[6]!!)
    c[3].add(v[7]!!)
    c[3].add(v[8]!!)

    testComponents(graph, v.map { it!! }.toTypedArray(), c.map { it as Set<String> })
  }

  fun testComponents(graph: Graph<String>, nodes: Array<String>, c: List<Set<String>>) {
    val finder = BicomponentClusterer<String, Number>()
    val bicomponents = finder.apply(graph)

    // check number of components
    assertEquals(bicomponents.size, c.size)

    // make sure that each set in c[] is found in bicomponents
    val clusterList = ArrayList<Set<String>>(bicomponents)
    var found = false
    for (i in c.indices) {
      for (j in 0 until bicomponents.size) {
        if (clusterList[j] == c[i]) {
          found = true
          break
        }
      }
      assertTrue(found)
    }

    // make sure that each node is represented in >=1 element of bicomponents
    val collapsedSet = HashSet<String>()
    for (set in bicomponents) {
      collapsedSet.addAll(set)
    }
    for (v in graph.nodes()) {
      assertTrue(collapsedSet.contains(v))
    }
  }

  companion object {
    fun suite(): Test {
      return TestSuite(TestBicomponentClusterer::class.java)
    }
  }
}
