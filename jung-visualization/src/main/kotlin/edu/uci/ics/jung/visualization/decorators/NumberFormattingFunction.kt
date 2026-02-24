/*
 * Created on Feb 16, 2009
 *
 * Copyright (c) 2009, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.decorators

import java.text.NumberFormat
import java.util.function.Function

/**
 * Transforms inputs to String representations by chaining an input `Number`-generating
 * `Function` with an internal `NumberFormat` instance.
 *
 * @author Joshua O'Madadhain
 */
open class NumberFormattingFunction<T>(
    private val values: Function<T, out Number>
) : Function<T, String> {

    private val formatter: NumberFormat = NumberFormat.getInstance()

    /** Returns a formatted string for the input. */
    override fun apply(input: T): String = formatter.format(values.apply(input))
}
