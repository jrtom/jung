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

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import edu.uci.ics.jung.graph.util.TreeUtils;
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

  protected transient P m_currentPoint; // = new Point();

  /** Creates an instance for the specified graph with default X and Y distances. */
  public TreeLayoutAlgorithm(DomainModel<P> domainModel) {
    this(domainModel, DEFAULT_DISTX, DEFAULT_DISTY);
  }

  /**
   * Creates an instance for the specified graph and X distance with default Y distance.
   *
   * @param distx the horizontal spacing between adjacent siblings
   */
  public TreeLayoutAlgorithm(DomainModel<P> domainModel, int distx) {
    this(domainModel, distx, DEFAULT_DISTY);
  }

  /**
   * Creates an instance for the specified graph, X distance, and Y distance.
   *
   * @param distx the horizontal spacing between adjacent siblings
   * @param disty the vertical spacing between adjacent siblings
   */
  public TreeLayoutAlgorithm(DomainModel<P> domainModel, int distx, int disty) {
    super(domainModel);
    Preconditions.checkArgument(distx >= 1, "X distance must be positive");
    Preconditions.checkArgument(disty >= 1, "Y distance must be positive");
    this.distX = distx;
    this.distY = disty;
    this.m_currentPoint = domainModel.newPoint(0, 0);
  }

  public void visit(LayoutModel<N, P> layoutModel) {
    buildTree(layoutModel);
  }

  protected void buildTree(LayoutModel<N, P> layoutModel) {
    alreadyDone = Sets.newHashSet();
    this.m_currentPoint = domainModel.newPoint(0, 20); //new Point(0, 20);
    Set<N> roots = TreeUtils.roots(layoutModel.getGraph());
    Preconditions.checkArgument(roots.size() > 0);
    calculateDimensionX(layoutModel, roots);
    for (N node : roots) {
      calculateDimensionX(layoutModel, node);

      domainModel.offset(m_currentPoint, this.basePositions.get(node) / 2 + this.distX, 0);
      buildTree(layoutModel, node, (int) domainModel.getX(this.m_currentPoint));
    }
  }

  protected void buildTree(LayoutModel<N, P> layoutModel, N node, int x) {

    if (alreadyDone.add(node)) {
      //go one level further down
      double newY = domainModel.getY(m_currentPoint) + this.distY;
      //      domainModel.offset(m_currentPoint, 0, this.distY);
      domainModel.setLocation(m_currentPoint, x, newY);

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

      domainModel.offset(m_currentPoint, 0, -this.distY);
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
      //      int childrenNum = layoutModel.getGraph().successors(node).size();
      //
      //      if (childrenNum != 0) {
      //        for (N element : layoutModel.getGraph().successors(node)) {
      //          size += calculateDimensionX(layoutModel, element) + distX;
      //        }
      //      }
      //      size = Math.max(0, size - distX);
      //      basePositions.put(node, size);
    }

    return size;
  }

  protected void setCurrentPositionFor(LayoutModel<N, P> layoutModel, N node) {
    int width = layoutModel.getWidth();
    int height = layoutModel.getHeight();
    int x = (int) domainModel.getX(m_currentPoint);
    int y = (int) domainModel.getY(m_currentPoint);
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
    domainModel.setLocation(location, m_currentPoint);
    //    layoutModel.set(node, got, true);
    setLocation(layoutModel, node, location);
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

  public boolean isLocked(N node) {
    return false;
  }

  public void lock(N node, boolean state) {}

  public void reset() {}

  public void setInitializer(Function<N, P> initializer) {}

  /** @return the center of this layout's area. */
  public P getCenter(LayoutModel<N, P> layoutModel) {
    return domainModel.newPoint(layoutModel.getWidth() / 2, layoutModel.getHeight() / 2);
  }
}
