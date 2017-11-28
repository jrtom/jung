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
package edu.uci.ics.jung.layout.util;

import com.google.common.graph.Graph;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.PointModel;
import java.util.ConcurrentModificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class RadiusNetworkNodeAccessor<N, P> implements NetworkNodeAccessor<N, P> {

  private static final Logger log = LoggerFactory.getLogger(RadiusNetworkNodeAccessor.class);
  protected final Graph<N> graph;
  protected double maxDistance;
  protected PointModel<P> pointModel;
  /** Creates an instance with an effectively infinite default maximum distance. */
  public RadiusNetworkNodeAccessor(Graph<N> graph, PointModel<P> pointModel) {
    this(graph, pointModel, Math.sqrt(Double.MAX_VALUE - 1000));
  }

  /**
   * Creates an instance with the specified default maximum distance.
   *
   * @param maxDistance the maximum distance at which any element can be from a specified location
   *     and still be returned
   */
  public RadiusNetworkNodeAccessor(Graph<N> graph, PointModel<P> pointModel, double maxDistance) {
    this.graph = graph;
    this.pointModel = pointModel;
    this.maxDistance = maxDistance;
  }

  /**
   * @param layoutModel
   * @param x the x coordinate of the pick point
   * @param y the y coordinate of the pick point
   * @return the node associated with (x, y)
   */
  @Override
  public N getNode(LayoutModel<N, P> layoutModel, double x, double y) {
    return getNode(layoutModel, x, y, 0);
  }

  /**
   * @param layoutModel
   * @param p the pick point
   * @return the node associated with location p
   */
  @Override
  public N getNode(LayoutModel<N, P> layoutModel, P p) {
    return getNode(layoutModel, pointModel.getX(p), pointModel.getY(p), pointModel.getZ(p));
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
  public N getNode(LayoutModel<N, P> layoutModel, double x, double y, double z) {
    //    long time = System.currentTimeMillis();
    double minDistance = maxDistance * maxDistance * maxDistance;
    N closest = null;
    while (true) {
      try {
        for (N node : graph.nodes()) {

          P p = layoutModel.apply(node);
          double dx = pointModel.getX(p) - x;
          double dy = pointModel.getY(p) - y;
          double dz = pointModel.getZ(p) - z;
          double dist = dx * dx + dy * dy + dz * dz;
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
