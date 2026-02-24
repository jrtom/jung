package edu.uci.ics.jung.visualization.spatial.rtree

import com.google.common.base.Preconditions
import com.google.common.collect.Sets
import edu.uci.ics.jung.visualization.spatial.TreeNode
import java.awt.Shape
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.util.Collections
import java.util.Optional
import org.slf4j.LoggerFactory

/**
 * R-Tree or R*-Tree implementation, depending on the type of Splitters passed in the
 * SplitterContext
 *
 * Based on [The R*-tree: An Efficient and Robust Access Method for Points and Rectangles+](http://dbs.mathematik.uni-marburg.de/publications/myPapers/1990/BKSS90.pdf)
 * Norbert Beckmann, Hans-Peter begel, Ralf Schneider, Bernhard Seeger
 * Praktuche Informatlk, Umversltaet Bremen, D-2800 Bremen 33, West Germany
 *
 * @author Tom Nelson
 */
class RTree<T> private constructor(node: Node<T>?) {

  companion object {
    private val log = LoggerFactory.getLogger(RTree::class.java)
    private const val marginIncrement = "   "

    /** create and return an empty R-Tree */
    @JvmStatic
    fun <T> create(): RTree<T> = RTree(null)

    private fun asString(r: Rectangle2D): String =
      "[${r.x.toInt()},${r.y.toInt()},${r.width.toInt()},${r.height.toInt()}]"

    private fun <T> asString(trees: Collection<RTree<T>>): String {
      val sb = StringBuilder()
      for (tree in trees) {
        if (sb.isNotEmpty()) {
          sb.append('\n')
        }
        sb.append(tree.asStringInternal())
      }
      return sb.toString()
    }

    private fun <T> asString(map: Map<T, Rectangle2D>): String {
      val sb = StringBuilder()
      for (entry in map.entries) {
        if (sb.isNotEmpty()) {
          sb.append('\n')
        }
        sb.append(asString(entry))
      }
      return sb.toString()
    }

    private fun <T> asString(entry: Map.Entry<T, Rectangle2D>): String =
      "${entry.key}->${asString(entry.value)}"
  }

  /** the root of the R-Tree */
  private val root: Optional<Node<T>>

  init {
    if (node != null) {
      Preconditions.checkArgument(
        !node.getParent().isPresent,
        "Error creating R-Tree with root that has parent"
      )
      root = Optional.of(node)
    } else {
      root = Optional.empty()
    }
  }

  fun getRoot(): Optional<Node<T>> = root

  /**
   * add one element to the RTree
   *
   * @param splitterContext the R*Tree or R-Tree rules
   * @param element to add to the tree
   * @param bounds for the element to add
   * @return a new RTree containing the added element
   */
  fun add(splitterContext: SplitterContext<T>, element: T, bounds: Rectangle2D): RTree<T> {
    // see if the root is not present (i.e. the RTree is empty
    if (!root.isPresent) {
      // The first element added to an empty RTree
      // Return a new RTree with the new LeafNode as its root
      return RTree(LeafNode.create(element, bounds))
    }
    // otherwise...
    val node = root.get()
    if (node is LeafNode<T>) {
      val got = node.add(splitterContext, element, bounds)
      Preconditions.checkArgument(
        !got.getParent().isPresent, "return from LeafNode add has a parent"
      )
      return RTree(got)
    } else {
      val innerNode = node as InnerNode<T>
      val got = innerNode.add(splitterContext, element, bounds)
      Preconditions.checkArgument(
        !got.getParent().isPresent, "return from InnerNode add has a parent"
      )
      return RTree(got)
    }
  }

  /**
   * remove an element from the tree
   */
  fun remove(element: T): RTree<T> {
    log.trace("want to remove {} from tree of size {}", element, count())
    if (!root.isPresent) {
      // this tree is empty
      return RTree(null)
    }
    val rootNode = root.get()
    val newRoot = rootNode.remove(element)
    return RTree(newRoot)
  }

  /**
   * return an object at point p
   *
   * @param p point to search
   * @return an element that contains p or null
   */
  fun getPickedObject(p: Point2D): T? {
    val root = this.root.get()
    return when (root) {
      is LeafNode<T> -> root.getPickedObject(p)
      is InnerNode<T> -> root.getPickedObject(p)
      else -> null
    }
  }

  /**
   * @return a collection of rectangular bounds of the R-Tree nodes
   */
  fun getGrid(): Set<Shape> {
    val areas: MutableSet<Shape> = Sets.newHashSet()
    if (root.isPresent) {
      val node = root.get()
      node.collectGrids(areas)
    }
    return areas
  }

  /**
   * get the R-Tree leaf nodes that would contain the passed point
   *
   * @param p the point to search
   * @return a Collection of R-Tree nodes that would contain p
   */
  fun getContainingLeafs(p: Point2D): Collection<TreeNode> {
    if (root.isPresent) {
      val theRoot = root.get()
      if (theRoot is LeafNode<T>) {
        return Collections.singleton(theRoot)
      } else if (theRoot is InnerNode<T>) {
        return theRoot.getContainingLeafs(Sets.newHashSet(), p)
      }
    }
    return Collections.emptySet()
  }

  /**
   * count all the elements in the R-Tree
   *
   * @return the count
   */
  fun count(): Int {
    var count = 0
    if (root.isPresent) {
      val node = root.get()
      count += node.count()
    }
    return count
  }

  private fun asStringInternal(): String =
    if (root.isPresent) root.get().asString("") else "Empty RTree"

  override fun toString(): String = asStringInternal()
}
