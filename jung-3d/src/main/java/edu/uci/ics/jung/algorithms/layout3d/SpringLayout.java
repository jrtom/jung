/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 */
package edu.uci.ics.jung.algorithms.layout3d;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ConcurrentModificationException;
import java.util.function.Function;
import javax.media.j3d.BoundingSphere;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

/**
 * The SpringLayout package represents a visualization of a set of nodes. The SpringLayout, which is
 * initialized with a Graph, assigns X/Y locations to each node. When called <code>relax()</code>,
 * the SpringLayout moves the visualization forward one step.
 *
 * @author Danyel Fisher
 * @author Joshua O'Madadhain
 */
public class SpringLayout<N, E> extends AbstractLayout<N, E> implements IterativeContext {

  protected double stretch = 0.70;
  protected LengthFunction<EndpointPair<N>> lengthFunction;
  protected int repulsion_range = 100;
  protected double force_multiplier = 1.0 / 3.0;
  int totalSteps = 2000;
  int step = 0;

  protected LoadingCache<N, SpringNodeData> springNodeData =
      CacheBuilder.newBuilder()
          .build(
              new CacheLoader<N, SpringNodeData>() {
                public SpringNodeData load(N node) {
                  return new SpringNodeData();
                }
              });

  protected LoadingCache<EndpointPair<N>, SpringEdgeData> springEdgeData =
      CacheBuilder.newBuilder()
          .build(
              new CacheLoader<EndpointPair<N>, SpringEdgeData>() {
                public SpringEdgeData load(EndpointPair<N> edge) {
                  return new SpringEdgeData<EndpointPair<N>>(edge);
                }
              });

  /**
   * Constructor for a SpringLayout for a raw graph with associated dimension--the input knows how
   * big the graph is. Defaults to the unit length function.
   */
  public SpringLayout(Network<N, E> g) {
    this(g, UNITLENGTHFUNCTION);
  }

  /**
   * Constructor for a SpringLayout for a raw graph with associated component.
   *
   * @param g the input Graph
   * @param f the length function
   */
  public SpringLayout(Network<N, E> g, LengthFunction<EndpointPair<N>> f) {
    super(g);
    this.lengthFunction = f;
  }

  /**
   * @return the current value for the stretch parameter
   * @see #setStretch(double)
   */
  public double getStretch() {
    return stretch;
  }

  /* (non-Javadoc)

  */
  @Override
  public void setSize(BoundingSphere bs) {
    Function<N, Point3f> rlt = new RandomLocationTransformer<N>(bs);
    setInitializer(rlt);
    super.setSize(bs);
  }

  /**
   * Sets the stretch parameter for this instance. This value specifies how much the degrees of an
   * edge's incident vertices should influence how easily the endpoints of that edge can move (that
   * is, that edge's tendency to change its length).
   *
   * <p>The default value is 0.70. Positive values less than 1 cause high-degree vertices to move
   * less than low-degree vertices, and values > 1 cause high-degree vertices to move more than
   * low-degree vertices. Negative values will have unpredictable and inconsistent results.
   *
   * @param stretch
   */
  public void setStretch(double stretch) {
    this.stretch = stretch;
  }

  /**
   * @return the current value for the node repulsion range
   * @see #setRepulsionRange(int)
   */
  public int getRepulsionRange() {
    return repulsion_range;
  }

  /**
   * Sets the node repulsion range (in drawing area units) for this instance. Outside this range,
   * nodes do not repel each other. The default value is 100. Negative values are treated as their
   * positive equivalents.
   *
   * @param range
   */
  public void setRepulsionRange(int range) {
    this.repulsion_range = range;
  }

  /**
   * @return the current value for the edge length force multiplier
   * @see #setForceMultiplier(double)
   */
  public double getForceMultiplier() {
    return force_multiplier;
  }

  /**
   * Sets the force multiplier for this instance. This value is used to specify how strongly an edge
   * "wants" to be its default length (higher values indicate a greater attraction for the default
   * length), which affects how much its endpoints move at each timestep. The default value is 1/3.
   * A value of 0 turns off any attempt by the layout to cause edges to conform to the default
   * length. Negative values cause long edges to get longer and short edges to get shorter; use at
   * your own risk.
   */
  public void setForceMultiplier(double force) {
    this.force_multiplier = force;
  }

  public void initialize() {
    Network<N, E> graph = getNetwork();
    BoundingSphere d = getSize();
    if (graph != null && d != null) {

      try {
        for (EndpointPair<N> e : graph.asGraph().edges()) {
          SpringEdgeData<EndpointPair<N>> sed = getSpringData(e);
          calcEdgeLength(sed, lengthFunction);
        }
      } catch (ConcurrentModificationException cme) {
        initialize();
      }
    }
  }

  /* ------------------------- */

  protected void calcEdgeLength(
      SpringEdgeData<EndpointPair<N>> sed, LengthFunction<EndpointPair<N>> f) {
    sed.length = f.getLength(sed.e);
  }

  /* ------------------------- */

  /** Relaxation step. Moves all nodes a smidge. */
  public void step() {
    step++;
    try {
      for (N v : getNetwork().nodes()) {
        SpringNodeData svd = getSpringData(v);
        //    			System.err.println("svd = "+svd);
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

  protected N getAVertex(EndpointPair<N> e) {

    return e.iterator().next();
  }

  protected void relaxEdges() {
    try {
      for (EndpointPair<N> e : getNetwork().asGraph().edges()) {

        N v1 = e.nodeV();
        N v2 = e.nodeU();

        Point3f p1 = apply(v1);
        Point3f p2 = apply(v2);
        if (p1 == null || p2 == null) continue;
        double vx = p1.x - p2.x;
        double vy = p1.y - p2.y;
        double vz = p1.z - p2.z;
        double len = Math.sqrt(vx * vx + vy * vy + vz * vz);

        SpringEdgeData<EndpointPair<N>> sed = getSpringData(e);
        if (sed == null) {
          continue;
        }
        double desiredLen = sed.length;

        // round from zero, if needed [zero would be Bad.].
        len = (len == 0) ? .0001 : len;

        double f = force_multiplier * (desiredLen - len) / len;

        f = f * Math.pow(stretch, (getNetwork().degree(v1) + getNetwork().degree(v2) - 2));

        // the actual movement distance 'dx' is the force multiplied by the
        // distance to go.
        double dx = f * vx;
        double dy = f * vy;
        double dz = f * vz;
        SpringNodeData v1D, v2D;
        v1D = getSpringData(v1);
        v2D = getSpringData(v2);

        sed.f = f;

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
    try {
      for (N v : getNetwork().nodes()) {
        if (isLocked(v)) continue;

        SpringNodeData svd = getSpringData(v);
        if (svd == null) continue;
        double dx = 0, dy = 0, dz = 0;

        for (N v2 : getNetwork().nodes()) {
          if (v == v2) continue;
          Point3f p = apply(v);
          Point3f p2 = apply(v2);
          if (p == null || p2 == null) continue;
          double vx = p.x - p2.x;
          double vy = p.y - p2.y;
          double vz = p.z - p2.z;
          double distance = vx * vx + vy * vy + vz * vz;
          if (distance == 0) {
            dx += Math.random();
            dy += Math.random();
            dz += Math.random();
          } else if (distance < repulsion_range * repulsion_range) {
            double factor = 1;
            dx += factor * vx / Math.pow(distance, 2);
            dy += factor * vy / Math.pow(distance, 2);
            dz += factor * vz / Math.pow(distance, 2);
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

    synchronized (getSize()) {
      try {
        for (N v : getNetwork().nodes()) {
          if (isLocked(v)) continue;
          SpringNodeData vd = getSpringData(v);
          if (vd == null) continue;
          Point3f xyd = apply(v);

          vd.dx += vd.repulsiondx + vd.edgedx;
          vd.dy += vd.repulsiondy + vd.edgedy;
          vd.dz += vd.repulsiondz + vd.edgedz;

          // keeps nodes from moving any faster than 5 per time unit
          xyd.set(
              (float) (xyd.x + Math.max(-5, Math.min(5, vd.dx))),
              (float) (xyd.y + Math.max(-5, Math.min(5, vd.dy))),
              (float) (xyd.z + Math.max(-5, Math.min(5, vd.dz))));

          BoundingSphere d = getSize();
          float radius = (float) d.getRadius();

          if (xyd.x < -radius) {
            xyd.set(-radius, xyd.y, xyd.z); //                     setX(0);
          } else if (xyd.x > radius) {
            xyd.set(radius, xyd.y, xyd.z); //setX(width);
          }

          if (xyd.y < -radius) {
            xyd.set(xyd.x, -radius, xyd.z); //setY(0);
          } else if (xyd.y > radius) {
            xyd.set(xyd.x, radius, xyd.z); //setY(height);
          }

          if (xyd.z < -radius) {
            xyd.set(xyd.x, xyd.y, -radius); //setY(0);
          } else if (xyd.z > radius) {
            xyd.set(xyd.x, xyd.y, radius); //setY(height);
          }

          //                    System.err.println(v+" xyd = "+xyd);

        }
      } catch (ConcurrentModificationException cme) {
        moveNodes();
      }
    }
  }

  public SpringNodeData getSpringData(N v) {
    return springNodeData.getUnchecked(v);
  }

  public SpringEdgeData<EndpointPair<N>> getSpringData(EndpointPair<N> e) {
    return springEdgeData.getUnchecked(e);
  }

  public double getLength(EndpointPair<N> e) {
    return springEdgeData.getUnchecked(e).length;
  }

  /* ---------------Length Function------------------ */

  /**
   * If the edge is weighted, then override this method to show what the visualized length is.
   *
   * @author Danyel Fisher
   */
  public static interface LengthFunction<E> {

    public double getLength(E e);
  }

  /**
   * Returns all edges as the same length: the input value
   *
   * @author danyelf
   */
  public static final class UnitLengthFunction<E> implements LengthFunction<E> {

    int length;

    public UnitLengthFunction(int length) {
      this.length = length;
    }

    public double getLength(E e) {
      return length;
    }
  }

  public static final LengthFunction UNITLENGTHFUNCTION = new UnitLengthFunction(30);

  /* ---------------User Data------------------ */

  protected static class SpringNodeData {

    public double edgedx;

    public double edgedy;

    public double edgedz;

    public double repulsiondx;

    public double repulsiondy;

    public double repulsiondz;

    public SpringNodeData() {}

    /** movement speed, x */
    public double dx;

    /** movement speed, y */
    public double dy;

    public double dz;

    public String toString() {
      return "SVD["
          + dx
          + ","
          + dy
          + ","
          + dz
          + "]"
          + "["
          + repulsiondx
          + ","
          + repulsiondy
          + ","
          + repulsiondz
          + "]";
    }
  }

  protected static class SpringEdgeData<E> {

    public double f;

    public SpringEdgeData(E e) {
      this.e = e;
    }

    E e;

    double length;
  }

  /* ---------------Resize handler------------------ */

  public class SpringDimensionChecker extends ComponentAdapter {

    public void componentResized(ComponentEvent e) {
      setSize(new BoundingSphere(new Point3d(), e.getComponent().getWidth()));
    }
  }

  /** This one is an incremental visualization */
  public boolean isIncremental() {
    return true;
  }

  /** For now, we pretend it never finishes. */
  public boolean done() {
    return step > totalSteps;
  }

  public void reset() {
    step = 0;
  }
}
