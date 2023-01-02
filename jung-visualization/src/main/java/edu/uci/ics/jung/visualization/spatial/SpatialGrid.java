package edu.uci.ics.jung.visualization.spatial;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
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
public class SpatialGrid<N> extends AbstractSpatial<N, N> implements Spatial<N>, TreeNode {

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
  private List<Shape> gridCache;

  /**
   * Create an instance
   *
   * @param layoutModel
   */
  public SpatialGrid(LayoutModel<N> layoutModel) {
    super(layoutModel);
    this.horizontalCount = 10;
    this.verticalCount = 10;
    this.setBounds(new Rectangle2D.Double(0, 0, layoutModel.getWidth(), layoutModel.getHeight()));
  }

  /**
   * Create an instance
   *
   * @param bounds the area of the grid
   * @param horizontalCount how many tiles in a row
   * @param verticalCount how many tiles in a column
   */
  public SpatialGrid(
      LayoutModel<N> layoutModel, Rectangle2D bounds, int horizontalCount, int verticalCount) {
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

  @Override
  public Set<TreeNode> getContainingLeafs(Point2D p) {
    int boxNumber = this.getBoxNumberFromLocation(p.getX(), p.getY());
    Rectangle2D r = (Rectangle2D) this.gridCache.get(boxNumber);
    SpatialGrid grid = new SpatialGrid(layoutModel, r, 1, 1);
    return Collections.singleton(grid);
  }

  @Override
  public Set<TreeNode> getContainingLeafs(double x, double y) {
    return getContainingLeafs(new Point2D.Double(x, y));
  }

  @Override
  public TreeNode getContainingLeaf(Object element) {
    for (Map.Entry<Integer, Collection<N>> entry : map.asMap().entrySet()) {
      if (entry.getValue().contains(element)) {
        int index = entry.getKey();
        Rectangle2D r = (Rectangle2D) this.gridCache.get(index);
        return new SpatialGrid<>(layoutModel, r, 1, 1);
      }
    }
    return null;
  }

  public static <N> List<Shape> getGrid(List<Shape> list, SpatialGrid<N> grid) {
    list.addAll(grid.getGrid());
    return list;
  }

  /**
   * Lazily compute the gridCache if needed. The gridCache is a list of rectangles overlaying the
   * layout area. They are numbered from 0 to horizontalCount*verticalCount-1
   *
   * @return the boxes in the grid
   */
  @Override
  public List<Shape> getGrid() {
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
  protected int getBoxNumberFromLocation(Point p) {
    int count = 0;
    for (Shape shape : getGrid()) {
      Rectangle2D r = shape.getBounds2D();
      if (r.contains(p.x, p.y) || r.intersects(p.x, p.y, 1, 1)) {
        return count;
      } else {
        count++;
      }
    }
    log.trace("no box for  {}", p);
    return -1;
  }

  /**
   * give a Point in the coordinate system, return the box number that contains it
   *
   * @param x, y a location in the coordinate system
   * @return the box number that contains the passed location
   */
  protected int getBoxNumberFromLocation(double x, double y) {
    int count = 0;
    for (Shape shape : getGrid()) {
      Rectangle2D r = shape.getBounds2D();
      if (r.contains(x, y) || r.intersects(x, y, 1, 1)) {
        return count;
      } else {
        count++;
      }
    }
    log.trace("no box for {},{}", x, y);
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
    for (Shape r : getGrid()) {
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

  @Override
  public void recalculate() {
    if (isActive()) {
      recalculate(layoutModel.getGraph().nodes());
    }
  }

  @Override
  public void clear() {
    this.map.clear();
  }

  /**
   * Recalculate the contents of the Map of box number to contained Nodes
   *
   * @param nodes the collection of nodes to update in the structure
   */
  public void recalculate(Collection<N> nodes) {
    clear();
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
  public void update(N node, Point location) {
    if (isActive()) {
      if (!this.getLayoutArea().contains(location.x, location.y)) {
        log.trace(location + " outside of spatial " + this.getLayoutArea());
        this.setBounds(this.getUnion(this.getLayoutArea(), location.x, location.y));
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
  }

  @Override
  public N getClosestElement(Point2D p) {
    return getClosestElement(p.getX(), p.getY());
  }

  @Override
  public N getClosestElement(double x, double y) {
    if (!isActive()) {
      return fallback.getNode(layoutModel, x, y);
    }
    Collection<TreeNode> leafs = getContainingLeafs(x, y);
    if (leafs.size() != 0) {
      TreeNode leaf = leafs.iterator().next();

      Rectangle2D area = leaf.getBounds();
      double radius = area.getWidth();
      N closest = null;
      while (closest == null) {

        double diameter = radius * 2;

        Ellipse2D searchArea = new Ellipse2D.Double(x - radius, y - radius, diameter, diameter);

        Collection<N> nodes = getVisibleElements(searchArea);
        closest = getClosest(nodes, x, y, radius);

        // if I have already considered all of the nodes in the graph
        // (in the spatialquadtree) there is no reason to enlarge the
        // area and try again
        if (nodes.size() >= layoutModel.getGraph().nodes().size()) {
          break;
        }
        // double the search area size and try again
        radius *= 2;
      }
      return closest;
    }

    return null;
  }
  /**
   * given a rectangular area and an offset, return the tile numbers that are contained in it
   *
   * @param visibleArea the (possibly) non-rectangular area of interest
   * @return the tile numbers that intersect with the visibleArea
   */
  protected Collection<Integer> getVisibleTiles(Shape visibleArea) {
    Set<Integer> visibleTiles = Sets.newHashSet();
    List<Shape> grid = getGrid();
    for (int i = 0; i < this.horizontalCount * this.verticalCount; i++) {
      if (visibleArea.intersects(grid.get(i).getBounds2D())) {
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
  public Set<N> getVisibleElements(Shape visibleArea) {
    if (!isActive()) {
      log.trace("not active so getting from the graph");
      return layoutModel.getGraph().nodes();
    }

    pickShapes.add(visibleArea);
    Area area = new Area(visibleArea);
    area.intersect(new Area(this.layoutArea));
    if (log.isTraceEnabled()) {
      log.trace("map is {}", map);
    }
    Set<N> visibleNodes = Sets.newHashSet();
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

  /**
   * @return the layout area rectangle for this grid
   */
  @Override
  public Rectangle2D getLayoutArea() {
    return layoutArea;
  }

  @Override
  public Rectangle2D getBounds() {
    return layoutArea;
  }

  @Override
  public Collection<? extends TreeNode> getChildren() {
    return null;
  }
}
