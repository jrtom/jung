package edu.uci.ics.jung.visualization.spatial

import com.google.common.collect.EvictingQueue
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.layout.util.RadiusNetworkNodeAccessor
import java.awt.Shape
import java.awt.geom.Rectangle2D
import org.slf4j.LoggerFactory

/**
 * @param T the element type that is managed by the spatial data structure. Node or Edge
 * @param NT the node type for the LayoutModel reference. Could be the same as T.
 */
abstract class AbstractSpatial<T, NT : Any>(
  protected val layoutModel: LayoutModel<NT>
) : Spatial<T> {

  companion object {
    private val log = LoggerFactory.getLogger(AbstractSpatial::class.java)
  }

  /** should this model actively update itself */
  var _active: Boolean = false

  protected var rectangle: Rectangle2D? = null

  protected var _pickShapes: MutableCollection<Shape> = EvictingQueue.create(4)

  /** a memoization of the grid rectangles used for rendering as Paintables for debugging */
  protected open var gridCache: List<Shape>? = null

  @JvmField
  protected var fallback: RadiusNetworkNodeAccessor<NT>

  init {
    this.rectangle = Rectangle2D.Double(0.0, 0.0, layoutModel.width.toDouble(), layoutModel.height.toDouble())
    this.fallback = RadiusNetworkNodeAccessor()
  }

  override fun getPickShapes(): Collection<Shape> = _pickShapes

  override fun isActive(): Boolean = _active

  override fun setActive(active: Boolean) {
    gridCache = null
    this._active = active
  }

  protected fun getClosest(nodes: Collection<NT>, x: Double, y: Double, radius: Double): NT? {
    // since I am comparing with distance squared, i need to square the radius
    val radiusSq = radius * radius
    if (nodes.isNotEmpty()) {
      var closestSoFar = Double.MAX_VALUE
      var winner: NT? = null
      var winningDistance = -1.0
      for (node in nodes) {
        val loc = layoutModel.apply(node)
        val dist = loc.distanceSquared(x, y)

        // consider only nodes that are inside the search radius
        // and are closer than previously found nodes
        if (dist < radiusSq && dist < closestSoFar) {
          closestSoFar = dist
          winner = node
          winningDistance = dist
        }
      }
      if (log.isTraceEnabled) {
        log.trace("closest winner is {} at distance {}", winner, winningDistance)
      }
      return winner
    } else {
      return null
    }
  }

  override fun layoutStateChanged(evt: LayoutModel.LayoutStateChangeEvent) {
    // if the layoutmodel is not _active, then it is safe to activate this
    log.trace("layoutStateChanged:{}", evt)
    setActive(!evt.active)
    // if the layout model is finished, then rebuild the spatial data structure
    if (!evt.active) {
      log.trace("will recalcluate")
      recalculate()
      if (layoutModel is LayoutModel.ChangeSupport) {
        (layoutModel as LayoutModel.ChangeSupport).fireChanged() // this will cause a repaint
      }
    }
  }
}
