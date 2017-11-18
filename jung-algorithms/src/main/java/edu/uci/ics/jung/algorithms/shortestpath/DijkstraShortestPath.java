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
public class DijkstraShortestPath<V, E> extends DijkstraDistance<V, E>
    implements ShortestPath<V, E> {
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
  public DijkstraShortestPath(Network<V, E> g, Function<E, ? extends Number> nev, boolean cached) {
    super(g, nev, cached);
  }

  /**
   * Creates an instance of <code>DijkstraShortestPath</code> for the specified graph and the
   * specified method of extracting weights from edges, which caches results locally.
   *
   * @param g the graph on which distances will be calculated
   * @param nev the class responsible for returning weights for edges
   */
  public DijkstraShortestPath(Network<V, E> g, Function<E, ? extends Number> nev) {
    super(g, nev);
  }

  /**
   * Creates an instance of <code>DijkstraShortestPath</code> for the specified unweighted graph
   * (that is, all weights 1) which caches results locally.
   *
   * @param g the graph on which distances will be calculated
   */
  public DijkstraShortestPath(Network<V, E> g) {
    super(g);
  }

  /**
   * Creates an instance of <code>DijkstraShortestPath</code> for the specified unweighted graph
   * (that is, all weights 1) which caches results locally.
   *
   * @param g the graph on which distances will be calculated
   * @param cached specifies whether the results are to be cached
   */
  public DijkstraShortestPath(Network<V, E> g, boolean cached) {
    super(g, cached);
  }

  @Override
  protected SourceData getSourceData(V source) {
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
   * <p>If either vertex is not in the graph for which this instance was created, throws <code>
   * IllegalArgumentException</code>.
   *
   * @param source the vertex where the shortest path starts
   * @param target the vertex where the shortest path ends
   * @return the last edge on a shortest path from {@code source} to {@code target} or null if
   *     {@code target} is not reachable from {@code source}
   */
  public E getIncomingEdge(V source, V target) {
    Preconditions.checkArgument(
        g.nodes().contains(target),
        "Specified target vertex %s  is not part of graph %s",
        target,
        g);
    Preconditions.checkArgument(
        g.nodes().contains(source),
        "Specified source vertex %s  is not part of graph %s",
        source,
        g);

    Set<V> targets = new HashSet<V>();
    targets.add(target);
    singleSourceShortestPath(source, targets, g.nodes().size());
    @SuppressWarnings("unchecked")
    Map<V, E> incomingEdgeMap = ((SourcePathData) sourceMap.get(source)).incomingEdges;
    E incomingEdge = incomingEdgeMap.get(target);

    if (!cached) {
      reset(source);
    }

    return incomingEdge;
  }

  /**
   * Returns a <code>LinkedHashMap</code> which maps each vertex in the graph (including the <code>
   * source</code> vertex) to the last edge on the shortest path from the <code>source</code>
   * vertex. The map's iterator will return the elements in order of increasing distance from <code>
   * source</code>.
   *
   * @see DijkstraDistance#getDistanceMap(Object,int)
   * @see DijkstraDistance#getDistance(Object,Object)
   * @param source the vertex from which distances are measured
   */
  public Map<V, E> getIncomingEdgeMap(V source) {
    return getIncomingEdgeMap(source, g.nodes().size());
  }

  /**
   * Returns a <code>List</code> of the edges on the shortest path from <code>source</code> to
   * <code>target</code>, in order of their occurrence on this path. If either vertex is not in the
   * graph for which this instance was created, throws <code>IllegalArgumentException</code>.
   *
   * @param source the starting vertex for the path to generate
   * @param target the ending vertex for the path to generate
   * @return the edges on the shortest path from {@code source} to {@code target}, in order of their
   *     occurrence
   */
  public List<E> getPath(V source, V target) {
    Preconditions.checkArgument(
        g.nodes().contains(target),
        "Specified target vertex %s  is not part of graph %s",
        target,
        g);
    Preconditions.checkArgument(
        g.nodes().contains(source),
        "Specified source vertex %s  is not part of graph %s",
        source,
        g);

    LinkedList<E> path = new LinkedList<E>();

    // collect path data; must use internal method rather than
    // calling getIncomingEdge() because getIncomingEdge() may
    // wipe out results if results are not cached
    Set<V> targets = new HashSet<V>();
    targets.add(target);
    singleSourceShortestPath(source, targets, g.nodes().size());
    @SuppressWarnings("unchecked")
    Map<V, E> incomingEdges = ((SourcePathData) sourceMap.get(source)).incomingEdges;

    if (incomingEdges.isEmpty() || incomingEdges.get(target) == null) {
      return path;
    }
    V current = target;
    while (!current.equals(source)) {
      E incoming = incomingEdges.get(current);
      path.addFirst(incoming);
      current = g.incidentNodes(incoming).adjacentNode(current);
    }
    return path;
  }

  /**
   * Returns a <code>LinkedHashMap</code> which maps each of the closest <code>numDests</code>
   * vertices to the <code>source</code> vertex in the graph (including the <code>source</code>
   * vertex) to the incoming edge along the path from that vertex. Throws an <code>
   * IllegalArgumentException</code> if <code>source</code> is not in this instance's graph, or if
   * <code>numDests</code> is either less than 1 or greater than the number of vertices in the
   * graph.
   *
   * @see #getIncomingEdgeMap(Object)
   * @see #getPath(Object,Object)
   * @param source the vertex from which distances are measured
   * @param numDests the number of vertices for which to measure distances
   * @return a map from each of the closest {@code numDests} vertices to the last edge on the
   *     shortest path to that vertex starting from {@code source}
   */
  public LinkedHashMap<V, E> getIncomingEdgeMap(V source, int numDests) {
    Preconditions.checkArgument(
        g.nodes().contains(source),
        "Specified source vertex %s  is not part of graph %s",
        source,
        g);
    Preconditions.checkArgument(
        numDests >= 1 && numDests <= g.nodes().size(),
        "number of destinations must be in [1, %d]",
        g.nodes().size());

    singleSourceShortestPath(source, null, numDests);

    @SuppressWarnings("unchecked")
    LinkedHashMap<V, E> incomingEdgeMap = ((SourcePathData) sourceMap.get(source)).incomingEdges;

    if (!cached) {
      reset(source);
    }

    return incomingEdgeMap;
  }

  /**
   * For a given source vertex, holds the estimated and final distances, tentative and final
   * assignments of incoming edges on the shortest path from the source vertex, and a priority queue
   * (ordered by estimaed distance) of the vertices for which distances are unknown.
   *
   * @author Joshua O'Madadhain
   */
  protected class SourcePathData extends SourceData {
    protected Map<V, E> tentativeIncomingEdges;
    protected LinkedHashMap<V, E> incomingEdges;

    protected SourcePathData(V source) {
      super(source);
      incomingEdges = new LinkedHashMap<V, E>();
      tentativeIncomingEdges = new HashMap<V, E>();
    }

    @Override
    public void update(V dest, E tentative_edge, double new_dist) {
      super.update(dest, tentative_edge, new_dist);
      tentativeIncomingEdges.put(dest, tentative_edge);
    }

    @Override
    public Map.Entry<V, Number> getNextVertex() {
      Map.Entry<V, Number> p = super.getNextVertex();
      V v = p.getKey();
      E incoming = tentativeIncomingEdges.remove(v);
      incomingEdges.put(v, incoming);
      return p;
    }

    @Override
    public void restoreVertex(V v, double dist) {
      super.restoreVertex(v, dist);
      E incoming = incomingEdges.get(v);
      tentativeIncomingEdges.put(v, incoming);
    }

    @Override
    public void createRecord(V w, E e, double new_dist) {
      super.createRecord(w, e, new_dist);
      tentativeIncomingEdges.put(w, e);
    }
  }
}
