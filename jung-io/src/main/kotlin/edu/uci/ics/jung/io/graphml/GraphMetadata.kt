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

import com.google.common.base.Preconditions

/**
 * Metadata structure for the 'graph' GraphML element.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 * @see "http://graphml.graphdrawing.org/specification.html"
 */
class GraphMetadata : AbstractMetadata() {

  enum class EdgeDefault {
    DIRECTED,
    UNDIRECTED
  }

  var id: String? = null
  var edgeDefault: EdgeDefault? = null
  var description: String? = null
  var graph: Any? = null
  private val nodes: MutableMap<Any, NodeMetadata> = HashMap()
  private val edges: MutableMap<Any, EdgeMetadata> = HashMap()

  fun addNodeMetadata(node: Any, metadata: NodeMetadata) {
    nodes[node] = metadata
  }

  fun getNodeMetadata(node: Any): NodeMetadata? = nodes[node]

  val nodeMap: MutableMap<Any, NodeMetadata>
    get() = nodes

  fun addEdgeMetadata(edge: Any, metadata: EdgeMetadata) {
    edges[edge] = metadata
  }

  fun getEdgeMetadata(edge: Any): EdgeMetadata? = edges[edge]

  val edgeMap: MutableMap<Any, EdgeMetadata>
    get() = edges

  override val metadataType: Metadata.MetadataType
    get() = Metadata.MetadataType.GRAPH

  /**
   * Gets the property for the given node object.
   *
   * @param node the subject node
   * @param key the property key
   * @return the property value
   * @throws IllegalArgumentException thrown if there is no metadata associated with the provided
   *     node object.
   */
  @Throws(IllegalArgumentException::class)
  fun getNodeProperty(node: Any, key: String): String? {
    val metadata = Preconditions.checkNotNull(getNodeMetadata(node))
    return metadata.getProperty(key)
  }

  /**
   * Gets the property for the given edge object.
   *
   * @param edge the subject edge.
   * @param key the property key
   * @return the property value
   * @throws IllegalArgumentException thrown if there is no metadata associated with the provided
   *     edge object.
   */
  @Throws(IllegalArgumentException::class)
  fun getEdgeProperty(edge: Any, key: String): String? {
    // First, try standard edges.
    val em = getEdgeMetadata(edge)
    if (em != null) {
      return em.getProperty(key)
    }

    // Couldn't find the edge.
    throw IllegalArgumentException("Metadata does not exist for provided edge")
  }
}
