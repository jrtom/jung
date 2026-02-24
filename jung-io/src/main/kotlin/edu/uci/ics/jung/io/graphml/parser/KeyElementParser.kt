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
import edu.uci.ics.jung.io.graphml.ExceptionConverter
import edu.uci.ics.jung.io.graphml.GraphMLConstants
import edu.uci.ics.jung.io.graphml.Key
import javax.xml.stream.XMLEventReader
import javax.xml.stream.events.Attribute
import javax.xml.stream.events.EndElement
import javax.xml.stream.events.StartElement

/**
 * Parses key elements.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
class KeyElementParser<G : MutableNetwork<N, E>, N, E>(
  parserContext: ParserContext<G, N, E>
) : AbstractElementParser<G, N, E>(parserContext) {

  @Throws(GraphIOException::class)
  override fun parse(xmlEventReader: XMLEventReader, start: StartElement): Key? {
    try {
      // Create the new key. ForType defaults to ALL.
      val key = Key()

      // Parse the attributes.
      @Suppress("UNCHECKED_CAST")
      val iterator = start.attributes as Iterator<Attribute>
      while (iterator.hasNext()) {
        val attribute = iterator.next()
        val name = attribute.name.localPart
        val value = attribute.value
        if (key.id == null && GraphMLConstants.ID_NAME == name) {
          key.id = value
        } else if (key.attributeName == null && GraphMLConstants.ATTRNAME_NAME == name) {
          key.attributeName = value
        } else if (key.attributeType == null && GraphMLConstants.ATTRTYPE_NAME == name) {
          key.attributeType = value
        } else if (GraphMLConstants.FOR_NAME == name) {
          key.forType = convertFor(value)
        }
      }

      // Make sure the id has been set.
      if (key.id == null) {
        throw GraphIOException("Element 'key' is missing attribute 'id'")
      }

      while (xmlEventReader.hasNext()) {
        val event = xmlEventReader.nextEvent()
        if (event.isStartElement) {
          val element = event as StartElement
          val name = element.name.localPart
          if (GraphMLConstants.DESC_NAME == name) {
            val desc = getParser(name).parse(xmlEventReader, element) as String
            key.description = desc
          } else if (GraphMLConstants.DEFAULT_NAME == name) {
            val defaultValue = getParser(name).parse(xmlEventReader, element) as String
            key.defaultValue = defaultValue
          } else {
            // Treat anything else as unknown
            getUnknownParser().parse(xmlEventReader, element)
          }
        }
        if (event.isEndElement) {
          val end = event as EndElement
          verifyMatch(start, end)
          break
        }
      }

      return key
    } catch (e: Exception) {
      ExceptionConverter.convert(e)
    }

    return null
  }

  companion object {
    @JvmStatic
    fun convertFor(value: String?): Key.ForType {
      if (value != null) {
        if (GraphMLConstants.GRAPH_NAME == value) {
          return Key.ForType.GRAPH
        }
        if (GraphMLConstants.EDGE_NAME == value) {
          return Key.ForType.EDGE
        }
        if (GraphMLConstants.ENDPOINT_NAME == value) {
          return Key.ForType.ENDPOINT
        }
        if (GraphMLConstants.NODE_NAME == value) {
          return Key.ForType.NODE
        }
        if (GraphMLConstants.PORT_NAME == value) {
          return Key.ForType.PORT
        }
      }
      return Key.ForType.ALL
    }
  }
}
