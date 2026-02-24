/*
 * Created on Jul 16, 2004
 *
 * Copyright (c) 2004, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.decorators

import java.awt.Shape
import java.util.function.Function

/**
 * @author Joshua O'Madadhain
 */
open class EllipseNodeShapeFunction<N> : AbstractNodeShapeFunction<N>, Function<N, Shape> {

    constructor() : super()

    constructor(vsf: Function<N, Int>, varf: Function<N, Float>) : super(vsf, varf)

    override fun apply(v: N): Shape = factory.getEllipse(v)
}
