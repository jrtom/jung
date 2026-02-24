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
import javax.xml.stream.XMLEventReader
import javax.xml.stream.events.StartElement

/**
 * Interface for all element parsers. All parsers will be registered with the registry.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 * @see ElementParserRegistry
 */
interface ElementParser {
  @Throws(GraphIOException::class)
  fun parse(xmlEventReader: XMLEventReader, start: StartElement): Any?
}
