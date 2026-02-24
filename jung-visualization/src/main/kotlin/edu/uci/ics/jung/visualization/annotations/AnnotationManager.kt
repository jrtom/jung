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
import edu.uci.ics.jung.visualization.transform.AffineTransformer
import edu.uci.ics.jung.visualization.transform.LensTransformer
import java.awt.Component
import java.awt.Dimension
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D

/**
 * handles the selection of annotations, and the support for the tools to draw them at specific
 * layers.
 *
 * @author Tom Nelson - tomnelson@dev.java.net
 */
open class AnnotationManager(private val rc: RenderContext<*, *>) {

    private val annotationRenderer: AnnotationRenderer = AnnotationRenderer()
    val lowerAnnotationPaintable: AnnotationPaintable = AnnotationPaintable(rc, annotationRenderer)
    val upperAnnotationPaintable: AnnotationPaintable = AnnotationPaintable(rc, annotationRenderer)
    private val transformer: AffineTransformer

    init {
        val mt = rc.getMultiLayerTransformer().getTransformer(MultiLayerTransformer.Layer.LAYOUT)
        transformer = when (mt) {
            is AffineTransformer -> mt
            is LensTransformer -> mt.delegate as AffineTransformer
            else -> throw IllegalStateException("Unexpected transformer type: ${mt::class.java}")
        }
    }

    fun getAnnotationPaintable(layer: Annotation.Layer): AnnotationPaintable? {
        return when (layer) {
            Annotation.Layer.LOWER -> lowerAnnotationPaintable
            Annotation.Layer.UPPER -> upperAnnotationPaintable
        }
    }

    fun add(layer: Annotation.Layer, annotation: Annotation<*>) {
        when (layer) {
            Annotation.Layer.LOWER -> lowerAnnotationPaintable.add(annotation)
            Annotation.Layer.UPPER -> upperAnnotationPaintable.add(annotation)
        }
    }

    fun remove(annotation: Annotation<*>?) {
        if (annotation == null) return
        lowerAnnotationPaintable.remove(annotation)
        upperAnnotationPaintable.remove(annotation)
    }

    fun getAnnotation(p: Point2D): Annotation<*>? {
        val annotations = HashSet<Annotation<*>>(lowerAnnotationPaintable.getAnnotations())
        annotations.addAll(upperAnnotationPaintable.getAnnotations())
        return getAnnotation(p, annotations)
    }

    fun getAnnotation(p: Point2D, annotations: Collection<Annotation<*>>): Annotation<*>? {
        var closestDistance = Double.MAX_VALUE
        var closestAnnotation: Annotation<*>? = null
        for (annotation in annotations) {
            val ann = annotation.annotation
            if (ann is Shape) {
                val ip = rc.getMultiLayerTransformer().inverseTransform(p)
                val shape = ann
                if (shape.contains(ip)) {
                    val shapeBounds = shape.bounds2D
                    val shapeCenter = Point2D.Double(shapeBounds.centerX, shapeBounds.centerY)
                    val distanceSq = shapeCenter.distanceSq(ip)
                    if (distanceSq < closestDistance) {
                        closestDistance = distanceSq
                        closestAnnotation = annotation
                    }
                }
            } else if (ann is String) {
                val ip = rc.getMultiLayerTransformer()
                    .inverseTransform(MultiLayerTransformer.Layer.VIEW, p)
                val ap = annotation.location
                val label = ann
                val component = prepareRenderer(rc, annotationRenderer, label)

                val base = AffineTransform(transformer.getTransform())
                val rotation = transformer.getRotation()
                // unrotate the annotation
                val unrotate = AffineTransform.getRotateInstance(-rotation, ap.x, ap.y)
                base.concatenate(unrotate)

                val d = component.preferredSize
                val componentBounds = Rectangle2D.Double(
                    ap.x, ap.y, d.width.toDouble(), d.height.toDouble()
                )

                val componentBoundsShape = base.createTransformedShape(componentBounds)
                val componentCenter = Point2D.Double(
                    componentBoundsShape.bounds.centerX,
                    componentBoundsShape.bounds.centerY
                )
                if (componentBoundsShape.contains(ip)) {
                    val distanceSq = componentCenter.distanceSq(ip)
                    if (distanceSq < closestDistance) {
                        closestDistance = distanceSq
                        closestAnnotation = annotation
                    }
                }
            }
        }
        return closestAnnotation
    }

    fun prepareRenderer(
        rc: RenderContext<*, *>, annotationRenderer: AnnotationRenderer, value: Any?
    ): Component =
        annotationRenderer.getAnnotationRendererComponent(rc.getScreenDevice(), value)
}
