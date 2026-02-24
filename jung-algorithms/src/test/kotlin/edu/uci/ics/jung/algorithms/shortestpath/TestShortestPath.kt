/*
 * Created on Aug 22, 2003
 *
 */
package edu.uci.ics.jung.algorithms.shortestpath

import com.google.common.collect.BiMap
import com.google.common.graph.MutableNetwork
import com.google.common.graph.Network
import com.google.common.graph.NetworkBuilder
import edu.uci.ics.jung.algorithms.util.Indexer
import junit.framework.TestCase
import java.util.function.Function
import java.util.function.Supplier

/**
 * @author Joshua O'Madadhain
 */
// TODO: needs major cleanup
class TestShortestPath : TestCase() {
  private lateinit var dg: MutableNetwork<String, Int>
  private lateinit var ug: MutableNetwork<String, Int>

  companion object {
    // graph based on Weiss, _Data Structures and Algorithm Analysis_,
    // 1992, p. 292
    private val edges = arrayOf(
        intArrayOf(1, 2, 2),
        intArrayOf(1, 4, 1), // 0, 1
        intArrayOf(2, 4, 3),
        intArrayOf(2, 5, 10), // 2, 3
        intArrayOf(3, 1, 4),
        intArrayOf(3, 6, 5), // 4, 5
        intArrayOf(4, 3, 2),
        intArrayOf(4, 5, 2),
        intArrayOf(4, 6, 8),
        intArrayOf(4, 7, 4), // 6,7,8,9
        intArrayOf(5, 7, 6), // 10
        intArrayOf(7, 6, 1), // 11
        intArrayOf(8, 9, 4), // (12) these three edges define a second connected component
        intArrayOf(9, 10, 1), // 13
        intArrayOf(10, 8, 2) // 14
    )

    private val ug_incomingEdges = arrayOf(
        arrayOf(null, 0, 6, 1, 7, 11, 9, null, null, null),
        arrayOf(0, null, 6, 2, 7, 11, 9, null, null, null),
        arrayOf(1, 2, null, 6, 7, 5, 9, null, null, null),
        arrayOf(1, 2, 6, null, 7, 11, 9, null, null, null),
        arrayOf(1, 2, 6, 7, null, 11, 10, null, null, null),
        arrayOf(1, 2, 5, 9, 10, null, 11, null, null, null),
        arrayOf(1, 2, 5, 9, 10, 11, null, null, null, null),
        arrayOf(null, null, null, null, null, null, null, null, 13, 14),
        arrayOf(null, null, null, null, null, null, null, 14, null, 13),
        arrayOf(null, null, null, null, null, null, null, 14, 13, null)
    )

    private val dg_incomingEdges = arrayOf(
        arrayOf(null, 0, 6, 1, 7, 11, 9, null, null, null),
        arrayOf(4, null, 6, 2, 7, 11, 9, null, null, null),
        arrayOf(4, 0, null, 1, 7, 5, 9, null, null, null),
        arrayOf(4, 0, 6, null, 7, 11, 9, null, null, null),
        arrayOf(null, null, null, null, null, 11, 10, null, null, null),
        arrayOf<Int?>(null, null, null, null, null, null, null, null, null, null),
        arrayOf(null, null, null, null, null, 11, null, null, null, null),
        arrayOf(null, null, null, null, null, null, null, null, 12, 13),
        arrayOf(null, null, null, null, null, null, null, 14, null, 13),
        arrayOf(null, null, null, null, null, null, null, 14, 12, null)
    )

    private val dg_distances = arrayOf(
        doubleArrayOf(0.0, 2.0, 3.0, 1.0, 3.0, 6.0, 5.0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
        doubleArrayOf(9.0, 0.0, 5.0, 3.0, 5.0, 8.0, 7.0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
        doubleArrayOf(4.0, 6.0, 0.0, 5.0, 7.0, 5.0, 9.0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
        doubleArrayOf(6.0, 8.0, 2.0, 0.0, 2.0, 5.0, 4.0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
        doubleArrayOf(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0, 7.0, 6.0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
        doubleArrayOf(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
        doubleArrayOf(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0, 0.0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
        doubleArrayOf(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0, 4.0, 5.0),
        doubleArrayOf(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 3.0, 0.0, 1.0),
        doubleArrayOf(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 2.0, 6.0, 0.0)
    )

    private val ug_distances = arrayOf(
        doubleArrayOf(0.0, 2.0, 3.0, 1.0, 3.0, 6.0, 5.0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
        doubleArrayOf(2.0, 0.0, 5.0, 3.0, 5.0, 8.0, 7.0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
        doubleArrayOf(3.0, 5.0, 0.0, 2.0, 4.0, 5.0, 6.0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
        doubleArrayOf(1.0, 3.0, 2.0, 0.0, 2.0, 5.0, 4.0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
        doubleArrayOf(3.0, 5.0, 4.0, 2.0, 0.0, 7.0, 6.0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
        doubleArrayOf(6.0, 8.0, 5.0, 5.0, 7.0, 0.0, 1.0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
        doubleArrayOf(5.0, 7.0, 6.0, 4.0, 6.0, 1.0, 0.0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
        doubleArrayOf(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0, 3.0, 2.0),
        doubleArrayOf(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 3.0, 0.0, 1.0),
        doubleArrayOf(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 2.0, 1.0, 0.0)
    )

    private val shortestPaths1 = arrayOf<Array<Int>?>(
        null,
        arrayOf(0),
        arrayOf(1, 6),
        arrayOf(1),
        arrayOf(1, 7),
        arrayOf(1, 9, 11),
        arrayOf(1, 9),
        null,
        null,
        null
    )
  }

  private lateinit var edgeArrays: MutableMap<Network<String, Int>, Array<Int?>>

  private lateinit var edgeWeights: MutableMap<Int, Number>

  private lateinit var nev: Function<Int, Number>

  private val nodeFactoryDG = object : Supplier<String> {
    var count = 0
    override fun get(): String {
      return "V" + count++
    }
  }
  private val nodeFactoryUG = object : Supplier<String> {
    var count = 0
    override fun get(): String {
      return "U" + count++
    }
  }

  lateinit var did: BiMap<String, Int>
  lateinit var uid: BiMap<String, Int>

  override fun setUp() {
    edgeWeights = HashMap()
    nev = Function { edgeWeights[it]!! }
    dg = NetworkBuilder.directed().allowsParallelEdges(true).allowsSelfLoops(true).build()
    for (i in dg_distances.indices) {
      dg.addNode(nodeFactoryDG.get())
    }
    did = Indexer.create(dg.nodes(), 1)
    val dgArray = arrayOfNulls<Int>(edges.size)
    addEdges(dg, did, dgArray)

    ug = NetworkBuilder.undirected().allowsParallelEdges(true).allowsSelfLoops(true).build()
    for (i in ug_distances.indices) {
      ug.addNode(nodeFactoryUG.get())
    }
    uid = Indexer.create(ug.nodes(), 1)
    val ugArray = arrayOfNulls<Int>(edges.size)
    addEdges(ug, uid, ugArray)

    edgeArrays = HashMap()
    edgeArrays[dg] = dgArray
    edgeArrays[ug] = ugArray
  }

  override fun tearDown() {}

  private fun exceptionTest(
      g: MutableNetwork<String, Int>, indexer: BiMap<String, Int>, index: Int) {
    val dsp = DijkstraShortestPath<String, Int>(g, nev)
    val start = indexer.inverse()[index]!!
    var e: Int? = null

    val v = "NOT IN GRAPH"

    try {
      dsp.getDistance(start, v)
      fail("getDistance(): illegal destination node")
    } catch (iae: IllegalArgumentException) {
    }
    try {
      dsp.getDistance(v, start)
      fail("getDistance(): illegal source node")
    } catch (iae: IllegalArgumentException) {
    }
    try {
      dsp.getDistanceMap(v, 1)
      fail("getDistanceMap(): illegal source node")
    } catch (iae: IllegalArgumentException) {
    }
    try {
      dsp.getDistanceMap(start, 0)
      fail("getDistanceMap(): too few nodes requested")
    } catch (iae: IllegalArgumentException) {
    }
    try {
      dsp.getDistanceMap(start, g.nodes().size + 1)
      fail("getDistanceMap(): too many nodes requested")
    } catch (iae: IllegalArgumentException) {
    }

    try {
      dsp.getIncomingEdge(start, v)
      fail("getIncomingEdge(): illegal destination node")
    } catch (iae: IllegalArgumentException) {
    }
    try {
      dsp.getIncomingEdge(v, start)
      fail("getIncomingEdge(): illegal source node")
    } catch (iae: IllegalArgumentException) {
    }
    try {
      dsp.getIncomingEdgeMap(v, 1)
      fail("getIncomingEdgeMap(): illegal source node")
    } catch (iae: IllegalArgumentException) {
    }
    try {
      dsp.getIncomingEdgeMap(start, 0)
      fail("getIncomingEdgeMap(): too few nodes requested")
    } catch (iae: IllegalArgumentException) {
    }
    try {
      dsp.getDistanceMap(start, g.nodes().size + 1)
      fail("getIncomingEdgeMap(): too many nodes requested")
    } catch (iae: IllegalArgumentException) {
    }

    try {
      // test negative edge weight exception
      val v1 = indexer.inverse()[1]!!
      val v2 = indexer.inverse()[7]!!
      e = g.edges().size + 1
      g.addEdge(v1, v2, e)
      edgeWeights[e] = -2
      dsp.reset()
      dsp.getDistanceMap(start)
      fail("DijkstraShortestPath should not accept negative edge weights")
    } catch (iae: IllegalArgumentException) {
      g.removeEdge(e!!)
    }
  }

  fun testDijkstra() {
    setUp()
    exceptionTest(dg, did, 1)

    setUp()
    exceptionTest(ug, uid, 1)

    setUp()
    getPathTest(dg, did, 1)

    setUp()
    getPathTest(ug, uid, 1)

    for (i in 1..dg_distances.size) {
      setUp()
      weightedTest(dg, did, i, true)

      setUp()
      weightedTest(dg, did, i, false)
    }

    for (i in 1..ug_distances.size) {
      setUp()
      weightedTest(ug, uid, i, true)

      setUp()
      weightedTest(ug, uid, i, false)
    }
  }

  private fun getPathTest(g: Network<String, Int>, indexer: BiMap<String, Int>, index: Int) {
    val dsp = DijkstraShortestPath<String, Int>(g, nev)
    val start = indexer.inverse()[index]!!
    val edgeArray = edgeArrays[g]!!
    val incomingEdges1 =
        if (g.isDirected) dg_incomingEdges[index - 1] else ug_incomingEdges[index - 1]
    assertEquals(incomingEdges1.size, g.nodes().size)

    // test getShortestPath(start, v)
    dsp.reset()
    for (i in 1..incomingEdges1.size) {
      val shortestPath = dsp.getPath(start, indexer.inverse()[i]!!)
      val indices = shortestPaths1[i - 1]
      val iter = shortestPath.listIterator()
      while (iter.hasNext()) {
        val j = iter.nextIndex()
        val e = iter.next()
        if (e != null) {
          assertEquals(edgeArray[indices!![j]], e)
        } else {
          assertNull(indices!![j])
        }
      }
    }
  }

  private fun weightedTest(
      g: Network<String, Int>, indexer: BiMap<String, Int>, index: Int, cached: Boolean) {
    val start = indexer.inverse()[index]!!
    val distances1: DoubleArray
    val incomingEdges1: Array<out Int?>
    if (g.isDirected) {
      distances1 = dg_distances[index - 1]
      incomingEdges1 = dg_incomingEdges[index - 1]
    } else {
      distances1 = ug_distances[index - 1]
      incomingEdges1 = ug_incomingEdges[index - 1]
    }
    assertEquals(distances1.size, g.nodes().size)
    assertEquals(incomingEdges1.size, g.nodes().size)
    val dsp = DijkstraShortestPath<String, Int>(g, nev, cached)
    val edgeArray = edgeArrays[g]!!

    // test getDistance(start, v)
    for (i in 1..distances1.size) {
      val v = indexer.inverse()[i]!!
      val n = dsp.getDistance(start, v)
      val d = distances1[i - 1]
      val dist: Double
      if (n == null) {
        dist = Double.POSITIVE_INFINITY
      } else {
        dist = n.toDouble()
      }

      assertEquals(d, dist, .001)
    }

    // test getIncomingEdge(start, v)
    dsp.reset()
    for (i in 1..incomingEdges1.size) {
      val v = indexer.inverse()[i]!!
      val e = dsp.getIncomingEdge(start, v)
      if (e != null) {
        assertEquals(edgeArray[incomingEdges1[i - 1]!!], e)
      } else {
        assertNull(incomingEdges1[i - 1])
      }
    }

    // test getDistanceMap(v)
    dsp.reset()
    var distances = dsp.getDistanceMap(start)
    assertTrue(distances.size <= g.nodes().size)
    var dPrev = 0.0 // smallest possible distance
    val reachable = HashSet<String>()
    for (cur in distances.keys) {
      val dCur = distances[cur]!!.toDouble()
      assertTrue(dCur >= dPrev)

      dPrev = dCur
      val i = indexer[cur]!!
      assertEquals(distances1[i - 1], dCur, .001)
      reachable.add(cur)
    }
    // make sure that non-reachable nodes have no entries
    for (v in g.nodes()) {
      assertEquals(reachable.contains(v), distances.keys.contains(v))
    }

    // test getIncomingEdgeMap(v)
    dsp.reset()
    var incomingEdgeMap = dsp.getIncomingEdgeMap(start)
    assertTrue(incomingEdgeMap.size <= g.nodes().size)
    for (v in incomingEdgeMap.keys) {
      val e = incomingEdgeMap[v]
      val i = indexer[v]!!
      if (e != null) {
        assertEquals(edgeArray[incomingEdges1[i - 1]!!], e)
      } else {
        assertNull(incomingEdges1[i - 1])
      }
    }

    // test getDistanceMap(v, k)
    dsp.reset()
    for (i in 1..distances1.size) {
      distances = dsp.getDistanceMap(start, i)
      assertTrue(distances.size <= i)
      dPrev = 0.0 // smallest possible distance

      reachable.clear()
      for (cur in distances.keys) {
        val dCur = distances[cur]!!.toDouble()
        assertTrue(dCur >= dPrev)

        dPrev = dCur
        val j = indexer[cur]!!

        assertEquals(distances1[j - 1], dCur, .001)
        reachable.add(cur)
      }
      for (node in g.nodes()) {
        assertEquals(reachable.contains(node), distances.keys.contains(node))
      }
    }

    // test getIncomingEdgeMap(v, k)
    dsp.reset()
    for (i in 1..incomingEdges1.size) {
      incomingEdgeMap = dsp.getIncomingEdgeMap(start, i)
      assertTrue(incomingEdgeMap.size <= i)
      for (v in incomingEdgeMap.keys) {
        val e = incomingEdgeMap[v]
        val j = indexer[v]!!
        if (e != null) {
          assertEquals(edgeArray[incomingEdges1[j - 1]!!], e)
        } else {
          assertNull(incomingEdges1[j - 1])
        }
      }
    }
  }

  private fun addEdges(
      g: MutableNetwork<String, Int>, indexer: BiMap<String, Int>, edgeArray: Array<Int?>) {
    for (i in edges.indices) {
      val edge = edges[i]
      val e = i
      g.addEdge(indexer.inverse()[edge[0]]!!, indexer.inverse()[edge[1]]!!, i)
      edgeArray[i] = e
      if (edge.size > 2) {
        edgeWeights[e] = edge[2]
      }
    }
  }
}
