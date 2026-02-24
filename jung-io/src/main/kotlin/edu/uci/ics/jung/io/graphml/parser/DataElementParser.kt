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
import javax.xml.stream.XMLEventReader
import javax.xml.stream.events.Attribute
import javax.xml.stream.events.Characters
import javax.xml.stream.events.EndElement
import javax.xml.stream.events.StartElement

/**
 * Parses the data element.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
class DataElementParser<G : MutableNetwork<N, E>, N, E>(
  parserContext: ParserContext<G, N, E>
) : AbstractElementParser<G, N, E>(parserContext) {

  @Throws(GraphIOException::class)
  override fun parse(xmlEventReader: XMLEventReader, start: StartElement): DataMetadata? {
    try {
      // Create the new data.
      val data = DataMetadata()

      // Parse the attributes.
      @Suppress("UNCHECKED_CAST")
      val iterator = start.attributes as Iterator<Attribute>
      while (iterator.hasNext()) {
        val attribute = iterator.next()
        val name = attribute.name.localPart
        val value = attribute.value
        if (data.key == null && GraphMLConstants.KEY_NAME == name) {
          data.key = value
        }
      }

      // Make sure the key has been set.
      if (data.key == null) {
        throw GraphIOException("Element 'data' is missing attribute 'key'")
      }

      while (xmlEventReader.hasNext()) {
        val event = xmlEventReader.nextEvent()
        if (event.isStartElement) {
          val element = event as StartElement
          // Treat any child elements as unknown
          getUnknownParser().parse(xmlEventReader, element)
        }
        if (event.isCharacters) {
          val characters = event as Characters
          data.value = characters.data
        }
        if (event.isEndElement) {
          val end = event as EndElement
          verifyMatch(start, end)
          break
        }
      }

      return data
    } catch (e: Exception) {
      ExceptionConverter.convert(e)
    }

    return null
  }
}
