package edu.uci.ics.jung.layout.spatial

import com.google.common.base.Preconditions
import org.slf4j.LoggerFactory

/**
 * A Node in the BarnesHutQuadTree. Has a rectangular dimension and a ForceObject that may be either
 * a graph node or a representation of the combined forces of the child nodes. May have 4 child
 * nodes.
 *
 * @author Tom Nelson
 */
class Node<T : Any>(
  val area: Rectangle
) {

  // a node contains a ForceObject and possibly 4 Nodes
  var forceObject: ForceObject<T>? = null

  var NW: Node<T>? = null
  var NE: Node<T>? = null
  var SE: Node<T>? = null
  var SW: Node<T>? = null

  constructor(x: Double, y: Double, width: Double, height: Double) : this(Rectangle(x, y, width, height))

  /**
   * @return true if this node has no child nodes, false otherwise
   */
  val isLeaf: Boolean
    get() = NW == null && NE == null && SE == null && SW == null

  /**
   * @return the rectangular bounds of this node
   */
  val bounds: Rectangle
    get() = area

  /**
   * insert a new ForceObject into the tree. This changes the combinedMass and the forceVector for
   * any Node that it is inserted into
   *
   * @param element
   */
  fun insert(element: ForceObject<T>) {
    if (log.isTraceEnabled) {
      log.trace("insert {} into {}", element, this)
    }
    val currentForce = forceObject
    if (currentForce == null) {
      forceObject = element
      return
    }
    if (isLeaf) {
      if (currentForce.p == element.p) {
        // compare points for special case where the 2 elements are at the same location
        // this would cause an infinite attempt to split and re-insert
        // just add the new mass
        forceObject = currentForce.add(element)
      } else {
        // there already is a forceObject and location is different, so split
        split()
        // put the current resident and the new one into the correct quadrants
        insertForceObject(currentForce)
        insertForceObject(element)
        // update the centerOfMass, Mass, and Force on this node
        forceObject = currentForce.add(element)
      }
    } else {
      if (currentForce === element) {
        log.error("can't insert {} into {}", element, currentForce)
      }
      // we're already split, update the forceElement for this new element
      forceObject = currentForce.add(element)
      // and follow down the tree to insert
      insertForceObject(element)
    }
  }

  /**
   * insert into the correct quadrant of this inner node
   *
   * @param forceObject object to insert
   */
  private fun insertForceObject(forceObject: ForceObject<T>) {
    when {
      NW!!.area.contains(forceObject.p) -> NW!!.insert(forceObject)
      NE!!.area.contains(forceObject.p) -> NE!!.insert(forceObject)
      SE!!.area.contains(forceObject.p) -> SE!!.insert(forceObject)
      SW!!.area.contains(forceObject.p) -> SW!!.insert(forceObject)
    }
  }

  /** remove all child nodes */
  fun clear() {
    forceObject = null
    NW = null
    NE = null
    SW = null
    SE = null
  }

  /*
   * Splits the Quadtree into 4 sub-QuadTrees
   */
  protected fun split() {
    if (log.isTraceEnabled) {
      log.trace("splitting {}", this)
    }
    val width = area.width / 2
    val height = area.height / 2
    val x = area.x
    val y = area.y
    NE = Node(x + width, y, width, height)
    NW = Node(x, y, width, height)
    SW = Node(x, y + height, width, height)
    SE = Node(x + width, y + height, width, height)
  }

  /**
   * accept a visit from the visitor force object, and add this node's forces to the visitor
   *
   * @param visitor the visitor
   */
  fun applyForcesTo(visitor: ForceObject<T>) {
    Preconditions.checkArgument(visitor != null, "Cannot apply forces to a null ForceObject")
    val currentForce = this.forceObject
    if (currentForce == null || visitor.element == currentForce.element) {
      return
    }

    if (isLeaf) {
      visitor.addForceFrom(currentForce)
    } else {
      // not a leaf. this node is an internal node
      //  calculate s/d
      val s = this.area.width
      //      distance between the incoming node's position and
      //      the center of mass for this node
      val d = currentForce.p.distance(visitor.p)
      if (s / d < THETA) {
        // this node is sufficiently far away, just use this node's forces
        visitor.addForceFrom(currentForce)
      } else {
        // down the tree we go
        NW!!.applyForcesTo(visitor)
        NE!!.applyForcesTo(visitor)
        SW!!.applyForcesTo(visitor)
        SE!!.applyForcesTo(visitor)
      }
    }
  }

  override fun toString(): String = asString("", this, "")

  companion object {
    private val log = LoggerFactory.getLogger(Node::class.java)
    const val THETA: Double = 0.5

    private fun asString(r: Rectangle): String =
      "[${r.x.toInt()},${r.y.toInt()},${r.width.toInt()},${r.height.toInt()}]"

    private fun <T : Any> asString(label: String, node: Node<T>, margin: String): String {
      val marginIncrement = "   "
      val s = StringBuilder()
      s.append("\n")
      s.append(margin)
      s.append(label)
      s.append("bounds=")
      s.append(asString(node.bounds))
      val forceObject = node.forceObject
      if (forceObject != null) {
        s.append(", forceObject:=")
        s.append(forceObject.toString())
      }
      node.NW?.let { s.append(asString("NW:", it, margin + marginIncrement)) }
      node.NE?.let { s.append(asString("NE:", it, margin + marginIncrement)) }
      node.SW?.let { s.append(asString("SW:", it, margin + marginIncrement)) }
      node.SE?.let { s.append(asString("SE:", it, margin + marginIncrement)) }
      return s.toString()
    }
  }
}
