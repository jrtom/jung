/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jun 17, 2005
 */
package edu.uci.ics.jung.visualization

import java.awt.Shape
import java.awt.geom.GeneralPath
import java.awt.geom.Line2D
import java.awt.geom.Point2D
import java.awt.image.BufferedImage

/**
 * Provides Supplier methods that, given a BufferedImage, an Image, or the fileName of an image,
 * will return a java.awt.Shape that is the contiguous traced outline of the opaque part of the
 * image. This could be used to define an image for use in a Node, where the shape used for picking
 * and edge-arrow placement follows the opaque part of an image that has a transparent background.
 * The methods try to detect lines in order to minimize points in the path
 *
 * @author Tom Nelson
 */
object PivotingImageShaper {

    /** the number of pixels to skip while sampling the images edges */
    @JvmStatic
    var sample: Int = 1

    /** the first x coordinate of the shape. Used to discern when we are done */
    private var firstx: Int = 0

    /**
     * Given an image, possibly with a transparent background, return the Shape of the opaque part of
     * the image
     *
     * @param image the image whose shape is being returned
     * @return the Shape
     */
    @JvmStatic
    fun getShape(image: BufferedImage): Shape {
        firstx = 0
        return leftEdge(image, GeneralPath())
    }

    private fun detectLine(
        p1: Point2D, p2: Point2D?, p: Point2D, line: Line2D, path: GeneralPath
    ): Point2D {
        var currentP2 = p2
        if (currentP2 == null) {
            currentP2 = p
            line.setLine(p1, currentP2)
        } else if (line.ptLineDistSq(p) < 1) { // its on the line
            // make it p2
            currentP2.setLocation(p)
        } else { // its not on the current line
            p1.setLocation(currentP2)
            currentP2.setLocation(p)
            line.setLine(p1, currentP2)
            path.lineTo(p1.x.toFloat(), p1.y.toFloat())
        }
        return currentP2
    }

    /**
     * trace the left side of the image
     */
    private fun leftEdge(image: BufferedImage, path: GeneralPath): Shape {
        var lastj = 0
        var p1: Point2D? = null
        var p2: Point2D? = null
        val line = Line2D.Float()
        var i = 0
        while (i < image.height) {
            var aPointExistsOnThisLine = false
            var j = 0
            while (j < image.width) {
                if ((image.getRGB(j, i) and 0xff000000.toInt()) != 0) {
                    val p = Point2D.Float(j.toFloat(), i.toFloat())
                    aPointExistsOnThisLine = true
                    if (path.currentPoint != null) {
                        p2 = detectLine(p1!!, p2, p, line, path)
                    } else {
                        path.moveTo(j.toFloat(), i.toFloat())
                        firstx = j
                        p1 = p
                    }
                    lastj = j
                    break
                }
                j += sample
            }
            if (!aPointExistsOnThisLine) {
                break
            }
            i += sample
        }
        return bottomEdge(image, path, lastj)
    }

    /**
     * trace the bottom of the image
     */
    private fun bottomEdge(image: BufferedImage, path: GeneralPath, start: Int): Shape {
        var lastj = 0
        var p1: Point2D = path.currentPoint
        var p2: Point2D? = null
        val line = Line2D.Float()
        var i = start
        while (i < image.width) {
            var aPointExistsOnThisLine = false
            var j = image.height - 1
            while (j >= 0) {
                if ((image.getRGB(i, j) and 0xff000000.toInt()) != 0) {
                    val p = Point2D.Float(i.toFloat(), j.toFloat())
                    aPointExistsOnThisLine = true
                    p2 = detectLine(p1, p2, p, line, path)
                    lastj = j
                    break
                }
                j -= sample
            }
            if (!aPointExistsOnThisLine) {
                break
            }
            i += sample
        }
        return rightEdge(image, path, lastj)
    }

    /**
     * trace the right side of the image
     */
    private fun rightEdge(image: BufferedImage, path: GeneralPath, start: Int): Shape {
        var lastj = 0
        var p1: Point2D = path.currentPoint
        var p2: Point2D? = null
        val line = Line2D.Float()
        var i = start
        while (i >= 0) {
            var aPointExistsOnThisLine = false
            var j = image.width - 1
            while (j >= 0) {
                if ((image.getRGB(j, i) and 0xff000000.toInt()) != 0) {
                    val p = Point2D.Float(j.toFloat(), i.toFloat())
                    aPointExistsOnThisLine = true
                    p2 = detectLine(p1, p2, p, line, path)
                    lastj = j
                    break
                }
                j -= sample
            }
            if (!aPointExistsOnThisLine) {
                break
            }
            i -= sample
        }
        return topEdge(image, path, lastj)
    }

    /**
     * trace the top of the image
     */
    private fun topEdge(image: BufferedImage, path: GeneralPath, start: Int): Shape {
        var p1: Point2D = path.currentPoint
        var p2: Point2D? = null
        val line = Line2D.Float()
        var i = start
        while (i >= firstx) {
            var aPointExistsOnThisLine = false
            var j = 0
            while (j < image.height) {
                if ((image.getRGB(i, j) and 0xff000000.toInt()) != 0) {
                    val p = Point2D.Float(i.toFloat(), j.toFloat())
                    aPointExistsOnThisLine = true
                    p2 = detectLine(p1, p2, p, line, path)
                    break
                }
                j += sample
            }
            if (!aPointExistsOnThisLine) {
                break
            }
            i -= sample
        }
        path.closePath()
        return path
    }
}
