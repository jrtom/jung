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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.graph.util.TreeUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code Layout} implementation that assigns positions to {@code Tree} or {@code Network} nodes
 * using associations with nested circles ("balloons"). A balloon is nested inside another balloon
 * if the first balloon's subtree is a subtree of the second balloon's subtree.
 *
 * @author Tom Nelson
 */
public class BalloonLayoutAlgorithm<N, P> extends TreeLayoutAlgorithm<N, P> {

  private static final Logger log = LoggerFactory.getLogger(BalloonLayoutAlgorithm.class);

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
   * @param
   */
  public BalloonLayoutAlgorithm(DomainModel<P> domainModel) {
    super(domainModel);
  }

  @Override
  public void visit(LayoutModel<N, P> layoutModel) {
    if (log.isTraceEnabled()) {
      log.trace("visit {}", layoutModel);
    }
    super.visit(layoutModel);
    setRootPolars(layoutModel);
  }

  private void putRadialPointsInModel(LayoutModel<N, P> layoutModel) {
    for (Map.Entry<N, PolarPoint> entry : polarLocations.asMap().entrySet()) {
      PolarPoint polar = entry.getValue();
      layoutModel.set(entry.getKey(), getCartesian(layoutModel, entry.getKey()));
    }
  }

  protected void setRootPolars(LayoutModel<N, P> layoutModel) {
    Graph<N> graph = layoutModel.getGraph();
    Set<N> roots = TreeUtils.roots(graph);
    int width = layoutModel.getWidth();
    if (roots.size() == 1) {
      // its a Tree
      N root = Iterables.getOnlyElement(roots);
      setRootPolar(layoutModel, root);
      setPolars(layoutModel, graph.successors(root), getCenter(layoutModel), width / 2);
    } else if (roots.size() > 1) {
      // its a Network
      setPolars(layoutModel, roots, getCenter(layoutModel), width / 2);
    }
  }

  protected void setRootPolar(LayoutModel<N, P> layoutModel, N root) {
    PolarPoint pp = new PolarPoint(0, 0);
    P p = getCenter(layoutModel);
    polarLocations.put(root, pp);
    layoutModel.set(root, p);
  }

  protected void setPolars(
      LayoutModel<N, P> layoutModel, Set<N> kids, P parentLocation, double parentRadius) {

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

      P p = PolarPoint.polarToCartesian(domainModel, pp);
      domainModel.setLocation(
          p,
          domainModel.getX(p) + domainModel.getX(parentLocation),
          domainModel.getY(p) + domainModel.getY(parentLocation));
      layoutModel.set(child, p);

      setPolars(layoutModel, layoutModel.getGraph().successors(child), p, childRadius);
    }
  }

  /**
   * @param node the node whose center is to be returned
   * @return the coordinates of {@code node}'s parent, or the center of this layout's area if it's a
   *     root.
   */
  public P getCenter(LayoutModel<N, P> layoutModel, N node) {
    Graph<N> graph = layoutModel.getGraph();
    N parent = Iterables.getOnlyElement(graph.predecessors(node), null);
    if (parent == null) {
      return getCenter(layoutModel);
    }
    return layoutModel.get(parent);
  }

  private P getCartesian(LayoutModel<N, P> layoutModel, N node) {
    PolarPoint pp = polarLocations.getUnchecked(node);
    double centerX = layoutModel.getWidth() / 2;
    double centerY = layoutModel.getHeight() / 2;
    P cartesian = PolarPoint.polarToCartesian(domainModel, pp);
    domainModel.setLocation(
        cartesian, domainModel.getX(cartesian) + centerX, domainModel.getY(cartesian) + centerY);
    return cartesian;
  }

  //  @Override
  protected void setLocation(LayoutModel<N, P> layoutModel, N node, P location) {
    P c = getCenter(layoutModel, node);
    P pv =
        domainModel.newPoint(
            domainModel.getX(location) - domainModel.getX(c),
            domainModel.getY(location) - domainModel.getY(c));
    PolarPoint newLocation =
        PolarPoint.cartesianToPolar(domainModel, domainModel.getX(pv), domainModel.getY(pv));
    polarLocations.getUnchecked(node).setLocation(newLocation);

    P center = getCenter(layoutModel, node);
    domainModel.setLocation(
        pv,
        domainModel.getX(pv) + domainModel.getX(center),
        domainModel.getY(pv) + domainModel.getY(center));
    layoutModel.set(node, pv);
  }

  /** @return the radii */
  public Map<N, Double> getRadii() {
    return radii;
  }
}
