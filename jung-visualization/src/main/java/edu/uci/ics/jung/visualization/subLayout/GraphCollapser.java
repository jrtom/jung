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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"rawtypes", "unchecked"})
public class GraphCollapser {

  private static final Logger logger = LoggerFactory.getLogger(GraphCollapser.class);
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

    // add all nodes in the delegate, unless the node is in the
    // cluster.
    for (Object v : inGraph.nodes()) {
      if (cluster.contains(v) == false) {
        graph.addNode(v);
      }
    }
    // add the clusterGraph as a node
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

  public Network expand(Network originalNetwork, Network inGraph, Network clusterGraphNode) {

    NetworkBuilder networkBuilder = NetworkBuilder.from(originalNetwork);
    // build a new empty network
    MutableNetwork newGraph = networkBuilder.build();
    // add all the nodes from inGraph that are not clusterGraphNode and are not in clusterGraphNode
    for (Object node : inGraph.nodes()) {
      if (!node.equals(clusterGraphNode) && !this.contains(clusterGraphNode, node)) {
        newGraph.addNode(node);
      }
    }

    // add all edges that don't have an endpoint that either is clusterGraphNode or is in
    // clusterGraphNode
    for (Object edge : inGraph.edges()) {
      EndpointPair endpoints = inGraph.incidentNodes(edge);
      boolean dontWantThis = false;
      for (Object endpoint : endpoints) {
        dontWantThis |=
            endpoint.equals(clusterGraphNode) || this.contains(clusterGraphNode, endpoint);
      }
      if (dontWantThis == false) {
        newGraph.addEdge(endpoints.nodeU(), endpoints.nodeV(), edge);
      }
    }

    // add all the nodes from the clusterGraphNode
    for (Object node : clusterGraphNode.nodes()) {
      newGraph.addNode(node);
    }

    // add all the edges that are in the clusterGraphNode
    for (Object edge : clusterGraphNode.edges()) {
      EndpointPair endpoints = clusterGraphNode.incidentNodes(edge);
      newGraph.addEdge(endpoints.nodeU(), endpoints.nodeV(), edge);
    }

    // add edges from ingraph where one endpoint is the clusterGraphNode
    // it will now be connected to the node that was expanded from clusterGraphNode
    for (Object edge : inGraph.edges()) {
      Set endpointsFromCollapsedGraph = Sets.newHashSet(inGraph.incidentNodes(edge));
      for (Object endpoint : inGraph.incidentNodes(edge)) {
        if (endpoint.equals(clusterGraphNode)) {
          // get the endpoints for this edge from the original graph
          Set endpointsFromOriginalGraph = Sets.newHashSet(originalNetwork.incidentNodes(edge));
          // remove the endpoint that is the cluster i am expanding
          endpointsFromCollapsedGraph.remove(endpoint);
          // put in the one that is in the collapsedGraphNode i am expanding
          for (Object originalEndpoint : endpointsFromOriginalGraph) {
            if (this.contains(clusterGraphNode, originalEndpoint)) {
              endpointsFromCollapsedGraph.add(originalEndpoint);
              break;
            }
          }
          List list = Lists.newArrayList(endpointsFromCollapsedGraph);
          newGraph.addEdge(list.get(0), list.get(1), edge);
        }
      }
    }
    return newGraph;
  }

  /**
   * @param inGraph
   * @param inNode
   * @return
   */
  public Object findNode(Network inGraph, Object inNode) {
    /** if the inNode is in the inGraph, return the inNode */
    if (inGraph.nodes().contains(inNode)) {
      return inNode;
    }

    /**
     * if the inNode is part of a node that is a Network, return the Network that contains inNode
     */
    for (Object node : inGraph.nodes()) {
      if ((node instanceof Network) && contains((Network) node, inNode)) {
        // return the node that is a Network containing inNode
        return node;
      }
    }
    return null;
  }

  Object findOriginalNode(Network inGraph, Object inNode, Network clusterGraph) {
    if (inGraph.nodes().contains(inNode)) {
      return inNode;
    }

    for (Object node : inGraph.nodes()) {
      if ((node instanceof Network) && !node.equals(clusterGraph)) {
        return node;
      }
      if ((node instanceof Network) && contains((Network) node, inNode)) {
        return node;
      }
    }
    return null;
  }

  public boolean contains(Network inGraph, Object inNode) {
    boolean contained = false;
    if (inGraph.nodes().contains(inNode)) {
      // inNode is one of the nodes in inGraph
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
          clusterGraph.addEdge(u, v, edge);
        }
      }
    }
    return clusterGraph;
  }
}
