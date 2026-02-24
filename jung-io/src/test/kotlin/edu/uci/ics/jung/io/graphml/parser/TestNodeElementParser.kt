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
import edu.uci.ics.jung.io.graphml.NodeMetadata
import org.junit.Assert
import org.junit.Test

class TestNodeElementParser : AbstractParserTest() {

  @Test(expected = GraphIOException::class)
  fun testNoId() {
    val xml = "<node/>"
    readObject(xml)
  }

  @Test
  fun testId() {
    val xml = "<node id=\"1\"/>"

    val node = readObject(xml) as NodeMetadata
    Assert.assertNotNull(node)
    Assert.assertEquals("1", node.id)
    Assert.assertEquals(null, node.description)
    Assert.assertEquals(0, node.ports.size)
  }

  @Test
  fun testDesc() {
    val xml = "<node id=\"1\">" + "<desc>this is my node</desc>" + "</node>"

    val node = readObject(xml) as NodeMetadata
    Assert.assertNotNull(node)
    Assert.assertEquals("1", node.id)
    Assert.assertEquals("this is my node", node.description)
    Assert.assertEquals(0, node.ports.size)
  }

  @Test
  fun testPort() {
    val xml = "<node id=\"1\">" +
      "<desc>this is my node</desc>" +
      "<port name=\"p1\">" +
      "<desc>port 1</desc>" +
      "</port>" +
      "</node>"

    val node = readObject(xml) as NodeMetadata
    Assert.assertNotNull(node)
    Assert.assertEquals("1", node.id)
    Assert.assertEquals("this is my node", node.description)
    Assert.assertEquals(1, node.ports.size)
    Assert.assertEquals("p1", node.ports[0].name)
  }

  @Test
  fun testMultiPort() {
    val xml = "<node id=\"1\">" +
      "<desc>this is my node</desc>" +
      "<port name=\"p1\"/>" +
      "<port name=\"p2\"/>" +
      "<port name=\"p3\"/>" +
      "<port name=\"p4\"/>" +
      "</node>"

    val node = readObject(xml) as NodeMetadata
    Assert.assertNotNull(node)
    Assert.assertEquals("1", node.id)
    Assert.assertEquals("this is my node", node.description)
    Assert.assertEquals(4, node.ports.size)
    Assert.assertEquals("p1", node.ports[0].name)
    Assert.assertEquals("p2", node.ports[1].name)
    Assert.assertEquals("p3", node.ports[2].name)
    Assert.assertEquals("p4", node.ports[3].name)
  }

  @Test
  fun testUserAttributes() {
    val xml = "<node id=\"1\" bob=\"abc123\"/>"

    val node = readObject(xml) as NodeMetadata
    Assert.assertNotNull(node)
    Assert.assertEquals("1", node.id)
    Assert.assertEquals(1, node.properties.size)
    Assert.assertEquals("abc123", node.getProperty("bob"))
    Assert.assertEquals(0, node.ports.size)
  }

  @Test
  fun testData() {
    val xml = "<node id=\"1\">" +
      "<data key=\"d1\">value1</data>" +
      "<data key=\"d2\">value2</data>" +
      "</node>"

    val node = readObject(xml) as NodeMetadata
    Assert.assertNotNull(node)
    Assert.assertEquals("1", node.id)
    Assert.assertEquals(2, node.properties.size)
    Assert.assertEquals("value1", node.getProperty("d1"))
    Assert.assertEquals("value2", node.getProperty("d2"))
    Assert.assertEquals(0, node.ports.size)
  }
}
