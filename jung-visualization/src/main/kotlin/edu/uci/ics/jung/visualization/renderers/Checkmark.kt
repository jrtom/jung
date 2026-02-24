package edu.uci.ics.jung.visualization.renderers

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.geom.GeneralPath
import java.util.Collections
import javax.swing.Icon

/**
 * a simple Icon that draws a checkmark in the lower-right quadrant of its area. Used to draw a
 * checkmark on Picked Nodes.
 *
 * @author Tom Nelson
 */
open class Checkmark @JvmOverloads constructor(
    private val color: Color = Color.green
) : Icon {

    private val path = GeneralPath().apply {
        moveTo(10f, 17f)
        lineTo(13f, 20f)
        lineTo(20f, 13f)
    }

    private val highlight: AffineTransform = AffineTransform.getTranslateInstance(-1.0, -1.0)
    private val lowlight: AffineTransform = AffineTransform.getTranslateInstance(1.0, 1.0)
    private val shadow: AffineTransform = AffineTransform.getTranslateInstance(2.0, 2.0)

    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        val shape = AffineTransform.getTranslateInstance(x.toDouble(), y.toDouble())
            .createTransformedShape(path)
        val g2d = g as Graphics2D
        g2d.addRenderingHints(
            Collections.singletonMap(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON
            )
        )
        val stroke = g2d.stroke
        g2d.stroke = BasicStroke(4f)
        g2d.color = Color.darkGray
        g2d.draw(shadow.createTransformedShape(shape))
        g2d.color = Color.black
        g2d.draw(lowlight.createTransformedShape(shape))
        g2d.color = Color.white
        g2d.draw(highlight.createTransformedShape(shape))
        g2d.color = color
        g2d.draw(shape)
        g2d.stroke = stroke
    }

    override fun getIconWidth(): Int = 20

    override fun getIconHeight(): Int = 20
}
