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
import java.awt.Shape
import java.awt.event.ActionListener
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D
import java.awt.geom.RectangularShape
import java.awt.geom.RoundRectangle2D
import javax.swing.DefaultListCellRenderer
import javax.swing.JButton
import javax.swing.JColorChooser
import javax.swing.JComboBox
import javax.swing.JList
import javax.swing.JToggleButton
import javax.swing.JToolBar

/**
 * a collection of controls for annotations. allows selection of colors, shapes, etc
 *
 * @author Tom Nelson
 */
open class AnnotationControls<N : Any, E : Any>(
    private val annotatingPlugin: AnnotatingGraphMousePlugin<N, E>
) {

    fun getShapeBox(): JComboBox<Shape> {
        val shapeBox = JComboBox(
            arrayOf<Shape>(
                Rectangle2D.Double(),
                RoundRectangle2D.Double(0.0, 0.0, 0.0, 0.0, 50.0, 50.0),
                Ellipse2D.Double()
            )
        )
        shapeBox.renderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                hasFocus: Boolean
            ): Component {
                var valueString = value.toString()
                valueString = valueString.substring(0, valueString.indexOf("2D"))
                valueString = valueString.substring(valueString.lastIndexOf('.') + 1)
                return super.getListCellRendererComponent(list, valueString, index, isSelected, hasFocus)
            }
        }
        shapeBox.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                annotatingPlugin.setRectangularShape(e.item as RectangularShape)
            }
        }
        return shapeBox
    }

    fun getColorChooserButton(): JButton {
        val colorChooser = JButton("Color")
        colorChooser.foreground = annotatingPlugin.annotationColor
        colorChooser.addActionListener(ActionListener {
            val color = JColorChooser.showDialog(
                colorChooser, "Annotation Color", colorChooser.foreground
            )
            annotatingPlugin.annotationColor = color
            colorChooser.foreground = color
        })
        return colorChooser
    }

    fun getLayerBox(): JComboBox<Annotation.Layer> {
        val layerBox = JComboBox(arrayOf(Annotation.Layer.LOWER, Annotation.Layer.UPPER))
        layerBox.addItemListener(object : ItemListener {
            override fun itemStateChanged(e: ItemEvent) {
                if (e.stateChange == ItemEvent.SELECTED) {
                    annotatingPlugin.layer = e.item as Annotation.Layer
                }
            }
        })
        return layerBox
    }

    fun getFillButton(): JToggleButton {
        val fillButton = JToggleButton("Fill")
        fillButton.addItemListener(object : ItemListener {
            override fun itemStateChanged(e: ItemEvent) {
                annotatingPlugin.isFill = e.stateChange == ItemEvent.SELECTED
            }
        })
        return fillButton
    }

    fun getAnnotationsToolBar(): JToolBar {
        val toolBar = JToolBar()
        toolBar.add(this.getShapeBox())
        toolBar.add(this.getColorChooserButton())
        toolBar.add(this.getFillButton())
        toolBar.add(this.getLayerBox())
        return toolBar
    }
}
