/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Apr 14, 2005
 */
package edu.uci.ics.jung.visualization.renderers

import java.awt.Color
import java.awt.Component
import java.awt.Font
import java.awt.Rectangle
import java.io.Serializable
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.border.Border
import javax.swing.border.EmptyBorder

/**
 * DefaultNodeLabelRenderer is similar to the cell renderers used by the JTable and JTree JFC
 * classes.
 *
 * @author Tom Nelson
 */
open class DefaultNodeLabelRenderer(
    protected var pickedNodeLabelColor: Color
) : JLabel(), NodeLabelRenderer, Serializable {

    companion object {
        private val noFocusBorder: Border = EmptyBorder(0, 0, 0, 0)
    }

    init {
        isOpaque = true
        border = noFocusBorder
    }

    /**
     * Overrides `JComponent.setForeground` to assign the unselected-foreground color to
     * the specified color.
     *
     * @param c set the foreground color to this value
     */
    override fun setForeground(c: Color?) {
        super.setForeground(c)
    }

    /**
     * Overrides `JComponent.setBackground` to assign the unselected-background color to
     * the specified color.
     *
     * @param c set the background color to this value
     */
    override fun setBackground(c: Color?) {
        super.setBackground(c)
    }

    /**
     * Notification from the `UIManager` that the look and feel has changed. Replaces the
     * current UI object with the latest version from the `UIManager`.
     *
     * @see JComponent.updateUI
     */
    override fun updateUI() {
        super.updateUI()
        foreground = null
        background = null
    }

    /**
     * Returns the default label renderer for a Node
     *
     * @param vv the `VisualizationViewer` to render on
     * @param value the value to assign to the label for `Node`
     * @param node the `Node`
     * @return the default label renderer
     */
    override fun <N> getNodeLabelRendererComponent(
        vv: JComponent,
        value: Any?,
        font: Font?,
        isSelected: Boolean,
        node: N
    ): Component {
        super.setForeground(vv.foreground)
        if (isSelected) {
            foreground = pickedNodeLabelColor
        }
        super.setBackground(vv.background)
        if (font != null) {
            setFont(font)
        } else {
            setFont(vv.font)
        }
        icon = null
        border = noFocusBorder
        setValue(value)
        return this
    }

    /**
     * Overridden for performance reasons. See the Implementation Note for more information.
     */
    override fun isOpaque(): Boolean {
        val back = background
        var p = parent
        if (p != null) {
            p = p.parent
        }
        val colorMatch = back != null && p != null && back == p.background && p.isOpaque
        return !colorMatch && super.isOpaque()
    }

    /**
     * Overridden for performance reasons.
     */
    override fun validate() {}

    /**
     * Overridden for performance reasons.
     */
    override fun revalidate() {}

    /**
     * Overridden for performance reasons.
     */
    override fun repaint(tm: Long, x: Int, y: Int, width: Int, height: Int) {}

    /**
     * Overridden for performance reasons.
     */
    override fun repaint(r: Rectangle?) {}

    /**
     * Overridden for performance reasons.
     */
    override fun firePropertyChange(propertyName: String?, oldValue: Any?, newValue: Any?) {
        // Strings get interned...
        if (propertyName === "text") {
            super.firePropertyChange(propertyName, oldValue, newValue)
        }
    }

    /**
     * Overridden for performance reasons.
     */
    override fun firePropertyChange(propertyName: String?, oldValue: Boolean, newValue: Boolean) {}

    /**
     * Sets the `String` object for the cell being rendered to `value`.
     *
     * @param value the string value for this cell; if value is `null` it sets the text
     *     value to an empty string
     * @see JLabel.setText
     */
    protected open fun setValue(value: Any?) {
        text = value?.toString() ?: ""
    }
}
