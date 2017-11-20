package edu.uci.ics.jung.visualization.spatial;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import edu.uci.ics.jung.layout.model.LayoutModel;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Spatial Data Structure to optimize rendering performance. The SpatialGrid is used to determine
 * which graph nodes are actually visible for a given rendering situation. Only the visible nodes
 * are passed to the rendering pipeline. When used with Edges, only Edges with at least one visible
 * endpoint are passed to the rendering pipeline.
 *
 * <p>See SimpleGraphSpatialTest (jung-samples) for a rendering that exposes the internals of the
 * SpatialGrid.
 *
 * @author Tom Nelson
 */
public class SpatialGrid<N> extends AbstractSpatial<N> implements Spatial<N> {

  private static final Logger log = LoggerFactory.getLogger(SpatialGrid.class);

  /** the number of grid cells across the width */
  private int horizontalCount;

  /** the number of grid cells across the height */
  private int verticalCount;

  /** the overall size of the area to be divided into a grid */
  private Dimension size;

  /** A mapping of grid cell identified to a collection of contained nodes */
  private Multimap<Integer, N> map = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());

  /** the width of a grid cell */
  private double boxWidth;
  /** the height of a grid cell */
  private double boxHeight;

  /** the overall area of the layout (x,y,width,height) */
  private Rectangle2D layoutArea;

  /** a cache of grid cell rectangles for performance */
  private List<Rectangle2D> gridCache;

  /**
   * Create an instance
   *
   * @param bounds the area of the grid
   * @param horizontalCount how many tiles in a row
   * @param verticalCount how many tiles in a column
   */
  public SpatialGrid(
      LayoutModel<N, Point2D> layoutModel,
      Rectangle2D bounds,
      int horizontalCount,
      int verticalCount) {
    super(layoutModel);
    this.horizontalCount = horizontalCount;
    this.verticalCount = verticalCount;
    this.setBounds(bounds);
  }

  /**
   * Set the layoutSize of the spatial grid and recompute the box widths and heights. null out the
   * obsolete grid cache
   *
   * @param bounds recalculate the size of the spatial area
   */
  public void setBounds(Rectangle2D bounds) {
    this.size = bounds.getBounds().getSize();
    this.layoutArea = bounds;
    this.boxWidth = size.getWidth() / horizontalCount;
    this.boxHeight = size.getHeight() / verticalCount;
    this.gridCache = null;
  }

  public static <N> List<Rectangle2D> getGrid(List<Rectangle2D> list, SpatialGrid<N> grid) {
    list.addAll(grid.getGrid());
    return list;
  }

  /**
   * Lazily compute the gridCache if needed. The gridCache is a list of rectangles overlaying the
   * layout area. They are numbered from 0 to horizontalCount*verticalCount-1
   *
   * @return the boxes in the grid
   */
  protected List<Rectangle2D> getGrid() {
    if (gridCache == null) {
      gridCache = Lists.newArrayList();
      for (int j = 0; j < verticalCount; j++) {
        for (int i = 0; i < horizontalCount; i++) {
          gridCache.add(
              new Rectangle2D.Double(
                  layoutArea.getX() + i * boxWidth,
                  layoutArea.getY() + j * boxHeight,
                  boxWidth,
                  boxHeight));
        }
      }
    }
    return gridCache;
  }

  /**
   * A Multimap of box number to Lists of nodes in that box
   *
   * @return the map of box numbers to contained nodes
   */
  public Multimap<Integer, N> getMap() {
    return map;
  }

  /**
   * given the box x,y coordinates (not the coordinate system) return the box number (0,0) has box 0
   * (horizontalCount,horizontalCount) has box horizontalCount*verticalCount - 1
   *
   * @param boxX the x value in the box grid
   * @param boxY the y value in the box grid
   * @return the box number for boxX,boxY
   */
  protected int getBoxNumber(int boxX, int boxY) {
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

  /**
   * @param boxXY the (x,y) in the grid coordinate system
   * @return the box number for that (x,y)
   */
  protected int getBoxNumber(int[] boxXY) {
    return getBoxNumber(boxXY[0], boxXY[1]);
  }

  /**
   * give a Point in the coordinate system, return the box number that contains it
   *
   * @param p a location in the coordinate system
   * @return the box number that contains the passed location
   */
  protected int getBoxNumberFromLocation(Point2D p) {
    int count = 0;
    for (Rectangle2D r : getGrid()) {
      if (r.contains(p) || r.intersects(p.getX(), p.getY(), 1, 1)) {
        return count;
      } else {
        count++;
      }
    }
    log.trace("no box for  {}", p);
    return -1;
  }

  /**
   * given (x,y) in the coordinate system, get the boxX,boxY for the box that it is in
   *
   * @param x a location in the coordinate system
   * @param y a location in the coordinate system
   * @return a 2 dimensional array of int containing the box x and y coordinates
   */
  protected int[] getBoxIndex(double x, double y) {

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

  /**
   * Recalculate the contents of the Map of box number to contained Nodes
   *
   * @param nodes the collection of nodes to update in the structure
   */
  @Override
  public void recalculate(Collection<N> nodes) {
    this.map.clear();
    while (true) {
      try {
        for (N node : nodes) {
          this.map.put(this.getBoxNumberFromLocation(layoutModel.apply(node)), node);
        }
        break;
      } catch (ConcurrentModificationException ex) {
        // ignore
      }
    }
  }

  /**
   * update the location of a node in the map of box number to node lists
   *
   * @param node the node to update in the structure
   */
  @Override
  public void update(N node) {
    Point2D location = layoutModel.apply(node);
    if (!this.getLayoutArea().contains(location)) {
      log.trace(location + " outside of spatial " + this.getLayoutArea());
      this.setBounds(this.getUnion(this.getLayoutArea(), location));
      recalculate(layoutModel.getGraph().nodes());
    }

    int rightBox = this.getBoxNumberFromLocation(layoutModel.apply(node));
    // node should end up in box 'rightBox'
    // check to see if it is already there
    if (map.get(rightBox).contains(node)) {
      // nothing to do here, just return
      return;
    }
    // remove node from the first (and only) wrong box it is found in
    Integer wrongBox = null;
    synchronized (map) {
      for (Integer box : map.keySet()) {
        if (map.get(box).contains(node)) {
          // remove it and stop, because node can be in only one box
          wrongBox = box;
          break;
        }
      }
    }
    if (wrongBox != null) {
      map.remove(wrongBox, node);
    }
    map.put(rightBox, node);
  }

  /**
   * given a rectangular area and an offset, return the tile numbers that are contained in it
   *
   * @param visibleArea the (possibly) non-rectangular area of interest
   * @return the tile numbers that intersect with the visibleArea
   */
  protected Collection<Integer> getVisibleTiles(Shape visibleArea) {
    Set<Integer> visibleTiles = Sets.newHashSet();
    List<Rectangle2D> grid = getGrid();
    for (int i = 0; i < this.horizontalCount * this.verticalCount; i++) {
      if (visibleArea.intersects(grid.get(i))) {
        visibleTiles.add(i);
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("visible boxes:{}", visibleTiles);
    }
    return visibleTiles;
  }

  /**
   * Given an area, return a collection of the nodes that are contained in it (the nodes that are
   * contained in the boxes that intersect with the area)
   *
   * @param visibleArea a shape projected on the grid
   * @return the nodes that should be visible
   */
  @Override
  public Collection<N> getVisibleNodes(Shape visibleArea) {
    Area area = new Area(visibleArea);
    area.intersect(new Area(this.layoutArea));
    if (log.isTraceEnabled()) {
      log.trace("map is {}", map);
    }
    Collection<N> visibleNodes = Sets.newHashSet();
    Collection<Integer> tiles = getVisibleTiles(area);
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
      log.debug("visibleNodes:{}", visibleNodes);
    }
    return visibleNodes;
  }

  /** @return the layout area rectangle for this grid */
  @Override
  public Rectangle2D getLayoutArea() {
    return layoutArea;
  }
}
