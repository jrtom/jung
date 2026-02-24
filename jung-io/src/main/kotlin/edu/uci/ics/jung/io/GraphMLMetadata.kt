/**
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description. Created on Jun 30, 2008
 */
package edu.uci.ics.jung.io

import java.util.function.Function

/**
 * Maintains information relating to data for the specified type. This includes a Function from
 * objects to their values, a default value, and a description.
 *
 * @param description a textual description of the object
 * @param default_value the default value for the object, as a String
 * @param transformer maps objects of this type to string representations
 */
class GraphMLMetadata<T>(
  /** The description of this data type. */
  @JvmField var description: String?,

  /** The default value for objects of this type. */
  @JvmField var default_value: String?,

  /** A Function mapping objects to string representations of their values. */
  @JvmField var transformer: Function<T, String>
)
