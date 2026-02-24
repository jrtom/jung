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

/** Maintains all the metadata read in from a single GraphML XML document. */
class GraphMLDocument {

  val keyMap: KeyMap = KeyMap()
  val graphMetadata: MutableList<GraphMetadata> = ArrayList()

  fun clear() {
    graphMetadata.clear()
    keyMap.clear()
  }
}
