/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization

import com.google.common.collect.Lists
import com.google.common.graph.Network
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.util.Caching
import edu.uci.ics.jung.layout.util.LayoutChangeListener
import edu.uci.ics.jung.layout.util.LayoutEvent
import edu.uci.ics.jung.layout.util.LayoutEventSupport
import edu.uci.ics.jung.layout.util.LayoutNetworkEvent
import edu.uci.ics.jung.visualization.control.ScalingControl
import edu.uci.ics.jung.visualization.control.TransformSupport
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction
import edu.uci.ics.jung.visualization.layout.BoundingRectangleCollector
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor
import edu.uci.ics.jung.visualization.picking.MultiPickedState
import edu.uci.ics.jung.visualization.picking.PickedState
import edu.uci.ics.jung.visualization.picking.ShapePickSupport
import edu.uci.ics.jung.visualization.properties.VisualizationViewerUI
import edu.uci.ics.jung.visualization.renderers.BasicRenderer
import edu.uci.ics.jung.visualization.renderers.Renderer
import edu.uci.ics.jung.visualization.spatial.Spatial
import edu.uci.ics.jung.visualization.spatial.SpatialRTree
import edu.uci.ics.jung.visualization.spatial.rtree.QuadraticLeafSplitter
import edu.uci.ics.jung.visualization.spatial.rtree.QuadraticSplitter
import edu.uci.ics.jung.visualization.spatial.rtree.RStarLeafSplitter
import edu.uci.ics.jung.visualization.spatial.rtree.RStarSplitter
import edu.uci.ics.jung.visualization.spatial.rtree.SplitterContext
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator
import edu.uci.ics.jung.visualization.util.ChangeEventSupport
import edu.uci.ics.jung.visualization.util.DefaultChangeEventSupport
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.RenderingHints.Key
import java.awt.Shape
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.ItemListener
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.io.IOException
import javax.swing.JPanel
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import org.slf4j.LoggerFactory

/**
 * A class that maintains many of the details necessary for creating visualizations of graphs. This
 * is the old VisualizationViewer without tooltips and mouse behaviors. Its purpose is to be a base
 * class that can also be used on the server side of a multi-tiered application.
 *
 * @author Joshua O'Madadhain
 * @author Tom Nelson
 * @author Danyel Fisher
 */
@Suppress("serial")
open class BasicVisualizationServer<N : Any, E : Any> :
    JPanel,
    ChangeListener,
    ChangeEventSupport,
    VisualizationServer<N, E>,
    LayoutChangeListener<N> {

    protected var changeSupport: ChangeEventSupport = DefaultChangeEventSupport(this)

    /** holds the state of this View */
    private var _model: VisualizationModel<N, E>

    /** handles the actual drawing of graph elements */
    private var _renderer: Renderer<N, E>

    /** rendering hints used in drawing. Anti-aliasing is on by default */
    private var _renderingHints: MutableMap<Key, Any> = HashMap()

    /** holds the state of which nodes of the graph are currently 'picked' */
    private var _pickedNodeState: PickedState<N>? = null

    /** holds the state of which edges of the graph are currently 'picked' */
    private var _pickedEdgeState: PickedState<E>? = null

    /**
     * a listener used to cause pick events to result in repaints, even if they come from another view
     */
    protected var pickEventListener: ItemListener? = null

    /** an offscreen image to render the graph Used if doubleBuffered is set to true */
    protected var offscreen: BufferedImage? = null

    /** graphics context for the offscreen image Used if doubleBuffered is set to true */
    protected var offscreenG2d: Graphics2D? = null

    /** user-settable choice to use the offscreen image or not. 'false' by default */
    private var _doubleBuffered: Boolean = false

    /**
     * a collection of user-implementable functions to render under the topology (before the graph is
     * rendered)
     */
    protected var preRenderers: MutableList<VisualizationServer.Paintable> = ArrayList()

    /**
     * a collection of user-implementable functions to render over the topology (after the graph is
     * rendered)
     */
    protected var postRenderers: MutableList<VisualizationServer.Paintable> = ArrayList()

    private var _renderContext: RenderContext<N, E>

    private var _transformSupport: TransformSupport<N, E> = TransformSupport()

    private var _nodeSpatial: Spatial<N>? = null

    private var _edgeSpatial: Spatial<E>? = null

    /**
     * @param network the network to render
     * @param layoutAlgorithm the algorithm to apply
     * @param preferredSize the size of the graph area
     */
    constructor(
        network: Network<N, E>, layoutAlgorithm: LayoutAlgorithm<N>, preferredSize: Dimension
    ) : this(BaseVisualizationModel(network, layoutAlgorithm, preferredSize), preferredSize)

    /**
     * Create an instance with the specified model and view dimension.
     *
     * @param model the model to use
     * @param preferredSize initial preferred layoutSize of the view
     */
    constructor(model: VisualizationModel<N, E>, preferredSize: Dimension) : super() {
        this._model = model
        _renderContext = PluggableRenderContext(model.getNetwork())
        _renderer = BasicRenderer()
        createSpatialStructures(model, _renderContext)
        _model.addChangeListener(this)
        _model.addLayoutChangeListener(this)
        setDoubleBuffered(false)
        this.addComponentListener(VisualizationListener(this))

        setPickSupport(ShapePickSupport(this))
        setPickedNodeState(MultiPickedState())
        setPickedEdgeState(MultiPickedState())

        _renderContext.setEdgeDrawPaintFunction(
            PickableEdgePaintFunction(getPickedEdgeState(), Color.black, Color.cyan)
        )
        _renderContext.setNodeFillPaintFunction(
            PickableNodePaintFunction(getPickedNodeState(), Color.red, Color.yellow)
        )

        this.preferredSize = preferredSize
        _renderingHints[RenderingHints.KEY_ANTIALIASING] = RenderingHints.VALUE_ANTIALIAS_ON

        _renderContext.getMultiLayerTransformer().addChangeListener(this)
        try {
            VisualizationViewerUI.getInstance(this).parse()
        } catch (e: IOException) {
            log.debug("Unable to read property files. Using defaults.")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun createSpatialStructures(model: VisualizationModel<*, *>, renderContext: RenderContext<*, *>) {
        setNodeSpatial(
            SpatialRTree.Nodes(
                model as VisualizationModel<N, *>,
                BoundingRectangleCollector.Nodes(renderContext, model),
                SplitterContext.of(RStarLeafSplitter(), RStarSplitter())
            )
        )
        setEdgeSpatial(
            SpatialRTree.Edges(
                model as VisualizationModel<N, E>,
                BoundingRectangleCollector.Edges<E>(renderContext, model),
                SplitterContext.of(QuadraticLeafSplitter<E>(), QuadraticSplitter<E>())
            ) as Spatial<E>
        )
    }

    override fun getNodeSpatial(): Spatial<N> = _nodeSpatial!!

    override fun setNodeSpatial(spatial: Spatial<N>) {
        if (this._nodeSpatial != null) {
            disconnectListeners(this._nodeSpatial!!)
        }
        this._nodeSpatial = spatial

        val layoutModelRelaxing = _model.getLayoutModel().isRelaxing
        spatial.setActive(!layoutModelRelaxing)
        if (!layoutModelRelaxing) {
            spatial.recalculate()
        }
        connectListeners(spatial)
    }

    override fun getEdgeSpatial(): Spatial<E> = _edgeSpatial!!

    override fun setEdgeSpatial(spatial: Spatial<E>) {
        if (this._edgeSpatial != null) {
            disconnectListeners(this._edgeSpatial!!)
        }
        this._edgeSpatial = spatial

        val layoutModelRelaxing = _model.getLayoutModel().isRelaxing
        spatial.setActive(!layoutModelRelaxing)
        if (!layoutModelRelaxing) {
            spatial.recalculate()
        }
        connectListeners(spatial)
    }

    /**
     * hook up events so that when the VisualizationModel gets an event from the LayoutModel and fires
     * it, the Spatial will get the same event and know to update or recalculate its space
     */
    @Suppress("UNCHECKED_CAST")
    private fun connectListeners(spatial: Spatial<*>) {
        if (_model is LayoutEventSupport<*> && spatial is LayoutChangeListener<*>) {
            _model.addLayoutChangeListener(spatial as LayoutChangeListener<N>)
        }
        // this one toggles active/inactive as the opposite of the LayoutModel's active/inactive state
        _model.getLayoutModel().layoutStateChangeSupport.addLayoutStateChangeListener(spatial)
    }

    /**
     * disconnect listeners that will no longer be used
     */
    @Suppress("UNCHECKED_CAST")
    private fun disconnectListeners(spatial: Spatial<*>) {
        if (_model is LayoutEventSupport<*>) {
            if (spatial is LayoutChangeListener<*>) {
                _model.removeLayoutChangeListener(spatial as LayoutChangeListener<N>)
            }
        }
        if (_model.getLayoutModel() is LayoutEventSupport<*>) {
            (_model.getLayoutModel() as LayoutEventSupport<N>)
                .removeLayoutChangeListener(spatial as LayoutChangeListener<N>)
        }
        if (_model.getLayoutModel() is LayoutModel.ChangeSupport) {
            if (spatial is LayoutModel.ChangeListener) {
                (_model.getLayoutModel() as LayoutModel.ChangeSupport)
                    .removeChangeListener(spatial as LayoutModel.ChangeListener)
            }
        }
        _model.getLayoutModel().layoutStateChangeSupport.removeLayoutStateChangeListener(spatial)
    }

    override fun setDoubleBuffered(doubleBuffered: Boolean) {
        this._doubleBuffered = doubleBuffered
    }

    override fun isDoubleBuffered(): Boolean = _doubleBuffered

    /**
     * Always sanity-check getLayoutSize so that we don't use a value that is improbable
     *
     * @see java.awt.Component.getSize
     */
    override fun getSize(): Dimension {
        var d = super.getSize()
        if (d.width <= 0 || d.height <= 0) {
            d = preferredSize
        }
        return d
    }

    /**
     * Ensure that, if doubleBuffering is enabled, the offscreen image buffer exists and is the
     * correct layoutSize.
     *
     * @param d the expected Dimension of the offscreen buffer
     */
    protected fun checkOffscreenImage(d: Dimension) {
        if (_doubleBuffered) {
            if (offscreen == null || offscreen!!.width != d.width || offscreen!!.height != d.height) {
                offscreen = BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB)
                offscreenG2d = offscreen!!.createGraphics()
            }
        }
    }

    override fun getModel(): VisualizationModel<N, E> = _model

    override fun setModel(model: VisualizationModel<N, E>) {
        this._model = model
    }

    override fun stateChanged(e: ChangeEvent) {
        repaint()
        fireStateChanged()
    }

    override fun setRenderer(r: Renderer<N, E>) {
        this._renderer = r
        repaint()
    }

    override fun getRenderer(): Renderer<N, E> = _renderer

    fun scaleToLayout(scaler: ScalingControl) {
        var vd = preferredSize
        if (this.isShowing) {
            vd = size
        }
        val ld = _model.getLayoutSize()
        if (vd != ld) {
            scaler.scale(this, (vd.getWidth() / ld.getWidth()).toFloat(), Point2D.Double())
        }
    }

    override fun getRenderingHints(): Map<Key, Any> = _renderingHints

    override fun setRenderingHints(renderingHints: Map<Key, Any>) {
        this._renderingHints = HashMap(renderingHints)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val g2d = g as Graphics2D
        if (_doubleBuffered) {
            checkOffscreenImage(size)
            renderGraph(offscreenG2d!!)
            g2d.drawImage(offscreen, null, 0, 0)
        } else {
            renderGraph(g2d)
        }
    }

    override fun viewOnLayout(): Shape {
        val d = this.size
        val vt = _renderContext.getMultiLayerTransformer()
        val s: Shape = Rectangle2D.Double(0.0, 0.0, d.width.toDouble(), d.height.toDouble())
        return vt.inverseTransform(s)
    }

    protected open fun renderGraph(g2d: Graphics2D) {
        if (_renderContext.getGraphicsContext() == null) {
            _renderContext.setGraphicsContext(GraphicsDecorator(g2d))
        } else {
            _renderContext.getGraphicsContext()!!.setDelegate(g2d)
        }
        _renderContext.setScreenDevice(this)

        g2d.setRenderingHints(_renderingHints)

        // the layoutSize of the VisualizationViewer
        val d = size

        // clear the offscreen image
        g2d.color = background
        g2d.fillRect(0, 0, d.width, d.height)

        val oldXform = g2d.transform
        val newXform = AffineTransform(oldXform)
        newXform.concatenate(
            _renderContext.getMultiLayerTransformer()
                .getTransformer(MultiLayerTransformer.Layer.VIEW).getTransform()
        )

        g2d.transform = newXform

        if (log.isTraceEnabled) {
            // when logging is set to trace, the grid will be drawn on the graph visualization
            addSpatialAnnotations(this._nodeSpatial, Color.blue)
            addSpatialAnnotations(this._edgeSpatial, Color.green)
        } else {
            removeSpatialAnnotations()
        }

        // if there are preRenderers set, paint them
        for (paintable in preRenderers) {
            if (paintable.useTransform()) {
                paintable.paint(g2d)
            } else {
                g2d.transform = oldXform
                paintable.paint(g2d)
                g2d.transform = newXform
            }
        }

        if (_model is Caching) {
            (_model as Caching).clear()
        }

        _renderer.render(_renderContext, _model, _nodeSpatial!!, _edgeSpatial!!)

        // if there are postRenderers set, do it
        for (paintable in postRenderers) {
            if (paintable.useTransform()) {
                paintable.paint(g2d)
            } else {
                g2d.transform = oldXform
                paintable.paint(g2d)
                g2d.transform = newXform
            }
        }
        g2d.transform = oldXform
    }

    override fun layoutChanged(evt: LayoutEvent<N>) {
        repaint()
    }

    override fun layoutChanged(evt: LayoutNetworkEvent<N>) {
        repaint()
    }

    /**
     * VisualizationListener reacts to changes in the layoutSize of the VisualizationViewer. When the
     * layoutSize changes, it ensures that the offscreen image is sized properly. If the layout is
     * locked to this view layoutSize, then the layout is also resized to be the same as the view
     * layoutSize.
     */
    protected inner class VisualizationListener(
        private val vv: BasicVisualizationServer<N, E>
    ) : ComponentAdapter() {
        /** create a new offscreen image for the graph whenever the window is resized */
        override fun componentResized(e: ComponentEvent) {
            val d = vv.size
            if (d.width <= 0 || d.height <= 0) {
                return
            }
            checkOffscreenImage(d)
            repaint()
        }
    }

    override fun addPreRenderPaintable(paintable: VisualizationServer.Paintable) {
        preRenderers.add(paintable)
    }

    fun prependPreRenderPaintable(paintable: VisualizationServer.Paintable) {
        preRenderers.add(0, paintable)
    }

    override fun removePreRenderPaintable(paintable: VisualizationServer.Paintable) {
        preRenderers.remove(paintable)
    }

    override fun addPostRenderPaintable(paintable: VisualizationServer.Paintable) {
        postRenderers.add(paintable)
    }

    fun prependPostRenderPaintable(paintable: VisualizationServer.Paintable) {
        postRenderers.add(0, paintable)
    }

    override fun removePostRenderPaintable(paintable: VisualizationServer.Paintable) {
        postRenderers.remove(paintable)
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

    override fun getPickedNodeState(): PickedState<N> = _pickedNodeState!!

    override fun getPickedEdgeState(): PickedState<E> = _pickedEdgeState!!

    override fun setPickedNodeState(pickedNodeState: PickedState<N>) {
        if (pickEventListener != null && this._pickedNodeState != null) {
            this._pickedNodeState!!.removeItemListener(pickEventListener)
        }
        this._pickedNodeState = pickedNodeState
        this._renderContext.setPickedNodeState(pickedNodeState)
        if (pickEventListener == null) {
            pickEventListener = ItemListener { repaint() }
        }
        pickedNodeState.addItemListener(pickEventListener)
    }

    override fun setPickedEdgeState(pickedEdgeState: PickedState<E>) {
        if (pickEventListener != null && this._pickedEdgeState != null) {
            this._pickedEdgeState!!.removeItemListener(pickEventListener)
        }
        this._pickedEdgeState = pickedEdgeState
        this._renderContext.setPickedEdgeState(pickedEdgeState)
        if (pickEventListener == null) {
            pickEventListener = ItemListener { repaint() }
        }
        pickedEdgeState.addItemListener(pickEventListener)
    }

    override fun getPickSupport(): NetworkElementAccessor<N, E> =
        _renderContext.getPickSupport()

    override fun setPickSupport(pickSupport: NetworkElementAccessor<N, E>) {
        _renderContext.setPickSupport(pickSupport)
    }

    override fun getCenter(): Point2D {
        val d = size
        return Point2D.Double((d.width / 2).toDouble(), (d.height / 2).toDouble())
    }

    override fun getRenderContext(): RenderContext<N, E> = _renderContext

    override fun setRenderContext(renderContext: RenderContext<N, E>) {
        this._renderContext = renderContext
    }

    private fun addSpatialAnnotations(spatial: Spatial<*>?, color: Color) {
        if (spatial != null) {
            addPreRenderPaintable(SpatialPaintable(spatial, color))
        }
    }

    private fun removeSpatialAnnotations() {
        val iterator = preRenderers.iterator()
        while (iterator.hasNext()) {
            val paintable = iterator.next()
            if (paintable is BasicVisualizationServer<*, *>.SpatialPaintable<*>) {
                iterator.remove()
            }
        }
    }

    override fun getTransformSupport(): TransformSupport<N, E> = _transformSupport

    fun setTransformSupport(transformSupport: TransformSupport<N, E>) {
        this._transformSupport = transformSupport
    }

    internal inner class SpatialPaintable<T>(
        private val quadTree: Spatial<T>,
        private val color: Color
    ) : VisualizationServer.Paintable {

        override fun useTransform(): Boolean = false

        override fun paint(g: Graphics) {
            val g2d = g as Graphics2D
            val oldColor = g2d.color
            // gather all the grid shapes
            val grid: List<Shape> = quadTree.getGrid()

            g2d.color = color
            for (r: Shape in grid) {
                val shape = _transformSupport.transform(this@BasicVisualizationServer, r)
                g2d.draw(shape)
            }
            g2d.color = Color.red

            for (pickShape: Shape in quadTree.getPickShapes()) {
                val shape = _transformSupport.transform(this@BasicVisualizationServer, pickShape)
                g2d.draw(shape)
            }
            g2d.color = oldColor
        }
    }

    companion object {
        @JvmStatic
        val log = LoggerFactory.getLogger(BasicVisualizationServer::class.java)
    }
}
