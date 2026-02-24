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

import edu.uci.ics.jung.algorithms.util.MapSettableTransformer
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer
import edu.uci.ics.jung.visualization.VisualizationViewer
import java.awt.Cursor
import java.awt.event.InputEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JOptionPane

/**
 * @author Tom Nelson
 */
open class LabelEditingGraphMousePlugin<N, E> @JvmOverloads constructor(
    selectionModifiers: Int = InputEvent.BUTTON1_MASK
) : AbstractGraphMousePlugin(selectionModifiers), MouseListener {

    /** the picked Node, if any */
    protected var node: N? = null

    /** the picked Edge, if any */
    protected var edge: E? = null

    init {
        this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
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
    override fun mouseClicked(e: MouseEvent) {
        if (e.modifiers == modifiers && e.clickCount == 2) {
            val vv = e.source as VisualizationViewer<N, E>
            val layoutModel = vv.getModel().getLayoutModel()
            val pickSupport = vv.getPickSupport()
            if (pickSupport != null) {
                val vs = vv.getRenderContext().getNodeLabelFunction()
                if (vs is MapSettableTransformer<*, *>) {
                    val mst = vs as MapSettableTransformer<N, String>
                    val p = e.point

                    val node = pickSupport.getNode(layoutModel, p.getX(), p.getY())
                    if (node != null) {
                        var newLabel = vs.apply(node)
                        newLabel = JOptionPane.showInputDialog("New Node Label for $node")
                        if (newLabel != null) {
                            mst.set(node, newLabel)
                            vv.repaint()
                        }
                        return
                    }
                }
                val es = vv.getRenderContext().getEdgeLabelFunction()
                if (es is MapSettableTransformer<*, *>) {
                    val mst = es as MapSettableTransformer<E, String>
                    val p = e.point
                    // take away the view transform
                    val ip = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, p)!!
                    val edge = pickSupport.getEdge(layoutModel, ip.x, ip.y)
                    if (edge != null) {
                        val newLabel = JOptionPane.showInputDialog("New Edge Label for $edge")
                        if (newLabel != null) {
                            mst.set(edge, newLabel)
                            vv.repaint()
                        }
                        return
                    }
                }
            }
            e.consume()
        }
    }

    /**
     * If the mouse is dragging a rectangle, pick the Nodes contained in that rectangle
     *
     * clean up settings from mousePressed
     */
    override fun mouseReleased(e: MouseEvent) {}

    /**
     * If the mouse is over a picked node, drag all picked nodes with the mouse. If the mouse is not
     * over a Node, draw the rectangle to select multiple Nodes
     */
    override fun mousePressed(e: MouseEvent) {}

    override fun mouseEntered(e: MouseEvent) {}

    override fun mouseExited(e: MouseEvent) {}
}
