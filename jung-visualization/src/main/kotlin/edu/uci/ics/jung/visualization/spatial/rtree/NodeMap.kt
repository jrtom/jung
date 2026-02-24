package edu.uci.ics.jung.visualization.spatial.rtree

import java.awt.geom.Rectangle2D
import org.slf4j.LoggerFactory

/**
 * A Map of elements to Rectangle2D where the union of the child elements is kept up to date
 * with the values in the Map
 *
 * @author Tom Nelson
 */
open class NodeMap<N> : HashMap<N, Rectangle2D>, BoundedMap<N>, Bounded {

  companion object {
    private val log = LoggerFactory.getLogger(NodeMap::class.java)
  }

  private var bounds: Rectangle2D? = null

  constructor() : super()

  constructor(initialCapacity: Int) : super(initialCapacity)

  constructor(map: Map<N, Rectangle2D>) : super(map) {
    recalculateBounds()
  }

  fun put(entry: Map.Entry<N, Rectangle2D>) {
    put(entry.key, entry.value)
  }

  override fun put(key: N, value: Rectangle2D): Rectangle2D? {
    addBoundsFor(value)
    return super.put(key, value)
  }

  override fun remove(key: N): Rectangle2D? {
    val removed = super<HashMap>.remove(key)
    recalculateBounds()
    return removed
  }

  override fun clear() {
    super.clear()
    bounds = null
  }

  override fun getBounds(): Rectangle2D = bounds ?: Rectangle2D.Double()

  private fun addBoundsFor(kids: Map<out N, Rectangle2D>) {
    for (entry in kids.entries) {
      addBoundsFor(entry.value)
    }
  }

  private fun addBoundsFor(r: Rectangle2D) {
    bounds = bounds?.createUnion(r) ?: r
  }

  /** iterate over all children and update the bounds. Called after removing from the collection */
  override fun recalculateBounds() {
    bounds = null
    for (r in this.values) {
      addBoundsFor(r)
    }
  }
}
