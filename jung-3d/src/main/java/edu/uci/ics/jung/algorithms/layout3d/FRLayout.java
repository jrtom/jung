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
import java.util.Collection;
import java.util.ConcurrentModificationException;
import javax.media.j3d.BoundingSphere;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * Implements the Fruchterman-Reingold algorithm for node layout.
 *
 * @author Scott White, Yan-Biao Boey, Danyel Fisher, Tom Nelson
 */
public class FRLayout<N, E> extends AbstractLayout<N, E> implements IterativeContext {

  private double forceConstant;

  private double temperature;

  private int currentIteration;

  private int mMaxIterations = 700;

  protected LoadingCache<N, Vector3f> frNodeData =
      CacheBuilder.newBuilder()
          .build(
              new CacheLoader<N, Vector3f>() {
                public Vector3f load(N node) {
                  return new Vector3f();
                }
              });

  private double attraction_multiplier = 0.75;

  private double attraction_constant;

  private double repulsion_multiplier = 0.75;

  private double repulsion_constant;

  //  private final Graph<N> graph;

  public FRLayout(Network<N, E> g) {
    super(g);
    //    this.graph = g;
  }

  public FRLayout(Network<N, E> g, BoundingSphere d) {
    super(g, new RandomLocationTransformer<N>(d), d);
    //    this.graph = g;
    initialize();
  }

  /* (non-Javadoc)
   *
   */
  @Override
  public void setSize(BoundingSphere size) {
    setInitializer(new RandomLocationTransformer<N>(size));
    super.setSize(size);
  }

  public void setAttractionMultiplier(double attraction) {
    this.attraction_multiplier = attraction;
  }

  public void setRepulsionMultiplier(double repulsion) {
    this.repulsion_multiplier = repulsion;
  }

  public void reset() {
    doInit();
  }

  public void initialize() {
    doInit();
  }

  private void doInit() {
    Collection<N> nodes = nodes();
    BoundingSphere d = getSize();
    double diameter = 2 * d.getRadius();
    if (nodes != null) { //&& d != null) {
      currentIteration = 0;
      temperature = d.getRadius() / 5;

      forceConstant = Math.sqrt(diameter * diameter / nodes.size());

      attraction_constant = attraction_multiplier * forceConstant;
      repulsion_constant = repulsion_multiplier * forceConstant;
    }
  }

  private double EPSILON = 0.000001D;

  /**
   * Moves the iteration forward one notch, calculation attraction and repulsion between vertices
   * and edges and cooling the temperature.
   */
  public synchronized void step() {
    currentIteration++;

    /** Calculate repulsion */
    while (true) {

      try {
        for (N v1 : nodes()) {
          //                    if (isLocked(v1)) continue;
          calcRepulsion(v1);
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }

    /** Calculate attraction */
    while (true) {
      try {
        for (EndpointPair<N> endpoints : network.asGraph().edges()) {
          calcAttraction(endpoints);
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }

    while (true) {
      try {
        for (N v : nodes()) {
          if (isLocked(v)) continue;
          calcPositions(v);
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    cool();
  }

  public synchronized void calcPositions(N v) {

    Vector3f fvd = frNodeData.getUnchecked(v);
    if (fvd == null) return;
    Point3f xyd = apply(v);

    double deltaLength = Math.max(EPSILON, fvd.length());

    Vector3f newDisp = new Vector3f(fvd);
    newDisp.scale((float) (Math.min(deltaLength, temperature) / deltaLength), fvd);
    xyd.add(newDisp);

    double borderWidth = 2 * getSize().getRadius() / 50.0;
    double min = -getSize().getRadius() + borderWidth;
    double max = -min;

    double[] min_pos = new double[3];
    double[] max_pos = new double[3];
    for (int i = 0; i < 3; i++) {
      min_pos[i] = min + Math.random() * borderWidth * 2;
      max_pos[i] = max - Math.random() * borderWidth * 2;
    }

    xyd.set(
        (float) Math.min(Math.max(xyd.x, min_pos[0]), max_pos[0]),
        (float) Math.min(Math.max(xyd.y, min_pos[1]), max_pos[1]),
        (float) Math.min(Math.max(xyd.z, min_pos[2]), max_pos[2]));
  }

  public void calcAttraction(EndpointPair<N> endpoints) {
    N node1 = endpoints.nodeU();
    N node2 = endpoints.nodeV();
    boolean v1_locked = isLocked(node1);
    boolean v2_locked = isLocked(node2);

    Point3f p1 = apply(node1);
    Point3f p2 = apply(node2);
    if (p1 == null || p2 == null) return;

    Vector3f delta = new Vector3f();
    delta.negate(p2);
    delta.add(p1);

    double deltaLength = Math.max(EPSILON, delta.length());

    double force = (deltaLength * deltaLength) / attraction_constant;

    if (Double.isNaN(force)) {
      throw new IllegalArgumentException(
          "Unexpected mathematical result in FRLayout:calcPositions [force]");
    }

    delta.scale((float) (force / deltaLength));

    frNodeData.getUnchecked(node2).add(delta);
    delta.negate();
    frNodeData.getUnchecked(node1).add(delta);
  }

  public void calcRepulsion(N v1) {
    Vector3f fvd1 = frNodeData.getUnchecked(v1);
    if (fvd1 == null) return;
    fvd1.set(0, 0, 0);

    try {
      for (N v2 : nodes()) {

        //                if (isLocked(v2)) continue;
        if (v1 != v2) {
          Point3f p1 = apply(v1);
          Point3f p2 = apply(v2);
          if (p1 == null || p2 == null) continue;

          Vector3f delta = new Vector3f();
          delta.negate(p2);
          delta.add(p1);

          double deltaLength = Math.max(EPSILON, delta.length());

          double force = (repulsion_constant * repulsion_constant) / deltaLength;

          if (Double.isNaN(force)) {
            throw new RuntimeException(
                "Unexpected mathematical result in FRLayout:calcPositions [repulsion]");
          }

          delta.scale((float) (force / deltaLength));
          fvd1.add(delta);
        }
      }
    } catch (ConcurrentModificationException cme) {
      calcRepulsion(v1);
    }
  }

  private void cool() {
    temperature *= (1.0 - currentIteration / (double) mMaxIterations);
  }

  public void setMaxIterations(int maxIterations) {
    mMaxIterations = maxIterations;
  }

  /** This one is an incremental visualization. */
  public boolean isIncremental() {
    return true;
  }

  /**
   * Returns true once the current iteration has passed the maximum count, <tt>MAX_ITERATIONS</tt>.
   */
  public boolean done() {
    if (currentIteration > mMaxIterations) {
      return true;
    }
    return false;
  }
}
