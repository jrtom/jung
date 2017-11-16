/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 *
 * Created on Apr 12, 2005
 */
package edu.uci.ics.jung.layout3d.util;

import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.PointModel;
import java.util.ConcurrentModificationException;

/**
 * Simple implementation of PickSupport that returns the vertex or edge that is closest to the
 * specified location. This implementation provides the same picking options that were available in
 * previous versions of
 *
 * <p>No element will be returned that is farther away than the specified maximum distance.
 *
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */
public class RadiusGraphElementAccessor<N, P> implements GraphElementAccessor<N, P> {

  protected double maxDistance;

  private PointModel<P> pointModel;

  /** Creates an instance with an effectively infinite default maximum distance. */
  public RadiusGraphElementAccessor(PointModel<P> pointModel) {
    this(pointModel, Math.sqrt(Double.MAX_VALUE - 1000));
  }

  /** Creates an instance with the specified default maximum distance. */
  public RadiusGraphElementAccessor(PointModel<P> pointModel, double maxDistance) {
    this.pointModel = pointModel;
    this.maxDistance = maxDistance;
  }

  /**
   * Gets the vertex nearest to the location of the (x,y) location selected, within a distance of
   * <tt>maxDistance</tt>. Iterates through all visible vertices and checks their distance from the
   * click. Override this method to provde a more efficient implementation.
   */
  public N getNode(LayoutModel<N, P> layoutModel, P p) {
    return getNode(layoutModel, p, this.maxDistance);
  }

  /**
   * Gets the vertex nearest to the location of the (x,y) location selected, within a distance of
   * <tt>maxDistance</tt>. Iterates through all visible vertices and checks their distance from the
   * click. Override this method to provde a more efficient implementation.
   *
   * @param maxDistance temporarily overrides member maxDistance
   */
  public N getNode(LayoutModel<N, P> layoutModel, P p, double maxDistance) {
    double minDistance = maxDistance * maxDistance;
    N closest = null;
    while (true) {
      try {
        for (N v : layoutModel.getGraph().nodes()) {

          P p2 = layoutModel.apply(v);
          double dist = Math.sqrt(distanceSquared(p, p2));
          //pointModel.distanceSquared(p, p2));
          //                  p.distance(p2);
          if (dist < minDistance) {
            minDistance = dist;
            closest = v;
          }
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    return closest;
  }

  /**
   * @param from
   * @param to
   * @return squared distance between points
   */
  private double distanceSquared(P from, P to) {
    double deltaX = pointModel.getX(to) - pointModel.getX(from);
    double deltaY = pointModel.getY(to) - pointModel.getY(from);
    double deltaZ = pointModel.getZ(to) - pointModel.getZ(from);
    return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
  }
}
