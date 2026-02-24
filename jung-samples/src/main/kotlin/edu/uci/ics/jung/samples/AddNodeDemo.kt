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
import com.google.common.graph.NetworkBuilder
import edu.uci.ics.jung.graph.ObservableNetwork
import edu.uci.ics.jung.graph.util.Graphs
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.SpringLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.StaticLayoutAlgorithm
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.layout.LayoutAlgorithmTransition
import edu.uci.ics.jung.visualization.renderers.Renderer
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.GridLayout
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.util.Timer
import java.util.TimerTask
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.WindowConstants

/**
 * A variation of old AddNodeDemo that animates transitions between graph algorithms.
 *
 * @author Tom Nelson
 */
class AddNodeDemo : JPanel() {

    private val g: MutableNetwork<Number, Number>
    private val vv: VisualizationViewer<Number, Number>
    private var layoutAlgorithm: LayoutAlgorithm<Number>
    private val timer: Timer
    private var done = false
    protected val switchLayout: JButton
    private var v_prev: Int? = null

    init {
        val original: MutableNetwork<Number, Number> =
            NetworkBuilder.directed().allowsParallelEdges(true).allowsSelfLoops(true).build()
        val ig: MutableNetwork<Number, Number> = Graphs.synchronizedNetwork(original)
        val og = ObservableNetwork(ig)
        if (log.isDebugEnabled) {
            og.addGraphEventListener(object : edu.uci.ics.jung.graph.event.NetworkEventListener<Number, Number> {
                override fun handleGraphEvent(evt: edu.uci.ics.jung.graph.event.NetworkEvent<Number, Number>) {
                    log.debug("got $evt")
                }
            })
        }

        g = og

        layoutAlgorithm = FRLayoutAlgorithm()

        val staticLayoutAlgorithm: LayoutAlgorithm<Number> = StaticLayoutAlgorithm()

        vv = VisualizationViewer(ig, staticLayoutAlgorithm, Dimension(600, 600))

        layout = BorderLayout()
        background = Color.lightGray
        font = Font("Serif", Font.PLAIN, 12)

        vv.setGraphMouse(DefaultModalGraphMouse<Number, Number>())

        vv.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.CNTR)
        vv.getRenderContext().setNodeLabelFunction { it.toString() }
        vv.foreground = Color.white

        add(vv)

        // add listener to change layout size and restart layoutalgorithm when
        // the view is resized
        vv.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                super.componentResized(e)
                @Suppress("UNCHECKED_CAST")
                val vv = e.component as VisualizationViewer<Any, Any>
                val model: VisualizationModel<Any, Any> = vv.getModel()
                val layoutModel: LayoutModel<Any> = model.getLayoutModel()
                layoutModel.setSize(vv.width, vv.height)
                layoutModel.accept(model.getLayoutAlgorithm())
            }
        })

        val animateChange = JRadioButton("Animate Layout Change")
        switchLayout = JButton("Switch to SpringLayout")
        switchLayout.addActionListener {
            if (switchLayout.text.indexOf("Spring") > 0) {
                switchLayout.text = "Switch to FRLayout"
                layoutAlgorithm = SpringLayoutAlgorithm { EDGE_LENGTH }
            } else {
                switchLayout.text = "Switch to SpringLayout"
                layoutAlgorithm = FRLayoutAlgorithm()
            }
            if (animateChange.isSelected) {
                LayoutAlgorithmTransition.animate(vv, layoutAlgorithm)
            } else {
                LayoutAlgorithmTransition.apply(vv, layoutAlgorithm)
            }
        }

        val southPanel = JPanel(GridLayout(1, 2))
        southPanel.add(switchLayout)
        southPanel.add(animateChange)
        add(southPanel, BorderLayout.SOUTH)

        timer = Timer()
        timer.schedule(RemindTask(), 1000, 1000) // subsequent rate
        vv.repaint()
    }

    fun process() {
        vv.getRenderContext().getPickedNodeState().clear()
        vv.getRenderContext().getPickedEdgeState().clear()
        try {
            if (g.nodes().size < 100) {
                // add a node
                val v1: Int = g.nodes().size

                g.addNode(v1)
                vv.getRenderContext().getPickedNodeState().pick(v1, true)

                // wire it to some edges
                if (v_prev != null) {
                    var edge: Int = g.edges().size
                    vv.getRenderContext().getPickedEdgeState().pick(edge, true)
                    g.addEdge(v_prev, v1, edge)
                    // let's connect to a random node, too!
                    val rand = (Math.random() * g.nodes().size).toInt()
                    edge = g.edges().size
                    vv.getRenderContext().getPickedEdgeState().pick(edge, true)
                    g.addEdge(v1, rand, edge)
                }

                v_prev = v1

                // accept the algorithm again so that it will turn off the old relaxer and start a new one
                vv.getModel().getLayoutModel().accept(layoutAlgorithm)

                vv.repaint()
            } else {
                done = true
            }
        } catch (e: Exception) {
            log.warn("exception:", e)
        }
    }

    internal inner class RemindTask : TimerTask() {
        override fun run() {
            process()
            if (done) {
                cancel()
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(AddNodeDemo::class.java)
        private const val serialVersionUID = -5345319851341875800L
        const val EDGE_LENGTH = 100

        @JvmStatic
        fun main(args: Array<String>) {
            val and = AddNodeDemo()
            val frame = JFrame()
            frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            frame.contentPane.add(and)
            frame.pack()
            frame.isVisible = true
        }
    }
}
