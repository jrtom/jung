/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.annotations

import edu.uci.ics.jung.visualization.MultiLayerTransformer
import edu.uci.ics.jung.visualization.RenderContext
import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.transform.AffineTransformer
import edu.uci.ics.jung.visualization.transform.LensTransformer
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.util.Collections
import java.util.LinkedHashSet
import javax.swing.JComponent

/**
 * handles the actual drawing of annotations
 *
 * @author Tom Nelson - tomnelson@dev.java.net
 */
open class AnnotationPaintable(
    private val rc: RenderContext<*, *>,
    private val annotationRenderer: AnnotationRenderer
) : VisualizationServer.Paintable {

    private val annotations: MutableSet<Annotation<*>> = LinkedHashSet()
    private val transformer: AffineTransformer

    init {
        val mt = rc.getMultiLayerTransformer().getTransformer(MultiLayerTransformer.Layer.LAYOUT)
        transformer = when (mt) {
            is AffineTransformer -> mt
            is LensTransformer -> mt.delegate as AffineTransformer
            else -> throw IllegalStateException("Unexpected transformer type: ${mt::class.java}")
        }
    }

    fun add(annotation: Annotation<*>) {
        annotations.add(annotation)
    }

    fun remove(annotation: Annotation<*>) {
        annotations.remove(annotation)
    }

    /**
     * @return the annotations
     */
    fun getAnnotations(): Set<Annotation<*>> = Collections.unmodifiableSet(annotations)

    override fun paint(g: Graphics) {
        val g2d = g as Graphics2D
        val oldColor = g.getColor()
        for (annotation in annotations) {
            val ann = annotation.annotation
            if (ann is Shape) {
                val paint = annotation.paint
                val s = transformer.transform(ann)
                g2d.paint = paint
                if (annotation.isFill) {
                    g2d.fill(s)
                } else {
                    g2d.draw(s)
                }
            } else if (ann is String) {
                val p = annotation.location
                val component = prepareRenderer(rc, annotationRenderer, ann)
                component.foreground = annotation.paint as Color
                if (annotation.isFill) {
                    (component as JComponent).isOpaque = true
                    component.background = annotation.paint as Color
                    component.foreground = Color.black
                }
                val d = component.preferredSize
                val old = g2d.transform
                val base = AffineTransform(old)
                val xform = transformer.getTransform()

                val rotation = transformer.getRotation()
                // unrotate the annotation
                val unrotate = AffineTransform.getRotateInstance(-rotation, p.x, p.y)
                base.concatenate(xform)
                base.concatenate(unrotate)
                g2d.transform = base
                rc.getRendererPane().paintComponent(
                    g,
                    component,
                    rc.getScreenDevice(),
                    p.x.toInt(),
                    p.y.toInt(),
                    d.width,
                    d.height,
                    true
                )
                g2d.transform = old
            }
        }
        g.setColor(oldColor)
    }

    fun prepareRenderer(
        rc: RenderContext<*, *>, annotationRenderer: AnnotationRenderer, value: Any?
    ): Component =
        annotationRenderer.getAnnotationRendererComponent(rc.getScreenDevice(), value)

    override fun useTransform(): Boolean = true
}
