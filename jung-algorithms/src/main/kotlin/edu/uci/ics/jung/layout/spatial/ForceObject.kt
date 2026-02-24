package edu.uci.ics.jung.layout.spatial

import edu.uci.ics.jung.layout.model.Point
import org.slf4j.LoggerFactory

/**
 * An instance used to gather forces while visiting the BarnesHut QuadTree.
 *
 * @author Tom Nelson
 */
open class ForceObject<T : Any>(
  val element: T,
  /** location of p */
  val p: Point,
  /** mass */
  protected var mass: Double = 1.0
) {

  /** force vector */
  var f: Point = Point.ORIGIN

  constructor(element: T, x: Double, y: Double) : this(element, Point.of(x, y), 1.0)

  constructor(element: T, x: Double, y: Double, mass: Double) : this(element, Point.of(x, y), mass)

  /**
   * override in the layoutAlgorithm to apply forces in a way that is consistent with the chosen
   * implementation. See FRBHVisitorLayoutAlgorithm and SpringVisitorLayoutAlgorithm.
   *
   * @param other the ForceObject (a node or a force vector) to apply force from
   */
  open fun addForceFrom(other: ForceObject<T>) {
    // no op
  }

  fun add(other: ForceObject<T>): ForceObject<T> {
    val totalMass = this.mass + other.mass
    val p = Point.of(
      (this.p.x * this.mass + other.p.x * other.mass) / totalMass,
      (this.p.y * this.mass + other.p.y * other.mass) / totalMass
    )
    @Suppress("UNCHECKED_CAST")
    return ForceObject("force" as T, p, totalMass)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false

    other as ForceObject<*>

    if (java.lang.Double.compare(other.mass, mass) != 0) return false
    if (p != other.p) return false
    return element == other.element
  }

  override fun hashCode(): Int {
    var result = p.hashCode()
    val temp = java.lang.Double.doubleToLongBits(mass)
    result = 31 * result + (temp xor (temp ushr 32)).toInt()
    result = 31 * result + element.hashCode()
    return result
  }

  override fun toString(): String =
    "ForceObject{element=$element, p=$p, mass=$mass, force=$f}"

  companion object {
    private val log = LoggerFactory.getLogger(ForceObject::class.java)
  }
}
