/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.scoring;

import com.google.common.base.Preconditions;
import com.google.common.graph.MutableNetwork;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * This algorithm measures the importance of nodes based upon both the number and length of disjoint
 * paths that lead to a given node from each of the nodes in the root set. Specifically the formula
 * for measuring the importance of a node is given by: I(t|R) = sum_i=1_|P(r,t)|_{alpha^|p_i|} where
 * alpha is the path decay coefficient, p_i is path i and P(r,t) is a set of maximum-sized
 * node-disjoint paths from r to t.
 *
 * <p>This algorithm uses heuristic breadth-first search to try and find the maximum-sized set of
 * node-disjoint paths between two nodes. As such, it is not guaranteed to give exact answers.
 *
 * <p>
 *
 * @author Scott White
 * @see "Algorithms for Estimating Relative Importance in Graphs by Scott White and Padhraic Smyth,
 *     2003"
 */
// TODO: versions for Graph/ValueGraph
// TODO: extend AbstractIterativeScorer and provide for iterating one step (depth) at a time?
// TODO: review this and make sure it's correctly implementing the algorithm
// TODO: this takes in a MutableNetwork and factories as a hack; there's got to be a better way;
// options include:
// (1) create a delegate class that pretends that the extra node/edge are there
// (2) refactor the internal logic so that we can emulate the presence of that node/edge
public class WeightedNIPaths<N, E> implements NodeScorer<N, Double> {
  private final MutableNetwork<N, E> graph;
  private final double alpha;
  private final int maxDepth;
  private final Set<N> priors;
  private final Map<E, Number> pathIndices = new HashMap<E, Number>();
  private final Map<Object, N> roots = new HashMap<Object, N>();
  private final Map<N, Set<Number>> pathsSeenMap = new HashMap<N, Set<Number>>();
  private final Map<N, Double> nodeScores = new LinkedHashMap<>();
  private final Supplier<N> nodeFactory;
  private final Supplier<E> edgeFactory;

  /**
   * Constructs and initializes the algorithm.
   *
   * @param graph the graph whose nodes are being measured for their importance
   * @param nodeFactory used to generate instances of V
   * @param edgeFactory used to generate instances of E
   * @param alpha the path decay coefficient (&ge;1); 2 is recommended
   * @param maxDepth the maximal depth to search out from the root set
   * @param priors the root set (starting nodes)
   */
  public WeightedNIPaths(
      MutableNetwork<N, E> graph,
      Supplier<N> nodeFactory,
      Supplier<E> edgeFactory,
      double alpha,
      int maxDepth,
      Set<N> priors) {
    // TODO: is this actually restricted to only work on directed graphs?
    Preconditions.checkArgument(graph.isDirected(), "Input graph must be directed");
    this.graph = graph;
    this.nodeFactory = nodeFactory;
    this.edgeFactory = edgeFactory;
    this.alpha = alpha;
    this.maxDepth = maxDepth;
    this.priors = priors;
    evaluate();
  }

  protected void incrementRankScore(N node, double rankValue) {
    nodeScores.computeIfPresent(node, (v, value) -> value + rankValue);
  }

  protected void computeWeightedPathsFromSource(N root, int depth) {

    int pathIdx = 1;

    for (E e : graph.outEdges(root)) {
      this.pathIndices.put(e, pathIdx);
      this.roots.put(e, root);
      newNodeEncountered(pathIdx, graph.incidentNodes(e).target(), root);
      pathIdx++;
    }

    List<E> edges = new ArrayList<E>();

    N virtualNode = nodeFactory.get();
    graph.addNode(virtualNode);
    E virtualSinkEdge = edgeFactory.get();

    graph.addEdge(virtualNode, root, virtualSinkEdge);
    edges.add(virtualSinkEdge);

    int currentDepth = 0;
    while (currentDepth <= depth) {
      double currentWeight = Math.pow(alpha, -1.0 * currentDepth);
      for (E currentEdge : edges) {
        incrementRankScore(graph.incidentNodes(currentEdge).target(), currentWeight);
      }

      if ((currentDepth == depth) || (edges.size() == 0)) {
        break;
      }

      List<E> newEdges = new ArrayList<E>();

      for (E currentSourceEdge : edges) {
        Number sourcePathIndex = this.pathIndices.get(currentSourceEdge);

        // from the currentSourceEdge, get its opposite end
        // then iterate over the out edges of that opposite end
        N newDestNode = graph.incidentNodes(currentSourceEdge).target();
        for (E currentDestEdge : graph.outEdges(newDestNode)) {
          N destEdgeRoot = this.roots.get(currentDestEdge);
          N destEdgeDest = graph.incidentNodes(currentDestEdge).target();

          if (currentSourceEdge == virtualSinkEdge) {
            newEdges.add(currentDestEdge);
            continue;
          }
          if (destEdgeRoot == root) {
            continue;
          }
          if (destEdgeDest == graph.incidentNodes(currentSourceEdge).source()) {
            continue;
          }
          Set<Number> pathsSeen = this.pathsSeenMap.get(destEdgeDest);

          if (pathsSeen == null) {
            newNodeEncountered(sourcePathIndex.intValue(), destEdgeDest, root);
          } else if (roots.get(destEdgeDest) != root) {
            roots.put(destEdgeDest, root);
            pathsSeen.clear();
            pathsSeen.add(sourcePathIndex);
          } else if (!pathsSeen.contains(sourcePathIndex)) {
            pathsSeen.add(sourcePathIndex);
          } else {
            continue;
          }

          this.pathIndices.put(currentDestEdge, sourcePathIndex);
          this.roots.put(currentDestEdge, root);
          newEdges.add(currentDestEdge);
        }
      }

      edges = newEdges;
      currentDepth++;
    }

    graph.removeNode(virtualNode);
  }

  private void newNodeEncountered(int sourcePathIndex, N dest, N root) {
    Set<Number> pathsSeen = new HashSet<Number>();
    pathsSeen.add(sourcePathIndex);
    this.pathsSeenMap.put(dest, pathsSeen);
    roots.put(dest, root);
  }

  private void evaluate() {
    for (N node : graph.nodes()) {
      nodeScores.put(node, 0.0);
    }

    for (N v : priors) {
      computeWeightedPathsFromSource(v, maxDepth);
    }

    double runningTotal = 0.0;
    for (N node : graph.nodes()) {
      runningTotal += nodeScores.get(node);
    }

    final double total = runningTotal;
    for (N node : graph.nodes()) {
      nodeScores.computeIfPresent(node, (n, value) -> value / total);
    }
  }

  @Override
  public Double getNodeScore(N v) {
    return nodeScores.get(v);
  }

  @Override
  public Map<N, Double> nodeScores() {
    return Collections.unmodifiableMap(nodeScores);
  }
}
