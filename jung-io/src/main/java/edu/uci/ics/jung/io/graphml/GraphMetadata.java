/*
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */

package edu.uci.ics.jung.io.graphml;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;

/**
 * Metadata structure for the 'graph' GraphML element.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 * @see "http://graphml.graphdrawing.org/specification.html"
 */
public class GraphMetadata extends AbstractMetadata {

  public enum EdgeDefault {
    DIRECTED,
    UNDIRECTED
  }

  private String id;
  private EdgeDefault edgeDefault;
  private String description;
  private Object graph;
  private final Map<Object, NodeMetadata> nodes = new HashMap<Object, NodeMetadata>();
  private final Map<Object, EdgeMetadata> edges = new HashMap<Object, EdgeMetadata>();
  private final Map<Object, HyperEdgeMetadata> hyperEdges =
      new HashMap<Object, HyperEdgeMetadata>();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public EdgeDefault getEdgeDefault() {
    return edgeDefault;
  }

  public void setEdgeDefault(EdgeDefault edgeDefault) {
    this.edgeDefault = edgeDefault;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String desc) {
    this.description = desc;
  }

  public void addNodeMetadata(Object node, NodeMetadata metadata) {
    nodes.put(node, metadata);
  }

  public NodeMetadata getNodeMetadata(Object node) {
    return nodes.get(node);
  }

  public Map<Object, NodeMetadata> getNodeMap() {
    return nodes;
  }

  public void addEdgeMetadata(Object edge, EdgeMetadata metadata) {
    edges.put(edge, metadata);
  }

  public EdgeMetadata getEdgeMetadata(Object edge) {
    return edges.get(edge);
  }

  public Map<Object, EdgeMetadata> getEdgeMap() {
    return edges;
  }

  public void addHyperEdgeMetadata(Object edge, HyperEdgeMetadata metadata) {
    hyperEdges.put(edge, metadata);
  }

  public HyperEdgeMetadata getHyperEdgeMetadata(Object edge) {
    return hyperEdges.get(edge);
  }

  public Map<Object, HyperEdgeMetadata> getHyperEdgeMap() {
    return hyperEdges;
  }

  public Object getGraph() {
    return graph;
  }

  public void setGraph(Object graph) {
    this.graph = graph;
  }

  public MetadataType getMetadataType() {
    return MetadataType.GRAPH;
  }

  /**
   * Gets the property for the given node object.
   *
   * @param node the subject node
   * @param key the property key
   * @return the property value
   * @throws IllegalArgumentException thrown if there is no metadata associated with the provided
   *     node object.
   */
  public String getNodeProperty(Object node, String key) throws IllegalArgumentException {
    NodeMetadata metadata = Preconditions.checkNotNull(getNodeMetadata(node));

    return metadata.getProperty(key);
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
  public String getEdgeProperty(Object edge, String key) throws IllegalArgumentException {

    // First, try standard edges.
    EdgeMetadata em = getEdgeMetadata(edge);
    if (em != null) {
      return em.getProperty(key);
    }

    // Next, try hyperedges.
    HyperEdgeMetadata hem = getHyperEdgeMetadata(edge);
    if (hem != null) {
      return hem.getProperty(key);
    }

    // Couldn't find the edge.
    throw new IllegalArgumentException("Metadata does not exist for provided edge");
  }
}
