/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.samples

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.common.graph.MutableNetwork
import com.google.common.graph.Network
import com.google.common.graph.NetworkBuilder
import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer
import edu.uci.ics.jung.io.PajekNetReader
import edu.uci.ics.jung.layout.algorithms.CircleLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.visualization.BaseVisualizationModel
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.layout.AggregateLayoutModel
import java.awt.BasicStroke
import java.awt.BorderLayout
import javax.swing.BoxLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.Paint
import java.awt.Stroke
import java.awt.event.ItemEvent
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.function.Supplier
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JSlider
import javax.swing.JToggleButton
import javax.swing.WindowConstants
import javax.swing.border.TitledBorder

/**
 * This simple app demonstrates how one can use our algorithms and visualization libraries in
 * unison. In this case, we generate use the Zachary karate club data set, widely known in the
 * social networks literature, then we cluster the nodes using an edge-betweenness clusterer, and
 * finally we visualize the graph using Fruchtermain-Rheingold layout and provide a slider so that
 * the user can adjust the clustering granularity.
 *
 * @author Scott White
 */
class ClusteringDemo : JPanel() {

    private lateinit var vv: VisualizationViewer<Number, Number>

    private val nodePaints: LoadingCache<Number, Paint> =
        CacheBuilder.newBuilder().build(CacheLoader.from<Number, Paint> { Color.white })
    private val edgePaints: LoadingCache<Number, Paint> =
        CacheBuilder.newBuilder().build(CacheLoader.from<Number, Paint> { Color.blue })

    val similarColors = arrayOf(
        Color(216, 134, 134),
        Color(135, 137, 211),
        Color(134, 206, 189),
        Color(206, 176, 134),
        Color(194, 204, 134),
        Color(145, 214, 134),
        Color(133, 178, 209),
        Color(103, 148, 255),
        Color(60, 220, 220),
        Color(30, 250, 100)
    )

    fun start() {
        val `is` = this.javaClass.classLoader.getResourceAsStream("datasets/zachary.net")
        val br = BufferedReader(InputStreamReader(`is`))

        try {
            setUpView(br)
        } catch (e: IOException) {
            println("Error in loading graph")
            e.printStackTrace()
        }
    }

    private fun setUpView(br: BufferedReader) {
        val nodeFactory = object : Supplier<Number> {
            var n = 0
            override fun get(): Number = n++
        }
        val edgeFactory = object : Supplier<Number> {
            var n = 0
            override fun get(): Number = n++
        }

        val pnr = PajekNetReader<MutableNetwork<Number, Number>, Number, Number>(nodeFactory, edgeFactory)

        val graph: MutableNetwork<Number, Number> = NetworkBuilder.undirected().build()

        pnr.load(br, graph)

        // Create a simple layout frame
        // specify the Fruchterman-Rheingold layout algorithm
        val algorithm: LayoutAlgorithm<Number> = FRLayoutAlgorithm()
        val delegateModel: LayoutModel<Number> =
            LoadingCacheLayoutModel.builder<Number>()
                .setGraph(graph.asGraph())
                .setSize(600, 600)
                .build()

        layout = BorderLayout()

        val layoutModel = AggregateLayoutModel(delegateModel)
        @Suppress("UNCHECKED_CAST")
        val visualizationModel: VisualizationModel<Number, Number> =
            BaseVisualizationModel(graph, layoutModel, algorithm) as VisualizationModel<Number, Number>

        vv = VisualizationViewer(visualizationModel, Dimension(800, 800))
        vv.background = Color.white
        // Tell the renderer to use our own customized color rendering
        vv.getRenderContext().setNodeFillPaintFunction(nodePaints)
        vv.getRenderContext().setNodeDrawPaintFunction { v ->
            if (vv.getPickedNodeState().isPicked(v)) Color.CYAN else Color.BLACK
        }

        vv.getRenderContext().setEdgeDrawPaintFunction(edgePaints)

        vv.getRenderContext().setEdgeStrokeFunction { e ->
            if (edgePaints.getUnchecked(e) === Color.LIGHT_GRAY) THIN else THICK
        }

        // add restart button
        val scramble = javax.swing.JButton("Restart")
        scramble.addActionListener {
            val layoutAlgorithm = vv.getModel().getLayoutAlgorithm()
            vv.getModel().getLayoutModel().accept(layoutAlgorithm)
        }

        val gm = DefaultModalGraphMouse<Number, Number>()
        vv.setGraphMouse(gm)

        val groupNodes = JToggleButton("Group Clusters")

        // Create slider to adjust the number of edges to remove when clustering
        val edgeBetweennessSlider = JSlider(JSlider.HORIZONTAL)
        edgeBetweennessSlider.background = Color.WHITE
        edgeBetweennessSlider.preferredSize = Dimension(210, 50)
        edgeBetweennessSlider.paintTicks = true
        edgeBetweennessSlider.maximum = graph.edges().size
        edgeBetweennessSlider.minimum = 0
        edgeBetweennessSlider.value = 0
        edgeBetweennessSlider.majorTickSpacing = 10
        edgeBetweennessSlider.paintLabels = true
        edgeBetweennessSlider.paintTicks = true

        val eastControls = JPanel()
        eastControls.isOpaque = true
        eastControls.layout = BoxLayout(eastControls, BoxLayout.Y_AXIS)
        eastControls.add(Box.createVerticalGlue())
        eastControls.add(edgeBetweennessSlider)

        val COMMANDSTRING = "Edges removed for clusters: "
        val eastSize = COMMANDSTRING + edgeBetweennessSlider.value

        val sliderBorder: TitledBorder = BorderFactory.createTitledBorder(eastSize)
        eastControls.border = sliderBorder
        eastControls.add(Box.createVerticalGlue())

        groupNodes.addItemListener { e ->
            clusterAndRecolor(
                layoutModel,
                graph,
                edgeBetweennessSlider.value,
                similarColors,
                e.stateChange == ItemEvent.SELECTED
            )
            vv.repaint()
        }

        clusterAndRecolor(layoutModel, graph, 0, similarColors, groupNodes.isSelected)

        edgeBetweennessSlider.addChangeListener { e ->
            val source = e.source as JSlider
            if (!source.valueIsAdjusting) {
                val numEdgesToRemove = source.value
                clusterAndRecolor(
                    layoutModel, graph, numEdgesToRemove, similarColors, groupNodes.isSelected
                )
                sliderBorder.title = COMMANDSTRING + edgeBetweennessSlider.value
                eastControls.repaint()
                vv.validate()
                vv.repaint()
            }
        }

        add(GraphZoomScrollPane(vv))
        val south = JPanel()
        val grid = JPanel(GridLayout(2, 1))
        grid.add(scramble)
        grid.add(groupNodes)
        south.add(grid)
        south.add(eastControls)
        val p = JPanel()
        p.border = BorderFactory.createTitledBorder("Mouse Mode")
        p.add(gm.getModeComboBox())
        south.add(p)
        add(south, BorderLayout.SOUTH)
    }

    fun clusterAndRecolor(
        layoutModel: AggregateLayoutModel<Number>,
        graph: Network<Number, Number>,
        numEdgesToRemove: Int,
        colors: Array<Color>,
        groupClusters: Boolean
    ) {
        layoutModel.removeAll()

        val clusterer = EdgeBetweennessClusterer<Number, Number>(numEdgesToRemove)
        val clusterSet = clusterer.apply(graph)
        val edges = clusterer.getEdgesRemoved()

        var i = 0
        // Set the colors of each node so that each cluster's nodes have the same color
        for (nodes in clusterSet) {
            val c = colors[i % colors.size]
            colorCluster(nodes, c)
            if (groupClusters) {
                groupCluster(layoutModel, nodes)
            }
            i++
        }
        for (e in graph.edges()) {
            edgePaints.put(e, if (edges.contains(e)) Color.LIGHT_GRAY else Color.BLACK)
        }
    }

    private fun colorCluster(nodes: Set<Number>, c: Color) {
        for (v in nodes) {
            nodePaints.put(v, c)
        }
    }

    private fun groupCluster(layoutModel: AggregateLayoutModel<Number>, nodes: Set<Number>) {
        if (nodes.size < vv.getModel().getNetwork().nodes().size) {
            val center: Point = layoutModel.apply(nodes.iterator().next())
            val subGraph: MutableNetwork<Number, Number> = NetworkBuilder.undirected().build()
            for (v in nodes) {
                subGraph.addNode(v)
            }
            val subLayoutAlgorithm: LayoutAlgorithm<Number> = CircleLayoutAlgorithm()

            val subModel: LayoutModel<Number> =
                LoadingCacheLayoutModel.builder<Number>()
                    .setGraph(subGraph.asGraph())
                    .setSize(40, 40)
                    .build()

            layoutModel.put(subModel, center)
            subModel.accept(subLayoutAlgorithm)
            vv.repaint()
        }
    }

    companion object {
        private val THIN: Stroke = BasicStroke(1f)
        private val THICK: Stroke = BasicStroke(2f)

        @JvmStatic
        @Throws(IOException::class)
        fun main(args: Array<String>) {
            val cd = ClusteringDemo()
            cd.start()
            // Add a restart button so the graph can be redrawn to fit the layoutSize of the frame
            val jf = JFrame()
            jf.contentPane.add(cd)

            jf.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            jf.pack()
            jf.isVisible = true
        }
    }
}
