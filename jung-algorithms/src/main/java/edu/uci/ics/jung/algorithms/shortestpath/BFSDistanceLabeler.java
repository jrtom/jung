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

import com.google.common.base.Preconditions;
import com.google.common.graph.Graph;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Labels each node in the graph according to the BFS distance from the start node(s). If nodes are
 * unreachable, then they are assigned a distance of -1. All nodes traversed at step k are marked as
 * predecessors of their successors traversed at step k+1.
 *
 * <p>Running time is: O(m)
 *
 * @author Scott White
 */
// TODO: update or replace
public class BFSDistanceLabeler<N> {

  private Map<N, Integer> distanceDecorator = new HashMap<N, Integer>();
  private List<N> mCurrentList;
  private Set<N> mUnvisitedVertices;
  private List<N> mVerticesInOrderVisited;
  private Map<N, HashSet<N>> mPredecessorMap;

  /**
   * Creates a new BFS labeler for the specified graph and root set The distances are stored in the
   * corresponding Vertex objects and are of type MutableInteger
   */
  public BFSDistanceLabeler() {
    mPredecessorMap = new HashMap<N, HashSet<N>>();
  }

  /**
   * Returns the list of vertices visited in order of traversal
   *
   * @return the list of vertices
   */
  public List<N> getVerticesInOrderVisited() {
    return mVerticesInOrderVisited;
  }

  /**
   * Returns the set of all vertices that were not visited
   *
   * @return the list of unvisited vertices
   */
  public Set<N> getUnvisitedVertices() {
    return mUnvisitedVertices;
  }

  /**
   * Given a vertex, returns the shortest distance from any node in the root set to v
   *
   * @param g the graph in which the distances are to be measured
   * @param v the vertex whose distance is to be retrieved
   * @return the shortest distance from any node in the root set to v
   */
  public int getDistance(Graph<N> g, N v) {
    Preconditions.checkArgument(
        g.nodes().contains(v), "Vertex %s is not contained in the graph %s", v, g);
    if (!g.nodes().contains(v)) {
      throw new IllegalArgumentException("Vertex is not contained in the graph.");
    }

    return distanceDecorator.get(v);
  }

  /**
   * Returns set of predecessors of the given vertex
   *
   * @param v the vertex whose predecessors are to be retrieved
   * @return the set of predecessors
   */
  public Set<N> getPredecessors(N v) {
    return mPredecessorMap.get(v);
  }

  protected void initialize(Graph<N> g, Set<N> rootSet) {
    mVerticesInOrderVisited = new ArrayList<N>();
    mUnvisitedVertices = new HashSet<N>();
    for (N currentVertex : g.nodes()) {
      mUnvisitedVertices.add(currentVertex);
      mPredecessorMap.put(currentVertex, new HashSet<N>());
    }

    mCurrentList = new ArrayList<N>();
    for (N v : rootSet) {
      distanceDecorator.put(v, new Integer(0));
      mCurrentList.add(v);
      mUnvisitedVertices.remove(v);
      mVerticesInOrderVisited.add(v);
    }
  }

  private void addPredecessor(N predecessor, N successor) {
    HashSet<N> predecessors = mPredecessorMap.get(successor);
    predecessors.add(predecessor);
  }

  /**
   * Computes the distances of all the node from the starting root nodes. If there is more than one
   * root node the minimum distance from each root node is used as the designated distance to a
   * given node. Also keeps track of the predecessors of each node traversed as well as the order of
   * nodes traversed.
   *
   * @param graph the graph to label
   * @param rootSet the set of starting vertices to traverse from
   */
  public void labelDistances(Graph<N> graph, Set<N> rootSet) {

    initialize(graph, rootSet);

    int distance = 1;
    while (true) {
      List<N> newList = new ArrayList<N>();
      for (N currentVertex : mCurrentList) {
        if (graph.nodes().contains(currentVertex)) {
          for (N next : graph.successors(currentVertex)) {
            visitNewVertex(currentVertex, next, distance, newList);
          }
        }
      }
      if (newList.size() == 0) {
        break;
      }
      mCurrentList = newList;
      distance++;
    }

    for (N v : mUnvisitedVertices) {
      distanceDecorator.put(v, new Integer(-1));
    }
  }

  /**
   * Computes the distances of all the node from the specified root node. Also keeps track of the
   * predecessors of each node traversed as well as the order of nodes traversed.
   *
   * @param graph the graph to label
   * @param root the single starting vertex to traverse from
   */
  public void labelDistances(Graph<N> graph, N root) {
    labelDistances(graph, Collections.singleton(root));
  }

  private void visitNewVertex(N predecessor, N neighbor, int distance, List<N> newList) {
    if (mUnvisitedVertices.contains(neighbor)) {
      distanceDecorator.put(neighbor, new Integer(distance));
      newList.add(neighbor);
      mVerticesInOrderVisited.add(neighbor);
      mUnvisitedVertices.remove(neighbor);
    }
    int predecessorDistance = distanceDecorator.get(predecessor).intValue();
    int successorDistance = distanceDecorator.get(neighbor).intValue();
    if (predecessorDistance < successorDistance) {
      addPredecessor(predecessor, neighbor);
    }
  }

  /**
   * Must be called after {@code labelDistances} in order to contain valid data.
   *
   * @return a map from vertices to minimum distances from the original source(s)
   */
  public Map<N, Integer> getDistanceDecorator() {
    return distanceDecorator;
  }
}
