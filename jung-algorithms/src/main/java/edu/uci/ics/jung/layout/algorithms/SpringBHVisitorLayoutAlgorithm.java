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
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This subclass of SpringLayoutAlgorithm applies a Barnes-Hut QuadTree optimization during the
 * calculation of node repulsion
 *
 * @author Tom Nelson
 */
public class SpringBHVisitorLayoutAlgorithm<N> extends SpringLayoutAlgorithm<N>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(SpringBHVisitorLayoutAlgorithm.class);

  private BarnesHutQuadTree<N> tree;

  public SpringBHVisitorLayoutAlgorithm() {}

  public SpringBHVisitorLayoutAlgorithm(
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
    tree.rebuild(layoutModel.getGraph().nodes(), layoutModel);
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
        ForceObject<N> nodeForceObject =
            new ForceObject(node, layoutModel.apply(node)) {
              @Override
              protected void addForceFrom(ForceObject other) {

                if (other == null || node == other.getElement()) {
                  return;
                }
                Point p = this.p;
                Point p2 = other.p;
                if (p == null || p2 == null) {
                  return;
                }
                double vx = p.x - p2.x;
                double vy = p.y - p2.y;
                double distanceSq = p.distanceSquared(p2);
                if (distanceSq == 0) {
                  f = f.add(random.nextDouble(), random.nextDouble());
                } else if (distanceSq < repulsion_range_sq) {
                  double factor = 1;
                  f = f.add(factor * vx / distanceSq, factor * vy / distanceSq);
                }
              }
            };
        tree.visit(nodeForceObject);
        Point f = nodeForceObject.f;
        double dlen = f.x * f.x + f.y * f.y;
        if (dlen > 0) {
          dlen = Math.sqrt(dlen) / 2;
          svd.repulsiondx += f.x / dlen;
          svd.repulsiondy += f.y / dlen;
        }
      }
    } catch (ConcurrentModificationException cme) {
      calculateRepulsion();
    }
  }
}
