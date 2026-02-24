/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.annotations

import java.awt.Color
import java.awt.Component
import java.awt.Rectangle
import java.io.Serializable
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.border.EmptyBorder

/**
 * AnnotationRenderer is similar to the cell renderers used by the JTable and JTree JFC classes.
 *
 * @author Tom Nelson
 */
@Suppress("serial")
open class AnnotationRenderer : JLabel(), Serializable {

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
     * Returns the default label renderer.
     *
     * @param vv the `VisualizationViewer` to render on
     * @param value the value to assign to the label
     * @return the default label renderer
     */
    fun getAnnotationRendererComponent(vv: JComponent, value: Any?): Component {
        super.setForeground(vv.foreground)
        super.setBackground(vv.background)

        font = vv.font
        icon = null
        border = noFocusBorder
        setValue(value)
        return this
    }

    /*
     * The following methods are overridden as a performance measure to
     * prune code-paths that are often called in the case of renders
     * but which we know are unnecessary.
     */

    /**
     * Overridden for performance reasons.
     */
    override fun isOpaque(): Boolean {
        val back = background
        val p = parent
        val pp = p?.parent
        val colorMatch = back != null && pp != null && back == pp.background && pp.isOpaque
        return !colorMatch && super.isOpaque()
    }

    /** Overridden for performance reasons. */
    override fun validate() {}

    /** Overridden for performance reasons. */
    override fun revalidate() {}

    /** Overridden for performance reasons. */
    override fun repaint(tm: Long, x: Int, y: Int, width: Int, height: Int) {}

    /** Overridden for performance reasons. */
    override fun repaint(r: Rectangle) {}

    /** Overridden for performance reasons. */
    override fun firePropertyChange(propertyName: String, oldValue: Any?, newValue: Any?) {
        // Strings get interned...
        if (propertyName === "text") {
            super.firePropertyChange(propertyName, oldValue, newValue)
        }
    }

    /** Overridden for performance reasons. */
    override fun firePropertyChange(propertyName: String, oldValue: Boolean, newValue: Boolean) {}

    /**
     * Sets the `String` object for the cell being rendered to `value`.
     *
     * @param value the string value for this cell; if value is `null` it sets the text
     *     value to an empty string
     * @see JLabel.setText
     */
    protected fun setValue(value: Any?) {
        text = value?.toString() ?: ""
    }

    companion object {
        @JvmStatic
        protected val noFocusBorder = EmptyBorder(0, 0, 0, 0)
    }
}
