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
import edu.uci.ics.jung.io.graphml.PortMetadata
import org.junit.Assert
import org.junit.Test

class TestPortElementParser : AbstractParserTest() {

  @Test(expected = GraphIOException::class)
  fun testNoName() {
    val xml = "<port/>"
    readObject(xml)
  }

  @Test
  fun testName() {
    val xml = "<port name=\"p1\"/>"

    val port = readObject(xml) as PortMetadata
    Assert.assertNotNull(port)
    Assert.assertEquals("p1", port.name)
    Assert.assertEquals(null, port.description)
  }

  @Test
  fun testDesc() {
    val xml = "<port name=\"p1\">" + "<desc>this is my port</desc>" + "</port>"

    val port = readObject(xml) as PortMetadata
    Assert.assertNotNull(port)
    Assert.assertEquals("p1", port.name)
    Assert.assertEquals("this is my port", port.description)
  }

  @Test
  fun testUserAttributes() {
    val xml = "<port name=\"p1\" bob=\"abc123\"/>"

    val port = readObject(xml) as PortMetadata
    Assert.assertNotNull(port)
    Assert.assertEquals("p1", port.name)
    Assert.assertEquals(1, port.properties.size)
    Assert.assertEquals("abc123", port.getProperty("bob"))
  }

  @Test
  fun testData() {
    val xml = "<port name=\"p1\">" +
      "<data key=\"d1\">value1</data>" +
      "<data key=\"d2\">value2</data>" +
      "</port>"

    val port = readObject(xml) as PortMetadata
    Assert.assertNotNull(port)
    Assert.assertEquals("p1", port.name)
    Assert.assertEquals(2, port.properties.size)
    Assert.assertEquals("value1", port.getProperty("d1"))
    Assert.assertEquals("value2", port.getProperty("d2"))
  }
}
