package edu.uci.ics.jung.visualization.spatial.rtree

import com.google.common.base.Preconditions
import com.google.common.collect.Lists
import edu.uci.ics.jung.visualization.spatial.TreeNode
import java.awt.Shape
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.util.Collections
import java.util.Optional
import org.slf4j.LoggerFactory

/**
 * a non-leaf node of the R-Tree. Contains a list of non leaf or leaf node children
 *
 * @author Tom Nelson
 */
open class InnerNode<T> : RTreeNode<T>, Node<T> {

  companion object {
    private val log = LoggerFactory.getLogger(InnerNode::class.java)

    /**
     * create a new InnerNode with one child
     *
     * @param node the first child for the created Node
     * @param T the type of the node and children
     * @return the newly created InnerNode
     */
    @JvmStatic
    fun <T> create(node: Node<T>): InnerNode<T> = InnerNode(node)

    /**
     * create a new InnerNode with one child
     *
     * @param node the first child of the created node
     * @param T the type of the Node
     * @return the newly created InnerNode
     */
    @JvmStatic
    fun <T> create(node: InnerNode<T>): InnerNode<T> = InnerNode(node as Node<T>)

    /**
     * create a new InnerNode with the passed nodes as children
     *
     * @param nodes the children for the new InnerNode
     * @param T the type of the Node
     * @return the newly created InnerNode
     */
    @JvmStatic
    fun <T> create(nodes: Collection<Node<T>>): InnerNode<T> = InnerNode(nodes)
  }

  private var bounds: Optional<Rectangle2D> = Optional.empty()

  /** child nodes of this InnerNode */
  private val children: MutableList<Node<T>>

  /** true if the child nodes are LeafNodes. false otherwise */
  private val leafChildren: Boolean

  /**
   * create an InnerNode with the passed Node as the first child
   *
   * @param node the first child for the InnerNode
   */
  internal constructor(node: Node<T>) {
    node.setParent(this)
    updateBounds(node.getBounds())
    leafChildren = node is LeafNode
    children = Lists.newArrayList(node)
  }

  /**
   * create an InnerNode with the passed nodes as children
   *
   * @param nodes the children for the new InnerNode
   */
  internal constructor(nodes: Collection<Node<T>>) {
    children = Lists.newArrayList()
    var sample: Node<T>? = null
    for (node in nodes) {
      sample = node
      node.setParent(this)
      updateBounds(node.getBounds())
      children.add(node)
    }
    leafChildren = sample is LeafNode // ugh
  }

  /**
   * true if the children are LeafNodes
   */
  override fun isLeafChildren(): Boolean = leafChildren

  /**
   * return the ith child node
   *
   * @param i the index of the child to return
   * @return the ith child
   */
  operator fun get(i: Int): Node<T> = children[i]

  /**
   * @return an immutable collection of the child nodes
   */
  override fun getChildren(): List<Node<T>> = Collections.unmodifiableList(children)

  /**
   * @return the bounding box of this InnerNode. A zero sized Rectangle is returned if this
   *     InnerNode is empty
   */
  override fun getBounds(): Rectangle2D = bounds.orElse(Rectangle2D.Double())

  /**
   * recompute the bounding box for this InnerNode, then the recompute for parent node
   * Climbs the tree to the root as it recalculates. This is required when a leaf node is removed.
   */
  override fun recalculateBounds(): Node<T> {
    bounds = Optional.empty()
    val size = children.size
    for (i in 0 until size) {
      updateBounds(children[i].getBounds())
    }
    return if (_parent.isPresent) {
      _parent.get().recalculateBounds()
    } else {
      this
    }
  }

  /**
   * @param p the point to search
   * @return the element in the Leaf node that is contained by p
   */
  override fun getPickedObject(p: Point2D): T? {
    var picked: T? = null
    if (getBounds().contains(p)) {
      val size = children.size
      for (i in 0 until size) {
        return children[i].getPickedObject(p)
      }
    }
    return picked
  }

  /**
   * @return the number of child nodes
   */
  override fun size(): Int = children.size

  private fun findElement(o: T): Node<T>? {
    var found: Node<T>? = null
    val size = children.size
    for (i in 0 until size) {
      val kid = children[i]
      if (kid is LeafNode) {
        return kid
      } else {
        found = (kid as InnerNode<T>).findElement(o)
      }
    }
    return found
  }

  /**
   * @param element the element to look for
   * @return the LeafNode that contains the element
   */
  override fun getContainingLeaf(element: T): LeafNode<T>? {
    var containingLeaf: LeafNode<T>? = null
    val size = children.size
    for (i in 0 until size) {
      containingLeaf = children[i].getContainingLeaf(element)
      if (containingLeaf != null) {
        break
      }
    }
    return containingLeaf
  }

  /**
   * @param element the element to look for
   * @return the LeafNode that contains the element
   */
  internal fun getContainingLeaf(element: T, bounds: Rectangle2D): LeafNode<T>? {
    var containingLeaf: LeafNode<T>? = null
    val size = children.size
    for (i in 0 until size) {
      val node = children[i]
      if (node.getBounds().intersects(bounds)) {
        containingLeaf = node.getContainingLeaf(element)
        if (containingLeaf != null) {
          break
        }
      }
    }
    return containingLeaf
  }

  /**
   * @param p the point to look for
   * @return Collection of the LeafNodes that would contain the passed point
   */
  override fun getContainingLeafs(containingLeafs: MutableSet<LeafNode<T>>, p: Point2D): Set<LeafNode<T>> =
    getContainingLeafs(containingLeafs, p.x, p.y)

  /**
   * @param x coordinate of a point to look for
   * @param y coordinate of a point to look for
   * @return Collection of the LeafNodes that would contain the passed coordinates
   */
  override fun getContainingLeafs(containingLeafs: MutableSet<LeafNode<T>>, x: Double, y: Double): Set<LeafNode<T>> {
    if (getBounds().contains(x, y)) {
      val size = children.size
      for (i in 0 until size) {
        val node = children[i]
        node.getContainingLeafs(containingLeafs, x, y)
      }
    }
    return containingLeafs
  }

  /**
   * gather the RTree Node rectangles into a Collection
   */
  override fun collectGrids(list: MutableCollection<Shape>): Collection<Shape> {
    list.add(getBounds())
    val size = children.size
    for (i in 0 until size) {
      children[i].collectGrids(list)
    }
    log.trace(
      "in nonleaf {}, added {} so list size now {}",
      this.hashCode(),
      children.size,
      list.size
    )
    return list
  }

  /**
   * add Nodes directly to the children list
   */
  private fun add(collection: Collection<Node<T>>) {
    children.addAll(collection)
  }

  private fun updateBounds(r: Rectangle2D) {
    bounds = if (bounds.isPresent) {
      Optional.of(bounds.get().createUnion(r))
    } else {
      Optional.of(r)
    }
    val b = bounds.get()
  }

  /**
   * @param splitterContext rules for splitting nodes
   * @param element the element to add
   * @param bounds the bounds of the element to add
   * @return the returned node or its parent
   */
  override fun add(splitterContext: SplitterContext<T>, element: T, bounds: Rectangle2D): Node<T> {
    // update bounds with the new element's bounds
    updateBounds(bounds)
    val pathToFollow = splitterContext.splitter.chooseSubtree(this, element, bounds)
    if (pathToFollow.isPresent) {
      val node = pathToFollow.get().add(splitterContext, element, bounds)
      return node.getParent().orElse(node)
    }
    return this
  }

  /**
   * remove the passed element. Find the LeafNode that contains the element, remove the element from
   * the LeafNode map
   *
   * @param element the element to remove
   * @return the parent node or this node
   */
  override fun remove(element: T): Node<T> {
    val containingLeaf = getContainingLeaf(element)
    if (containingLeaf == null) {
      log.warn("{} is not in the tree! ", element)
      return this
    }
    return containingLeaf.remove(element)!!
  }

  /**
   * directly add a child node to this node.
   */
  internal fun addNode(node: Node<T>) {
    Preconditions.checkArgument(node !== this, "Attempt to add self as child")
    Preconditions.checkArgument(!children.contains(node), "Attempt to add duplicate child")
    node.setParent(this)
    updateBounds(node.getBounds())
    children.add(node)
  }

  /**
   * directly remove a child node from this node
   */
  internal fun removeNode(node: Node<T>) {
    children.remove(node)
  }

  fun add(splitterContext: SplitterContext<T>, vararg nodes: Node<T>): InnerNode<T> {
    var top = this
    for (node in nodes) {
      top = add(splitterContext, node)
    }
    return if (top.getParent().isPresent) {
      top.getParent().get() as InnerNode<T>
    } else {
      top
    }
  }

  /**
   * adding either a LeafNode or an InnerNode
   *
   * @param node
   * @return the parent, if exists, or this
   */
  private fun add(splitterContext: SplitterContext<T>, node: Node<T>): InnerNode<T> {
    Preconditions.checkArgument(node !== this, "Attempt to add self as child")

    updateBounds(node.getBounds())

    if (size() > Node.M) {
      log.trace("splitting InnerNode {}", this)
      val pair = splitterContext.splitter.split(children, node)

      if (_parent.isPresent) {
        val innerNodeParent = _parent.get() as InnerNode<T>
        // sanity check
        Preconditions.checkArgument(
          this !== pair.left && this !== pair.right,
          "Pair left {} or right {} the same as this {}",
          pair.left,
          pair.right,
          this
        )
        innerNodeParent.removeNode(this)
        return innerNodeParent.add(splitterContext, pair.left, pair.right)
      } else {
        // create a new parent
        val innerNodeParent = create(pair.left)
        return innerNodeParent.add(splitterContext, pair.right)
      }
    } else {
      // no split required
      addNode(node)
      return (_parent.orElse(this)) as InnerNode<T>
    }
  }

  /**
   * @param shape the shape to filter the visible elements
   * @return a collection of all elements that intersect with the passed shape
   */
  override fun getVisibleElements(visibleElements: MutableSet<T>, shape: Shape): Set<T> {
    if (shape.intersects(getBounds())) {
      val size = children.size
      for (i in 0 until size) {
        children[i].getVisibleElements(visibleElements, shape)
      }
    }
    log.trace("visibleElements of InnerNode inside {} are {}", shape, visibleElements)
    return visibleElements
  }

  /**
   * descend into the tree and count all children
   */
  override fun count(): Int {
    var count = 0
    val size = children.size
    for (i in 0 until size) {
      count += children[i].count()
    }
    return count
  }

  // to string methods:

  private fun asStringInternal(): String = asString("")

  override fun toString(): String = asStringInternal()

  override fun asString(margin: String): String {
    val s = StringBuilder()
    s.append(margin)
    s.append("InnerNode:parent:").append(if (_parent.isPresent) "yes" else "none")
    s.append(" bounds=")
    s.append(Node.asString(this.getBounds()))
    s.append('\n')
    for (child in this.children) {
      s.append(child.asString(margin + Node.marginIncrement))
    }
    return s.toString()
  }
}
