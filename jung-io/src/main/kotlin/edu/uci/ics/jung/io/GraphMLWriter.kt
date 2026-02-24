/*
 * Created on June 16, 2008
 *
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.io

import com.google.common.graph.Network
import java.io.BufferedWriter
import java.io.IOException
import java.io.Writer
import java.util.Collections
import java.util.HashMap
import java.util.function.Function

/**
 * Writes graphs out in GraphML format.
 *
 * Current known issues:
 * - Only supports one graph per output file.
 * - Does not indent lines for text-format readability.
 */
open class GraphMLWriter<N, E> {
  internal var node_ids: Function<in N, String> = Function { it.toString() }
  internal var edge_ids: Function<in E, String?> = Function { null }
  internal var graph_data: Map<String, GraphMLMetadata<Network<N, E>>> = Collections.emptyMap()
  internal var node_data: Map<String, GraphMLMetadata<N>> = Collections.emptyMap()
  internal var edge_data: Map<String, GraphMLMetadata<E>> = Collections.emptyMap()
  internal var node_desc: Function<in N, String?> = Function { null }
  internal var edge_desc: Function<in E, String?> = Function { null }
  internal var graph_desc: Function<in Network<N, E>, String?> = Function { null }
  internal var directed: Boolean = false
  internal var nest_level: Int = 0

  /**
   * Writes [graph] out using [w].
   *
   * @param g the graph to write out
   * @param w the writer instance to which the graph data will be written out
   * @throws IOException if writing the graph fails
   */
  @Throws(IOException::class)
  fun save(g: Network<N, E>, w: Writer) {
    val bw = BufferedWriter(w)

    // write out boilerplate header
    bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
    bw.write(
      "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns/graphml\"\n" +
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  \n"
    )
    bw.write("xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns/graphml\">\n")

    // write out data specifiers, including defaults
    for (key in graph_data.keys) {
      writeKeySpecification(key, "graph", graph_data[key]!!, bw)
    }
    for (key in node_data.keys) {
      writeKeySpecification(key, "node", node_data[key]!!, bw)
    }
    for (key in edge_data.keys) {
      writeKeySpecification(key, "edge", edge_data[key]!!, bw)
    }

    // write out graph-level information
    // set edge default direction
    bw.write("<graph edgedefault=\"")
    if (g.isDirected) {
      bw.write("directed\">\n")
    } else {
      bw.write("undirected\">\n")
    }

    // write graph description, if any
    val desc = graph_desc.apply(g)
    if (desc != null) {
      bw.write("<desc>$desc</desc>\n")
    }

    // write graph data out if any
    for (key in graph_data.keys) {
      val t = graph_data[key]!!.transformer
      val value = t.apply(g)
      if (value != null) {
        bw.write(format("data", "key", key, value.toString()) + "\n")
      }
    }

    // write node information
    writeNodeData(g, bw)

    // write edge information
    writeEdgeData(g, bw)

    // close graph
    bw.write("</graph>\n")
    bw.write("</graphml>\n")
    bw.flush()

    bw.close()
  }

  @Throws(IOException::class)
  internal open fun writeIndentedText(w: BufferedWriter, to_write: String) {
    for (i in 0 until nest_level) {
      w.write("  ")
    }
    w.write(to_write)
  }

  @Throws(IOException::class)
  internal open fun writeNodeData(graph: Network<N, E>, w: BufferedWriter) {
    for (v in graph.nodes()) {
      val v_string = String.format("<node id=\"%s\"", node_ids.apply(v))
      var closed = false
      // write description out if any
      val desc = node_desc.apply(v)
      if (desc != null) {
        w.write(v_string + ">\n")
        closed = true
        w.write("<desc>$desc</desc>\n")
      }
      // write data out if any
      for (key in node_data.keys) {
        val t = node_data[key]!!.transformer
        if (t != null) {
          val value = t.apply(v)
          if (value != null) {
            if (!closed) {
              w.write(v_string + ">\n")
              closed = true
            }
            w.write(format("data", "key", key, value.toString()) + "\n")
          }
        }
      }
      if (!closed) {
        w.write(v_string + "/>\n") // no contents; close the node with "/>"
      } else {
        w.write("</node>\n")
      }
    }
  }

  @Throws(IOException::class)
  internal open fun writeEdgeData(g: Network<N, E>, w: Writer) {
    for (e in g.edges()) {
      val endpoints = g.incidentNodes(e)
      val id = edge_ids.apply(e)
      var e_string = "<edge "
      // add ID if present
      if (id != null) {
        e_string += "id=\"$id\" "
      }
      // add edge type if doesn't match default
      e_string += "source=\"${node_ids.apply(endpoints.nodeU())}\" target=\"${node_ids.apply(endpoints.nodeV())}\""

      var closed = false
      // write description out if any
      val desc = edge_desc.apply(e)
      if (desc != null) {
        w.write(e_string + ">\n")
        closed = true
        w.write("<desc>$desc</desc>\n")
      }
      // write data out if any
      for (key in edge_data.keys) {
        val t = edge_data[key]!!.transformer
        val value = t.apply(e)
        if (value != null) {
          if (!closed) {
            w.write(e_string + ">\n")
            closed = true
          }
          w.write(format("data", "key", key, value.toString()) + "\n")
        }
      }

      if (!closed) {
        w.write(e_string + "/>\n") // no contents; close the edge with "/>"
      } else {
        w.write("</edge>\n")
      }
    }
  }

  @Throws(IOException::class)
  internal open fun writeKeySpecification(
    key: String,
    type: String,
    ds: GraphMLMetadata<*>,
    bw: BufferedWriter
  ) {
    bw.write("<key id=\"$key\" for=\"$type\"")
    var closed = false
    // write out description if any
    val desc = ds.description
    if (desc != null) {
      if (!closed) {
        bw.write(">\n")
        closed = true
      }
      bw.write("<desc>$desc</desc>\n")
    }
    // write out default if any
    val def = ds.default_value
    if (def != null) {
      if (!closed) {
        bw.write(">\n")
        closed = true
      }
      bw.write("<default>$def</default>\n")
    }
    if (!closed) {
      bw.write("/>\n")
    } else {
      bw.write("</key>\n")
    }
  }

  internal open fun format(type: String, attr: String, value: String, contents: String): String {
    return String.format("<%s %s=\"%s\">%s</%s>", type, attr, value, contents, type)
  }

  /**
   * Provides an ID that will be used to identify a node in the output file. If the node IDs are not
   * set, the ID for each node will default to the output of `toString` (and thus not
   * guaranteed to be unique).
   *
   * @param node_ids a mapping from node to ID
   */
  fun setNodeIDs(node_ids: Function<N, String>) {
    this.node_ids = node_ids
  }

  /**
   * Provides an ID that will be used to identify an edge in the output file. If any edge ID is
   * missing, no ID will be written out for the corresponding edge.
   *
   * @param edge_ids a mapping from edge to ID
   */
  fun setEdgeIDs(edge_ids: Function<in E, String?>) {
    this.edge_ids = edge_ids
  }

  /**
   * Provides a map from data type name to graph data.
   *
   * @param graph_map map from data type name to graph data
   */
  fun setGraphData(graph_map: Map<String, GraphMLMetadata<Network<N, E>>>) {
    graph_data = graph_map
  }

  /**
   * Provides a map from data type name to node data.
   *
   * @param node_map map from data type name to node data
   */
  fun setNodeData(node_map: Map<String, GraphMLMetadata<N>>) {
    node_data = node_map
  }

  /**
   * Provides a map from data type name to edge data.
   *
   * @param edge_map map from data type name to edge data
   */
  fun setEdgeData(edge_map: Map<String, GraphMLMetadata<E>>) {
    edge_data = edge_map
  }

  /**
   * Adds a new graph data specification.
   *
   * @param id the ID of the data to add
   * @param description a description of the data to add
   * @param default_value a default value for the data type
   * @param graph_transformer a mapping from graphs to their string representations
   */
  fun addGraphData(
    id: String,
    description: String?,
    default_value: String?,
    graph_transformer: Function<Network<N, E>, String>
  ) {
    if (graph_data == Collections.EMPTY_MAP) {
      graph_data = HashMap<String, GraphMLMetadata<Network<N, E>>>()
    }
    (graph_data as MutableMap)[id] = GraphMLMetadata(description, default_value, graph_transformer)
  }

  /**
   * Adds a new node data specification.
   *
   * @param id the ID of the data to add
   * @param description a description of the data to add
   * @param default_value a default value for the data type
   * @param node_transformer a mapping from nodes to their string representations
   */
  fun addNodeData(
    id: String,
    description: String?,
    default_value: String?,
    node_transformer: Function<N, String>
  ) {
    if (node_data == Collections.EMPTY_MAP) {
      node_data = HashMap<String, GraphMLMetadata<N>>()
    }
    (node_data as MutableMap)[id] = GraphMLMetadata(description, default_value, node_transformer)
  }

  /**
   * Adds a new edge data specification.
   *
   * @param id the ID of the data to add
   * @param description a description of the data to add
   * @param default_value a default value for the data type
   * @param edge_transformer a mapping from edges to their string representations
   */
  fun addEdgeData(
    id: String,
    description: String?,
    default_value: String?,
    edge_transformer: Function<E, String>
  ) {
    if (edge_data == Collections.EMPTY_MAP) {
      edge_data = HashMap<String, GraphMLMetadata<E>>()
    }
    (edge_data as MutableMap)[id] = GraphMLMetadata(description, default_value, edge_transformer)
  }

  /**
   * Provides node descriptions.
   *
   * @param node_desc a mapping from nodes to their descriptions
   */
  fun setNodeDescriptions(node_desc: Function<in N, String?>) {
    this.node_desc = node_desc
  }

  /**
   * Provides edge descriptions.
   *
   * @param edge_desc a mapping from edges to their descriptions
   */
  fun setEdgeDescriptions(edge_desc: Function<in E, String?>) {
    this.edge_desc = edge_desc
  }

  /**
   * Provides graph descriptions.
   *
   * @param graph_desc a mapping from graphs to their descriptions
   */
  fun setGraphDescriptions(graph_desc: Function<in Network<N, E>, String?>) {
    this.graph_desc = graph_desc
  }
}
