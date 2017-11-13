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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Iterables;
import com.google.common.math.Stats;
import java.util.Arrays;
import java.util.Collection;

/**
 * A utility class for calculating properties of discrete distributions. Generally, these
 * distributions are represented as arrays of <code>double</code> values, which are assumed to be
 * normalized such that the entries in a single array sum to 1.
 *
 * @author Joshua O'Madadhain
 */
public class DiscreteDistribution {

  /**
   * Returns the Kullback-Leibler divergence between the two specified distributions, which must
   * have the same number of elements. This is defined as the sum over all <code>i</code> of <code>
   * dist[i] * Math.log(dist[i] / reference[i])</code>. Note that this value is not symmetric; see
   * <code>symmetricKL</code> for a symmetric variant.
   *
   * @see #symmetricKL(double[], double[])
   * @param dist the distribution whose divergence from {@code reference} is being measured
   * @param reference the reference distribution
   * @return sum_i of {@code dist[i] * Math.log(dist[i] / reference[i])}
   */
  public static double kullbackLeibler(double[] dist, double[] reference) {
    double distance = 0;

    checkArgument(dist.length == reference.length, "input arrays must be of the same length");

    for (int i = 0; i < dist.length; i++) {
      if (dist[i] > 0 && reference[i] > 0) {
        distance += dist[i] * Math.log(dist[i] / reference[i]);
      }
    }
    return distance;
  }

  /**
   * @param dist the distribution whose divergence from {@code reference} is being measured
   * @param reference the reference distribution
   * @return <code>kullbackLeibler(dist, reference) + kullbackLeibler(reference, dist)</code>
   * @see #kullbackLeibler(double[], double[])
   */
  public static double symmetricKL(double[] dist, double[] reference) {
    return kullbackLeibler(dist, reference) + kullbackLeibler(reference, dist);
  }

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

    checkArgument(dist.length == reference.length, "input arrays must be of the same length");

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

    checkArgument(dist.length == reference.length, "input arrays must be of the same length");

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

    for (double aDist : dist) {
      if (aDist > 0) {
        total += aDist * Math.log(aDist);
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

    for (double count : counts) {
      totalCount += count;
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
   * @throws IllegalArgumentException if the outer distributions array is empty, or if any of the
   *     inner distribution arrays are empty, or if not all of the inner distribution arrays are the
   *     same length
   */
  public static double[] mean(Collection<double[]> distributions) {
    checkArgument(!distributions.isEmpty(), "Distribution collection must be non-empty");
    checkArraysAreNonEmpty(distributions);
    checkArraysAreSameLength(distributions);

    return distributions.stream().mapToDouble(Stats::meanOf).toArray();
  }

  private static void checkArraysAreNonEmpty(Collection<double[]> arrays) {
    checkArgument(
        arrays.stream().mapToInt(d -> d.length).allMatch(len -> len != 0),
        "All distributions (inner arrays) must be non-empty");
  }

  private static void checkArraysAreSameLength(Collection<double[]> arrays) {
    int lengthOfFirstArray = Iterables.get(arrays, 0).length;
    checkArgument(
        arrays.stream().mapToInt(array -> array.length).allMatch(len -> len == lengthOfFirstArray),
        "All distributions (inner arrays) must be the same length");
  }

  /**
   * Returns the mean of the specified array of distributions, which are assumed to be normalized
   * arrays of <code>double</code> values.
   *
   * @param distributions the distributions whose mean is to be calculated
   * @return the mean of the distributions
   * @throws IllegalArgumentException if the outer distributions array is empty, or if any of the
   *     inner distribution arrays are empty, or if not all of the inner distribution arrays are the
   *     same length
   */
  public static double[] mean(double[][] distributions) {
    return mean(Arrays.asList(distributions));
  }
}
