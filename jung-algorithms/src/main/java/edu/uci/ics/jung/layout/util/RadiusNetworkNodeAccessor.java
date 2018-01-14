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

import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.Point;
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
public class RadiusNetworkNodeAccessor<N> implements NetworkNodeAccessor<N> {

  private static final Logger log = LoggerFactory.getLogger(RadiusNetworkNodeAccessor.class);
  protected double maxDistance;
  /** Creates an instance with an effectively infinite default maximum distance. */
  public RadiusNetworkNodeAccessor() {
    this(Math.sqrt(Double.MAX_VALUE - 1000));
  }

  /**
   * Creates an instance with the specified default maximum distance.
   *
   * @param maxDistance the maximum distance at which any element can be from a specified location
   *     and still be returned
   */
  public RadiusNetworkNodeAccessor(double maxDistance) {
    this.maxDistance = maxDistance;
  }

  /**
   * @param layoutModel
   * @param p the pick point
   * @return the node associated with location p
   */
  @Override
  public N getNode(LayoutModel<N> layoutModel, Point p) {
    return getNode(layoutModel, p.x, p.y);
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
  public N getNode(LayoutModel<N> layoutModel, double x, double y) {

    double minDistance = maxDistance * maxDistance * maxDistance;
    N closest = null;
    while (true) {
      try {
        for (N node : layoutModel.getLocations().keySet()) {

          Point p = layoutModel.apply(node);
          double dx = p.x - x;
          double dy = p.y - y;
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
