/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Dec 4, 2003
 */
package edu.uci.ics.jung.layout.algorithms;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@code Layout} suitable for tree-like directed acyclic graphs. Parts of it
 * will probably not terminate if the graph is cyclic! The layout will result in directed edges
 * pointing generally upwards. Any nodes with no successors are considered to be level 0, and tend
 * towards the top of the layout. Any node has a level one greater than the maximum level of all its
 * successors.
 *
 * @author John Yesberg
 */
public class DAGLayoutAlgorithm<N> extends SpringLayoutAlgorithm<N> {

  private static final Logger log = LoggerFactory.getLogger(DAGLayoutAlgorithm.class);
  /**
   * Each node has a minimumLevel. Any node with no successors has minimumLevel of zero. The
   * minimumLevel of any node must be strictly greater than the minimumLevel of its parents. (node A
   * is a parent of node B iff there is an edge from B to A.) Typically, a node will have a
   * minimumLevel which is one greater than the minimumLevel of its parent's. However, if the node
   * has two parents, its minimumLevel will be one greater than the maximum of the parents'. We need
   * to calculate the minimumLevel for each node. When we layout the graph, nodes cannot be drawn
   * any higher than the minimumLevel. The graphHeight of a graph is the greatest minimumLevel that
   * is used. We will modify the SpringLayout calculations so that nodes cannot move above their
   * assigned minimumLevel.
   */
  private Map<N, Number> minLevels = new HashMap<N, Number>();
  // Simpler than the "pair" technique.
  static int graphHeight;
  static int numRoots;
  final double SPACEFACTOR = 1.3;
  // How much space do we allow for additional floating at the bottom.
  final double LEVELATTRACTIONRATE = 0.8;

  /**
   * A bunch of parameters to help work out when to stop quivering.
   *
   * <p>If the MeanSquareVel(ocity) ever gets below the MSV_THRESHOLD, then we will start a final
   * cool-down phase of COOL_DOWN_INCREMENT increments. If the MeanSquareVel ever exceeds the
   * threshold, we will exit the cool down phase, and continue looking for another opportunity.
   */
  final double MSV_THRESHOLD = 10.0;

  double meanSquareVel;
  boolean stoppingIncrements = false;
  int incrementsLeft;
  final int COOL_DOWN_INCREMENTS = 200;

  @Override
  public void visit(LayoutModel<N> layoutModel) {
    super.visit(layoutModel);
    initialize();
  }

  /**
   * Calculates the level of each node in the graph. Level 0 is allocated to each node with no
   * successors. Level n+1 is allocated to any node whose successors' maximum level is n.
   */
  public void setRoot() {
    Graph<N> graph = layoutModel.getGraph();
    numRoots = 0;
    //    Network<N, E> g = network;
    for (N node : graph.nodes()) {
      if (graph.successors(node).isEmpty()) {
        setRoot(node);
        numRoots++;
      }
    }
  }

  /**
   * Set node v to be level 0.
   *
   * @param node the node to set as root
   */
  public void setRoot(N node) {
    minLevels.put(node, new Integer(0));
    // set all the levels.
    propagateMinimumLevel(node);
  }

  /**
   * A recursive method for allocating the level for each node. Ensures that all predecessors of v
   * have a level which is at least one greater than the level of v.
   *
   * @param node the node whose minimum level is to be calculated
   */
  public void propagateMinimumLevel(N node) {
    Graph<N> graph = layoutModel.getGraph();
    int level = minLevels.get(node).intValue();
    for (N child : graph.predecessors(node)) {
      int oldLevel, newLevel;
      Number o = minLevels.get(child);
      if (o != null) {
        oldLevel = o.intValue();
      } else {
        oldLevel = 0;
      }
      newLevel = Math.max(oldLevel, level + 1);
      minLevels.put(child, new Integer(newLevel));

      if (newLevel > graphHeight) {
        graphHeight = newLevel;
      }
      propagateMinimumLevel(child);
    }
  }

  /**
   * Sets a random location for a node within the dimensions of the space.
   *
   * @param node the node whose position is to be set
   * @param coord the coordinates of the node once the position has been set
   */
  private void initializeLocation(N node, Point coord, int width, int height) {

    int level = minLevels.get(node).intValue();
    int minY = (int) (level * height / (graphHeight * SPACEFACTOR));
    double x = Math.random() * width;
    double y = Math.random() * (height - minY) + minY;
    layoutModel.set(node, x, y);
  }

  /** Had to override this one as well, to ensure that setRoot() is called. */
  @Override
  public void initialize() {
    super.initialize();
    setRoot();
  }

  /**
   * Override the moveNodes() method from SpringLayout. The only change we need to make is to make
   * sure that nodes don't float higher than the minY coordinate, as calculated by their
   * minimumLevel.
   */
  @Override
  protected void moveNodes() {
    int width = layoutModel.getWidth();
    int height = layoutModel.getHeight();
    Graph<N> graph = layoutModel.getGraph();
    double oldMSV = meanSquareVel;
    meanSquareVel = 0;

    synchronized (layoutModel) {
      for (N node : graph.nodes()) {
        if (layoutModel.isLocked(node)) {
          continue;
        }
        SpringNodeData vd = springNodeData.getUnchecked(node);
        Point xyd = layoutModel.apply(node);

        // (JY addition: three lines are new)
        int level = minLevels.get(node).intValue();
        int minY = (int) (level * height / (graphHeight * SPACEFACTOR));
        int maxY = level == 0 ? (int) (height / (graphHeight * SPACEFACTOR * 2)) : height;

        // JY added 2* - double the sideways repulsion.
        vd.dx += 2 * vd.repulsiondx + vd.edgedx;
        vd.dy += vd.repulsiondy + vd.edgedy;

        // JY Addition: Attract the node towards it's minimumLevel
        // height.
        double delta = xyd.y - minY;
        vd.dy -= delta * LEVELATTRACTIONRATE;
        if (level == 0) {
          vd.dy -= delta * LEVELATTRACTIONRATE;
        }
        // twice as much at the top.

        // JY addition:
        meanSquareVel += (vd.dx * vd.dx + vd.dy * vd.dy);

        double posX = xyd.x + Math.max(-5, Math.min(5, vd.dx));
        double posY = xyd.y + Math.max(-5, Math.min(5, vd.dy));

        if (posX < 0) {
          posX = 0;
        } else if (posX > width) {
          posX = width;
        }
        if (posY < 0) {
          posY = 0;
        } else if (posY > height) {
          posY = height;
        }

        // (JY addition: if there's only one root, anchor it in the
        // middle-top of the screen)
        if (numRoots == 1 && level == 0) {
          posX = width / 2;
        }
        setLocation(node, posX, posY);
      }
    }
    // System.out.println("MeanSquareAccel="+meanSquareVel);
    if (!stoppingIncrements && Math.abs(meanSquareVel - oldMSV) < MSV_THRESHOLD) {
      stoppingIncrements = true;
      incrementsLeft = COOL_DOWN_INCREMENTS;
    } else if (stoppingIncrements && Math.abs(meanSquareVel - oldMSV) <= MSV_THRESHOLD) {
      incrementsLeft--;
      if (incrementsLeft <= 0) {
        incrementsLeft = 0;
      }
    }
  }

  /** Override incrementsAreDone so that we can eventually stop. */
  @Override
  public boolean done() {
    if (stoppingIncrements && incrementsLeft == 0) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Override forceMove so that if someone moves a node, we can re-layout everything.
   *
   * @param picked the node whose location is to be set
   * @param x the x coordinate of the location to set
   * @param y the y coordinate of the location to set
   */
  public void setLocation(N picked, double x, double y) {
    Point coord = layoutModel.apply(picked);
    layoutModel.set(picked, coord);
    stoppingIncrements = false;
  }

  /**
   * Override forceMove so that if someone moves a node, we can re-layout everything.
   *
   * @param picked the node whose location is to be set
   * @param p the location to set
   */
  public void setLocation(N picked, Point p) {
    setLocation(picked, p.x, p.y);
  }

  /**
   * Overridden relaxEdges. This one reduces the effect of edges between greatly different levels.
   */
  @Override
  protected void relaxEdges() {
    Graph<N> graph = layoutModel.getGraph();
    for (EndpointPair<N> endpoints : graph.edges()) {
      N node1 = endpoints.nodeU();
      N node2 = endpoints.nodeV();

      Point p1 = layoutModel.apply(node1);
      Point p2 = layoutModel.apply(node2);
      double vx = p1.x - p2.x;
      double vy = p1.y - p2.y;
      double len = Math.sqrt(vx * vx + vy * vy);

      // JY addition.
      int level1 = minLevels.get(node1).intValue();
      int level2 = minLevels.get(node2).intValue();

      double desiredLen = lengthFunction.apply(endpoints);

      // round from zero, if needed [zero would be Bad.].
      len = (len == 0) ? .0001 : len;

      // force factor: optimal length minus actual length,
      // is made smaller as the current actual length gets larger.
      // why?

      double f = force_multiplier * (desiredLen - len) / len;

      f = f * Math.pow(stretch / 100.0, (graph.degree(node1) + graph.degree(node2) - 2));

      // JY addition. If this is an edge which stretches a long way,
      // don't be so concerned about it.
      if (level1 != level2) {
        f = f / Math.pow(Math.abs(level2 - level1), 1.5);
      }

      // the actual movement distance 'dx' is the force multiplied by the
      // distance to go.
      double dx = f * vx;
      double dy = f * vy;
      SpringNodeData v1D, v2D;
      v1D = springNodeData.getUnchecked(node1);
      v2D = springNodeData.getUnchecked(node2);

      v1D.edgedx += dx;
      v1D.edgedy += dy;
      v2D.edgedx += -dx;
      v2D.edgedy += -dy;
    }
  }
}
