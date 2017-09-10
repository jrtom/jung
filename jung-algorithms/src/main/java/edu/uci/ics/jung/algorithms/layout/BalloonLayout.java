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
import com.google.common.collect.Iterables;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.graph.util.TreeUtils;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A {@code Layout} implementation that assigns positions to {@code Tree} or {@code Network} nodes
 * using associations with nested circles ("balloons"). A balloon is nested inside another balloon
 * if the first balloon's subtree is a subtree of the second balloon's subtree.
 *
 * @author Tom Nelson
 */
public class BalloonLayout<N> extends TreeLayout<N> {

  protected LoadingCache<N, PolarPoint> polarLocations =
      CacheBuilder.newBuilder()
          .build(
              new CacheLoader<N, PolarPoint>() {
                public PolarPoint load(N node) {
                  return new PolarPoint();
                }
              });

  protected Map<N, Double> radii = new HashMap<N, Double>();

  /**
   * Creates an instance based on the input Network.
   *
   * @param g the Network on which this layout will operate
   */
  public BalloonLayout(Graph<N> g) {
    super(g);
  }

  protected void setRootPolars() {
    Set<N> roots = TreeUtils.roots(graph);
    if (roots.size() == 1) {
      // its a Tree
      N root = Iterables.getOnlyElement(roots);
      setRootPolar(root);
      setPolars(graph.successors(root), getCenter(), getSize().width / 2);
    } else if (roots.size() > 1) {
      // its a Network
      setPolars(roots, getCenter(), getSize().width / 2);
    }
  }

  protected void setRootPolar(N root) {
    PolarPoint pp = new PolarPoint(0, 0);
    Point2D p = getCenter();
    polarLocations.put(root, pp);
    locations.put(root, p);
  }

  protected void setPolars(Set<N> kids, Point2D parentLocation, double parentRadius) {

    int childCount = kids.size();
    if (childCount == 0) {
      return;
    }
    // handle the 1-child case with 0 limit on angle.
    double angle = Math.max(0, Math.PI / 2 * (1 - 2.0 / childCount));
    double childRadius = parentRadius * Math.cos(angle) / (1 + Math.cos(angle));
    double radius = parentRadius - childRadius;

    double rand = Math.random();

    int i = 0;
    for (N child : kids) {
      double theta = i++ * 2 * Math.PI / childCount + rand;
      radii.put(child, childRadius);

      PolarPoint pp = new PolarPoint(theta, radius);
      polarLocations.put(child, pp);

      Point2D p = PolarPoint.polarToCartesian(pp);
      p.setLocation(p.getX() + parentLocation.getX(), p.getY() + parentLocation.getY());
      locations.put(child, p);

      setPolars(graph.successors(child), p, childRadius);
    }
  }

  @Override
  public void setSize(Dimension size) {
    this.size = size;
    setRootPolars();
  }

  /**
   * @param v the node whose center is to be returned
   * @return the coordinates of {@code node}'s parent, or the center of this layout's area if it's a
   *     root.
   */
  public Point2D getCenter(N node) {
    N parent = Iterables.getOnlyElement(graph.predecessors(node), null);
    if (parent == null) {
      return getCenter();
    }
    return locations.getUnchecked(parent);
  }

  @Override
  public void setLocation(N node, Point2D location) {
    Point2D c = getCenter(node);
    Point2D pv = new Point2D.Double(location.getX() - c.getX(), location.getY() - c.getY());
    PolarPoint newLocation = PolarPoint.cartesianToPolar(pv);
    polarLocations.getUnchecked(node).setLocation(newLocation);

    Point2D center = getCenter(node);
    pv.setLocation(pv.getX() + center.getX(), pv.getY() + center.getY());
    locations.put(node, pv);
  }

  @Override
  public Point2D apply(N node) {
    return locations.getUnchecked(node);
  }

  /** @return the radii */
  public Map<N, Double> getRadii() {
    return radii;
  }
}
