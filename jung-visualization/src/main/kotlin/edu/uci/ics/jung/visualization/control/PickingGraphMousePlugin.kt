/*
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * Created on Mar 8, 2005
 *
 */
package edu.uci.ics.jung.visualization.control

import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.visualization.MultiLayerTransformer
import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.picking.PickedState
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.Cursor
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.event.InputEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import javax.swing.JComponent

/**
 * PickingGraphMousePlugin supports the picking of graph elements with the mouse. MouseButtonOne
 * picks a single node or edge, and MouseButtonTwo adds to the set of selected Nodes or EdgeType. If
 * a Node is selected and the mouse is dragged while on the selected Node, then that Node will be
 * repositioned to follow the mouse until the button is released.
 *
 * @author Tom Nelson
 */
open class PickingGraphMousePlugin<N : Any, E : Any> @JvmOverloads constructor(
    selectionModifiers: Int = InputEvent.BUTTON1_MASK,
    /** additional modifiers for the action of adding to an existing selection */
    protected var addToSelectionModifiers: Int = InputEvent.BUTTON1_MASK or InputEvent.SHIFT_MASK
) : AbstractGraphMousePlugin(selectionModifiers), MouseListener, MouseMotionListener {

    /** the picked Node, if any */
    protected var node: N? = null

    /** the picked Edge, if any */
    protected var edge: E? = null

    /** controls whether the Nodes may be moved with the mouse */
    var isLocked: Boolean = false

    /** used to draw a rectangle to contain picked nodes */
    protected var viewRectangle: Rectangle2D = Rectangle2D.Float()

    // viewRectangle projected onto the layout coordinate system
    protected var layoutTargetShape: Shape = viewRectangle

    /** the Paintable for the lens picking rectangle */
    protected var lensPaintable: VisualizationServer.Paintable

    /** color for the picking rectangle */
    var lensColor: Color = Color.cyan

    protected var deltaDown: Point2D? = null

    init {
        this.lensPaintable = LensPaintable()
        this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    }

    /**
     * a Paintable to draw the rectangle used to pick multiple Nodes
     *
     * @author Tom Nelson
     */
    internal inner class LensPaintable : VisualizationServer.Paintable {

        override fun paint(g: Graphics) {
            val oldColor = g.color
            g.color = lensColor
            (g as Graphics2D).draw(viewRectangle)
            g.color = oldColor
        }

        override fun useTransform(): Boolean {
            return false
        }
    }

    /**
     * For primary modifiers (default, MouseButton1): pick a single Node or Edge that is under the
     * mouse pointer. If no Node or edge is under the pointer, unselect all picked Nodes and edges,
     * and set up to draw a rectangle for multiple selection of contained Nodes. For additional
     * selection (default Shift+MouseButton1): Add to the selection, a single Node or Edge that is
     * under the mouse pointer. If a previously picked Node or Edge is under the pointer, it is
     * un-picked. If no node or Edge is under the pointer, set up to draw a multiple selection
     * rectangle (as above) but do not unpick previously picked elements.
     *
     * @param e the event
     */
    @Suppress("UNCHECKED_CAST")
    override fun mousePressed(e: MouseEvent) {
        down = e.point
        log.trace("mouse pick at screen coords {}", e.point)
        deltaDown = down
        val vv = e.source as VisualizationViewer<N, E>
        val transformSupport = vv.getTransformSupport()
        val layoutModel = vv.getModel().getLayoutModel()
        val pickSupport = vv.getPickSupport()
        val pickedNodeState = vv.getPickedNodeState()
        val pickedEdgeState = vv.getPickedEdgeState()
        if (pickSupport != null && pickedNodeState != null) {
            val multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer()

            // subclass can override to account for view distortion effects
            updatePickingTargets(vv, multiLayerTransformer, down!!, down!!)

            // layoutPoint is the mouse event point projected on the layout coordinate system

            // subclass can override to account for view distortion effects
            val layoutPoint = transformSupport.inverseTransform(vv, down!!)
            log.trace("layout coords of mouse click {}", layoutPoint)
            if (e.modifiers == modifiers) {

                node = pickSupport.getNode(layoutModel, layoutPoint.x, layoutPoint.y)
                log.trace("mousePressed set the node to {}", node)
                val currentNode = node
                if (currentNode != null) {
                    // picked a node
                    if (!pickedNodeState.isPicked(currentNode)) {
                        pickedNodeState.clear()
                        pickedNodeState.pick(currentNode, true)
                    }

                } else if (pickSupport.getEdge(layoutModel, layoutPoint).also { edge = it } != null) {
                    // picked an edge
                    pickedEdgeState?.clear()
                    pickedEdgeState?.pick(edge!!, true)
                } else {
                    // prepare to draw a pick area and clear previous picks
                    vv.addPostRenderPaintable(lensPaintable)
                    pickedEdgeState?.clear()
                    pickedNodeState.clear()
                }

            } else if (e.modifiers == addToSelectionModifiers) {
                vv.addPostRenderPaintable(lensPaintable)

                node = pickSupport.getNode(layoutModel, layoutPoint.x, layoutPoint.y)
                log.trace("mousePressed with add set the node to {}", node)
                val currentNode2 = node
                if (currentNode2 != null) {
                    val wasThere = pickedNodeState.pick(currentNode2, !pickedNodeState.isPicked(currentNode2))
                    if (wasThere) {
                        log.trace("already, so now node will be null")
                        node = null
                    }
                } else if (pickSupport.getEdge(layoutModel, layoutPoint).also { edge = it } != null) {
                    val currentEdge = edge!!
                    pickedEdgeState?.pick(currentEdge, !pickedEdgeState!!.isPicked(currentEdge))
                }
            }
        }
        if (node != null) {
            e.consume()
        }
    }

    /**
     * If the mouse is dragging a rectangle, pick the Nodes contained in that rectangle
     *
     * clean up settings from mousePressed
     */
    @Suppress("UNCHECKED_CAST")
    override fun mouseReleased(e: MouseEvent) {
        val out = e.point

        val vv = e.source as VisualizationViewer<N, E>
        vv.getNodeSpatial().setActive(true)
        vv.getEdgeSpatial().setActive(true)
        val multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer()

        if (e.modifiers == modifiers) {
            if (down != null) {
                if (node == null && !heyThatsTooClose(down!!, out, 5.0)) {
                    pickContainedNodes(vv, layoutTargetShape, true)
                }
            }
        } else if (e.modifiers == this.addToSelectionModifiers) {
            if (down != null) {
                if (node == null && !heyThatsTooClose(down!!, out, 5.0)) {
                    pickContainedNodes(vv, layoutTargetShape, false)
                }
            }
        }
        log.trace("down:{} out:{}", down, out)
        if (node != null && down != out) {

            // dragging points and changing their layout locations
            val graphPoint = multiLayerTransformer.inverseTransform(out)
            log.trace("p in graph coords is {}", graphPoint)
            val graphDown = multiLayerTransformer.inverseTransform(deltaDown!!)
            log.trace("graphDown (down in graph coords) is {}", graphDown)
            val visualizationModel = vv.getModel()
            val layoutModel = visualizationModel.getLayoutModel()
            val dx = graphPoint.x - graphDown.x
            val dy = graphPoint.y - graphDown.y
            log.trace("dx, dy: {},{}", dx, dy)
            val ps: PickedState<N> = vv.getPickedNodeState()

            for (v in ps.getPicked()) {
                var vp = layoutModel.apply(v)
                vp = Point.of(vp.x + dx, vp.y + dy)
                layoutModel.set(v, vp)
            }
            deltaDown = out
        }

        down = null
        node = null
        edge = null
        viewRectangle.setFrame(0.0, 0.0, 0.0, 0.0)
        layoutTargetShape = multiLayerTransformer.inverseTransform(viewRectangle)
        vv.removePostRenderPaintable(lensPaintable)
        vv.repaint()
    }

    /**
     * If the mouse is over a picked node, drag all picked nodes with the mouse. If the mouse is not
     * over a Node, draw the rectangle to select multiple Nodes
     */
    @Suppress("UNCHECKED_CAST")
    override fun mouseDragged(e: MouseEvent) {
        log.trace("mouseDragged")
        val vv = e.source as VisualizationViewer<N, E>
        vv.getNodeSpatial().setActive(false)
        vv.getEdgeSpatial().setActive(false)
        if (!isLocked) {

            val multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer()
            val p = e.point
            log.trace("view p for drag event is {}", p)
            log.trace("down is {}", down)
            if (node != null) {
                // dragging points and changing their layout locations
                val graphPoint = multiLayerTransformer.inverseTransform(p)
                log.trace("p in graph coords is {}", graphPoint)
                val graphDown = multiLayerTransformer.inverseTransform(deltaDown!!)
                log.trace("graphDown (down in graph coords) is {}", graphDown)
                val visualizationModel = vv.getModel()
                val layoutModel = visualizationModel.getLayoutModel()
                val dx = graphPoint.x - graphDown.x
                val dy = graphPoint.y - graphDown.y
                log.trace("dx, dy: {},{}", dx, dy)
                val ps: PickedState<N> = vv.getPickedNodeState()

                for (v in ps.getPicked()) {
                    var vp = layoutModel.apply(v)
                    vp = Point.of(vp.x + dx, vp.y + dy)
                    layoutModel.set(v, vp)
                }
                deltaDown = p

            } else {
                val out = e.point
                if (e.modifiers == this.addToSelectionModifiers || e.modifiers == modifiers) {
                    updatePickingTargets(vv, multiLayerTransformer, down!!, out)
                }
            }
            if (node != null) {
                e.consume()
            }
            vv.repaint()
        }
    }

    /**
     * rejects picking if the rectangle is too small, like if the user meant to select one node but
     * moved the mouse slightly
     *
     * @param p
     * @param q
     * @param min
     * @return
     */
    private fun heyThatsTooClose(p: Point2D, q: Point2D, min: Double): Boolean {
        return Math.abs(p.x - q.x) < min && Math.abs(p.y - q.y) < min
    }

    /**
     * override to consider Lens effects
     *
     * @param vv
     * @param p
     * @return
     */
    protected open fun inverseTransform(vv: VisualizationViewer<N, E>, p: Point2D): Point2D {
        val multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer()
        return multiLayerTransformer.inverseTransform(p)
    }

    /**
     * override to consider Lens effects
     *
     * @param vv
     * @param shape
     * @return
     */
    protected open fun transform(vv: VisualizationViewer<N, E>, shape: Shape): Shape {
        val multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer()
        return multiLayerTransformer.transform(shape)
    }

    /**
     * override to consider Lens effects
     *
     * @param vv
     * @param multiLayerTransformer
     * @param down
     * @param out
     */
    protected open fun updatePickingTargets(
        vv: VisualizationViewer<*, *>,
        multiLayerTransformer: MultiLayerTransformer,
        down: Point2D,
        out: Point2D
    ) {
        log.trace("updatePickingTargets with {} to {}", down, out)
        viewRectangle.setFrameFromDiagonal(down, out)

        layoutTargetShape = multiLayerTransformer.inverseTransform(viewRectangle)

        if (log.isTraceEnabled) {
            log.trace("viewRectangle {}", viewRectangle)
            log.trace("layoutTargetShape bounds {}", layoutTargetShape.bounds)
        }
    }

    /**
     * pick the nodes inside the rectangle created from points 'down' and 'out' (two diagonally
     * opposed corners of the rectangle)
     *
     * @param vv the viewer containing the layout and picked state
     * @param pickTarget - the shape to pick nodes in (layout coordinate system)
     * @param clear whether to reset existing picked state
     */
    protected fun pickContainedNodes(vv: VisualizationViewer<N, E>, pickTarget: Shape, clear: Boolean) {
        val pickedNodeState = vv.getPickedNodeState()

        if (pickedNodeState != null) {
            if (clear) {
                pickedNodeState.clear()
            }
            val pickSupport = vv.getPickSupport()
            val layoutModel = vv.getModel().getLayoutModel()
            val picked = pickSupport.getNodes(layoutModel, pickTarget)
            for (v in picked) {
                pickedNodeState.pick(v, true)
            }
        }
    }

    override fun mouseClicked(e: MouseEvent) {}

    override fun mouseEntered(e: MouseEvent) {
        val c = e.source as JComponent
        c.cursor = cursor
    }

    override fun mouseExited(e: MouseEvent) {
        val c = e.source as JComponent
        c.cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
    }

    override fun mouseMoved(e: MouseEvent) {}

    companion object {
        private val log = LoggerFactory.getLogger(PickingGraphMousePlugin::class.java)
    }
}
