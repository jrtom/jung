package edu.uci.ics.jung.visualization

import edu.uci.ics.jung.visualization.transform.MutableAffineTransformer
import edu.uci.ics.jung.visualization.transform.MutableTransformer
import edu.uci.ics.jung.visualization.transform.shape.ShapeTransformer
import edu.uci.ics.jung.visualization.util.ChangeEventSupport
import edu.uci.ics.jung.visualization.util.DefaultChangeEventSupport
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import org.slf4j.LoggerFactory

/**
 * A basic implementation of the MultiLayerTransformer interface that provides two Layers: VIEW and
 * LAYOUT. It also provides ChangeEventSupport
 *
 * @author Tom Nelson
 */
open class BasicTransformer :
    MultiLayerTransformer, ShapeTransformer, ChangeListener, ChangeEventSupport {

    protected var changeSupport: ChangeEventSupport = DefaultChangeEventSupport(this)

    protected var _viewTransformer: MutableTransformer =
        MutableAffineTransformer(AffineTransform())

    protected var _layoutTransformer: MutableTransformer =
        MutableAffineTransformer(AffineTransform())

    /**
     * Creates an instance and notifies the view and layout Functions to listen to changes published
     * by this instance.
     */
    init {
        _viewTransformer.addChangeListener(this)
        _layoutTransformer.addChangeListener(this)
    }

    protected fun setViewTransformer(function: MutableTransformer) {
        this._viewTransformer.removeChangeListener(this)
        this._viewTransformer = function
        this._viewTransformer.addChangeListener(this)
    }

    protected fun setLayoutTransformer(function: MutableTransformer) {
        this._layoutTransformer.removeChangeListener(this)
        this._layoutTransformer = function
        this._layoutTransformer.addChangeListener(this)
    }

    protected fun getLayoutTransformer(): MutableTransformer = _layoutTransformer

    protected fun getViewTransformer(): MutableTransformer = _viewTransformer

    override fun inverseTransform(p: Point2D): Point2D =
        inverseLayoutTransform(inverseViewTransform(p))

    override fun inverseTransform(x: Double, y: Double): Point2D =
        inverseTransform(Point2D.Double(x, y))

    protected fun inverseViewTransform(p: Point2D): Point2D =
        _viewTransformer.inverseTransform(p)

    protected fun inverseLayoutTransform(p: Point2D): Point2D =
        _layoutTransformer.inverseTransform(p)

    override fun transform(p: Point2D): Point2D =
        viewTransform(layoutTransform(p))

    override fun transform(x: Double, y: Double): Point2D =
        transform(Point2D.Double(x, y))

    protected fun viewTransform(p: Point2D): Point2D =
        _viewTransformer.transform(p)!!

    protected fun layoutTransform(p: Point2D): Point2D =
        _layoutTransformer.transform(p)!!

    override fun inverseTransform(shape: Shape): Shape =
        inverseLayoutTransform(inverseViewTransform(shape))

    protected fun inverseViewTransform(shape: Shape): Shape =
        _viewTransformer.inverseTransform(shape)

    protected fun inverseLayoutTransform(shape: Shape): Shape =
        _layoutTransformer.inverseTransform(shape)

    override fun transform(shape: Shape): Shape =
        viewTransform(layoutTransform(shape))

    protected fun viewTransform(shape: Shape): Shape =
        _viewTransformer.transform(shape)

    protected fun layoutTransform(shape: Shape): Shape =
        _layoutTransformer.transform(shape)

    override fun setToIdentity() {
        _layoutTransformer.setToIdentity()
        _viewTransformer.setToIdentity()
    }

    override fun addChangeListener(l: ChangeListener) {
        changeSupport.addChangeListener(l)
    }

    override fun removeChangeListener(l: ChangeListener) {
        changeSupport.removeChangeListener(l)
    }

    override fun getChangeListeners(): Array<ChangeListener> =
        changeSupport.getChangeListeners()

    override fun fireStateChanged() {
        changeSupport.fireStateChanged()
    }

    override fun stateChanged(e: ChangeEvent) {
        fireStateChanged()
    }

    override fun getTransformer(layer: MultiLayerTransformer.Layer): MutableTransformer {
        return when (layer) {
            MultiLayerTransformer.Layer.LAYOUT -> _layoutTransformer
            MultiLayerTransformer.Layer.VIEW -> _viewTransformer
        }
    }

    override fun inverseTransform(layer: MultiLayerTransformer.Layer, p: Point2D): Point2D? {
        return when (layer) {
            MultiLayerTransformer.Layer.LAYOUT -> inverseLayoutTransform(p)
            MultiLayerTransformer.Layer.VIEW -> inverseViewTransform(p)
        }
    }

    override fun inverseTransform(layer: MultiLayerTransformer.Layer, x: Double, y: Double): Point2D? =
        inverseTransform(layer, Point2D.Double(x, y))

    override fun setTransformer(layer: MultiLayerTransformer.Layer, function: MutableTransformer) {
        when (layer) {
            MultiLayerTransformer.Layer.LAYOUT -> setLayoutTransformer(function)
            MultiLayerTransformer.Layer.VIEW -> setViewTransformer(function)
        }
    }

    override fun transform(layer: MultiLayerTransformer.Layer, p: Point2D): Point2D? {
        return when (layer) {
            MultiLayerTransformer.Layer.LAYOUT -> layoutTransform(p)
            MultiLayerTransformer.Layer.VIEW -> viewTransform(p)
        }
    }

    override fun transform(layer: MultiLayerTransformer.Layer, x: Double, y: Double): Point2D? =
        transform(layer, Point2D.Double(x, y))

    override fun transform(layer: MultiLayerTransformer.Layer, shape: Shape): Shape? {
        if (log.isTraceEnabled) {
            log.trace("transform {} {}", layer, shape)
        }
        return when (layer) {
            MultiLayerTransformer.Layer.LAYOUT -> layoutTransform(shape)
            MultiLayerTransformer.Layer.VIEW -> viewTransform(shape)
        }
    }

    override fun inverseTransform(layer: MultiLayerTransformer.Layer, shape: Shape): Shape? {
        if (log.isTraceEnabled) {
            log.trace("inverseTransform {} {}", layer, shape)
        }
        return when (layer) {
            MultiLayerTransformer.Layer.LAYOUT -> inverseLayoutTransform(shape)
            MultiLayerTransformer.Layer.VIEW -> inverseViewTransform(shape)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(BasicTransformer::class.java)
    }
}
