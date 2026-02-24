/*
 * Created on Jul 19, 2005
 *
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.layout.util

import edu.uci.ics.jung.layout.model.Point
import java.util.Date
import java.util.Random
import java.util.function.Function

/**
 * Provides a random node location within the bounds of the width and height. This provides a random
 * location for unmapped nodes the first time they are accessed.
 *
 * **Note**: the generated values are not cached, so animate() will generate a new random
 * location for the passed node every time it is called. If you want a consistent value, wrap this
 * layout's generated values in a instance.
 *
 * @author Tom Nelson
 * @param N the node type
 */
class RandomLocationTransformer<N : Any>(
  private val origin: Origin = Origin.NE,
  protected var width: Double,
  protected var height: Double,
  seed: Long = Date().time
) : Function<N, Point> {

  protected var depth: Double = 0.0
  protected var random: Random = Random(seed)

  enum class Origin {
    NE,
    CENTER
  }

  /**
   * Creates an instance with the specified layoutSize which uses the current time as the random
   * seed.
   *
   * @param width, height the layoutSize of the layout area
   */
  constructor(width: Double, height: Double) : this(Origin.NE, width, height, Date().time)

  /**
   * Creates an instance with the specified dimension and random seed.
   *
   * @param seed the seed for the internal random number generator
   */
  constructor(width: Double, height: Double, seed: Long) : this(Origin.NE, width, height, seed)

  private fun applyNE(node: N): Point =
    Point.of(random.nextDouble() * width, random.nextDouble() * height)

  private fun applyCenter(node: N): Point {
    val radiusX = width / 2
    val radiusY = height / 2
    return Point.of(random.nextDouble() * width - radiusX, random.nextDouble() * height - radiusY)
  }

  override fun apply(node: N): Point = when (origin) {
    Origin.NE -> applyNE(node)
    Origin.CENTER -> applyCenter(node)
  }
}
