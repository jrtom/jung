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
import edu.uci.ics.jung.io.GraphIOException
import edu.uci.ics.jung.io.graphml.DataMetadata
import edu.uci.ics.jung.io.graphml.ExceptionConverter
import edu.uci.ics.jung.io.graphml.GraphMLConstants
import edu.uci.ics.jung.io.graphml.NodeMetadata
import edu.uci.ics.jung.io.graphml.PortMetadata
import javax.xml.stream.XMLEventReader
import javax.xml.stream.events.Attribute
import javax.xml.stream.events.EndElement
import javax.xml.stream.events.StartElement

/**
 * Parses node elements.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
class NodeElementParser<G : MutableNetwork<N, E>, N, E>(
  parserContext: ParserContext<G, N, E>
) : AbstractElementParser<G, N, E>(parserContext) {

  @Suppress("UNCHECKED_CAST")
  @Throws(GraphIOException::class)
  override fun parse(xmlEventReader: XMLEventReader, start: StartElement): NodeMetadata? {
    try {
      // Create the new node.
      val node = NodeMetadata()

      // Parse the attributes.
      val iterator = start.attributes as Iterator<Attribute>
      while (iterator.hasNext()) {
        val attribute = iterator.next()
        val name = attribute.name.localPart
        val value = attribute.value
        if (node.id == null && GraphMLConstants.ID_NAME == name) {
          node.id = value
        } else {
          node.setProperty(name, value)
        }
      }

      // Make sure the name has been set.
      if (node.id == null) {
        throw GraphIOException("Element 'node' is missing attribute 'id'")
      }

      while (xmlEventReader.hasNext()) {
        val event = xmlEventReader.nextEvent()
        if (event.isStartElement) {
          val element = event as StartElement
          val name = element.name.localPart
          if (GraphMLConstants.DESC_NAME == name) {
            val desc = getParser(name).parse(xmlEventReader, element) as String
            node.description = desc
          } else if (GraphMLConstants.DATA_NAME == name) {
            val data = getParser(name).parse(xmlEventReader, element) as DataMetadata
            node.addData(data)
          } else if (GraphMLConstants.PORT_NAME == name) {
            val port = getParser(name).parse(xmlEventReader, element) as PortMetadata
            node.addPort(port)
          } else {
            // Treat anything else as unknown
            getUnknownParser().parse(xmlEventReader, element)
          }
        } else if (event.isEndElement) {
          val end = event as EndElement
          verifyMatch(start, end)
          break
        }
      }

      // Apply the keys to this object.
      applyKeys(node)

      return node
    } catch (e: Exception) {
      ExceptionConverter.convert(e)
    }

    return null
  }
}
