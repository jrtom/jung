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
package edu.uci.ics.jung.algorithms.util;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * A utility class for calculating properties of discrete distributions. Generally, these
 * distributions are represented as arrays of <code>double</code> values, which are assumed to be
 * normalized such that the entries in a single array sum to 1.
 *
 * @author Joshua O'Madadhain
 */
public class DiscreteDistribution {

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
   * Returns the cosine distance between the two specified distributions, which must have the same
   * number of elements. The distributions are treated as vectors in <code>dist.length</code>
   * -dimensional space. Given the following definitions
   *
   * <ul>
   *   <li><code>v</code> = the sum over all <code>i</code> of <code>dist[i] * dist[i]</code>
   *   <li><code>w</code> = the sum over all <code>i</code> of <code>reference[i] * reference[i]
   *       </code>
   *   <li><code>vw</code> = the sum over all <code>i</code> of <code>dist[i] * reference[i]</code>
   * </ul>
   *
   * the value returned is defined as <code>vw / (Math.sqrt(v) * Math.sqrt(w))</code>.
   *
   * @param dist the distribution whose distance from {@code reference} is being measured
   * @param reference the reference distribution
   * @return the cosine distance between {@code dist} and {@code reference}, considered as vectors
   */
  public static double cosine(double[] dist, double[] reference) {
    double vProd = 0; // dot product x*x
    double wProd = 0; // dot product y*y
    double vwProd = 0; // dot product x*y

    Preconditions.checkArgument(
        dist.length == reference.length, "input arrays must be of the same length");

    for (int i = 0; i < dist.length; i++) {
      vwProd += dist[i] * reference[i];
      vProd += dist[i] * dist[i];
      wProd += reference[i] * reference[i];
    }
    // cosine distance between v and w
    return vwProd / (Math.sqrt(vProd) * Math.sqrt(wProd));
  }

  /**
   * Returns the entropy of this distribution. High entropy indicates that the distribution is close
   * to uniform; low entropy indicates that the distribution is close to a Dirac delta (i.e., if the
   * probability mass is concentrated at a single point, this method returns 0). Entropy is defined
   * as the sum over all <code>i</code> of <code>-(dist[i] * Math.log(dist[i]))</code>
   *
   * @param dist the distribution whose entropy is being measured
   * @return sum_i {@code -(dist[i] * Math.log(dist[i]))}
   */
  public static double entropy(double[] dist) {
    double total = 0;

    for (int i = 0; i < dist.length; i++) {
      if (dist[i] > 0) {
        total += dist[i] * Math.log(dist[i]);
      }
    }
    return -total;
  }

  /**
   * Normalizes, with Lagrangian smoothing, the specified <code>double</code> array, so that the
   * values sum to 1 (i.e., can be treated as probabilities). The effect of the Lagrangian smoothing
   * is to ensure that all entries are nonzero; effectively, a value of <code>alpha</code> is added
   * to each entry in the original array prior to normalization.
   *
   * @param counts the array to be converted into a probability distribution
   * @param alpha the value to add to each entry prior to normalization
   */
  public static void normalize(double[] counts, double alpha) {
    double totalCount = 0;

    for (int i = 0; i < counts.length; i++) {
      totalCount += counts[i];
    }

    for (int i = 0; i < counts.length; i++) {
      counts[i] = (counts[i] + alpha) / (totalCount + counts.length * alpha);
    }
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
  public static double[] mean(double[][] distributions) {
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
}
