package edu.uci.ics.jung.visualization.spatial;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
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

  private Rectangle2D layoutArea;

  /**
   * Create an instance
   *
   * @param bounds the area of the grid
   * @param horizontalCount how many tiles in a row
   * @param verticalCount how many tiles in a column
   */
  public SpatialGrid(Rectangle2D bounds, int horizontalCount, int verticalCount) {
    this.horizontalCount = horizontalCount;
    this.verticalCount = verticalCount;
    this.setBounds(bounds);
  }

  public void setBounds(Rectangle2D bounds) {
    this.size = bounds.getBounds().getSize();
    this.layoutArea = bounds;
    this.boxWidth = size.getWidth() / horizontalCount;
    this.boxHeight = size.getHeight() / verticalCount;
  }

  public Collection<Rectangle2D> getGrid() {
    Collection<Rectangle2D> grid = Lists.newArrayList();
    for (int j = 0; j < verticalCount; j++) {
      for (int i = 0; i < horizontalCount; i++) {
        grid.add(
            new Rectangle2D.Double(
                this.layoutArea.getX() + i * boxWidth,
                this.layoutArea.getY() + j * boxHeight,
                boxWidth,
                boxHeight));
      }
    }
    return grid;
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
    if (log.isTraceEnabled()) {
      if (log.isTraceEnabled()) {
        log.trace(
            "{},{} clamped to {},{}",
            boxX,
            boxY,
            Math.max(0, Math.min(boxX, this.horizontalCount - 1)),
            Math.max(0, Math.min(boxY, this.verticalCount - 1)));
      }
    }
    boxX = Math.max(0, Math.min(boxX, this.horizontalCount - 1));
    boxY = Math.max(0, Math.min(boxY, this.verticalCount - 1));
    if (log.isTraceEnabled()) {
      log.trace("getBoxNumber({},{}):{}", boxX, boxY, (boxY * this.horizontalCount + boxX));
    }
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
    int count = 0;
    for (Rectangle2D r : getGrid()) {
      if (r.contains(p)) {
        if (log.isTraceEnabled()) {
          log.trace("r:{} contains {}", r.getBounds2D(), p);
        }
        return count;
      } else {
        count++;
      }
    }
    return -1;
  }

  /**
   * given (x,y) in the coordinate system, get the boxX,boxY for the box that it is in
   *
   * @param x
   * @param y
   * @return
   */
  public int[] getBoxIndex(double x, double y) {

    // clamp the x and y to be within the bounds of the layout grid
    int[] boxIndex = new int[2];
    int hcount = 0;
    int vcount = 0;
    for (Rectangle2D r : getGrid()) {
      if (r.contains(new Point2D.Double(x, y))) {
        boxIndex = new int[] {hcount, vcount};
        break;
      }
      hcount++;
      if (hcount >= this.horizontalCount) {
        hcount = 0;
        vcount++;
      }
    }
    if (log.isTraceEnabled()) {
      log.trace("boxIndex for ({},{}) is {}", x, y, Arrays.toString(boxIndex));
    }
    return boxIndex;
  }

  public void recalculate(Function<N, Point2D> layout, Collection<N> nodes) {
    this.map.clear();
    for (N node : nodes) {
      this.map.put(this.getBoxNumberFromLocation(layout.apply(node)), node);
    }
  }
  /** given a rectangular area and an offset, return the tiles that are contained in it */
  public Collection<Integer> getVisibleTiles(Rectangle2D visibleArea) {
    Set<Integer> visibleTiles = Sets.newHashSet();
    int[] upperLeftBox = getBoxIndex(visibleArea.getMinX(), visibleArea.getMinY());
    int[] lowerRightBox = getBoxIndex(visibleArea.getMaxX() - 1, visibleArea.getMaxY() - 1);
    for (int i = upperLeftBox[0]; i <= lowerRightBox[0]; i++) {
      for (int j = upperLeftBox[1]; j <= lowerRightBox[1]; j++) {
        int boxNumber = getBoxNumber(i, j);
        if (boxNumber >= 0) {
          visibleTiles.add(boxNumber);
        }
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("visibleTiles are {}", visibleTiles);
    }
    return visibleTiles;
  }

  public Collection<N> getVisibleNodes(Rectangle2D visibleArea) {
    visibleArea = visibleArea.createIntersection(this.layoutArea);
    if (log.isDebugEnabled()) {
      log.debug("visibleArea: {}", visibleArea);
      log.debug("map is {}", map);
    }
    Collection<N> visibleNodes = Sets.newHashSet();
    Collection<Integer> tiles = getVisibleTiles(visibleArea);
    for (Integer index : tiles) {
      Collection<N> toAdd = this.map.get(index);
      if (toAdd.size() > 0) {
        visibleNodes.addAll(toAdd);
        if (log.isTraceEnabled()) {
          log.trace("added all of: {} from index {} to visibleNodes", toAdd, index);
        }
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("visibleNodes in tiles:{}", tiles);
      log.debug("  in visibleArea:{}", visibleArea.getBounds2D());
      log.debug("    are:{}", visibleNodes);
    }
    return visibleNodes;
  }

  public Rectangle2D getLayoutArea() {
    return layoutArea;
  }

  public void setLayoutArea(Rectangle2D layoutArea) {
    this.layoutArea = layoutArea;
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
