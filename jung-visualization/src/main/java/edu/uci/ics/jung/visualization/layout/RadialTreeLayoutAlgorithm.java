/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 9, 2005
 */

package edu.uci.ics.jung.visualization.layout;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A radial layout for Tree or Forest graphs.
 *
 * @author Tom Nelson
 */
public class RadialTreeLayoutAlgorithm<N, P> extends TreeLayoutAlgorithm<N, P> {

  private static final Logger log = LoggerFactory.getLogger(RadialTreeLayoutAlgorithm.class);

  protected Map<N, PolarPoint> polarLocations;
  //  protected LayoutModel<N, P> layoutModel;

  public RadialTreeLayoutAlgorithm(DomainModel<P> domainModel) {
    this(domainModel, DEFAULT_DISTX, DEFAULT_DISTY);
  }

  public RadialTreeLayoutAlgorithm(DomainModel<P> domainModel, int distx) {
    this(domainModel, distx, DEFAULT_DISTY);
  }

  public RadialTreeLayoutAlgorithm(DomainModel<P> domainModel, int distx, int disty) {
    super(domainModel, distx, disty);
    this.polarLocations = new HashMap<N, PolarPoint>();
  }

  @Override
  protected void buildTree(LayoutModel<N, P> layoutModel) {
    super.buildTree(layoutModel);
    //    this.polarLocations = new HashMap<N, PolarPoint>();
    setRadialLocations(layoutModel);
    putRadialPointsInModel(layoutModel);
  }

  private void putRadialPointsInModel(LayoutModel<N, P> layoutModel) {
    for (Map.Entry<N, PolarPoint> entry : polarLocations.entrySet()) {
      PolarPoint polar = entry.getValue();
      layoutModel.set(entry.getKey(), getCartesian(layoutModel, entry.getKey()));
      //              PolarPoint.polarToCartesian(domainModel, polar));
    }
  }

  @Override
  protected void setLocation(LayoutModel<N, P> layoutModel, N node, P location) {
    P c = getCenter(layoutModel);
    P pv =
        domainModel.newPoint(
            domainModel.getX(location) - domainModel.getX(c),
            domainModel.getY(location) - domainModel.getY(c));
    PolarPoint newLocation = PolarPoint.cartesianToPolar(domainModel, pv);
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

  //  @Override
  private P getCartesian(LayoutModel<N, P> layoutModel, N node) {
    PolarPoint pp = polarLocations.get(node);
    double centerX = layoutModel.getWidth() / 2;
    double centerY = layoutModel.getHeight() / 2;
    P cartesian = PolarPoint.polarToCartesian(domainModel, pp);
    domainModel.setLocation(
        cartesian, domainModel.getX(cartesian) + centerX, domainModel.getY(cartesian) + centerY);
    return cartesian;
  }

  private P getMaxXY(LayoutModel<N, P> layoutModel) {
    double maxx = 0;
    double maxy = 0;
    Collection<N> nodes = layoutModel.getGraph().nodes();
    for (N node : nodes) {
      P location = layoutModel.apply(node);
      maxx = Math.max(maxx, domainModel.getX(location));
      maxy = Math.max(maxy, domainModel.getY(location));
    }
    return domainModel.newPoint(maxx, maxy);
  }

  private void setRadialLocations(LayoutModel<N, P> layoutModel) {
    int width = layoutModel.getWidth();
    P max = getMaxXY(layoutModel);
    double maxx = domainModel.getX(max);
    double maxy = domainModel.getY(max);
    maxx = Math.max(maxx, width);
    double theta = 2 * Math.PI / maxx;

    double deltaRadius = width / 2 / maxy;
    for (N node : layoutModel.getGraph().nodes()) {
      P p = layoutModel.get(node);

      PolarPoint polarPoint =
          new PolarPoint(
              domainModel.getX(p) * theta, (domainModel.getY(p) - this.distY) * deltaRadius);
      polarLocations.put(node, polarPoint);
    }
  }
}
