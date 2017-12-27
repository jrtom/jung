/*
 * Created on Apr 2, 2004
 *
 * Copyright (c) 2004, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.shortestpath;

import java.util.Map;

/**
 * An interface for classes which calculate the distance between one node and another.
 *
 * @author Joshua O'Madadhain
 */
public interface Distance<N> {
  /**
   * Returns the distance from the <code>source</code> node to the <code>target</code> node. If
   * <code>target</code> is not reachable from <code>source</code>, returns null.
   *
   * @param source the node from which distance is to be measured
   * @param target the node to which distance is to be measured
   * @return the distance from {@code source} to {@code target}
   */
  Number getDistance(N source, N target);

  /**
   * Returns a <code>Map</code> which maps each node in the graph (including the <code>source
   * </code> node) to its distance (represented as a Number) from <code>source</code>. If any node
   * is not reachable from <code>source</code>, no distance is stored for that node.
   *
   * @param source the node from which distances are to be measured
   * @return a {@code Map} of the distances from {@code source} to other nodes in the graph
   */
  Map<N, ? extends Number> getDistanceMap(N source);
}
