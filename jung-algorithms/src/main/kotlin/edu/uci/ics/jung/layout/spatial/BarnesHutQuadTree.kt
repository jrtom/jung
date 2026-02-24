package edu.uci.ics.jung.layout.spatial

import com.google.common.base.Preconditions
import edu.uci.ics.jung.layout.model.Point
import org.slf4j.LoggerFactory

/**
 * A QuadTree that can gather combined forces from visited nodes. Inspired by
 * http://arborjs.org/docs/barnes-hut
 * http://www.cs.princeton.edu/courses/archive/fall03/cs126/assignments/barnes-hut.html
 * https://github.com/chindesaurus/BarnesHut-N-Body
 *
 * @author Tom Nelson
 */
class BarnesHutQuadTree<T : Any>(
  /** the root node of the quad tree */
  private var root: Node<T>
) {

  private val lock = Any()

  constructor(width: Double, height: Double) : this(Node<T>(Rectangle(0.0, 0.0, width, height)))

  constructor(r: Rectangle) : this(Node<T>(r))

  /**
   * the bounds of this quad tree
   *
   * @return
   */
  val bounds: Rectangle
    get() = root.bounds

  /**
   * @return the root [Node] of this tree
   */
  fun getRoot(): Node<T> = root

  /*
   * Clears the quadtree
   */
  fun clear() {
    root.clear()
  }

  /**
   * passed [ForceObject] will visit nodes in the quad tree and accumulate their forces
   *
   * @param visitor
   */
  fun applyForcesTo(visitor: ForceObject<T>) {
    Preconditions.checkArgument(visitor != null, "Cannot apply forces to a null ForceObject")
    if (root.forceObject !== visitor) {
      root.applyForcesTo(visitor)
    }
  }

  /*
   * Insert the object into the quadtree. If the node exceeds the capacity, it
   * will split and add all objects to their corresponding nodes.
   */
  internal fun insert(node: ForceObject<T>) {
    synchronized(lock) {
      root.insert(node)
    }
  }

  /**
   * rebuild the quad tree with the nodes and location mappings of the passed LayoutModel
   *
   * @param locations - mapping of elements to locations
   */
  fun rebuild(locations: Map<T, Point>) {
    clear()
    synchronized(lock) {
      for ((key, value) in locations) {
        val forceObject = ForceObject(key, value)
        insert(forceObject)
      }
    }
  }

  override fun toString(): String = "Tree:$root"

  companion object {
    private val log = LoggerFactory.getLogger(BarnesHutQuadTree::class.java)
  }
}
