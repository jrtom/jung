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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
  public static int DEFAULT_HORIZONTAL_NODE_SPACING = 50;

  /** The default vertical node spacing. Initialized to 50. */
  public static int DEFAULT_VERTICAL_NODE_SPACING = 50;

  /** The horizontal node spacing. Defaults to {@code DEFAULT_HORIZONTAL_NODE_SPACING}. */
  protected int horizontalNodeSpacing = 50;

  /** The vertical node spacing. Defaults to {@code DEFAULT_VERTICAL_NODE_SPACING}. */
  protected int verticalNodeSpacing = 50;

  protected double currentX;
  protected double currentY;

  /** Creates an instance for the specified graph with default X and Y distances. */
  public TreeLayoutAlgorithm() {
    this(DEFAULT_HORIZONTAL_NODE_SPACING, DEFAULT_VERTICAL_NODE_SPACING);
  }

  /**
   * Creates an instance for the specified graph and X distance with default Y distance.
   *
   * @param horizontalNodeSpacing the horizontal spacing between adjacent siblings
   */
  public TreeLayoutAlgorithm(int horizontalNodeSpacing) {
    this(horizontalNodeSpacing, DEFAULT_VERTICAL_NODE_SPACING);
  }

  /**
   * Creates an instance for the specified graph, X distance, and Y distance.
   *
   * @param horizontalNodeSpacing the horizontal spacing between adjacent siblings
   * @param verticalNodeSpacing the vertical spacing between adjacent siblings
   */
  public TreeLayoutAlgorithm(int horizontalNodeSpacing, int verticalNodeSpacing) {
    Preconditions.checkArgument(
        horizontalNodeSpacing >= 1, "horizontalNodeSpacing must be positive");
    Preconditions.checkArgument(horizontalNodeSpacing >= 1, "verticalNodeSpacing must be positive");
    this.horizontalNodeSpacing = horizontalNodeSpacing;
    this.verticalNodeSpacing = verticalNodeSpacing;
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
    // the width of the tree under 'roots'. Includes one 'horizontalNodeSpacing' per child node
    int overallWidth = calculateWidth(layoutModel, roots);
    // add one additional 'horizontalNodeSpacing' for each tree (each root)
    overallWidth += (roots.size() + 1) * horizontalNodeSpacing;
    int overallHeight = calculateHeight(layoutModel, roots);
    overallHeight += 2 * verticalNodeSpacing;
    layoutModel.setSize(
        Math.max(layoutModel.getWidth(), overallWidth),
        Math.max(layoutModel.getHeight(), overallHeight));
    for (N node : roots) {
      calculateWidth(layoutModel, node);
      currentX += (this.basePositions.get(node) / 2 + this.horizontalNodeSpacing);
      buildTree(layoutModel, node, (int) currentX);
    }
  }

  protected void buildTree(LayoutModel<N> layoutModel, N node, int x) {
    if (alreadyDone.add(node)) {
      //go one level further down
      double newY = this.currentY + this.verticalNodeSpacing;
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

        lastX = lastX + sizeXofChild + horizontalNodeSpacing;
      }

      this.currentY -= this.verticalNodeSpacing;
    }
  }

  private int calculateWidth(LayoutModel<N> layoutModel, N node) {

    int size = 0;
    for (N element : layoutModel.getGraph().successors(node)) {
      size += calculateWidth(layoutModel, element) + horizontalNodeSpacing;
    }
    size = Math.max(0, size - horizontalNodeSpacing);
    basePositions.put(node, size);

    return size;
  }

  private int calculateWidth(LayoutModel<N> layoutModel, Collection<N> roots) {

    int size = 0;
    for (N node : roots) {
      size += calculateWidth(layoutModel, node);
    }

    return size;
  }

  private int calculateHeight(LayoutModel<N> layoutModel, N node) {

    int size = 0;
    for (N element : layoutModel.getGraph().successors(node)) {
      size = Math.max(size, calculateHeight(layoutModel, element) + verticalNodeSpacing);
    }
    return size;
  }

  private int calculateHeight(LayoutModel<N> layoutModel, Collection<N> roots) {

    int size = 0;
    for (N node : roots) {
      size += calculateHeight(layoutModel, node);
    }
    return size;
  }

  /** @return the center of this layout's area. */
  public Point getCenter(LayoutModel<N> layoutModel) {
    return Point.of(layoutModel.getWidth() / 2, layoutModel.getHeight() / 2);
  }
}
