/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.layout3d.algorithms;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.layout.algorithms.AbstractIterativeLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.util.RandomLocationTransformer;
import java.util.ConcurrentModificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the Fruchterman-Reingold force-directed algorithm for node layout.
 *
 * <p>Behavior is determined by the following settable parameters:
 *
 * <ul>
 *   <li>attraction multiplier: how much edges try to keep their nodes together
 *   <li>repulsion multiplier: how much nodes try to push each other apart
 *   <li>maximum iterations: how many iterations this algorithm will use before stopping
 * </ul>
 *
 * Each of the first two defaults to 0.75; the maximum number of iterations defaults to 700.
 *
 * @see "Fruchterman and Reingold, 'Graph Drawing by Force-directed Placement'"
 * @see
 *     "http://i11www.ilkd.uni-karlsruhe.de/teaching/SS_04/visualisierung/papers/fruchterman91graph.pdf"
 * @author Scott White, Yan-Biao Boey, Danyel Fisher, Tom Nelson
 */
public class FRLayoutAlgorithm<N, P> extends AbstractIterativeLayoutAlgorithm<N, P>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(FRLayoutAlgorithm.class);

  private double forceConstant;

  private double temperature;

  private int currentIteration;

  private int mMaxIterations = 700;

  protected LoadingCache<N, P> frNodeData;

  private double attraction_multiplier = 0.75;

  private double attraction_constant;

  private double repulsion_multiplier = 0.75;

  private double repulsion_constant;

  private boolean initialized = false;

  private float width;
  private float height;
  private float depth;
  private float xMax;
  private float xMin;
  private float yMax;
  private float yMin;
  private float zMax;
  private float zMin;
  private float border;

  public FRLayoutAlgorithm() {
    this.frNodeData =
        CacheBuilder.newBuilder()
            .build(
                new CacheLoader<N, P>() {
                  public P load(N node) {
                    return pointModel.newPoint(0, 0);
                  }
                });
  }

  @Override
  public void visit(LayoutModel<N, P> layoutModel) {
    super.visit(layoutModel);
    log.trace("visiting " + layoutModel);
    layoutModel.setInitializer(
        new RandomLocationTransformer<N, P>(
            RandomLocationTransformer.Origin.CENTER, pointModel, 600, 600, 600));

    this.width = layoutModel.getWidth() * (float) Math.sqrt(2.0f) / 2.0f;
    this.height = layoutModel.getHeight() * (float) Math.sqrt(2.0f) / 2.0f;
    this.depth = layoutModel.getDepth() * (float) Math.sqrt(2.0f) / 2.0f;
    this.xMax = width / 2;
    this.xMin = -this.xMax;
    this.yMax = height / 2;
    this.yMin = -this.yMax;
    this.zMax = depth / 2;
    this.zMin = -this.zMax;
    this.border = Math.max(width, Math.max(height, depth)) / 50;

    super.visit(layoutModel);
    initialize();
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
    Graph<N> graph = layoutModel.getGraph();
    if (graph != null && graph.nodes().size() > 0) {
      currentIteration = 0;
      float maxRadius = Math.max(Math.max(this.xMax, this.yMax), this.zMax);
      temperature = maxRadius / 5;

      forceConstant = Math.sqrt(width * height / graph.nodes().size());

      attraction_constant = attraction_multiplier * forceConstant;
      repulsion_constant = repulsion_multiplier * forceConstant;
      initialized = true;
    }
  }

  private double EPSILON = 0.000001D;

  /**
   * Moves the iteration forward one notch, calculation attraction and repulsion between nodes and
   * edges and cooling the temperature.
   */
  public synchronized void step() {

    if (!initialized) {
      doInit();
    }
    Graph<N> graph = layoutModel.getGraph();
    currentIteration++;

    /** Calculate repulsion */
    while (true) {

      try {
        for (N node1 : graph.nodes()) {
          calcRepulsion(node1);
        }
        break;
      } catch (ConcurrentModificationException cme) {
        log.warn("repulsion got a cme", cme);
      }
    }

    /** Calculate attraction */
    while (true) {
      try {
        for (EndpointPair<N> endpoints : graph.edges()) {
          calcAttraction(endpoints);
        }
        break;
      } catch (ConcurrentModificationException cme) {
        log.warn("attraction got a cme", cme);
      }
    }

    while (true) {
      try {
        for (N node : graph.nodes()) {
          if (layoutModel.isLocked(node)) {
            continue;
          }
          calcPositions(node);
        }
        break;
      } catch (ConcurrentModificationException cme) {
        log.warn("positions got a cme", cme);
      }
    }
    cool();
  }

  protected synchronized void calcPositions(N node) {

    P fvd = getFRData(node);
    if (fvd == null) {
      return;
    }
    P xyd = layoutModel.apply(node);
    double deltaLength = Math.max(EPSILON, pointModel.distance(fvd));

    double newXDisp = pointModel.getX(fvd) / deltaLength * Math.min(deltaLength, temperature);
    double newYDisp = pointModel.getY(fvd) / deltaLength * Math.min(deltaLength, temperature);
    double newZDisp = pointModel.getZ(fvd) / deltaLength * Math.min(deltaLength, temperature);

    pointModel.setLocation(
        xyd,
        pointModel.getX(xyd) + newXDisp,
        pointModel.getY(xyd) + newYDisp,
        pointModel.getZ(xyd) + newZDisp);

    double newXPos = pointModel.getX(xyd);
    newXPos = Math.min(Math.max(newXPos, this.xMin), this.xMax);

    double newYPos = pointModel.getY(xyd);
    newYPos = Math.min(Math.max(newYPos, this.yMin), this.yMax);

    double newZPos = pointModel.getZ(xyd);
    newZPos = Math.min(Math.max(newZPos, this.zMin), this.zMax);

    //    pointModel.setLocation(xyd, newXPos, newYPos, newZPos);
    layoutModel.set(node, newXPos, newYPos, newZPos);
  }

  protected void calcAttraction(EndpointPair<N> endpoints) {
    N node1 = endpoints.nodeU();
    N node2 = endpoints.nodeV();
    boolean v1_locked = layoutModel.isLocked(node1);
    boolean v2_locked = layoutModel.isLocked(node2);

    if (v1_locked && v2_locked) {
      // both locked, do nothing
      return;
    }
    P p1 = layoutModel.apply(node1);
    P p2 = layoutModel.apply(node2);
    if (p1 == null || p2 == null) {
      return;
    }
    double xDelta = pointModel.getX(p1) - pointModel.getX(p2);
    double yDelta = pointModel.getY(p1) - pointModel.getY(p2);
    double zDelta = pointModel.getZ(p1) - pointModel.getZ(p2);

    double deltaLength =
        Math.max(EPSILON, Math.sqrt((xDelta * xDelta) + (yDelta * yDelta) + (zDelta * zDelta)));

    double force = (deltaLength * deltaLength) / attraction_constant;

    Preconditions.checkState(
        !Double.isNaN(force), "Unexpected mathematical result in FRLayout:calcPositions [force]");

    double dx = (xDelta / deltaLength) * force;
    double dy = (yDelta / deltaLength) * force;
    double dz = (zDelta / deltaLength) * force;

    if (v1_locked == false) {
      P fvd1 = getFRData(node1);
      pointModel.offset(fvd1, -dx, -dy, -dz);
    }
    if (v2_locked == false) {
      P fvd2 = getFRData(node2);
      pointModel.offset(fvd2, dx, dy, dz);
    }
  }

  protected void calcRepulsion(N node1) {
    P fvd1 = getFRData(node1);
    if (fvd1 == null) {
      return;
    }
    pointModel.setLocation(fvd1, 0, 0, 0);

    try {
      for (N node2 : layoutModel.getGraph().nodes()) {

        if (node1 != node2) {
          P p1 = layoutModel.apply(node1);
          P p2 = layoutModel.apply(node2);
          if (p1 == null || p2 == null) {
            continue;
          }
          double xDelta = pointModel.getX(p1) - pointModel.getX(p2);
          double yDelta = pointModel.getY(p1) - pointModel.getY(p2);
          double zDelta = pointModel.getZ(p1) - pointModel.getZ(p2);

          double deltaLength =
              Math.max(
                  EPSILON, Math.sqrt((xDelta * xDelta) + (yDelta * yDelta) + (zDelta * zDelta)));

          double force = (repulsion_constant * repulsion_constant) / deltaLength;

          if (Double.isNaN(force)) {
            throw new RuntimeException(
                "Unexpected mathematical result in FRLayout:calcPositions [repulsion]");
          }

          pointModel.offset(
              fvd1,
              (xDelta / deltaLength) * force,
              (yDelta / deltaLength) * force,
              (zDelta / deltaLength) * force);
        }
      }
    } catch (ConcurrentModificationException cme) {
      calcRepulsion(node1);
    }
  }

  private void cool() {
    temperature *= (1.0 - currentIteration / (double) mMaxIterations);
  }

  public void setMaxIterations(int maxIterations) {
    mMaxIterations = maxIterations;
  }

  protected P getFRData(N node) {
    return frNodeData.getUnchecked(node);
  }

  /** @return true once the current iteration has passed the maximum count. */
  public boolean done() {
    if (currentIteration > mMaxIterations) { //|| temperature < 1.0 / max_dimension) {
      return true;
    }
    return false;
  }
}
