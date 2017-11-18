/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.layout3d.algorithms;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.layout.algorithms.AbstractIterativeLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.PointModel;
import java.util.ConcurrentModificationException;
import java.util.function.Function;

/**
 * The SpringLayout package represents a visualization of a set of nodes. The SpringLayout, which is
 * initialized with a Graph, assigns X/Y locations to each node. When called <code>relax()</code>,
 * the SpringLayout moves the visualization forward one step.
 *
 * @author Danyel Fisher
 * @author Joshua O'Madadhain
 * @author Tom Nelson
 */
public class SpringLayoutAlgorithm<N, P> extends AbstractIterativeLayoutAlgorithm<N, P>
    implements IterativeContext {

  protected double stretch = 0.70;
  protected Function<? super EndpointPair<N>, Integer> lengthFunction;
  protected int repulsion_range_sq = 100 * 100;
  protected double force_multiplier = 1.0 / 3.0;

  protected LoadingCache<N, SpringNodeData> springNodeData =
      CacheBuilder.newBuilder().build(CacheLoader.from(() -> new SpringNodeData()));

  public SpringLayoutAlgorithm(PointModel<P> pointModel) {
    this(pointModel, n -> 30);
  }

  public SpringLayoutAlgorithm(
      PointModel<P> pointModel, Function<? super EndpointPair<N>, Integer> length_function) {
    super(pointModel);
    this.lengthFunction = length_function;
  }

  /** @return the current value for the stretch parameter */
  public double getStretch() {
    return stretch;
  }

  public void setStretch(double stretch) {
    this.stretch = stretch;
  }

  public int getRepulsionRange() {
    return (int) (Math.sqrt(repulsion_range_sq));
  }

  public void setRepulsionRange(int range) {
    this.repulsion_range_sq = range * range;
  }

  public double getForceMultiplier() {
    return force_multiplier;
  }

  public void setForceMultiplier(double force) {
    this.force_multiplier = force;
  }

  public void initialize() {}

  public void step() {
    Graph<N> graph = layoutModel.getGraph();
    try {
      for (N node : graph.nodes()) {
        SpringNodeData svd = springNodeData.getUnchecked(node);
        if (svd == null) {
          continue;
        }
        svd.dx /= 4;
        svd.dy /= 4;
        svd.dz /= 4;
        svd.edgedx = svd.edgedy = svd.edgedz = 0;
        svd.repulsiondx = svd.repulsiondy = svd.repulsiondz = 0;
      }
    } catch (ConcurrentModificationException cme) {
      step();
    }

    relaxEdges();
    calculateRepulsion();
    moveNodes();
  }

  protected void relaxEdges() {
    Graph<N> graph = layoutModel.getGraph();
    try {
      for (EndpointPair<N> endpoints : layoutModel.getGraph().edges()) {
        N node1 = endpoints.nodeU();
        N node2 = endpoints.nodeV();

        P p1 = this.layoutModel.get(node1);
        P p2 = this.layoutModel.get(node2);
        if (p1 == null || p2 == null) {
          continue;
        }
        double vx = pointModel.getX(p1) - pointModel.getX(p2);
        double vy = pointModel.getY(p1) - pointModel.getY(p2);
        double vz = pointModel.getZ(p1) - pointModel.getZ(p2);
        double len = Math.sqrt(vx * vx + vy * vy + vz * vz);

        double desiredLen = lengthFunction.apply(endpoints);

        // round from zero, if needed [zero would be Bad.].
        len = (len == 0) ? .0001 : len;

        double f = force_multiplier * (desiredLen - len) / len;

        f = f * Math.pow(stretch, (graph.degree(node1) + graph.degree(node2) - 2));

        // the actual movement distance 'dx' is the force multiplied by the
        // distance to go.
        double dx = f * vx;
        double dy = f * vy;
        double dz = f * vz;
        SpringNodeData v1D, v2D;
        v1D = springNodeData.getUnchecked(node1);
        v2D = springNodeData.getUnchecked(node2);

        v1D.edgedx += dx;
        v1D.edgedy += dy;
        v1D.edgedz += dz;
        v2D.edgedx += -dx;
        v2D.edgedy += -dy;
        v2D.edgedz += -dz;
      }
    } catch (ConcurrentModificationException cme) {
      relaxEdges();
    }
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
        double dx = 0, dy = 0, dz = 0;

        for (N node2 : graph.nodes()) {
          if (node == node2) {
            continue;
          }
          P p = layoutModel.apply(node);
          P p2 = layoutModel.apply(node2);
          if (p == null || p2 == null) {
            continue;
          }
          double vx = pointModel.getX(p) - pointModel.getX(p2);
          double vy = pointModel.getY(p) - pointModel.getY(p2);
          double vz = pointModel.getZ(p) - pointModel.getZ(p2);
          double distanceSq = pointModel.distanceSquared(p, p2);
          if (distanceSq == 0) {
            dx += Math.random();
            dy += Math.random();
            dz += Math.random();
          } else if (distanceSq < repulsion_range_sq) {
            double factor = 1;
            dx += factor * vx / distanceSq;
            dy += factor * vy / distanceSq;
            dz += factor * vz / distanceSq;
          }
        }
        double dlen = dx * dx + dy * dy + dz * dz;
        if (dlen > 0) {
          dlen = Math.sqrt(dlen) / 2;
          svd.repulsiondx += dx / dlen;
          svd.repulsiondy += dy / dlen;
          svd.repulsiondz += dz / dlen;
        }
      }
    } catch (ConcurrentModificationException cme) {
      calculateRepulsion();
    }
  }

  protected void moveNodes() {
    Graph<N> graph = layoutModel.getGraph();

    synchronized (layoutModel) {
      try {
        for (N node : graph.nodes()) {
          if (layoutModel.isLocked(node)) {
            continue;
          }
          SpringNodeData vd = springNodeData.getUnchecked(node);
          if (vd == null) {
            continue;
          }
          P xyd = layoutModel.apply(node);

          vd.dx += vd.repulsiondx + vd.edgedx;
          vd.dy += vd.repulsiondy + vd.edgedy;
          vd.dz += vd.repulsiondz + vd.edgedz;

          // keeps nodes from moving any faster than 5 per time unit
          pointModel.setLocation(
              xyd,
              pointModel.getX(xyd) + Math.max(-5, Math.min(5, vd.dx)),
              pointModel.getY(xyd) + Math.max(-5, Math.min(5, vd.dy)),
              pointModel.getZ(xyd) + Math.max(-5, Math.min(5, vd.dz)));

          int radiusX = layoutModel.getWidth() / 2;
          int radiusY = layoutModel.getHeight() / 2;
          int radiusZ = layoutModel.getDepth() / 2;

          if (pointModel.getX(xyd) < -radiusX) {
            pointModel.setLocation(xyd, -radiusX, pointModel.getY(xyd), pointModel.getZ(xyd));
          } else if (pointModel.getX(xyd) > radiusX) {
            pointModel.setLocation(xyd, radiusX, pointModel.getY(xyd), pointModel.getZ(xyd));
          }
          if (pointModel.getY(xyd) < -radiusY) {
            pointModel.setLocation(xyd, pointModel.getX(xyd), -radiusY, pointModel.getZ(xyd));
          } else if (pointModel.getY(xyd) > radiusY) {
            pointModel.setLocation(xyd, pointModel.getX(xyd), radiusY, pointModel.getZ(xyd));
          }
          if (pointModel.getZ(xyd) < -radiusZ) {
            pointModel.setLocation(xyd, pointModel.getX(xyd), pointModel.getY(xyd), -radiusZ);
          } else if (pointModel.getZ(xyd) > radiusZ) {
            pointModel.setLocation(xyd, pointModel.getX(xyd), pointModel.getY(xyd), radiusZ);
          }
          // after the bounds have been honored above, really set the location
          // in the layout model
          layoutModel.set(node, xyd);
        }
      } catch (ConcurrentModificationException cme) {
        moveNodes();
      }
    }
  }

  @Override
  public void reset() {}

  protected static class SpringNodeData {
    protected double edgedx;
    protected double edgedy;
    protected double edgedz;
    protected double repulsiondx;
    protected double repulsiondy;
    protected double repulsiondz;

    /** movement speed, x */
    protected double dx;

    /** movement speed, y */
    protected double dy;

    protected double dz;
  }

  public boolean done() {
    return false;
  }
}
