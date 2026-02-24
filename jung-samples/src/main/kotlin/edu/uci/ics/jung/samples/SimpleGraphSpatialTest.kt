/*
 * Copyright (c) 2008, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.samples

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.google.common.graph.Network
import edu.uci.ics.jung.graph.util.TestGraphs
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm
import edu.uci.ics.jung.layout.util.RandomLocationTransformer
import edu.uci.ics.jung.visualization.BaseVisualizationModel
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.renderers.Renderer
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants
import org.slf4j.LoggerFactory

/**
 * A test program to show the SpatialLayout structure and allow users to manipulate the graph ('p'
 * for pick mode, 't' for transform mode) and watch the Spatial structure update.
 *
 * @author Tom Nelson
 */
class SimpleGraphSpatialTest : JPanel() {

    init {
        layout = BorderLayout()

        val g = TestGraphs.getOneComponentGraph()

        val viewPreferredSize = Dimension(600, 600)
        val layoutPreferredSize = Dimension(600, 600)
        val layoutAlgorithm = FRLayoutAlgorithm<Any>()

        val scaler = CrossoverScalingControl()
        @Suppress("UNCHECKED_CAST")
        val model = BaseVisualizationModel<Any, Any>(
            g as Network<Any, Any>,
            layoutAlgorithm,
            RandomLocationTransformer<Any>(600.0, 600.0, System.currentTimeMillis()),
            layoutPreferredSize
        )
        val vv = VisualizationViewer<Any, Any>(model, viewPreferredSize)
        val graphMouse = DefaultModalGraphMouse<Any, Any>()
        vv.setGraphMouse(graphMouse)
        vv.getRenderContext().setNodeLabelFunction { it.toString() }
        vv.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.CNTR)
        vv.addKeyListener(graphMouse.modeKeyListener!!)
        vv.toolTipText = "<html><center>Type 'p' for Pick mode<p>Type 't' for Transform mode"
        vv.foreground = Color.white
        vv.scaleToLayout(scaler)
        this.add(vv)
    }

    companion object {
        private val log = LoggerFactory.getLogger(SimpleGraphSpatialTest::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            // programmatically set the log level so that the spatial grid is drawn for this demo and the
            // SpatialGrid logging is output
            val ctx = LoggerFactory.getILoggerFactory() as LoggerContext
            ctx.getLogger("edu.uci.ics.jung.visualization.spatial").level = Level.DEBUG
            ctx.getLogger("edu.uci.ics.jung.visualization.BasicVisualizationServer").level = Level.TRACE
            ctx.getLogger("edu.uci.ics.jung.visualization.picking").level = Level.TRACE

            val jf = JFrame()
            jf.contentPane.add(SimpleGraphSpatialTest())
            jf.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            jf.pack()
            jf.isVisible = true
        }
    }
}
