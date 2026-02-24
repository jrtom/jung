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
package edu.uci.ics.jung.algorithms.util

import com.google.common.base.Preconditions
import com.google.common.math.Stats
import java.util.Random

/**
 * Groups items into a specified number of clusters, based on their proximity in d-dimensional
 * space, using the k-means algorithm. Calls to `cluster` will terminate when either of
 * the two following conditions is true:
 *
 *  * the number of iterations is > `max_iterations`
 *  * none of the centroids has moved as much as `convergence_threshold` since the
 *    previous iteration
 *
 * @author Joshua O'Madadhain
 */
open class KMeansClusterer<T : Any>(
  protected var max_iterations: Int = 100,
  protected var convergence_threshold: Double = 0.001
) {

  protected var rand: Random = Random()

  /**
   * @return the maximum number of iterations
   */
  fun getMaxIterations(): Int = max_iterations

  /**
   * @param max_iterations the maximum number of iterations
   */
  fun setMaxIterations(max_iterations: Int) {
    Preconditions.checkArgument(max_iterations >= 0, "max iterations must be >= 0")
    this.max_iterations = max_iterations
  }

  /**
   * @return the convergence threshold
   */
  fun getConvergenceThreshold(): Double = convergence_threshold

  /**
   * @param convergence_threshold the convergence threshold
   */
  fun setConvergenceThreshold(convergence_threshold: Double) {
    Preconditions.checkArgument(convergence_threshold > 0, "convergence threshold must be > 0")
    this.convergence_threshold = convergence_threshold
  }

  /**
   * Returns a `Collection` of clusters, where each cluster is represented as a `Map`
   * of `Objects` to locations in d-dimensional space.
   *
   * @param object_locations a map of the items to cluster, to `double` arrays that
   *     specify their locations in d-dimensional space.
   * @param num_clusters the number of clusters to create
   * @return a clustering of the input objects in d-dimensional space
   * @throws NotEnoughClustersException if [num_clusters] is larger than the number of
   *     distinct points in object_locations
   */
  @Suppress("UNCHECKED_CAST")
  fun cluster(object_locations: Map<T, DoubleArray>, num_clusters: Int): Collection<Map<T, DoubleArray>> {
    Preconditions.checkNotNull(object_locations)
    Preconditions.checkArgument(object_locations.isNotEmpty(), "'objects' must be non-empty")
    Preconditions.checkArgument(
      num_clusters in 2..object_locations.size,
      "number of clusters must be >= 2 and <= number of objects"
    )

    val centroids = HashSet<DoubleArray>()

    val objList = object_locations.keys.toList()
    val tried = HashSet<T>()

    // create the specified number of clusters
    while (centroids.size < num_clusters && tried.size < object_locations.size) {
      val o = objList[(rand.nextDouble() * objList.size).toInt()]
      tried.add(o)
      val meanValue = object_locations[o]!!
      var duplicate = false
      for (cur in centroids) {
        if (meanValue.contentEquals(cur)) {
          duplicate = true
        }
      }
      if (!duplicate) {
        centroids.add(meanValue)
      }
    }

    if (tried.size >= object_locations.size) {
      throw NotEnoughClustersException()
    }

    // put items in their initial clusters
    var clusterMap = assignToClusters(object_locations, centroids)

    // keep reconstituting clusters until either
    // (a) membership is stable, or
    // (b) number of iterations passes max_iterations, or
    // (c) max movement of any centroid is <= convergence_threshold
    var iterations = 0
    var maxMovement = Double.POSITIVE_INFINITY
    while (iterations++ < max_iterations && maxMovement > convergence_threshold) {
      maxMovement = 0.0
      val newCentroids = HashSet<DoubleArray>()
      // calculate new mean for each cluster
      for ((centroid, elements) in clusterMap) {
        val locations = ArrayList(elements.values)

        val means = DoubleArray(locations.size)
        var index = 0
        for (location in locations) {
          means[index++] = Stats.meanOf(*location)
        }
        maxMovement = maxOf(maxMovement, Math.sqrt(squaredError(centroid, means)))
        newCentroids.add(means)
      }

      // TODO: check membership of clusters: have they changed?

      // regenerate cluster membership based on means
      clusterMap = assignToClusters(object_locations, newCentroids)
    }
    return clusterMap.values
  }

  /**
   * Assigns each object to the cluster whose centroid is closest to the object.
   *
   * @param object_locations a map of objects to locations
   * @param centroids the centroids of the clusters to be formed
   * @return a map of objects to assigned clusters
   */
  protected fun assignToClusters(
    object_locations: Map<T, DoubleArray>, centroids: Set<DoubleArray>
  ): Map<DoubleArray, MutableMap<T, DoubleArray>> {
    val clusterMap = HashMap<DoubleArray, MutableMap<T, DoubleArray>>()
    for (centroid in centroids) {
      clusterMap[centroid] = HashMap()
    }

    for ((obj, location) in object_locations) {
      // find the cluster with the closest centroid
      val cIter = centroids.iterator()
      var closest = cIter.next()
      var distance = squaredError(location, closest)

      while (cIter.hasNext()) {
        val centroid = cIter.next()
        val distCur = squaredError(location, centroid)
        if (distCur < distance) {
          distance = distCur
          closest = centroid
        }
      }
      clusterMap[closest]!![obj] = location
    }

    return clusterMap
  }

  /** Replaces the internal random number generator by a new instance. */
  fun setRandom(random: Random) {
    this.rand = random
  }

  /**
   * An exception that indicates that the specified data points cannot be clustered into the number
   * of clusters requested by the user. This will happen if and only if there are fewer distinct
   * points than requested clusters. (If there are fewer total data points than requested clusters,
   * `IllegalArgumentException` will be thrown.)
   *
   * @author Joshua O'Madadhain
   */
  class NotEnoughClustersException : RuntimeException() {
    override val message: String
      get() = "Not enough distinct points in the input data set to form " +
        "the requested number of clusters"
  }
}

/**
 * Returns the squared difference between the two specified distributions, which must have the
 * same number of elements. This is defined as the sum over all `i` of the square of
 * `(dist[i] - reference[i])`.
 *
 * @param dist the distribution whose distance from [reference] is being measured
 * @param reference the reference distribution
 * @return sum_i `(dist[i] - reference[i])^2`
 */
private fun squaredError(dist: DoubleArray, reference: DoubleArray): Double {
  var error = 0.0

  Preconditions.checkArgument(
    dist.size == reference.size, "input arrays must be of the same length"
  )

  for (i in dist.indices) {
    val difference = dist[i] - reference[i]
    error += difference * difference
  }
  return error
}
