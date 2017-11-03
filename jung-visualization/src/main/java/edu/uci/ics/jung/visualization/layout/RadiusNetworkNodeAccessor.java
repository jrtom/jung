/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 *
 * Created on Apr 12, 2005
 */
package edu.uci.ics.jung.visualization.layout;

import com.google.common.graph.Graph;
import java.util.ConcurrentModificationException;

/**
 * Simple implementation of PickSupport that returns the node or edge that is closest to the
 * specified location. This implementation provides the same picking options that were available in
 * previous versions of
 *
 * <p>No element will be returned that is farther away than the specified maximum distance.
 *
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */
public class RadiusNetworkNodeAccessor<N, P> implements NetworkNodeAccessor<N> {
  protected final Graph<N> graph;
  protected final LayoutModel<N, P> layoutModel;
  protected double maxDistance;
  protected DomainModel<P> domainModel;
  /** Creates an instance with an effectively infinite default maximum distance. */
  public RadiusNetworkNodeAccessor(
      Graph<N> graph, LayoutModel<N, P> layoutModel, DomainModel<P> domainModel) {
    this(graph, layoutModel, domainModel, Math.sqrt(Double.MAX_VALUE - 1000));
  }

  /**
   * Creates an instance with the specified default maximum distance.
   *
   * @param maxDistance the maximum distance at which any element can be from a specified location
   *     and still be returned
   */
  public RadiusNetworkNodeAccessor(
      Graph<N> graph,
      LayoutModel<N, P> layoutModel,
      DomainModel<P> domainModel,
      double maxDistance) {
    this.graph = graph;
    this.layoutModel = layoutModel;
    this.domainModel = domainModel;
    this.maxDistance = maxDistance;
  }

  /**
   * Gets the node nearest to the location of the (x,y) location selected, within a distance of
   * {@code this.maxDistance}. Iterates through all visible nodes and checks their distance from the
   * location. Override this method to provide a more efficient implementation.
   *
   * @param x the x coordinate of the location
   * @param y the y coordinate of the location
   * @return a node which is associated with the location {@code (x,y)} as given by {@code layout}
   */
  @Override
  public N getNode(double x, double y) {
    double minDistance = maxDistance * maxDistance;
    N closest = null;
    while (true) {
      try {
        for (N node : graph.nodes()) {

          P p = layoutModel.apply(node);
          double dx = domainModel.getX(p) - x;
          double dy = domainModel.getY(p) - y;
          double dist = dx * dx + dy * dy;
          if (dist < minDistance) {
            minDistance = dist;
            closest = node;
          }
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    return closest;
  }
}
