package edu.uci.ics.jung.visualization.spatial

import com.google.common.collect.Lists
import com.google.common.collect.Sets
import com.google.common.graph.EndpointPair
import com.google.common.graph.Network
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.layout.util.LayoutChangeListener
import edu.uci.ics.jung.layout.util.LayoutEvent
import edu.uci.ics.jung.layout.util.LayoutNetworkEvent
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.layout.BoundingRectangleCollector
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor
import edu.uci.ics.jung.visualization.layout.RadiusNetworkElementAccessor
import edu.uci.ics.jung.visualization.spatial.rtree.LeafNode
import edu.uci.ics.jung.visualization.spatial.rtree.Node
import edu.uci.ics.jung.visualization.spatial.rtree.RTree
import edu.uci.ics.jung.visualization.spatial.rtree.SplitterContext
import java.awt.Shape
import java.awt.geom.Ellipse2D
import java.awt.geom.Line2D
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.util.Collections
import org.slf4j.LoggerFactory

/**
 * @param T The Type for the elements managed by the RTree, Nodes or Edges
 * @param NT The Type for the Nodes of the graph. May be the same as T
 * @author Tom Nelson
 */
abstract class SpatialRTree<T : Any, NT : Any>(
  layoutModel: LayoutModel<NT>,
  /** a container for the splitter functions to use, quadratic or R*Tree */
  protected val splitterContext: SplitterContext<T>
) : AbstractSpatial<T, NT>(layoutModel), Spatial<T> {

  companion object {
    private val log = LoggerFactory.getLogger(SpatialRTree::class.java)
  }

  /** the RTree to use. Add/Remove methods may change this to a new immutable RTree reference */
  protected var rtree: RTree<T> = RTree.create()

  /** gathers the bounding rectangles of the elements managed by the RTree. Node or Edge shapes */
  protected var boundingRectangleCollector: BoundingRectangleCollector<T>? = null

  /**
   * gather the RTree nodes into a list for display as Paintables
   */
  protected abstract fun collectGrids(list: MutableList<Shape>, tree: RTree<T>): List<Shape>

  /**
   * @return the 2 dimensional area of interest for this class
   */
  override fun getLayoutArea(): Rectangle2D = rectangle!!

  /**
   * @param bounds the new bounds for the data structure
   */
  override fun setBounds(bounds: Rectangle2D) {
    this.rectangle = bounds
  }

  override fun getGrid(): List<Shape> {
    if (gridCache == null) {
      log.trace("getting Grid from tree size {}", rtree.count())
      if (!isActive()) {
        // just return the entire layout area
        return Collections.singletonList(getLayoutArea())
      }
      val areas: MutableList<Shape> = Lists.newArrayList()

      gridCache = collectGrids(areas, rtree)
      log.trace("getGrid got {} and {}", areas.size, gridCache!!.size)
      return gridCache!!
    } else {
      return gridCache!!
    }
  }

  override fun clear() {
    rtree = RTree.create()
  }

  override fun getContainingLeafs(x: Double, y: Double): Set<LeafNode<T>> {
    if (!isActive() || !rtree.getRoot().isPresent) {
      return Collections.emptySet()
    }

    val theRoot = rtree.getRoot().get()
    return theRoot.getContainingLeafs(Sets.newHashSet(), x, y)
  }

  override fun getContainingLeafs(p: Point2D): Set<LeafNode<T>> =
    getContainingLeafs(p.x, p.y)

  @Suppress("UNCHECKED_CAST")
  override fun getContainingLeaf(element: Any): LeafNode<T>? {
    if (!rtree.getRoot().isPresent) {
      return null // nothing in this tree
    }
    val theRoot = rtree.getRoot().get()
    return theRoot.getContainingLeaf(element as T)
  }

  protected fun recalculate(elements: Collection<T>) {
    log.trace("start recalculate")
    clear()
    if (boundingRectangleCollector != null) {
      for (element in elements) {
        rtree = rtree.add(
          splitterContext,
          element,
          boundingRectangleCollector!!.getForElement(element)
        )
        log.trace("added {} got {} nodes in {}", element, rtree.count(), rtree)
      }
    } else {
      log.trace("got no rectangles")
    }
    log.trace("end recalculate")
  }

  class Nodes<N : Any> : SpatialRTree<N, N>, Spatial<N>, LayoutChangeListener<N> {

    companion object {
      private val log = LoggerFactory.getLogger(Nodes::class.java)
    }

    constructor(layoutModel: LayoutModel<N>, splitterContext: SplitterContext<N>) :
      super(layoutModel, splitterContext) {
      rtree = RTree.create()
    }

    @Suppress("UNCHECKED_CAST")
    constructor(
      visualizationModel: VisualizationModel<*, *>,
      boundingRectangleCollector: BoundingRectangleCollector.Nodes<N>,
      splitterContext: SplitterContext<N>
    ) : super(visualizationModel.getLayoutModel() as LayoutModel<N>, splitterContext) {
      this.boundingRectangleCollector = boundingRectangleCollector
      rtree = RTree.create()
    }

    override fun getVisibleElements(shape: Shape): Set<N> {
      if (!isActive() || !rtree.getRoot().isPresent) {
        return layoutModel.graph.nodes()
      }
      _pickShapes.add(shape)

      val root = rtree.getRoot().get()
      log.trace("out of nodes {}", layoutModel.graph.nodes())
      val visibleElements: MutableSet<N> = Sets.newHashSet()
      return root.getVisibleElements(visibleElements, shape)
    }

    override fun update(element: N, location: Point) {
      gridCache = null
      // do nothing if we are not active
      if (isActive() && rtree.getRoot().isPresent) {
        val containingLeaf = getContainingLeaf(element)
        val itsShape = boundingRectangleCollector!!.getForElement(element, location)
        if (containingLeaf != null) {
          if (containingLeaf.getBounds().contains(itsShape)) {
            containingLeaf.remove(element)
            containingLeaf.add(splitterContext, element, itsShape)
          } else {
            rtree = rtree.remove(element)
            rtree = rtree.add(splitterContext, element, itsShape)
          }
        } else {
          rtree = rtree.add(splitterContext, element, itsShape)
        }
      }
    }

    override fun getClosestElement(p: Point2D): N? = getClosestElement(p.x, p.y)

    override fun getClosestElement(x: Double, y: Double): N? {
      if (!isActive() || !rtree.getRoot().isPresent) {
        // use the fallback NetworkNodeAccessor
        return fallback.getNode(layoutModel, x, y)
      }

      var radius = layoutModel.width / 20.0

      var closest: N? = null
      while (closest == null) {
        val diameter = radius * 2

        val searchArea = Ellipse2D.Double(x - radius, y - radius, diameter, diameter)

        val nodes = getVisibleElements(searchArea)
        closest = getClosest(nodes, x, y, radius)

        // if i found a winner or
        // if I have already considered all of the nodes in the graph
        // (in the spatialtree) there is no reason to enlarge the
        // area and try again
        if (closest != null || nodes.size >= layoutModel.graph.nodes().size) {
          break
        }
        // double the search area size and try again
        radius *= 2
      }
      return closest
    }

    override fun collectGrids(list: MutableList<Shape>, tree: RTree<N>): List<Shape> {
      if (tree.getRoot().isPresent) {
        val root = tree.getRoot().get()
        root.collectGrids(list)
      }
      return list
    }

    override fun recalculate() {
      gridCache = null
      log.trace(
        "called recalculate while active:{} layout model relaxing:{}",
        isActive(),
        layoutModel.isRelaxing
      )
      if (isActive()) {
        log.trace("recalculate for nodes: {}", layoutModel.graph.nodes())
        recalculate(layoutModel.graph.nodes())
      } else {
        log.trace("no recalculate when active: {}", isActive())
      }
    }

    override fun layoutChanged(evt: LayoutEvent<N>) {
      this.update(evt.node, evt.location)
    }

    override fun layoutChanged(evt: LayoutNetworkEvent<N>) {
      update(evt.node, evt.location)
    }
  }

  class Edges<E : Any, N : Any> : SpatialRTree<E, N>, Spatial<E>, LayoutChangeListener<N> {

    companion object {
      private val log = LoggerFactory.getLogger(Edges::class.java)
    }

    private var networkElementAccessor: NetworkElementAccessor<N, E>

    // Edges gets a VisualizationModel reference to access the Network and work with edges
    private val visualizationModel: VisualizationModel<N, E>

    @Suppress("UNCHECKED_CAST")
    constructor(
      visualizationModel: VisualizationModel<N, E>,
      boundingRectangleCollector: BoundingRectangleCollector.Edges<E>,
      splitterContext: SplitterContext<E>
    ) : super(visualizationModel.getLayoutModel() as LayoutModel<N>, splitterContext) {
      this.visualizationModel = visualizationModel
      this.boundingRectangleCollector = boundingRectangleCollector
      networkElementAccessor = RadiusNetworkElementAccessor(visualizationModel.getNetwork())
      rtree = RTree.create()
      recalculate()
    }

    override fun getVisibleElements(shape: Shape): Set<E> {
      if (!isActive() || !rtree.getRoot().isPresent) {
        log.trace("not relaxing so getting from the network")
        return visualizationModel.getNetwork().edges()
      }
      _pickShapes.add(shape)
      val root = rtree.getRoot().get()
      val visibleElements: MutableSet<E> = Sets.newHashSet()
      return root.getVisibleElements(visibleElements, shape)
    }

    @Suppress("UNCHECKED_CAST")
    override fun update(element: E, location: Point) {
      gridCache = null
      if (isActive()) {
        // get the endpoints for this edge
        val nodes = visualizationModel.getNetwork().incidentNodes(element)

        // there should be 2
        var n1: N? = null
        var n2: N? = null
        val iterator = (nodes as Iterable<N>).iterator()

        if (iterator.hasNext()) {
          n1 = iterator.next()
        }
        if (iterator.hasNext()) {
          n2 = iterator.next()
        }
        if (n2 == null) {
          n2 = n1
        }
        if (n1 != null && n2 != null) {
          val itsShape = boundingRectangleCollector!!.getForElement(
            element, layoutModel.apply(n1), layoutModel.apply(n2)
          )
          val containingLeaf = getContainingLeaf(element)
          if (containingLeaf != null) {
            if (containingLeaf.getBounds().contains(itsShape)) {
              containingLeaf.remove(element)
              containingLeaf.add(splitterContext, element, itsShape)
              log.trace("{} changed in place", element)
            } else {
              containingLeaf.remove(element)
              log.trace("rtree now size {}", rtree.count())
              rtree = rtree.add(splitterContext, element, itsShape)
              log.trace("added back {} with {} into rtree size {}", element, itsShape, rtree.count())
            }
          } else {
            rtree = rtree.add(splitterContext, element, itsShape)
          }
        }
      }
    }

    override fun layoutChanged(evt: LayoutEvent<N>) {
      // need to take care of edge changes
      val node = evt.node
      val p = evt.location
      if (visualizationModel.getNetwork().nodes().contains(node)) {
        val edges: Set<E> = visualizationModel.getNetwork().incidentEdges(node)
        for (edge in edges as Iterable<E>) {
          update(edge, p)
        }
      }
    }

    override fun layoutChanged(evt: LayoutNetworkEvent<N>) {
      val node = evt.node
      val p = evt.location
      if (visualizationModel.getNetwork().nodes().contains(node)) {
        val edges: Set<E> = visualizationModel.getNetwork().incidentEdges(node)
        for (edge in edges as Iterable<E>) {
          update(edge, p)
        }
      }
    }

    override fun getClosestElement(p: Point2D): E? = getClosestElement(p.x, p.y)

    override fun getClosestElement(x: Double, y: Double): E? {
      if (!isActive() || !rtree.getRoot().isPresent) {
        // not active or empty
        // use the fallback NetworkNodeAccessor
        return networkElementAccessor.getEdge(layoutModel, x, y)
      }
      val root = rtree.getRoot().get()
      var radius = layoutModel.width / 20.0

      var closest: E? = null
      while (closest == null) {
        val diameter = radius * 2

        val searchArea = Ellipse2D.Double(x - radius, y - radius, diameter, diameter)

        val edges = getVisibleElements(searchArea)
        closest = getClosestEdge(edges, x, y, radius)

        // If i found a winner, break. also
        // if I have already considered all of the nodes in the graph
        // (in the spatialquadtree) there is no reason to enlarge the
        // area and try again
        if (closest != null || edges.size >= layoutModel.graph.edges().size) {
          break
        }
        // double the search area size and try again
        radius *= 2
      }
      return closest
    }

    protected fun getClosestEdge(edges: Collection<E>, x: Double, y: Double, radius: Double): E? {
      // since I am comparing with distance squared, i need to square the radius
      val radiusSq = radius * radius
      if (edges.isNotEmpty()) {
        var closestSoFar = Double.MAX_VALUE
        var winner: E? = null
        var winningDistance = -1.0
        for (edge in edges) {
          // get the 2 endpoints
          val network: Network<N, E> = visualizationModel.getNetwork()
          val endpoints: EndpointPair<N> = network.incidentNodes(edge)
          val u = endpoints.nodeU()
          val v = endpoints.nodeV()
          val up = layoutModel.apply(u)
          val vp = layoutModel.apply(v)
          // compute the distance between my point and a Line connecting u and v
          val line = Line2D.Double(up.x, up.y, vp.x, vp.y)
          val dist = line.ptSegDist(x, y)

          // consider only edges that cross inside the search radius
          // and are closer than previously found nodes
          if (dist < radiusSq && dist < closestSoFar) {
            closestSoFar = dist
            winner = edge
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

    override fun collectGrids(list: MutableList<Shape>, tree: RTree<E>): List<Shape> {
      if (tree.getRoot().isPresent) {
        val root = tree.getRoot().get()
        root.collectGrids(list)
      }
      return list
    }

    /** rebuild the data structure */
    override fun recalculate() {
      gridCache = null
      log.trace(
        "called recalculate while active:{} layout model relaxing:{}",
        isActive(),
        layoutModel.isRelaxing
      )
      if (isActive()) {
        log.trace("recalculate for edges: {}", visualizationModel.getNetwork().edges())
        recalculate(visualizationModel.getNetwork().edges())
      }
    }
  }

  override fun toString(): String = rtree.toString()
}
