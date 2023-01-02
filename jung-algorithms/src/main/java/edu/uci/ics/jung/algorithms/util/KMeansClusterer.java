/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
/*
 * Created on Aug 9, 2004
 *
 */
package edu.uci.ics.jung.algorithms.util;

import com.google.common.base.Preconditions;
import com.google.common.math.Stats;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Groups items into a specified number of clusters, based on their proximity in d-dimensional
 * space, using the k-means algorithm. Calls to <code>cluster</code> will terminate when either of
 * the two following conditions is true:
 *
 * <ul>
 *   <li>the number of iterations is &gt; <code>max_iterations</code>
 *   <li>none of the centroids has moved as much as <code>convergence_threshold</code> since the
 *       previous iteration
 * </ul>
 *
 * @author Joshua O'Madadhain
 */
public class KMeansClusterer<T> {
  protected int max_iterations;
  protected double convergence_threshold;
  protected Random rand;

  /**
   * Creates an instance which will terminate when either the maximum number of iterations has been
   * reached, or all changes are smaller than the convergence threshold.
   *
   * @param max_iterations the maximum number of iterations to employ
   * @param convergence_threshold the smallest change we want to track
   */
  public KMeansClusterer(int max_iterations, double convergence_threshold) {
    this.max_iterations = max_iterations;
    this.convergence_threshold = convergence_threshold;
    this.rand = new Random();
  }

  /** Creates an instance with max iterations of 100 and convergence threshold of 0.001. */
  public KMeansClusterer() {
    this(100, 0.001);
  }

  /**
   * @return the maximum number of iterations
   */
  public int getMaxIterations() {
    return max_iterations;
  }

  /**
   * @param max_iterations the maximum number of iterations
   */
  public void setMaxIterations(int max_iterations) {
    Preconditions.checkArgument(max_iterations >= 0, "max iterations must be >= 0");

    this.max_iterations = max_iterations;
  }

  /**
   * @return the convergence threshold
   */
  public double getConvergenceThreshold() {
    return convergence_threshold;
  }

  /**
   * @param convergence_threshold the convergence threshold
   */
  public void setConvergenceThreshold(double convergence_threshold) {
    Preconditions.checkArgument(convergence_threshold > 0, "convergence threshold must be > 0");

    this.convergence_threshold = convergence_threshold;
  }

  /**
   * Returns a <code>Collection</code> of clusters, where each cluster is represented as a <code>Map
   * </code> of <code>Objects</code> to locations in d-dimensional space.
   *
   * @param object_locations a map of the items to cluster, to <code>double</code> arrays that
   *     specify their locations in d-dimensional space.
   * @param num_clusters the number of clusters to create
   * @return a clustering of the input objects in d-dimensional space
   * @throws NotEnoughClustersException if {@code num_clusters} is larger than the number of
   *     distinct points in object_locations
   */
  @SuppressWarnings("unchecked")
  public Collection<Map<T, double[]>> cluster(Map<T, double[]> object_locations, int num_clusters) {
    Preconditions.checkNotNull(object_locations);
    Preconditions.checkArgument(!object_locations.isEmpty(), "'objects' must be non-empty");

    Preconditions.checkArgument(
        num_clusters >= 2 && num_clusters <= object_locations.size(),
        "number of clusters must be >= 2 and <= number of objects");

    Set<double[]> centroids = new HashSet<double[]>();

    Object[] obj_array = object_locations.keySet().toArray();
    Set<T> tried = new HashSet<T>();

    // create the specified number of clusters
    while (centroids.size() < num_clusters && tried.size() < object_locations.size()) {
      T o = (T) obj_array[(int) (rand.nextDouble() * obj_array.length)];
      tried.add(o);
      double[] mean_value = object_locations.get(o);
      boolean duplicate = false;
      for (double[] cur : centroids) {
        if (Arrays.equals(mean_value, cur)) {
          duplicate = true;
        }
      }
      if (!duplicate) {
        centroids.add(mean_value);
      }
    }

    if (tried.size() >= object_locations.size()) {
      throw new NotEnoughClustersException();
    }

    // put items in their initial clusters
    Map<double[], Map<T, double[]>> clusterMap = assignToClusters(object_locations, centroids);

    // keep reconstituting clusters until either
    // (a) membership is stable, or
    // (b) number of iterations passes max_iterations, or
    // (c) max movement of any centroid is <= convergence_threshold
    int iterations = 0;
    double max_movement = Double.POSITIVE_INFINITY;
    while (iterations++ < max_iterations && max_movement > convergence_threshold) {
      max_movement = 0;
      Set<double[]> new_centroids = new HashSet<double[]>();
      // calculate new mean for each cluster
      for (Map.Entry<double[], Map<T, double[]>> entry : clusterMap.entrySet()) {
        double[] centroid = entry.getKey();
        Map<T, double[]> elements = entry.getValue();
        ArrayList<double[]> locations = new ArrayList<double[]>(elements.values());

        double[] means = new double[locations.size()];
        int index = 0;
        for (double[] location : locations) {
          means[index++] = Stats.meanOf(location);
        }
        max_movement = Math.max(max_movement, Math.sqrt(squaredError(centroid, means)));
        new_centroids.add(means);
      }

      // TODO: check membership of clusters: have they changed?

      // regenerate cluster membership based on means
      clusterMap = assignToClusters(object_locations, new_centroids);
    }
    return clusterMap.values();
  }

  /**
   * Assigns each object to the cluster whose centroid is closest to the object.
   *
   * @param object_locations a map of objects to locations
   * @param centroids the centroids of the clusters to be formed
   * @return a map of objects to assigned clusters
   */
  protected Map<double[], Map<T, double[]>> assignToClusters(
      Map<T, double[]> object_locations, Set<double[]> centroids) {
    Map<double[], Map<T, double[]>> clusterMap = new HashMap<double[], Map<T, double[]>>();
    for (double[] centroid : centroids) {
      clusterMap.put(centroid, new HashMap<T, double[]>());
    }

    for (Map.Entry<T, double[]> object_location : object_locations.entrySet()) {
      T object = object_location.getKey();
      double[] location = object_location.getValue();

      // find the cluster with the closest centroid
      Iterator<double[]> c_iter = centroids.iterator();
      double[] closest = c_iter.next();
      double distance = squaredError(location, closest);

      while (c_iter.hasNext()) {
        double[] centroid = c_iter.next();
        double dist_cur = squaredError(location, centroid);
        if (dist_cur < distance) {
          distance = dist_cur;
          closest = centroid;
        }
      }
      clusterMap.get(closest).put(object, location);
    }

    return clusterMap;
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
  private static double squaredError(double[] dist, double[] reference) {
    double error = 0;

    Preconditions.checkArgument(
        dist.length == reference.length, "input arrays must be of the same length");

    for (int i = 0; i < dist.length; i++) {
      double difference = dist[i] - reference[i];
      error += difference * difference;
    }
    return error;
  }

  /** Replaces the internal random number generator by a new instance. */
  public void setRandom(Random random) {
    this.rand = random;
  }

  /**
   * An exception that indicates that the specified data points cannot be clustered into the number
   * of clusters requested by the user. This will happen if and only if there are fewer distinct
   * points than requested clusters. (If there are fewer total data points than requested clusters,
   * <code>IllegalArgumentException</code> will be thrown.)
   *
   * @author Joshua O'Madadhain
   */
  @SuppressWarnings("serial")
  public static class NotEnoughClustersException extends RuntimeException {
    @Override
    public String getMessage() {
      return "Not enough distinct points in the input data set to form "
          + "the requested number of clusters";
    }
  }
}
