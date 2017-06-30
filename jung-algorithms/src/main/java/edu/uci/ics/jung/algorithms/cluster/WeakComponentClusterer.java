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

import com.google.common.base.Function;
import edu.uci.ics.jung.graph.Graph;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Finds all weak components in a graph as sets of vertex sets. A weak component is defined as a
 * maximal subgraph in which all pairs of vertices in the subgraph are reachable from one another in
 * the underlying undirected subgraph.
 *
 * <p>This implementation identifies components as sets of vertex sets. To create the induced graphs
 * from any or all of these vertex sets, see <code>algorithms.filters.FilterUtils</code>.
 *
 * <p>Running time: O(|V| + |E|) where |V| is the number of vertices and |E| is the number of edges.
 *
 * @author Scott White
 */
public class WeakComponentClusterer<V, E> implements Function<Graph<V, E>, Set<Set<V>>> {
  /**
   * Extracts the weak components from a graph.
   *
   * @param graph the graph whose weak components are to be extracted
   * @return the list of weak components
   */
  public Set<Set<V>> apply(Graph<V, E> graph) {

    Set<Set<V>> clusterSet = new HashSet<Set<V>>();

    HashSet<V> unvisitedVertices = new HashSet<V>(graph.getVertices());

    while (!unvisitedVertices.isEmpty()) {
      Set<V> cluster = new HashSet<V>();
      V root = unvisitedVertices.iterator().next();
      unvisitedVertices.remove(root);
      cluster.add(root);

      Queue<V> queue = new LinkedList<V>();
      queue.add(root);

      while (!queue.isEmpty()) {
        V currentVertex = queue.remove();
        Collection<V> neighbors = graph.getNeighbors(currentVertex);

        for (V neighbor : neighbors) {
          if (unvisitedVertices.contains(neighbor)) {
            queue.add(neighbor);
            unvisitedVertices.remove(neighbor);
            cluster.add(neighbor);
          }
        }
      }
      clusterSet.add(cluster);
    }
    return clusterSet;
  }
}
