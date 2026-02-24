package edu.uci.ics.jung.visualization

import edu.uci.ics.jung.visualization.util.ImageShapeUtils
import java.awt.image.BufferedImage
import junit.framework.TestCase

class TestImageShaper : TestCase() {

  lateinit var image: BufferedImage

  override fun setUp() {
    super.setUp()
    val width = 6
    val height = 5
    image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    for (i in 0 until height) {
      for (j in 0 until width) {
        image.setRGB(j, i, 0x00000000)
      }
    }
    image.setRGB(3, 1, 0xffffffff.toInt())
    image.setRGB(4, 1, 0xffffffff.toInt())
    image.setRGB(2, 2, 0xffffffff.toInt())
    image.setRGB(4, 2, 0xffffffff.toInt())
    image.setRGB(1, 3, 0xffffffff.toInt())
    image.setRGB(2, 3, 0xffffffff.toInt())
    image.setRGB(3, 3, 0xffffffff.toInt())
    image.setRGB(4, 3, 0xffffffff.toInt())
  }

  fun testShaper() {
    val shape = ImageShapeUtils.getShape(image, 30)
    val seg = FloatArray(6)
    val pathIterator = shape.getPathIterator(null, 1.0)
    while (!pathIterator.isDone) {
      val ret = pathIterator.currentSegment(seg)
      pathIterator.next()
    }
  }
}
