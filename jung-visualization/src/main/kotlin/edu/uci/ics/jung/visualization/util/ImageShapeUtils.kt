/*
 * Copyright (c) 2015, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Nov 7, 2015
 */
package edu.uci.ics.jung.visualization.util

import edu.uci.ics.jung.visualization.FourPassImageShaper
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

object ImageShapeUtils {

    /**
     * Given the fileName of an image, possibly with a transparent background, return the Shape of the
     * opaque part of the image.
     *
     * @param fileName name of the image, loaded from the classpath
     * @return the Shape
     */
    @JvmStatic
    fun getShape(fileName: String): Shape = getShape(fileName, Int.MAX_VALUE)

    /**
     * Given the fileName of an image, possibly with a transparent background, return the Shape of the
     * opaque part of the image.
     *
     * @param fileName name of the image, loaded from the classpath
     * @param max the maximum dimension of the traced shape
     * @return the Shape
     * @see getShape(Image, Int)
     */
    @JvmStatic
    fun getShape(fileName: String, max: Int): Shape {
        var image: BufferedImage? = null
        try {
            image = ImageIO.read(ImageShapeUtils::class.java.getResource(fileName))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return getShape(image!!, max)
    }

    /**
     * Given an image, possibly with a transparent background, return the Shape of the opaque part of
     * the image.
     *
     * @param image the image whose shape is to be returned
     * @return the Shape
     */
    @JvmStatic
    fun getShape(image: Image): Shape = getShape(image, Int.MAX_VALUE)

    @JvmStatic
    fun getShape(image: Image, max: Int): Shape {
        val bi = BufferedImage(
            image.getWidth(null),
            image.getHeight(null),
            BufferedImage.TYPE_INT_ARGB
        )
        val g = bi.createGraphics()
        g.drawImage(image, 0, 0, null)
        g.dispose()
        return getShape(bi, max)
    }

    /**
     * Given an image, possibly with a transparent background, return the Shape of the opaque part of
     * the image.
     *
     * If the image is larger than max in either direction, scale the image down to max-by-max, do
     * the trace (on fewer points) then scale the resulting shape back up to the layoutSize of the
     * original image.
     *
     * @param image the image to trace
     * @param max used to restrict number of points in the resulting shape
     * @return the Shape
     */
    @JvmStatic
    fun getShape(image: BufferedImage, max: Int): Shape {
        val width = image.width.toFloat()
        val height = image.height.toFloat()
        if (width > max || height > max) {
            val smaller = BufferedImage(max, max, BufferedImage.TYPE_INT_ARGB)
            val g = smaller.createGraphics()
            val at = AffineTransform.getScaleInstance((max / width).toDouble(), (max / height).toDouble())
            val back = AffineTransform.getScaleInstance((width / max).toDouble(), (height / max).toDouble())
            val g2 = g as Graphics2D
            g2.drawImage(image, at, null)
            g2.dispose()
            return back.createTransformedShape(getShape(smaller))
        } else {
            return FourPassImageShaper.getShape(image)
        }
    }
}
