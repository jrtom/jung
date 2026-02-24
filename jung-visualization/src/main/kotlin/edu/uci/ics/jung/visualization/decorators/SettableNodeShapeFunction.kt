/*
 * Created on Jul 18, 2004
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
interface SettableNodeShapeFunction<N> : Function<N, Shape> {
    fun setSizeTransformer(vsf: Function<N, Int>)
    fun setAspectRatioTransformer(varf: Function<N, Float>)
}
