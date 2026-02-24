/*
 * Created on Nov 3, 2005
 *
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.decorators

import com.google.common.base.Preconditions
import java.util.function.Function

/**
 * Provides node sizes that are spaced proportionally between min_size and max_size depending on
 *
 * @author Joshua O'Madadhain
 */
open class InterpolatingNodeSizeFunction<N>(
    private val values: Function<N, out Number>,
    min_size: Int,
    max_size: Int
) : Function<N, Int> {

    private var min: Double = 0.0
    private var max: Double = 0.0
    private var min_size: Int = 0
    private var size_diff: Int = 0

    init {
        Preconditions.checkArgument(min_size >= 0 && max_size >= 0, "sizes must be non-negative")
        Preconditions.checkArgument(min_size <= max_size, "min_size must be <= max_size")
        setMinSize(min_size)
        setMaxSize(max_size)
    }

    override fun apply(v: N): Int {
        val n = values.apply(v)
        var value = min
        if (n != null) {
            value = n.toDouble()
        }
        min = Math.min(this.min, value)
        max = Math.max(this.max, value)

        if (min == max) {
            return min_size
        }

        // interpolate between min and max sizes based on how big value is
        // with respect to min and max values
        return min_size + ((value - min) / (max - min) * size_diff).toInt()
    }

    fun setMinSize(min_size: Int) {
        this.min_size = min_size
    }

    fun setMaxSize(max_size: Int) {
        this.size_diff = max_size - this.min_size
    }
}
