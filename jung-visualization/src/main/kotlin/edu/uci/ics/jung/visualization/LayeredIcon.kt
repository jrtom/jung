package edu.uci.ics.jung.visualization

import java.awt.Component
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Image
import javax.swing.Icon
import javax.swing.ImageIcon

/**
 * An icon that is made up of a collection of Icons. They are rendered in layers starting with the
 * first Icon added (from the constructor).
 *
 * @author Tom Nelson
 */
open class LayeredIcon(image: Image) : ImageIcon(image) {

    private val iconSet: MutableSet<Icon> = LinkedHashSet()

    override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
        super.paintIcon(c, g, x, y)
        val d = Dimension(iconWidth, iconHeight)
        for (icon in iconSet) {
            val id = Dimension(icon.iconWidth, icon.iconHeight)
            val dx = (d.width - id.width) / 2
            val dy = (d.height - id.height) / 2
            icon.paintIcon(c, g, x + dx, y + dy)
        }
    }

    fun add(icon: Icon) {
        iconSet.add(icon)
    }

    fun remove(icon: Icon): Boolean = iconSet.remove(icon)
}
