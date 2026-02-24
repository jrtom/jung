package edu.uci.ics.jung.algorithms.generators.random

/*
 * Copyright (c) 2009, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */

import com.google.common.base.Preconditions
import com.google.common.graph.Graph
import com.google.common.graph.MutableNetwork
import edu.uci.ics.jung.algorithms.shortestpath.Distance
import edu.uci.ics.jung.algorithms.util.WeightedChoice
import java.util.Optional
import java.util.Random
import java.util.function.Supplier

/**
 * Graph generator that adds edges to an existing graph so as to give it small world properties.
 * To create a small-world graph based on a 2D lattice, use `Lattice2DGenerator`:
 *
 * ```
 *   Lattice2DGenerator<N, E> latticeGenerator = new Lattice2DGenerator(rowCount, colCount, false);
 *   MutableNetwork<N, E> network = latticeGenerator.generateNetwork(true, nodeSupplier, edgeSupplier);
 *   KleinbergSmallWorldGenerator<N, E> generator =
 *       KleinbergSmallWorldGenerator.builder().connectionCount(3).build();
 *   generator.addSmallWorldConnections(network, latticeGenerator.distance(network.asGraph()), edgeSupplier);
 * ```
 *
 * The underlying model is an mxn (optionally toroidal) lattice. Each node u
 * has four local connections, one to each of its neighbors, and
 * in addition 1+ long range connections to some node v where v is chosen randomly according to
 * probability proportional to d^-alpha where d is the lattice distance between u and v and alpha
 * is the clustering exponent.
 *
 * @see "Navigation in a small world J. Kleinberg, Nature 406(2000), 845."
 * @author Joshua O'Madadhain
 */
// TODO:
// * can we make this cleaner/clearer?
// * consider renaming this class; it's no longer a "generator"
class KleinbergSmallWorld<N : Any, E : Any> private constructor(builder: Builder<N, E>) {

    private val clusteringExponent: Double = builder.exponent
    private val random: Random = builder.random
    private val connectionCount: Int = builder.numConnections

    companion object {
        @JvmStatic
        fun <N : Any, E : Any> builder(): Builder<N, E> = Builder()
    }

    class Builder<N : Any, E : Any> internal constructor() {
        internal var exponent: Double = 2.0
        internal var numConnections: Int = 1
        internal var seed: Optional<Long> = Optional.empty()
        internal var random: Random = Random(0)

        /** Specifies the clustering exponent. Defaults to 2. */
        fun clusteringExponent(exponent: Double): Builder<N, E> {
            this.exponent = exponent
            return this
        }

        /** Specifies the random seed. No default value specified. */
        fun randomSeed(seed: Long): Builder<N, E> {
            this.random.setSeed(seed)
            return this
        }

        /** Specifies the random number generator to use. Defaults to `new Random()`. */
        fun random(random: Random): Builder<N, E> {
            this.random = Preconditions.checkNotNull(random)
            if (!seed.isPresent) {
                seed = Optional.of(0L)
            }
            this.random.setSeed(seed.get())
            return this
        }

        /** Specifies the number of connections to add to each node. Defaults to 1. */
        fun connectionCount(count: Int): Builder<N, E> {
            this.numConnections = count
            return this
        }

        fun build(): KleinbergSmallWorld<N, E> = KleinbergSmallWorld(this)
    }

    fun addSmallWorldConnections(
        graph: MutableNetwork<N, E>,
        distance: Distance<N>,
        edgeFactory: Supplier<E>,
    ) {
        // verify that it's actually possible to give each node 'connectionCount' new incident edges
        // without creating parallel edges or self-loops (both are disallowed)
        Preconditions.checkArgument(graph.nodes().size - 5 >= connectionCount)

        // TODO: For toroidal graphs, we can make this more clever by pre-creating the WeightedChoice
        // object
        // and using the output as an offset to the current node location.

        for (node in graph.nodes()) {
            // TODO: come up with a better random selection mechanism.
            // in this case we want selection without replacement, which is not what WeightedChoice
            // does;
            // otherwise we can keep selecting the same target over and over again, which is
            // inefficient.
            val weightedChoice = getWeightedChoiceForDistance(node, graph.asGraph(), distance)

            val targets = HashSet<N>()
            while (targets.size < connectionCount) {
                // the item returned is guaranteed by getWeightedChoiceForDistance() to not be equal
                // to node
                // or any of its successors; we may try to add the same node to targets more than
                // once
                // (see the note above re: selection w/o replacement) but the Set semantics
                // disallows
                // duplicates
                val item = weightedChoice.nextItem() ?: continue
                targets.add(item)
            }

            for (target in targets) {
                graph.addEdge(node, target, edgeFactory.get())
            }
        }
    }

    private fun getWeightedChoiceForDistance(
        source: N,
        graph: Graph<N>,
        distance: Distance<N>,
    ): WeightedChoice<N> {
        val nodeWeights = HashMap<N, Double>()
        val successors = graph.successors(source)

        for (node in graph.nodes()) {
            // don't include the source or its successors
            if (node != source && !successors.contains(node)) {
                nodeWeights[node] =
                    Math.pow(
                        distance.getDistance(source, node)!!.toDouble(),
                        -clusteringExponent,
                    )
            }
        }
        Preconditions.checkState(
            nodeWeights.size >= connectionCount,
            "number of possible targets (%s) must be greater than connection count (%s)",
            nodeWeights.size,
            connectionCount,
        )
        return WeightedChoice(nodeWeights, random)
    }
}
