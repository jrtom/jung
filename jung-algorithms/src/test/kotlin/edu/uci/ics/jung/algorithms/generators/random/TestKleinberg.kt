package edu.uci.ics.jung.algorithms.generators.random

import edu.uci.ics.jung.algorithms.generators.Lattice2DGenerator
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.function.Supplier

/**
 * @author Joshua O'Madadhain
 */
@RunWith(JUnit4::class)
class TestKleinberg {

  protected lateinit var nodeFactory: Supplier<String>
  protected lateinit var edgeFactory: Supplier<Integer>

  @Before
  fun setUp() {
    nodeFactory = object : Supplier<String> {
      var count = 0
      override fun get(): String = ('a' + count++).toChar().toString()
    }
    edgeFactory = object : Supplier<Integer> {
      var count = 0
      override fun get(): Integer = count++ as Integer
    }
  }

  @Test
  fun testConnectionCount() {
    val generator = Lattice2DGenerator<String, Integer>(4, 4, true /* toroidal */)
    val graph = generator.generateNetwork(true /* directed */, nodeFactory, edgeFactory)
    val connectionCount = 2

    val ksw = KleinbergSmallWorld.builder<String, Integer>().connectionCount(connectionCount).build()
    ksw.addSmallWorldConnections(graph, generator.distance(graph.asGraph()), edgeFactory)

    for (node in graph.nodes()) {
      assertEquals(graph.outDegree(node), 4 + connectionCount)
    }
  }
}
