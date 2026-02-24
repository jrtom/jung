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
import org.junit.Assert
import org.junit.Test

class TestEdgeElementParser : AbstractParserTest() {

  @Test(expected = GraphIOException::class)
  fun testNoSource() {
    val xml = "<edge target=\"2\"/>"
    readObject(xml)
  }

  @Test(expected = GraphIOException::class)
  fun testNoTarget() {
    val xml = "<edge source=\"2\"/>"
    readObject(xml)
  }

  @Test
  fun testId() {
    val xml = "<edge source=\"1\" target=\"2\" id=\"e1\"/>"

    val edge = readObject(xml) as EdgeMetadata
    Assert.assertNotNull(edge)
    Assert.assertEquals("e1", edge.id)
    Assert.assertEquals(null, edge.description)
    Assert.assertEquals("1", edge.source)
    Assert.assertEquals("2", edge.target)
    Assert.assertEquals(null, edge.isDirected)
    Assert.assertEquals(null, edge.sourcePort)
    Assert.assertEquals(null, edge.targetPort)
  }

  @Test
  fun testDirectedTrue() {
    val xml = "<edge source=\"1\" target=\"2\" directed=\"true\"/>"

    val edge = readObject(xml) as EdgeMetadata
    Assert.assertNotNull(edge)
    Assert.assertEquals(null, edge.id)
    Assert.assertEquals(null, edge.description)
    Assert.assertEquals("1", edge.source)
    Assert.assertEquals("2", edge.target)
    Assert.assertEquals(true, edge.isDirected)
    Assert.assertEquals(null, edge.sourcePort)
    Assert.assertEquals(null, edge.targetPort)
  }

  @Test
  fun testDirectedFalse() {
    val xml = "<edge source=\"1\" target=\"2\" directed=\"false\"/>"

    val edge = readObject(xml) as EdgeMetadata
    Assert.assertNotNull(edge)
    Assert.assertEquals(null, edge.id)
    Assert.assertEquals(null, edge.description)
    Assert.assertEquals("1", edge.source)
    Assert.assertEquals("2", edge.target)
    Assert.assertEquals(false, edge.isDirected)
    Assert.assertEquals(null, edge.sourcePort)
    Assert.assertEquals(null, edge.targetPort)
  }

  @Test
  fun testSourceTargetPorts() {
    val xml = "<edge source=\"1\" target=\"2\" sourceport=\"a\" targetport=\"b\"/>"

    val edge = readObject(xml) as EdgeMetadata
    Assert.assertNotNull(edge)
    Assert.assertEquals(null, edge.id)
    Assert.assertEquals(null, edge.description)
    Assert.assertEquals("1", edge.source)
    Assert.assertEquals("2", edge.target)
    Assert.assertEquals(null, edge.isDirected)
    Assert.assertEquals("a", edge.sourcePort)
    Assert.assertEquals("b", edge.targetPort)
  }

  @Test
  fun testDesc() {
    val xml = "<edge source=\"1\" target=\"2\">" + "<desc>hello world</desc>" + "</edge>"

    val edge = readObject(xml) as EdgeMetadata
    Assert.assertNotNull(edge)
    Assert.assertEquals(null, edge.id)
    Assert.assertEquals("hello world", edge.description)
    Assert.assertEquals("1", edge.source)
    Assert.assertEquals("2", edge.target)
    Assert.assertEquals(null, edge.isDirected)
    Assert.assertEquals(null, edge.sourcePort)
    Assert.assertEquals(null, edge.targetPort)
  }

  @Test
  fun testUserAttributes() {
    val xml = "<edge source=\"1\" target=\"2\" bob=\"abc123\">" + "</edge>"

    val edge = readObject(xml) as EdgeMetadata
    Assert.assertNotNull(edge)
    Assert.assertEquals(null, edge.id)
    Assert.assertEquals(null, edge.description)
    Assert.assertEquals("1", edge.source)
    Assert.assertEquals("2", edge.target)
    Assert.assertEquals(null, edge.isDirected)
    Assert.assertEquals(null, edge.sourcePort)
    Assert.assertEquals(null, edge.targetPort)
    Assert.assertEquals(1, edge.properties.size)
    Assert.assertEquals("abc123", edge.getProperty("bob"))
  }

  @Test
  fun testData() {
    val xml = "<edge source=\"1\" target=\"2\">" +
      "<data key=\"d1\">value1</data>" +
      "<data key=\"d2\">value2</data>" +
      "</edge>"

    val edge = readObject(xml) as EdgeMetadata
    Assert.assertNotNull(edge)
    Assert.assertEquals(null, edge.id)
    Assert.assertEquals(null, edge.description)
    Assert.assertEquals("1", edge.source)
    Assert.assertEquals("2", edge.target)
    Assert.assertEquals(null, edge.isDirected)
    Assert.assertEquals(null, edge.sourcePort)
    Assert.assertEquals(null, edge.targetPort)
    Assert.assertEquals(2, edge.properties.size)
    Assert.assertEquals("value1", edge.getProperty("d1"))
    Assert.assertEquals("value2", edge.getProperty("d2"))
  }
}
