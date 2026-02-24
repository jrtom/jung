/*
 * Copyright (c) 2008, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.samples

import com.google.common.graph.MutableNetwork
import com.google.common.graph.Network
import com.google.common.graph.NetworkBuilder
import edu.uci.ics.jung.io.PajekNetReader
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm
import edu.uci.ics.jung.visualization.VisualizationViewer
import java.awt.Dimension
import java.io.IOException
import java.io.InputStreamReader
import javax.swing.JFrame
import javax.swing.WindowConstants

/** A class that shows the minimal work necessary to load and visualize a graph. */
object SimpleGraphDraw {

    @JvmStatic
    @Throws(IOException::class)
    fun main(args: Array<String>) {
        val jf = JFrame()
        @Suppress("UNCHECKED_CAST")
        val g = getGraph() as Network<Any, Any>
        val layoutAlgorithm = FRLayoutAlgorithm<Any>()
        val vv = VisualizationViewer<Any, Any>(g, layoutAlgorithm, Dimension(900, 900))
        jf.contentPane.add(vv)
        jf.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        jf.pack()
        jf.isVisible = true
    }

    /**
     * Generates a graph: in this case, reads it from the file (in the classpath):
     * "datasets/simple.net"
     *
     * @return A sample undirected graph
     * @throws IOException if there is an error in reading the file
     */
    @Throws(IOException::class)
    fun getGraph(): Network<*, *> {
        val pnr = PajekNetReader<MutableNetwork<Any, Any>, Any, Any>(::Any)

        val g = NetworkBuilder.undirected().build<Any, Any>()
        val reader = InputStreamReader(SimpleGraphDraw::class.java.getResourceAsStream("/datasets/simple.net"))
        pnr.load(reader, g)
        return g
    }
}
