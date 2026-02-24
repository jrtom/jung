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
import java.awt.geom.Area
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
object FourPassImageShaper {

    @JvmStatic
    fun getShape(image: BufferedImage): Shape {
        val area = Area(leftEdge(image))
        area.intersect(Area(bottomEdge(image)))
        area.intersect(Area(rightEdge(image)))
        area.intersect(Area(topEdge(image)))
        return area
    }

    /**
     * Checks to see if point p is on a line that passes thru points p1 and p2. If p is on the line,
     * extend the line segment so that it is from p1 to the location of p. If the point p is not on
     * the line, update my shape with a line extending to the old p2 location, make the old p2 the new
     * p1, and make p2 the old p
     */
    private fun detectLine(
        p1: Point2D, p2: Point2D, p: Point2D, line: Line2D, path: GeneralPath
    ): Point2D {
        // check for line
        // if p is on the line that extends thru p1 and p2
        if (line.ptLineDistSq(p) == 0.0) { // p is on the line p1,p2
            // extend line so that p2 is at p
            p2.setLocation(p)
        } else { // its not on the current line
            // start a new line from p2 to p
            p1.setLocation(p2)
            p2.setLocation(p)
            line.setLine(p1, p2)
            // end the ongoing path line at the new p1 (the old p2)
            path.lineTo(p1.x.toFloat(), p1.y.toFloat())
        }
        return p2
    }

    /**
     * trace the left side of the image
     */
    private fun leftEdge(image: BufferedImage): Shape {
        val path = GeneralPath()
        var p1: Point2D? = null
        var p2: Point2D? = null
        val line = Line2D.Float()
        var p: Point2D = Point2D.Float()
        var foundPointY = -1
        for (i in 0 until image.height) {
            for (j in 0 until image.width) {
                if ((image.getRGB(j, i) and 0xff000000.toInt()) != 0) {
                    p = Point2D.Float(j.toFloat(), i.toFloat())
                    foundPointY = i
                    break
                }
            }
            if (foundPointY >= 0) {
                if (p2 == null) {
                    p1 = Point2D.Float((image.width - 1).toFloat(), foundPointY.toFloat())
                    path.moveTo(p1.x, p1.y)
                    p2 = Point2D.Float()
                    p2.setLocation(p)
                } else {
                    p2 = detectLine(p1!!, p2, p, line, path)
                }
            }
        }
        path.lineTo(p.x.toFloat(), p.y.toFloat())
        if (foundPointY >= 0) {
            path.lineTo((image.width - 1).toFloat(), foundPointY.toFloat())
        }
        path.closePath()
        return path
    }

    /**
     * trace the bottom of the image
     */
    private fun bottomEdge(image: BufferedImage): Shape {
        val path = GeneralPath()
        var p1: Point2D? = null
        var p2: Point2D? = null
        val line = Line2D.Float()
        val p = Point2D.Float()
        var foundPointX = -1
        for (i in 0 until image.width) {
            for (j in image.height - 1 downTo 0) {
                if ((image.getRGB(i, j) and 0xff000000.toInt()) != 0) {
                    p.setLocation(i.toFloat(), j.toFloat())
                    foundPointX = i
                    break
                }
            }
            if (foundPointX >= 0) {
                if (p2 == null) {
                    p1 = Point2D.Float(foundPointX.toFloat(), 0f)
                    path.moveTo(p1.x, p1.y)
                    p2 = Point2D.Float()
                    p2.setLocation(p)
                } else {
                    p2 = detectLine(p1!!, p2, p, line, path)
                }
            }
        }
        path.lineTo(p.x, p.y)
        if (foundPointX >= 0) {
            path.lineTo(foundPointX.toFloat(), 0f)
        }
        path.closePath()
        return path
    }

    /**
     * trace the right side of the image
     */
    private fun rightEdge(image: BufferedImage): Shape {
        val path = GeneralPath()
        var p1: Point2D? = null
        var p2: Point2D? = null
        val line = Line2D.Float()
        val p = Point2D.Float()
        var foundPointY = -1
        for (i in image.height - 1 downTo 0) {
            for (j in image.width - 1 downTo 0) {
                if ((image.getRGB(j, i) and 0xff000000.toInt()) != 0) {
                    p.setLocation(j.toFloat(), i.toFloat())
                    foundPointY = i
                    break
                }
            }
            if (foundPointY >= 0) {
                if (p2 == null) {
                    p1 = Point2D.Float(0f, foundPointY.toFloat())
                    path.moveTo(p1.x, p1.y)
                    p2 = Point2D.Float()
                    p2.setLocation(p)
                } else {
                    p2 = detectLine(p1!!, p2, p, line, path)
                }
            }
        }
        path.lineTo(p.x, p.y)
        if (foundPointY >= 0) {
            path.lineTo(0f, foundPointY.toFloat())
        }
        path.closePath()
        return path
    }

    /**
     * trace the top of the image
     */
    private fun topEdge(image: BufferedImage): Shape {
        val path = GeneralPath()
        var p1: Point2D? = null
        var p2: Point2D? = null
        val line = Line2D.Float()
        val p = Point2D.Float()
        var foundPointX = -1
        for (i in image.width - 1 downTo 0) {
            for (j in 0 until image.height) {
                if ((image.getRGB(i, j) and 0xff000000.toInt()) != 0) {
                    p.setLocation(i.toFloat(), j.toFloat())
                    foundPointX = i
                    break
                }
            }
            if (foundPointX >= 0) {
                if (p2 == null) {
                    p1 = Point2D.Float(foundPointX.toFloat(), (image.height - 1).toFloat())
                    path.moveTo(p1.x, p1.y)
                    p2 = Point2D.Float()
                    p2.setLocation(p)
                } else {
                    p2 = detectLine(p1!!, p2, p, line, path)
                }
            }
        }
        path.lineTo(p.x, p.y)
        if (foundPointX >= 0) {
            path.lineTo(foundPointX.toFloat(), (image.height - 1).toFloat())
        }
        path.closePath()
        return path
    }
}
