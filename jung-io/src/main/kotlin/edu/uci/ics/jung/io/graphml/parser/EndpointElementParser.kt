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
import edu.uci.ics.jung.io.graphml.EndpointMetadata
import edu.uci.ics.jung.io.graphml.EndpointMetadata.EndpointType
import edu.uci.ics.jung.io.graphml.ExceptionConverter
import edu.uci.ics.jung.io.graphml.GraphMLConstants
import javax.xml.stream.XMLEventReader
import javax.xml.stream.events.Attribute
import javax.xml.stream.events.EndElement
import javax.xml.stream.events.StartElement

/**
 * Parses endpoint elements.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
class EndpointElementParser<G : MutableNetwork<N, E>, N, E>(
  parserContext: ParserContext<G, N, E>
) : AbstractElementParser<G, N, E>(parserContext) {

  @Throws(GraphIOException::class)
  override fun parse(xmlEventReader: XMLEventReader, start: StartElement): EndpointMetadata? {
    try {
      // Create the new endpoint.
      val endpoint = EndpointMetadata()

      // Parse the attributes.
      @Suppress("UNCHECKED_CAST")
      val iterator = start.attributes as Iterator<Attribute>
      while (iterator.hasNext()) {
        val attribute = iterator.next()
        val name = attribute.name.localPart
        val value = attribute.value
        if (endpoint.id == null && GraphMLConstants.ID_NAME == name) {
          endpoint.id = value
        }
        if (endpoint.port == null && GraphMLConstants.PORT_NAME == name) {
          endpoint.port = value
        }
        if (endpoint.node == null && GraphMLConstants.NODE_NAME == name) {
          endpoint.node = value
        }
        if (GraphMLConstants.TYPE_NAME == name) {
          val t = endpointTypeMap[value] ?: EndpointType.UNDIR
          endpoint.endpointType = t
        } else {
          endpoint.setProperty(name, value)
        }
      }

      // Make sure the node has been set.
      if (endpoint.node == null) {
        throw GraphIOException("Element 'endpoint' is missing attribute 'node'")
      }

      while (xmlEventReader.hasNext()) {
        val event = xmlEventReader.nextEvent()
        if (event.isStartElement) {
          val element = event as StartElement
          val name = element.name.localPart
          if (GraphMLConstants.DESC_NAME == name) {
            val desc = getParser(name).parse(xmlEventReader, element) as String
            endpoint.description = desc
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

      // Apply the keys to this object.
      applyKeys(endpoint)

      return endpoint
    } catch (e: Exception) {
      ExceptionConverter.convert(e)
    }

    return null
  }

  companion object {
    private val endpointTypeMap: Map<String, EndpointType> = mapOf(
      GraphMLConstants.IN_NAME to EndpointType.IN,
      GraphMLConstants.OUT_NAME to EndpointType.OUT,
      GraphMLConstants.UNDIR_NAME to EndpointType.UNDIR
    )
  }
}
