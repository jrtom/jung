/*
 * Created on May 3, 2004
 *
 * Copyright (c) 2004, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.io

import com.google.common.graph.MutableNetwork
import com.google.common.graph.Network
import com.google.common.graph.NetworkBuilder
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.StringReader
import java.util.function.Function
import java.util.function.Supplier
import junit.framework.Assert
import junit.framework.TestCase

/**
 * Needed tests: - edgeslist, arcslist - unit test to catch bug in readArcsOrEdges() [was skipping
 * until e_pred, not c_pred]
 *
 * @author Joshua O'Madadhain
 * @author Tom Nelson - converted to jung2
 */
class PajekNetIOTest : TestCase() {
  protected val node_labels = arrayOf("alpha", "beta", "gamma", "delta", "epsilon")

  val directedGraphFactory: Supplier<MutableNetwork<Number, Number>> =
    object : Supplier<MutableNetwork<Number, Number>> {
      override fun get(): MutableNetwork<Number, Number> {
        return NetworkBuilder.directed().allowsSelfLoops(true).build()
      }
    }

  val undirectedGraphFactory: Supplier<MutableNetwork<Number, Number>> =
    object : Supplier<MutableNetwork<Number, Number>> {
      override fun get(): MutableNetwork<Number, Number> {
        return NetworkBuilder.undirected().allowsSelfLoops(true).build()
      }
    }

  lateinit var nodeFactory: Supplier<Number>
  lateinit var edgeFactory: Supplier<Number>
  lateinit var pnr: PajekNetReader<MutableNetwork<Number, Number>, Number, Number>

  override fun setUp() {
    nodeFactory = object : Supplier<Number> {
      var n = 0
      override fun get(): Number = n++
    }
    edgeFactory = object : Supplier<Number> {
      var n = 0
      override fun get(): Number = n++
    }
    pnr = PajekNetReader<MutableNetwork<Number, Number>, Number, Number>(
      nodeFactory, edgeFactory
    )
  }

  fun testNull() {}

  fun testFileNotFound() {
    try {
      pnr.load("/dev/null/foo", directedGraphFactory)
      fail("File load did not fail on nonexistent file")
    } catch (fnfe: FileNotFoundException) {
    } catch (ioe: IOException) {
      fail("unexpected IOException")
    }
  }

  fun testNoLabels() {
    val test = "*Nodes 3\n1\n2\n3\n*Edges\n1 2\n2 2"
    val r = StringReader(test)

    val g = pnr.load(r, undirectedGraphFactory)
    assertEquals(g.nodes().size, 3)
    assertEquals(g.edges().size, 2)
  }

  fun testDirectedSaveLoadSave() {
    val graph1 = directedGraphFactory.get()
    for (i in 1..5) {
      graph1.addNode(i)
    }
    val id = ArrayList<Number>(graph1.nodes())
    val gl = GreekLabels(id)
    var j = 0
    graph1.addEdge(1, 2, j++)
    graph1.addEdge(1, 3, j++)
    graph1.addEdge(2, 3, j++)
    graph1.addEdge(2, 4, j++)
    graph1.addEdge(2, 5, j++)
    graph1.addEdge(5, 3, j++)

    assertEquals(graph1.edges().size, 6)

    val testFilename = "dtest.net"
    val testFilename2 = testFilename + "2"

    val pnw = PajekNetWriter<Number, Number>()
    pnw.save(graph1, testFilename, gl, null, null)

    val graph2 = pnr.load(testFilename, directedGraphFactory)

    assertEquals(graph1.nodes().size, graph2.nodes().size)
    assertEquals(graph1.edges().size, graph2.edges().size)

    pnw.save(graph2, testFilename2, pnr.getNodeLabeller(), null, null)

    compareIndexedGraphs(graph1, graph2)

    val graph3 = pnr.load(testFilename2, directedGraphFactory)

    compareIndexedGraphs(graph2, graph3)

    val file1 = File(testFilename)
    val file2 = File(testFilename2)

    Assert.assertTrue(file1.length() == file2.length())
    file1.delete()
    file2.delete()
  }

  fun testUndirectedSaveLoadSave() {
    val graph1 = undirectedGraphFactory.get()
    for (i in 1..5) {
      graph1.addNode(i)
    }

    val id = ArrayList<Number>(graph1.nodes())
    var j = 0
    val gl = GreekLabels(id)
    graph1.addEdge(1, 2, j++)
    graph1.addEdge(1, 3, j++)
    graph1.addEdge(2, 3, j++)
    graph1.addEdge(2, 4, j++)
    graph1.addEdge(2, 5, j++)
    graph1.addEdge(5, 3, j++)

    assertEquals(graph1.edges().size, 6)

    val testFilename = "utest.net"
    val testFilename2 = testFilename + "2"

    val pnw = PajekNetWriter<Number, Number>()
    pnw.save(graph1, testFilename, gl, null, null)

    val graph2 = pnr.load(testFilename, undirectedGraphFactory)

    assertEquals(graph1.nodes().size, graph2.nodes().size)
    assertEquals(graph1.edges().size, graph2.edges().size)

    pnw.save(graph2, testFilename2, pnr.getNodeLabeller(), null, null)
    compareIndexedGraphs(graph1, graph2)

    val graph3 = pnr.load(testFilename2, undirectedGraphFactory)

    compareIndexedGraphs(graph2, graph3)

    val file1 = File(testFilename)
    val file2 = File(testFilename2)

    Assert.assertTrue(file1.length() == file2.length())
    file1.delete()
    file2.delete()
  }

  /**
   * Tests to see whether these two graphs are structurally equivalent, based on the connectivity of
   * the nodes with matching indices in each graph. Assumes a 0-based index.
   */
  private fun compareIndexedGraphs(g1: Network<Number, Number>, g2: Network<Number, Number>) {
    val n1 = g1.nodes().size
    val n2 = g2.nodes().size

    assertEquals(n1, n2)

    assertEquals(g1.edges().size, g2.edges().size)

    val id1 = ArrayList<Number>(g1.nodes())
    val id2 = ArrayList<Number>(g2.nodes())

    for (i in 0 until n1) {
      val v1 = id1[i]
      val v2 = id2[i]
      assertNotNull(v1)
      assertNotNull(v2)

      checkSets(g1.predecessors(v1), g2.predecessors(v2), id1, id2)
      checkSets(g1.successors(v1), g2.successors(v2), id1, id2)
    }
  }

  private fun checkSets(
    s1: Collection<Number>,
    s2: Collection<Number>,
    id1: List<Number>,
    id2: List<Number>
  ) {
    for (u in s1) {
      val j = id1.indexOf(u)
      assertTrue(s2.contains(id2[j]))
    }
  }

  private inner class GreekLabels<N>(private val id: List<N>) : Function<N, String> {
    override fun apply(v: N): String {
      return node_labels[id.indexOf(v)]
    }
  }
}
