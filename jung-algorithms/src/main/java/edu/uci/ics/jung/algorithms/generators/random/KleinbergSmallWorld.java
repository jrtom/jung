package edu.uci.ics.jung.algorithms.generators.random;

/*
 * Copyright (c) 2009, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */

import com.google.common.base.Preconditions;
import com.google.common.graph.Graph;
import com.google.common.graph.MutableNetwork;
import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import edu.uci.ics.jung.algorithms.util.WeightedChoice;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Graph generator that adds edges to an existing graph so as to give it small world properties.
 * To create a small-world graph based on a 2D lattice, use {@code Lattice2DGenerator}:
 *
 * <pre>{@code
 *   Lattice2DGenerator<N, E> latticeGenerator = new Lattice2DGenerator(rowCount, colCount, false);
 *   MutableNetwork<N, E> network = latticeGenerator.generateNetwork(true, nodeSupplier, edgeSupplier);
 *   KleinbergSmallWorldGenerator<N, E> generator =
 *       KleinbergSmallWorldGenerator.builder().connectionCount(3).build();
 *   generator.addSmallWorldConnections(network, latticeGenerator.distance(network.asGraph()), edgeSupplier);
 * </pre>
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
public class KleinbergSmallWorld<N, E> {
  private final double clusteringExponent;
  private final Random random;
  private final int connectionCount;

  private KleinbergSmallWorld(Builder<N, E> builder) {
    this.clusteringExponent = builder.exponent;
    this.random = builder.random;
    this.connectionCount = builder.numConnections;
  }

  public static <N, E> Builder<N, E> builder() {
    return new Builder<>();
  }

  public static class Builder<N, E> {
    private double exponent = 2.0;
    private int numConnections = 1;
    private Optional<Long> seed = Optional.empty();
    private Random random = new Random(0);

    private Builder() {}

    /** Specifies the clustering exponent. Defaults to 2. */
    public Builder<N, E> clusteringExponent(double exponent) {
      this.exponent = exponent;
      return this;
    }

    /** Specifies the random seed. No default value specified. */
    public Builder<N, E> randomSeed(long seed) {
      this.random.setSeed(seed);
      return this;
    }

    /** Specifies the random number generator to use. Defaults to {@code new Random()}. */
    public Builder<N, E> random(Random random) {
      this.random = Preconditions.checkNotNull(random);
      if (!seed.isPresent()) {
        seed = Optional.of(0L);
      }
      this.random.setSeed(seed.get());
      return this;
    }

    /** Specifies the number of connections to add to each node. Defaults to 1. */
    public Builder<N, E> connectionCount(int count) {
      this.numConnections = count;
      return this;
    }

    public KleinbergSmallWorld<N, E> build() {
      return new KleinbergSmallWorld<N, E>(this);
    }
  }

  public void addSmallWorldConnections(
      MutableNetwork<N, E> graph, Distance<N> distance, Supplier<E> edgeFactory) {
    // verify that it's actually possible to give each node 'connectionCount' new incident edges
    // without creating parallel edges or self-loops (both are disallowed)
    Preconditions.checkArgument(graph.nodes().size() - 5 >= connectionCount);

    // TODO: For toroidal graphs, we can make this more clever by pre-creating the WeightedChoice
    // object
    // and using the output as an offset to the current node location.

    for (N node : graph.nodes()) {
      // TODO: come up with a better random selection mechanism.
      // in this case we want selection without replacement, which is not what WeightedChoice does;
      // otherwise we can keep selecting the same target over and over again, which is inefficient.
      WeightedChoice<N> weightedChoice =
          getWeightedChoiceForDistance(node, graph.asGraph(), distance);

      Set<N> targets = new HashSet<>();
      while (targets.size() < connectionCount) {
        // the item returned is guaranteed by getWeightedChoiceForDistance() to not be equal to node
        // or any of its successors; we may try to add the same node to targets more than once
        // (see the note above re: selection w/o replacement) but the Set semantics disallows
        // duplicates
        targets.add(weightedChoice.nextItem());
      }

      for (N target : targets) {
        graph.addEdge(node, target, edgeFactory.get());
      }
    }
  }

  private WeightedChoice<N> getWeightedChoiceForDistance(
      N source, Graph<N> graph, Distance<N> distance) {
    Map<N, Double> nodeWeights = new HashMap<>();
    Set<N> successors = graph.successors(source);

    for (N node : graph.nodes()) {
      // don't include the source or its successors
      if (!node.equals(source) && !successors.contains(node)) {
        nodeWeights.put(
            node, Math.pow(distance.getDistance(source, node).doubleValue(), -clusteringExponent));
      }
    }
    Preconditions.checkState(
        nodeWeights.size() >= connectionCount,
        "number of possible targets (%s) must be greater than connection count (%s)",
        nodeWeights.size(),
        connectionCount);
    WeightedChoice<N> weightedChoice = new WeightedChoice<>(nodeWeights, random);

    return weightedChoice;
  }
}
