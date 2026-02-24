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
 * GraphML key object that was parsed from the input stream.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
class Key {

  /**
   * Enumeration for the 'for' type of this key. The for property indicates which elements (e.g.
   * graph, node, edge) this key applies to.
   */
  enum class ForType {
    ALL,
    GRAPH,
    NODE,
    EDGE,
    PORT,
    ENDPOINT
  }

  var id: String? = null
  var description: String? = null
  var attributeName: String? = null
  var attributeType: String? = null
  var defaultValue: String? = null
  var forType: ForType = ForType.ALL

  fun applyKey(metadata: Metadata) {
    val props = metadata.properties
    if (defaultValue != null && !props.containsKey(id)) {
      props[id!!] = defaultValue!!
    }
  }
}
