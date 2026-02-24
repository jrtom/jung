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
import edu.uci.ics.jung.io.graphml.Metadata
import javax.xml.stream.events.EndElement
import javax.xml.stream.events.StartElement

/**
 * Base class for element parsers - provides some minimal functionality.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
abstract class AbstractElementParser<G : MutableNetwork<N, E>, N, E>(
  val parserContext: ParserContext<G, N, E>
) : ElementParser {

  fun getParser(localName: String): ElementParser =
    parserContext.elementParserRegistry.getParser(localName)

  fun applyKeys(metadata: Metadata) {
    parserContext.keyMap.applyKeys(metadata)
  }

  fun getUnknownParser(): ElementParser =
    parserContext.elementParserRegistry.unknownElementParser

  @Throws(GraphIOException::class)
  protected fun verifyMatch(start: StartElement, end: EndElement) {
    val startName = start.name.localPart
    val endName = end.name.localPart
    if (startName != endName) {
      throw GraphIOException(
        "Failed parsing document: Start/end tag mismatch! " +
          "StartTag:" + startName + ", EndTag: " + endName
      )
    }
  }
}
