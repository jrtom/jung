/*
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.annotations

import edu.uci.ics.jung.visualization.MultiLayerTransformer
import edu.uci.ics.jung.visualization.RenderContext
import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin
import java.awt.Color
import java.awt.Cursor
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.event.InputEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.geom.Rectangle2D
import java.awt.geom.RectangularShape
import javax.swing.JComponent
import javax.swing.JOptionPane

/**
 * AnnotatingGraphMousePlugin can create Shape and Text annotations in a layer of the graph
 * visualization.
 *
 * @author Tom Nelson
 */
open class AnnotatingGraphMousePlugin<N : Any, E : Any> : AbstractGraphMousePlugin,
    MouseListener, MouseMotionListener {

    /** additional modifiers for the action of adding to an existing selection */
    protected val additionalModifiers: Int

    /** used to draw a Shape annotation */
    protected var _rectangularShape: RectangularShape = Rectangle2D.Float()

    /** the Paintable for the Shape annotation */
    protected val lensPaintable: VisualizationServer.Paintable

    /** a Paintable to store all Annotations */
    protected val annotationManager: AnnotationManager

    /** color for annotations */
    var annotationColor: Color = Color.cyan

    /** layer for annotations */
    var layer: Annotation.Layer = Annotation.Layer.LOWER

    var isFill: Boolean = false

    /** holds rendering transforms */
    protected val basicTransformer: MultiLayerTransformer

    /** holds rendering settings */
    protected val rc: RenderContext<N, E>

    /** set to true when the AnnotationPaintable has been added to the view component */
    protected var added: Boolean = false

    /**
     * Create an instance with defaults for primary (button 1) and secondary (button 1 + shift)
     * selection.
     *
     * @param rc the RenderContext for which this plugin will be used
     */
    constructor(rc: RenderContext<N, E>) :
        this(rc, InputEvent.BUTTON1_MASK, InputEvent.BUTTON1_MASK or InputEvent.SHIFT_MASK)

    /**
     * Create an instance with the specified primary and secondary selection mechanisms.
     *
     * @param rc the RenderContext for which this plugin will be used
     * @param selectionModifiers for primary selection
     * @param additionalModifiers for additional selection
     */
    constructor(
        rc: RenderContext<N, E>, selectionModifiers: Int, additionalModifiers: Int
    ) : super(selectionModifiers) {
        this.rc = rc
        this.basicTransformer = rc.getMultiLayerTransformer()
        this.additionalModifiers = additionalModifiers
        this.lensPaintable = LensPaintable()
        this.annotationManager = AnnotationManager(rc)
        this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    }

    /** the Paintable that draws a Shape annotation only while it is being created */
    private inner class LensPaintable : VisualizationServer.Paintable {
        override fun paint(g: Graphics) {
            val oldColor = g.color
            g.color = annotationColor
            (g as Graphics2D).draw(_rectangularShape)
            g.color = oldColor
        }

        override fun useTransform(): Boolean = false
    }

    /**
     * Sets the location for an Annotation. Will either pop up a dialog to prompt for text input for a
     * text annotation, or begin the process of drawing a Shape annotation
     */
    @Suppress("UNCHECKED_CAST")
    override fun mousePressed(e: MouseEvent) {
        val vv = e.source as VisualizationViewer<N, E>
        down = e.point

        if (!added) {
            vv.addPreRenderPaintable(annotationManager.lowerAnnotationPaintable)
            vv.addPostRenderPaintable(annotationManager.upperAnnotationPaintable)
            added = true
        }

        if (e.isPopupTrigger) {
            val annotationString = JOptionPane.showInputDialog(vv, "Annotation:")
            if (annotationString != null && annotationString.isNotEmpty()) {
                val p = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(down!!)
                val annotation = Annotation(annotationString, layer, annotationColor, isFill, p)
                annotationManager.add(layer, annotation)
            }
        } else if (e.modifiers == additionalModifiers) {
            val annotation = annotationManager.getAnnotation(down!!)
            annotationManager.remove(annotation)
        } else if (e.modifiers == modifiers) {
            _rectangularShape.setFrameFromDiagonal(down, down)
            vv.addPostRenderPaintable(lensPaintable)
        }
        vv.repaint()
    }

    /** Completes the process of adding a Shape annotation and removed the transient paintable */
    @Suppress("UNCHECKED_CAST")
    override fun mouseReleased(e: MouseEvent) {
        val vv = e.source as VisualizationViewer<N, E>
        if (e.isPopupTrigger) {
            val annotationString = JOptionPane.showInputDialog(vv, "Annotation:")
            if (annotationString != null && annotationString.isNotEmpty()) {
                val p = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(down!!)
                val annotation = Annotation(annotationString, layer, annotationColor, isFill, p)
                annotationManager.add(layer, annotation)
            }
        } else if (e.modifiers == modifiers) {
            if (down != null) {
                val out = e.point
                val arect = _rectangularShape.clone() as RectangularShape
                arect.setFrameFromDiagonal(down, out)
                val s = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(arect)
                val annotation = Annotation<Shape>(s, layer, annotationColor, isFill, out)
                annotationManager.add(layer, annotation)
            }
        }
        down = null
        vv.removePostRenderPaintable(lensPaintable)
        vv.repaint()
    }

    /**
     * Draws the transient Paintable that will become a Shape annotation when the mouse button is
     * released
     */
    @Suppress("UNCHECKED_CAST")
    override fun mouseDragged(e: MouseEvent) {
        val vv = e.source as VisualizationViewer<N, E>
        val out = e.point
        if (e.modifiers == additionalModifiers) {
            _rectangularShape.setFrameFromDiagonal(down, out)
        } else if (e.modifiers == modifiers) {
            _rectangularShape.setFrameFromDiagonal(down, out)
        }
        _rectangularShape.setFrameFromDiagonal(down, out)
        vv.repaint()
    }

    override fun mouseClicked(e: MouseEvent) {}

    override fun mouseEntered(e: MouseEvent) {
        val c = e.source as JComponent
        c.cursor = cursor
    }

    override fun mouseExited(e: MouseEvent) {
        val c = e.source as JComponent
        c.cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
    }

    override fun mouseMoved(e: MouseEvent) {}

    /**
     * @return the rect
     */
    fun getRectangularShape(): RectangularShape = _rectangularShape

    /**
     * @param rect the rect to set
     */
    fun setRectangularShape(rect: RectangularShape) {
        this._rectangularShape = rect
    }
}
