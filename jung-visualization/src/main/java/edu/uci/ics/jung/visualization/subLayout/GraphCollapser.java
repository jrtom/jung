/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 23, 2005
 */
package edu.uci.ics.jung.visualization.subLayout;

import com.google.common.base.Preconditions;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Logger;

@SuppressWarnings({"rawtypes", "unchecked"})
public class GraphCollapser {

  private static final Logger logger = Logger.getLogger(GraphCollapser.class.getClass().getName());
  private Network originalGraph;
  private NetworkBuilder graphBuilder;

  public GraphCollapser(Network originalGraph) {
    this.originalGraph = originalGraph;
    this.graphBuilder = NetworkBuilder.from(originalGraph);
  }

  public Network collapse(Network inGraph, Network clusterGraph) {

    if (clusterGraph.nodes().size() < 2) {
      return inGraph;
    }

    MutableNetwork graph = graphBuilder.build();
    Collection cluster = clusterGraph.nodes();

    // add all vertices in the delegate, unless the vertex is in the
    // cluster.
    for (Object v : inGraph.nodes()) {
      if (cluster.contains(v) == false) {
        graph.addNode(v);
      }
    }
    // add the clusterGraph as a vertex
    graph.addNode(clusterGraph);

    // add all edges from the inGraph, unless both endpoints of
    // the edge are in the cluster
    for (Object e : inGraph.edges()) {
      EndpointPair endpoints = inGraph.incidentNodes(e);
      Object u = endpoints.nodeU();
      Object v = endpoints.nodeV();
      // only add edges whose endpoints are not both in the cluster
      if (cluster.contains(u) && cluster.contains(v)) {
        continue;
      }

      if (cluster.contains(u)) {
        graph.addEdge(clusterGraph, v, e);
      } else if (cluster.contains(v)) {
        graph.addEdge(u, clusterGraph, e);
      } else {
        graph.addEdge(u, v, e);
      }
    }
    return graph;
  }

  public Network expand(Network inGraph, Network clusterGraph) {
    MutableNetwork graph = graphBuilder.build();
    Collection clusterNodes = clusterGraph.nodes();
    logger.fine("cluster to expand is " + clusterNodes);

    // put all clusterGraph vertices and edges into the new Graph
    for (Object node : clusterNodes) {
      graph.addNode(node);
      for (Object edge : clusterGraph.incidentEdges(node)) {
        Object u = clusterGraph.incidentNodes(edge).nodeU();
        Object v = clusterGraph.incidentNodes(edge).nodeV();
        graph.addEdge(u, v, edge);
      }
    }
    // add all the vertices from the current graph except for
    // the cluster we are expanding
    for (Object node : inGraph.nodes()) {
      if (node.equals(clusterGraph) == false) {
        graph.addNode(node);
      }
    }

    // now that all vertices have been added, add the edges,
    // ensuring that no edge contains a vertex that has not
    // already been added
    for (Object node : inGraph.nodes()) {
      if (node.equals(clusterGraph) == false) {
        for (Object edge : inGraph.incidentEdges(node)) {
          Object u = inGraph.incidentNodes(edge).nodeU();
          Object v = inGraph.incidentNodes(edge).nodeV();
          // only add edges if both u and v are not already in the graph
          if (!(clusterNodes.contains(u) && clusterNodes.contains(v))) {
            continue;
          }

          if (clusterGraph.equals(u)) {
            Object originalU = originalGraph.incidentNodes(edge).nodeU();
            Object newU = findNode(graph, originalU);
            Preconditions.checkNotNull(newU);
            graph.addEdge(newU, v, edge);
          } else if (clusterGraph.equals(v)) {
            Object originalV = originalGraph.incidentNodes(edge).nodeV();
            Object newV = findNode(graph, originalV);
            Preconditions.checkNotNull(newV);
            graph.addEdge(u, newV, edge);
          } else {
            graph.addEdge(u, v, edge);
          }
        }
      }
    }
    return graph;
  }

  Object findNode(Network inGraph, Object inNode) {
    if (inGraph.nodes().contains(inNode)) {
      return inNode;
    }

    for (Object node : inGraph.nodes()) {
      if ((node instanceof Network) && contains((Network) node, inNode)) {
        return node;
      }
    }
    return null;
  }

  private boolean contains(Network inGraph, Object inNode) {
    boolean contained = false;
    if (inGraph.nodes().contains(inNode)) {
      return true;
    }

    for (Object node : inGraph.nodes()) {
      contained |= (node instanceof Network) && contains((Network) node, inNode);
    }
    return contained;
  }

  public Network getClusterGraph(Network inGraph, Collection picked) {
    MutableNetwork clusterGraph = graphBuilder.build();
    for (Object node : picked) {
      clusterGraph.addNode(node);
      Set edges = inGraph.incidentEdges(node);
      for (Object edge : edges) {
        Object u = inGraph.incidentNodes(edge).nodeU();
        Object v = inGraph.incidentNodes(edge).nodeV();
        if (picked.contains(u) && picked.contains(v)) {
          clusterGraph.addEdge(edge, u, v);
        }
      }
    }
    return clusterGraph;
  }
}
