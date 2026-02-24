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

/**
 * Abstract base class for metadata - implements the property functionality
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
abstract class AbstractMetadata : Metadata {

  override val properties: MutableMap<String, String> = HashMap()

  fun getProperty(key: String): String? = properties[key]

  fun setProperty(key: String, value: String): String? = properties.put(key, value)

  fun addData(data: DataMetadata) {
    properties[data.key!!] = data.value!!
  }
}
