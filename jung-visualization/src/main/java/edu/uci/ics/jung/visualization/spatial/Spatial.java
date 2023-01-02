package edu.uci.ics.jung.visualization.spatial;

import com.google.common.collect.Sets;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.layout.util.RadiusNetworkNodeAccessor;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.layout.RadiusNetworkElementAccessor;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Basic interface for Spatial data
 *
 * @author Tom Nelson
 */
public interface Spatial<T> extends LayoutModel.LayoutStateChangeListener {

  /**
   * a flag to suggest whether or not the spatial structure should be used
   *
   * @param active
   */
  void setActive(boolean active);

  /**
   * @return a hint about whether the spatial structure should be used
   */
  boolean isActive();

  /**
   * @return a geometic representation of the spatial structure
   */
  List<Shape> getGrid();

  /**
   * a short-lived collection of recent pick target areas
   *
   * @return
   */
  Collection<Shape> getPickShapes();

  /** destroy the current spatial structure */
  void clear();

  /** rebuild the data structure */
  void recalculate();

  /**
   * @return the 2 dimensional area of interest for this class
   */
  Rectangle2D getLayoutArea();

  /**
   * @param bounds the new bounds for the data struture
   */
  void setBounds(Rectangle2D bounds);

  /**
   * expands the passed rectangle so that it includes the passed point
   *
   * @param rect the area to consider
   * @param p the point that may be outside of the area
   * @return a new rectangle
   */
  default Rectangle2D getUnion(Rectangle2D rect, Point2D p) {
    rect.add(p);
    return rect;
  }

  default Rectangle2D getUnion(Rectangle2D rect, double x, double y) {
    rect.add(x, y);
    return rect;
  }

  /**
   * update the spatial structure with the (possibly new) location of the passed element
   *
   * @param element the element to consider
   * @param location the location of the element
   */
  void update(T element, Point location);

  /**
   * @param p a point to search in the spatial structure
   * @return all leaf nodes that contain the passed point
   */
  Set<? extends TreeNode> getContainingLeafs(Point2D p);

  /**
   * @param x the x location to search for
   * @param y the y location to search for
   * @return all leaf nodes that contain the passed coordinates
   */
  Set<? extends TreeNode> getContainingLeafs(double x, double y);

  /**
   * @param element element to search for
   * @return the leaf node that currently contains the element (not a spatial search)
   */
  TreeNode getContainingLeaf(Object element);

  /**
   * @param shape a shape to filter the spatial structure's elements
   * @return all elements that are contained in the passed shape
   */
  Set<T> getVisibleElements(Shape shape);

  /**
   * @param p a point to search in the spatial structure
   * @return the closest element to the passed point
   */
  T getClosestElement(Point2D p);

  /**
   * @param x coordinate of a point to search in the spatial structure
   * @param y coordinate of a point to search in the spatial structure
   * @return the closest element to the passed coordinates
   */
  T getClosestElement(double x, double y);

  /**
   * a special case Spatial that does no filtering
   *
   * @param <T> the type for elements in the spatial
   * @param <NT> the type for the Nodes in a LayoutModel
   */
  abstract class NoOp<T, NT> extends AbstractSpatial<T, NT> {

    private TreeNode treeNode;

    public NoOp(LayoutModel<NT> layoutModel) {
      super(layoutModel);
      this.treeNode = new DegenerateTreeNode(layoutModel);
    }

    /**
     * return the entire area
     *
     * @return
     */
    @Override
    public List<Shape> getGrid() {
      return Collections.singletonList(getLayoutArea());
    }

    /** nothing to clear */
    @Override
    public void clear() {
      // no op
    }

    /** nothing to recalculate */
    @Override
    public void recalculate() {
      // no op
    }

    /**
     * return the entire area
     *
     * @return
     */
    @Override
    public Rectangle2D getLayoutArea() {
      return new Rectangle2D.Double(0, 0, layoutModel.getWidth(), layoutModel.getHeight());
    }

    /**
     * nothing to change
     *
     * @param bounds the new bounds for the data struture
     */
    @Override
    public void setBounds(Rectangle2D bounds) {
      // no op
    }

    /**
     * nothing to update
     *
     * @param element the element to consider
     * @param location the location of the element
     */
    @Override
    public void update(T element, Point location) {
      // no op
    }

    /**
     * @param p a point to search in the spatial structure
     * @return the single element that contains everything
     */
    @Override
    public Set<? extends TreeNode> getContainingLeafs(Point2D p) {
      return Collections.singleton(this.treeNode);
    }

    /**
     * @param x the x location to search for
     * @param y the y location to search for
     * @return the single element that contains everything
     */
    @Override
    public Set<? extends TreeNode> getContainingLeafs(double x, double y) {
      return Collections.singleton(this.treeNode);
    }

    /**
     * @param element element to search for
     * @return the single leaf that contains everything
     */
    @Override
    public TreeNode getContainingLeaf(Object element) {
      return this.treeNode;
    }

    /**
     * a TreeNode that is immutable and covers the entire layout area
     *
     * @param <N>
     */
    public static class DegenerateTreeNode<N> implements TreeNode {
      LayoutModel<N> layoutModel;

      public DegenerateTreeNode(LayoutModel<N> layoutModel) {
        this.layoutModel = layoutModel;
      }

      @Override
      public Rectangle2D getBounds() {
        return new Rectangle2D.Double(0, 0, layoutModel.getWidth(), layoutModel.getHeight());
      }

      /**
       * contains no children
       *
       * @return
       */
      @Override
      public List<? extends TreeNode> getChildren() {
        return Collections.emptyList();
      }
    }

    public static class Node<N> extends NoOp<N, N> {

      RadiusNetworkNodeAccessor<N> accessor;

      public Node(LayoutModel<N> layoutModel) {
        super(layoutModel);
        this.accessor = new RadiusNetworkNodeAccessor<>();
      }

      @Override
      public Set<N> getVisibleElements(Shape shape) {
        return Sets.newHashSet(layoutModel.getGraph().nodes());
      }

      @Override
      public void setActive(boolean active) {
        // noop
      }

      @Override
      public N getClosestElement(Point2D p) {
        return getClosestElement(p.getX(), p.getY());
      }

      @Override
      public N getClosestElement(double x, double y) {
        return null; // use radius node accessor
      }
    }

    public static class Edge<E, N> extends NoOp<E, N> {

      private VisualizationModel<N, E> visualizationModel;
      RadiusNetworkElementAccessor<N, E> accessor;

      public Edge(VisualizationModel<N, E> visualizationModel) {
        super(visualizationModel.getLayoutModel());
        this.visualizationModel = visualizationModel;
        this.accessor = new RadiusNetworkElementAccessor<>(visualizationModel.getNetwork());
      }

      @Override
      public Set<E> getVisibleElements(Shape shape) {
        return Sets.newHashSet(visualizationModel.getNetwork().edges());
      }

      @Override
      public void setActive(boolean active) {
        // noop
      }

      @Override
      public E getClosestElement(Point2D p) {
        return getClosestElement(p.getX(), p.getY());
      }

      @Override
      public E getClosestElement(double x, double y) {
        return accessor.getEdge(layoutModel, x, y);
      }
    }
  }
}
