/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.shortestpath;

import com.google.common.graph.Graph;
import java.util.HashMap;
import java.util.Map;

/**
 * Computes the shortest path distances for graphs whose edges are not weighted (using BFS).
 *
 * @author Scott White
 */
// TODO: refactor to make this (much!) more efficient
public class UnweightedShortestPath<N> implements Distance<N> {
  private Map<N, Map<N, Integer>> mDistanceMap;
  private Map<N, Map<N, N>> mPredecessorMap;
  private Graph<N> mGraph;
  private Map<N, Integer> distances = new HashMap<N, Integer>();

  /**
   * Constructs and initializes algorithm
   *
   * @param g the graph
   */
  public UnweightedShortestPath(Graph<N> g) {
    mDistanceMap = new HashMap<>();
    mPredecessorMap = new HashMap<>();
    mGraph = g;
  }

  /** @see edu.uci.ics.jung.algorithms.shortestpath.Distance#getDistance(Object, Object) */
  public Integer getDistance(N source, N target) {
    Map<N, Integer> sourceSPMap = getDistanceMap(source);
    return sourceSPMap.get(target);
  }

  /** @see edu.uci.ics.jung.algorithms.shortestpath.Distance#getDistanceMap(Object) */
  public Map<N, Integer> getDistanceMap(N source) {
    Map<N, Integer> sourceSPMap = mDistanceMap.get(source);
    if (sourceSPMap == null) {
      computeShortestPathsFromSource(source);
      sourceSPMap = mDistanceMap.get(source);
    }
    return sourceSPMap;
  }

  /** @see edu.uci.ics.jung.algorithms.shortestpath.ShortestPath#getIncomingEdgeMap(Object) */
  public Map<N, N> getIncomingEdgeMap(N source) {
    Map<N, N> sourceIEMap = mPredecessorMap.get(source);
    if (sourceIEMap == null) {
      computeShortestPathsFromSource(source);
      sourceIEMap = mPredecessorMap.get(source);
    }
    return sourceIEMap;
  }

  /**
   * Computes the shortest path distances from a given node to all other nodes.
   *
   * @param source the source node
   */
  private void computeShortestPathsFromSource(N source) {
    BFSDistanceLabeler<N> labeler = new BFSDistanceLabeler<N>();
    labeler.labelDistances(mGraph, source);
    distances = labeler.getDistanceDecorator();
    Map<N, Integer> currentSourceSPMap = new HashMap<N, Integer>();
    Map<N, N> currentSourcePredMap = new HashMap<N, N>();

    for (N node : mGraph.nodes()) {

      Integer distanceVal = distances.get(node);
      // BFSDistanceLabeler uses -1 to indicate unreachable nodes;
      // don't bother to store unreachable nodes
      if (distanceVal != null && distanceVal.intValue() >= 0) {
        currentSourceSPMap.put(node, distanceVal);
        int minDistance = distanceVal.intValue();
        for (N predecessor : mGraph.predecessors(node)) {
          if (predecessor.equals(node)) {
            continue;
          }

          Integer predDistance = distances.get(predecessor);
          if (predDistance < minDistance && predDistance >= 0) {
            minDistance = predDistance.intValue();
            currentSourcePredMap.put(node, predecessor);
          }
        }
      }
    }
    mDistanceMap.put(source, currentSourceSPMap);
    mPredecessorMap.put(source, currentSourcePredMap);
  }

  /**
   * Clears all stored distances for this instance. Should be called whenever the graph is modified
   * (edge weights changed or edges added/removed). If the user knows that some currently calculated
   * distances are unaffected by a change, <code>reset(V)</code> may be appropriate instead.
   *
   * @see #reset(Object)
   */
  public void reset() {
    mDistanceMap.clear();
    mPredecessorMap.clear();
  }

  /**
   * Clears all stored distances for the specified source node <code>source</code>. Should be called
   * whenever the stored distances from this node are invalidated by changes to the graph.
   *
   * @see #reset()
   * @param v the node for which distances should be cleared
   */
  public void reset(N v) {
    mDistanceMap.remove(v);
    mPredecessorMap.remove(v);
  }
}
