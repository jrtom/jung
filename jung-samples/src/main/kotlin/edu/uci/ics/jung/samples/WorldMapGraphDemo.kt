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
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.StaticLayoutAlgorithm
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.visualization.BaseVisualizationModel
import edu.uci.ics.jung.visualization.GraphZoomScrollPane
import edu.uci.ics.jung.visualization.MultiLayerTransformer
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.ScalingControl
import edu.uci.ics.jung.visualization.renderers.BasicNodeLabelRenderer
import edu.uci.ics.jung.visualization.renderers.GradientNodeRenderer
import edu.uci.ics.jung.visualization.renderers.Renderer
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Container
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.util.function.Function
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

/**
 * Shows a graph overlaid on a world map image. Scaling of the graph also scales the image
 * background.
 *
 * @author Tom Nelson
 */
@Suppress("serial")
class WorldMapGraphDemo : JPanel() {

    /** the graph */
    val graph: Network<String, Number>

    /** the visual component and renderer for the graph */
    val vv: VisualizationViewer<String, Number>

    val cityList: List<String>

    /** create an instance of a simple graph with controls to demo the zoom features. */
    init {
        layout = BorderLayout()

        val map = buildMap()

        cityList = ArrayList(map.keys)

        // create a simple graph for the demo
        graph = buildGraph(map)

        var mapIcon: ImageIcon? = null
        val imageLocation = "/images/political_world_map.jpg"
        try {
            mapIcon = ImageIcon(javaClass.getResource(imageLocation))
        } catch (ex: Exception) {
            System.err.println("Can't load \"$imageLocation\"")
        }
        val icon = mapIcon

        val layoutAlgorithm: LayoutAlgorithm<String> = StaticLayoutAlgorithm()

        val initializer: Function<String, Point> =
            CityTransformer(map).andThen(LatLonPixelTransformer(Dimension(2000, 1000)))
        val model: VisualizationModel<String, Number> =
            BaseVisualizationModel(graph, layoutAlgorithm, initializer, Dimension(2000, 1000))

        vv = VisualizationViewer(model, Dimension(800, 400))

        if (icon != null) {
            vv.addPreRenderPaintable(object : VisualizationServer.Paintable {
                override fun paint(g: Graphics) {
                    val g2d = g as Graphics2D
                    val oldXform = g2d.transform
                    val lat = vv.getRenderContext()
                        .getMultiLayerTransformer()
                        .getTransformer(MultiLayerTransformer.Layer.LAYOUT)
                        .getTransform()
                    val vat = vv.getRenderContext()
                        .getMultiLayerTransformer()
                        .getTransformer(MultiLayerTransformer.Layer.VIEW)
                        .getTransform()
                    val at = AffineTransform()
                    at.concatenate(g2d.transform)
                    at.concatenate(vat)
                    at.concatenate(lat)
                    g2d.transform = at
                    g.drawImage(icon.image, 0, 0, icon.iconWidth, icon.iconHeight, vv)
                    g2d.transform = oldXform
                }

                override fun useTransform(): Boolean = false
            })
        }

        vv.getRenderer().setNodeRenderer(
            GradientNodeRenderer<String, Number>(vv, Color.white, Color.red, Color.white, Color.blue, false))

        // add my listeners for ToolTips
        vv.setNodeToolTipFunction { n: String -> n }
        vv.setEdgeToolTipFunction { edge: Number -> "E${graph.incidentNodes(edge)}" }

        vv.getRenderContext().setNodeLabelFunction { n: String -> n }
        vv.getRenderer()
            .getNodeLabelRenderer()
            .setPositioner(BasicNodeLabelRenderer.InsidePositioner())
        vv.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.AUTO)

        val panel = GraphZoomScrollPane(vv)
        add(panel)
        val graphMouse: AbstractModalGraphMouse = DefaultModalGraphMouse<String, Number>()
        vv.setGraphMouse(graphMouse)

        vv.addKeyListener(graphMouse.modeKeyListener!!)
        vv.toolTipText = "<html><center>Type 'p' for Pick mode<p>Type 't' for Transform mode"

        val scaler: ScalingControl = CrossoverScalingControl()

        val plus = JButton("+")
        plus.addActionListener { scaler.scale(vv, 1.1f, vv.getCenter()) }

        val minus = JButton("-")
        minus.addActionListener { scaler.scale(vv, 1.0f / 1.1f, vv.getCenter()) }

        val reset = JButton("reset")
        reset.addActionListener {
            vv.getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(MultiLayerTransformer.Layer.LAYOUT)
                .setToIdentity()
            vv.getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(MultiLayerTransformer.Layer.VIEW)
                .setToIdentity()
        }

        val controls = JPanel()
        controls.add(plus)
        controls.add(minus)
        controls.add(reset)
        add(controls, BorderLayout.SOUTH)
    }

    private fun buildMap(): Map<String, Array<String>> {
        val map = HashMap<String, Array<String>>()

        map["TYO"] = arrayOf("35 40 N", "139 45 E")
        map["PEK"] = arrayOf("39 55 N", "116 26 E")
        map["MOW"] = arrayOf("55 45 N", "37 42 E")
        map["JRS"] = arrayOf("31 47 N", "35 13 E")
        map["CAI"] = arrayOf("30 03 N", "31 15 E")
        map["CPT"] = arrayOf("33 55 S", "18 22 E")
        map["PAR"] = arrayOf("48 52 N", "2 20 E")
        map["LHR"] = arrayOf("51 30 N", "0 10 W")
        map["HNL"] = arrayOf("21 18 N", "157 51 W")
        map["NYC"] = arrayOf("40 77 N", "73 98 W")
        map["SFO"] = arrayOf("37 62 N", "122 38 W")
        map["AKL"] = arrayOf("36 55 S", "174 47 E")
        map["BNE"] = arrayOf("27 28 S", "153 02 E")
        map["HKG"] = arrayOf("22 15 N", "114 10 E")
        map["KTM"] = arrayOf("27 42 N", "85 19 E")
        map["IST"] = arrayOf("41 01 N", "28 58 E")
        map["STO"] = arrayOf("59 20 N", "18 03 E")
        map["RIO"] = arrayOf("22 54 S", "43 14 W")
        map["LIM"] = arrayOf("12 03 S", "77 03 W")
        map["YTO"] = arrayOf("43 39 N", "79 23 W")

        return map
    }

    private fun buildGraph(map: Map<String, Array<String>>): Network<String, Number> {
        val graph: MutableNetwork<String, Number> =
            NetworkBuilder.directed().allowsParallelEdges(true).allowsSelfLoops(true).build()
        for (city in map.keys) {
            graph.addNode(city)
        }
        for (i in 0 until (map.keys.size * 1.3).toInt()) {
            graph.addEdge(randomCity(), randomCity(), Math.random())
        }
        return graph
    }

    private fun randomCity(): String {
        val m = cityList.size
        return cityList[(Math.random() * m).toInt()]
    }

    class CityTransformer(private val map: Map<String, Array<String>>) :
        Function<String, Array<String>> {

        /** transform airport code to latlon string */
        override fun apply(city: String): Array<String> = map[city]!!
    }

    class LatLonPixelTransformer(private val d: Dimension) : Function<Array<String>, Point> {

        /** transform a lat */
        override fun apply(latlon: Array<String>): Point {
            val lat = latlon[0].split(" ")
            val lon = latlon[1].split(" ")
            var latitude = lat[0].toInt() + lat[1].toInt() / 60.0
            latitude *= d.height / 180.0
            var longitude = lon[0].toInt() + lon[1].toInt() / 60.0
            longitude *= d.width / 360.0
            if (lat[2] == "N") {
                latitude = d.height / 2.0 - latitude
            } else { // assume S
                latitude = d.height / 2.0 + latitude
            }

            if (lon[2] == "W") {
                longitude = d.width / 2.0 - longitude
            } else { // assume E
                longitude = d.width / 2.0 + longitude
            }

            return Point.of(longitude, latitude)
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // create a frame to hold the graph
            val frame = JFrame()
            val content: Container = frame.contentPane
            content.add(WorldMapGraphDemo())
            frame.pack()
            frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            frame.isVisible = true
        }
    }
}
