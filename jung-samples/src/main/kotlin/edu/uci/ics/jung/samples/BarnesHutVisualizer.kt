package edu.uci.ics.jung.samples

import com.google.common.collect.Maps
import com.google.common.collect.Sets
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.layout.spatial.BarnesHutQuadTree
import edu.uci.ics.jung.layout.spatial.ForceObject
import edu.uci.ics.jung.layout.spatial.Node
import edu.uci.ics.jung.layout.spatial.Rectangle
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.Ellipse2D
import java.awt.geom.Line2D
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel

/**
 * Draws a Barnes-Hut Quad Tree. Mouse clicks on empty space add a new forceObject. Mouse clicks on
 * an existing object will highlight the other forces that will act on the clicked object
 *
 * @author Tom Nelson
 */
class BarnesHutVisualizer : JPanel() {

    private var tree: BarnesHutQuadTree<String>
    private val elements: MutableMap<String, Point> = Maps.newHashMap()
    private val stuffToDraw: MutableCollection<Shape> = Sets.newHashSet()

    init {
        layout = BorderLayout()

        elements["A"] = Point.of(200.0, 100.0)
        elements["B"] = Point.of(100.0, 200.0)
        elements["C"] = Point.of(100.0, 100.0)
        elements["D"] = Point.of(500.0, 100.0)

        tree = BarnesHutQuadTree(600.0, 600.0)
        tree.rebuild(elements)

        val drawingPanel = object : JPanel() {
            override fun getPreferredSize(): Dimension = Dimension(600, 600)

            override fun paint(g: Graphics) {
                super.paint(g)
                val g2d = g as Graphics2D
                draw(g2d, tree.getRoot())
                for (shape in stuffToDraw) {
                    g2d.draw(shape)
                }
            }
        }
        add(drawingPanel)
        drawingPanel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                super.mouseClicked(e)
                stuffToDraw.clear()
                val p: Point2D = e.point
                val got = getNodeAt(p)
                if (got != null) {
                    val nodeForceObject = object : ForceObject<String>(got, elements[got]!!) {
                        override fun addForceFrom(other: ForceObject<String>) {
                            log.info("adding force from {}", other)
                            val ellipse = Ellipse2D.Double(other.p.x - 15, other.p.y - 15, 30.0, 30.0)
                            stuffToDraw.add(ellipse)
                            val line = Line2D.Double(this.p.x, this.p.y, other.p.x, other.p.y)
                            stuffToDraw.add(line)
                        }
                    }
                    tree.applyForcesTo(nodeForceObject)
                } else {
                    addShapeAt(p)
                }
                repaint()
            }
        })

        val clear = JButton("clear")
        clear.addActionListener { clearNetwork() }
        val go = JButton("Log all forces")
        go.addActionListener {
            for (node in elements.keys) {
                val nodeForceObject = object : ForceObject<String>(node, elements[node]!!) {
                    override fun addForceFrom(other: ForceObject<String>) {
                        log.info("for node {}, next force object is {}", node, other)
                    }
                }
                tree.applyForcesTo(nodeForceObject)
            }
        }
        val controls = JPanel()
        controls.add(go)
        controls.add(clear)
        add(controls, BorderLayout.SOUTH)
    }

    private fun clearNetwork() {
        elements.clear()
        tree.clear()
        tree.rebuild(elements)
        repaint()
    }

    private fun addShapeAt(p: Point2D) {
        val n = "N${elements.size}"
        elements[n] = Point.of(p.x, p.y)
        tree.rebuild(elements)
        repaint()
    }

    private fun getNodeAt(p: Point2D): String? {
        for (node in elements.keys) {
            val loc = elements[node]
            if (loc != null && loc.distanceSquared(p.x, p.y) < 20) {
                return node
            }
        }
        return null
    }

    private fun draw(g: Graphics2D, node: Node<String>) {
        val bounds: Rectangle = node.bounds
        val r = Rectangle2D.Double(bounds.x, bounds.y, bounds.width, bounds.height)
        g.draw(r)
        val forceObject = node.forceObject
        if (forceObject != null) {
            val center = node.forceObject!!.p
            val forceCenter = Ellipse2D.Double(center.x - 5, center.y - 5, 10.0, 10.0)
            val oldColor = g.color
            g.color = Color.red

            val centerOfNode = Point2D.Double(r.centerX, r.centerY)
            val centerOfForce = Point2D.Double(center.x, center.y)
            g.draw(Line2D.Double(centerOfNode, centerOfForce))
            g.draw(forceCenter)
            g.color = oldColor
        }
        if (node.NW != null) {
            draw(g, node.NW!!)
        }
        if (node.NE != null) {
            draw(g, node.NE!!)
        }
        if (node.SW != null) {
            draw(g, node.SW!!)
        }
        if (node.SE != null) {
            draw(g, node.SE!!)
        }
        if (forceObject != null) {
            val p = forceObject.p
            val circle = Ellipse2D.Double(p.x - 2, p.y - 2, 4.0, 4.0)
            g.fill(circle)
            g.drawString(forceObject.element.toString(), p.x.toInt() + 4, p.y.toInt() - 4)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(BarnesHutVisualizer::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            val frame = JFrame()
            frame.contentPane.add(BarnesHutVisualizer())
            frame.pack()
            frame.isVisible = true
        }
    }
}
