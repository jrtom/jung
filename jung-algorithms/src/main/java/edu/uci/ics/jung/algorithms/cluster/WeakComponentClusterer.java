/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.cluster;

import com.google.common.graph.Network;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

/**
 * Finds all weak components in a graph as sets of node sets. A weak component is defined as a
 * maximal subgraph in which all pairs of nodes in the subgraph are reachable from one another in
 * the underlying undirected subgraph.
 *
 * <p>This implementation identifies components as sets of node sets. To create the induced graphs
 * from any or all of these node sets, see <code>algorithms.filters.FilterUtils</code>.
 *
 * <p>Running time: O(|V| + |E|) where |V| is the number of nodes and |E| is the number of edges.
 *
 * @author Scott White
 */
public class WeakComponentClusterer<V, E> implements Function<Network<V, E>, Set<Set<V>>> {
  /**
   * Extracts the weak components from a graph.
   *
   * @param graph the graph whose weak components are to be extracted
   * @return the list of weak components
   */
  public Set<Set<V>> apply(Network<V, E> graph) {

    Set<Set<V>> clusterSet = new HashSet<Set<V>>();

    HashSet<V> unvisitedNodes = new HashSet<V>(graph.nodes());

    while (!unvisitedNodes.isEmpty()) {
      Set<V> cluster = new HashSet<V>();
      V root = unvisitedNodes.iterator().next();
      unvisitedNodes.remove(root);
      cluster.add(root);

      Queue<V> queue = new LinkedList<V>();
      queue.add(root);

      while (!queue.isEmpty()) {
        V currentNode = queue.remove();
        Collection<V> neighbors = graph.adjacentNodes(currentNode);

        for (V neighbor : neighbors) {
          if (unvisitedNodes.contains(neighbor)) {
            queue.add(neighbor);
            unvisitedNodes.remove(neighbor);
            cluster.add(neighbor);
          }
        }
      }
      clusterSet.add(cluster);
    }
    return clusterSet;
  }
}
