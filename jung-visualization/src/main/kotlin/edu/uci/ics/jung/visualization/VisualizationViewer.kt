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

import com.google.common.graph.Network
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.visualization.control.GraphMouseListener
import edu.uci.ics.jung.visualization.control.MouseListenerTranslator
import java.awt.Dimension
import java.awt.event.KeyListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.event.MouseWheelListener
import java.awt.geom.Point2D
import java.util.function.Function
import javax.swing.ToolTipManager

/**
 * Adds mouse behaviors and tooltips to the graph visualization base class
 *
 * @author Joshua O'Madadhain
 * @author Tom Nelson
 * @author Danyel Fisher
 */
@Suppress("serial")
open class VisualizationViewer<N : Any, E : Any> : BasicVisualizationServer<N, E> {

    private var _nodeToolTipFunction: Function<in N, String>? = null
    private var _edgeToolTipFunction: Function<in E, String>? = null
    private var _mouseEventToolTipFunction: Function<MouseEvent, String>? = null

    /** provides MouseListener, MouseMotionListener, and MouseWheelListener events to the graph */
    private var _graphMouse: GraphMouse? = null

    protected val requestFocusListener: MouseListener = object : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            requestFocusInWindow()
        }
    }

    /**
     * @param network the network to render
     * @param size the size for the layout and for the view
     */
    constructor(network: Network<N, E>, size: Dimension) :
        this(network, size, size)

    /**
     * @param network the network to visualize
     * @param layoutSize the size of the layout area
     * @param viewSize the size of the view area
     */
    constructor(network: Network<N, E>, layoutSize: Dimension, viewSize: Dimension) :
        this(network, null, layoutSize, viewSize)

    /**
     * @param network the network to visualize
     * @param layoutAlgorithm the algorithm to apply
     * @param layoutSize the size for the layout area
     * @param viewSize the size of the window to display the network
     */
    constructor(
        network: Network<N, E>,
        layoutAlgorithm: LayoutAlgorithm<N>?,
        layoutSize: Dimension,
        viewSize: Dimension
    ) : this(BaseVisualizationModel(network, layoutAlgorithm, layoutSize), viewSize)

    /**
     * @param network the network to render
     * @param layoutAlgorithm the algorithm to apply
     * @param preferredSize the size to use for both the layout and the screen display
     */
    constructor(
        network: Network<N, E>,
        layoutAlgorithm: LayoutAlgorithm<N>,
        preferredSize: Dimension
    ) : this(BaseVisualizationModel(network, layoutAlgorithm, preferredSize), preferredSize)

    /**
     * @param model the model for the view
     * @param preferredSize the initial size of the window to display the network
     */
    constructor(model: VisualizationModel<N, E>, preferredSize: Dimension) : super(model, preferredSize) {
        isFocusable = true
        addMouseListener(requestFocusListener)
    }

    /**
     * a setter for the GraphMouse. This will remove any previous GraphMouse (including the one that
     * is added in the initMouseClicker method.
     *
     * @param graphMouse new value
     */
    fun setGraphMouse(graphMouse: GraphMouse) {
        this._graphMouse = graphMouse
        val ml = mouseListeners
        for (i in ml.indices) {
            if (ml[i] is GraphMouse) {
                removeMouseListener(ml[i])
            }
        }
        val mml = mouseMotionListeners
        for (i in mml.indices) {
            if (mml[i] is GraphMouse) {
                removeMouseMotionListener(mml[i])
            }
        }
        val mwl = mouseWheelListeners
        for (i in mwl.indices) {
            if (mwl[i] is GraphMouse) {
                removeMouseWheelListener(mwl[i])
            }
        }
        addMouseListener(graphMouse)
        addMouseMotionListener(graphMouse)
        addMouseWheelListener(graphMouse)
    }

    /**
     * @return the current `GraphMouse`
     */
    fun getGraphMouse(): GraphMouse? = _graphMouse

    /**
     * This is the interface for adding a mouse listener. The GEL will be called back with mouse
     * clicks on nodes.
     *
     * @param gel the mouse listener to add
     */
    fun addGraphMouseListener(gel: GraphMouseListener<N>) {
        addMouseListener(MouseListenerTranslator(gel, this))
    }

    /**
     * Override to request focus on mouse enter, if a key listener is added
     *
     * @see java.awt.Component.addKeyListener
     */
    @Synchronized
    override fun addKeyListener(l: KeyListener) {
        super.addKeyListener(l)
    }

    /**
     * @param edgeToolTipFunction the edgeToolTipFunction to set
     */
    fun setEdgeToolTipFunction(edgeToolTipFunction: Function<in E, String>) {
        this._edgeToolTipFunction = edgeToolTipFunction
        ToolTipManager.sharedInstance().registerComponent(this)
    }

    /**
     * @param mouseEventToolTipFunction the mouseEventToolTipFunction to set
     */
    fun setMouseEventToolTipFunction(mouseEventToolTipFunction: Function<MouseEvent, String>) {
        this._mouseEventToolTipFunction = mouseEventToolTipFunction
        ToolTipManager.sharedInstance().registerComponent(this)
    }

    /**
     * @param nodeToolTipFunction the nodeToolTipFunction to set
     */
    fun setNodeToolTipFunction(nodeToolTipFunction: Function<in N, String>) {
        this._nodeToolTipFunction = nodeToolTipFunction
        ToolTipManager.sharedInstance().registerComponent(this)
    }

    /** called by the superclass to display tooltips */
    override fun getToolTipText(event: MouseEvent): String? {
        val layoutModel = getModel().getLayoutModel()
        var p: Point2D? = null
        if (_nodeToolTipFunction != null) {
            p = getTransformSupport().inverseTransform(this, event.point)
            val node = getPickSupport().getNode(layoutModel, p.x, p.y)
            if (node != null) {
                return _nodeToolTipFunction!!.apply(node)
            }
        }
        if (_edgeToolTipFunction != null) {
            if (p == null) {
                p = getRenderContext().getMultiLayerTransformer()
                    .inverseTransform(MultiLayerTransformer.Layer.VIEW, event.point)
            }
            val edge = getPickSupport().getEdge(layoutModel, p!!.x, p.y)
            if (edge != null) {
                return _edgeToolTipFunction!!.apply(edge)
            }
        }
        if (_mouseEventToolTipFunction != null) {
            return _mouseEventToolTipFunction!!.apply(event)
        }
        return super.getToolTipText(event)
    }

    /**
     * a convenience type to represent a class that processes all types of mouse events for the graph
     */
    interface GraphMouse : MouseListener, MouseMotionListener, MouseWheelListener
}
