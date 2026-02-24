/*
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.io.graphml.parser

import edu.uci.ics.jung.io.GraphIOException
import edu.uci.ics.jung.io.graphml.EdgeMetadata
import edu.uci.ics.jung.io.graphml.GraphMetadata
import edu.uci.ics.jung.io.graphml.GraphMetadata.EdgeDefault
import edu.uci.ics.jung.io.graphml.NodeMetadata
import org.junit.Assert
import org.junit.Test

class TestGraphElementParser : AbstractParserTest() {

  @Test(expected = GraphIOException::class)
  fun testNoEdgeDefault() {
    val xml = "<graph/>"
    readObject(xml)
  }

  @Test
  fun testEdgeDefaultDirected() {
    val xml = "<graph edgedefault=\"directed\"/>"

    val g = readObject(xml) as GraphMetadata
    Assert.assertNotNull(g)
    Assert.assertEquals(EdgeDefault.DIRECTED, g.edgeDefault)
    Assert.assertEquals(null, g.id)
    Assert.assertEquals(null, g.description)
    Assert.assertEquals(0, g.nodeMap.size)
    Assert.assertEquals(0, g.edgeMap.size)
  }

  @Test
  fun testEdgeDefaultUndirected() {
    val xml = "<graph edgedefault=\"undirected\"/>"

    val g = readObject(xml) as GraphMetadata
    Assert.assertNotNull(g)
    Assert.assertEquals(EdgeDefault.UNDIRECTED, g.edgeDefault)
    Assert.assertEquals(null, g.id)
    Assert.assertEquals(null, g.description)
    Assert.assertEquals(0, g.nodeMap.size)
    Assert.assertEquals(0, g.edgeMap.size)
  }

  @Test
  fun testDesc() {
    val xml = "<graph edgedefault=\"undirected\">" + "<desc>hello world</desc>" + "</graph>"

    val g = readObject(xml) as GraphMetadata
    Assert.assertNotNull(g)
    Assert.assertEquals(EdgeDefault.UNDIRECTED, g.edgeDefault)
    Assert.assertEquals(null, g.id)
    Assert.assertEquals("hello world", g.description)
    Assert.assertEquals(0, g.nodeMap.size)
    Assert.assertEquals(0, g.edgeMap.size)
  }

  @Test
  fun testNodes() {
    val xml = "<graph edgedefault=\"undirected\">" +
      "<node id=\"1\"/>" +
      "<node id=\"2\"/>" +
      "<node id=\"3\"/>" +
      "</graph>"

    val g = readObject(xml) as GraphMetadata
    Assert.assertNotNull(g)
    Assert.assertEquals(EdgeDefault.UNDIRECTED, g.edgeDefault)
    Assert.assertEquals(null, g.id)
    Assert.assertEquals(null, g.description)
    Assert.assertEquals(3, g.nodeMap.size)
    val nodes = ArrayList<NodeMetadata>(g.nodeMap.values)
    nodes.sortWith(compareBy { it.id })
    Assert.assertEquals("1", nodes[0].id)
    Assert.assertEquals("2", nodes[1].id)
    Assert.assertEquals("3", nodes[2].id)
  }

  @Test
  fun testEdges() {
    val xml = "<graph edgedefault=\"undirected\">" +
      "<node id=\"1\"/>" +
      "<node id=\"2\"/>" +
      "<node id=\"3\"/>" +
      "<edge source=\"1\" target=\"2\"/>" +
      "<edge source=\"2\" target=\"3\"/>" +
      "</graph>"

    val g = readObject(xml) as GraphMetadata
    Assert.assertNotNull(g)
    Assert.assertEquals(EdgeDefault.UNDIRECTED, g.edgeDefault)
    Assert.assertEquals(null, g.id)
    Assert.assertEquals(null, g.description)
    val edges = ArrayList<EdgeMetadata>(g.edgeMap.values)
    edges.sortWith(compareBy { it.source })
    Assert.assertEquals(2, edges.size)
    Assert.assertEquals("1", edges[0].source)
    Assert.assertEquals("2", edges[1].source)
  }

  @Test
  fun testUserAttributes() {
    val xml = "<graph edgedefault=\"undirected\" bob=\"abc123\">" + "</graph>"

    val g = readObject(xml) as GraphMetadata
    Assert.assertNotNull(g)
    Assert.assertEquals(EdgeDefault.UNDIRECTED, g.edgeDefault)
    Assert.assertEquals(null, g.id)
    Assert.assertEquals(null, g.description)
    Assert.assertEquals(1, g.properties.size)
    Assert.assertEquals("abc123", g.getProperty("bob"))
  }

  @Test
  fun testData() {
    val xml = "<graph edgedefault=\"undirected\">" +
      "<data key=\"d1\">value1</data>" +
      "<data key=\"d2\">value2</data>" +
      "</graph>"

    val g = readObject(xml) as GraphMetadata
    Assert.assertNotNull(g)
    Assert.assertEquals(EdgeDefault.UNDIRECTED, g.edgeDefault)
    Assert.assertEquals(null, g.id)
    Assert.assertEquals(null, g.description)
    Assert.assertEquals(2, g.properties.size)
    Assert.assertEquals("value1", g.getProperty("d1"))
    Assert.assertEquals("value2", g.getProperty("d2"))
  }
}
