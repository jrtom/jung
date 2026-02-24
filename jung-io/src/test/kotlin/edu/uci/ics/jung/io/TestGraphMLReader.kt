/*
 * Copyright (c) 2003, The JUNG Authors
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
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.function.Supplier
import junit.framework.Assert
import junit.framework.Test
import junit.framework.TestCase
import junit.framework.TestSuite

/**
 * @author Scott White
 * @author Tom Nelson - converted to jung2
 */
class TestGraphMLReader : TestCase() {

  lateinit var graphFactory: Supplier<MutableNetwork<Number, Number>>
  lateinit var nodeFactory: Supplier<Number>
  lateinit var edgeFactory: Supplier<Number>
  lateinit var gmlreader: GraphMLReader<MutableNetwork<Number, Number>, Number, Number>

  companion object {
    @JvmStatic
    fun suite(): Test {
      return TestSuite(TestGraphMLReader::class.java)
    }
  }

  override fun setUp() {
    graphFactory = object : Supplier<MutableNetwork<Number, Number>> {
      override fun get(): MutableNetwork<Number, Number> {
        return NetworkBuilder.directed()
          .allowsSelfLoops(true)
          .allowsParallelEdges(true)
          .build()
      }
    }
    nodeFactory = object : Supplier<Number> {
      var n = 0
      override fun get(): Number = n++
    }
    edgeFactory = object : Supplier<Number> {
      var n = 0
      override fun get(): Number = n++
    }
    gmlreader = GraphMLReader<MutableNetwork<Number, Number>, Number, Number>(nodeFactory, edgeFactory)
  }

  fun testLoad() {
    val testFilename = "toy_graph.ml"

    val graph = loadGraph(testFilename)

    Assert.assertEquals(graph.nodes().size, 3)
    Assert.assertEquals(graph.edges().size, 3)

    val node_ids = gmlreader.getNodeIDs()

    val joe = node_ids.inverse()["1"]!!
    val bob = node_ids.inverse()["2"]!!
    val sue = node_ids.inverse()["3"]!!

    val node_metadata = gmlreader.getNodeMetadata()
    val name = node_metadata["name"]!!.transformer
    Assert.assertEquals(name.apply(joe), "Joe")
    Assert.assertEquals(name.apply(bob), "Bob")
    Assert.assertEquals(name.apply(sue), "Sue")

    Assert.assertTrue(graph.predecessors(joe).contains(bob))
    Assert.assertTrue(graph.predecessors(bob).contains(joe))
    Assert.assertTrue(graph.predecessors(sue).contains(joe))
    Assert.assertFalse(graph.predecessors(joe).contains(sue))
    Assert.assertFalse(graph.predecessors(sue).contains(bob))
    Assert.assertFalse(graph.predecessors(bob).contains(sue))

    val testFile = File(testFilename)
    testFile.delete()
  }

  @Throws(IOException::class)
  fun testAttributes() {
    val graph: MutableNetwork<Number, Number> =
      NetworkBuilder.undirected().allowsSelfLoops(true).build()
    gmlreader.load("src/test/resources/edu/uci/ics/jung/io/graphml/attributes.graphml", graph)

    Assert.assertEquals(graph.nodes().size, 6)
    Assert.assertEquals(graph.edges().size, 7)

    // test node IDs
    val node_ids = gmlreader.getNodeIDs()
    for (entry in node_ids.entries) {
      Assert.assertEquals(entry.value[0], 'n')
      Assert.assertEquals(
        Integer.parseInt(entry.value.substring(1)), entry.key.toInt()
      )
    }

    // test edge IDs
    val edge_ids = gmlreader.getEdgeIDs()
    for (entry in edge_ids.entries) {
      Assert.assertEquals(entry.value[0], 'e')
      Assert.assertEquals(
        Integer.parseInt(entry.value.substring(1)), entry.key.toInt()
      )
    }

    // test data
    val node_metadata = gmlreader.getNodeMetadata()
    val edge_metadata = gmlreader.getEdgeMetadata()

    // test node colors
    val node_color = node_metadata["d0"]!!.transformer
    Assert.assertEquals(node_color.apply(0), "green")
    Assert.assertEquals(node_color.apply(1), "yellow")
    Assert.assertEquals(node_color.apply(2), "blue")
    Assert.assertEquals(node_color.apply(3), "red")
    Assert.assertEquals(node_color.apply(4), "yellow")
    Assert.assertEquals(node_color.apply(5), "turquoise")

    // test edge weights
    val edge_weight = edge_metadata["d1"]!!.transformer
    Assert.assertEquals(edge_weight.apply(0), "1.0")
    Assert.assertEquals(edge_weight.apply(1), "1.0")
    Assert.assertEquals(edge_weight.apply(2), "2.0")
    Assert.assertEquals(edge_weight.apply(3), null)
    Assert.assertEquals(edge_weight.apply(4), null)
    Assert.assertEquals(edge_weight.apply(5), null)
    Assert.assertEquals(edge_weight.apply(6), "1.1")
  }

  private fun loadGraph(testFilename: String): Network<Number, Number> {
    val writer = BufferedWriter(FileWriter(testFilename))
    writer.write("<?xml version=\"1.0\" encoding=\"iso-8859-1\" ?>\n")
    writer.write("<?meta name=\"GENERATOR\" content=\"XML::Smart 1.3.1\" ?>\n")
    writer.write("<graph edgedefault=\"directed\">\n")
    writer.write("<node id=\"1\" name=\"Joe\"/>\n")
    writer.write("<node id=\"2\" name=\"Bob\"/>\n")
    writer.write("<node id=\"3\" name=\"Sue\"/>\n")
    writer.write("<edge source=\"1\" target=\"2\"/>\n")
    writer.write("<edge source=\"2\" target=\"1\"/>\n")
    writer.write("<edge source=\"1\" target=\"3\"/>\n")
    writer.write("</graph>\n")
    writer.close()

    val graph = graphFactory.get()
    gmlreader.load(testFilename, graph)
    return graph
  }
}
