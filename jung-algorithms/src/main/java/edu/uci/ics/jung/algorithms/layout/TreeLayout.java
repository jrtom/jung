/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 9, 2005
 */

package edu.uci.ics.jung.algorithms.layout;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.Graph;
import com.google.common.graph.Graphs;
import edu.uci.ics.jung.graph.util.TreeUtils;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Karlheinz Toni
 * @author Tom Nelson - converted to jung2
 */
public class TreeLayout<N> implements Layout<N> {

  protected Dimension size = new Dimension(600, 600);
  protected Graph<N> graph;
  protected Map<N, Integer> basePositions = new HashMap<N, Integer>();

  protected LoadingCache<N, Point2D> locations =
      CacheBuilder.newBuilder()
          .build(
              new CacheLoader<N, Point2D>() {
                public Point2D load(N node) {
                  return new Point2D.Double();
                }
              });

  protected transient Set<N> alreadyDone = new HashSet<N>();

  /** The default horizontal node spacing. Initialized to 50. */
  public static int DEFAULT_DISTX = 50;

  /** The default vertical node spacing. Initialized to 50. */
  public static int DEFAULT_DISTY = 50;

  /** The horizontal node spacing. Defaults to {@code DEFAULT_XDIST}. */
  protected int distX = 50;

  /** The vertical node spacing. Defaults to {@code DEFAULT_YDIST}. */
  protected int distY = 50;

  protected transient Point m_currentPoint = new Point();

  /**
   * Creates an instance for the specified graph with default X and Y distances.
   *
   * @param g the graph on which the layout algorithm is to operate
   */
  public TreeLayout(Graph<N> g) {
    this(g, DEFAULT_DISTX, DEFAULT_DISTY);
  }

  /**
   * Creates an instance for the specified graph and X distance with default Y distance.
   *
   * @param g the graph on which the layout algorithm is to operate
   * @param distx the horizontal spacing between adjacent siblings
   */
  public TreeLayout(Graph<N> g, int distx) {
    this(g, distx, DEFAULT_DISTY);
  }

  /**
   * Creates an instance for the specified graph, X distance, and Y distance.
   *
   * @param g the graph on which the layout algorithm is to operate
   * @param distx the horizontal spacing between adjacent siblings
   * @param disty the vertical spacing between adjacent siblings
   */
  public TreeLayout(Graph<N> g, int distx, int disty) {
    this.graph = Preconditions.checkNotNull(g);
    Preconditions.checkArgument(distx >= 1, "X distance must be positive");
    Preconditions.checkArgument(disty >= 1, "Y distance must be positive");
    //		Preconditions.checkArgument(TreeUtils.isForestShaped(g), "Input graph must be forest-shaped: \n%s", g);
    Preconditions.checkArgument(
        !Graphs.hasCycle(g), "Input graph must not contain cycles: \n%s", g);
    this.distX = distx;
    this.distY = disty;
    buildTree();
  }

  protected void buildTree() {
    this.m_currentPoint = new Point(0, 20);
    Set<N> roots = TreeUtils.roots(graph);
    Preconditions.checkArgument(roots.size() > 0);
    calculateDimensionX(roots);
    for (N node : roots) {
      calculateDimensionX(node);
      m_currentPoint.x += this.basePositions.get(node) / 2 + this.distX;
      buildTree(node, this.m_currentPoint.x);
    }
  }

  protected void buildTree(N node, int x) {

    if (alreadyDone.add(node)) {
      //go one level further down
      this.m_currentPoint.y += this.distY;
      this.m_currentPoint.x = x;

      this.setCurrentPositionFor(node);

      int sizeXofCurrent = basePositions.get(node);

      int lastX = x - sizeXofCurrent / 2;

      int sizeXofChild;
      int startXofChild;

      for (N element : graph.successors(node)) {
        sizeXofChild = this.basePositions.get(element);
        startXofChild = lastX + sizeXofChild / 2;
        buildTree(element, startXofChild);
        lastX = lastX + sizeXofChild + distX;
      }
      this.m_currentPoint.y -= this.distY;
    }
  }

  private int calculateDimensionX(N node) {

    int size = 0;
    int childrenNum = graph.successors(node).size();

    if (childrenNum != 0) {
      for (N element : graph.successors(node)) {
        size += calculateDimensionX(element) + distX;
      }
    }
    size = Math.max(0, size - distX);
    basePositions.put(node, size);

    return size;
  }

  private int calculateDimensionX(Collection<N> roots) {

    int size = 0;
    for (N node : roots) {
      int childrenNum = graph.successors(node).size();

      if (childrenNum != 0) {
        for (N element : graph.successors(node)) {
          size += calculateDimensionX(element) + distX;
        }
      }
      size = Math.max(0, size - distX);
      basePositions.put(node, size);
    }

    return size;
  }

  /**
   * This method is not supported by this class. The size of the layout is determined by the
   * topology of the tree, and by the horizontal and vertical spacing (optionally set by the
   * constructor).
   */
  public void setSize(Dimension size) {
    throw new UnsupportedOperationException(
        "Size of TreeLayout is set" + " by node spacing in constructor");
  }

  protected void setCurrentPositionFor(N node) {
    int x = m_currentPoint.x;
    int y = m_currentPoint.y;
    if (x < 0) {
      size.width -= x;
    }

    if (x > size.width - distX) {
      size.width = x + distX;
    }

    if (y < 0) {
      size.height -= y;
    }
    if (y > size.height - distY) {
      size.height = y + distY;
    }
    locations.getUnchecked(node).setLocation(m_currentPoint);
  }

  public Dimension getSize() {
    return size;
  }

  public void initialize() {}

  public boolean isLocked(N node) {
    return false;
  }

  public void lock(N node, boolean state) {}

  public void reset() {}

  public void setInitializer(Function<N, Point2D> initializer) {}

  /** @return the center of this layout's area. */
  public Point2D getCenter() {
    return new Point2D.Double(size.getWidth() / 2, size.getHeight() / 2);
  }

  public void setLocation(N node, Point2D location) {
    locations.getUnchecked(node).setLocation(location);
  }

  public Point2D apply(N node) {
    return locations.getUnchecked(node);
  }

  @Override
  public Set<N> nodes() {
    return graph.nodes();
  }
}
