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

import com.google.common.graph.Graph;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

/**
 * A radial layout for Tree or Forest graphs.
 *
 * @author Tom Nelson
 */
public class RadialTreeLayout<N> extends TreeLayout<N> {

  protected Map<N, PolarPoint> polarLocations;

  public RadialTreeLayout(Graph<N> g) {
    this(g, DEFAULT_DISTX, DEFAULT_DISTY);
  }

  public RadialTreeLayout(Graph<N> g, int distx) {
    this(g, distx, DEFAULT_DISTY);
  }

  public RadialTreeLayout(Graph<N> g, int distx, int disty) {
    super(g, distx, disty);
  }

  @Override
  protected void buildTree() {
    super.buildTree();
    this.polarLocations = new HashMap<N, PolarPoint>();
    setRadialLocations();
  }

  @Override
  public void setSize(Dimension size) {
    this.size = size;
    buildTree();
  }

  @Override
  protected void setCurrentPositionFor(N node) {
    locations.getUnchecked(node).setLocation(m_currentPoint);
  }

  @Override
  public void setLocation(N node, Point2D location) {
    Point2D c = getCenter();
    Point2D pv = new Point2D.Double(location.getX() - c.getX(), location.getY() - c.getY());
    PolarPoint newLocation = PolarPoint.cartesianToPolar(pv);
    PolarPoint currentLocation = polarLocations.get(node);
    if (currentLocation == null) {
      polarLocations.put(node, newLocation);
    } else {
      currentLocation.setLocation(newLocation);
    }
  }

  /** @return a map from nodes to their locations in polar coordinates. */
  public Map<N, PolarPoint> getPolarLocations() {
    return polarLocations;
  }

  @Override
  public Point2D apply(N node) {
    PolarPoint pp = polarLocations.get(node);
    double centerX = getSize().getWidth() / 2;
    double centerY = getSize().getHeight() / 2;
    Point2D cartesian = PolarPoint.polarToCartesian(pp);
    cartesian.setLocation(cartesian.getX() + centerX, cartesian.getY() + centerY);
    return cartesian;
  }

  private Point2D getMaxXY() {
    double maxx = 0;
    double maxy = 0;
    for (Point2D p : locations.asMap().values()) {
      maxx = Math.max(maxx, p.getX());
      maxy = Math.max(maxy, p.getY());
    }
    return new Point2D.Double(maxx, maxy);
  }

  private void setRadialLocations() {
    Point2D max = getMaxXY();
    double maxx = max.getX();
    double maxy = max.getY();
    maxx = Math.max(maxx, size.width);
    double theta = 2 * Math.PI / maxx;

    double deltaRadius = size.width / 2 / maxy;
    for (Map.Entry<N, Point2D> entry : locations.asMap().entrySet()) {
      N node = entry.getKey();
      Point2D p = entry.getValue();
      PolarPoint polarPoint =
          new PolarPoint(p.getX() * theta, (p.getY() - this.distY) * deltaRadius);
      polarLocations.put(node, polarPoint);
    }
  }
}
