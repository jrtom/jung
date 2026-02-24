/*
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.io.graphml

import edu.uci.ics.jung.io.GraphIOException
import javax.xml.stream.XMLStreamException

/**
 * Converts an exception to a GraphIOException. Runtime exceptions are checked for the cause. If
 * the cause is an XMLStreamException, it is converted to a GraphIOException. Otherwise, the
 * RuntimeException is rethrown.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
object ExceptionConverter {

  /**
   * Converts an exception to a GraphIOException. Runtime exceptions are checked for the cause.
   * If the cause is an XMLStreamException, it is converted to a GraphReaderException. Otherwise,
   * the RuntimeException is rethrown.
   *
   * @param e the exception to be converted
   * @throws GraphIOException the converted exception
   */
  @JvmStatic
  @Throws(GraphIOException::class)
  fun convert(e: Exception) {
    if (e is GraphIOException) {
      throw e
    }

    if (e is RuntimeException) {
      // If the cause was an XMLStreamException, throw a GraphReaderException
      if (e.cause is XMLStreamException) {
        throw GraphIOException(e.cause!!)
      }
      throw e
    }

    throw GraphIOException(e)
  }
}
