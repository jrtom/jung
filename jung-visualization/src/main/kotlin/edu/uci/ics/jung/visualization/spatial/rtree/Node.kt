package edu.uci.ics.jung.visualization.spatial.rtree

import edu.uci.ics.jung.visualization.spatial.TreeNode
import java.awt.Shape
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.util.Optional
import org.slf4j.LoggerFactory

/**
 * Interface for R-Tree nodes
 * Includes static functions for area, union, margin, overlap
 *
 * @author Tom Nelson
 */
interface Node<T> : TreeNode, Bounded {

  fun asString(margin: String): String

  fun getPickedObject(p: Point2D): T?

  fun size(): Int

  fun setParent(node: Node<T>)

  fun getParent(): Optional<Node<T>>

  fun add(splitterContext: SplitterContext<T>, element: T, bounds: Rectangle2D): Node<T>

  fun isLeafChildren(): Boolean

  fun count(): Int

  fun getContainingLeafs(containingLeafs: MutableSet<LeafNode<T>>, x: Double, y: Double): Set<LeafNode<T>>

  fun getContainingLeafs(containingLeafs: MutableSet<LeafNode<T>>, p: Point2D): Set<LeafNode<T>>

  fun getContainingLeaf(element: T): LeafNode<T>?

  fun remove(element: T): Node<T>?

  fun recalculateBounds(): Node<T>

  fun collectGrids(list: MutableCollection<Shape>): Collection<Shape>

  fun getVisibleElements(visibleElements: MutableSet<T>, shape: Shape): Set<T>

  companion object {
    val log = LoggerFactory.getLogger(Node::class.java)!!

    const val M: Int = 10

    val m: Int = (M * .4).toInt() // m is 40% of M

    const val marginIncrement: String = "   "

    @JvmStatic
    fun asString(rectangles: List<Shape>): String {
      val sb = StringBuilder()
      for (r in rectangles) {
        sb.append(asString(r.bounds))
        sb.append("\n")
      }
      return sb.toString()
    }

    @JvmStatic
    fun asString(r: Rectangle2D): String =
      "[${r.x.toInt()},${r.y.toInt()},${r.width.toInt()},${r.height.toInt()}]"

    @JvmStatic
    fun <T> asString(entry: Map.Entry<T, Rectangle2D>): String =
      "${entry.key}->${asString(entry.value)}"

    @JvmStatic
    fun <T> asString(node: Node<T>, margin: String): String {
      val s = StringBuilder()
      s.append(margin)
      s.append("bounds=")
      s.append(asString(node.getBounds()))
      s.append('\n')
      s.append(node.asString(margin + marginIncrement))
      return s.toString()
    }

    @JvmStatic
    fun <T> entryBoundingBox(entries: Collection<Map.Entry<T, Rectangle2D>>): Rectangle2D? {
      var boundingBox: Rectangle2D? = null
      for (entry in entries) {
        val rectangle = entry.value
        boundingBox = boundingBox?.createUnion(rectangle) ?: rectangle
      }
      return boundingBox
    }

    @JvmStatic
    fun <T> nodeBoundingBox(nodes: Collection<Node<T>>): Rectangle2D? {
      var boundingBox: Rectangle2D? = null
      for (node in nodes) {
        val rectangle = node.getBounds()
        boundingBox = boundingBox?.createUnion(rectangle) ?: rectangle
      }
      return boundingBox
    }

    @JvmStatic
    fun boundingBox(rectangles: Collection<Rectangle2D>): Rectangle2D? {
      var boundingBox: Rectangle2D? = null
      for (rectangle in rectangles) {
        boundingBox = boundingBox?.createUnion(rectangle) ?: rectangle
      }
      return boundingBox
    }

    @JvmStatic
    fun area(rectangles: Collection<Rectangle2D>): Double = area(boundingBox(rectangles)!!)

    @JvmStatic
    fun <T> nodeArea(nodes: Collection<Node<T>>): Double = area(nodeBoundingBox(nodes)!!)

    @JvmStatic
    fun <T> entryArea(entries: Collection<Map.Entry<T, Rectangle2D>>): Double =
      area(entryBoundingBox(entries)!!)

    @JvmStatic
    fun <T> entryArea(
      left: Collection<Map.Entry<T, Rectangle2D>>,
      right: Collection<Map.Entry<T, Rectangle2D>>
    ): Double = entryArea(left) + entryArea(right)

    @JvmStatic
    fun <T> nodeArea(left: Collection<Node<T>>, right: Collection<Node<T>>): Double =
      nodeArea(left) + nodeArea(right)

    @JvmStatic
    fun area(left: Collection<Rectangle2D>, right: Collection<Rectangle2D>): Double =
      area(left) + area(right)

    @JvmStatic
    fun area(r: Rectangle2D): Double {
      val area = r.width * r.height
      return if (area < 0) -area else area
    }

    @JvmStatic
    fun area(left: Rectangle2D, right: Rectangle2D): Double = area(left) + area(right)

    @JvmStatic
    fun margin(rectangles: Collection<Rectangle2D>): Double = margin(boundingBox(rectangles)!!)

    @JvmStatic
    fun margin(r: Rectangle2D): Double {
      val width = r.maxX - r.minX
      val height = r.maxY - r.minY
      return 2 * (width + height)
    }

    @JvmStatic
    fun margin(left: Rectangle2D, right: Rectangle2D): Double = margin(left) + margin(right)

    @JvmStatic
    fun margin(left: Collection<Rectangle2D>, right: Collection<Rectangle2D>): Double =
      margin(left) + margin(right)

    @JvmStatic
    fun <T> nodeMargin(left: Collection<Node<T>>, right: Collection<Node<T>>): Double =
      margin(nodeBoundingBox(left)!!) + margin(nodeBoundingBox(right)!!)

    @JvmStatic
    fun <T> entryMargin(
      left: Collection<Map.Entry<T, Rectangle2D>>,
      right: Collection<Map.Entry<T, Rectangle2D>>
    ): Double = margin(entryBoundingBox(left)!!) + margin(entryBoundingBox(right)!!)

    @JvmStatic
    fun <T> entryOverlap(
      left: Collection<Map.Entry<T, Rectangle2D>>,
      right: Collection<Map.Entry<T, Rectangle2D>>
    ): Double = overlap(entryBoundingBox(left)!!, entryBoundingBox(right)!!)

    @JvmStatic
    fun <T> nodeOverlap(left: Collection<Node<T>>, right: Collection<Node<T>>): Double =
      overlap(nodeBoundingBox(left)!!, nodeBoundingBox(right)!!)

    @JvmStatic
    fun overlap(left: Collection<Rectangle2D>, right: Collection<Rectangle2D>): Double =
      overlap(boundingBox(left)!!, boundingBox(right)!!)

    @JvmStatic
    fun overlap(left: Rectangle2D, right: Rectangle2D): Double =
      area(left.createIntersection(right))

    @JvmStatic
    fun union(boundedItems: Collection<Bounded>): Rectangle2D? {
      var union: Rectangle2D? = null
      for (r in boundedItems) {
        union = if (union == null) r.getBounds() else r.getBounds().createUnion(union)
      }
      return union
    }

    @JvmStatic
    fun width(boundedItems: Collection<Bounded>): Double {
      var min = 600.0
      var max = 0.0
      for (b in boundedItems) {
        min = Math.min(b.getBounds().minX, min)
        max = Math.max(b.getBounds().maxX, max)
      }
      return max - min
    }
  }
}
