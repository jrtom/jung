/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Feb 18, 2004
 */
package edu.uci.ics.jung.algorithms.internal;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * An internal utility class for maths functions.
 *
 * <p><b>Warning:</b> Use this class at your own risk! It is an implementation detail and is not
 * guaranteed to exist in future versions of JUNG.
 *
 * @author Joshua O'Madadhain
 * @author Jonathan Bluett-Duncan
 */
public class MathUtils {

  /**
   * Returns the squared difference between the two specified distributions, which must have the
   * same number of elements. This is defined as the sum over all <code>i</code> of the square of
   * <code>(dist[i] - reference[i])</code>.
   *
   * @param dist the distribution whose distance from {@code reference} is being measured
   * @param reference the reference distribution
   * @return sum_i {@code (dist[i] - reference[i])^2}
   */
  public static double squaredError(double[] dist, double[] reference) {
    double error = 0;

    Preconditions.checkArgument(
        dist.length == reference.length, "input arrays must be of the same length");

    for (int i = 0; i < dist.length; i++) {
      double difference = dist[i] - reference[i];
      error += difference * difference;
    }
    return error;
  }

  /**
   * Returns the mean of the specified <code>Collection</code> of distributions, which are assumed
   * to be normalized arrays of <code>double</code> values.
   *
   * @see #mean(double[][])
   * @param distributions the distributions whose mean is to be calculated
   * @return the mean of the distributions
   */
  public static double[] mean(Collection<double[]> distributions) {
    Preconditions.checkArgument(
        !distributions.isEmpty(), "Distribution collection must be non-empty");
    // TODO: Consider checking that the inner arrays of `distributions` are non-empty
    // TODO: Consider using com.google.common.math.Stats.meanOf
    Iterator<double[]> iter = distributions.iterator();
    double[] first = iter.next();
    double[][] dArray = new double[distributions.size()][first.length];
    dArray[0] = first;
    for (int i = 1; i < dArray.length; i++) {
      dArray[i] = iter.next();
    }

    return mean(dArray);
  }

  /**
   * Returns the mean of the specified array of distributions, represented as normalized arrays of
   * <code>double</code> values. Will throw an "index out of bounds" exception if the distribution
   * arrays are not all of the same length.
   *
   * @param distributions the distributions whose mean is to be calculated
   * @return the mean of the distributions
   */
  private static double[] mean(double[][] distributions) {
    // TODO: Consider checking that the array and inner arrays of `distributions` are non-empty
    // TODO: Consider using com.google.common.math.Stats.meanOf
    double[] dMean = new double[distributions[0].length];
    Arrays.fill(dMean, 0);

    for (int i = 0; i < distributions.length; i++) {
      for (int j = 0; j < dMean.length; j++) {
        dMean[j] += distributions[i][j] / distributions.length;
      }
    }

    return dMean;
  }

  private MathUtils() {}
}
