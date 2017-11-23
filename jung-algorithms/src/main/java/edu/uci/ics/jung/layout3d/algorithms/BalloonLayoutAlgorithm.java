/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 9, 2005
 */

package edu.uci.ics.jung.layout3d.algorithms;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.graph.util.TreeUtils;
import edu.uci.ics.jung.layout.algorithms.AbstractLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.PointModel;
import java.util.Collection;
import java.util.List;
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
public class BalloonLayoutAlgorithm<N, P> extends AbstractLayoutAlgorithm<N, P>
    implements Spherical<P> {

  private static final Logger log = LoggerFactory.getLogger(BalloonLayoutAlgorithm.class);

  private Map<P, Integer> sphereLocations = Maps.newHashMap();

  private LayoutModel<N, P> layoutModel;

  int depth = 200;

  private Graph<N> tree;
  /**
   * Creates an instance based on the input Network.
   *
   * @param
   */
  public BalloonLayoutAlgorithm(PointModel<P> pointModel) {
    super(pointModel);
  }

  @Override
  public void visit(LayoutModel<N, P> layoutModel) {
    this.layoutModel = layoutModel;
    if (log.isTraceEnabled()) {
      log.trace("visit {}", layoutModel);
    }
    Graph graph = layoutModel.getGraph();
    this.tree = graph;
    Set<N> roots = TreeUtils.roots(layoutModel.getGraph());

    if (roots.size() == 1) {
      layoutModel.set(roots.iterator().next(), pointModel.newPoint(0, 0, 0));
    }
    buildTrees(roots);
  }

  public Map<P, Integer> getSphereLocations() {
    return sphereLocations;
  }

  protected void breadth(N root) {
    List<N> queue = Lists.newArrayList();
    queue.add(root);
    while (!queue.isEmpty()) {
      N n = queue.remove(0);
      depth *= .9;
      P center = layoutModel.get(n);
      arrangeInSphere(tree.successors(n), center, depth);

      for (N kid : tree.successors(n)) {
        queue.add(kid);
      }
    }
  }

  public static <N> int getDepth(Graph<N> tree, N node) {
    int depth = 0;
    N kid = node;
    while (tree.predecessors(kid).size() > 0) {
      depth++;
      kid = tree.predecessors(kid).iterator().next();
    }
    return depth;
  }

  protected void buildTree(N root) {

    Collection<N> kids = tree.successors(root);
    if (kids.size() > 0) {
      P center = layoutModel.get(root);
      int subDepth = depth / (1 + getDepth(tree, root));
      arrangeInSphere(kids, center, subDepth);
      buildTrees(kids);
    }
  }

  protected void buildTrees(Collection<N> roots) {

    for (N node : roots) {
      buildTree(node);
    }
  }

  protected void arrangeInSphere(Collection<N> nodes, P center, double radius) {

    sphereLocations.put(center, (int) radius);

    int i = 0;
    double offset = 2.0 / nodes.size();
    int count = nodes.size();
    double rnd = 1.0;
    double increment = Math.PI * (3. - Math.sqrt(5.));
    double centerX = pointModel.getX(center);
    double centerY = pointModel.getY(center);
    double centerZ = pointModel.getZ(center);

    for (N node : nodes) {

      double y = ((i * offset) - 1) + (offset / 2);
      double r = Math.sqrt(1 - Math.pow(y, 2));

      double phi = ((i + rnd) % count) * increment;

      double x = Math.cos(phi) * r;
      double z = Math.sin(phi) * r;

      x *= radius;
      y *= radius;
      z *= radius;
      x += centerX;
      y += centerY;
      z += centerZ;
      P coord = layoutModel.apply(node);
      pointModel.setLocation(coord, x, y, z);
      layoutModel.set(node, coord);
      log.debug("placed {} at {}", node, coord);
      i++;
      buildTree(node);
    }
  }
}
