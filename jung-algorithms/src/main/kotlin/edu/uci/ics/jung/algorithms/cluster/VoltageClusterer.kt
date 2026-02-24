/*
 * Copyright (c) 2004, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 12, 2004
 */
package edu.uci.ics.jung.algorithms.cluster

import com.google.common.base.Preconditions
import com.google.common.graph.Network
import com.google.common.math.Stats
import edu.uci.ics.jung.algorithms.scoring.VoltageScorer
import edu.uci.ics.jung.algorithms.util.KMeansClusterer
import java.util.Collections
import java.util.LinkedList
import java.util.Random

/**
 * Clusters nodes of a `Network` based on their ranks as calculated by
 * `VoltageScorer`. This algorithm is based on, but not identical with, the method described in
 * the paper below. The primary difference is that Wu and Huberman assume a priori that the clusters
 * are of approximately the same size, and therefore use a more complex method than k-means (which
 * is used here) for determining cluster membership based on co-occurrence data.
 *
 * The algorithm proceeds as follows:
 *
 * * first, randomly generate a set of candidate clusters as follows:
 *     * randomly pick (widely separated) node pair, run VoltageScorer
 *     * group the nodes in two clusters according to their voltages
 *     * store resulting candidate clusters
 * * second, generate k-1 clusters as follows:
 *     * pick a node v as a cluster 'seed'
 *       (Wu/Huberman: most frequent node in candidate clusters)
 *     * calculate co-occurrence over all candidate clusters of v with each other node
 *     * separate co-occurrence counts into high/low; high nodes constitute a cluster
 *     * remove v's nodes from candidate clusters; continue
 * * finally, remaining unassigned nodes are assigned to the kth ("garbage") cluster.
 *
 * **NOTE**: Depending on how the co-occurrence data splits the data into clusters, the number
 * of clusters returned by this algorithm may be less than the number of clusters requested. The
 * number of clusters will never be more than the number requested, however.
 *
 * @author Joshua O'Madadhain
 * @see "'Finding communities in linear time: a physics approach', Fang Wu and Bernardo Huberman,
 *     http://www.hpl.hp.com/research/idl/papers/linear/"
 * @see VoltageScorer
 * @see KMeansClusterer
 */
open class VoltageClusterer<N : Any, E : Any>(
    protected val g: Network<N, E>,
    protected var randomSampleCount: Int,
) {
    protected val kmc: KMeansClusterer<N> = KMeansClusterer()
    protected var rand: Random = Random()

    init {
        Preconditions.checkArgument(randomSampleCount >= 1, "must generate >= 1 candidates")
    }

    protected fun setRandomSeed(randomSeed: Int) {
        rand = Random(randomSeed.toLong())
    }

    /**
     * @param v the node whose community we wish to discover
     * @return a community (cluster) centered around `v`.
     */
    fun getCommunity(v: N): Collection<Set<N>> = clusterInternal(v, 2)

    /**
     * Clusters the nodes of `g` into `numClusters` clusters, based on their
     * connectivity.
     *
     * @param numClusters the number of clusters to identify
     * @return a collection of clusters (sets of nodes)
     */
    fun cluster(numClusters: Int): Collection<Set<N>> = clusterInternal(null, numClusters)

    /**
     * Does the work of `getCommunity` and `cluster`.
     *
     * @param origin the node around which clustering is to be done
     * @param numClusters the (maximum) number of clusters to find
     * @return a collection of clusters (sets of nodes)
     */
    protected open fun clusterInternal(origin: N?, numClusters: Int): Collection<Set<N>> {
        // generate candidate clusters
        // repeat the following 'random_sample_count' times:
        // * pick (widely separated) node pair, run VoltageScorer
        // * use k-means to identify 2 communities in ranked graph
        // * store resulting candidate communities
        val vArray = ArrayList(g.nodes())

        val candidates = LinkedList<MutableSet<N>>()

        for (j in 0 until randomSampleCount) {
            val source: N =
                if (origin == null) {
                    vArray[(rand.nextDouble() * vArray.size).toInt()]
                } else {
                    origin
                }
            var target: N
            do {
                target = vArray[(rand.nextDouble() * vArray.size).toInt()]
            } while (source === target)
            val vs = VoltageScorer<N, E>(g, source, target)
            vs.evaluate()

            val voltageRanks = HashMap<N, DoubleArray>()
            for (v in g.nodes()) {
                voltageRanks[v] = doubleArrayOf(vs.getNodeScore(v))
            }

            // addOneCandidateCluster(candidates, voltage_ranks);
            addTwoCandidateClusters(candidates, voltageRanks)
        }

        // repeat the following k-1 times:
        // * pick a node v as a cluster seed
        //   (Wu/Huberman: most frequent node in candidates)
        // * calculate co-occurrence (in candidate clusters)
        //   of this node with all others
        // * use k-means to separate co-occurrence counts into high/low;
        //   high nodes are a cluster
        // * remove v's nodes from candidate clusters

        val clusters = LinkedList<Set<N>>()
        val remaining = HashSet(g.nodes())

        val seedCandidates = getSeedCandidates(candidates)
        var seedIndex = 0

        for (j in 0 until (numClusters - 1)) {
            if (remaining.isEmpty()) {
                break
            }

            var seed: N
            if (seedIndex == 0 && origin != null) {
                seed = origin
            } else {
                do {
                    seed = seedCandidates[seedIndex++]
                } while (!remaining.contains(seed))
            }

            val occurCounts = getObjectCounts(candidates, seed)
            if (occurCounts.size < 2) {
                break
            }

            // now that we have the counts, cluster them...
            try {
                val highLow = kmc.cluster(occurCounts, 2)
                // ...get the cluster with the highest-valued centroid...
                val hIter = highLow.iterator()
                val cluster1 = hIter.next()
                val cluster2 = hIter.next()
                val centroid1 = meansOf(cluster1.values)
                val centroid2 = meansOf(cluster2.values)
                val newCluster: Set<N> =
                    if (centroid1[0] >= centroid2[0]) {
                        cluster1.keys
                    } else {
                        cluster2.keys
                    }

                // ...remove the elements of new_cluster from each candidate...
                for (cluster in candidates) {
                    cluster.removeAll(newCluster)
                }
                clusters.add(newCluster)
                remaining.removeAll(newCluster)
            } catch (nece: KMeansClusterer.NotEnoughClustersException) {
                // all remaining nodes are in the same cluster
                break
            }
        }

        // identify remaining nodes (if any) as a 'garbage' cluster
        if (remaining.isNotEmpty()) {
            clusters.add(remaining)
        }

        return clusters
    }

    /**
     * Do k-means with three intervals and pick the smaller two clusters (presumed to be on the ends);
     * this is closer to the Wu-Huberman method.
     *
     * @param candidates the list of clusters to populate
     * @param voltageRanks the voltage values for each node
     */
    protected open fun addTwoCandidateClusters(
        candidates: LinkedList<MutableSet<N>>,
        voltageRanks: Map<N, DoubleArray>,
    ) {
        try {
            val clusters = ArrayList(kmc.cluster(voltageRanks, 3))
            val b01 = clusters[0].size > clusters[1].size
            val b02 = clusters[0].size > clusters[2].size
            val b12 = clusters[1].size > clusters[2].size
            when {
                b01 && b02 -> {
                    candidates.add(clusters[1].keys.toMutableSet())
                    candidates.add(clusters[2].keys.toMutableSet())
                }
                !b01 && b12 -> {
                    candidates.add(clusters[0].keys.toMutableSet())
                    candidates.add(clusters[2].keys.toMutableSet())
                }
                !b02 && !b12 -> {
                    candidates.add(clusters[0].keys.toMutableSet())
                    candidates.add(clusters[1].keys.toMutableSet())
                }
            }
        } catch (e: KMeansClusterer.NotEnoughClustersException) {
            // no valid candidates, continue
        }
    }

    /**
     * Alternative to addTwoCandidateClusters(): cluster nodes by voltages into 2 clusters. We only
     * consider the smaller of the two clusters returned by k-means to be a 'true' cluster candidate;
     * the other is a garbage cluster.
     *
     * @param candidates the list of clusters to populate
     * @param voltageRanks the voltage values for each node
     */
    protected open fun addOneCandidateCluster(
        candidates: LinkedList<MutableSet<N>>,
        voltageRanks: Map<N, DoubleArray>,
    ) {
        try {
            val clusters = ArrayList(kmc.cluster(voltageRanks, 2))
            if (clusters[0].size < clusters[1].size) {
                candidates.add(clusters[0].keys.toMutableSet())
            } else {
                candidates.add(clusters[1].keys.toMutableSet())
            }
        } catch (e: KMeansClusterer.NotEnoughClustersException) {
            // no valid candidates, continue
        }
    }

    /**
     * Returns a list of cluster seeds, ranked in decreasing order of number of appearances in the
     * specified collection of candidate clusters.
     *
     * @param candidates the set of candidate clusters
     * @return a set of cluster seeds
     */
    protected open fun getSeedCandidates(candidates: Collection<Set<N>>): List<N> {
        val occurCounts = getObjectCounts(candidates, null)

        val occurrences = ArrayList(occurCounts.keys)
        Collections.sort(occurrences, MapValueArrayComparator(occurCounts))

        // System.out.println("occurrences: ");
        for (i in occurrences.indices) {
            System.out.println(occurCounts[occurrences[i]]!![0])
        }

        return occurrences
    }

    protected open fun getObjectCounts(
        candidates: Collection<Set<N>>,
        seed: N?,
    ): Map<N, DoubleArray> {
        val occurCounts = HashMap<N, DoubleArray>()
        for (v in g.nodes()) {
            occurCounts[v] = doubleArrayOf(0.0)
        }

        for (candidate in candidates) {
            if (seed == null) {
                System.out.println(candidate.size)
            }
            if (seed == null || candidate.contains(seed)) {
                for (element in candidate) {
                    val count = occurCounts[element]!!
                    count[0]++
                }
            }
        }

        if (seed == null) {
            System.out.println("occur_counts size: ${occurCounts.size}")
            for (v in occurCounts.keys) {
                System.out.println(occurCounts[v]!![0])
            }
        }

        return occurCounts
    }

    protected inner class MapValueArrayComparator(
        private val map: Map<N, DoubleArray>,
    ) : Comparator<N> {

        override fun compare(o1: N, o2: N): Int {
            val count0 = map[o1]!!
            val count1 = map[o2]!!
            return when {
                count0[0] < count1[0] -> 1
                count0[0] > count1[0] -> -1
                else -> 0
            }
        }
    }

    companion object {
        private fun meansOf(collectionOfDoubleArrays: Collection<DoubleArray>): DoubleArray {
            val result = DoubleArray(collectionOfDoubleArrays.size)
            var index = 0
            for (array in collectionOfDoubleArrays) {
                result[index++] = Stats.meanOf(*array)
            }
            return result
        }
    }
}
