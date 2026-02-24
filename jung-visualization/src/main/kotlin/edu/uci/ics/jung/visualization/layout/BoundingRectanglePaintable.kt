package edu.uci.ics.jung.visualization.layout

import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.visualization.MultiLayerTransformer
import edu.uci.ics.jung.visualization.RenderContext
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.util.ChangeEventSupport
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.geom.Rectangle2D
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

open class BoundingRectanglePaintable<N : Any>(
    private val rc: RenderContext<*, *>,
    visualizationModel: VisualizationModel<N, *>
) : VisualizationServer.Paintable {

    private val layoutModel: LayoutModel<N> = visualizationModel.getLayoutModel()
    private var rectangles: List<Rectangle2D>

    init {
        val brc = BoundingRectangleCollector.Nodes<N>(rc, visualizationModel)
        this.rectangles = brc.getRectangles()
        if (layoutModel is ChangeEventSupport) {
            (layoutModel as ChangeEventSupport).addChangeListener(object : ChangeListener {
                override fun stateChanged(e: ChangeEvent) {
                    brc.compute()
                    rectangles = brc.getRectangles()
                }
            })
        }
    }

    override fun paint(g: Graphics) {
        val g2d = g as Graphics2D
        g.setColor(Color.cyan)

        for (r in rectangles) {
            g2d.draw(rc.getMultiLayerTransformer().transform(MultiLayerTransformer.Layer.LAYOUT, r))
        }
    }

    override fun useTransform(): Boolean = true
}
