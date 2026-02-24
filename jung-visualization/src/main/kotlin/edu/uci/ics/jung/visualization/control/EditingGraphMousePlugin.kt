package edu.uci.ics.jung.visualization.control

import edu.uci.ics.jung.visualization.VisualizationViewer
import java.awt.Cursor
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.util.function.Supplier
import javax.swing.JComponent

/**
 * A plugin that can create nodes, undirected edges, and directed edges using mouse gestures.
 *
 * nodeSupport and edgeSupport member classes are responsible for actually creating the new graph
 * elements, and for repainting the view when changes were made.
 *
 * @author Tom Nelson
 */
open class EditingGraphMousePlugin<N : Any, E : Any> @JvmOverloads constructor(
    modifiers: Int = MouseEvent.BUTTON1_MASK,
    nodeFactory: Supplier<N>,
    edgeFactory: Supplier<E>
) : AbstractGraphMousePlugin(modifiers), MouseListener, MouseMotionListener {

    var nodeSupport: NodeSupport<N, E>
    var edgeSupport: EdgeSupport<N, E>
    private var createMode = Creating.UNDETERMINED

    private enum class Creating {
        EDGE,
        NODE,
        UNDETERMINED
    }

    init {
        this.cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)
        this.nodeSupport = SimpleNodeSupport(nodeFactory)
        this.edgeSupport = SimpleEdgeSupport(edgeFactory)
    }

    /**
     * Overridden to be more flexible, and pass events with key combinations. The default responds to
     * both ButtonOne and ButtonOne+Shift
     */
    override fun checkModifiers(e: MouseEvent): Boolean {
        return (e.modifiers and modifiers) != 0
    }

    /**
     * If the mouse is pressed in an empty area, create a new node there. If the mouse is pressed on
     * an existing node, prepare to create an edge from that node to another
     */
    @Suppress("UNCHECKED_CAST")
    override fun mousePressed(e: MouseEvent) {
        if (checkModifiers(e)) {
            val vv = e.source as VisualizationViewer<N, E>
            val layoutModel = vv.getModel().getLayoutModel()
            val p = e.point
            val pickSupport = vv.getPickSupport()
            if (pickSupport != null) {
                val node = pickSupport.getNode(layoutModel, p.getX(), p.getY())
                if (node != null) { // get ready to make an edge
                    this.createMode = Creating.EDGE
                    edgeSupport.startEdgeCreate(vv, node, e.point)
                } else { // make a new node
                    this.createMode = Creating.NODE
                    nodeSupport.startNodeCreate(vv, e.point)
                }
            }
        }
    }

    /**
     * If startNode is non-null, and the mouse is released over an existing node, create an edge from
     * startNode to the node under the mouse pointer.
     */
    @Suppress("UNCHECKED_CAST")
    override fun mouseReleased(e: MouseEvent) {
        if (checkModifiers(e)) {
            val vv = e.source as VisualizationViewer<N, E>
            val layoutModel = vv.getModel().getLayoutModel()
            val p = e.point
            if (createMode == Creating.EDGE) {
                val pickSupport = vv.getPickSupport()
                var node: N? = null
                if (pickSupport != null) {
                    node = pickSupport.getNode(layoutModel, p.getX(), p.getY())
                }
                if (node != null) {
                    edgeSupport.endEdgeCreate(vv, node)
                    vv.getEdgeSpatial().recalculate()
                } else {
                    edgeSupport.abort(vv)
                }
            } else if (createMode == Creating.NODE) {
                nodeSupport.endNodeCreate(vv, e.point)
                vv.getNodeSpatial().recalculate()
            }
        }
        createMode = Creating.UNDETERMINED
    }

    /**
     * If startNode is non-null, stretch an edge shape between startNode and the mouse pointer to
     * simulate edge creation
     */
    @Suppress("UNCHECKED_CAST")
    override fun mouseDragged(e: MouseEvent) {
        if (checkModifiers(e)) {
            val vv = e.source as VisualizationViewer<N, E>
            if (createMode == Creating.EDGE) {
                edgeSupport.midEdgeCreate(vv, e.point)
            } else if (createMode == Creating.NODE) {
                nodeSupport.midNodeCreate(vv, e.point)
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

    fun edgesupport(): EdgeSupport<N, E> {
        return edgeSupport
    }
}
