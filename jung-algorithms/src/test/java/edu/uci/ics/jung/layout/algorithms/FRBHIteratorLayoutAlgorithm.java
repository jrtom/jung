/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.layout.algorithms;

import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.layout.spatial.BarnesHutQuadTree;
import edu.uci.ics.jung.layout.spatial.ForceObject;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This subclass of FRLayoutAlgorithm applies a Barnes-Hut QuadTree optimization during the
 * calculation of node repulsion. It uses an Iterator over the reduced set of nodes for comparison.
 * As it is not as performant as the Visitor model, it is in the test module for comparison of
 * output between the implementations
 *
 * @author Tom Nelson
 */
public class FRBHIteratorLayoutAlgorithm<N> extends FRLayoutAlgorithm<N>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(FRBHIteratorLayoutAlgorithm.class);

  private BarnesHutQuadTree<N> tree;

  @Override
  public void visit(LayoutModel<N> layoutModel) {
    super.visit(layoutModel);
    tree = new BarnesHutQuadTree(layoutModel.getWidth(), layoutModel.getHeight());
  }

  @Override
  public synchronized void step() {
    tree.rebuild(layoutModel.getLocations());
    super.step();
  }

  protected void calcRepulsion(N node1) {
    Point fvd1 = getFRData(node1);
    if (fvd1 == null) {
      return;
    }
    log.trace("fvd1 for {} starts as {}", node1, fvd1);
    frNodeData.put(node1, Point.ORIGIN);
    ForceObject<N> nodeForceObject = new ForceObject<>(node1, layoutModel.apply(node1));
    Iterator<ForceObject<N>> forceObjectIterator = new ForceObjectIterator<>(tree, nodeForceObject);
    try {
      while (forceObjectIterator.hasNext()) {
        ForceObject<N> nextForceObject = forceObjectIterator.next();
        if (nextForceObject != null && !nextForceObject.equals(nodeForceObject)) {
          if (log.isTraceEnabled()) {
            log.trace(
                "Iter {} at {} visiting {} at {}",
                nextForceObject.getElement(),
                nextForceObject.p,
                nodeForceObject.getElement(),
                nodeForceObject.p);
          }
          fvd1 = getFRData(node1);

          Point p1 = nodeForceObject.p;
          Point p2 = nextForceObject.p;
          if (p1 == null || p2 == null) {
            continue;
          }
          double xDelta = p1.x - p2.x;
          double yDelta = p1.y - p2.y;
          log.trace("xDelta,yDelta:{},{}", xDelta, yDelta);

          double deltaLength = Math.max(EPSILON, Math.sqrt((xDelta * xDelta) + (yDelta * yDelta)));
          log.trace("deltaLength:{}", deltaLength);

          double force = (repulsion_constant * repulsion_constant) / deltaLength;
          log.trace("force:{}", force);

          if (Double.isNaN(force)) {
            throw new RuntimeException(
                "Unexpected mathematical result in FRLayout:calcPositions [repulsion]");
          }
          if (log.isTraceEnabled()) {
            log.trace("frNodeData for {} went from {}...", node1, frNodeData.getUnchecked(node1));
          }
          fvd1 = fvd1.add((xDelta / deltaLength) * force, (yDelta / deltaLength) * force);
          frNodeData.put(node1, fvd1);
          log.trace("...to {}", frNodeData.getUnchecked(node1));
        }
      }
    } catch (ConcurrentModificationException cme) {
      calcRepulsion(node1);
    }
  }
}
