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
import com.google.common.graph.Network;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Calculates distances and shortest paths using Dijkstra's single-source-shortest-path algorithm.
 * This is a lightweight extension of <code>DijkstraDistance</code> that also stores path
 * information, so that the shortest paths can be reconstructed.
 *
 * <p>The elements in the maps returned by <code>getIncomingEdgeMap</code> are ordered (that is,
 * returned by the iterator) by nondecreasing distance from <code>source</code>.
 *
 * @author Joshua O'Madadhain
 * @author Tom Nelson converted to jung2
 * @see DijkstraDistance
 */
public class DijkstraShortestPath<N, E> extends DijkstraDistance<N, E>
    implements ShortestPath<N, E> {
  // TODO: refactor the heck out of this and of DijkstraDistance

  /**
   * Creates an instance of <code>DijkstraShortestPath</code> for the specified graph and the
   * specified method of extracting weights from edges, which caches results locally if and only if
   * <code>cached</code> is <code>true</code>.
   *
   * @param g the graph on which distances will be calculated
   * @param nev the class responsible for returning weights for edges
   * @param cached specifies whether the results are to be cached
   */
  public DijkstraShortestPath(Network<N, E> g, Function<E, ? extends Number> nev, boolean cached) {
    super(g, nev, cached);
  }

  /**
   * Creates an instance of <code>DijkstraShortestPath</code> for the specified graph and the
   * specified method of extracting weights from edges, which caches results locally.
   *
   * @param g the graph on which distances will be calculated
   * @param nev the class responsible for returning weights for edges
   */
  public DijkstraShortestPath(Network<N, E> g, Function<E, ? extends Number> nev) {
    super(g, nev);
  }

  /**
   * Creates an instance of <code>DijkstraShortestPath</code> for the specified unweighted graph
   * (that is, all weights 1) which caches results locally.
   *
   * @param g the graph on which distances will be calculated
   */
  public DijkstraShortestPath(Network<N, E> g) {
    super(g);
  }

  /**
   * Creates an instance of <code>DijkstraShortestPath</code> for the specified unweighted graph
   * (that is, all weights 1) which caches results locally.
   *
   * @param g the graph on which distances will be calculated
   * @param cached specifies whether the results are to be cached
   */
  public DijkstraShortestPath(Network<N, E> g, boolean cached) {
    super(g, cached);
  }

  @Override
  protected SourceData getSourceData(N source) {
    SourceData sd = sourceMap.get(source);
    if (sd == null) {
      sd = new SourcePathData(source);
    }
    return sd;
  }

  /**
   * Returns the last edge on a shortest path from <code>source</code> to <code>target</code>, or
   * null if <code>target</code> is not reachable from <code>source</code>.
   *
   * <p>If either node is not in the graph for which this instance was created, throws <code>
   * IllegalArgumentException</code>.
   *
   * @param source the node where the shortest path starts
   * @param target the node where the shortest path ends
   * @return the last edge on a shortest path from {@code source} to {@code target} or null if
   *     {@code target} is not reachable from {@code source}
   */
  public E getIncomingEdge(N source, N target) {
    Preconditions.checkArgument(
        g.nodes().contains(target), "Specified target node %s  is not part of graph %s", target, g);
    Preconditions.checkArgument(
        g.nodes().contains(source), "Specified source node %s  is not part of graph %s", source, g);

    Set<N> targets = new HashSet<N>();
    targets.add(target);
    singleSourceShortestPath(source, targets, g.nodes().size());
    @SuppressWarnings("unchecked")
    Map<N, E> incomingEdgeMap = ((SourcePathData) sourceMap.get(source)).incomingEdges;
    E incomingEdge = incomingEdgeMap.get(target);

    if (!cached) {
      reset(source);
    }

    return incomingEdge;
  }

  /**
   * Returns a <code>LinkedHashMap</code> which maps each node in the graph (including the <code>
   * source</code> node) to the last edge on the shortest path from the <code>source</code> node.
   * The map's iterator will return the elements in order of increasing distance from <code>
   * source</code>.
   *
   * @see DijkstraDistance#getDistanceMap(Object,int)
   * @see DijkstraDistance#getDistance(Object,Object)
   * @param source the node from which distances are measured
   */
  public Map<N, E> getIncomingEdgeMap(N source) {
    return getIncomingEdgeMap(source, g.nodes().size());
  }

  /**
   * Returns a <code>List</code> of the edges on the shortest path from <code>source</code> to
   * <code>target</code>, in order of their occurrence on this path. If either node is not in the
   * graph for which this instance was created, throws <code>IllegalArgumentException</code>.
   *
   * @param source the starting node for the path to generate
   * @param target the ending node for the path to generate
   * @return the edges on the shortest path from {@code source} to {@code target}, in order of their
   *     occurrence
   */
  public List<E> getPath(N source, N target) {
    Preconditions.checkArgument(
        g.nodes().contains(target), "Specified target node %s  is not part of graph %s", target, g);
    Preconditions.checkArgument(
        g.nodes().contains(source), "Specified source node %s  is not part of graph %s", source, g);

    // we use a LinkedList here because we're always appending to the front
    LinkedList<E> path = new LinkedList<E>();

    // collect path data; must use internal method rather than
    // calling getIncomingEdge() because getIncomingEdge() may
    // wipe out results if results are not cached
    Set<N> targets = new HashSet<N>();
    targets.add(target);
    singleSourceShortestPath(source, targets, g.nodes().size());
    @SuppressWarnings("unchecked")
    Map<N, E> incomingEdges = ((SourcePathData) sourceMap.get(source)).incomingEdges;

    if (incomingEdges.isEmpty() || incomingEdges.get(target) == null) {
      return path;
    }
    N current = target;
    while (!current.equals(source)) {
      E incoming = incomingEdges.get(current);
      path.addFirst(incoming);
      current = g.incidentNodes(incoming).adjacentNode(current);
    }

    if (!cached) {
      reset(source);
    }

    return new ArrayList(path);
  }

  /**
   * Returns a <code>LinkedHashMap</code> which maps each of the closest <code>numDests</code> nodes
   * to the <code>source</code> node in the graph (including the <code>source</code> node) to the
   * incoming edge along the path from that node. Throws an <code>
   * IllegalArgumentException</code> if <code>source</code> is not in this instance's graph, or if
   * <code>numDests</code> is either less than 1 or greater than the number of nodes in the graph.
   *
   * @see #getIncomingEdgeMap(Object)
   * @see #getPath(Object,Object)
   * @param source the node from which distances are measured
   * @param numDests the number of nodes for which to measure distances
   * @return a map from each of the closest {@code numDests} nodes to the last edge on the shortest
   *     path to that node starting from {@code source}
   */
  public LinkedHashMap<N, E> getIncomingEdgeMap(N source, int numDests) {
    Preconditions.checkArgument(
        g.nodes().contains(source), "Specified source node %s  is not part of graph %s", source, g);
    Preconditions.checkArgument(
        numDests >= 1 && numDests <= g.nodes().size(),
        "number of destinations must be in [1, %d]",
        g.nodes().size());

    singleSourceShortestPath(source, null, numDests);

    @SuppressWarnings("unchecked")
    LinkedHashMap<N, E> incomingEdgeMap = ((SourcePathData) sourceMap.get(source)).incomingEdges;

    if (!cached) {
      reset(source);
    }

    return incomingEdgeMap;
  }

  /**
   * For a given source node, holds the estimated and final distances, tentative and final
   * assignments of incoming edges on the shortest path from the source node, and a priority queue
   * (ordered by estimaed distance) of the nodes for which distances are unknown.
   *
   * @author Joshua O'Madadhain
   */
  protected class SourcePathData extends SourceData {
    protected Map<N, E> tentativeIncomingEdges;
    protected LinkedHashMap<N, E> incomingEdges;

    protected SourcePathData(N source) {
      super(source);
      incomingEdges = new LinkedHashMap<N, E>();
      tentativeIncomingEdges = new HashMap<N, E>();
    }

    @Override
    public void update(N dest, E tentative_edge, double new_dist) {
      super.update(dest, tentative_edge, new_dist);
      tentativeIncomingEdges.put(dest, tentative_edge);
    }

    @Override
    public Map.Entry<N, Number> getNextNode() {
      Map.Entry<N, Number> p = super.getNextNode();
      N v = p.getKey();
      E incoming = tentativeIncomingEdges.remove(v);
      incomingEdges.put(v, incoming);
      return p;
    }

    @Override
    public void restoreNode(N v, double dist) {
      super.restoreNode(v, dist);
      E incoming = incomingEdges.get(v);
      tentativeIncomingEdges.put(v, incoming);
    }

    @Override
    public void createRecord(N w, E e, double new_dist) {
      super.createRecord(w, e, new_dist);
      tentativeIncomingEdges.put(w, e);
    }
  }
}
