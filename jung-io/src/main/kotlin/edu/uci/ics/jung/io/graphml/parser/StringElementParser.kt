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
import javax.xml.stream.XMLEventReader
import javax.xml.stream.events.Characters
import javax.xml.stream.events.EndElement
import javax.xml.stream.events.StartElement

/**
 * Parses an element that just contains text.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
class StringElementParser<G : MutableNetwork<N, E>, N, E>(
  parserContext: ParserContext<G, N, E>
) : AbstractElementParser<G, N, E>(parserContext) {

  @Throws(GraphIOException::class)
  override fun parse(xmlEventReader: XMLEventReader, start: StartElement): String? {
    try {
      var str: String? = null

      while (xmlEventReader.hasNext()) {
        val event = xmlEventReader.nextEvent()
        if (event.isStartElement) {
          // Parse the unknown element.
          getUnknownParser().parse(xmlEventReader, event.asStartElement())
        } else if (event.isEndElement) {
          val end = event as EndElement
          verifyMatch(start, end)
          break
        } else if (event.isCharacters) {
          val characters = event as Characters
          str = characters.data
        }
      }

      return str
    } catch (e: Exception) {
      ExceptionConverter.convert(e)
    }

    return null
  }
}
