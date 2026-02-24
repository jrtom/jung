package edu.uci.ics.jung.visualization.control

import edu.uci.ics.jung.visualization.BasicVisualizationServer
import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.util.ArrowFactory
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.CubicCurve2D
import java.awt.geom.Point2D

open class CubicCurveEdgeEffects<N : Any, E : Any> : EdgeEffects<N, E> {

    protected var rawEdge: CubicCurve2D = CubicCurve2D.Float()
    protected var edgeShape: Shape? = null
    protected var rawArrowShape: Shape
    protected var arrowShape: Shape? = null
    protected var edgePaintable: VisualizationServer.Paintable
    protected var arrowPaintable: VisualizationServer.Paintable

    init {
        this.rawEdge.setCurve(0.0, 0.0, 0.33, 100.0, 0.66, -50.0, 1.0, 0.0)
        rawArrowShape = ArrowFactory.getNotchedArrow(20f, 16f, 8f)
        this.edgePaintable = EdgePaintable()
        this.arrowPaintable = ArrowPaintable()
    }

    override fun startEdgeEffects(vv: BasicVisualizationServer<N, E>, down: Point2D, out: Point2D) {
        transformEdgeShape(down, out)
        vv.addPostRenderPaintable(edgePaintable)
    }

    override fun midEdgeEffects(vv: BasicVisualizationServer<N, E>, down: Point2D, out: Point2D) {
        transformEdgeShape(down, out)
    }

    override fun endEdgeEffects(vv: BasicVisualizationServer<N, E>) {
        vv.removePostRenderPaintable(edgePaintable)
    }

    override fun startArrowEffects(vv: BasicVisualizationServer<N, E>, down: Point2D, out: Point2D) {
        transformArrowShape(down, out)
        vv.addPostRenderPaintable(arrowPaintable)
    }

    override fun midArrowEffects(vv: BasicVisualizationServer<N, E>, down: Point2D, out: Point2D) {
        transformArrowShape(down, out)
    }

    override fun endArrowEffects(vv: BasicVisualizationServer<N, E>) {
        vv.removePostRenderPaintable(arrowPaintable)
    }

    /** code lifted from PluggableRenderer to move an edge shape into an arbitrary position */
    private fun transformEdgeShape(down: Point2D, out: Point2D) {
        val x1 = down.x.toFloat()
        val y1 = down.y.toFloat()
        val x2 = out.x.toFloat()
        val y2 = out.y.toFloat()

        val xform = AffineTransform.getTranslateInstance(x1.toDouble(), y1.toDouble())

        val dx = x2 - x1
        val dy = y2 - y1
        val thetaRadians = Math.atan2(dy.toDouble(), dx.toDouble()).toFloat()
        xform.rotate(thetaRadians.toDouble())
        val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        xform.scale((dist / rawEdge.bounds.getWidth()), 1.0)
        edgeShape = xform.createTransformedShape(rawEdge)
    }

    private fun transformArrowShape(down: Point2D, out: Point2D) {
        val x1 = down.x.toFloat()
        val y1 = down.y.toFloat()
        val x2 = out.x.toFloat()
        val y2 = out.y.toFloat()

        val xform = AffineTransform.getTranslateInstance(x2.toDouble(), y2.toDouble())

        val dx = x2 - x1
        val dy = y2 - y1
        val thetaRadians = Math.atan2(dy.toDouble(), dx.toDouble()).toFloat()
        xform.rotate(thetaRadians.toDouble())
        arrowShape = xform.createTransformedShape(rawArrowShape)
    }

    /** Used for the edge creation visual effect during mouse drag */
    internal inner class EdgePaintable : VisualizationServer.Paintable {

        override fun paint(g: Graphics) {
            if (edgeShape != null) {
                val oldColor = g.color
                g.color = Color.black
                (g as Graphics2D).draw(edgeShape)
                g.color = oldColor
            }
        }

        override fun useTransform(): Boolean {
            return false
        }
    }

    /** Used for the directed edge creation visual effect during mouse drag */
    internal inner class ArrowPaintable : VisualizationServer.Paintable {

        override fun paint(g: Graphics) {
            if (arrowShape != null) {
                val oldColor = g.color
                g.color = Color.black
                (g as Graphics2D).fill(arrowShape)
                g.color = oldColor
            }
        }

        override fun useTransform(): Boolean {
            return false
        }
    }
}
