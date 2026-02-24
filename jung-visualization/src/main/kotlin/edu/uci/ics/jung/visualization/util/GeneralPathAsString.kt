package edu.uci.ics.jung.visualization.util

import java.awt.geom.GeneralPath
import java.awt.geom.PathIterator
import java.awt.geom.Point2D

object GeneralPathAsString {

    @JvmStatic
    fun toString(newPath: GeneralPath): String {
        val sb = StringBuilder()
        val coords = FloatArray(6)
        val iterator = newPath.getPathIterator(null)
        while (!iterator.isDone) {
            val type = iterator.currentSegment(coords)
            when (type) {
                PathIterator.SEG_MOVETO -> {
                    val p = Point2D.Float(coords[0], coords[1])
                    sb.append("moveTo $p--")
                }
                PathIterator.SEG_LINETO -> {
                    val p = Point2D.Float(coords[0], coords[1])
                    sb.append("lineTo $p--")
                }
                PathIterator.SEG_QUADTO -> {
                    val p = Point2D.Float(coords[0], coords[1])
                    val q = Point2D.Float(coords[2], coords[3])
                    sb.append("quadTo $p controlled by $q")
                }
                PathIterator.SEG_CUBICTO -> {
                    val p = Point2D.Float(coords[0], coords[1])
                    val q = Point2D.Float(coords[2], coords[3])
                    val r = Point2D.Float(coords[4], coords[5])
                    sb.append("cubeTo $p controlled by $q,$r")
                }
                PathIterator.SEG_CLOSE -> {
                    newPath.closePath()
                    sb.append("close")
                }
            }
            iterator.next()
        }
        return sb.toString()
    }
}
