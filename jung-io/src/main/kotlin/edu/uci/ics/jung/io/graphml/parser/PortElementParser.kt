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
import edu.uci.ics.jung.io.graphml.PortMetadata
import javax.xml.stream.XMLEventReader
import javax.xml.stream.events.Attribute
import javax.xml.stream.events.EndElement
import javax.xml.stream.events.StartElement

/**
 * Parses port elements.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
class PortElementParser<G : MutableNetwork<N, E>, N, E>(
  parserContext: ParserContext<G, N, E>
) : AbstractElementParser<G, N, E>(parserContext) {

  @Throws(GraphIOException::class)
  override fun parse(xmlEventReader: XMLEventReader, start: StartElement): PortMetadata? {
    try {
      // Create the new port.
      val port = PortMetadata()

      // Parse the attributes.
      @Suppress("UNCHECKED_CAST")
      val iterator = start.attributes as Iterator<Attribute>
      while (iterator.hasNext()) {
        val attribute = iterator.next()
        val name = attribute.name.localPart
        val value = attribute.value
        if (port.name == null && GraphMLConstants.NAME_NAME == name) {
          port.name = value
        } else {
          port.setProperty(name, value)
        }
      }

      // Make sure the name has been set.
      if (port.name == null) {
        throw GraphIOException("Element 'port' is missing attribute 'name'")
      }

      while (xmlEventReader.hasNext()) {
        val event = xmlEventReader.nextEvent()
        if (event.isStartElement) {
          val element = event as StartElement
          val name = element.name.localPart
          if (GraphMLConstants.DESC_NAME == name) {
            val desc = getParser(name).parse(xmlEventReader, element) as String
            port.description = desc
          } else if (GraphMLConstants.DATA_NAME == name) {
            val data = getParser(name).parse(xmlEventReader, element) as DataMetadata
            port.addData(data)
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

      // Apply the keys to this port.
      applyKeys(port)

      return port
    } catch (e: Exception) {
      ExceptionConverter.convert(e)
    }

    return null
  }
}
