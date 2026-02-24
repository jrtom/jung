/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.util

import com.google.common.base.Preconditions

/**
 * Provides basic infrastructure for iterative algorithms. Services provided include:
 *
 *  * storage of current and max iteration count
 *  * framework for initialization, iterative evaluation, and finalization
 *  * test for convergence
 *  * etc.
 *
 * Algorithms that subclass this class are typically used in the following way:
 *
 * ```
 * val foo = FooAlgorithm(...)
 * foo.setMaximumIterations(100) // set up conditions
 * ...
 * foo.evaluate() // key method which initiates iterative process
 * foo.getSomeResult()
 * ```
 *
 * @author Scott White (originally written by Didier Besset)
 */
abstract class IterativeProcess : IterativeContext {
  /** Number of iterations performed. */
  var iterations: Int = 0
    private set

  /** Maximum allowed number of iterations. */
  var maximumIterations: Int = 50
    private set

  /** Desired precision. */
  var desiredPrecision: Double = Double.MIN_VALUE
    private set

  /** Achieved precision. */
  var precision: Double = 0.0

  /**
   * Performs the iterative process. Note: this method does not return anything because Java does
   * not allow mixing double, int, or objects
   */
  fun evaluate() {
    iterations = 0
    initializeIterations()
    while (iterations++ < maximumIterations) {
      step()
      precision = precision
      if (hasConverged()) {
        break
      }
    }
    finalizeIterations()
  }

  /** Evaluate the result of the current iteration. */
  abstract override fun step()

  /** Perform eventual clean-up operations (must be implement by subclass when needed). */
  protected open fun finalizeIterations() {}

  /**
   * Check to see if the result has been attained.
   *
   * @return boolean
   */
  fun hasConverged(): Boolean = precision < desiredPrecision

  override fun done(): Boolean = hasConverged()

  /** Initializes internal parameters to start the iterative process. */
  protected open fun initializeIterations() {}

  open fun reset() {}

  /**
   * @return double
   * @param epsilon double
   * @param x double
   */
  fun relativePrecision(epsilon: Double, x: Double): Double =
    if (x > desiredPrecision) epsilon / x else epsilon

  /**
   * @param prec the desired precision.
   */
  @Throws(IllegalArgumentException::class)
  fun setDesiredPrecision(prec: Double) {
    Preconditions.checkArgument(prec > 0, "precision must be positive")
    desiredPrecision = prec
  }

  /**
   * @param maxIter the maximum allowed number of iterations
   */
  @Throws(IllegalArgumentException::class)
  fun setMaximumIterations(maxIter: Int) {
    Preconditions.checkArgument(maxIter >= 1, "max iterations must be >= 1")
    maximumIterations = maxIter
  }
}
