package edu.uci.ics.jung.visualization.layout

import com.google.common.graph.EndpointPair
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.visualization.RenderContext
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.util.Context
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import org.slf4j.LoggerFactory

@Suppress("UNCHECKED_CAST")
abstract class BoundingRectangleCollector<T : Any>(
    protected val rc: RenderContext<*, *>,
    protected val visualizationModel: VisualizationModel<*, *>
) {
    protected var _rectangles: MutableList<Rectangle2D> = ArrayList()

    init {
        compute()
    }

    class Points<N : Any>(rc: RenderContext<*, *>, visualizationModel: VisualizationModel<*, *>) :
        BoundingRectangleCollector<N>(rc, visualizationModel) {

        override fun getForElement(element: N): Rectangle2D {
            val shape: Shape = Rectangle2D.Double()
            val p = (visualizationModel.getLayoutModel() as LayoutModel<Any>).apply(element) as Point
            val x = p.x.toFloat()
            val y = p.y.toFloat()
            val xform = AffineTransform.getTranslateInstance(x.toDouble(), y.toDouble())
            val xfs = xform.createTransformedShape(shape).bounds2D
            log.trace("node {} with shape bounds {} is at {}", element, xfs, p)
            return xfs
        }

        override fun getForElement(element: N, p1: Point, p2: Point): Rectangle2D =
            getForElement(element, p1)

        override fun getForElement(element: N, p: Point): Rectangle2D {
            val shape = (rc as RenderContext<N, *>).getNodeShapeFunction().apply(element)
            log.trace("node is at {}", p)
            val x = p.x.toFloat()
            val y = p.y.toFloat()
            val xform = AffineTransform.getTranslateInstance(x.toDouble(), y.toDouble())
            return xform.createTransformedShape(shape).bounds2D
        }

        fun compute(nodes: Collection<*>) {
            super.compute()
            val layoutModel = visualizationModel.getLayoutModel() as LayoutModel<Any>
            for (v in nodes) {
                if (v == null) continue
                val shape = (rc as RenderContext<Any, *>).getNodeShapeFunction().apply(v)
                val p = layoutModel.apply(v) as Point2D
                val x = p.x.toFloat()
                val y = p.y.toFloat()
                val xform = AffineTransform.getTranslateInstance(x.toDouble(), y.toDouble())
                val transformed = xform.createTransformedShape(shape)
                _rectangles.add(transformed.bounds2D)
            }
        }

        override fun compute() {
            super.compute()
            val layoutModel = visualizationModel.getLayoutModel() as LayoutModel<Any>
            for (v in visualizationModel.getNetwork().nodes()) {
                val shape = (rc as RenderContext<Any, *>).getNodeShapeFunction().apply(v)
                val p = layoutModel.apply(v) as Point2D
                val x = p.x.toFloat()
                val y = p.y.toFloat()
                val xform = AffineTransform.getTranslateInstance(x.toDouble(), y.toDouble())
                val transformed = xform.createTransformedShape(shape)
                _rectangles.add(transformed.bounds2D)
            }
        }

        companion object {
            private val log = LoggerFactory.getLogger(Points::class.java)
        }
    }

    class Nodes<N : Any>(rc: RenderContext<*, *>, visualizationModel: VisualizationModel<*, *>) :
        BoundingRectangleCollector<N>(rc, visualizationModel) {

        override fun getForElement(element: N): Rectangle2D {
            val shape = (rc as RenderContext<N, *>).getNodeShapeFunction().apply(element)
            val p = (visualizationModel.getLayoutModel() as LayoutModel<Any>).apply(element) as Point
            val x = p.x.toFloat()
            val y = p.y.toFloat()
            val xform = AffineTransform.getTranslateInstance(x.toDouble(), y.toDouble())
            val xfs = xform.createTransformedShape(shape).bounds2D
            log.trace("node {} with shape bounds {} is at {}", element, xfs, p)
            return xfs
        }

        override fun getForElement(element: N, p1: Point, p2: Point): Rectangle2D =
            getForElement(element, p1)

        override fun getForElement(element: N, p: Point): Rectangle2D {
            val shape = (rc as RenderContext<N, *>).getNodeShapeFunction().apply(element)
            log.trace("node is at {}", p)
            val x = p.x.toFloat()
            val y = p.y.toFloat()
            val xform = AffineTransform.getTranslateInstance(x.toDouble(), y.toDouble())
            return xform.createTransformedShape(shape).bounds2D
        }

        fun compute(nodes: Collection<*>) {
            super.compute()
            val layoutModel = visualizationModel.getLayoutModel() as LayoutModel<Any>
            for (v in nodes) {
                if (v == null) continue
                val shape = (rc as RenderContext<Any, *>).getNodeShapeFunction().apply(v)
                val p = layoutModel.apply(v) as Point
                val x = p.x.toFloat()
                val y = p.y.toFloat()
                val xform = AffineTransform.getTranslateInstance(x.toDouble(), y.toDouble())
                val transformed = xform.createTransformedShape(shape)
                _rectangles.add(transformed.bounds2D)
            }
        }

        override fun compute() {
            super.compute()
            val layoutModel = visualizationModel.getLayoutModel() as LayoutModel<Any>
            for (v in visualizationModel.getNetwork().nodes()) {
                val shape = (rc as RenderContext<Any, *>).getNodeShapeFunction().apply(v)
                val p = layoutModel.apply(v) as Point
                val x = p.x.toFloat()
                val y = p.y.toFloat()
                val xform = AffineTransform.getTranslateInstance(x.toDouble(), y.toDouble())
                val transformed = xform.createTransformedShape(shape)
                _rectangles.add(transformed.bounds2D)
            }
        }

        companion object {
            private val log = LoggerFactory.getLogger(Nodes::class.java)
        }
    }

    class Edges<E : Any>(rc: RenderContext<*, *>, visualizationModel: VisualizationModel<*, *>) :
        BoundingRectangleCollector<E>(rc, visualizationModel) {

        override fun getForElement(element: E): Rectangle2D {
            val network = visualizationModel.getNetwork() as com.google.common.graph.Network<Any, Any>
            val layoutModel = visualizationModel.getLayoutModel() as LayoutModel<Any>
            val endpoints = network.incidentNodes(element) as EndpointPair<Any>
            val v1 = endpoints.nodeU()
            val v2 = endpoints.nodeV()
            val p1 = layoutModel.apply(v1) as Point
            val p2 = layoutModel.apply(v2) as Point
            val x1 = p1.x.toFloat()
            val y1 = p1.y.toFloat()
            val x2 = p2.x.toFloat()
            val y2 = p2.y.toFloat()

            val isLoop = v1 == v2
            val s2 = (rc as RenderContext<Any, Any>).getNodeShapeFunction().apply(v2)
            var edgeShape = rc.getEdgeShapeFunction()
                .apply(Context.getInstance(visualizationModel.getNetwork() as com.google.common.graph.Network<Any, Any>, element as Any))

            val xform = AffineTransform.getTranslateInstance(x1.toDouble(), y1.toDouble())
            if (isLoop) {
                val s2Bounds = s2.bounds2D
                xform.scale(s2Bounds.width, s2Bounds.height)
                xform.translate(0.0, -edgeShape.bounds2D.width / 2)
            } else {
                val dx = x2 - x1
                val dy = y2 - y1
                val theta = Math.atan2(dy.toDouble(), dx.toDouble()).toFloat()
                xform.rotate(theta.toDouble())
                val dist = p1.distance(p2).toFloat()
                xform.scale(dist.toDouble(), 1.0)
            }
            edgeShape = xform.createTransformedShape(edgeShape)
            return edgeShape.bounds2D
        }

        override fun getForElement(element: E, p: Point): Rectangle2D =
            getForElement(element, p, p)

        override fun getForElement(element: E, p1: Point, p2: Point): Rectangle2D {
            val network = visualizationModel.getNetwork() as com.google.common.graph.Network<Any, Any>
            val endpoints = network.incidentNodes(element) as EndpointPair<Any>
            val v1 = endpoints.nodeU()
            val v2 = endpoints.nodeV()
            val x1 = p1.x.toFloat()
            val y1 = p1.y.toFloat()
            val x2 = p2.x.toFloat()
            val y2 = p2.y.toFloat()

            val isLoop = v1 == v2
            val s2 = (rc as RenderContext<Any, Any>).getNodeShapeFunction().apply(v2)
            var edgeShape = rc.getEdgeShapeFunction()
                .apply(Context.getInstance(visualizationModel.getNetwork() as com.google.common.graph.Network<Any, Any>, element as Any))

            val xform = AffineTransform.getTranslateInstance(x1.toDouble(), y1.toDouble())
            if (isLoop) {
                val s2Bounds = s2.bounds2D
                xform.scale(s2Bounds.width, s2Bounds.height)
                xform.translate(0.0, -edgeShape.bounds2D.width / 2)
            } else {
                val dx = x2 - x1
                val dy = y2 - y1
                val theta = Math.atan2(dy.toDouble(), dx.toDouble()).toFloat()
                xform.rotate(theta.toDouble())
                val dist = p1.distance(p2).toFloat()
                xform.scale(dist.toDouble(), 1.0)
            }
            edgeShape = xform.createTransformedShape(edgeShape)
            return edgeShape.bounds2D
        }

        override fun compute() {
            super.compute()
            val network = visualizationModel.getNetwork() as com.google.common.graph.Network<Any, Any>
            val layoutModel = visualizationModel.getLayoutModel() as LayoutModel<Any>
            for (e in network.edges()) {
                val endpoints = network.incidentNodes(e) as EndpointPair<Any>
                val v1 = endpoints.nodeU()
                val v2 = endpoints.nodeV()
                val p1 = layoutModel.apply(v1) as Point
                val p2 = layoutModel.apply(v2) as Point
                val x1 = p1.x.toFloat()
                val y1 = p1.y.toFloat()
                val x2 = p2.x.toFloat()
                val y2 = p2.y.toFloat()

                val isLoop = v1 == v2
                val s2 = (rc as RenderContext<Any, Any>).getNodeShapeFunction().apply(v2)
                var edgeShape = rc.getEdgeShapeFunction()
                    .apply(Context.getInstance(visualizationModel.getNetwork() as com.google.common.graph.Network<Any, Any>, e as Any))

                val xform = AffineTransform.getTranslateInstance(x1.toDouble(), y1.toDouble())
                if (isLoop) {
                    val s2Bounds = s2.bounds2D
                    xform.scale(s2Bounds.width, s2Bounds.height)
                    xform.translate(0.0, -edgeShape.bounds2D.width / 2)
                } else {
                    val dx = x2 - x1
                    val dy = y2 - y1
                    val theta = Math.atan2(dy.toDouble(), dx.toDouble()).toFloat()
                    xform.rotate(theta.toDouble())
                    val dist = p1.distance(p2).toFloat()
                    xform.scale(dist.toDouble(), 1.0)
                }
                edgeShape = xform.createTransformedShape(edgeShape)
                _rectangles.add(edgeShape.bounds2D)
            }
        }
    }

    abstract fun getForElement(element: T): Rectangle2D

    abstract fun getForElement(element: T, p: Point): Rectangle2D

    abstract fun getForElement(element: T, p1: Point, p2: Point): Rectangle2D

    /**
     * @return the _rectangles
     */
    fun getRectangles(): List<Rectangle2D> = _rectangles

    open fun compute() {
        _rectangles.clear()
    }
}
