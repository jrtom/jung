/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 9, 2005
 */

package edu.uci.ics.jung.algorithms.layout;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.util.TreeUtils;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@code Layout} implementation that assigns positions to {@code Tree} or {@code Forest} vertices
 * using associations with nested circles ("balloons"). A balloon is nested inside another balloon
 * if the first balloon's subtree is a subtree of the second balloon's subtree.
 *
 * @author Tom Nelson
 */
public class BalloonLayout<V, E> extends TreeLayout<V, E> {

  protected LoadingCache<V, PolarPoint> polarLocations =
      CacheBuilder.newBuilder()
          .build(
              new CacheLoader<V, PolarPoint>() {
                public PolarPoint load(V vertex) {
                  return new PolarPoint();
                }
              });

  protected Map<V, Double> radii = new HashMap<V, Double>();

  /**
   * Creates an instance based on the input forest.
   *
   * @param g the forest on which this layout will operate
   */
  public BalloonLayout(Forest<V, E> g) {
    super(g);
  }

  protected void setRootPolars() {
    List<V> roots = TreeUtils.getRoots(graph);
    if (roots.size() == 1) {
      // its a Tree
      V root = roots.get(0);
      setRootPolar(root);
      setPolars(new ArrayList<V>(graph.getChildren(root)), getCenter(), getSize().width / 2);
    } else if (roots.size() > 1) {
      // its a Forest
      setPolars(roots, getCenter(), getSize().width / 2);
    }
  }

  protected void setRootPolar(V root) {
    PolarPoint pp = new PolarPoint(0, 0);
    Point2D p = getCenter();
    polarLocations.put(root, pp);
    locations.put(root, p);
  }

  protected void setPolars(List<V> kids, Point2D parentLocation, double parentRadius) {

    int childCount = kids.size();
    if (childCount == 0) {
      return;
    }
    // handle the 1-child case with 0 limit on angle.
    double angle = Math.max(0, Math.PI / 2 * (1 - 2.0 / childCount));
    double childRadius = parentRadius * Math.cos(angle) / (1 + Math.cos(angle));
    double radius = parentRadius - childRadius;

    double rand = Math.random();

    for (int i = 0; i < childCount; i++) {
      V child = kids.get(i);
      double theta = i * 2 * Math.PI / childCount + rand;
      radii.put(child, childRadius);

      PolarPoint pp = new PolarPoint(theta, radius);
      polarLocations.put(child, pp);

      Point2D p = PolarPoint.polarToCartesian(pp);
      p.setLocation(p.getX() + parentLocation.getX(), p.getY() + parentLocation.getY());
      locations.put(child, p);
      setPolars(new ArrayList<V>(graph.getChildren(child)), p, childRadius);
    }
  }

  @Override
  public void setSize(Dimension size) {
    this.size = size;
    setRootPolars();
  }

  /**
   * @param v the vertex whose center is to be returned
   * @return the coordinates of {@code v}'s parent, or the center of this layout's area if it's a
   *     root.
   */
  public Point2D getCenter(V v) {
    V parent = graph.getParent(v);
    if (parent == null) {
      return getCenter();
    }
    return locations.getUnchecked(parent);
  }

  @Override
  public void setLocation(V v, Point2D location) {
    Point2D c = getCenter(v);
    Point2D pv = new Point2D.Double(location.getX() - c.getX(), location.getY() - c.getY());
    PolarPoint newLocation = PolarPoint.cartesianToPolar(pv);
    polarLocations.getUnchecked(v).setLocation(newLocation);

    Point2D center = getCenter(v);
    pv.setLocation(pv.getX() + center.getX(), pv.getY() + center.getY());
    locations.put(v, pv);
  }

  @Override
  public Point2D apply(V v) {
    return locations.getUnchecked(v);
  }

  /** @return the radii */
  public Map<V, Double> getRadii() {
    return radii;
  }
}
