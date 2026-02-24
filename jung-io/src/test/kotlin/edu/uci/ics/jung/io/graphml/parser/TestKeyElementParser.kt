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
import edu.uci.ics.jung.io.graphml.Key
import org.junit.Assert
import org.junit.Test

class TestKeyElementParser : AbstractParserTest() {

  @Test(expected = GraphIOException::class)
  fun testNoId() {
    val xml = "<key/>"
    readObject(xml)
  }

  @Test
  fun testId() {
    val xml = "<key id=\"d1\"/>"

    val key = readObject(xml) as Key
    Assert.assertNotNull(key)
    Assert.assertEquals("d1", key.id)
    Assert.assertEquals(null, key.description)
    Assert.assertEquals(null, key.defaultValue)
    Assert.assertEquals(null, key.attributeName)
    Assert.assertEquals(null, key.attributeType)
    Assert.assertEquals(Key.ForType.ALL, key.forType)
  }

  @Test
  fun testDesc() {
    val xml = "<key id=\"d1\">" + "<desc>this is my key</desc>" + "</key>"

    val key = readObject(xml) as Key
    Assert.assertNotNull(key)
    Assert.assertEquals("d1", key.id)
    Assert.assertEquals("this is my key", key.description)
    Assert.assertEquals(null, key.defaultValue)
    Assert.assertEquals(null, key.attributeName)
    Assert.assertEquals(null, key.attributeType)
    Assert.assertEquals(Key.ForType.ALL, key.forType)
  }

  @Test
  fun testDefault() {
    val xml = "<key id=\"d1\">" + "<default>yellow</default>" + "</key>"

    val key = readObject(xml) as Key
    Assert.assertNotNull(key)
    Assert.assertEquals("d1", key.id)
    Assert.assertEquals(null, key.description)
    Assert.assertEquals("yellow", key.defaultValue)
    Assert.assertEquals(null, key.attributeName)
    Assert.assertEquals(null, key.attributeType)
    Assert.assertEquals(Key.ForType.ALL, key.forType)
  }

  @Test
  fun testAttrNameType() {
    val xml = "<key id=\"d1\" attr.name=\"myattr\" attr.type=\"double\">" + "</key>"

    val key = readObject(xml) as Key
    Assert.assertNotNull(key)
    Assert.assertEquals("d1", key.id)
    Assert.assertEquals(null, key.description)
    Assert.assertEquals(null, key.defaultValue)
    Assert.assertEquals("myattr", key.attributeName)
    Assert.assertEquals("double", key.attributeType)
    Assert.assertEquals(Key.ForType.ALL, key.forType)
  }

  @Test
  fun testForNode() {
    val xml = "<key id=\"d1\" for=\"node\">" + "</key>"

    val key = readObject(xml) as Key
    Assert.assertNotNull(key)
    Assert.assertEquals("d1", key.id)
    Assert.assertEquals(null, key.description)
    Assert.assertEquals(null, key.defaultValue)
    Assert.assertEquals(null, key.attributeName)
    Assert.assertEquals(null, key.attributeType)
    Assert.assertEquals(Key.ForType.NODE, key.forType)
  }

  @Test
  fun testForEdge() {
    val xml = "<key id=\"d1\" for=\"edge\">" + "</key>"

    val key = readObject(xml) as Key
    Assert.assertNotNull(key)
    Assert.assertEquals("d1", key.id)
    Assert.assertEquals(null, key.description)
    Assert.assertEquals(null, key.defaultValue)
    Assert.assertEquals(null, key.attributeName)
    Assert.assertEquals(null, key.attributeType)
    Assert.assertEquals(Key.ForType.EDGE, key.forType)
  }

  @Test
  fun testForGraph() {
    val xml = "<key id=\"d1\" for=\"graph\">" + "</key>"

    val key = readObject(xml) as Key
    Assert.assertNotNull(key)
    Assert.assertEquals("d1", key.id)
    Assert.assertEquals(null, key.description)
    Assert.assertEquals(null, key.defaultValue)
    Assert.assertEquals(null, key.attributeName)
    Assert.assertEquals(null, key.attributeType)
    Assert.assertEquals(Key.ForType.GRAPH, key.forType)
  }

  @Test
  fun testForAll() {
    val xml = "<key id=\"d1\" for=\"all\">" + "</key>"

    val key = readObject(xml) as Key
    Assert.assertNotNull(key)
    Assert.assertEquals("d1", key.id)
    Assert.assertEquals(null, key.description)
    Assert.assertEquals(null, key.defaultValue)
    Assert.assertEquals(null, key.attributeName)
    Assert.assertEquals(null, key.attributeType)
    Assert.assertEquals(Key.ForType.ALL, key.forType)
  }
}
