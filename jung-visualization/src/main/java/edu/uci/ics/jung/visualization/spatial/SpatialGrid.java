package edu.uci.ics.jung.visualization.spatial;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by Tom Nelson */
public class SpatialGrid<N> implements Spatial<N> {

  Logger log = LoggerFactory.getLogger(SpatialGrid.class);

  private int horizontalCount;

  private int verticalCount;

  private Dimension size;

  private Multimap<Integer, N> map = ArrayListMultimap.create();

  private double boxWidth;
  private double boxHeight;

  private Rectangle visibleArea;

  /**
   * Create an instance
   *
   * @param bounds the area of the grid
   * @param horizontalCount how many tiles in a row
   * @param verticalCount how many tiles in a column
   */
  public SpatialGrid(Rectangle bounds, int horizontalCount, int verticalCount) {
    this.size = bounds.getSize();
    this.visibleArea = bounds;
    this.horizontalCount = horizontalCount;
    this.verticalCount = verticalCount;
    this.boxWidth = size.getWidth() / horizontalCount;
    this.boxHeight = size.getHeight() / verticalCount;
  }

  /**
   * A Multimap of box number to Lists of nodes in that box
   *
   * @return
   */
  public Multimap<Integer, N> getMap() {
    return map;
  }

  /**
   * given the box x,y coordinates (not the coordinate system) return the box number (0,0) has box 0
   * (horizontalCount,horizontalCount) has box horizontalCount*verticalCount - 1
   *
   * @param boxX
   * @param boxY
   * @return
   */
  public int getBoxNumber(int boxX, int boxY) {
    return boxY * this.horizontalCount + boxX;
  }

  public int getBoxNumber(int[] boxXY) {
    return getBoxNumber(boxXY[0], boxXY[1]);
  }

  /**
   * given a box number, return the x,y coordinates in the grid coordinate system
   *
   * @param boxIndex
   * @return
   */
  public int[] getBoxXYFromBoxIndex(int boxIndex) {
    int[] boxXY = new int[2];
    boxXY[0] = boxIndex % this.horizontalCount;
    boxXY[1] = boxIndex / this.horizontalCount;
    return boxXY;
  }

  /**
   * given x,y in the view coordinate system, return the box number that contains it
   *
   * @param x
   * @param y
   * @return
   */
  public int getBoxNumberFromLocation(int x, int y) {
    return this.getBoxNumber(this.getBoxIndex(x, y));
  }

  /**
   * give a Point in the coordinate system, return the box number that contains it
   *
   * @param p
   * @return
   */
  public int getBoxNumberFromLocation(Point2D p) {
    if (p == null) {
      return -1;
    }
    return this.getBoxNumber(this.getBoxIndex((int) p.getX(), (int) p.getY()));
  }

  /**
   * given (x,y) in the coordinate system, get the boxX,boxY for the box that it is in
   *
   * @param x
   * @param y
   * @return
   */
  public int[] getBoxIndex(int x, int y) {
    int[] boxIndex = new int[2];

    boxIndex[0] = (int) ((double) x / boxWidth);
    boxIndex[1] = (int) ((double) y / boxHeight);
    return boxIndex;
  }

  /**
   * update the location of a node in the map of box number to node lists
   *
   * @param node
   * @param x
   * @param y
   */
  public void updateBox(N node, int x, int y) {
    int newBoxNumber = this.getBoxNumberFromLocation(x, y);
    ListMultimap<N, Integer> inverse = Multimaps.invertFrom(map, ArrayListMultimap.create());
    List<Integer> oldBoxes = inverse.get(node);
    if (oldBoxes.contains(newBoxNumber) == false) {
      map.removeAll(node);
      map.put(newBoxNumber, node);
    }
  }

  /** given a rectangular area and an offset, return the tiles that are contained in it */
  public Collection<Integer> getVisibleTiles(Rectangle visibleArea) {
    Dimension d = visibleArea.getSize();
    int xOffset = visibleArea.x;
    int yOffset = visibleArea.y;
    Set<Integer> visibleTiles = Sets.newHashSet();
    int[] upperLeftBox = getBoxIndex(xOffset, yOffset);
    int[] lowerRightBox =
        getBoxIndex(
            Math.min(size.width - 1, xOffset + d.width - 1),
            Math.min(size.height - 1, yOffset + d.height - 1));
    for (int i = upperLeftBox[0]; i <= lowerRightBox[0]; i++) {
      for (int j = upperLeftBox[1]; j <= lowerRightBox[1]; j++) {
        visibleTiles.add(getBoxNumber(i, j));
      }
    }
    return visibleTiles;
  }

  /**
   * given a rectangular area and an offset, return the tiles that are contained in it
   *
   * @param d
   * @param xOffset
   * @param yOffset
   * @return
   */
  public Collection<Integer> getVisibleTiles(Dimension d, int xOffset, int yOffset) {
    Set<Integer> visibleTiles = Sets.newHashSet();
    int[] upperLeftBox = getBoxIndex(xOffset, yOffset);
    int[] lowerRightBox =
        getBoxIndex(
            Math.min(size.width - 1, xOffset + d.width - 1),
            Math.min(size.height - 1, yOffset + d.height - 1));
    for (int i = upperLeftBox[0]; i <= lowerRightBox[0]; i++) {
      for (int j = upperLeftBox[1]; j <= lowerRightBox[1]; j++) {
        visibleTiles.add(getBoxNumber(i, j));
      }
    }
    return visibleTiles;
  }

  public void setVisibleArea(Rectangle visibleArea) {
    this.visibleArea = visibleArea;
  }
  /**
   * given a rectangular area and an offset, return the nodes that are contained in it.
   *
   * @param d
   * @param xOffset
   * @param yOffset
   * @return
   */
  public Collection<N> getVisibleNodes(Dimension d, int xOffset, int yOffset) {
    Collection<N> visibleNodes = Sets.newHashSet();
    for (Integer index : getVisibleTiles(d, xOffset, yOffset)) {
      visibleNodes.addAll(this.map.get(index));
    }
    log.debug(
        "visibleNodes in x:" + xOffset + ",y:" + yOffset + ",d:" + d + " are " + visibleNodes);
    return visibleNodes;
  }

  public Collection<N> getVisibleNodes(Rectangle visibleArea) {
    Collection<N> visibleNodes = Sets.newHashSet();
    for (Integer index : getVisibleTiles(visibleArea)) {
      visibleNodes.addAll(this.map.get(index));
    }
    log.debug("visibleNodes in " + visibleArea + " are " + visibleNodes);
    return visibleNodes;
  }

  /** given a rectangular area and an offset, return the nodes that are contained in it. */
  public Collection<N> getVisibleNodes() {
    return getVisibleNodes(this.visibleArea);
  }

  @Override
  public Rectangle getVisibleArea() {
    return this.visibleArea;
  }

  /**
   * given the boxX, boxY for a box, get the list of adjacent boxes, including myself
   *
   * @param boxX
   * @param boxY
   * @return
   */
  public List<Integer> getAdjacentCellList(int boxX, int boxY) {
    List<Integer> adjacents = Lists.newArrayList();
    final int[] foo = {-1, 0, 1};
    for (int i : foo) {
      for (int j : foo) {
        //                if (i != 0 || j != 0) {
        int x = boxX + i;
        int y = boxY + j;
        if (0 <= x && x < horizontalCount && 0 <= y && y < verticalCount) {
          adjacents.add(y * horizontalCount + x);
        }
      }
    }
    return adjacents;
  }

  public List<Integer> getAdjacentCellList(int boxIndex) {
    int[] boxXY = getBoxXYFromBoxIndex(boxIndex);
    return getAdjacentCellList(boxXY[0], boxXY[1]);
  }
}
