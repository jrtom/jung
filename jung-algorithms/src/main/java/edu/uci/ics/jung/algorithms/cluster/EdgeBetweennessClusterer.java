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

import com.google.common.base.Preconditions;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * An algorithm for computing clusters (community structure) in graphs based on edge betweenness.
 * The betweenness of an edge is defined as the extent to which that edge lies along shortest paths
 * between all pairs of nodes.
 *
 * <p>This algorithm works by iteratively following the 2 step process:
 *
 * <ul>
 *   <li>Compute edge betweenness for all edges in current graph
 *   <li>Remove edge with highest betweenness
 * </ul>
 *
 * <p>Running time is: O(kmn) where k is the number of edges to remove, m is the total number of
 * edges, and n is the total number of nodes. For very sparse graphs the running time is closer to
 * O(kn^2) and for graphs with strong community structure, the complexity is even lower.
 *
 * <p>This algorithm is a slight modification of the algorithm discussed below in that the number of
 * edges to be removed is parameterized.
 *
 * @author Scott White
 * @author Tom Nelson (converted to jung2)
 * @author Joshua O'Madadhain (converted to common.graph)
 * @see "Community structure in social and biological networks by Michelle Girvan and Mark Newman"
 */
public class EdgeBetweennessClusterer<N, E> implements Function<Network<N, E>, Set<Set<N>>> {
  private final int mNumEdgesToRemove;
  private LinkedHashSet<E> edgesRemoved;

  /**
   * Constructs a new clusterer for the specified graph.
   *
   * @param numEdgesToRemove the number of edges to be progressively removed from the graph
   */
  public EdgeBetweennessClusterer(int numEdgesToRemove) {
    Preconditions.checkArgument(
        numEdgesToRemove >= 0, "Number of edges to remove must be positive");
    mNumEdgesToRemove = numEdgesToRemove;
    edgesRemoved = new LinkedHashSet<>(mNumEdgesToRemove);
  }

  /**
   * Finds the set of clusters which have the strongest "community structure". The more edges
   * removed the smaller and more cohesive the clusters.
   *
   * @param graph the graph
   */
  public Set<Set<N>> apply(Network<N, E> graph) {
    Preconditions.checkArgument(
        mNumEdgesToRemove <= graph.edges().size(),
        "Number of edges to remove must be <= the number of edges in the graph");
    // TODO(jrtom): is there something smarter that we can do if we're removing
    // (almost) all the edges in the graph?
    MutableNetwork<N, E> filtered = Graphs.copyOf(graph);
    edgesRemoved.clear();

    for (int k = 0; k < mNumEdgesToRemove; k++) {
      BetweennessCentrality<N, E> bc = new BetweennessCentrality<N, E>(filtered);
      E to_remove = null;
      double score = 0;
      for (E e : filtered.edges()) {
        if (bc.getEdgeScore(e) > score) {
          to_remove = e;
          score = bc.getEdgeScore(e);
        }
      }
      edgesRemoved.add(to_remove);
      filtered.removeEdge(to_remove);
    }

    WeakComponentClusterer<N, E> wcSearch = new WeakComponentClusterer<N, E>();
    Set<Set<N>> clusterSet = wcSearch.apply(filtered);

    return clusterSet;
  }

  /**
   * Retrieves the set of all edges that were removed. The edges returned are stored in order in
   * which they were removed.
   *
   * @return the edges removed from the original graph
   */
  public Set<E> getEdgesRemoved() {
    return edgesRemoved;
  }
}
