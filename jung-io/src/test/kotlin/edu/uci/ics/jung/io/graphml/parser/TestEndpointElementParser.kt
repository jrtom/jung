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
import edu.uci.ics.jung.io.graphml.EndpointMetadata
import edu.uci.ics.jung.io.graphml.EndpointMetadata.EndpointType
import org.junit.Assert
import org.junit.Test

class TestEndpointElementParser : AbstractParserTest() {

  @Test(expected = GraphIOException::class)
  fun testNoNode() {
    val xml = "<endpoint/>"
    readObject(xml)
  }

  @Test
  fun testId() {
    val xml = "<endpoint node=\"1\" id=\"ep1\"/>"

    val ep = readObject(xml) as EndpointMetadata
    Assert.assertNotNull(ep)
    Assert.assertEquals("1", ep.node)
    Assert.assertEquals("ep1", ep.id)
    Assert.assertEquals(null, ep.description)
    Assert.assertEquals(null, ep.port)
    Assert.assertEquals(EndpointType.UNDIR, ep.endpointType)
  }

  @Test
  fun testDesc() {
    val xml = "<endpoint node=\"1\" id=\"ep1\">" + "<desc>hello world</desc>" + "</endpoint>"

    val ep = readObject(xml) as EndpointMetadata
    Assert.assertNotNull(ep)
    Assert.assertEquals("1", ep.node)
    Assert.assertEquals("ep1", ep.id)
    Assert.assertEquals("hello world", ep.description)
    Assert.assertEquals(null, ep.port)
    Assert.assertEquals(EndpointType.UNDIR, ep.endpointType)
  }

  @Test
  fun testPort() {
    val xml = "<endpoint node=\"1\" port=\"abc123\" id=\"ep1\">" + "</endpoint>"

    val ep = readObject(xml) as EndpointMetadata
    Assert.assertNotNull(ep)
    Assert.assertEquals("1", ep.node)
    Assert.assertEquals("ep1", ep.id)
    Assert.assertEquals(null, ep.description)
    Assert.assertEquals("abc123", ep.port)
    Assert.assertEquals(EndpointType.UNDIR, ep.endpointType)
  }

  @Test
  fun testTypeIn() {
    val xml = "<endpoint node=\"1\" type=\"in\">" + "</endpoint>"

    val ep = readObject(xml) as EndpointMetadata
    Assert.assertNotNull(ep)
    Assert.assertEquals("1", ep.node)
    Assert.assertEquals(null, ep.id)
    Assert.assertEquals(null, ep.description)
    Assert.assertEquals(null, ep.port)
    Assert.assertEquals(EndpointType.IN, ep.endpointType)
  }

  @Test
  fun testTypeOut() {
    val xml = "<endpoint node=\"1\" type=\"out\">" + "</endpoint>"

    val ep = readObject(xml) as EndpointMetadata
    Assert.assertNotNull(ep)
    Assert.assertEquals("1", ep.node)
    Assert.assertEquals(null, ep.id)
    Assert.assertEquals(null, ep.description)
    Assert.assertEquals(null, ep.port)
    Assert.assertEquals(EndpointType.OUT, ep.endpointType)
  }

  @Test
  fun testTypeUndir() {
    val xml = "<endpoint node=\"1\" type=\"undir\">" + "</endpoint>"

    val ep = readObject(xml) as EndpointMetadata
    Assert.assertNotNull(ep)
    Assert.assertEquals("1", ep.node)
    Assert.assertEquals(null, ep.id)
    Assert.assertEquals(null, ep.description)
    Assert.assertEquals(null, ep.port)
    Assert.assertEquals(EndpointType.UNDIR, ep.endpointType)
  }

  @Test
  fun testTypeInvalid() {
    val xml = "<endpoint node=\"1\" type=\"blaa\">" + "</endpoint>"

    val ep = readObject(xml) as EndpointMetadata
    Assert.assertNotNull(ep)
    Assert.assertEquals("1", ep.node)
    Assert.assertEquals(null, ep.id)
    Assert.assertEquals(null, ep.description)
    Assert.assertEquals(null, ep.port)
    Assert.assertEquals(EndpointType.UNDIR, ep.endpointType)
  }
}
