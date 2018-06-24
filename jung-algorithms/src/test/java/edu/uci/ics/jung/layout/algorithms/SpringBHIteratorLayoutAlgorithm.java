/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.layout.algorithms;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.layout.spatial.BarnesHutQuadTree;
import edu.uci.ics.jung.layout.spatial.ForceObject;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This subclass of SpringLayoutAlgorithm applies a Barnes-Hut QuadTree optimization during the
 * calculation of node repulsion. It uses an Iterator over the reduced set of nodes for comparison.
 * As it is not as performant as the Visitor model, it is in the test module for comparison of
 * output between the implementations.
 *
 * @author Tom Nelson
 */
public class SpringBHIteratorLayoutAlgorithm<N> extends SpringLayoutAlgorithm<N>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(SpringBHIteratorLayoutAlgorithm.class);

  private BarnesHutQuadTree<N> tree;

  public SpringBHIteratorLayoutAlgorithm() {}

  public SpringBHIteratorLayoutAlgorithm(
      Function<? super EndpointPair<N>, Integer> length_function) {
    super(length_function);
  }

  @Override
  public void visit(LayoutModel<N> layoutModel) {
    super.visit(layoutModel);
    tree = new BarnesHutQuadTree(layoutModel.getWidth(), layoutModel.getHeight());
  }

  @Override
  public void step() {
    tree.rebuild(layoutModel.getLocations());
    super.step();
  }

  protected void calculateRepulsion() {
    Graph<N> graph = layoutModel.getGraph();

    try {
      for (N node : graph.nodes()) {
        if (layoutModel.isLocked(node)) {
          continue;
        }

        SpringNodeData svd = springNodeData.getUnchecked(node);
        if (svd == null) {
          continue;
        }
        double dx = 0, dy = 0;

        ForceObject<N> nodeForceObject = new ForceObject<>(node, layoutModel.apply(node));
        Iterator<ForceObject<N>> forceObjectIterator =
            new ForceObjectIterator<>(tree, nodeForceObject);
        while (forceObjectIterator.hasNext()) {
          ForceObject<N> nextForceObject = forceObjectIterator.next();
          if (nextForceObject == null || node == nextForceObject.getElement()) {
            continue;
          }
          Point p = nodeForceObject.p;
          Point p2 = nextForceObject.p;
          if (p == null || p2 == null) {
            continue;
          }
          double vx = p.x - p2.x;
          double vy = p.y - p2.y;
          double distanceSq = p.distanceSquared(p2);
          if (distanceSq == 0) {
            dx += random.nextDouble();
            dy += random.nextDouble();
          } else if (distanceSq < repulsion_range_sq) {
            double factor = 1;
            dx += factor * vx / distanceSq;
            dy += factor * vy / distanceSq;
          }
        }

        double dlen = dx * dx + dy * dy;
        if (dlen > 0) {
          dlen = Math.sqrt(dlen) / 2;
          svd.repulsiondx += dx / dlen;
          svd.repulsiondy += dy / dlen;
        }
      }
    } catch (ConcurrentModificationException cme) {
      calculateRepulsion();
    }
  }
}
