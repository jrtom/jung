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
package edu.uci.ics.jung.visualization.util;

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
public class ParallelEdgeIndexFunction<N, E> implements EdgeIndexFunction<N, E> {
  protected Map<E, Integer> edge_index = new HashMap<>();

  public int getIndex(Context<Network<N, E>, E> context) {
    Network<N, E> network = context.graph;
    E edge = context.element;
    Integer index = edge_index.get(edge);
    if (index == null) {
      EndpointPair<N> endpoints = network.incidentNodes(edge);
      N u = endpoints.nodeU();
      N v = endpoints.nodeV();
      int count = 0;
      for (E connectingEdge : network.edgesConnecting(u, v)) {
        edge_index.put(connectingEdge, count++);
      }
      return edge_index.get(edge);
    }
    return index;
  }

  public void reset(Context<Network<N, E>, E> context) {
    edge_index.remove(context.element);
  }

  public void reset() {
    edge_index.clear();
  }
}
