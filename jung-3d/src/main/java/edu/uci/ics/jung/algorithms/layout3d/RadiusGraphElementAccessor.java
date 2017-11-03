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
package edu.uci.ics.jung.algorithms.layout3d;

import java.util.ConcurrentModificationException;
import javax.vecmath.Point3f;

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
public class RadiusGraphElementAccessor<N> implements GraphElementAccessor<N> {

  protected double maxDistance;

  /** Creates an instance with an effectively infinite default maximum distance. */
  public RadiusGraphElementAccessor() {
    this(Math.sqrt(Double.MAX_VALUE - 1000));
  }

  /** Creates an instance with the specified default maximum distance. */
  public RadiusGraphElementAccessor(double maxDistance) {
    this.maxDistance = maxDistance;
  }

  /**
   * Gets the vertex nearest to the location of the (x,y) location selected, within a distance of
   * <tt>maxDistance</tt>. Iterates through all visible vertices and checks their distance from the
   * click. Override this method to provde a more efficient implementation.
   */
  public N getNode(Layout<N, ?> layout, Point3f p) {
    return getNode(layout, p, this.maxDistance);
  }

  /**
   * Gets the vertex nearest to the location of the (x,y) location selected, within a distance of
   * <tt>maxDistance</tt>. Iterates through all visible vertices and checks their distance from the
   * click. Override this method to provde a more efficient implementation.
   *
   * @param maxDistance temporarily overrides member maxDistance
   */
  public N getNode(Layout<N, ?> layout, Point3f p, double maxDistance) {
    double minDistance = maxDistance * maxDistance;
    N closest = null;
    while (true) {
      try {
        for (N v : layout.nodes()) {

          Point3f p2 = layout.apply(v);
          double dist = p.distance(p2);
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
}
