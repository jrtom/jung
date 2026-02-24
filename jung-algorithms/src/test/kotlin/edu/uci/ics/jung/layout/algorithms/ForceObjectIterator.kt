package edu.uci.ics.jung.layout.algorithms

import com.google.common.collect.Sets
import edu.uci.ics.jung.layout.spatial.BarnesHutQuadTree
import edu.uci.ics.jung.layout.spatial.ForceObject
import edu.uci.ics.jung.layout.spatial.Node
import java.util.Collections

/**
 * An iterator over the (logn) force objects to apply to the passed target This approach is slower
 * than visiting the BarnesHutQuadTree, but the results should be identical for each approach. This
 * exists for testing and verification only.
 *
 * package level access for the associated test algorithm implementations only
 *
 * @author Tom Nelson
 */
internal class ForceObjectIterator<T : Any>(
  private val tree: BarnesHutQuadTree<T>,
  private val target: ForceObject<T>
) : Iterator<ForceObject<T>> {

  private val forceObjects: Set<ForceObject<T>>
  private val iterator: Iterator<ForceObject<T>>

  init {
    forceObjects = getForceObjectsFor(Sets.newLinkedHashSet(), target)
    iterator = forceObjects.iterator()
  }

  override fun hasNext(): Boolean = iterator.hasNext()

  override fun next(): ForceObject<T> = iterator.next()

  private fun getForceObjectsFor(
    forceObjects: MutableSet<ForceObject<T>>,
    target: ForceObject<T>
  ): Set<ForceObject<T>> {
    val root = tree.getRoot()
    return if (root.forceObject != target) {
      getForceObjectsFor(forceObjects, target, root)
    } else {
      Collections.emptySet()
    }
  }

  private fun getForceObjectsFor(
    forceObjects: MutableSet<ForceObject<T>>,
    target: ForceObject<T>,
    from: Node<T>
  ): Set<ForceObject<T>> {
    val THETA = 0.5

    if (from.forceObject == null || target == from.forceObject) {
      forceObjects.add(target)
    }

    if (from.isLeaf) {
      from.forceObject?.let { forceObjects.add(it) }
    } else {
      // not a leaf
      //  this node is an internal node
      //  calculate s/d
      val s = from.area.width
      //      distance between the incoming node's position and
      //      the center of mass for this node
      val d = from.forceObject!!.p.distance(target.p)
      if (s / d < THETA) {
        // this node is sufficiently far away
        // just use this node's forces
        forceObjects.add(from.forceObject!!)
      } else {
        // down the tree we go
        getForceObjectsFor(forceObjects, target, from.NW!!)
        getForceObjectsFor(forceObjects, target, from.NE!!)
        getForceObjectsFor(forceObjects, target, from.SW!!)
        getForceObjectsFor(forceObjects, target, from.SE!!)
      }
    }
    return forceObjects
  }
}
