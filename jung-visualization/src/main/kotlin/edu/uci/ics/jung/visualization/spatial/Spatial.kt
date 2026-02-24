package edu.uci.ics.jung.visualization.spatial

import com.google.common.collect.Sets
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.layout.util.RadiusNetworkNodeAccessor
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.layout.RadiusNetworkElementAccessor
import java.awt.Shape
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.util.Collections

/**
 * Basic interface for Spatial data
 *
 * @author Tom Nelson
 */
interface Spatial<T> : LayoutModel.LayoutStateChangeListener {

  /**
   * a flag to suggest whether or not the spatial structure should be used
   */
  fun setActive(active: Boolean)

  /**
   * @return a hint about whether the spatial structure should be used
   */
  fun isActive(): Boolean

  /**
   * @return a geometric representation of the spatial structure
   */
  fun getGrid(): List<Shape>

  /**
   * a short-lived collection of recent pick target areas
   */
  fun getPickShapes(): Collection<Shape>

  /** destroy the current spatial structure */
  fun clear()

  /** rebuild the data structure */
  fun recalculate()

  /**
   * @return the 2 dimensional area of interest for this class
   */
  fun getLayoutArea(): Rectangle2D

  /**
   * @param bounds the new bounds for the data structure
   */
  fun setBounds(bounds: Rectangle2D)

  /**
   * expands the passed rectangle so that it includes the passed point
   *
   * @param rect the area to consider
   * @param p the point that may be outside of the area
   * @return a new rectangle
   */
  fun getUnion(rect: Rectangle2D, p: Point2D): Rectangle2D {
    rect.add(p)
    return rect
  }

  fun getUnion(rect: Rectangle2D, x: Double, y: Double): Rectangle2D {
    rect.add(x, y)
    return rect
  }

  /**
   * update the spatial structure with the (possibly new) location of the passed element
   *
   * @param element the element to consider
   * @param location the location of the element
   */
  fun update(element: T, location: Point)

  /**
   * @param p a point to search in the spatial structure
   * @return all leaf nodes that contain the passed point
   */
  fun getContainingLeafs(p: Point2D): Set<out TreeNode>

  /**
   * @param x the x location to search for
   * @param y the y location to search for
   * @return all leaf nodes that contain the passed coordinates
   */
  fun getContainingLeafs(x: Double, y: Double): Set<out TreeNode>

  /**
   * @param element element to search for
   * @return the leaf node that currently contains the element (not a spatial search)
   */
  fun getContainingLeaf(element: Any): TreeNode?

  /**
   * @param shape a shape to filter the spatial structure's elements
   * @return all elements that are contained in the passed shape
   */
  fun getVisibleElements(shape: Shape): Set<T>

  /**
   * @param p a point to search in the spatial structure
   * @return the closest element to the passed point
   */
  fun getClosestElement(p: Point2D): T?

  /**
   * @param x coordinate of a point to search in the spatial structure
   * @param y coordinate of a point to search in the spatial structure
   * @return the closest element to the passed coordinates
   */
  fun getClosestElement(x: Double, y: Double): T?

  /**
   * a special case Spatial that does no filtering
   *
   * @param T the type for elements in the spatial
   * @param NT the type for the Nodes in a LayoutModel
   */
  abstract class NoOp<T, NT : Any>(layoutModel: LayoutModel<NT>) : AbstractSpatial<T, NT>(layoutModel) {

    private val treeNode: TreeNode

    init {
      this.treeNode = DegenerateTreeNode(layoutModel)
    }

    /**
     * return the entire area
     */
    override fun getGrid(): List<Shape> = Collections.singletonList(getLayoutArea())

    /** nothing to clear */
    override fun clear() {
      // no op
    }

    /** nothing to recalculate */
    override fun recalculate() {
      // no op
    }

    /**
     * return the entire area
     */
    override fun getLayoutArea(): Rectangle2D =
      Rectangle2D.Double(0.0, 0.0, layoutModel.width.toDouble(), layoutModel.height.toDouble())

    /**
     * nothing to change
     *
     * @param bounds the new bounds for the data structure
     */
    override fun setBounds(bounds: Rectangle2D) {
      // no op
    }

    /**
     * nothing to update
     *
     * @param element the element to consider
     * @param location the location of the element
     */
    override fun update(element: T, location: Point) {
      // no op
    }

    /**
     * @param p a point to search in the spatial structure
     * @return the single element that contains everything
     */
    override fun getContainingLeafs(p: Point2D): Set<out TreeNode> =
      Collections.singleton(this.treeNode)

    /**
     * @param x the x location to search for
     * @param y the y location to search for
     * @return the single element that contains everything
     */
    override fun getContainingLeafs(x: Double, y: Double): Set<out TreeNode> =
      Collections.singleton(this.treeNode)

    /**
     * @param element element to search for
     * @return the single leaf that contains everything
     */
    override fun getContainingLeaf(element: Any): TreeNode = this.treeNode

    /**
     * a TreeNode that is immutable and covers the entire layout area
     */
    class DegenerateTreeNode<N : Any>(private val layoutModel: LayoutModel<N>) : TreeNode {
      override fun getBounds(): Rectangle2D =
        Rectangle2D.Double(0.0, 0.0, layoutModel.width.toDouble(), layoutModel.height.toDouble())

      /**
       * contains no children
       */
      override fun getChildren(): List<TreeNode> = Collections.emptyList()
    }

    class Node<N : Any>(layoutModel: LayoutModel<N>) : NoOp<N, N>(layoutModel) {
      private val accessor: RadiusNetworkNodeAccessor<N> = RadiusNetworkNodeAccessor()

      override fun getVisibleElements(shape: Shape): Set<N> =
        Sets.newHashSet(layoutModel.graph.nodes())

      override fun setActive(active: Boolean) {
        // noop
      }

      override fun getClosestElement(p: Point2D): N? = getClosestElement(p.x, p.y)

      override fun getClosestElement(x: Double, y: Double): N? = null // use radius node accessor
    }

    class Edge<E : Any, N : Any>(private val visualizationModel: VisualizationModel<N, E>) :
      NoOp<E, N>(visualizationModel.getLayoutModel()) {

      private val accessor: RadiusNetworkElementAccessor<N, E> =
        RadiusNetworkElementAccessor(visualizationModel.getNetwork())

      override fun getVisibleElements(shape: Shape): Set<E> =
        Sets.newHashSet(visualizationModel.getNetwork().edges())

      override fun setActive(active: Boolean) {
        // noop
      }

      override fun getClosestElement(p: Point2D): E? = getClosestElement(p.x, p.y)

      override fun getClosestElement(x: Double, y: Double): E? =
        accessor.getEdge(layoutModel, x, y)
    }
  }
}
