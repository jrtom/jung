/*
 * Created on Jul 15, 2007
 *
 * Copyright (c) 2007, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.scoring;

import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.scoring.util.ScoringUtils;
import java.util.function.Function;

/**
 * Assigns hub and authority scores to each node depending on the topology of the network. The
 * essential idea is that a node is a hub to the extent that it links to authoritative nodes, and is
 * an authority to the extent that it links to 'hub' nodes.
 *
 * <p>The classic HITS algorithm essentially proceeds as follows:
 *
 * <pre>
 * assign equal initial hub and authority values to each node
 * repeat
 *   for each node w:
 *     w.hub = sum over successors x of x.authority
 *     w.authority = sum over predecessors v of v.hub
 *   normalize hub and authority scores so that the sum of the squares of each = 1
 * until scores converge
 * </pre>
 *
 * HITS is somewhat different from random walk/eigenvector-based algorithms such as PageRank in
 * that:
 *
 * <ul>
 *   <li>there are two mutually recursive scores being calculated, rather than a single value
 *   <li>the edge weights are effectively all 1, i.e., they can't be interpreted as transition
 *       probabilities. This means that the more inlinks and outlinks that a node has, the better,
 *       since adding an inlink (or outlink) does not dilute the influence of the other inlinks (or
 *       outlinks) as in random walk-based algorithms.
 *   <li>the scores cannot be interpreted as posterior probabilities (due to the different
 *       normalization)
 * </ul>
 *
 * This implementation has the classic behavior by default. However, it has been generalized
 * somewhat so that it can act in a more "PageRank-like" fashion:
 *
 * <ul>
 *   <li>this implementation has an optional 'random jump probability' parameter analogous to the
 *       'alpha' parameter used by PageRank. Varying this value between 0 and 1 allows the user to
 *       vary between the classic HITS behavior and one in which the scores are smoothed to a
 *       uniform distribution. The default value for this parameter is 0 (no random jumps possible).
 *   <li>the edge weights can be set to anything the user likes, and in particular they can be set
 *       up (e.g. using <code>UniformDegreeWeight</code>) so that the weights of the relevant edges
 *       incident to a node sum to 1.
 *   <li>The node score normalization has been factored into its own method so that it can be
 *       overridden by a subclass. Thus, for example, since the nodes' values are set to sum to 1
 *       initially, if the weights of the relevant edges incident to a node sum to 1, then the
 *       nodes' values will continue to sum to 1 if the "sum-of-squares" normalization code is
 *       overridden to a no-op. (Other normalization methods may also be employed.)
 * </ul>
 *
 * @param <N> the node type
 * @param <E> the edge type
 * @see "'Authoritative sources in a hyperlinked environment' by Jon Kleinberg, 1997"
 */
public class HITS<N, E> extends HITSWithPriors<N, E> {

  /**
   * Creates an instance for the specified graph, edge weights, and alpha (random jump probability)
   * parameter.
   *
   * @param g the input graph
   * @param edge_weights the weights to use for each edge
   * @param alpha the probability of a hub giving some authority to all nodes, and of an authority
   *     increasing the score of all hubs (not just those connected via links)
   */
  public HITS(Network<N, E> g, Function<E, Double> edge_weights, double alpha) {
    super(g, edge_weights, ScoringUtils.getHITSUniformRootPrior(g.nodes()), alpha);
  }

  /**
   * Creates an instance for the specified graph and alpha (random jump probability) parameter. The
   * edge weights are all set to 1.
   *
   * @param g the input graph
   * @param alpha the probability of a hub giving some authority to all nodes, and of an authority
   *     increasing the score of all hubs (not just those connected via links)
   */
  public HITS(Network<N, E> g, double alpha) {
    super(g, ScoringUtils.getHITSUniformRootPrior(g.nodes()), alpha);
  }

  /**
   * Creates an instance for the specified graph. The edge weights are all set to 1 and alpha is set
   * to 0.
   *
   * @param g the input graph
   */
  public HITS(Network<N, E> g) {
    this(g, 0.0);
  }

  /** Maintains hub and authority score information for a node. */
  public static class Scores {
    /** The hub score for a node. */
    public double hub;

    /** The authority score for a node. */
    public double authority;

    /**
     * Creates an instance with the specified hub and authority score.
     *
     * @param hub the hub score
     * @param authority the authority score
     */
    public Scores(double hub, double authority) {
      this.hub = hub;
      this.authority = authority;
    }

    @Override
    public String toString() {
      return String.format("[h:%.4f,a:%.4f]", this.hub, this.authority);
    }
  }
}
