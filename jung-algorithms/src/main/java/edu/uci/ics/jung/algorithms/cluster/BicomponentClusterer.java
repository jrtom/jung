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

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Finds all biconnected components (bicomponents) of an graph, <b>ignoring edge direction</b>. A
 * graph is a biconnected component if at least 2 nodes must be removed in order to disconnect the
 * graph. (Graphs consisting of one node, or of two connected nodes, are also biconnected.)
 * Biconnected components of three or more nodes have the property that every pair of nodes in the
 * component are connected by two or more node-disjoint paths.
 *
 * <p>Running time: O(|V| + |E|) where |V| is the number of nodes and |E| is the number of edges
 *
 * @see "Depth first search and linear graph algorithms by R. E. Tarjan (1972), SIAM J. Comp."
 * @author Joshua O'Madadhain
 */
public class BicomponentClusterer<N, E> implements Function<Graph<N>, Set<Set<N>>> {
  protected Map<N, Number> dfs_num;
  protected Map<N, Number> high;
  protected Map<N, N> parents;
  protected Deque<EndpointPair<N>> stack;
  protected int converse_depth;

  /** Constructs a new bicomponent finder */
  public BicomponentClusterer() {}

  /**
   * Extracts the bicomponents from the graph.
   *
   * @param graph the graph whose bicomponents are to be extracted
   * @return the <code>ClusterSet</code> of bicomponents
   */
  public Set<Set<N>> apply(Graph<N> graph) {
    Set<Set<N>> bicomponents = new LinkedHashSet<Set<N>>();

    if (graph.nodes().isEmpty()) {
      return bicomponents;
    }

    // initialize DFS number for each node to 0
    dfs_num = new HashMap<N, Number>();
    for (N v : graph.nodes()) {
      dfs_num.put(v, 0);
    }

    for (N v : graph.nodes()) {
      if (dfs_num.get(v).intValue() == 0) { // if we haven't hit this node yet...
        high = new HashMap<N, Number>();
        stack = new ArrayDeque<EndpointPair<N>>();
        parents = new HashMap<N, N>();
        converse_depth = graph.nodes().size();
        // find the biconnected components for this subgraph, starting from v
        findBiconnectedComponents(graph, v, bicomponents);

        // if we only visited one node, this method won't have
        // ID'd it as a biconnected component, so mark it as one
        if (graph.nodes().size() - converse_depth == 1) {
          Set<N> s = new HashSet<N>();
          s.add(v);
          bicomponents.add(s);
        }
      }
    }

    return bicomponents;
  }

  /**
   * Stores, in <code>bicomponents</code>, all the biconnected components that are reachable from
   * <code>v</code>.
   *
   * <p>The algorithm basically proceeds as follows: do a depth-first traversal starting from <code>
   * v</code>, marking each node with a value that indicates the order in which it was encountered
   * (dfs_num), and with a value that indicates the highest point in the DFS tree that is known to
   * be reachable from this node using non-DFS edges (high). (Since it is measured on non-DFS edges,
   * "high" tells you how far back in the DFS tree you can reach by two distinct paths, hence
   * biconnectivity.) Each time a new node w is encountered, push the edge just traversed on a
   * stack, and call this method recursively. If w.high is no greater than v.dfs_num, then the
   * contents of the stack down to (v,w) is a biconnected component (and v is an articulation point,
   * that is, a component boundary). In either case, set v.high to max(v.high, w.high), and
   * continue. If w has already been encountered but is not v's parent, set v.high max(v.high,
   * w.dfs_num) and continue.
   *
   * <p>(In case anyone cares, the version of this algorithm on p. 224 of Udi Manber's "Introduction
   * to Algorithms: A Creative Approach" seems to be wrong: the stack should be initialized outside
   * this method, (v,w) should only be put on the stack if w hasn't been seen already, and there's
   * no real benefit to putting v on the stack separately: just check for (v,w) on the stack rather
   * than v. Had I known this, I could have saved myself a few days. JRTOM)
   *
   * @param g the graph to check for biconnected components
   * @param v the starting place for searching for biconnected components
   * @param bicomponents storage for the biconnected components found by this algorithm
   */
  protected void findBiconnectedComponents(Graph<N> g, N v, Set<Set<N>> bicomponents) {
    int v_dfs_num = converse_depth;
    dfs_num.put(v, v_dfs_num);
    converse_depth--;
    high.put(v, v_dfs_num);

    for (N w : g.adjacentNodes(v)) {
      int w_dfs_num = dfs_num.get(w).intValue();
      EndpointPair<N> vw = EndpointPair.unordered(v, w);
      if (w_dfs_num == 0) { // w hasn't yet been visited
        parents.put(w, v); // v is w's parent in the DFS tree
        stack.push(vw);
        findBiconnectedComponents(g, w, bicomponents);
        int w_high = high.get(w).intValue();
        if (w_high <= v_dfs_num) {
          // v disconnects w from the rest of the graph,
          // i.e., v is an articulation point
          // thus, everything between the top of the stack and
          // v is part of a single biconnected component
          Set<N> bicomponent = new HashSet<N>();
          EndpointPair<N> endpoints;
          do {
            endpoints = stack.pop();
            bicomponent.add(endpoints.nodeU());
            bicomponent.add(endpoints.nodeV());
          } while (!endpoints.equals(vw));
          bicomponents.add(bicomponent);
        }
        high.put(v, Math.max(w_high, high.get(v).intValue()));
      } else if (w != parents.get(v)) { // (v,w) is a back or a forward edge
        high.put(v, Math.max(w_dfs_num, high.get(v).intValue()));
      }
    }
  }
}
