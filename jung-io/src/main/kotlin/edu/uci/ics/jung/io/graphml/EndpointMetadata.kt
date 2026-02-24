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
 * Metadata structure for the 'endpoint' GraphML element.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 * @see "http://graphml.graphdrawing.org/specification.html"
 */
class EndpointMetadata : AbstractMetadata() {

  enum class EndpointType {
    IN,
    OUT,
    UNDIR
  }

  var id: String? = null
  var port: String? = null
  var node: String? = null
  var description: String? = null
  var endpointType: EndpointType = EndpointType.UNDIR

  override val metadataType: Metadata.MetadataType
    get() = Metadata.MetadataType.ENDPOINT
}
