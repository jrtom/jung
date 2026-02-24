package edu.uci.ics.jung.visualization.spatial

import com.google.common.collect.ImmutableMap
import com.google.common.collect.Lists
import com.google.common.collect.Sets
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.layout.util.LayoutChangeListener
import edu.uci.ics.jung.layout.util.LayoutEvent
import edu.uci.ics.jung.layout.util.LayoutNetworkEvent
import java.awt.Shape
import java.awt.geom.Ellipse2D
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.util.Collections
import java.util.ConcurrentModificationException
import org.slf4j.LoggerFactory

/**
 * A spatial data structure that uses a quadtree.
 *
 * @author Tom Nelson
 * @param N the node type
 */
open class SpatialQuadTree<N : Any> : AbstractSpatial<N, N>, TreeNode, Spatial<N>, LayoutChangeListener<N> {

  companion object {
    private val log = LoggerFactory.getLogger(SpatialQuadTree::class.java)
  }

  private val lock = Any()

  override fun getBounds(): Rectangle2D = rectangle!!

  override fun getChildren(): Collection<TreeNode> =
    children?.values ?: Collections.emptyList()

  /** the four quadrant keys for the child cells */
  enum class Quadrant {
    NE, NW, SW, SE
  }

  /** how many nodes per cell */
  private var MAX_OBJECTS = 1

  /** max tree height */
  private var MAX_LEVELS = 12

  /** the level of this cell in the tree */
  private val level: Int

  /** the nodes contained in this cell, assuming this cell is a leaf */
  private val nodes: MutableSet<N>

  /** the area for this cell */
  private var area: Rectangle2D

  /** a collection of child nodes, assuming this is not a leaf */
  private var children: Map<Quadrant, SpatialQuadTree<N>>? = null

  /** a cache of grid cell rectangles for performance, shadows the parent's gridCache */
  private var nodesGridCache: MutableList<Spatial<*>>? = null

  constructor(layoutModel: LayoutModel<N>) :
    this(layoutModel, 0, 0.0, 0.0, layoutModel.width.toDouble(), layoutModel.height.toDouble())

  constructor(layoutModel: LayoutModel<N>, width: Double, height: Double) :
    this(layoutModel, 0, 0.0, 0.0, width, height)

  constructor(
    layoutModel: LayoutModel<N>,
    level: Int,
    x: Double,
    y: Double,
    width: Double,
    height: Double
  ) : this(layoutModel, level, Rectangle2D.Double(x, y, width, height))

  constructor(layoutModel: LayoutModel<N>, pLevel: Int, area: Rectangle2D) : super(layoutModel) {
    level = pLevel
    nodes = Collections.synchronizedSet(Sets.newHashSet())
    this.area = area
  }

  fun setMaxObjects(o: Int): SpatialQuadTree<N> {
    MAX_OBJECTS = o
    return this
  }

  fun setMaxLevels(l: Int): SpatialQuadTree<N> {
    MAX_LEVELS = l
    return this
  }

  internal fun getLevel(): Int = level

  fun getNodes(): Set<N> = nodes

  override fun clear() {
    nodes.clear()
    synchronized(lock) {
      children = null
      nodesGridCache = null
    }
  }

  protected fun split() {
    log.trace("splitting {}", this)
    val width = area.width / 2
    val height = area.height / 2
    val x = area.x
    val y = area.y

    val childLevel = level + 1
    val ne = SpatialQuadTree<N>(layoutModel, childLevel, x + width, y, width, height)
    val nw = SpatialQuadTree<N>(layoutModel, childLevel, x, y, width, height)
    val sw = SpatialQuadTree<N>(layoutModel, childLevel, x, y + height, width, height)
    val se = SpatialQuadTree<N>(layoutModel, childLevel, x + width, y + height, width, height)
    synchronized(lock) {
      children = ImmutableMap.of(
        Quadrant.NE, ne,
        Quadrant.NW, nw,
        Quadrant.SW, sw,
        Quadrant.SE, se
      )
    }
  }

  protected fun getQuadrant(p: Point): Quadrant? = getQuadrant(p.x, p.y)

  protected fun getQuadrant(x: Double, y: Double): Quadrant? {
    val centerX = area.centerX
    val centerY = area.centerY

    val inNorth = y < centerY
    val inSouth = y >= centerY
    val inWest = x < centerX

    if (inNorth && inWest) return Quadrant.NW
    if (inSouth && inWest) return Quadrant.SW
    val inEast = x >= centerX
    if (inNorth && inEast) return Quadrant.NE
    if (inSouth && inEast) return Quadrant.SE
    return null
  }

  internal fun insert(p: N) {
    nodesGridCache = null
    log.trace("{} inserting {} at {}", this, p, layoutModel.apply(p))
    if (children != null) {
      // there are child QuadTrees available
      val quadrant = getQuadrant(layoutModel.apply(p))
      if (quadrant != null && children!![quadrant] != null) {
        // insert into the child QuadTree
        children!![quadrant]!!.insert(p)
        return
      }
    }
    // insert into this QuadTree unless capacity is exceeded
    nodes.add(p)
    // if capacity is exceeded, split and put all objects into child QuadTrees
    if (nodes.size > MAX_OBJECTS && level < MAX_LEVELS) {
      split()
      // now this QuadTree has child QuadTrees

      val iterator = nodes.iterator()
      while (iterator.hasNext()) {
        val node = iterator.next()
        val quadrant = getQuadrant(layoutModel.apply(node))
        children!![quadrant]!!.insert(node)
        iterator.remove()
      }
    }
  }

  protected fun retrieve(returnObjects: MutableSet<N>, r: Rectangle2D): Set<N> {
    if (children == null) {
      // i am a leaf, add any nodes i have
      returnObjects.addAll(nodes)
    } else {
      for (entry in children!!.entries) {
        if (entry.value.area.intersects(r)) {
          children!![entry.key]!!.retrieve(returnObjects, r)
        }
      }
    }
    return returnObjects
  }

  /**
   * Return all objects that are within the passed shape
   * This is needed when the layout is rotated/skewed and the shape edges are no longer
   * parallel to the grid edges.
   */
  protected fun retrieve(returnObjects: MutableSet<N>, shape: Shape): Set<N> {
    if (children == null) {
      // i am a leaf, add any nodes i have
      returnObjects.addAll(nodes)
    } else {
      synchronized(lock) {
        for (entry in children!!.entries) {
          if (shape.intersects(entry.value.area)) {
            children!![entry.key]!!.retrieve(returnObjects, shape)
          }
        }
      }
    }
    return returnObjects
  }

  fun getNodes(list: MutableList<Spatial<*>>): List<Spatial<*>> {
    if (nodesGridCache == null) {
      list.addAll(this.collectNodes(list, this))
      nodesGridCache = list
    }
    return nodesGridCache!!
  }

  override fun getGrid(): List<Shape> {
    val areas: MutableList<Shape> = Lists.newArrayList()
    return collectGrids(areas, this)
  }

  private fun collectGrids(list: MutableList<Shape>, tree: SpatialQuadTree<N>): List<Shape> {
    list.add(tree.area)
    if (tree.children != null) {
      for (entry in tree.children!!.entries) {
        collectGrids(list, entry.value)
      }
    }
    return list
  }

  private fun collectNodes(list: MutableList<Spatial<*>>, tree: SpatialQuadTree<N>): List<Spatial<*>> {
    list.add(tree)
    if (tree.children != null) {
      for (entry in tree.children!!.entries) {
        collectNodes(list, entry.value)
      }
    }
    return list
  }

  override fun getVisibleElements(shape: Shape): Set<N> {
    if (!isActive()) {
      log.trace("not active so getting from the graph")
      return layoutModel.graph.nodes()
    }

    _pickShapes.add(shape)
    val set: MutableSet<N> = Sets.newHashSet()
    val visibleNodes = this.retrieve(set, shape)
    if (log.isDebugEnabled) {
      log.debug("visibleNodes:{}", visibleNodes)
    }

    return visibleNodes
  }

  fun getVisibleNodes(r: Rectangle2D): Set<N> {
    if (!isActive()) {
      log.trace("not active so getting from the graph")
      return layoutModel.graph.nodes()
    }

    val set: MutableSet<N> = Sets.newHashSet()
    val visibleNodes = this.retrieve(set, r)
    if (log.isDebugEnabled) {
      log.debug("visibleNodes:{}", visibleNodes)
    }
    return visibleNodes
  }

  override fun getLayoutArea(): Rectangle2D = area

  override fun recalculate() {
    if (isActive()) {
      recalculate(layoutModel.graph.nodes())
    }
  }

  private fun recalculate(nodes: Collection<N>) {
    this.clear()
    while (true) {
      try {
        for (node in nodes) {
          this.insert(node)
        }
        break
      } catch (ex: ConcurrentModificationException) {
        // ignore
      }
    }
  }

  fun getContainingQuadTreeLeaf(node: N): TreeNode? {
    // find where it is now, not where the layoutModel will put it
    if (this.nodes.contains(node)) {
      if (log.isTraceEnabled) {
        log.trace("nodes {} in {} does contain {}", nodes, this, node)
      }
      return this
    }
    if (children != null) {
      for (entry in children!!.entries) {
        val child = entry.value
        val leaf = child.getContainingQuadTreeLeaf(node)
        if (leaf != null) {
          return leaf
        }
      }
    }
    return null
  }

  override fun getContainingLeafs(p: Point2D): Set<SpatialQuadTree<N>> {
    val leaf = getContainingQuadTreeLeaf(p) ?: return Collections.emptySet()
    return Collections.singleton(leaf)
  }

  override fun getContainingLeafs(x: Double, y: Double): Set<SpatialQuadTree<N>> {
    val leaf = getContainingQuadTreeLeaf(x, y) ?: return Collections.emptySet()
    return Collections.singleton(leaf)
  }

  @Suppress("UNCHECKED_CAST")
  override fun getContainingLeaf(element: Any): TreeNode? =
    getContainingQuadTreeLeaf(element as N)

  fun getContainingQuadTreeLeaf(p: Point2D): SpatialQuadTree<N>? =
    getContainingQuadTreeLeaf(p.x, p.y)

  fun getContainingQuadTreeLeaf(x: Double, y: Double): SpatialQuadTree<N>? {
    if (this.area.contains(x, y)) {
      if (this.children != null) {
        for (entry in this.children!!.entries) {
          if (entry.value.area.contains(x, y)) {
            return entry.value.getContainingQuadTreeLeaf(x, y)
          }
        }
      } else {
        // i am a leaf. return myself
        return this
      }
    }
    return null
  }

  override fun getClosestElement(p: Point2D): N? = getClosestElement(p.y, p.y)

  override fun getClosestElement(x: Double, y: Double): N? {
    if (!isActive()) {
      return fallback.getNode(layoutModel, x, y)
    }
    val leaf: Spatial<*>? = getContainingQuadTreeLeaf(x, y)
    val area = leaf?.getLayoutArea() ?: return null
    var radius = area.width
    var closest: N? = null
    while (closest == null) {
      val diameter = radius * 2

      val searchArea = Ellipse2D.Double(x - radius, y - radius, diameter, diameter)

      val nodes = getVisibleElements(searchArea)
      closest = getClosest(nodes, x, y, radius)

      // if I have already considered all of the nodes in the graph
      // (in the spatialquadtree) there is no reason to enlarge the
      // area and try again
      if (nodes.size >= layoutModel.graph.nodes().size) {
        break
      }
      // double the search area size and try again
      radius *= 2
    }
    return closest
  }

  override fun setBounds(bounds: Rectangle2D) {
    nodesGridCache = null
    this.area = bounds
  }

  override fun update(node: N, location: Point) {
    if (isActive()) {
      nodesGridCache = null
      if (!this.getLayoutArea().contains(location.x, location.y)) {
        log.trace("$location outside of spatial ${this.getLayoutArea()}")
        this.setBounds(this.getUnion(this.getLayoutArea(), location.x, location.y))
        this.recalculate(layoutModel.graph.nodes())
      }
      val locationContainingLeaf: Spatial<*>? = getContainingQuadTreeLeaf(location.x, location.y)
      log.trace("leaf {} contains {}", locationContainingLeaf, location)
      val nodeContainingLeaf = getContainingQuadTreeLeaf(node)
      log.trace("leaf {} contains node {}", nodeContainingLeaf, node)
      if (locationContainingLeaf == null) {
        log.trace("got null for leaf containing {}", location)
      }
      if (nodeContainingLeaf == null) {
        log.trace("got null for leaf containing {}", node)
      }
      if (locationContainingLeaf != null && locationContainingLeaf != nodeContainingLeaf) {
        log.trace("time to recalculate")
        this.recalculate(layoutModel.graph.nodes())
      }
      this.insert(node)
    }
  }

  override fun layoutChanged(evt: LayoutEvent<N>) {
    val location = evt.location
    val node = evt.node
    this.update(node, evt.location)
  }

  override fun layoutChanged(evt: LayoutNetworkEvent<N>) {
    this.update(evt.node, evt.location)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false

    other as SpatialQuadTree<*>

    if (level != other.level) return false
    if (nodes != other.nodes) return false
    if (area != other.area) return false
    return layoutModel == other.layoutModel
  }

  override fun hashCode(): Int {
    var result = level
    result = 31 * result + nodes.hashCode()
    result = 31 * result + area.hashCode()
    result = 31 * result + layoutModel.hashCode()
    return result
  }

  override fun toString(): String =
    "SpatialQuadTree{level=$level, nodes=$nodes, area=$area, children=$children}"
}
