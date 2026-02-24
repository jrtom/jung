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
 * Metadata structure for the 'port' GraphML element.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 * @see "http://graphml.graphdrawing.org/specification.html"
 */
class PortMetadata : AbstractMetadata() {

  var name: String? = null
  var description: String? = null

  override val metadataType: Metadata.MetadataType
    get() = Metadata.MetadataType.PORT
}
