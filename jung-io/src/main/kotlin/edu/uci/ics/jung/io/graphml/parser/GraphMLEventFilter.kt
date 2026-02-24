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

import javax.xml.stream.EventFilter
import javax.xml.stream.XMLStreamConstants
import javax.xml.stream.events.XMLEvent

/**
 * Filter to ignore unsupported XML events.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
class GraphMLEventFilter : EventFilter {

  override fun accept(event: XMLEvent): Boolean {
    return when (event.eventType) {
      XMLStreamConstants.START_ELEMENT,
      XMLStreamConstants.END_ELEMENT,
      XMLStreamConstants.CHARACTERS,
      XMLStreamConstants.ATTRIBUTE,
      XMLStreamConstants.NAMESPACE,
      XMLStreamConstants.START_DOCUMENT,
      XMLStreamConstants.END_DOCUMENT -> true
      else -> false
    }
  }
}
