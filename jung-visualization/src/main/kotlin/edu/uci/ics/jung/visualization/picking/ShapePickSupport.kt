/*
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * Created on Mar 11, 2005
 *
 */
package edu.uci.ics.jung.visualization.picking

import com.google.common.collect.Sets
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor
import edu.uci.ics.jung.visualization.spatial.Spatial
import edu.uci.ics.jung.visualization.spatial.SpatialRTree
import edu.uci.ics.jung.visualization.spatial.rtree.Node
import edu.uci.ics.jung.visualization.util.Context
import org.slf4j.LoggerFactory
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Ellipse2D
import java.awt.geom.GeneralPath
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.util.ConcurrentModificationException
import java.util.HashSet
import java.util.function.Predicate

/**
 * A `NetworkElementAccessor` that returns elements whose `Shape` contains the
 * specified pick point or region.
 *
 * @author Tom Nelson
 */
open class ShapePickSupport<N : Any, E : Any> : NetworkElementAccessor<N, E> {

    companion object {
        private val log = LoggerFactory.getLogger(ShapePickSupport::class.java)
    }

    /**
     * The available picking heuristics:
     *
     *  * `Style.CENTERED`: returns the element whose center is closest to the pick point.
     *  * `Style.LOWEST`: returns the first such element encountered. (If the element
     *      collection has a consistent ordering, this will also be the element "on the bottom", that
     *      is, the one which is rendered first.)
     *  * `Style.HIGHEST`: returns the last such element encountered. (If the element
     *      collection has a consistent ordering, this will also be the element "on the top", that
     *      is, the one which is rendered last.)
     */
    enum class Style {
        LOWEST, CENTERED, HIGHEST
    }

    protected var _pickSize: Float

    /**
     * The `VisualizationServer` in which the this instance is being used for picking. Used
     * to retrieve properties such as the layout, renderer, node and edge shapes, and coordinate
     * transformations.
     */
    protected val vv: VisualizationServer<N, E>

    /** The current picking heuristic for this instance. Defaults to `CENTERED`. */
    var style: Style = Style.CENTERED

    /**
     * Creates a `ShapePickSupport` for the `vv` VisualizationServer, with the
     * specified pick footprint and the default pick style. The `VisualizationServer` is
     * used to access properties of the current visualization (layout, renderer, coordinate
     * transformations, node/edge shapes, etc.).
     *
     * @param vv source of the current `Layout`.
     * @param _pickSize the layoutSize of the pick footprint for line edges
     */
    constructor(vv: VisualizationServer<N, E>, _pickSize: Float) {
        this.vv = vv
        this._pickSize = _pickSize
    }

    /**
     * Create a `ShapePickSupport` for the specified `VisualizationServer` with
     * a default pick footprint of layoutSize 2.
     *
     * @param vv the visualization server used for rendering
     */
    constructor(vv: VisualizationServer<N, E>) {
        this.vv = vv
        this._pickSize = 2f
    }

    override fun getNode(layoutModel: LayoutModel<N>, p: Point): N? {
        return getNode(layoutModel, p.x, p.y)
    }

    /**
     * Returns the node, if any, whose shape contains (x, y). If (x,y) is contained in more than one
     * node's shape, returns the node whose center is closest to the pick point.
     *
     * @param x the x coordinate of the pick point
     * @param y the y coordinate of the pick point
     * @return the node whose shape contains (x,y), and whose center is closest to the pick point
     */
    override fun getNode(layoutModel: LayoutModel<N>, x: Double, y: Double): N? {
        log.trace("look for node at (layout coords) {},{}", x, y)
        val transformSupport = vv.getTransformSupport()
        var closest: N? = null
        var minDistance = Double.MAX_VALUE
        // x,y is in layout coordinate system.
        val pickPoint = Point2D.Double(x, y)

        val nodeSpatial = vv.getNodeSpatial()
        if (nodeSpatial.isActive()) {
            return getNode(nodeSpatial, layoutModel, pickPoint.x, pickPoint.y)
        }

        // fall back on checking every node
        while (true) {
            try {
                for (v in getFilteredNodes()) {
                    // get the shape for the node (it is at the origin)
                    var shape = vv.getRenderContext().getNodeShapeFunction().apply(v)
                    // get the node location in layout coordinate system
                    val p = layoutModel.apply(v) ?: continue
                    // translate the shape to the node location in layout coordinates
                    val xform = AffineTransform.getTranslateInstance(p.x, p.y)
                    shape = xform.createTransformedShape(shape)

                    if (shape.contains(pickPoint.x, pickPoint.y)) {
                        if (style == Style.LOWEST) {
                            // return the first match
                            return v
                        } else if (style == Style.HIGHEST) {
                            // will return the last match
                            closest = v
                        } else {
                            // return the node closest to the
                            // center of a node shape
                            val bounds = shape.getBounds2D()
                            val dx = bounds.centerX - pickPoint.y
                            val dy = bounds.centerY - pickPoint.y
                            val dist = dx * dx + dy * dy
                            if (dist < minDistance) {
                                minDistance = dist
                                closest = v
                            }
                        }
                    }
                }
                break
            } catch (cme: ConcurrentModificationException) {
                // retry
            }
        }
        return closest
    }

    /**
     * uses the spatialRTree to find the closest node to the points
     *
     * @param spatial
     * @param layoutModel
     * @param x in the layout coordinate system
     * @param y in the layout coordinate system
     * @return the picked node
     */
    protected open fun getNode(
        spatial: Spatial<N>,
        layoutModel: LayoutModel<N>,
        x: Double,
        y: Double
    ): N? {
        val transformSupport = vv.getTransformSupport()

        // find the leaf node that would contain a point at x,y
        val containingLeafs = spatial.getContainingLeafs(Point2D.Double(x, y))
        if (log.isTraceEnabled) {
            log.trace("leaf for {},{} is {}", x, y, containingLeafs)
        }
        if (containingLeafs == null || containingLeafs.isEmpty()) return null
        // make a target circle the same size as the leaf node
        // leaf nodes are small when nodes are close and large when they are sparse
        // union up all the leafs then make a target
        var union: Rectangle2D? = null
        for (r in containingLeafs) {
            union = if (union == null) {
                r.getBounds()
            } else {
                union.createUnion(r.getBounds())
            }
        }
        val width = union!!.width
        val height = union.height
        val radiusx = width / 2
        val radiusy = height / 2
        val target = Ellipse2D.Double(x - radiusx, y - radiusy, width, height)
        if (log.isTraceEnabled) {
            log.trace("target is {}", target)
        }

        var minDistance = Double.MAX_VALUE

        // will be the picked node
        var closest: N? = null

        // get the all nodes from any leafs that intersect the target
        val nodes = spatial.getVisibleElements(target)
        if (log.isTraceEnabled) {
            log.trace("instead of checking all nodes: {}", getFilteredNodes())
            log.trace("out of these candidates: {}...", nodes)
        }
        // Check the (smaller) set of eligible nodes
        // to return the one that contains the (x,y)
        for (node in nodes) {
            // get the shape for the node (centered at the origin)
            var shape = vv.getRenderContext().getNodeShapeFunction().apply(node)
            // get the node location
            val p = layoutModel.apply(node) ?: continue
            // translate the node shape to its location in layout coordinates
            val xform = AffineTransform.getTranslateInstance(p.x, p.y)
            shape = xform.createTransformedShape(shape)

            // translate the pick point from layout coords to screen coords
            val layoutPoint = Point2D.Double(x, y)
            log.trace("layout coords of pick point: {}", layoutPoint)
            val screenPoint = transformSupport.transform(vv, layoutPoint)
            log.trace("screen coords of pick point: {}", screenPoint)
            shape = transformSupport.transform(vv, shape)
            log.trace("looking in a shape at {} for {}", Node.asString(shape.getBounds2D()), screenPoint)

            if (shape.contains(screenPoint)) {
                if (style == Style.LOWEST) {
                    // return the first match
                    return node
                } else if (style == Style.HIGHEST) {
                    // will return the last match
                    closest = node
                } else {
                    // return the node closest to the
                    // center of a node shape
                    val bounds = shape.getBounds2D()
                    val dx = bounds.centerX - x
                    val dy = bounds.centerY - y
                    val dist = dx * dx + dy * dy
                    if (dist < minDistance) {
                        minDistance = dist
                        closest = node
                    }
                }
            }
        }
        if (log.isTraceEnabled) {
            log.trace("picked {} with spatial quadtree", closest)
        }
        return closest
    }

    /**
     * Returns the nodes whose layout coordinates are contained in `Shape`. The shape is in
     * screen coordinates, and the graph nodes are transformed to screen coordinates before they are
     * tested for inclusion.
     *
     * @return the `Collection` of nodes whose `layout` coordinates are
     *     contained in `shape`.
     */
    override fun getNodes(layoutModel: LayoutModel<N>, shape: Shape): Collection<N> {
        val pickedNodes = HashSet<N>()

        // the pick target shape is in layout coordinate system.

        val spatial = vv.getNodeSpatial()
        if (spatial != null) {
            return getContained(spatial, layoutModel, shape)
        }

        // fall back on checking every node
        while (true) {
            try {
                for (v in getFilteredNodes()) {
                    val p = layoutModel.apply(v) ?: continue
                    if (shape.contains(p.x, p.y)) {
                        pickedNodes.add(v)
                    }
                }
                break
            } catch (cme: ConcurrentModificationException) {
                // retry
            }
        }
        return pickedNodes
    }

    /**
     * use the spatial structure to find nodes inside the passed shape
     *
     * @param spatial
     * @param layoutModel
     * @param shape a target shape in layout coordinates
     * @return the nodes contained in the target shape
     */
    protected open fun getContained(
        spatial: Spatial<N>,
        layoutModel: LayoutModel<N>,
        shape: Shape
    ): Collection<N> {
        val visible: MutableCollection<N> = Sets.newHashSet(spatial.getVisibleElements(shape))
        if (log.isTraceEnabled) {
            log.trace("your shape intersects tree cells with these nodes: {}", visible)
        }

        // some of the nodes that the spatial tree considers visible may be outside
        // of the pick target shape. Check this smaller set of nodes and return only
        // those that are inside the shape
        val iterator = visible.iterator()
        while (iterator.hasNext()) {
            val node = iterator.next()
            val p = layoutModel.apply(node) ?: continue
            if (!shape.contains(p.x, p.y)) {
                iterator.remove()
            }
        }
        if (log.isTraceEnabled) {
            log.trace("these were actually picked: {}", visible)
        }
        return visible
    }

    /**
     * use the spatial R tree to find edges inside the passed shape
     *
     * @param spatial
     * @param layoutModel
     * @param shape a target shape in layout coordinates
     * @return the nodes contained in the target shape
     */
    protected open fun getContained(
        spatial: SpatialRTree.Edges<E, N>,
        layoutModel: LayoutModel<N>,
        shape: Shape
    ): Collection<E> {
        val visible: MutableCollection<E> = spatial.getVisibleElements(shape).toMutableList()
        if (log.isTraceEnabled) {
            log.trace("your shape intersects tree cells with these nodes: {}", visible)
        }
        // some of the nodes that the spatial tree considers visible may be outside
        // of the pick target shape. Check this smaller set of nodes and return only
        // those that intersect the shape
        val iterator = visible.iterator()
        while (iterator.hasNext()) {
            val edge = iterator.next()
            val edgeShape = getTransformedEdgeShape(edge) ?: continue
            if (!edgeShape.intersects(shape.bounds)) {
                iterator.remove()
            }
        }
        if (log.isTraceEnabled) {
            log.trace("these were actually picked: {}", visible)
        }
        return visible
    }

    /**
     * Returns an edge whose shape intersects the 'pickArea' footprint of the passed x,y, coordinates.
     *
     * @param x the x coordinate of the location (layout coordinate system)
     * @param y the y coordinate of the location (layout coordinate system)
     * @return an edge whose shape intersects the pick area centered on the location `(x,y)`
     */
    override fun getEdge(layoutModel: LayoutModel<N>, x: Double, y: Double): E? {
        // as a Line has no area, we can't always use edgeshape.contains(point) so we
        // make a small rectangular pickArea around the point and check if the
        // edgeshape.intersects(pickArea)
        val pickArea = Rectangle2D.Float(
            (x - _pickSize / 2).toFloat(),
            (y - _pickSize / 2).toFloat(),
            _pickSize,
            _pickSize
        )
        var closest: E? = null
        var minDistance = Double.MAX_VALUE
        val pickPoint = Point2D.Double(x, y)

        val edgeSpatial = vv.getEdgeSpatial()
        if (edgeSpatial != null && edgeSpatial is SpatialRTree.Edges<*, *>) {
            @Suppress("UNCHECKED_CAST")
            return getEdge(
                edgeSpatial as SpatialRTree.Edges<E, N>,
                layoutModel,
                pickPoint.x,
                pickPoint.y
            )
        }
        while (true) {
            try {
                // this checks every edge.
                for (e in getFilteredEdges()) {
                    val edgeShape = getTransformedEdgeShape(e) ?: continue

                    // because of the transform, the edgeShape is now a GeneralPath
                    // see if this edge is the closest of any that intersect
                    if (edgeShape.intersects(pickArea)) {
                        var cx = 0f
                        var cy = 0f
                        val f = FloatArray(6)
                        val pi = GeneralPath(edgeShape).getPathIterator(null)
                        if (!pi.isDone) {
                            pi.next()
                            pi.currentSegment(f)
                            cx = f[0]
                            cy = f[1]
                            if (!pi.isDone) {
                                pi.currentSegment(f)
                                cx = f[0]
                                cy = f[1]
                            }
                        }
                        val dx = cx - x.toFloat()
                        val dy = cy - y.toFloat()
                        val dist = dx * dx + dy * dy
                        if (dist < minDistance) {
                            minDistance = dist.toDouble()
                            closest = e
                        }
                    }
                }
                break
            } catch (cme: ConcurrentModificationException) {
                // retry
            }
        }
        return closest
    }

    override fun getEdge(layoutModel: LayoutModel<N>, p: Point2D): E? {
        return getEdge(layoutModel, p.x, p.y)
    }

    /**
     * uses the spatialRTree to find the closest node to the points
     *
     * @param spatial
     * @param layoutModel
     * @param x in the layout coordinate system
     * @param y in the layout coordinate system
     * @return the picked node
     */
    protected open fun getEdge(
        spatial: SpatialRTree.Edges<E, N>,
        layoutModel: LayoutModel<N>,
        x: Double,
        y: Double
    ): E? {
        // find the leaf nodes that would contain a point at x,y
        val containingLeafs = spatial.getContainingLeafs(Point2D.Double(x, y))
        if (log.isTraceEnabled) {
            log.trace("leaf for {},{} is {}", x, y, containingLeafs)
        }
        if (containingLeafs == null || containingLeafs.isEmpty()) return null
        // make a target circle the same size as the leaf node area union
        // leaf nodes are small when nodes are close and large when they are sparse
        // union up all the leafs then make a target
        var union: Rectangle2D? = null
        for (r in containingLeafs) {
            union = if (union == null) {
                r.getBounds()
            } else {
                union.createUnion(r.getBounds())
            }
        }
        val width = union!!.width
        val height = union.height
        val radiusx = width / 2
        val radiusy = height / 2
        val target = Ellipse2D.Double(x - radiusx, y - radiusy, width, height)
        if (log.isTraceEnabled) {
            log.trace("target is {}", target)
        }

        var minDistance = Double.MAX_VALUE

        // will be the picked edge
        var closest: E? = null

        // get the all nodes from any leafs that intersect the target
        val edges = spatial.getVisibleElements(target)
        if (log.isTraceEnabled) {
            log.trace(
                "instead of checking all {} edges: {}",
                getFilteredEdges().size,
                getFilteredEdges()
            )
            log.trace("out of these {} candidates: {}...", edges.size, edges)
        }

        val pickArea = Rectangle2D.Float(
            (x - _pickSize / 2).toFloat(),
            (y - _pickSize / 2).toFloat(),
            _pickSize,
            _pickSize
        )

        // Check the (smaller) set of eligible nodes
        // to return the one that contains the (x,y)
        for (edge in edges) {
            val edgeShape = getTransformedEdgeShape(edge) ?: continue

            // because of the transform, the edgeShape is now a GeneralPath
            // see if this edge is the closest of any that intersect
            if (edgeShape.intersects(pickArea)) {
                var cx = 0f
                var cy = 0f
                val f = FloatArray(6)
                val pi = GeneralPath(edgeShape).getPathIterator(null)
                if (!pi.isDone) {
                    pi.next()
                    pi.currentSegment(f)
                    cx = f[0]
                    cy = f[1]
                    if (!pi.isDone) {
                        pi.currentSegment(f)
                        cx = f[0]
                        cy = f[1]
                    }
                }
                val dx = cx - x.toFloat()
                val dy = cy - y.toFloat()
                val dist = dx * dx + dy * dy
                if (dist < minDistance) {
                    minDistance = dist.toDouble()
                    closest = edge
                }
            }
        }
        return closest
    }

    /**
     * Retrieves the shape template for `e` and transforms it according to the positions of
     * its endpoints in `layout`.
     *
     * @param e the edge whose shape is to be returned
     * @return the transformed shape
     */
    private fun getTransformedEdgeShape(e: E): Shape? {
        val endpoints = vv.getModel().getNetwork().incidentNodes(e)
        val v1 = endpoints.nodeU()
        val v2 = endpoints.nodeV()
        val isLoop = v1 == v2
        val layoutModel = vv.getModel().getLayoutModel()
        val p1 = layoutModel.apply(v1)
        val p2 = layoutModel.apply(v2)
        if (p1 == null || p2 == null) {
            return null
        }
        val x1 = p1.x.toFloat()
        val y1 = p1.y.toFloat()
        val x2 = p2.x.toFloat()
        val y2 = p2.y.toFloat()

        // translate the edge to the starting node
        val xform = AffineTransform.getTranslateInstance(x1.toDouble(), y1.toDouble())

        var edgeShape = vv.getRenderContext()
            .getEdgeShapeFunction()
            .apply(Context.getInstance(vv.getModel().getNetwork(), e))
        if (isLoop) {
            // make the loops proportional to the layoutSize of the node
            val s2 = vv.getRenderContext().getNodeShapeFunction().apply(v2)
            val s2Bounds = s2.getBounds2D()
            xform.scale(s2Bounds.width, s2Bounds.height)
            // move the loop so that the nadir is centered in the node
            xform.translate(0.0, -edgeShape.getBounds2D().height / 2)
        } else {
            val dx = x2 - x1
            val dy = y2 - y1
            // rotate the edge to the angle between the nodes
            val theta = Math.atan2(dy.toDouble(), dx.toDouble())
            xform.rotate(theta)
            // stretch the edge to span the distance between the nodes
            val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
            xform.scale(dist.toDouble(), 1.0)
        }

        // transform the edge to its location and dimensions
        edgeShape = xform.createTransformedShape(edgeShape)
        return edgeShape
    }

    protected open fun getFilteredNodes(): Collection<N> {
        val nodes = vv.getModel().getNetwork().nodes()
        return if (nodesAreFiltered()) {
            Sets.filter(nodes) { n -> vv.getRenderContext().getNodeIncludePredicate().test(n) }
        } else {
            nodes
        }
    }

    protected open fun getFilteredEdges(): Collection<E> {
        val edges = vv.getModel().getNetwork().edges()
        return if (edgesAreFiltered()) {
            Sets.filter(edges) { e -> vv.getRenderContext().getEdgeIncludePredicate().test(e) }
        } else {
            edges
        }
    }

    /**
     * Quick test to allow optimization of `getFilteredNodes()`.
     *
     * @return `true` if there is an relaxing node filtering mechanism for this
     *     visualization, `false` otherwise
     */
    protected open fun nodesAreFiltered(): Boolean {
        val nodeIncludePredicate = vv.getRenderContext().getNodeIncludePredicate()
        return nodeIncludePredicate != null &&
            nodeIncludePredicate != Predicate<N> { true }
    }

    /**
     * Quick test to allow optimization of `getFilteredEdges()`.
     *
     * @return `true` if there is an relaxing edge filtering mechanism for this
     *     visualization, `false` otherwise
     */
    protected open fun edgesAreFiltered(): Boolean {
        val edgeIncludePredicate = vv.getRenderContext().getEdgeIncludePredicate()
        return edgeIncludePredicate != null &&
            edgeIncludePredicate != Predicate<E> { true }
    }

    /**
     * Returns `true` if this node in this graph is included in the collections of elements
     * to be rendered, and `false` otherwise.
     *
     * @return `true` if this node is included in the collections of elements to be
     *     rendered, `false` otherwise.
     */
    protected open fun isNodeRendered(node: N): Boolean {
        val nodeIncludePredicate = vv.getRenderContext().getNodeIncludePredicate()
        return nodeIncludePredicate == null || nodeIncludePredicate.test(node)
    }

    /**
     * Returns `true` if this edge and its endpoints in this graph are all included in the
     * collections of elements to be rendered, and `false` otherwise.
     *
     * @return `true` if this edge and its endpoints are all included in the collections of
     *     elements to be rendered, `false` otherwise.
     */
    protected open fun isEdgeRendered(edge: E): Boolean {
        val nodeIncludePredicate = vv.getRenderContext().getNodeIncludePredicate()
        val edgeIncludePredicate = vv.getRenderContext().getEdgeIncludePredicate()
        val g = vv.getModel().getNetwork()
        if (edgeIncludePredicate != null && !edgeIncludePredicate.test(edge)) {
            return false
        }
        val endpoints = g.incidentNodes(edge)
        val v1 = endpoints.nodeU()
        val v2 = endpoints.nodeV()
        return nodeIncludePredicate == null ||
            (nodeIncludePredicate.test(v1) && nodeIncludePredicate.test(v2))
    }

    /**
     * Returns the layoutSize of the edge picking area. The picking area is square; the layoutSize is
     * specified as the length of one side, in view coordinates.
     *
     * @return the layoutSize of the edge picking area
     */
    fun getPickSize(): Float = _pickSize

    /**
     * Sets the layoutSize of the edge picking area.
     *
     * @param _pickSize the length of one side of the (square) picking area, in view coordinates
     */
    fun setPickSize(pickSize: Float) {
        this._pickSize = pickSize
    }
}
