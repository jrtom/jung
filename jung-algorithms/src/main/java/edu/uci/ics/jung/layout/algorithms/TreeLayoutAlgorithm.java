/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 9, 2005
 */

package edu.uci.ics.jung.layout.algorithms;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import edu.uci.ics.jung.graph.util.TreeUtils;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Karlheinz Toni
 * @author Tom Nelson - converted to jung2, refactored into Algorithm/Visitor
 */
public class TreeLayoutAlgorithm<N> implements LayoutAlgorithm<N> {

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
  public TreeLayoutAlgorithm() {
    this(DEFAULT_DISTX, DEFAULT_DISTY);
  }

  /**
   * Creates an instance for the specified graph and X distance with default Y distance.
   *
   * @param distx the horizontal spacing between adjacent siblings
   */
  public TreeLayoutAlgorithm(int distx) {
    this(distx, DEFAULT_DISTY);
  }

  /**
   * Creates an instance for the specified graph, X distance, and Y distance.
   *
   * @param distx the horizontal spacing between adjacent siblings
   * @param disty the vertical spacing between adjacent siblings
   */
  public TreeLayoutAlgorithm(int distx, int disty) {
    Preconditions.checkArgument(distx >= 1, "X distance must be positive");
    Preconditions.checkArgument(disty >= 1, "Y distance must be positive");
    this.distX = distx;
    this.distY = disty;
    this.currentX = this.currentY = 0;
  }

  @Override
  public void visit(LayoutModel<N> layoutModel) {
    buildTree(layoutModel);
  }

  protected void buildTree(LayoutModel<N> layoutModel) {
    alreadyDone = Sets.newHashSet();
    this.currentX = 0;
    this.currentY = 0;
    Set<N> roots = TreeUtils.roots(layoutModel.getGraph());
    Preconditions.checkArgument(roots.size() > 0);
    int overallWidth = calculateDimensionX(layoutModel, roots);
    overallWidth += (roots.size() + 1) * distX;
    int overallHeight = calculateDimensionY(layoutModel, roots);
    overallHeight += 2 * distY;
    layoutModel.setSize(
        Math.max(layoutModel.getWidth(), overallWidth),
        Math.max(layoutModel.getHeight(), overallHeight));
    for (N node : roots) {
      calculateDimensionX(layoutModel, node);
      currentX += (this.basePositions.get(node) / 2 + this.distX);
      buildTree(layoutModel, node, (int) currentX);
    }
  }

  protected void buildTree(LayoutModel<N> layoutModel, N node, int x) {
    if (alreadyDone.add(node)) {
      //go one level further down
      double newY = this.currentY + this.distY;
      this.currentX = x;
      this.currentY = newY;
      layoutModel.set(node, currentX, currentY);

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

  private int calculateDimensionX(LayoutModel<N> layoutModel, N node) {

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

  private int calculateDimensionX(LayoutModel<N> layoutModel, Collection<N> roots) {

    int size = 0;
    for (N node : roots) {
      size += calculateDimensionX(layoutModel, node);
    }

    return size;
  }

  private int calculateDimensionY(LayoutModel<N> layoutModel, N node) {

    int size = 0;
    int childrenNum = layoutModel.getGraph().successors(node).size();

    if (childrenNum != 0) {
      for (N element : layoutModel.getGraph().successors(node)) {
        size = Math.max(size, calculateDimensionY(layoutModel, element) + distX);
      }
    }
    return size;
  }

  private int calculateDimensionY(LayoutModel<N> layoutModel, Collection<N> roots) {

    int size = 0;
    for (N node : roots) {
      size += calculateDimensionY(layoutModel, node);
    }
    return size;
  }

  public void setInitializer(Function<N, Point> initializer) {}

  /** @return the center of this layout's area. */
  public Point getCenter(LayoutModel<N> layoutModel) {
    return Point.of(layoutModel.getWidth() / 2, layoutModel.getHeight() / 2);
  }
}
