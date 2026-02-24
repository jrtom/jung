package edu.uci.ics.jung.visualization

import edu.uci.ics.jung.visualization.transform.BidirectionalTransformer
import edu.uci.ics.jung.visualization.transform.MutableTransformer
import edu.uci.ics.jung.visualization.transform.shape.ShapeTransformer
import edu.uci.ics.jung.visualization.util.ChangeEventSupport
import java.awt.Shape
import java.awt.geom.Point2D

interface MultiLayerTransformer : BidirectionalTransformer, ShapeTransformer, ChangeEventSupport {

    enum class Layer {
        LAYOUT,
        VIEW
    }

    fun setTransformer(layer: Layer, function: MutableTransformer)

    fun getTransformer(layer: Layer): MutableTransformer

    fun inverseTransform(layer: Layer, p: Point2D): Point2D?

    fun inverseTransform(layer: Layer, x: Double, y: Double): Point2D?

    fun transform(layer: Layer, p: Point2D): Point2D?

    fun transform(layer: Layer, x: Double, y: Double): Point2D?

    fun transform(layer: Layer, shape: Shape): Shape?

    fun inverseTransform(layer: Layer, shape: Shape): Shape?

    fun setToIdentity()
}
