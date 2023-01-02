/*
 * Created on Sep 24, 2005
 *
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.graph.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Network;
import java.util.HashMap;
import java.util.Map;

/**
 * A class which creates and maintains indices for parallel edges. Parallel edges are defined here
 * to be the collection of edges that are returned by <code>graph.edgesConnecting(v, w)</code> for
 * some <code>v</code> and <code>w</code>.
 *
 * <p>At this time, users are responsible for resetting the indices (by calling <code>reset()</code>
 * ) if changes to the graph make it appropriate.
 *
 * @author Joshua O'Madadhain
 * @author Tom Nelson
 */
public class ParallelEdgeIndexFunction<N, E> implements EdgeIndexFunction<E> {
  protected Map<E, Integer> edgeIndex = new HashMap<E, Integer>();
  protected Network<N, E> graph;

  /**
   * @param graph the graph for which this index function is defined
   */
  public ParallelEdgeIndexFunction(Network<N, E> graph) {
    this.graph = checkNotNull(graph, "graph");
  }

  public int getIndex(E edge) {
    checkNotNull(edge, "edge");
    Integer index = edgeIndex.get(edge);
    if (index == null) {
      EndpointPair<N> endpoints = graph.incidentNodes(edge);
      N u = endpoints.nodeU();
      N v = endpoints.nodeV();
      int count = 0;
      for (E connectingEdge : graph.edgesConnecting(u, v)) {
        edgeIndex.put(connectingEdge, count++);
      }
      return edgeIndex.get(edge);
    }
    return index;
  }

  public void reset(E edge) {
    edgeIndex.remove(checkNotNull(edge, "edge"));
  }

  public void reset() {
    edgeIndex.clear();
  }
}
