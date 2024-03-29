/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.util;

import com.google.common.base.Preconditions;

/**
 * Provides basic infrastructure for iterative algorithms. Services provided include:
 *
 * <ul>
 *   <li>storage of current and max iteration count
 *   <li>framework for initialization, iterative evaluation, and finalization
 *   <li>test for convergence
 *   <li>etc.
 * </ul>
 *
 * <p>Algorithms that subclass this class are typically used in the following way: <br>
 *
 * <pre>
 * FooAlgorithm foo = new FooAlgorithm(...)
 * foo.setMaximumIterations(100); //set up conditions
 * ...
 * foo.evaluate(); //key method which initiates iterative process
 * foo.getSomeResult();
 * </pre>
 *
 * @author Scott White (originally written by Didier Besset)
 */
public abstract class IterativeProcess implements IterativeContext {
  /** Number of iterations performed. */
  private int iterations;
  /** Maximum allowed number of iterations. */
  private int maximumIterations = 50;
  /** Desired precision. */
  private double desiredPrecision = Double.MIN_VALUE;
  /** Achieved precision. */
  private double precision;

  /** Generic constructor. */
  public IterativeProcess() {}

  /**
   * Performs the iterative process. Note: this method does not return anything because Java does
   * not allow mixing double, int, or objects
   */
  public void evaluate() {
    iterations = 0;
    initializeIterations();
    while (iterations++ < maximumIterations) {
      step();
      precision = getPrecision();
      if (hasConverged()) {
        break;
      }
    }
    finalizeIterations();
  }

  /** Evaluate the result of the current iteration. */
  public abstract void step();

  /** Perform eventual clean-up operations (must be implement by subclass when needed). */
  protected void finalizeIterations() {}

  /**
   * @return the desired precision.
   */
  public double getDesiredPrecision() {
    return desiredPrecision;
  }

  /**
   * @return the number of iterations performed.
   */
  public int getIterations() {
    return iterations;
  }

  /**
   * @return the maximum allowed number of iterations.
   */
  public int getMaximumIterations() {
    return maximumIterations;
  }

  /**
   * @return the attained precision.
   */
  public double getPrecision() {
    return precision;
  }

  /**
   * @param precision the precision to set
   */
  public void setPrecision(double precision) {
    this.precision = precision;
  }

  /**
   * Check to see if the result has been attained.
   *
   * @return boolean
   */
  public boolean hasConverged() {
    return precision < desiredPrecision;
  }

  public boolean done() {
    return hasConverged();
  }

  /** Initializes internal parameters to start the iterative process. */
  protected void initializeIterations() {}

  /** */
  public void reset() {}

  /**
   * @return double
   * @param epsilon double
   * @param x double
   */
  public double relativePrecision(double epsilon, double x) {
    return x > desiredPrecision ? epsilon / x : epsilon;
  }

  /**
   * @param prec the desired precision.
   */
  public void setDesiredPrecision(double prec) throws IllegalArgumentException {
    Preconditions.checkArgument(prec > 0, "precision must be positive");
    desiredPrecision = prec;
  }

  /**
   * @param maxIter the maximum allowed number of iterations
   */
  public void setMaximumIterations(int maxIter) throws IllegalArgumentException {
    Preconditions.checkArgument(maxIter >= 1, "max iterations must be >= 1");
    maximumIterations = maxIter;
  }
}
