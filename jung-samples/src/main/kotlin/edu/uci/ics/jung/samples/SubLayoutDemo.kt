/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.samples

import com.google.common.graph.MutableNetwork
import com.google.common.graph.Network
import com.google.common.graph.NetworkBuilder
import edu.uci.ics.jung.graph.util.TestGraphs
import edu.uci.ics.jung.layout.algorithms.CircleLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.KKLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.SpringLayoutAlgorithm
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.layout.util.RandomLocationTransformer
import edu.uci.ics.jung.samples.util.ControlHelpers
import edu.uci.ics.jung.visualization.BaseVisualizationModel
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.ModalGraphMouse
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction
import edu.uci.ics.jung.visualization.layout.AggregateLayoutModel
import edu.uci.ics.jung.visualization.picking.PickedState
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.ItemEvent
import java.awt.geom.Point2D
import java.lang.reflect.Constructor
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.DefaultListCellRenderer
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JList
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.WindowConstants

/**
 * Demonstrates the AggregateLayout class. In this demo, nodes are visually clustered as they are
 * selected. The cluster is formed in a new Layout centered at the middle locations of the selected
 * nodes. The layoutSize and layout algorithm for each new cluster is selectable.
 *
 * @author Tom Nelson
 */
@Suppress("serial")
class SubLayoutDemo : JPanel() {

    private val instructions =
        "<html>" +
            "Use the Layout combobox to select the " +
            "<p>underlying layout." +
            "<p>Use the SubLayout combobox to select " +
            "<p>the type of layout for any clusters you create." +
            "<p>To create clusters, use the mouse to select " +
            "<p>multiple nodes, either by dragging a region, " +
            "<p>or by shift-clicking on multiple nodes." +
            "<p>After you select nodes, use the " +
            "<p>Cluster Picked button to cluster them using the " +
            "<p>layout and layoutSize specified in the Sublayout comboboxen." +
            "<p>Use the Uncluster All button to remove all" +
            "<p>clusters." +
            "<p>You can drag the cluster with the mouse." +
            "<p>Use the 'Picking'/'Transforming' combo-box to switch" +
            "<p>between picking and transforming mode.</html>"

    /** the graph */
    val graph: Network<String, Number>

    @Suppress("UNCHECKED_CAST")
    val layoutClasses: Array<Class<LayoutAlgorithm<*>>> = arrayOf(
        CircleLayoutAlgorithm::class.java,
        SpringLayoutAlgorithm::class.java,
        FRLayoutAlgorithm::class.java,
        KKLayoutAlgorithm::class.java
    ) as Array<Class<LayoutAlgorithm<*>>>

    /** the visual component and renderer for the graph */
    val vv: VisualizationViewer<String, Number>

    val clusteringLayoutModel: AggregateLayoutModel<String>

    var subLayoutSize: Dimension? = null

    val ps: PickedState<String>

    @Suppress("UNCHECKED_CAST")
    var subLayoutType: Class<CircleLayoutAlgorithm<*>> =
        CircleLayoutAlgorithm::class.java as Class<CircleLayoutAlgorithm<*>>

    init {
        layout = BorderLayout()
        // create a simple graph for the demo
        graph = TestGraphs.getOneComponentGraph()

        // ClusteringLayout is a decorator class that delegates
        // to another layout, but can also separately manage the
        // layout of sub-sets of nodes in circular clusters.
        val preferredSize = Dimension(600, 600)

        val layoutAlgorithm: LayoutAlgorithm<String> = FRLayoutAlgorithm()
        clusteringLayoutModel = AggregateLayoutModel(
            LoadingCacheLayoutModel.builder<String>()
                .setGraph(graph.asGraph())
                .setSize(preferredSize.width, preferredSize.height)
                .build()
        )

        clusteringLayoutModel.accept(layoutAlgorithm)

        val visualizationModel =
            BaseVisualizationModel(graph, clusteringLayoutModel, layoutAlgorithm)

        vv = VisualizationViewer(visualizationModel, preferredSize)

        ps = vv.getPickedNodeState()
        vv.getRenderContext().setEdgeDrawPaintFunction(
            PickableEdgePaintFunction(vv.getPickedEdgeState(), Color.black, Color.red)
        )
        vv.getRenderContext().setNodeFillPaintFunction(
            PickableNodePaintFunction(vv.getPickedNodeState(), Color.red, Color.yellow)
        )
        vv.background = Color.white

        // add a listener for ToolTips
        vv.setNodeToolTipFunction { it.toString() }

        /** the regular graph mouse for the normal view */
        val graphMouse = DefaultModalGraphMouse<Any, Any>()

        vv.setGraphMouse(graphMouse)

        val gzsp = GraphZoomScrollPane(vv)
        add(gzsp)

        val modeBox = graphMouse.getModeComboBox()
        modeBox.addItemListener(graphMouse.getModeListener())
        graphMouse.setMode(ModalGraphMouse.Mode.PICKING)

        val cluster = JButton("Cluster Picked")
        cluster.addActionListener { clusterPicked() }

        val uncluster = JButton("UnCluster All")
        uncluster.addActionListener { uncluster() }

        val layoutTypeComboBox = JComboBox<Any>(layoutClasses)
        layoutTypeComboBox.setRenderer(object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component {
                var valueString = value.toString()
                valueString = valueString.substring(valueString.lastIndexOf('.') + 1)
                return super.getListCellRendererComponent(
                    list, valueString, index, isSelected, cellHasFocus
                )
            }
        })
        layoutTypeComboBox.selectedItem = FRLayoutAlgorithm::class.java
        layoutTypeComboBox.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                @Suppress("UNCHECKED_CAST")
                val clazz = e.item as Class<CircleLayoutAlgorithm<*>>
                try {
                    vv.getModel().getLayoutModel().accept(getLayoutAlgorithmFor(clazz))
                    vv.repaint()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }

        val subLayoutTypeComboBox = JComboBox<Any>(layoutClasses)
        subLayoutTypeComboBox.setRenderer(object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component {
                var valueString = value.toString()
                valueString = valueString.substring(valueString.lastIndexOf('.') + 1)
                return super.getListCellRendererComponent(
                    list, valueString, index, isSelected, cellHasFocus
                )
            }
        })
        subLayoutTypeComboBox.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                @Suppress("UNCHECKED_CAST")
                subLayoutType = e.item as Class<CircleLayoutAlgorithm<*>>
                uncluster()
                clusterPicked()
            }
        }

        val subLayoutDimensionComboBox = JComboBox<Any>(
            arrayOf(
                Dimension(75, 75),
                Dimension(100, 100),
                Dimension(150, 150),
                Dimension(200, 200),
                Dimension(250, 250),
                Dimension(300, 300)
            )
        )
        subLayoutDimensionComboBox.setRenderer(object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component {
                var valueString = value.toString()
                valueString = valueString.substring(valueString.lastIndexOf('['))
                valueString = valueString.replace("idth", "")
                valueString = valueString.replace("eight", "")
                return super.getListCellRendererComponent(
                    list, valueString, index, isSelected, cellHasFocus
                )
            }
        })
        subLayoutDimensionComboBox.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                subLayoutSize = e.item as Dimension
                uncluster()
                clusterPicked()
            }
        }

        subLayoutDimensionComboBox.selectedIndex = 1

        val help = JButton("Help")
        help.addActionListener { e ->
            JOptionPane.showMessageDialog(
                e.source as JComponent, instructions, "Help", JOptionPane.PLAIN_MESSAGE
            )
        }

        val space = Dimension(20, 20)
        val controls = Box.createVerticalBox()
        controls.add(Box.createRigidArea(space))

        val zoomControls: JComponent = ControlHelpers.getZoomControls(vv, "Zoom")
        heightConstrain(zoomControls)
        controls.add(zoomControls)
        controls.add(Box.createRigidArea(space))

        val clusterControls = JPanel(GridLayout(0, 1))
        clusterControls.border = BorderFactory.createTitledBorder("Clustering")
        clusterControls.add(cluster)
        clusterControls.add(uncluster)
        heightConstrain(clusterControls)
        controls.add(clusterControls)
        controls.add(Box.createRigidArea(space))

        val layoutControls = JPanel(GridLayout(0, 1))
        layoutControls.border = BorderFactory.createTitledBorder("Layout")
        layoutControls.add(layoutTypeComboBox)
        heightConstrain(layoutControls)
        controls.add(layoutControls)

        val subLayoutControls = JPanel(GridLayout(0, 1))
        subLayoutControls.border = BorderFactory.createTitledBorder("SubLayout")
        subLayoutControls.add(subLayoutTypeComboBox)
        subLayoutControls.add(subLayoutDimensionComboBox)
        heightConstrain(subLayoutControls)
        controls.add(subLayoutControls)
        controls.add(Box.createRigidArea(space))

        val modePanel = JPanel(GridLayout(1, 1))
        modePanel.border = BorderFactory.createTitledBorder("Mouse Mode")
        modePanel.add(modeBox)
        heightConstrain(modePanel)
        controls.add(modePanel)
        controls.add(Box.createRigidArea(space))

        controls.add(help)
        controls.add(Box.createVerticalGlue())
        add(controls, BorderLayout.EAST)
    }

    private fun heightConstrain(component: Component) {
        val d = Dimension(component.maximumSize.width, component.minimumSize.height)
        component.maximumSize = d
    }

    private fun getLayoutAlgorithmFor(
        layoutClass: Class<CircleLayoutAlgorithm<*>>
    ): LayoutAlgorithm<String> {
        val constructor: Constructor<CircleLayoutAlgorithm<*>> = layoutClass.getConstructor()
        @Suppress("UNCHECKED_CAST")
        return constructor.newInstance() as LayoutAlgorithm<String>
    }

    private fun clusterPicked() {
        cluster(true)
    }

    private fun uncluster() {
        cluster(false)
    }

    private fun cluster(state: Boolean) {
        if (state) {
            // put the picked nodes into a new sublayout
            val picked = ps.getPicked()
            if (picked.size > 1) {
                val center = Point2D.Double()
                var x = 0.0
                var y = 0.0
                for (node in picked.toSet()) {
                    val p = clusteringLayoutModel.apply(node)
                    x += p.x
                    y += p.y
                }
                x /= picked.size
                y /= picked.size
                center.setLocation(x, y)

                try {
                    val subGraph: MutableNetwork<String, Number> =
                        NetworkBuilder.from(graph).build()
                    for (node in picked.toSet()) {
                        subGraph.addNode(node)
                        for (edge in graph.incidentEdges(node).toSet()) {
                            val endpoints = graph.incidentNodes(edge)
                            val nodeU = endpoints.nodeU()
                            val nodeV = endpoints.nodeV()
                            if (picked.contains(nodeU) && picked.contains(nodeV)) {
                                // put this edge into the subgraph
                                subGraph.addEdge(nodeU, nodeV, edge)
                            }
                        }
                    }

                    val subLayoutAlgorithm: LayoutAlgorithm<String> =
                        getLayoutAlgorithmFor(subLayoutType)

                    val layoutSize = subLayoutSize ?: return
                    val newLayoutModel: LayoutModel<String> =
                        LoadingCacheLayoutModel.builder<String>()
                            .setGraph(subGraph.asGraph())
                            .setSize(layoutSize.width, layoutSize.height)
                            .setInitializer(
                                RandomLocationTransformer(layoutSize.width.toDouble(), layoutSize.height.toDouble(), 0L)
                            )
                            .build()

                    clusteringLayoutModel.put(
                        newLayoutModel, Point.of(center.x, center.y)
                    )
                    newLayoutModel.accept(subLayoutAlgorithm)
                    vv.repaint()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            // remove all sublayouts
            clusteringLayoutModel.removeAll()
            vv.repaint()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val f = JFrame()
            f.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            f.contentPane.add(SubLayoutDemo())
            f.pack()
            f.isVisible = true
        }
    }
}
