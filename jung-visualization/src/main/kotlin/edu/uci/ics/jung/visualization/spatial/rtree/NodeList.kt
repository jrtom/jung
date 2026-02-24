package edu.uci.ics.jung.visualization.spatial.rtree

import java.awt.geom.Rectangle2D
import org.slf4j.LoggerFactory

/**
 * A list of elements that are Bounded by a Rectangle2D. The bounding box of the list is the union
 * of its elements
 *
 * @author Tom Nelson
 */
open class NodeList<B : Bounded> : ArrayList<B>, BoundedList<B>, Bounded {

  companion object {
    private val log = LoggerFactory.getLogger(NodeList::class.java)
  }

  private var bounds: Rectangle2D? = null

  constructor() : super()

  constructor(initialCapacity: Int) : super(initialCapacity)

  constructor(list: Collection<B>) : super() {
    for (node in list) {
      add(node)
    }
  }

  constructor(vararg nodes: B) : super() {
    for (node in nodes) {
      add(node)
    }
  }

  override fun add(element: B): Boolean {
    if (element is Node<*>) {
      val node = element as Node<*>
      if (node.getParent() == null || !node.getParent().isPresent) {
        log.error("adding a node {} with unset parent {}", node, node.getParent())
      }
    } else {
      log.error("adding something that is not a Node: {}", element)
    }
    addBoundsFor(element)
    return super.add(element)
  }

  override fun add(index: Int, element: B) {
    addBoundsFor(element)
    super.add(index, element)
  }

  override fun removeAt(index: Int): B {
    val removed = super.removeAt(index)
    recalculateBounds()
    return removed
  }

  override fun remove(element: B): Boolean {
    val removed = super.remove(element)
    recalculateBounds()
    return removed
  }

  override fun clear() {
    super.clear()
    bounds = null
  }

  override fun addAll(elements: Collection<B>): Boolean {
    addBoundsForCollection(elements)
    return super.addAll(elements)
  }

  override fun addAll(index: Int, elements: Collection<B>): Boolean {
    return super.addAll(index, elements)
  }

  override fun removeRange(fromIndex: Int, toIndex: Int) {
    super.removeRange(fromIndex, toIndex)
  }

  override fun removeAll(elements: Collection<B>): Boolean {
    return super.removeAll(elements.toSet())
  }

  override fun retainAll(elements: Collection<B>): Boolean {
    return super.retainAll(elements.toSet())
  }

  override fun getBounds(): Rectangle2D {
    return bounds ?: Rectangle2D.Double()
  }

  private fun addBoundsForCollection(kids: Collection<B>) {
    for (kid in kids) {
      addBoundsFor(kid)
    }
  }

  private fun addBoundsFor(kid: B) {
    bounds = bounds?.createUnion(kid.getBounds()) ?: kid.getBounds()
  }

  /** iterate over all children and update the bounds. Called after removing from the collection */
  override fun recalculateBounds() {
    bounds = null
    for (n in this) {
      addBoundsFor(n)
    }
  }
}
