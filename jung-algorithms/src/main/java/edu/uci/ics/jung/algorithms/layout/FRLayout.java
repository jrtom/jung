/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.layout;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ConcurrentModificationException;

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
 * @author Scott White, Yan-Biao Boey, Danyel Fisher
 */
public class FRLayout<N> extends AbstractLayout<N> implements IterativeContext {

  private double forceConstant;

  private double temperature;

  private int currentIteration;

  private int mMaxIterations = 700;

  protected LoadingCache<N, FRNodeData> frNodeData =
      CacheBuilder.newBuilder()
          .build(
              new CacheLoader<N, FRNodeData>() {
                public FRNodeData load(N node) {
                  return new FRNodeData();
                }
              });

  private double attraction_multiplier = 0.75;

  private double attraction_constant;

  private double repulsion_multiplier = 0.75;

  private double repulsion_constant;

  private double max_dimension;

  private final Graph<N> graph;

  public FRLayout(Graph<N> g) {
    super(g);
    this.graph = g;
  }

  public FRLayout(Graph<N> g, Dimension d) {
    super(g, new RandomLocationTransformer<N>(d), d);
    this.graph = g;
    initialize();
    max_dimension = Math.max(d.height, d.width);
  }

  @Override
  public void setSize(Dimension size) {
    if (initialized == false) {
      setInitializer(new RandomLocationTransformer<N>(size));
    }
    super.setSize(size);
    max_dimension = Math.max(size.height, size.width);
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
    Dimension d = getSize();
    if (graph != null && d != null) {
      currentIteration = 0;
      temperature = d.getWidth() / 10;

      forceConstant = Math.sqrt(d.getHeight() * d.getWidth() / graph.nodes().size());

      attraction_constant = attraction_multiplier * forceConstant;
      repulsion_constant = repulsion_multiplier * forceConstant;
    }
  }

  private double EPSILON = 0.000001D;

  /**
   * Moves the iteration forward one notch, calculation attraction and repulsion between nodes and
   * edges and cooling the temperature.
   */
  public synchronized void step() {
    currentIteration++;

    /** Calculate repulsion */
    while (true) {

      try {
        for (N node1 : graph.nodes()) {
          calcRepulsion(node1);
        }
        break;
      } catch (ConcurrentModificationException cme) {
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
      }
    }

    while (true) {
      try {
        for (N node : graph.nodes()) {
          if (isLocked(node)) {
            continue;
          }
          calcPositions(node);
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    cool();
  }

  protected synchronized void calcPositions(N node) {
    FRNodeData fvd = getFRData(node);
    if (fvd == null) {
      return;
    }
    Point2D xyd = apply(node);
    double deltaLength = Math.max(EPSILON, fvd.norm());

    double newXDisp = fvd.getX() / deltaLength * Math.min(deltaLength, temperature);

    if (Double.isNaN(newXDisp)) {
      throw new IllegalArgumentException(
          "Unexpected mathematical result in FRLayout:calcPositions [xdisp]");
    }

    double newYDisp = fvd.getY() / deltaLength * Math.min(deltaLength, temperature);
    xyd.setLocation(xyd.getX() + newXDisp, xyd.getY() + newYDisp);

    double borderWidth = getSize().getWidth() / 50.0;
    double newXPos = xyd.getX();
    if (newXPos < borderWidth) {
      newXPos = borderWidth + Math.random() * borderWidth * 2.0;
    } else if (newXPos > (getSize().getWidth() - borderWidth)) {
      newXPos = getSize().getWidth() - borderWidth - Math.random() * borderWidth * 2.0;
    }

    double newYPos = xyd.getY();
    if (newYPos < borderWidth) {
      newYPos = borderWidth + Math.random() * borderWidth * 2.0;
    } else if (newYPos > (getSize().getHeight() - borderWidth)) {
      newYPos = getSize().getHeight() - borderWidth - Math.random() * borderWidth * 2.0;
    }

    xyd.setLocation(newXPos, newYPos);
  }

  protected void calcAttraction(EndpointPair<N> endpoints) {
    N node1 = endpoints.nodeU();
    N node2 = endpoints.nodeV();
    boolean v1_locked = isLocked(node1);
    boolean v2_locked = isLocked(node2);

    if (v1_locked && v2_locked) {
      // both locked, do nothing
      return;
    }
    Point2D p1 = apply(node1);
    Point2D p2 = apply(node2);
    if (p1 == null || p2 == null) {
      return;
    }
    double xDelta = p1.getX() - p2.getX();
    double yDelta = p1.getY() - p2.getY();

    double deltaLength = Math.max(EPSILON, Math.sqrt((xDelta * xDelta) + (yDelta * yDelta)));

    double force = (deltaLength * deltaLength) / attraction_constant;

    if (Double.isNaN(force)) {
      throw new IllegalArgumentException(
          "Unexpected mathematical result in FRLayout:calcPositions [force]");
    }

    double dx = (xDelta / deltaLength) * force;
    double dy = (yDelta / deltaLength) * force;
    if (v1_locked == false) {
      FRNodeData fvd1 = getFRData(node1);
      fvd1.offset(-dx, -dy);
    }
    if (v2_locked == false) {
      FRNodeData fvd2 = getFRData(node2);
      fvd2.offset(dx, dy);
    }
  }

  protected void calcRepulsion(N node1) {
    FRNodeData fvd1 = getFRData(node1);
    if (fvd1 == null) {
      return;
    }
    fvd1.setLocation(0, 0);

    try {
      for (N node2 : graph.nodes()) {

        //                if (isLocked(node2)) continue;
        if (node1 != node2) {
          Point2D p1 = apply(node1);
          Point2D p2 = apply(node2);
          if (p1 == null || p2 == null) {
            continue;
          }
          double xDelta = p1.getX() - p2.getX();
          double yDelta = p1.getY() - p2.getY();

          double deltaLength = Math.max(EPSILON, Math.sqrt((xDelta * xDelta) + (yDelta * yDelta)));

          double force = (repulsion_constant * repulsion_constant) / deltaLength;

          if (Double.isNaN(force)) {
            throw new RuntimeException(
                "Unexpected mathematical result in FRLayout:calcPositions [repulsion]");
          }

          fvd1.offset((xDelta / deltaLength) * force, (yDelta / deltaLength) * force);
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

  protected FRNodeData getFRData(N node) {
    return frNodeData.getUnchecked(node);
  }

  /** @return true */
  public boolean isIncremental() {
    return true;
  }

  /** @return true once the current iteration has passed the maximum count. */
  public boolean done() {
    if (currentIteration > mMaxIterations || temperature < 1.0 / max_dimension) {
      return true;
    }
    return false;
  }

  @SuppressWarnings("serial")
  protected static class FRNodeData extends Point2D.Double {
    protected void offset(double x, double y) {
      this.x += x;
      this.y += y;
    }

    protected double norm() {
      return Math.sqrt(x * x + y * y);
    }
  }
}
