package edu.uci.ics.jung.visualization.spatial;

import static edu.uci.ics.jung.visualization.spatial.SpatialQuadTree.Quadrant.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.uci.ics.jung.layout.model.LayoutModel;
import java.awt.*;
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
public class SpatialQuadTree<N> extends AbstractSpatial<N> implements Spatial<N> {

  private static final Logger log = LoggerFactory.getLogger(SpatialQuadTree.class);

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
  private Map<Quadrant, SpatialQuadTree> children;

  /**
   * @param layoutModel
   * @param width
   * @param height
   */
  public SpatialQuadTree(LayoutModel<N, Point2D> layoutModel, double width, double height) {
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
      LayoutModel<N, Point2D> layoutModel,
      int level,
      double x,
      double y,
      double width,
      double height) {
    this(layoutModel, level, new Rectangle2D.Double(x, y, width, height));
  }

  public SpatialQuadTree(LayoutModel<N, Point2D> layoutModel, int pLevel, Rectangle2D area) {
    super(layoutModel);
    this.layoutModel = layoutModel;
    level = pLevel;
    nodes = Sets.newHashSet();
    this.area = area;
  }

  /**
   * @param o max number of objects allowed
   * @return this QuadTree
   */
  public SpatialQuadTree setMaxObjects(int o) {
    MAX_OBJECTS = o;
    return this;
  }

  /**
   * @param l max levels allowed
   * @return
   */
  public SpatialQuadTree setMaxLevels(int l) {
    MAX_LEVELS = l;
    return this;
  }

  /** @return the level of this cell */
  protected int getLevel() {
    return level;
  }

  /** @return the nodes in this cell, assuming it is a leaf */
  public Set<N> getNodes() {
    return Collections.unmodifiableSet(nodes);
  }

  /*
   * Clears the quadtree
   */
  public void clear() {
    nodes.clear();
    if (children != null) children.clear();
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
    SpatialQuadTree ne = new SpatialQuadTree(layoutModel, childLevel, x + width, y, width, height);
    SpatialQuadTree nw = new SpatialQuadTree(layoutModel, childLevel, x, y, width, height);
    SpatialQuadTree sw = new SpatialQuadTree(layoutModel, childLevel, x, y + height, width, height);
    SpatialQuadTree se =
        new SpatialQuadTree(layoutModel, childLevel, x + width, y + height, width, height);
    if (children == null) {
      children = Maps.newHashMap();
    }
    children.put(NE, ne);
    children.put(NW, nw);
    children.put(SW, sw);
    children.put(SE, se);
  }

  /**
   * find the quadrant that the point would be in
   *
   * @param p the point of interest
   * @return the quadrant that would contain the point
   */
  protected Quadrant getQuadrant(Point2D p) {

    double centerX = area.getCenterX();
    double centerY = area.getCenterY();

    boolean inNorth = p.getY() < centerY;

    boolean inSouth = p.getY() >= centerY;

    boolean inWest = p.getX() < centerX;

    if (inNorth && inWest) {
      return Quadrant.NW;
    }
    if (inSouth && inWest) {
      return Quadrant.SW;
    }
    boolean inEast = p.getX() >= centerX;
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
  protected Collection<N> retrieve(Collection<N> returnObjects, Rectangle2D r) {
    if (children == null) {
      // i am a leaf, add any nodes i have
      returnObjects.addAll(nodes);
    } else {

      for (Map.Entry<Quadrant, SpatialQuadTree> entry : children.entrySet()) {
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
  protected Collection<N> retrieve(Collection<N> returnObjects, Shape shape) {
    if (children == null) {
      // i am a leaf, add any nodes i have
      returnObjects.addAll(nodes);
    } else {

      for (Map.Entry<Quadrant, SpatialQuadTree> entry : children.entrySet()) {
        if (shape.intersects(entry.getValue().area)) {
          children.get(entry.getKey()).retrieve(returnObjects, shape);
        }
      }
    }
    return returnObjects;
  }

  public static <N> List<SpatialQuadTree> getNodes(
      List<SpatialQuadTree> list, SpatialQuadTree<N> tree) {
    list.addAll(tree.collectNodes(list, tree));
    return list;
  }

  protected List<Rectangle2D> getGrid() {
    List<Rectangle2D> areas = Lists.newArrayList();

    return collectGrids(areas, this);
  }

  private List<Rectangle2D> collectGrids(List<Rectangle2D> list, SpatialQuadTree<N> tree) {
    list.add(tree.area);
    if (tree.children != null) {
      for (Map.Entry<Quadrant, SpatialQuadTree> entry : tree.children.entrySet()) {
        collectGrids(list, entry.getValue());
      }
    }
    return list;
  }

  private List<SpatialQuadTree> collectNodes(List<SpatialQuadTree> list, SpatialQuadTree<N> tree) {
    list.add(tree);
    if (tree.children != null) {
      for (Map.Entry<Quadrant, SpatialQuadTree> entry : tree.children.entrySet()) {
        collectNodes(list, entry.getValue());
      }
    }
    return list;
  }

  @Override
  public Collection<N> getVisibleNodes(Shape shape) {
    Set<N> list = Sets.newHashSet();
    Collection<N> visibleNodes = this.retrieve(list, shape);
    if (log.isDebugEnabled()) {
      log.debug("visibleNodes:{}", visibleNodes);
    }

    return visibleNodes;
  }

  @Override
  public Rectangle2D getLayoutArea() {
    return area;
  }

  @Override
  public void recalculate(Collection<N> nodes) {

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

  protected SpatialQuadTree<N> getContainingQuadTreeLeaf(N node) {
    // find where it is now, not where the layoutModel will put it
    if (this.nodes.contains(node)) {
      log.trace("nodes {} in {} does contain {}", nodes, this, node);
      return this;
    }
    if (children != null) {
      for (Map.Entry<Quadrant, SpatialQuadTree> entry : children.entrySet()) {
        SpatialQuadTree<N> child = entry.getValue();
        SpatialQuadTree<N> leaf = child.getContainingQuadTreeLeaf(node);
        if (leaf != null) {
          return leaf;
        }
      }
    }
    return null;
  }

  /**
   * find the cell that would contain the passed point
   *
   * @param p the point of interest
   * @return the cell that would contain p
   */
  protected SpatialQuadTree<N> getContainingQuadTreeLeaf(Point2D p) {
    return getContainingQuadTreeLeaf(p.getX(), p.getY());
  }

  /**
   * @param x location of interest
   * @param y location of interest
   * @return the cell that would contain (x, y)
   */
  protected SpatialQuadTree<N> getContainingQuadTreeLeaf(double x, double y) {
    if (this.area.contains(x, y)) {
      if (this.children != null) {
        for (Map.Entry<Quadrant, SpatialQuadTree> entry : this.children.entrySet()) {
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

  /**
   * reset the side of this structure
   *
   * @param bounds the new bounds for the data struture
   */
  @Override
  public void setBounds(Rectangle2D bounds) {
    this.area = bounds;
  }

  /**
   * Update the structure for the passed node. If the node is still in the same cell, don't rebuild
   * the structure. If it moved to a new cell, rebuild the structure
   *
   * @param node
   */
  @Override
  public void update(N node) {
    Point2D location = layoutModel.apply(node);
    if (!this.getLayoutArea().contains(location)) {
      log.trace(location + " outside of spatial " + this.getLayoutArea());
      this.setBounds(this.getUnion(this.getLayoutArea(), location));
      this.recalculate(layoutModel.getGraph().nodes());
    }
    SpatialQuadTree<N> locationContainingLeaf = getContainingQuadTreeLeaf(location);
    log.trace("leaf {} contains {}", locationContainingLeaf, location);
    SpatialQuadTree<N> nodeContainingLeaf = getContainingQuadTreeLeaf(node);
    log.trace("leaf {} contains node {}", nodeContainingLeaf, node);
    if (locationContainingLeaf == null) {
      log.error("got null for leaf containing {}", location);
    }
    if (nodeContainingLeaf == null) {
      log.warn("got null for leaf containing {}", node);
    }
    if (!locationContainingLeaf.equals(nodeContainingLeaf)) {
      log.trace("time to recalculate");
      this.recalculate(layoutModel.getGraph().nodes());
    }
    this.insert(node);
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
