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

import com.google.common.graph.MutableNetwork
import edu.uci.ics.jung.io.graphml.DummyEdge
import edu.uci.ics.jung.io.graphml.DummyGraphObjectBase
import edu.uci.ics.jung.io.graphml.DummyNode
import edu.uci.ics.jung.io.graphml.KeyMap
import java.io.StringReader
import javax.xml.stream.XMLInputFactory
import org.junit.After
import org.junit.Assert
import org.junit.Before

abstract class AbstractParserTest {

  private var registry: ElementParserRegistry<MutableNetwork<DummyNode, DummyEdge>, DummyNode, DummyEdge>? = null

  @Before
  fun setUp() {
    registry = ElementParserRegistry<MutableNetwork<DummyNode, DummyEdge>, DummyNode, DummyEdge>(
      KeyMap(),
      DummyGraphObjectBase.UndirectedNetworkFactory(),
      DummyNode.Factory(),
      DummyEdge.EdgeFactory()
    )
  }

  @After
  fun tearDown() {
    registry = null
  }

  @Throws(Exception::class)
  protected fun readObject(xml: String): Any? {
    val fileReader = StringReader(xml)
    val factory = XMLInputFactory.newInstance()
    var xmlEventReader = factory.createXMLEventReader(fileReader)
    xmlEventReader = factory.createFilteredReader(xmlEventReader, GraphMLEventFilter())

    try {
      while (xmlEventReader.hasNext()) {
        val event = xmlEventReader.nextEvent()
        if (event.isStartElement) {
          val start = event.asStartElement()
          val name = start.name.localPart
          return registry!!.getParser(name).parse(xmlEventReader, start)
        }
      }
    } finally {
      xmlEventReader.close()
    }

    Assert.fail("failed to read object from XML: $xml")
    return null
  }
}
