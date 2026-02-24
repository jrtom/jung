/*
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.io.graphml

import com.google.common.graph.MutableNetwork
import com.google.common.graph.Network
import edu.uci.ics.jung.io.GraphIOException
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringReader
import java.util.function.Function
import org.junit.After
import org.junit.Assert
import org.junit.Test

class TestGraphMLReader2 {

  companion object {
    const val graphMLDocStart =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
        "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">"
  }

  private var reader: GraphMLReader2<MutableNetwork<DummyNode, DummyEdge>, DummyNode, DummyEdge>? = null

  @After
  fun tearDown() {
    reader?.close()
    reader = null
  }

  @Test(expected = GraphIOException::class)
  fun testEmptyFile() {
    val xml = ""
    readGraph(
      xml,
      DummyGraphObjectBase.UndirectedNetworkFactory(),
      DummyNode.Factory(),
      DummyEdge.EdgeFactory()
    )
  }

  @Test
  fun testBasics() {
    val xml = graphMLDocStart +
      "<key id=\"d0\" for=\"node\" attr.name=\"color\" attr.type=\"string\">" +
      "<default>yellow</default>" +
      "</key>" +
      "<key id=\"d1\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\"/>" +
      "<graph id=\"G\" edgedefault=\"undirected\">" +
      "<node id=\"n0\">" +
      "<data key=\"d0\">green</data>" +
      "</node>" +
      "<node id=\"n1\"/>" +
      "<node id=\"n2\">" +
      "<data key=\"d0\">blue</data>" +
      "</node>" +
      "<edge id=\"e0\" source=\"n0\" target=\"n2\">" +
      "<data key=\"d1\">1.0</data>" +
      "</edge>" +
      "</graph>" +
      "</graphml>"

    // Read the graph object.
    val graph = readGraph(
      xml,
      DummyGraphObjectBase.UndirectedNetworkFactory(),
      DummyNode.Factory(),
      DummyEdge.EdgeFactory()
    )

    // Check out the graph.
    Assert.assertNotNull(graph)
    Assert.assertEquals(3, graph.nodes().size)
    Assert.assertEquals(1, graph.edges().size)

    // Check out metadata.
    Assert.assertEquals(1, reader!!.getGraphMLDocument().graphMetadata.size)
    val edges = ArrayList(
      reader!!.getGraphMLDocument().graphMetadata[0].edgeMap.values
    )
    Assert.assertEquals(1, edges.size)
    Assert.assertEquals("n0", edges[0].source)
    Assert.assertEquals("n2", edges[0].target)
  }

  @Test
  fun testData() {
    val xml = graphMLDocStart +
      "<key id=\"d0\" for=\"node\" attr.name=\"color\" attr.type=\"string\">" +
      "<default>yellow</default>" +
      "</key>" +
      "<key id=\"d1\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\"/>" +
      "<graph id=\"G\" edgedefault=\"undirected\">" +
      "<node id=\"n0\">" +
      "<data key=\"d0\">green</data>" +
      "</node>" +
      "<node id=\"n1\"/>" +
      "<node id=\"n2\">" +
      "<data key=\"d0\">blue</data>" +
      "</node>" +
      "<edge id=\"e0\" source=\"n0\" target=\"n2\">" +
      "<data key=\"d1\">1.0</data>" +
      "</edge>" +
      "</graph>" +
      "</graphml>"

    // Read the graph object.
    readGraph(
      xml,
      DummyGraphObjectBase.UndirectedNetworkFactory(),
      DummyNode.Factory(),
      DummyEdge.EdgeFactory()
    )

    // Check out metadata.
    Assert.assertEquals(1, reader!!.getGraphMLDocument().graphMetadata.size)
    val edges = ArrayList(
      reader!!.getGraphMLDocument().graphMetadata[0].edgeMap.values
    )
    val nodes = ArrayList(
      reader!!.getGraphMLDocument().graphMetadata[0].nodeMap.values
    )
    nodes.sortWith(compareBy { it.id })
    Assert.assertEquals(1, edges.size)
    Assert.assertEquals("1.0", edges[0].properties["d1"])
    Assert.assertEquals(3, nodes.size)
    Assert.assertEquals("green", nodes[0].properties["d0"])
    Assert.assertEquals("yellow", nodes[1].properties["d0"])
    Assert.assertEquals("blue", nodes[2].properties["d0"])
  }

  @Test(expected = GraphIOException::class)
  fun testEdgeWithInvalidNode() {
    val xml = graphMLDocStart +
      "<key id=\"d0\" for=\"node\" attr.name=\"color\" attr.type=\"string\">" +
      "<default>yellow</default>" +
      "</key>" +
      "<key id=\"d1\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\"/>" +
      "<graph id=\"G\" edgedefault=\"undirected\">" +
      "<node id=\"n0\">" +
      "<data key=\"d0\">green</data>" +
      "</node>" +
      "<node id=\"n1\"/>" +
      "<node id=\"n2\">" +
      "<data key=\"d0\">blue</data>" +
      "</node>" +
      "<edge id=\"e0\" source=\"n0\" target=\"n3\">" + // Invalid node: n3
      "<data key=\"d1\">1.0</data>" +
      "</edge>" +
      "</graphml>"

    readGraph(
      xml,
      DummyGraphObjectBase.UndirectedNetworkFactory(),
      DummyNode.Factory(),
      DummyEdge.EdgeFactory()
    )
  }

  @Test
  fun testAttributesFile() {
    // Read the graph object.
    val graph = readGraphFromFile(
      "attributes.graphml",
      DummyGraphObjectBase.UndirectedNetworkFactory(),
      DummyNode.Factory(),
      DummyEdge.EdgeFactory()
    )

    Assert.assertEquals(6, graph.nodes().size)
    Assert.assertEquals(7, graph.edges().size)

    Assert.assertEquals(1, reader!!.getGraphMLDocument().graphMetadata.size)

    // Test node ids
    var id = 0
    val nodes = ArrayList(
      reader!!.getGraphMLDocument().graphMetadata[0].nodeMap.values
    )
    nodes.sortWith(compareBy { it.id })
    Assert.assertEquals(6, nodes.size)
    for (md in nodes) {
      Assert.assertEquals('n', md.id!![0])
      Assert.assertEquals(id++, Integer.parseInt(md.id!!.substring(1)))
    }

    // Test edge ids
    id = 0
    val edges = ArrayList<EdgeMetadata>(
      reader!!.getGraphMLDocument().graphMetadata[0].edgeMap.values
    )
    edges.sortWith(compareBy { it.id })
    Assert.assertEquals(7, edges.size)
    for (md in edges) {
      Assert.assertEquals('e', md.id!![0])
      Assert.assertEquals(id++, Integer.parseInt(md.id!!.substring(1)))
    }

    Assert.assertEquals("green", nodes[0].properties["d0"])
    Assert.assertEquals("yellow", nodes[1].properties["d0"])
    Assert.assertEquals("blue", nodes[2].properties["d0"])
    Assert.assertEquals("red", nodes[3].properties["d0"])
    Assert.assertEquals("yellow", nodes[4].properties["d0"])
    Assert.assertEquals("turquoise", nodes[5].properties["d0"])

    Assert.assertEquals("1.0", edges[0].getProperty("d1"))
    Assert.assertEquals("1.0", edges[1].getProperty("d1"))
    Assert.assertEquals("2.0", edges[2].getProperty("d1"))
    Assert.assertNull(edges[3].getProperty("d1"))
    Assert.assertNull(edges[4].getProperty("d1"))
    Assert.assertNull(edges[5].getProperty("d1"))
    Assert.assertEquals("1.1", edges[6].getProperty("d1"))
  }

  private fun readGraph(
    xml: String,
    gf: Function<GraphMetadata, MutableNetwork<DummyNode, DummyEdge>>,
    nf: DummyNode.Factory,
    ef: DummyEdge.EdgeFactory
  ): Network<DummyNode, DummyEdge> {
    val fileReader: Reader = StringReader(xml)
    reader = GraphMLReader2<MutableNetwork<DummyNode, DummyEdge>, DummyNode, DummyEdge>(
      fileReader, gf, nf, ef
    )
    return reader!!.readGraph()
  }

  private fun readGraphFromFile(
    file: String,
    gf: Function<GraphMetadata, MutableNetwork<DummyNode, DummyEdge>>,
    nf: DummyNode.Factory,
    ef: DummyEdge.EdgeFactory
  ): Network<DummyNode, DummyEdge> {
    val inputStream = javaClass.getResourceAsStream(file)
    val fileReader: Reader = InputStreamReader(inputStream)
    reader = GraphMLReader2<MutableNetwork<DummyNode, DummyEdge>, DummyNode, DummyEdge>(
      fileReader, gf, nf, ef
    )
    return reader!!.readGraph()
  }
}
