package edu.uci.ics.jung.algorithms.generators

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.util.function.Supplier

@RunWith(Parameterized::class)
class TestLattice2D(
  private val edgeType: EdgeType,
  private val topology: Topology
) {
  enum class EdgeType {
    UNDIRECTED,
    DIRECTED
  }

  enum class Topology {
    FLAT,
    TOROIDAL
  }

  companion object {
    @JvmStatic
    @Parameters
    fun data(): Array<Array<Any>> = arrayOf(
      arrayOf(EdgeType.UNDIRECTED, Topology.FLAT),
      arrayOf(EdgeType.DIRECTED, Topology.FLAT),
      arrayOf(EdgeType.UNDIRECTED, Topology.TOROIDAL),
      arrayOf(EdgeType.DIRECTED, Topology.TOROIDAL)
    )
  }

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
  fun testCreateSingular() {
    try {
      @Suppress("UNUSED_VARIABLE")
      var unused = Lattice2DGenerator<Any, Any>(1, 2, toroidal())
      unused = Lattice2DGenerator(2, 1, toroidal())
      fail("Did not reject lattice of size < 2")
    } catch (iae: IllegalArgumentException) {
    }
  }

  @Test
  fun testGenerateNetwork() {
    for (rowCount in 4..6) {
      for (colCount in 4..6) {
        val generator = Lattice2DGenerator<String, Integer>(rowCount, colCount, toroidal())
        val graph = generator.generateNetwork(directed(), nodeFactory, edgeFactory)
        assertEquals(graph.nodes().size, rowCount * colCount)

        val boundaryAdjustment = if (toroidal()) 0 else 1
        var expectedEdgeCount = colCount * (rowCount - boundaryAdjustment) +
            rowCount * (colCount - boundaryAdjustment)
        if (directed()) {
          expectedEdgeCount *= 2
        }
        assertEquals(graph.edges().size, expectedEdgeCount)
        val expectedPerimeterNodes = if (toroidal()) 0 else (rowCount - 1) * 2 + (colCount - 1) * 2
        val nodeCount = graph.nodes().size
        val expectedInteriorNodes = if (toroidal()) nodeCount else nodeCount - expectedPerimeterNodes
        var perimeterNodes = 0
        var interiorNodes = 0
        val degreeMultiplier = if (directed()) 2 else 1
        for (node in graph.nodes()) {
          if (toroidal()) {
            val expectedDegree = 4 * degreeMultiplier
            assertEquals(graph.degree(node), expectedDegree)
            interiorNodes++
          } else {
            val degree = graph.degree(node)
            if (degree == 4 * degreeMultiplier) {
              interiorNodes++
            } else if (degree == 3 * degreeMultiplier || degree == 2 * degreeMultiplier) {
              perimeterNodes++
            } else {
              val message = String.format(
                "degree does not match expectations: " +
                    "degree: %s, multiplier: %s\n" +
                    "row count: %s, col count: %s, toroidal: %s\n" +
                    "graph: %s",
                degree, degreeMultiplier, rowCount, colCount, toroidal(), graph
              )
              fail(message)
            }
          }
        }

        assertEquals(expectedInteriorNodes, interiorNodes)
        assertEquals(expectedPerimeterNodes, perimeterNodes)
      }
    }
  }

  private fun toroidal(): Boolean = when (topology) {
    Topology.TOROIDAL -> true
    Topology.FLAT -> false
  }

  private fun directed(): Boolean = when (edgeType) {
    EdgeType.DIRECTED -> true
    EdgeType.UNDIRECTED -> false
  }
}
