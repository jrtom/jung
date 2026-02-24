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

import edu.uci.ics.jung.visualization.util.NodeShapeFactory
import java.util.function.Function

/**
 * @author Joshua O'Madadhain
 */
abstract class AbstractNodeShapeFunction<N> @JvmOverloads constructor(
    protected var vsf: Function<in N, Int> = Function { DEFAULT_SIZE },
    protected var varf: Function<in N, Float> = Function { DEFAULT_ASPECT_RATIO }
) : SettableNodeShapeFunction<N> {

    protected var factory: NodeShapeFactory<N> = NodeShapeFactory(vsf, varf)

    override fun setSizeTransformer(vsf: Function<N, Int>) {
        this.vsf = vsf
        factory = NodeShapeFactory(vsf, varf)
    }

    override fun setAspectRatioTransformer(varf: Function<N, Float>) {
        this.varf = varf
        factory = NodeShapeFactory(vsf, varf)
    }

    companion object {
        @JvmField
        val DEFAULT_SIZE: Int = 8

        @JvmField
        val DEFAULT_ASPECT_RATIO: Float = 1.0f
    }
}
