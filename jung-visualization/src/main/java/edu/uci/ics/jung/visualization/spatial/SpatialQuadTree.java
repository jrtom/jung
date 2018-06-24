package edu.uci.ics.jung.visualization.spatial;

import static edu.uci.ics.jung.visualization.spatial.SpatialQuadTree.Quadrant.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.layout.util.LayoutChangeListener;
import edu.uci.ics.jung.layout.util.LayoutEvent;
import edu.uci.ics.jung.layout.util.LayoutNetworkEvent;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A spatial data structure that uses a quadtree.
 *
 * @author Tom Nelson
 * @param <N> the node type
 */
public class SpatialQuadTree<N> extends AbstractSpatial<N, N>
    implements TreeNode, Spatial<N>, LayoutChangeListener<N> {

  private static final Logger log = LoggerFactory.getLogger(SpatialQuadTree.class);

  private final Object lock = new Object();

  @Override
  public Rectangle2D getBounds() {
    return rectangle;
  }

  @Override
  public Collection<? extends TreeNode> getChildren() {
    return children.values();
  }

  /** the four quadrant keys for the child cells */
  enum Quadrant {
    NE,
    NW,
    SW,
    SE;
  }

  /** how many nodes per cell */
  private int MAX_OBJECTS = 1;
  /** max tree height */
  private int MAX_LEVELS = 12;

  /** the level of this cell in the tree */
  private int level;
  /** the nodes contains in this cell, assuming this cell is a leaf */
  private Set<N> nodes;
  /** the area for this cell */
  private Rectangle2D area;
  /** a collection of child nodes, assuming this is not a leaf */
  private Map<Quadrant, SpatialQuadTree<N>> children;

  /** a cache of grid cell rectangles for performance */
  private List<Spatial> gridCache;

  //  private Collection<Shape> pickShapes = EvictingQueue.create(4);

  /** @param layoutModel */
  public SpatialQuadTree(LayoutModel<N> layoutModel) {
    this(layoutModel, 0, 0, 0, layoutModel.getWidth(), layoutModel.getHeight());
  }

  /**
   * @param layoutModel
   * @param width
   * @param height
   */
  public SpatialQuadTree(LayoutModel<N> layoutModel, double width, double height) {
    this(layoutModel, 0, 0, 0, width, height);
  }

  /**
   * @param level level to start at. 0 is the root
   * @param x
   * @param y
   * @param width
   * @param height
   */
  public SpatialQuadTree(
      LayoutModel<N> layoutModel, int level, double x, double y, double width, double height) {
    this(layoutModel, level, new Rectangle2D.Double(x, y, width, height));
  }

  public SpatialQuadTree(LayoutModel<N> layoutModel, int pLevel, Rectangle2D area) {
    super(layoutModel);
    level = pLevel;
    nodes = Collections.synchronizedSet(Sets.newHashSet());
    this.area = area;
  }

  /**
   * @param o max number of objects allowed
   * @return this QuadTree
   */
  public SpatialQuadTree<N> setMaxObjects(int o) {
    MAX_OBJECTS = o;
    return this;
  }

  /**
   * @param l max levels allowed
   * @return
   */
  public SpatialQuadTree<N> setMaxLevels(int l) {
    MAX_LEVELS = l;
    return this;
  }

  /** @return the level of this cell */
  protected int getLevel() {
    return level;
  }

  /** @return the nodes in this cell, assuming it is a leaf */
  public Set<N> getNodes() {
    return nodes;
  }

  /*
   * Clears the quadtree
   */
  @Override
  public void clear() {
    nodes.clear();
    synchronized (lock) {
      children = null;
      gridCache = null;
    }
  }

  /*
   * Splits the Quadtree into 4 sub-QuadTrees
   */
  protected void split() {
    log.trace("splitting {}", this);
    double width = (area.getWidth() / 2);
    double height = (area.getHeight() / 2);
    double x = area.getX();
    double y = area.getY();

    int childLevel = level + 1;
    SpatialQuadTree<N> ne =
        new SpatialQuadTree(layoutModel, childLevel, x + width, y, width, height);
    SpatialQuadTree<N> nw = new SpatialQuadTree(layoutModel, childLevel, x, y, width, height);
    SpatialQuadTree<N> sw =
        new SpatialQuadTree(layoutModel, childLevel, x, y + height, width, height);
    SpatialQuadTree<N> se =
        new SpatialQuadTree(layoutModel, childLevel, x + width, y + height, width, height);
    synchronized (lock) {
      children = ImmutableMap.of(NE, ne, NW, nw, SW, sw, SE, se);
    }
  }

  /**
   * find the quadrant that the point would be in
   *
   * @param p the point of interest
   * @return the quadrant that would contain the point
   */
  protected Quadrant getQuadrant(Point p) {
    return getQuadrant(p.x, p.y);
  }

  /**
   * find the quadrant that the point would be in
   *
   * @param x, y the point of interest
   * @return the quadrant that would contain the point
   */
  protected Quadrant getQuadrant(double x, double y) {

    double centerX = area.getCenterX();
    double centerY = area.getCenterY();

    boolean inNorth = y < centerY;

    boolean inSouth = y >= centerY;

    boolean inWest = x < centerX;

    if (inNorth && inWest) {
      return Quadrant.NW;
    }
    if (inSouth && inWest) {
      return Quadrant.SW;
    }
    boolean inEast = x >= centerX;
    if (inNorth && inEast) {
      return Quadrant.NE;
    }
    if (inSouth && inEast) {
      return Quadrant.SE;
    }
    return null;
  }

  /*
   * Insert the object into the quadtree. If the node exceeds the capacity, it
   * will split and add all objects to their corresponding nodes.
   */
  protected void insert(N p) {
    gridCache = null;
    log.trace("{} inserting {} at {}", this, p, layoutModel.apply(p));
    if (children != null) {
      // there are child QuadTrees available
      Quadrant quadrant = getQuadrant(layoutModel.apply(p));
      if (quadrant != null && children.get(quadrant) != null) {
        // insert into the child QuadTree
        children.get(quadrant).insert(p);
        return;
      }
    }
    // insert into this QuadTree unless capacity is exceeded
    nodes.add(p);
    // if capacity is exceeded, split and put all objects into child QuadTrees
    if (nodes.size() > MAX_OBJECTS && level < MAX_LEVELS) {
      split();
      // now this QuadTree has child QuadTrees

      for (Iterator<N> iterator = nodes.iterator(); iterator.hasNext(); ) {
        N node = iterator.next();
        Quadrant quadrant = getQuadrant(layoutModel.apply(node));
        children.get(quadrant).insert(node);
        iterator.remove();
      }
    }
  }

  /*
   * Return all objects that are within the passed rectangle
   */
  protected Set<N> retrieve(Set<N> returnObjects, Rectangle2D r) {
    if (children == null) {
      // i am a leaf, add any nodes i have
      returnObjects.addAll(nodes);
    } else {

      for (Map.Entry<Quadrant, SpatialQuadTree<N>> entry : children.entrySet()) {
        if (entry.getValue().area.intersects(r)) {
          children.get(entry.getKey()).retrieve(returnObjects, r);
        }
      }
    }
    return returnObjects;
  }

  /**
   * Return all objects that are within the passed shape This is needed when the layout is
   * rotated/skewed and the shape edges are no longer parallel to the grid edges.
   */
  protected Set<N> retrieve(Set<N> returnObjects, Shape shape) {
    if (children == null) {
      // i am a leaf, add any nodes i have
      returnObjects.addAll(nodes);
    } else {

      synchronized (lock) {
        for (Map.Entry<Quadrant, SpatialQuadTree<N>> entry : children.entrySet()) {
          if (shape.intersects(entry.getValue().area)) {
            children.get(entry.getKey()).retrieve(returnObjects, shape);
          }
        }
      }
    }
    return returnObjects;
  }

  public List<Spatial> getNodes(List<Spatial> list) {
    if (gridCache == null) {
      list.addAll(this.collectNodes(list, this));
      gridCache = list;
    }
    return gridCache;
  }

  @Override
  public List<Shape> getGrid() {
    List<Shape> areas = Lists.newArrayList();

    return collectGrids(areas, this);
  }

  private List<Shape> collectGrids(List<Shape> list, SpatialQuadTree<N> tree) {
    list.add(tree.area);
    if (tree.children != null) {
      for (Map.Entry<Quadrant, SpatialQuadTree<N>> entry : tree.children.entrySet()) {
        collectGrids(list, entry.getValue());
      }
    }
    return list;
  }

  private List<Spatial> collectNodes(List<Spatial> list, SpatialQuadTree<N> tree) {
    list.add(tree);
    if (tree.children != null) {
      for (Map.Entry<Quadrant, SpatialQuadTree<N>> entry : tree.children.entrySet()) {
        collectNodes(list, entry.getValue());
      }
    }
    return list;
  }

  /**
   * @param shape the possibly non-rectangular area of interest
   * @return the nodes that are in the quadtree cells that intersect with the passed shape
   */
  @Override
  public Set<N> getVisibleElements(Shape shape) {
    if (!isActive()) {
      log.trace("not active so getting from the graph");
      return layoutModel.getGraph().nodes();
    }

    pickShapes.add(shape);
    Set<N> set = Sets.newHashSet();
    Set<N> visibleNodes = this.retrieve(set, shape);
    if (log.isDebugEnabled()) {
      log.debug("visibleNodes:{}", visibleNodes);
    }

    return visibleNodes;
  }

  /**
   * @param r
   * @return the nodes that are in the quadtree cells that intersect with the passed rectangle
   */
  public Set<N> getVisibleNodes(Rectangle2D r) {
    if (!isActive()) {
      log.trace("not active so getting from the graph");
      return layoutModel.getGraph().nodes();
    }

    Set<N> set = Sets.newHashSet();
    Set<N> visibleNodes = this.retrieve(set, r);
    if (log.isDebugEnabled()) {
      log.debug("visibleNodes:{}", visibleNodes);
    }
    return visibleNodes;
  }

  /**
   * tha layout area that this tree cell operates over
   *
   * @return
   */
  @Override
  public Rectangle2D getLayoutArea() {
    return area;
  }

  @Override
  public void recalculate() {
    if (isActive()) {
      recalculate(layoutModel.getGraph().nodes());
    }
  }

  private void recalculate(Collection<N> nodes) {

    this.clear();
    while (true) {
      try {
        for (N node : nodes) {
          this.insert(node);
        }
        break;
      } catch (ConcurrentModificationException ex) {
        // ignore
      }
    }
  }

  /**
   * @param node the node to search for
   * @return the quadtree leaf that contains the passed node
   */
  public TreeNode getContainingQuadTreeLeaf(N node) {
    // find where it is now, not where the layoutModel will put it
    if (this.nodes.contains(node)) {
      if (log.isTraceEnabled()) {
        log.trace("nodes {} in {} does contain {}", nodes, this, node);
      }
      return this;
    }
    if (children != null) {
      for (Map.Entry<Quadrant, SpatialQuadTree<N>> entry : children.entrySet()) {
        SpatialQuadTree<N> child = entry.getValue();
        TreeNode leaf = child.getContainingQuadTreeLeaf(node);
        if (leaf != null) {
          return leaf;
        }
      }
    }
    return null;
  }

  public Set<SpatialQuadTree<N>> getContainingLeafs(Point2D p) {
    return Collections.singleton(getContainingQuadTreeLeaf(p));
  }

  public Set<SpatialQuadTree<N>> getContainingLeafs(double x, double y) {
    return Collections.singleton(getContainingQuadTreeLeaf(x, y));
  }

  @Override
  public TreeNode getContainingLeaf(Object element) {

    return getContainingQuadTreeLeaf((N) element);
  }

  /**
   * find the cell that would contain the passed point
   *
   * @param p the point of interest
   * @return the cell that would contain p
   */
  public SpatialQuadTree<N> getContainingQuadTreeLeaf(Point2D p) {
    return getContainingQuadTreeLeaf(p.getX(), p.getY());
  }

  /**
   * @param x location of interest
   * @param y location of interest
   * @return the cell that would contain (x, y)
   */
  public SpatialQuadTree<N> getContainingQuadTreeLeaf(double x, double y) {
    if (this.area.contains(x, y)) {
      if (this.children != null) {
        for (Map.Entry<Quadrant, SpatialQuadTree<N>> entry : this.children.entrySet()) {
          if (entry.getValue().area.contains(x, y)) {
            return entry.getValue().getContainingQuadTreeLeaf(x, y);
          }
        }
      } else {
        // i am a leaf. return myself
        return this;
      }
    }
    return null;
  }

  @Override
  public N getClosestElement(Point2D p) {
    return getClosestElement(p.getY(), p.getY());
  }

  /**
   * get the node that is closest to the passed (x,y)
   *
   * @param x
   * @param y
   * @return the node closest to x,y
   */
  @Override
  public N getClosestElement(double x, double y) {
    if (!isActive()) {
      return fallback.getNode(layoutModel, x, y);
    }
    Spatial leaf = getContainingQuadTreeLeaf(x, y);
    Rectangle2D area = leaf.getLayoutArea();
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

  /**
   * reset the side of this structure
   *
   * @param bounds the new bounds for the data struture
   */
  @Override
  public void setBounds(Rectangle2D bounds) {
    gridCache = null;
    this.area = bounds;
  }

  /**
   * Update the structure for the passed node. If the node is still in the same cell, don't rebuild
   * the structure. If it moved to a new cell, rebuild the structure
   *
   * @param node
   */
  @Override
  public void update(N node, Point location) {
    if (isActive()) {
      gridCache = null;
      if (!this.getLayoutArea().contains(location.x, location.y)) {
        log.trace(location + " outside of spatial " + this.getLayoutArea());
        this.setBounds(this.getUnion(this.getLayoutArea(), location.x, location.y));
        this.recalculate(layoutModel.getGraph().nodes());
      }
      Spatial locationContainingLeaf = getContainingQuadTreeLeaf(location.x, location.y);
      log.trace("leaf {} contains {}", locationContainingLeaf, location);
      TreeNode nodeContainingLeaf = getContainingQuadTreeLeaf(node);
      log.trace("leaf {} contains node {}", nodeContainingLeaf, node);
      if (locationContainingLeaf == null) {
        log.trace("got null for leaf containing {}", location);
      }
      if (nodeContainingLeaf == null) {
        log.trace("got null for leaf containing {}", node);
      }
      if (locationContainingLeaf != null && !locationContainingLeaf.equals(nodeContainingLeaf)) {
        log.trace("time to recalculate");
        this.recalculate(layoutModel.getGraph().nodes());
      }
      this.insert(node);
    }
  }

  @Override
  public void layoutChanged(LayoutEvent<N> evt) {
    Point location = evt.getLocation();
    N node = evt.getNode();
    this.update(node, evt.getLocation());
  }

  @Override
  public void layoutChanged(LayoutNetworkEvent<N> evt) {
    this.update(evt.getNode(), evt.getLocation());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SpatialQuadTree<?> that = (SpatialQuadTree<?>) o;

    if (level != that.level) return false;
    if (!nodes.equals(that.nodes)) return false;
    if (!area.equals(that.area)) return false;
    return layoutModel.equals(that.layoutModel);
  }

  @Override
  public int hashCode() {
    int result = level;
    result = 31 * result + nodes.hashCode();
    result = 31 * result + area.hashCode();
    result = 31 * result + layoutModel.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "SpatialQuadTree{"
        + "level="
        + level
        + ", nodes="
        + nodes
        + ", area="
        + area
        + ", children="
        + children
        + '}';
  }
}
