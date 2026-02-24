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

/**
 * A `SettableTransformer` that operates on an underlying `Map` instance.
 * Similar to `MapTransformer`.
 *
 * @author Joshua O'Madadhain
 */
class MapSettableTransformer<I, O>(
  protected val map: MutableMap<I, O>
) : SettableTransformer<I, O> {

  override fun apply(input: I): O = map[input] as O

  override fun set(input: I, output: O) {
    map[input] = output
  }
}
