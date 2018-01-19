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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.graph.util.TreeUtils;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.layout.model.PolarPoint;
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
public class BalloonLayoutAlgorithm<N> extends TreeLayoutAlgorithm<N> {

  private static final Logger log = LoggerFactory.getLogger(BalloonLayoutAlgorithm.class);

  protected LoadingCache<N, PolarPoint> polarLocations =
      CacheBuilder.newBuilder()
          .build(
              new CacheLoader<N, PolarPoint>() {
                public PolarPoint load(N node) {
                  return PolarPoint.ORIGIN;
                }
              });

  protected Map<N, Double> radii = new HashMap<N, Double>();

  @Override
  public void visit(LayoutModel<N> layoutModel) {
    super.visit(layoutModel);
    if (log.isTraceEnabled()) {
      log.trace("visit {}", layoutModel);
    }
    super.visit(layoutModel);
    setRootPolars(layoutModel);
  }

  protected void setRootPolars(LayoutModel<N> layoutModel) {
    Graph<N> graph = layoutModel.getGraph();
    Set<N> roots = TreeUtils.roots(graph);
    int width = layoutModel.getWidth();
    if (roots.size() == 1) {
      // its a Tree
      N root = Iterables.getOnlyElement(roots);
      setRootPolar(layoutModel, root);
      setPolars(
          layoutModel,
          graph.successors(root),
          getCenter(layoutModel),
          getCenter(layoutModel),
          width / 2);
    } else if (roots.size() > 1) {
      // its a Network
      setPolars(layoutModel, roots, getCenter(layoutModel), getCenter(layoutModel), width / 2);
    }
  }

  protected void setRootPolar(LayoutModel<N> layoutModel, N root) {
    PolarPoint pp = PolarPoint.ORIGIN;
    Point p = getCenter(layoutModel);
    polarLocations.put(root, pp);
    layoutModel.set(root, p);
  }

  protected void setPolars(
      LayoutModel<N> layoutModel,
      Set<N> kids,
      Point parentLocation,
      Point grandparentLocation,
      double parentRadius) {

    int childCount = kids.size();
    if (childCount == 0) {
      return;
    }
    // handle the 1-child case with 0 limit on angle.
    double angle = Math.max(0, Math.PI / 2 * (1 - 2.0 / childCount));
    double childRadius = parentRadius * Math.cos(angle) / (1 + Math.cos(angle));
    double radius = parentRadius - childRadius;

    double angleToParent =
        Math.atan2(
            parentLocation.y - grandparentLocation.y, grandparentLocation.x - parentLocation.x);

    // the angle between the child nodes placed equally on a circle
    double angleBetweenKids = 2 * Math.PI / childCount;
    // how much to offset each angle to bisect the angle between 2 child nodes
    double offset = angleBetweenKids / 2 - angleToParent;

    int i = 0;
    for (N child : kids) {

      // increment for each child. include the offset to space edge to parent
      // in between 2 edges to children
      double theta = i++ * angleBetweenKids + offset;

      // if there's only one child, offset for more pleasing effect
      if (kids.size() == 1) {
        theta += Math.PI / 4;
      }

      radii.put(child, childRadius);

      PolarPoint pp = PolarPoint.of(theta, radius);
      polarLocations.put(child, pp);

      Point p = PolarPoint.polarToCartesian(pp);
      p = p.add(parentLocation.x, parentLocation.y);

      layoutModel.set(child, p);
      setPolars(
          layoutModel, layoutModel.getGraph().successors(child), p, parentLocation, childRadius);
    }
  }

  /**
   * @param node the node whose center is to be returned
   * @return the coordinates of {@code node}'s parent, or the center of this layout's area if it's a
   *     root.
   */
  public Point getCenter(LayoutModel<N> layoutModel, N node) {
    Graph<N> graph = layoutModel.getGraph();
    N parent = Iterables.getOnlyElement(graph.predecessors(node), null);
    if (parent == null) {
      return getCenter(layoutModel);
    }
    return layoutModel.get(parent);
  }

  private Point getCartesian(LayoutModel<N> layoutModel, N node) {
    PolarPoint pp = polarLocations.getUnchecked(node);
    double centerX = layoutModel.getWidth() / 2;
    double centerY = layoutModel.getHeight() / 2;
    Point cartesian = PolarPoint.polarToCartesian(pp);
    cartesian = cartesian.add(centerX, centerY);
    return cartesian;
  }

  /** @return the radii */
  public Map<N, Double> getRadii() {
    return radii;
  }
}
