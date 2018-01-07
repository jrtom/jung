/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.layout.algorithms;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.layout.spatial.BarnesHutQuadTree;
import edu.uci.ics.jung.layout.spatial.ForceObject;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Random;
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
public class FRBHLayoutAlgorithm<N> extends AbstractIterativeLayoutAlgorithm<N>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(FRBHLayoutAlgorithm.class);

  private double forceConstant;

  private double temperature;

  private int currentIteration;

  private int mMaxIterations = 700;

  protected LoadingCache<N, Point> frNodeData;

  private double attraction_multiplier = 0.75;

  private double attraction_constant;

  private double repulsion_multiplier = 0.75;

  private double repulsion_constant;

  private double max_dimension;

  private boolean initialized = false;

  private BarnesHutQuadTree<N> tree;

  private Random random = new Random();

  public FRBHLayoutAlgorithm() {
    this.frNodeData =
        CacheBuilder.newBuilder()
            .build(
                new CacheLoader<N, Point>() {
                  public Point load(N node) {
                    return Point.ORIGIN;
                  }
                });
  }

  @Override
  public void visit(LayoutModel<N> layoutModel) {
    if (log.isTraceEnabled()) {
      log.trace("visiting " + layoutModel);
    }
    super.visit(layoutModel);
    max_dimension = Math.max(layoutModel.getWidth(), layoutModel.getHeight());
    initialize();
  }

  public void setRandomSeed(long randomSeed) {
    this.random = new Random(randomSeed);
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
      temperature = layoutModel.getWidth() / 10;

      forceConstant =
          Math.sqrt(layoutModel.getHeight() * layoutModel.getWidth() / graph.nodes().size());

      attraction_constant = attraction_multiplier * forceConstant;
      repulsion_constant = repulsion_multiplier * forceConstant;
      initialized = true;
      tree = new BarnesHutQuadTree(layoutModel);
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
    // the cost of building the tree each time is less than the O(n^2) cost in the repulsion part
    tree.rebuild();
    if (log.isTraceEnabled()) {
      log.trace("tree: {}", tree);
    }

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
          if (layoutModel.isLocked(node)) {
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

    Point fvd = getFRData(node);
    if (fvd == null) {
      return;
    }
    Point xyd = layoutModel.apply(node);
    double deltaLength = Math.max(EPSILON, fvd.length());

    double positionX = xyd.x;
    double positionY = xyd.y;
    double newXDisp = fvd.x / deltaLength * Math.min(deltaLength, temperature);
    double newYDisp = fvd.y / deltaLength * Math.min(deltaLength, temperature);

    positionX += newXDisp;
    positionY += newYDisp;

    double borderWidth = layoutModel.getWidth() / 50.0;

    if (positionX < borderWidth) {
      positionX = borderWidth + random.nextDouble() * borderWidth * 2.0;
    } else if (positionX > layoutModel.getWidth() - borderWidth * 2) {
      positionX = layoutModel.getWidth() - borderWidth - random.nextDouble() * borderWidth * 2.0;
    }

    if (positionY < borderWidth) {
      positionY = borderWidth + random.nextDouble() * borderWidth * 2.0;
    } else if (positionY > layoutModel.getWidth() - borderWidth * 2) {
      positionY = layoutModel.getWidth() - borderWidth - random.nextDouble() * borderWidth * 2.0;
    }

    layoutModel.set(node, positionX, positionY);
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
    Point p1 = layoutModel.apply(node1);
    Point p2 = layoutModel.apply(node2);
    if (p1 == null || p2 == null) {
      return;
    }
    double xDelta = p1.x - p2.x;
    double yDelta = p1.y - p2.y;

    double deltaLength = Math.max(EPSILON, Math.sqrt((xDelta * xDelta) + (yDelta * yDelta)));

    double force = (deltaLength * deltaLength) / attraction_constant;

    Preconditions.checkState(
        !Double.isNaN(force), "Unexpected mathematical result in FRLayout:calcPositions [force]");

    double dx = (xDelta / deltaLength) * force;
    double dy = (yDelta / deltaLength) * force;
    if (v1_locked == false) {
      Point fvd1 = getFRData(node1);
      frNodeData.put(node1, fvd1.add(-dx, -dy));
    }
    if (v2_locked == false) {
      Point fvd2 = getFRData(node2);
      frNodeData.put(node2, fvd2.add(dx, dy));
    }
  }

  protected void calcRepulsion(N node1) {
    Point fvd1 = getFRData(node1);
    if (fvd1 == null) {
      return;
    }
    log.trace("fvd1 for {} starts as {}", node1, fvd1);
    frNodeData.put(node1, Point.ORIGIN);
    ForceObject<N> nodeForceObject = new ForceObject<>(node1, layoutModel.apply(node1));
    Iterator<ForceObject<N>> forceObjectIterator =
        new BarnesHutQuadTree.ForceObjectIterator<>(tree, nodeForceObject);
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

  private void cool() {
    temperature *= (1.0 - currentIteration / (double) mMaxIterations);
  }

  public void setMaxIterations(int maxIterations) {
    mMaxIterations = maxIterations;
  }

  protected Point getFRData(N node) {
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
}
