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
 * Interface for any GraphML metadata.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
interface Metadata {

  /** Metadata type enumeration */
  enum class MetadataType {
    GRAPH,
    NODE,
    EDGE,
    PORT,
    ENDPOINT
  }

  /**
   * Gets the metadata type of this object.
   *
   * @return the metadata type
   */
  val metadataType: MetadataType

  /**
   * Gets any properties that were associated with this metadata in the GraphML
   *
   * @return GraphML properties
   */
  val properties: MutableMap<String, String>
}
