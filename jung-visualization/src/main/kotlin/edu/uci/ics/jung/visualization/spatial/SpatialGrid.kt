package edu.uci.ics.jung.visualization.spatial

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Lists
import com.google.common.collect.Multimaps
import com.google.common.collect.Sets
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.Point
import java.awt.Dimension
import java.awt.Shape
import java.awt.geom.Area
import java.awt.geom.Ellipse2D
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.util.Arrays
import java.util.Collections
import java.util.ConcurrentModificationException
import org.slf4j.LoggerFactory

/**
 * A Spatial Data Structure to optimize rendering performance. The SpatialGrid is used to determine
 * which graph nodes are actually visible for a given rendering situation. Only the visible nodes
 * are passed to the rendering pipeline. When used with Edges, only Edges with at least one visible
 * endpoint are passed to the rendering pipeline.
 *
 * See SimpleGraphSpatialTest (jung-samples) for a rendering that exposes the internals of the
 * SpatialGrid.
 *
 * @author Tom Nelson
 */
open class SpatialGrid<N : Any> : AbstractSpatial<N, N>, Spatial<N>, TreeNode {

  companion object {
    private val log = LoggerFactory.getLogger(SpatialGrid::class.java)

    @JvmStatic
    fun <N : Any> getGrid(list: MutableList<Shape>, grid: SpatialGrid<N>): List<Shape> {
      list.addAll(grid.getGrid())
      return list
    }
  }

  /** the number of grid cells across the width */
  private var horizontalCount: Int

  /** the number of grid cells across the height */
  private var verticalCount: Int

  /** the overall size of the area to be divided into a grid */
  private lateinit var size: Dimension

  /** A mapping of grid cell identified to a collection of contained nodes */
  private val map = Multimaps.synchronizedListMultimap(ArrayListMultimap.create<Int, N>())

  /** the width of a grid cell */
  private var boxWidth: Double = 0.0

  /** the height of a grid cell */
  private var boxHeight: Double = 0.0

  /** the overall area of the layout (x,y,width,height) */
  private lateinit var layoutArea: Rectangle2D

  /** a cache of grid cell rectangles for performance */
  override var gridCache: List<Shape>? = null

  /**
   * Create an instance
   */
  constructor(layoutModel: LayoutModel<N>) : super(layoutModel) {
    this.horizontalCount = 10
    this.verticalCount = 10
    this.setBounds(Rectangle2D.Double(0.0, 0.0, layoutModel.width.toDouble(), layoutModel.height.toDouble()))
  }

  /**
   * Create an instance
   *
   * @param bounds the area of the grid
   * @param horizontalCount how many tiles in a row
   * @param verticalCount how many tiles in a column
   */
  constructor(
    layoutModel: LayoutModel<N>,
    bounds: Rectangle2D,
    horizontalCount: Int,
    verticalCount: Int
  ) : super(layoutModel) {
    this.horizontalCount = horizontalCount
    this.verticalCount = verticalCount
    this.setBounds(bounds)
  }

  /**
   * Set the layoutSize of the spatial grid and recompute the box widths and heights. null out the
   * obsolete grid cache
   *
   * @param bounds recalculate the size of the spatial area
   */
  override fun setBounds(bounds: Rectangle2D) {
    this.size = bounds.bounds.size
    this.layoutArea = bounds
    this.boxWidth = size.getWidth() / horizontalCount
    this.boxHeight = size.getHeight() / verticalCount
    this.gridCache = null
  }

  override fun getContainingLeafs(p: Point2D): Set<TreeNode> {
    val boxNumber = this.getBoxNumberFromLocation(p.x, p.y)
    val r = this.gridCache!![boxNumber] as Rectangle2D
    val grid: SpatialGrid<N> = SpatialGrid(layoutModel, r, 1, 1)
    return Collections.singleton(grid)
  }

  override fun getContainingLeafs(x: Double, y: Double): Set<TreeNode> =
    getContainingLeafs(Point2D.Double(x, y))

  override fun getContainingLeaf(element: Any): TreeNode? {
    for (entry in map.asMap().entries) {
      if (entry.value.contains(element)) {
        val index = entry.key
        val r = this.gridCache!![index] as Rectangle2D
        return SpatialGrid<N>(layoutModel, r, 1, 1)
      }
    }
    return null
  }

  /**
   * Lazily compute the gridCache if needed. The gridCache is a list of rectangles overlaying the
   * layout area. They are numbered from 0 to horizontalCount*verticalCount-1
   *
   * @return the boxes in the grid
   */
  override fun getGrid(): List<Shape> {
    if (gridCache == null) {
      val cache = Lists.newArrayList<Shape>()
      for (j in 0 until verticalCount) {
        for (i in 0 until horizontalCount) {
          cache.add(
            Rectangle2D.Double(
              layoutArea.x + i * boxWidth,
              layoutArea.y + j * boxHeight,
              boxWidth,
              boxHeight
            )
          )
        }
      }
      gridCache = cache
    }
    return gridCache!!
  }

  /**
   * A Multimap of box number to Lists of nodes in that box
   *
   * @return the map of box numbers to contained nodes
   */
  fun getMap() = map

  /**
   * given the box x,y coordinates (not the coordinate system) return the box number (0,0) has box 0
   * (horizontalCount,horizontalCount) has box horizontalCount*verticalCount - 1
   */
  protected fun getBoxNumber(boxX: Int, boxY: Int): Int {
    var bx = boxX
    var by = boxY
    if (log.isTraceEnabled) {
      log.trace(
        "{},{} clamped to {},{}",
        bx, by,
        Math.max(0, Math.min(bx, this.horizontalCount - 1)),
        Math.max(0, Math.min(by, this.verticalCount - 1))
      )
    }
    bx = Math.max(0, Math.min(bx, this.horizontalCount - 1))
    by = Math.max(0, Math.min(by, this.verticalCount - 1))
    if (log.isTraceEnabled) {
      log.trace("getBoxNumber({},{}):{}", bx, by, (by * this.horizontalCount + bx))
    }
    return by * this.horizontalCount + bx
  }

  protected fun getBoxNumber(boxXY: IntArray): Int = getBoxNumber(boxXY[0], boxXY[1])

  /**
   * give a Point in the coordinate system, return the box number that contains it
   */
  protected fun getBoxNumberFromLocation(p: Point): Int {
    var count = 0
    for (shape in getGrid()) {
      val r = shape.bounds2D
      if (r.contains(p.x, p.y) || r.intersects(p.x, p.y, 1.0, 1.0)) {
        return count
      } else {
        count++
      }
    }
    log.trace("no box for  {}", p)
    return -1
  }

  /**
   * give a Point in the coordinate system, return the box number that contains it
   */
  protected fun getBoxNumberFromLocation(x: Double, y: Double): Int {
    var count = 0
    for (shape in getGrid()) {
      val r = shape.bounds2D
      if (r.contains(x, y) || r.intersects(x, y, 1.0, 1.0)) {
        return count
      } else {
        count++
      }
    }
    log.trace("no box for {},{}", x, y)
    return -1
  }

  /**
   * given (x,y) in the coordinate system, get the boxX,boxY for the box that it is in
   */
  internal fun getBoxIndex(x: Double, y: Double): IntArray {
    var boxIndex = intArrayOf(0, 0)
    var hcount = 0
    var vcount = 0
    for (r in getGrid()) {
      if (r.contains(Point2D.Double(x, y))) {
        boxIndex = intArrayOf(hcount, vcount)
        break
      }
      hcount++
      if (hcount >= this.horizontalCount) {
        hcount = 0
        vcount++
      }
    }
    if (log.isTraceEnabled) {
      log.trace("boxIndex for ({},{}) is {}", x, y, Arrays.toString(boxIndex))
    }
    return boxIndex
  }

  override fun recalculate() {
    if (isActive()) {
      recalculate(layoutModel.graph.nodes())
    }
  }

  override fun clear() {
    this.map.clear()
  }

  /**
   * Recalculate the contents of the Map of box number to contained Nodes
   *
   * @param nodes the collection of nodes to update in the structure
   */
  fun recalculate(nodes: Collection<N>) {
    clear()
    while (true) {
      try {
        for (node in nodes) {
          this.map.put(this.getBoxNumberFromLocation(layoutModel.apply(node)), node)
        }
        break
      } catch (ex: ConcurrentModificationException) {
        // ignore
      }
    }
  }

  /**
   * update the location of a node in the map of box number to node lists
   */
  override fun update(node: N, location: Point) {
    if (isActive()) {
      if (!this.getLayoutArea().contains(location.x, location.y)) {
        log.trace("$location outside of spatial ${this.getLayoutArea()}")
        this.setBounds(this.getUnion(this.getLayoutArea(), location.x, location.y))
        recalculate(layoutModel.graph.nodes())
      }

      val rightBox = this.getBoxNumberFromLocation(layoutModel.apply(node))
      // node should end up in box 'rightBox'
      // check to see if it is already there
      if (map.get(rightBox).contains(node)) {
        // nothing to do here, just return
        return
      }
      // remove node from the first (and only) wrong box it is found in
      var wrongBox: Int? = null
      synchronized(map) {
        for (box in map.keySet()) {
          if (map.get(box).contains(node)) {
            // remove it and stop, because node can be in only one box
            wrongBox = box
            break
          }
        }
      }
      if (wrongBox != null) {
        map.remove(wrongBox, node)
      }
      map.put(rightBox, node)
    }
  }

  override fun getClosestElement(p: Point2D): N? = getClosestElement(p.x, p.y)

  override fun getClosestElement(x: Double, y: Double): N? {
    if (!isActive()) {
      return fallback.getNode(layoutModel, x, y)
    }
    val leafs = getContainingLeafs(x, y)
    if (leafs.isNotEmpty()) {
      val leaf = leafs.iterator().next()

      val area = leaf.getBounds()
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

    return null
  }

  /**
   * given a rectangular area and an offset, return the tile numbers that are contained in it
   */
  internal fun getVisibleTiles(visibleArea: Shape): Collection<Int> {
    val visibleTiles: MutableSet<Int> = Sets.newHashSet()
    val grid = getGrid()
    for (i in 0 until this.horizontalCount * this.verticalCount) {
      if (visibleArea.intersects(grid[i].bounds2D)) {
        visibleTiles.add(i)
      }
    }
    if (log.isDebugEnabled) {
      log.debug("visible boxes:{}", visibleTiles)
    }
    return visibleTiles
  }

  /**
   * Given an area, return a collection of the nodes that are contained in it (the nodes that are
   * contained in the boxes that intersect with the area)
   */
  override fun getVisibleElements(visibleArea: Shape): Set<N> {
    if (!isActive()) {
      log.trace("not active so getting from the graph")
      return layoutModel.graph.nodes()
    }

    _pickShapes.add(visibleArea)
    val area = Area(visibleArea)
    area.intersect(Area(this.layoutArea))
    if (log.isTraceEnabled) {
      log.trace("map is {}", map)
    }
    val visibleNodes: MutableSet<N> = Sets.newHashSet()
    val tiles = getVisibleTiles(area)
    for (index in tiles) {
      val toAdd = this.map.get(index)
      if (toAdd.size > 0) {
        visibleNodes.addAll(toAdd)
        if (log.isTraceEnabled) {
          log.trace("added all of: {} from index {} to visibleNodes", toAdd, index)
        }
      }
    }
    if (log.isDebugEnabled) {
      log.debug("visibleNodes:{}", visibleNodes)
    }
    return visibleNodes
  }

  /**
   * @return the layout area rectangle for this grid
   */
  override fun getLayoutArea(): Rectangle2D = layoutArea

  override fun getBounds(): Rectangle2D = layoutArea

  override fun getChildren(): Collection<TreeNode>? = null
}
