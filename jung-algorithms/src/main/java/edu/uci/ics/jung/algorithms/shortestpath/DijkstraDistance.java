/*
 * Created on Jul 9, 2005
 *
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.shortestpath;

import static java.util.Comparator.comparingDouble;

import com.google.common.base.Preconditions;
import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.util.MapBinaryHeap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Calculates distances in a specified graph, using Dijkstra's single-source-shortest-path
 * algorithm. All edge weights in the graph must be nonnegative; if any edge with negative weight is
 * found in the course of calculating distances, an <code>IllegalArgumentException</code> will be
 * thrown. (Note: this exception will only be thrown when such an edge would be used to update a
 * given tentative distance; the algorithm does not check for negative-weight edges "up front".)
 *
 * <p>Distances and partial results are optionally cached (by this instance) for later reference.
 * Thus, if the 10 closest nodes to a specified source node are known, calculating the 20 closest
 * nodes does not require starting Dijkstra's algorithm over from scratch.
 *
 * <p>Distances are stored as double-precision values. If a node is not reachable from the specified
 * source node, no distance is stored. <b>This is new behavior with version 1.4</b>; the previous
 * behavior was to store a value of <code>Double.POSITIVE_INFINITY</code>. This change gives the
 * algorithm an approximate complexity of O(kD log k), where k is either the number of requested
 * targets or the number of reachable nodes (whichever is smaller), and D is the average degree of a
 * node.
 *
 * <p>The elements in the maps returned by <code>getDistanceMap</code> are ordered (that is,
 * returned by the iterator) by nondecreasing distance from <code>source</code>.
 *
 * <p>Users are cautioned that distances calculated should be assumed to be invalidated by changes
 * to the graph, and should invoke <code>reset()</code> when appropriate so that the distances can
 * be recalculated.
 *
 * @author Joshua O'Madadhain
 * @author Tom Nelson converted to jung2
 */
public class DijkstraDistance<N, E> implements Distance<N> {
  protected Network<N, E> g;
  protected Function<? super E, ? extends Number> nev;
  protected Map<N, SourceData> sourceMap; // a map of source nodes to an instance of SourceData
  protected boolean cached;
  protected double maxDistance;
  protected int maxTargets;

  /**
   * Creates an instance of <code>DijkstraShortestPath</code> for the specified graph and the
   * specified method of extracting weights from edges, which caches results locally if and only if
   * <code>cached</code> is <code>true</code>.
   *
   * @param g the graph on which distances will be calculated
   * @param nev the class responsible for returning weights for edges
   * @param cached specifies whether the results are to be cached
   */
  public DijkstraDistance(
      Network<N, E> g, Function<? super E, ? extends Number> nev, boolean cached) {
    this.g = g;
    this.nev = nev;
    this.sourceMap = new HashMap<>();
    this.cached = cached;
    this.maxDistance = Double.POSITIVE_INFINITY;
    this.maxTargets = Integer.MAX_VALUE;
  }

  /**
   * Creates an instance of <code>DijkstraShortestPath</code> for the specified graph and the
   * specified method of extracting weights from edges, which caches results locally.
   *
   * @param g the graph on which distances will be calculated
   * @param nev the class responsible for returning weights for edges
   */
  public DijkstraDistance(Network<N, E> g, Function<? super E, ? extends Number> nev) {
    this(g, nev, true);
  }

  /**
   * Creates an instance of <code>DijkstraShortestPath</code> for the specified unweighted graph
   * (that is, all weights 1) which caches results locally.
   *
   * @param g the graph on which distances will be calculated
   */
  public DijkstraDistance(Network<N, E> g) {
    this(g, e -> 1, true);
  }

  /**
   * Creates an instance of <code>DijkstraShortestPath</code> for the specified unweighted graph
   * (that is, all weights 1) which caches results locally.
   *
   * @param g the graph on which distances will be calculated
   * @param cached specifies whether the results are to be cached
   */
  public DijkstraDistance(Network<N, E> g, boolean cached) {
    this(g, e -> 1, cached);
  }

  /**
   * Implements Dijkstra's single-source shortest-path algorithm for weighted graphs. Uses a <code>
   * MapBinaryHeap</code> as the priority queue, which gives this algorithm a time complexity of O(m
   * lg n) (m = # of edges, n = # of nodes). This algorithm will terminate when any of the following
   * have occurred (in order of priority):
   *
   * <ul>
   *   <li>the distance to the specified target (if any) has been found
   *   <li>no more nodes are reachable
   *   <li>the specified # of distances have been found, or the maximum distance desired has been
   *       exceeded
   *   <li>all distances have been found
   * </ul>
   *
   * @param source the node from which distances are to be measured
   * @param numDistances the number of distances to measure
   * @param targets the set of nodes to which distances are to be measured
   * @return a mapping from node to the shortest distance from the source to each target
   */
  protected LinkedHashMap<N, Number> singleSourceShortestPath(
      N source, Collection<N> targets, int numDistances) {
    SourceData sd = getSourceData(source);

    Set<N> toGet = new HashSet<>();
    if (targets != null) {
      toGet.addAll(targets);
      Set<N> existingDistances = sd.distances.keySet();
      for (N o : targets) {
        if (existingDistances.contains(o)) {
          toGet.remove(o);
        }
      }
    }

    // if we've exceeded the max distance or max # of distances we're willing to calculate, or
    // if we already have all the distances we need,
    // terminate
    if (sd.reachedMax
        || (targets != null && toGet.isEmpty())
        || (sd.distances.size() >= numDistances)) {
      return sd.distances;
    }

    while (!sd.unknownNodes.isEmpty() && (sd.distances.size() < numDistances || !toGet.isEmpty())) {
      Map.Entry<N, Number> p = sd.getNextNode();
      N v = p.getKey();
      double vDist = p.getValue().doubleValue();
      toGet.remove(v);
      if (vDist > this.maxDistance) {
        // we're done; put this node back in so that we're not including
        // a distance beyond what we specified
        sd.restoreNode(v, vDist);
        sd.reachedMax = true;
        break;
      }
      sd.distanceReached = vDist;

      if (sd.distances.size() >= this.maxTargets) {
        sd.reachedMax = true;
        break;
      }

      for (N w : g.successors(v)) {
        for (E e : g.edgesConnecting(v, w)) {
          if (!sd.distances.containsKey(w)) {
            double edgeWeight = nev.apply(e).doubleValue();
            Preconditions.checkArgument(
                edgeWeight >= 0,
                "encountered negative edge weight %s for edge %s",
                nev.apply(e),
                e);
            double newDist = vDist + edgeWeight;
            if (!sd.estimatedDistances.containsKey(w)) {
              sd.createRecord(w, e, newDist);
            } else {
              double wDist = (Double) sd.estimatedDistances.get(w);
              if (newDist < wDist) { // update tentative distance & path for w
                sd.update(w, e, newDist);
              }
            }
          }
        }
      }
    }
    return sd.distances;
  }

  protected SourceData getSourceData(N source) {
    SourceData sd = sourceMap.get(source);
    if (sd == null) {
      sd = new SourceData(source);
    }
    return sd;
  }

  /**
   * Returns the length of a shortest path from the source to the target node, or null if the target
   * is not reachable from the source. If either node is not in the graph for which this instance
   * was created, throws <code>IllegalArgumentException</code>.
   *
   * @param source the node from which the distance to {@code target} is to be measured
   * @param target the node to which the distance from {@code source} is to be measured
   * @return the distance between {@code source} and {@code target}
   * @see #getDistanceMap(Object)
   * @see #getDistanceMap(Object,int)
   */
  public Number getDistance(N source, N target) {
    Preconditions.checkArgument(
        g.nodes().contains(target), "Specified target node %s  is not part of graph %s", target, g);
    Preconditions.checkArgument(
        g.nodes().contains(source), "Specified source node %s  is not part of graph %s", source, g);

    Set<N> targets = new HashSet<>();
    targets.add(target);
    Map<N, Number> distanceMap = getDistanceMap(source, targets);
    return distanceMap.get(target);
  }

  /**
   * Returns a {@code Map} from each element {@code t} of {@code targets} to the shortest-path
   * distance from {@code source} to {@code t}.
   *
   * @param source the node from which the distance to each target is to be measured
   * @param targets the nodes to which the distance from the source is to be measured
   * @return {@code Map} from each element of {@code targets} to its distance from {@code source}
   */
  public Map<N, Number> getDistanceMap(N source, Collection<N> targets) {
    Preconditions.checkArgument(
        g.nodes().contains(source), "Specified source node %s  is not part of graph %s", source, g);
    Preconditions.checkArgument(
        targets.size() <= maxTargets,
        "size of target set %d exceeds maximum number of targets allowed: %d",
        targets.size(),
        this.maxTargets);

    Map<N, Number> distanceMap =
        singleSourceShortestPath(source, targets, Math.min(g.nodes().size(), maxTargets));
    if (!cached) {
      reset(source);
    }

    return distanceMap;
  }

  /**
   * Returns a <code>LinkedHashMap</code> which maps each node in the graph (including the <code>
   * source</code> node) to its distance from the <code>source</code> node. The map's iterator will
   * return the elements in order of increasing distance from <code>source</code>.
   *
   * <p>The size of the map returned will be the number of nodes reachable from <code>source
   * </code>.
   *
   * @see #getDistanceMap(Object,int)
   * @see #getDistance(Object,Object)
   * @param source the node from which distances are measured
   * @return a mapping from each node in the graph to its distance from {@code source}
   */
  public Map<N, Number> getDistanceMap(N source) {
    return getDistanceMap(source, Math.min(g.nodes().size(), maxTargets));
  }

  /**
   * Returns a <code>LinkedHashMap</code> which maps each of the closest <code>numDist</code> nodes
   * to the <code>source</code> node in the graph (including the <code>source</code> node) to its
   * distance from the <code>source</code> node. Throws an <code>
   * IllegalArgumentException</code> if <code>source</code> is not in this instance's graph, or if
   * <code>numDests</code> is either less than 1 or greater than the number of nodes in the graph.
   *
   * <p>The size of the map returned will be the smaller of <code>numDests</code> and the number of
   * nodes reachable from <code>source</code>.
   *
   * @see #getDistanceMap(Object)
   * @see #getDistance(Object,Object)
   * @param source the node from which distances are measured
   * @param numDests the number of nodes for which to measure distances
   * @return a mapping from the {@code numDests} nodes in the graph closest to {@code source}, to
   *     their distance from {@code source}
   */
  public LinkedHashMap<N, Number> getDistanceMap(N source, int numDests) {
    Preconditions.checkArgument(
        g.nodes().contains(source), "Specified source node %s is not part of graph %s", source, g);
    Preconditions.checkArgument(
        numDests >= 1 && numDests <= g.nodes().size(),
        "number of destinations must be in [1, %d]",
        g.nodes().size());

    Preconditions.checkArgument(
        numDests <= maxTargets,
        "size of target set %d exceeds maximum number of targets allowed: %d",
        numDests,
        this.maxTargets);

    LinkedHashMap<N, Number> distanceMap = singleSourceShortestPath(source, null, numDests);

    if (!cached) {
      reset(source);
    }

    return distanceMap;
  }

  /**
   * Allows the user to specify the maximum distance that this instance will calculate. Any nodes
   * past this distance will effectively be unreachable from the source, in the sense that the
   * algorithm will not calculate the distance to any nodes which are farther away than this
   * distance. A negative value for <code>maxDistance</code> will ensure that no further distances
   * are calculated.
   *
   * <p>This can be useful for limiting the amount of time and space used by this algorithm if the
   * graph is very large.
   *
   * <p>Note: if this instance has already calculated distances greater than <code>maxDistance
   * </code>, and the results are cached, those results will still be valid and available; this
   * limit applies only to subsequent distance calculations.
   *
   * @param maxDistance the maximum distance that this instance will calculate
   * @see #setMaxTargets(int)
   */
  public void setMaxDistance(double maxDistance) {
    this.maxDistance = maxDistance;
    for (N v : sourceMap.keySet()) {
      SourceData sd = sourceMap.get(v);
      sd.reachedMax =
          (this.maxDistance <= sd.distanceReached) || (sd.distances.size() >= maxTargets);
    }
  }

  /**
   * Allows the user to specify the maximum number of target nodes per source node for which this
   * instance will calculate distances. Once this threshold is reached, any further nodes will
   * effectively be unreachable from the source, in the sense that the algorithm will not calculate
   * the distance to any more nodes. A negative value for <code>maxTargets</code> will ensure that
   * no further distances are calculated.
   *
   * <p>This can be useful for limiting the amount of time and space used by this algorithm if the
   * graph is very large.
   *
   * <p>Note: if this instance has already calculated distances to a greater number of targets than
   * <code>maxTargets</code>, and the results are cached, those results will still be valid and
   * available; this limit applies only to subsequent distance calculations.
   *
   * @param maxTargets the maximum number of targets for which this instance will calculate
   *     distances
   * @see #setMaxDistance(double)
   */
  public void setMaxTargets(int maxTargets) {
    this.maxTargets = maxTargets;
    for (N v : sourceMap.keySet()) {
      SourceData sd = sourceMap.get(v);
      sd.reachedMax =
          (this.maxDistance <= sd.distanceReached) || (sd.distances.size() >= maxTargets);
    }
  }

  /**
   * Clears all stored distances for this instance. Should be called whenever the graph is modified
   * (edge weights changed or edges added/removed). If the user knows that some currently calculated
   * distances are unaffected by a change, <code>reset(V)</code> may be appropriate instead.
   *
   * @see #reset(Object)
   */
  public void reset() {
    sourceMap = new HashMap<>();
  }

  /**
   * Specifies whether or not this instance of <code>DijkstraShortestPath</code> should cache its
   * results (final and partial) for future reference.
   *
   * @param enable <code>true</code> if the results are to be cached, and <code>false</code>
   *     otherwise
   */
  public void enableCaching(boolean enable) {
    this.cached = enable;
  }

  /**
   * Clears all stored distances for the specified source node <code>source</code>. Should be called
   * whenever the stored distances from this node are invalidated by changes to the graph.
   *
   * @param source the node for which stored distances should be cleared
   * @see #reset()
   */
  public void reset(N source) {
    sourceMap.put(source, null);
  }

  /**
   * For a given source node, holds the estimated and final distances, tentative and final
   * assignments of incoming edges on the shortest path from the source node, and a priority queue
   * (ordered by estimated distance) of the nodes for which distances are unknown.
   *
   * @author Joshua O'Madadhain
   */
  protected class SourceData {
    protected LinkedHashMap<N, Number> distances;
    protected Map<N, Number> estimatedDistances;
    protected MapBinaryHeap<N> unknownNodes;
    protected boolean reachedMax = false;
    protected double distanceReached = 0;

    protected SourceData(N source) {
      distances = new LinkedHashMap<>();
      estimatedDistances = new HashMap<>();
      unknownNodes =
          new MapBinaryHeap<>(comparingDouble(n -> estimatedDistances.get(n).doubleValue()));

      sourceMap.put(source, this);

      // initialize priority queue
      estimatedDistances.put(source, 0d); // distance from source to itself is 0
      unknownNodes.add(source);
      reachedMax = false;
      distanceReached = 0d;
    }

    protected Map.Entry<N, Number> getNextNode() {
      N v = unknownNodes.remove();
      Number dist = estimatedDistances.remove(v);
      distances.put(v, dist);
      return new SimpleImmutableEntry<>(v, dist);
    }

    protected void update(N dest, E tentativeEdge, double newDist) {
      estimatedDistances.put(dest, newDist);
      unknownNodes.update(dest);
    }

    protected void createRecord(N w, E e, double newDist) {
      estimatedDistances.put(w, newDist);
      unknownNodes.add(w);
    }

    protected void restoreNode(N v, double dist) {
      estimatedDistances.put(v, dist);
      unknownNodes.add(v);
      distances.remove(v);
    }
  }
}
