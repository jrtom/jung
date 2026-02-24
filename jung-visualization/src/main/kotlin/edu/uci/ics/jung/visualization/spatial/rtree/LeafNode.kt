package edu.uci.ics.jung.visualization.spatial.rtree

import edu.uci.ics.jung.visualization.spatial.TreeNode
import java.awt.Shape
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.util.AbstractMap
import java.util.Collections
import org.slf4j.LoggerFactory

/**
 * a leaf node of an R-Tree. Contains a map of elements to their 2d bounds
 *
 * @author Tom Nelson
 */
open class LeafNode<T> : RTreeNode<T>, Node<T> {

  companion object {
    private val log = LoggerFactory.getLogger(LeafNode::class.java)

    /** a LeafNode with no children */
    @JvmField
    val EMPTY: LeafNode<Any> = LeafNode()

    @JvmStatic
    fun <T> create(entry: Map.Entry<T, Rectangle2D>): LeafNode<T> = LeafNode(entry)

    @JvmStatic
    fun <T> create(element: T, bounds: Rectangle2D): LeafNode<T> = LeafNode(element, bounds)

    @JvmStatic
    fun <T> create(entries: Collection<Map.Entry<T, Rectangle2D>>): LeafNode<T> = LeafNode(entries)
  }

  /** a map of child elements for this LeafNode */
  internal val map: NodeMap<T> = NodeMap()

  internal constructor(entries: Collection<Map.Entry<T, Rectangle2D>>) {
    for (entry in entries) {
      map.put(entry.key, entry.value)
    }
  }

  internal constructor(entry: Map.Entry<T, Rectangle2D>) {
    map.put(entry.key, entry.value)
  }

  internal constructor(element: T, bounds: Rectangle2D) {
    map.put(element, bounds)
  }

  /** create an empty LeafNode */
  private constructor()

  /**
   * @param splitterContext how to split on overflow (R-Tree or R*-Tree)
   * @param entries add to this LeafNode
   * @return the last node added to
   */
  fun add(splitterContext: SplitterContext<T>, vararg entries: Map.Entry<T, Rectangle2D>): Node<T> {
    var top: Node<T> = this
    for (entry in entries) {
      top = add(splitterContext, entry.key, entry.value)
    }
    return top
  }

  /**
   * @param splitterContext how to split on overflow (R-Tree or R*-Tree)
   * @param element the element to add
   * @param bounds the bounding box of the element
   * @return the highest node available after adding (this or parent)
   */
  override fun add(splitterContext: SplitterContext<T>, element: T, bounds: Rectangle2D): Node<T> {
    if (size() > Node.M) {
      // overflow. Split this node into 2
      val pair = splitterContext.leafSplitter.split(
        map.entries,
        AbstractMap.SimpleEntry(element, bounds)
      )

      if (_parent.isPresent) {
        // if there is a parent node, remove this node from it
        // and add the pair from the split
        val innerNodeParent = _parent.get() as InnerNode<T>
        innerNodeParent.removeNode(this)
        return innerNodeParent.add(splitterContext, pair.left, pair.right)
      } else {
        // if there is no parent, create one then add the pair from the split
        val newParent = InnerNode.create(pair.left)
        return newParent.add(splitterContext, pair.right)
      }
    } else {
      // no split required
      // just add this element to the map
      map.put(element, bounds)
      return _parent.orElse(this)
    }
  }

  /**
   * always false. children are elements, not LeafNodes
   */
  override fun isLeafChildren(): Boolean = false

  /**
   * remove passed element from the map if it exists
   * call recalculateBounds to update all parent node bounds after removal of the element
   *
   * @param element the element to remove
   * @return the parent node, recurses to the top
   */
  override fun remove(element: T): Node<T>? {
    log.trace("LeafNode wants to remove {}", element)
    if (map.containsKey(element)) {
      map.remove(element)
      if (_parent.isPresent) {
        val parentNode = _parent.get() as InnerNode<T>
        if (map.size == 0) {
          parentNode.removeNode(this)
        }
        return parentNode.recalculateBounds()
      } else {
        log.trace("no parent? return this {}", this)
        return this
      }
    } else {
      return null
    }
  }

  /**
   * called after a removal. climb the tree to the root recalculating the bounds
   *
   * @return this LeafNode
   */
  override fun recalculateBounds(): Node<T> {
    return if (_parent.isPresent) {
      _parent.get().recalculateBounds()
    } else {
      this
    }
  }

  fun getBoundsFor(element: T): Rectangle2D? = map[element]

  /**
   * add the passed Entry to the map
   *
   * @param splitterContext how to split on overflow
   * @param entry the entry to add to the map
   * @return the parent or this
   */
  fun add(splitterContext: SplitterContext<T>, entry: Map.Entry<T, Rectangle2D>): Node<T> =
    add(splitterContext, entry.key, entry.value)

  /**
   * @param key the element key to search for
   * @return true if the key exists in the map. false otherwise
   */
  fun contains(key: Any?): Boolean = map.containsKey(key)

  /**
   * the number of children of this node
   */
  override fun size(): Int = map.size

  /**
   * returns the elements from the child map
   */
  internal fun getKeys(): Collection<T> = map.keys

  /**
   * @param element the element to find
   * @return the LeafNode that contains the passed element
   */
  override fun getContainingLeaf(element: T): LeafNode<T>? =
    if (map.containsKey(element)) this else null

  /**
   * @param p the point to search for
   * @return a Collection of LeafNodes that would contain the point
   */
  override fun getContainingLeafs(containingLeafs: MutableSet<LeafNode<T>>, p: Point2D): Set<LeafNode<T>> =
    getContainingLeafs(containingLeafs, p.x, p.y)

  /**
   * @param x coordinate of the point to search for
   * @param y coordinate of the point to search for
   * @return a Collection of LeafNodes that would contain the passed coordinates
   */
  override fun getContainingLeafs(containingLeafs: MutableSet<LeafNode<T>>, x: Double, y: Double): Set<LeafNode<T>> {
    if (getBounds().contains(x, y)) {
      containingLeafs.add(this)
      return containingLeafs
    }
    return Collections.emptySet()
  }

  /**
   * gather the bounds of the node children of this node
   *
   * @param list a list to populate with child bounds rectangles
   * @return the list of child bounds rectangles
   */
  override fun collectGrids(list: MutableCollection<Shape>): Collection<Shape> {
    list.add(getBounds())
    for (r in map.values) {
      list.add(r)
    }
    log.trace("in leaf {}, added {} so list size now {}", this.hashCode(), map.size, list.size)
    return list
  }

  /**
   * the bounding box of this node is held in the children map
   *
   * @return the bounding box of this node
   */
  override fun getBounds(): Rectangle2D = map.getBounds()

  /**
   * @param p a point to search for
   * @return the map entry key whose bounds value contains the passed point
   */
  override fun getPickedObject(p: Point2D): T? {
    var picked: T? = null
    for (entry in map.entries) {
      if (entry.value.contains(p)) {
        picked = entry.key
      }
    }
    return picked
  }

  /**
   * @param shape a shape to filter the visible elements
   * @return a subset of elements whose bounds intersect with the passed shape
   */
  override fun getVisibleElements(visibleElements: MutableSet<T>, shape: Shape): Set<T> {
    if (shape.intersects(getBounds())) {
      for (entry in map.entries) {
        if (shape.intersects(entry.value)) {
          visibleElements.add(entry.key)
        }
      }
    }
    log.trace("visibleElements of LeafNode inside {} are {}", shape, visibleElements)
    return visibleElements
  }

  /**
   * @return the number of children in this node
   */
  override fun count(): Int = size()

  // to string methods

  override fun asString(margin: String): String {
    val s = StringBuilder()
    s.append(margin)
    s.append("LeafNode:")
    s.append("parent:")
    s.append(if (_parent.isPresent) "yes" else "none")
    s.append(" bounds=")
    s.append(Node.asString(this.getBounds()))
    s.append('\n')
    for (entry in this.map.entries) {
      s.append(margin)
      s.append(Node.marginIncrement)
      s.append("entry=")
      s.append(entryAsString(entry))
      s.append('\n')
    }
    return s.toString()
  }

  override fun getChildren(): Collection<TreeNode> = Collections.emptySet()

  override fun toString(): String = asString("")
}

private fun <T> entryAsString(entry: Map.Entry<T, Rectangle2D>): String =
  "${entry.key}->${Node.asString(entry.value)}"
