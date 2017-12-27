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
package edu.uci.ics.jung.io;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Network;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Writes graphs out in GraphML format.
 *
 * <p>Current known issues:
 *
 * <ul>
 *   <li>Only supports one graph per output file.
 *   <li>Does not indent lines for text-format readability.
 * </ul>
 */
public class GraphMLWriter<N, E> {
  protected Function<? super N, String> node_ids;
  protected Function<? super E, String> edge_ids;
  protected Map<String, GraphMLMetadata<Network<N, E>>> graph_data;
  protected Map<String, GraphMLMetadata<N>> node_data;
  protected Map<String, GraphMLMetadata<E>> edge_data;
  protected Function<? super N, String> node_desc;
  protected Function<? super E, String> edge_desc;
  protected Function<? super Network<N, E>, String> graph_desc;
  protected boolean directed;
  protected int nest_level;

  public GraphMLWriter() {
    node_ids = N::toString;
    edge_ids = e -> null;
    graph_data = Collections.emptyMap();
    node_data = Collections.emptyMap();
    edge_data = Collections.emptyMap();
    node_desc = n -> null;
    edge_desc = e -> null;
    graph_desc = g -> null;
    nest_level = 0;
  }

  /**
   * Writes {@code graph} out using {@code w}.
   *
   * @param g the graph to write out
   * @param w the writer instance to which the graph data will be written out
   * @throws IOException if writing the graph fails
   */
  public void save(Network<N, E> g, Writer w) throws IOException {
    BufferedWriter bw = new BufferedWriter(w);

    // write out boilerplate header
    bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    bw.write(
        "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns/graphml\"\n"
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  \n");
    bw.write("xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns/graphml\">\n");

    // write out data specifiers, including defaults
    for (String key : graph_data.keySet()) {
      writeKeySpecification(key, "graph", graph_data.get(key), bw);
    }
    for (String key : node_data.keySet()) {
      writeKeySpecification(key, "node", node_data.get(key), bw);
    }
    for (String key : edge_data.keySet()) {
      writeKeySpecification(key, "edge", edge_data.get(key), bw);
    }

    // write out graph-level information
    // set edge default direction
    bw.write("<graph edgedefault=\"");
    if (g.isDirected()) {
      bw.write("directed\">\n");
    } else {
      bw.write("undirected\">\n");
    }

    // write graph description, if any
    String desc = graph_desc.apply(g);
    if (desc != null) {
      bw.write("<desc>" + desc + "</desc>\n");
    }

    // write graph data out if any
    for (String key : graph_data.keySet()) {
      Function<Network<N, E>, ?> t = graph_data.get(key).transformer;
      Object value = t.apply(g);
      if (value != null) {
        bw.write(format("data", "key", key, value.toString()) + "\n");
      }
    }

    // write node information
    writeNodeData(g, bw);

    // write edge information
    writeEdgeData(g, bw);

    // close graph
    bw.write("</graph>\n");
    bw.write("</graphml>\n");
    bw.flush();

    bw.close();
  }

  protected void writeIndentedText(BufferedWriter w, String to_write) throws IOException {
    for (int i = 0; i < nest_level; i++) {
      w.write("  ");
    }
    w.write(to_write);
  }

  protected void writeNodeData(Network<N, E> graph, BufferedWriter w) throws IOException {
    for (N v : graph.nodes()) {
      String v_string = String.format("<node id=\"%s\"", node_ids.apply(v));
      boolean closed = false;
      // write description out if any
      String desc = node_desc.apply(v);
      if (desc != null) {
        w.write(v_string + ">\n");
        closed = true;
        w.write("<desc>" + desc + "</desc>\n");
      }
      // write data out if any
      for (String key : node_data.keySet()) {
        Function<N, ?> t = node_data.get(key).transformer;
        if (t != null) {
          Object value = t.apply(v);
          if (value != null) {
            if (!closed) {
              w.write(v_string + ">\n");
              closed = true;
            }
            w.write(format("data", "key", key, value.toString()) + "\n");
          }
        }
      }
      if (!closed) {
        w.write(v_string + "/>\n"); // no contents; close the node with "/>"
      } else {
        w.write("</node>\n");
      }
    }
  }

  protected void writeEdgeData(Network<N, E> g, Writer w) throws IOException {
    for (E e : g.edges()) {
      EndpointPair<N> endpoints = g.incidentNodes(e);
      String id = edge_ids.apply(e);
      String e_string;
      e_string = "<edge ";
      // add ID if present
      if (id != null) {
        e_string += "id=\"" + id + "\" ";
      }
      // add edge type if doesn't match default
      e_string +=
          "source=\""
              + node_ids.apply(endpoints.nodeU())
              + "\" target=\""
              + node_ids.apply(endpoints.nodeV())
              + "\"";

      boolean closed = false;
      // write description out if any
      String desc = edge_desc.apply(e);
      if (desc != null) {
        w.write(e_string + ">\n");
        closed = true;
        w.write("<desc>" + desc + "</desc>\n");
      }
      // write data out if any
      for (String key : edge_data.keySet()) {
        Function<E, ?> t = edge_data.get(key).transformer;
        Object value = t.apply(e);
        if (value != null) {
          if (!closed) {
            w.write(e_string + ">\n");
            closed = true;
          }
          w.write(format("data", "key", key, value.toString()) + "\n");
        }
      }

      if (!closed) {
        w.write(e_string + "/>\n"); // no contents; close the edge with "/>"
      } else {
        w.write("</edge>\n");
      }
    }
  }

  protected void writeKeySpecification(
      String key, String type, GraphMLMetadata<?> ds, BufferedWriter bw) throws IOException {
    bw.write("<key id=\"" + key + "\" for=\"" + type + "\"");
    boolean closed = false;
    // write out description if any
    String desc = ds.description;
    if (desc != null) {
      if (!closed) {
        bw.write(">\n");
        closed = true;
      }
      bw.write("<desc>" + desc + "</desc>\n");
    }
    // write out default if any
    Object def = ds.default_value;
    if (def != null) {
      if (!closed) {
        bw.write(">\n");
        closed = true;
      }
      bw.write("<default>" + def.toString() + "</default>\n");
    }
    if (!closed) {
      bw.write("/>\n");
    } else {
      bw.write("</key>\n");
    }
  }

  protected String format(String type, String attr, String value, String contents) {
    return String.format("<%s %s=\"%s\">%s</%s>", type, attr, value, contents, type);
  }

  /**
   * Provides an ID that will be used to identify a node in the output file. If the node IDs are not
   * set, the ID for each node will default to the output of <code>toString</code> (and thus not
   * guaranteed to be unique).
   *
   * @param node_ids a mapping from node to ID
   */
  public void setNodeIDs(Function<N, String> node_ids) {
    this.node_ids = node_ids;
  }

  /**
   * Provides an ID that will be used to identify an edge in the output file. If any edge ID is
   * missing, no ID will be written out for the corresponding edge.
   *
   * @param edge_ids a mapping from edge to ID
   */
  public void setEdgeIDs(Function<E, String> edge_ids) {
    this.edge_ids = edge_ids;
  }

  /**
   * Provides a map from data type name to graph data.
   *
   * @param graph_map map from data type name to graph data
   */
  public void setGraphData(Map<String, GraphMLMetadata<Network<N, E>>> graph_map) {
    graph_data = graph_map;
  }

  /**
   * Provides a map from data type name to node data.
   *
   * @param node_map map from data type name to node data
   */
  public void setNodeData(Map<String, GraphMLMetadata<N>> node_map) {
    node_data = node_map;
  }

  /**
   * Provides a map from data type name to edge data.
   *
   * @param edge_map map from data type name to edge data
   */
  public void setEdgeData(Map<String, GraphMLMetadata<E>> edge_map) {
    edge_data = edge_map;
  }

  /**
   * Adds a new graph data specification.
   *
   * @param id the ID of the data to add
   * @param description a description of the data to add
   * @param default_value a default value for the data type
   * @param graph_transformer a mapping from graphs to their string representations
   */
  public void addGraphData(
      String id,
      String description,
      String default_value,
      Function<Network<N, E>, String> graph_transformer) {
    if (graph_data.equals(Collections.EMPTY_MAP)) {
      graph_data = new HashMap<String, GraphMLMetadata<Network<N, E>>>();
    }
    graph_data.put(
        id, new GraphMLMetadata<Network<N, E>>(description, default_value, graph_transformer));
  }

  /**
   * Adds a new node data specification.
   *
   * @param id the ID of the data to add
   * @param description a description of the data to add
   * @param default_value a default value for the data type
   * @param node_transformer a mapping from nodes to their string representations
   */
  public void addNodeData(
      String id, String description, String default_value, Function<N, String> node_transformer) {
    if (node_data.equals(Collections.EMPTY_MAP)) {
      node_data = new HashMap<String, GraphMLMetadata<N>>();
    }
    node_data.put(id, new GraphMLMetadata<N>(description, default_value, node_transformer));
  }

  /**
   * Adds a new edge data specification.
   *
   * @param id the ID of the data to add
   * @param description a description of the data to add
   * @param default_value a default value for the data type
   * @param edge_transformer a mapping from edges to their string representations
   */
  public void addEdgeData(
      String id, String description, String default_value, Function<E, String> edge_transformer) {
    if (edge_data.equals(Collections.EMPTY_MAP)) {
      edge_data = new HashMap<String, GraphMLMetadata<E>>();
    }
    edge_data.put(id, new GraphMLMetadata<E>(description, default_value, edge_transformer));
  }

  /**
   * Provides node descriptions.
   *
   * @param node_desc a mapping from nodes to their descriptions
   */
  public void setNodeDescriptions(Function<N, String> node_desc) {
    this.node_desc = node_desc;
  }

  /**
   * Provides edge descriptions.
   *
   * @param edge_desc a mapping from edges to their descriptions
   */
  public void setEdgeDescriptions(Function<E, String> edge_desc) {
    this.edge_desc = edge_desc;
  }

  /**
   * Provides graph descriptions.
   *
   * @param graph_desc a mapping from graphs to their descriptions
   */
  public void setGraphDescriptions(Function<Network<N, E>, String> graph_desc) {
    this.graph_desc = graph_desc;
  }
}
