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
import edu.uci.ics.jung.io.graphml.EdgeMetadata
import edu.uci.ics.jung.io.graphml.ExceptionConverter
import edu.uci.ics.jung.io.graphml.GraphMLConstants
import javax.xml.stream.XMLEventReader
import javax.xml.stream.events.Attribute
import javax.xml.stream.events.EndElement
import javax.xml.stream.events.StartElement

/**
 * Parses an edge element.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
class EdgeElementParser<G : MutableNetwork<N, E>, N, E>(
  parserContext: ParserContext<G, N, E>
) : AbstractElementParser<G, N, E>(parserContext) {

  @Throws(GraphIOException::class)
  override fun parse(xmlEventReader: XMLEventReader, start: StartElement): EdgeMetadata? {
    try {
      // Create the new edge.
      val edge = EdgeMetadata()

      // Parse the attributes.
      @Suppress("UNCHECKED_CAST")
      val iterator = start.attributes as Iterator<Attribute>
      while (iterator.hasNext()) {
        val attribute = iterator.next()
        val name = attribute.name.localPart
        val value = attribute.value
        if (edge.id == null && GraphMLConstants.ID_NAME == name) {
          edge.id = value
        } else if (edge.isDirected == null && GraphMLConstants.DIRECTED_NAME == name) {
          edge.isDirected = "true" == value
        } else if (edge.source == null && GraphMLConstants.SOURCE_NAME == name) {
          edge.source = value
        } else if (edge.target == null && GraphMLConstants.TARGET_NAME == name) {
          edge.target = value
        } else if (edge.sourcePort == null && GraphMLConstants.SOURCEPORT_NAME == name) {
          edge.sourcePort = value
        } else if (edge.targetPort == null && GraphMLConstants.TARGETPORT_NAME == name) {
          edge.targetPort = value
        } else {
          edge.setProperty(name, value)
        }
      }

      // Make sure the source and target have been set.
      if (edge.source == null || edge.target == null) {
        throw GraphIOException("Element 'edge' is missing attribute 'source' or 'target'")
      }

      while (xmlEventReader.hasNext()) {
        val event = xmlEventReader.nextEvent()
        if (event.isStartElement) {
          val element = event as StartElement
          val name = element.name.localPart
          if (GraphMLConstants.DESC_NAME == name) {
            val desc = getParser(name).parse(xmlEventReader, element) as String
            edge.description = desc
          } else if (GraphMLConstants.DATA_NAME == name) {
            val data = getParser(name).parse(xmlEventReader, element) as DataMetadata
            edge.addData(data)
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
      applyKeys(edge)

      return edge
    } catch (e: Exception) {
      ExceptionConverter.convert(e)
    }

    return null
  }
}
