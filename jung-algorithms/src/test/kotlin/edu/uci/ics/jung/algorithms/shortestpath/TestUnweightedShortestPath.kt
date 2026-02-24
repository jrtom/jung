/*
 * Created on Aug 22, 2003
 *
 */
package edu.uci.ics.jung.algorithms.shortestpath

import com.google.common.collect.BiMap
import com.google.common.graph.GraphBuilder
import edu.uci.ics.jung.algorithms.util.Indexer
import junit.framework.Assert
import junit.framework.Test
import junit.framework.TestCase
import junit.framework.TestSuite
import java.util.function.Supplier

/**
 * @author Scott White
 */
class TestUnweightedShortestPath : TestCase() {
  private val nodeFactory = object : Supplier<String> {
    var count = 0
    override fun get(): String {
      return "V" + count++
    }
  }

  lateinit var id: BiMap<String, Int>

  override fun setUp() {}

  companion object {
    fun suite(): Test {
      return TestSuite(TestUnweightedShortestPath::class.java)
    }
  }

  fun testUndirected() {
    val ug = GraphBuilder.undirected().allowsSelfLoops(true).build<String>()
    for (i in 0 until 5) {
      ug.addNode(nodeFactory.get())
    }
    id = Indexer.create<String>(ug.nodes())

    ug.putEdge(id.inverse()[0]!!, id.inverse()[1]!!)
    ug.putEdge(id.inverse()[1]!!, id.inverse()[2]!!)
    ug.putEdge(id.inverse()[2]!!, id.inverse()[3]!!)
    ug.putEdge(id.inverse()[0]!!, id.inverse()[4]!!)
    ug.putEdge(id.inverse()[4]!!, id.inverse()[3]!!)

    val usp = UnweightedShortestPath<String>(ug)
    Assert.assertEquals(usp.getDistance(id.inverse()[0]!!, id.inverse()[3]!!)!!, 2)
    Assert.assertEquals(
        (usp.getDistanceMap(id.inverse()[0]!!)[id.inverse()[3]!!])!!, 2)
    Assert.assertNull(usp.getIncomingEdgeMap(id.inverse()[0]!!)[id.inverse()[0]!!])
    Assert.assertNotNull(usp.getIncomingEdgeMap(id.inverse()[0]!!)[id.inverse()[3]!!])
  }

  fun testDirected() {
    val dg = GraphBuilder.directed().allowsSelfLoops(true).build<String>()
    for (i in 0 until 5) {
      dg.addNode(nodeFactory.get())
    }
    id = Indexer.create<String>(dg.nodes())
    dg.putEdge(id.inverse()[0]!!, id.inverse()[1]!!)
    dg.putEdge(id.inverse()[1]!!, id.inverse()[2]!!)
    dg.putEdge(id.inverse()[2]!!, id.inverse()[3]!!)
    dg.putEdge(id.inverse()[0]!!, id.inverse()[4]!!)
    dg.putEdge(id.inverse()[4]!!, id.inverse()[3]!!)
    dg.putEdge(id.inverse()[3]!!, id.inverse()[0]!!)

    val usp = UnweightedShortestPath<String>(dg)
    Assert.assertEquals(usp.getDistance(id.inverse()[0]!!, id.inverse()[3]!!)!!, 2)
    Assert.assertEquals(
        (usp.getDistanceMap(id.inverse()[0]!!)[id.inverse()[3]!!])!!, 2)
    Assert.assertNull(usp.getIncomingEdgeMap(id.inverse()[0]!!)[id.inverse()[0]!!])
    Assert.assertNotNull(usp.getIncomingEdgeMap(id.inverse()[0]!!)[id.inverse()[3]!!])
  }
}
