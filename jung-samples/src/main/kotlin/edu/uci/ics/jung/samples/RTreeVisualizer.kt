package edu.uci.ics.jung.samples

import edu.uci.ics.jung.visualization.spatial.rtree.Bounded
import edu.uci.ics.jung.visualization.spatial.rtree.InnerNode
import edu.uci.ics.jung.visualization.spatial.rtree.LeafNode
import edu.uci.ics.jung.visualization.spatial.rtree.Node
import edu.uci.ics.jung.visualization.spatial.rtree.RStarLeafSplitter
import edu.uci.ics.jung.visualization.spatial.rtree.RStarSplitter
import edu.uci.ics.jung.visualization.spatial.rtree.RTree
import edu.uci.ics.jung.visualization.spatial.rtree.SplitterContext
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.WindowConstants
import org.slf4j.LoggerFactory

/**
 * A visualization of the R-Tree structure. Users can add random elements, elements at mouse-click
 * location, or 2000 randomly generated elements. The structure of the R-Tree is also drawn.
 *
 * @author Tom Nelson
 */
class RTreeVisualizer : JPanel() {

    private val splitterContext: SplitterContext<Any> =
        SplitterContext.of(RStarLeafSplitter(), RStarSplitter())
    private var rTree: RTree<Any> = RTree.create()
    private var count = 0

    init {
        background = Color.white
        layout = BorderLayout()

        val addStuff = JButton("Add something")
        addStuff.addActionListener { addRandomShape() }
        val drawingPane = object : JPanel() {
            override fun getPreferredSize(): Dimension = Dimension(600, 600)

            override fun paint(g: Graphics) {
                super.paint(g)
                val g2d = g as Graphics2D
                val grid = rTree.getGrid()
                log.info("grid size is {}", grid.size)
                for (r in grid.toList()) {
                    g2d.draw(r)
                }
            }
        }
        val addLots = JButton("Add Many")
        addLots.addActionListener { addMany() }

        drawingPane.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                super.mouseClicked(e)

                if (SwingUtilities.isRightMouseButton(e)) {
                    val o = rTree.getPickedObject(e.point)
                    rTree = rTree.remove(o!!)
                    log.info("after removing {} rtree:{}", o, rTree)
                    repaint()
                } else {
                    addShapeAt(e.point)
                }
                repaint()
            }
        })
        val samePoint = JButton("Add Same")
        samePoint.addActionListener { addShapeAt(Point2D.Double(200.0, 200.0)) }
        val clear = JButton("clear")
        clear.addActionListener {
            rTree = RTree.create()
            repaint()
        }
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                log.info("clicked at {}", e.point)
                val node = "N${count++}"
                repaint()
            }
        })

        val controls = JPanel()
        controls.add(addStuff)
        controls.add(addLots)
        controls.add(clear)
        controls.add(samePoint)
        add(drawingPane)
        add(controls, BorderLayout.SOUTH)
    }

    private fun addRandomShape() {
        val width = 10.0
        val height = 10.0
        val x = Math.random() * 600 - width
        val y = Math.random() * 600 - height
        val r = Rectangle2D.Double(x, y, width, height)
        rTree = rTree.add(splitterContext, "N${count++}", r)
        repaint()
    }

    private fun addMany() {
        for (i in 0 until 2000) {
            val width = 4.0
            val height = 4.0
            val x = Math.random() * 600 - width
            val y = Math.random() * 600 - height
            val r = Rectangle2D.Double(x, y, width, height)
            rTree = rTree.add(splitterContext, "N${count++}", r)
            checkBounds(rTree)
            repaint()
        }
        repaint()
    }

    private fun addShapeAt(p: Point2D) {
        val width = 30.0
        val height = 30.0
        val r = Rectangle2D.Double(p.x - width / 2, p.y - height / 2, width, height)
        rTree = rTree.add(splitterContext, "N${count++}", r)
        log.info("after adding {} rtree:{}", "N${count - 1}", rTree)
        checkBounds(rTree)
        repaint()
    }

    private fun checkBounds(tree: RTree<*>) {
        checkBounds(tree.getRoot().get())
    }

    private fun getBounds(nodes: Collection<out Bounded>): Rectangle2D? {
        var bounds: Rectangle2D? = null
        for (b in nodes) {
            bounds = if (bounds == null) b.getBounds() else bounds.createUnion(b.getBounds())
        }
        return bounds
    }

    private fun checkBounds(node: InnerNode<*>) {
        if (node.getBounds() != getBounds(node.getChildren())) {
            log.error("bounds not equal \n{} != \n{}", node.getBounds(), getBounds(node.getChildren()))
        }
    }

    private fun checkBounds(node: Node<*>) {
        when (node) {
            is InnerNode<*> -> checkBounds(node)
            is LeafNode<*> -> log.info("leafNode: {}", node)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(RTreeVisualizer::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            val f = JFrame()
            f.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            f.contentPane.add(RTreeVisualizer())
            f.pack()
            f.isVisible = true
        }
    }
}
