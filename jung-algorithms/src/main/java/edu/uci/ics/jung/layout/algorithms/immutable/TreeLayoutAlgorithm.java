/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 9, 2005
 */

package edu.uci.ics.jung.layout.algorithms.immutable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import edu.uci.ics.jung.graph.util.TreeUtils;
import edu.uci.ics.jung.layout.algorithms.AbstractLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.PointModel;
import java.util.*;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Karlheinz Toni
 * @author Tom Nelson - converted to jung2, refactored into Algorithm/Visitor
 */
public class TreeLayoutAlgorithm<N, P> extends AbstractLayoutAlgorithm<N, P> {

  private static final Logger log = LoggerFactory.getLogger(TreeLayoutAlgorithm.class);

  protected Map<N, Integer> basePositions = new HashMap<N, Integer>();

  protected transient Set<N> alreadyDone = new HashSet<N>();

  /** The default horizontal node spacing. Initialized to 50. */
  public static int DEFAULT_DISTX = 50;

  /** The default vertical node spacing. Initialized to 50. */
  public static int DEFAULT_DISTY = 50;

  /** The horizontal node spacing. Defaults to {@code DEFAULT_XDIST}. */
  protected int distX = 50;

  /** The vertical node spacing. Defaults to {@code DEFAULT_YDIST}. */
  protected int distY = 50;

  protected double currentX;
  protected double currentY;

  /** Creates an instance for the specified graph with default X and Y distances. */
  public TreeLayoutAlgorithm(PointModel<P> pointModel) {
    this(pointModel, DEFAULT_DISTX, DEFAULT_DISTY);
  }

  /**
   * Creates an instance for the specified graph and X distance with default Y distance.
   *
   * @param distx the horizontal spacing between adjacent siblings
   */
  public TreeLayoutAlgorithm(PointModel<P> pointModel, int distx) {
    this(pointModel, distx, DEFAULT_DISTY);
  }

  /**
   * Creates an instance for the specified graph, X distance, and Y distance.
   *
   * @param distx the horizontal spacing between adjacent siblings
   * @param disty the vertical spacing between adjacent siblings
   */
  public TreeLayoutAlgorithm(PointModel<P> pointModel, int distx, int disty) {
    super(pointModel);
    Preconditions.checkArgument(distx >= 1, "X distance must be positive");
    Preconditions.checkArgument(disty >= 1, "Y distance must be positive");
    this.distX = distx;
    this.distY = disty;
    this.currentX = this.currentY = 0;
  }

  public void visit(LayoutModel<N, P> layoutModel) {
    buildTree(layoutModel);
  }

  protected void buildTree(LayoutModel<N, P> layoutModel) {
    alreadyDone = Sets.newHashSet();
    this.currentX = 0;
    this.currentY = 20;
    Set<N> roots = TreeUtils.roots(layoutModel.getGraph());
    Preconditions.checkArgument(roots.size() > 0);
    calculateDimensionX(layoutModel, roots);
    for (N node : roots) {
      calculateDimensionX(layoutModel, node);
      double posX = this.basePositions.get(node) / 2 + this.distX;
      buildTree(layoutModel, node, (int) posX);
    }
  }

  protected void buildTree(LayoutModel<N, P> layoutModel, N node, int x) {

    if (alreadyDone.add(node)) {
      //go one level further down
      double newY = this.currentY + this.distY;
      this.currentX = x;
      this.currentY = newY;
      this.setCurrentPositionFor(layoutModel, node);

      int sizeXofCurrent = basePositions.get(node);

      int lastX = x - sizeXofCurrent / 2;

      int sizeXofChild;
      int startXofChild;

      for (N element : layoutModel.getGraph().successors(node)) {
        sizeXofChild = this.basePositions.get(element);
        startXofChild = lastX + sizeXofChild / 2;
        buildTree(layoutModel, element, startXofChild);

        lastX = lastX + sizeXofChild + distX;
      }

      this.currentY -= this.distY;
    }
  }

  private int calculateDimensionX(LayoutModel<N, P> layoutModel, N node) {

    int size = 0;
    int childrenNum = layoutModel.getGraph().successors(node).size();

    if (childrenNum != 0) {
      for (N element : layoutModel.getGraph().successors(node)) {
        size += calculateDimensionX(layoutModel, element) + distX;
      }
    }
    size = Math.max(0, size - distX);
    basePositions.put(node, size);

    return size;
  }

  private int calculateDimensionX(LayoutModel<N, P> layoutModel, Collection<N> roots) {

    int size = 0;
    for (N node : roots) {
      size += calculateDimensionX(layoutModel, node);
    }

    return size;
  }

  protected void setCurrentPositionFor(LayoutModel<N, P> layoutModel, N node) {
    int width = layoutModel.getWidth();
    int height = layoutModel.getHeight();
    int x = (int) this.currentX;
    int y = (int) this.currentY;
    if (x < 0) {
      width -= x;
    }

    if (x >= width - distX) {
      width = x + distX;
    }

    if (y < 0) {
      height -= y;
    }
    if (y >= height - distY) {
      height = y + distY;
    }
    if (layoutModel.getWidth() < width || layoutModel.getHeight() < height) {
      layoutModel.setSize(width, height);
    }

    P location = layoutModel.get(node);
    setLocation(layoutModel, node, this.currentX, this.currentY);
  }

  /**
   * can be overridden to add behavior
   *
   * @param layoutModel
   * @param node
   * @param location
   */
  protected void setLocation(LayoutModel<N, P> layoutModel, N node, P location) {
    layoutModel.set(node, location, true);
  }

  protected void setLocation(LayoutModel<N, P> layoutModel, N node, double x, double y) {
    layoutModel.set(node, x, y, true);
  }

  public boolean isLocked(N node) {
    return false;
  }

  public void lock(N node, boolean state) {}

  public void reset() {}

  public void setInitializer(Function<N, P> initializer) {}

  /** @return the center of this layout's area. */
  public P getCenter(LayoutModel<N, P> layoutModel) {
    return pointModel.newPoint(layoutModel.getWidth() / 2, layoutModel.getHeight() / 2);
  }
}
