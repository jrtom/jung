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
import edu.uci.ics.jung.io.graphml.ExceptionConverter
import java.util.ArrayDeque
import javax.xml.stream.XMLEventReader
import javax.xml.stream.events.StartElement

/**
 * Skips an entire unknown subtree of the XML
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
class UnknownElementParser : ElementParser {

  /**
   * Skips an entire subtree starting with the provided unknown element.
   *
   * @param xmlEventReader the event reader
   * @param start the unknown element to be skipped.
   * @return null
   */
  @Throws(GraphIOException::class)
  override fun parse(xmlEventReader: XMLEventReader, start: StartElement): Any? {
    try {
      val skippedElements = ArrayDeque<String>()
      skippedElements.add(start.name.localPart)

      while (xmlEventReader.hasNext()) {
        val event = xmlEventReader.nextEvent()
        if (event.isStartElement) {
          val name = event.asStartElement().name.localPart
          // Push the name of the unknown element.
          skippedElements.push(name)
        }
        if (event.isEndElement) {
          val name = event.asEndElement().name.localPart

          if (skippedElements.size == 0 || skippedElements.peek() != name) {
            throw GraphIOException(
              "Failed parsing GraphML document - startTag/endTag mismatch"
            )
          }

          // Pop the stack.
          skippedElements.pop()
          if (skippedElements.isEmpty()) {
            break
          }
        }
      }

      return null
    } catch (e: Exception) {
      ExceptionConverter.convert(e)
    }

    return null
  }
}
