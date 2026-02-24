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
 * Metadata structure for the 'edge' GraphML element.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 * @see "http://graphml.graphdrawing.org/specification.html"
 */
class EdgeMetadata : AbstractMetadata() {

  var id: String? = null
  var isDirected: Boolean? = null
  var source: String? = null
  var target: String? = null
  var sourcePort: String? = null
  var targetPort: String? = null
  var description: String? = null
  var edge: Any? = null

  override val metadataType: Metadata.MetadataType
    get() = Metadata.MetadataType.EDGE
}
