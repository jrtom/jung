/*
 * Created on Aug 5, 2007
 *
 * Copyright (c) 2007, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.util

import java.util.function.Function

/**
 * An interface for classes that can set the value to be returned (from `transform()`)
 * when invoked on a given input.
 *
 * @author Joshua O'Madadhain
 */
interface SettableTransformer<I, O> : Function<I, O> {
  /**
   * Sets the value (`output`) to be returned by a call to `transform(input)`.
   *
   * @param input the value whose output value is being specified
   * @param output the output value for [input]
   */
  fun set(input: I, output: O)
}
