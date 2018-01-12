/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 9, 2005
 */

package edu.uci.ics.jung.layout.algorithms;

import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.layout.model.PolarPoint;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A radial layout for Tree or Forest graphs. Positions vertices in concentric circles with the root
 * at the center
 *
 * @author Tom Nelson
 */
public class RadialTreeLayoutAlgorithm<N> extends TreeLayoutAlgorithm<N> {

  private static final Logger log = LoggerFactory.getLogger(RadialTreeLayoutAlgorithm.class);

  protected Map<N, PolarPoint> polarLocations;

  public RadialTreeLayoutAlgorithm() {
    this(DEFAULT_DISTX, DEFAULT_DISTY);
  }

  public RadialTreeLayoutAlgorithm(int distx) {
    this(distx, DEFAULT_DISTY);
  }

  public RadialTreeLayoutAlgorithm(int distx, int disty) {
    super(distx, disty);
    this.polarLocations = new HashMap<>();
  }

  @Override
  protected void buildTree(LayoutModel<N> layoutModel) {
    super.buildTree(layoutModel);
    setRadialLocations(layoutModel);
    putRadialPointsInModel(layoutModel);
  }

  private void putRadialPointsInModel(LayoutModel<N> layoutModel) {
    for (Map.Entry<N, PolarPoint> entry : polarLocations.entrySet()) {
      PolarPoint polar = entry.getValue();
      layoutModel.set(entry.getKey(), getCartesian(layoutModel, entry.getKey()));
    }
  }

  @Override
  protected void setLocation(LayoutModel<N> layoutModel, N node, Point location) {
    Point c = getCenter(layoutModel);
    Point pv = location.add(-c.x, -c.y);
    PolarPoint newLocation = PolarPoint.cartesianToPolar(pv);
    PolarPoint currentLocation = polarLocations.get(node);
    if (currentLocation == null) {
      polarLocations.put(node, newLocation);
    } else {
      polarLocations.put(node, newLocation);
    }
  }

  /** @return a map from nodes to their locations in polar coordinates. */
  public Map<N, PolarPoint> getPolarLocations() {
    return polarLocations;
  }

  private Point getCartesian(LayoutModel<N> layoutModel, N node) {
    PolarPoint pp = polarLocations.get(node);
    double centerX = layoutModel.getWidth() / 2;
    double centerY = layoutModel.getHeight() / 2;
    Point cartesian = PolarPoint.polarToCartesian(pp);
    cartesian = cartesian.add(centerX, centerY);
    return cartesian;
  }

  private Point getMaxXY(LayoutModel<N> layoutModel) {
    double maxx = 0;
    double maxy = 0;
    Collection<N> nodes = layoutModel.getGraph().nodes();
    for (N node : nodes) {
      Point location = layoutModel.apply(node);
      maxx = Math.max(maxx, location.x);
      maxy = Math.max(maxy, location.y);
    }
    return Point.of(maxx, maxy);
  }

  private void setRadialLocations(LayoutModel<N> layoutModel) {
    int width = layoutModel.getWidth();
    Point max = getMaxXY(layoutModel);
    double maxx = max.x;
    double maxy = max.y;
    maxx = Math.max(maxx, width);
    double theta = 2 * Math.PI / maxx;

    double deltaRadius = width / 2 / maxy;
    for (N node : layoutModel.getGraph().nodes()) {
      Point p = layoutModel.get(node);

      PolarPoint polarPoint = PolarPoint.of(p.x * theta, (p.y - this.distY) * deltaRadius);
      polarLocations.put(node, polarPoint);
    }
  }
}
