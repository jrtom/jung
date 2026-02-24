/*
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.io

/**
 * Exception thrown when IO errors occur when reading/writing graphs.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
class GraphIOException : Exception {
  /** Creates a new instance with no specified message or cause. */
  constructor() : super()

  /**
   * Creates a new instance with the specified message and cause.
   *
   * @param message a description of the exception-triggering event
   * @param cause the exception which triggered this one
   */
  constructor(message: String, cause: Throwable) : super(message, cause)

  /**
   * Creates a new instance with the specified message and no specified cause.
   *
   * @param message a description of the exception-triggering event
   */
  constructor(message: String) : super(message)

  /**
   * Creates a new instance with the specified cause and no specified message.
   *
   * @param cause the exception which triggered this one
   */
  constructor(cause: Throwable) : super(cause)
}
