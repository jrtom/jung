/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.annotations

import java.awt.Paint
import java.awt.geom.Point2D

/**
 * stores an annotation, either a shape or a string
 *
 * @author Tom Nelson - tomnelson@dev.java.net
 * @param T the type of the annotation object
 */
open class Annotation<T>(
    var annotation: T,
    var layer: Layer,
    var paint: Paint,
    var isFill: Boolean,
    var location: Point2D
) {
    enum class Layer {
        LOWER,
        UPPER
    }
}
