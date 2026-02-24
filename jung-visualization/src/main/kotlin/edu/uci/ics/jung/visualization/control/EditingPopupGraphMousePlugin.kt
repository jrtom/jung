package edu.uci.ics.jung.visualization.control

import com.google.common.graph.MutableNetwork
import edu.uci.ics.jung.visualization.VisualizationViewer
import java.awt.event.ActionEvent
import java.awt.event.MouseEvent
import java.util.function.Supplier
import javax.swing.AbstractAction
import javax.swing.JMenu
import javax.swing.JPopupMenu

/**
 * a plugin that uses popup menus to create nodes, undirected edges, and directed edges.
 *
 * @author Tom Nelson
 */
open class EditingPopupGraphMousePlugin<N, E>(
    protected var nodeFactory: Supplier<N>,
    protected var edgeFactory: Supplier<E>
) : AbstractPopupGraphMousePlugin() {

    @Suppress("UNCHECKED_CAST")
    override fun handlePopup(e: MouseEvent) {
        val vv = e.source as VisualizationViewer<N, E>
        val layoutModel = vv.getModel().getLayoutModel()

        val graph = vv.getModel().getNetwork() as MutableNetwork<N, E>
        val p = e.point
        val pickSupport = vv.getPickSupport()
        if (pickSupport != null) {

            val node = pickSupport.getNode(layoutModel, p.getX(), p.getY())
            val edge = pickSupport.getEdge(layoutModel, p.getX(), p.getY())
            val pickedNodeState = vv.getPickedNodeState()
            val pickedEdgeState = vv.getPickedEdgeState()

            val popup = JPopupMenu()
            if (node != null) {
                val picked: Set<N> = pickedNodeState.getPicked()
                if (picked.size > 0) {
                    val menu = JMenu("Create " + (if (graph.isDirected) "Directed" else "Undirected") + " Edge")
                    popup.add(menu)
                    for (other: N in picked) {
                        menu.add(object : AbstractAction("[$other,$node]") {
                            override fun actionPerformed(e: ActionEvent) {
                                graph.addEdge(other, node, edgeFactory.get())
                                vv.repaint()
                            }
                        })
                    }
                }
                popup.add(object : AbstractAction("Delete Node") {
                    override fun actionPerformed(e: ActionEvent) {
                        pickedNodeState.pick(node, false)
                        graph.removeNode(node)
                        vv.getNodeSpatial().recalculate()
                        vv.repaint()
                    }
                })
            } else if (edge != null) {
                popup.add(object : AbstractAction("Delete Edge") {
                    override fun actionPerformed(e: ActionEvent) {
                        pickedEdgeState.pick(edge, false)
                        graph.removeEdge(edge)
                        vv.getEdgeSpatial().recalculate()
                        vv.repaint()
                    }
                })
            } else {
                popup.add(object : AbstractAction("Create Node") {
                    override fun actionPerformed(e: ActionEvent) {
                        val newNode = nodeFactory.get()
                        graph.addNode(newNode)
                        val p2d = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(p)
                        vv.getModel().getLayoutModel().set(newNode, p2d.x, p2d.y)
                        vv.repaint()
                    }
                })
            }
            if (popup.componentCount > 0) {
                popup.show(vv, e.x, e.y)
            }
        }
    }
}
