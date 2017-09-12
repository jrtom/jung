/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.generators.random;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Graph generator that generates undirected graphs with power-law degree distributions.
 *
 * @author Joshua O'Madadhain
 * @author Scott White
 * @see "A Steady State Model for Graph Power Law by David Eppstein and Joseph Wang"
 */
public class EppsteinPowerLawGenerator<V> {
  private int nodeCount;
  private int edgeCount;
  private int mNumIterations;
  private double mMaxDegree;
  private Random mRandom;
  private Supplier<V> vertexFactory;
  private List<V> nodes;

  /**
   * Creates an instance with the specified factories and specifications.
   *
   * @param vertexFactory the Supplier to use to create vertices
   * @param numVertices the number of vertices for the generated graph
   * @param numEdges the number of edges the generated graph will have, should be Theta(numVertices)
   * @param r the number of iterations to use; the larger the value the better the graph's degree
   *     distribution will approximate a power-law
   */
  public EppsteinPowerLawGenerator(
      Supplier<V> vertexFactory, int numVertices, int numEdges, int r) {
    this.vertexFactory = vertexFactory;
    nodeCount = numVertices;
    edgeCount = numEdges;
    mNumIterations = r;
    mRandom = new Random();
  }

  protected MutableGraph<V> initializeGraph() {
    MutableGraph<V> graph = GraphBuilder.undirected().build();
    nodes = new ArrayList<V>(nodeCount);
    for (int i = 0; i < nodeCount; i++) {
      V node = vertexFactory.get();
      graph.addNode(node);
      nodes.add(node);
    }
    while (graph.edges().size() < edgeCount) {
      V u = nodes.get((int) (mRandom.nextDouble() * nodeCount));
      V v = nodes.get((int) (mRandom.nextDouble() * nodeCount));
      if (!u.equals(v)) { // no self-loops
        graph.putEdge(u, v);
      }
    }

    double maxDegree = 0;
    for (V v : graph.nodes()) {
      maxDegree = Math.max(graph.degree(v), maxDegree);
    }
    mMaxDegree = maxDegree;

    return graph;
  }

  /**
   * Generates a graph whose degree distribution approximates a power-law.
   *
   * @return the generated graph
   */
  public Graph<V> get() {
    MutableGraph<V> graph = initializeGraph();

    for (int rIdx = 0; rIdx < mNumIterations; rIdx++) {

      V v = null;
      do {
        v = nodes.get((int) (mRandom.nextDouble() * nodeCount));
      } while (graph.degree(v) == 0);

      Set<V> neighbors = graph.adjacentNodes(v);
      int neighborIndex = (int) (mRandom.nextDouble() * neighbors.size());
      int i = 0;
      V w = null;
      for (V neighbor : graph.adjacentNodes(v)) {
        if (i++ == neighborIndex) {
          w = neighbor;
          break;
        }
      }

      // FIXME: use WeightedChoice (see BarabasiAlbert) for a more efficient impl
      // for finding an edge

      V x = nodes.get((int) (mRandom.nextDouble() * nodeCount));
      V y = null;
      do {
        y = nodes.get((int) (mRandom.nextDouble() * nodeCount));
      } while (mRandom.nextDouble() > ((graph.degree(y) + 1) / mMaxDegree));

      // TODO: figure out why we sometimes have insufficient edges in the graph
      // if we make the two clauses below part of the while condition above
      if (!x.equals(y) && !graph.successors(x).contains(y)) {
        graph.removeEdge(v, w);
        graph.putEdge(x, y);
      }
    }

    return graph;
  }

  /**
   * Sets the seed for the random number generator.
   *
   * @param seed input to the random number generator.
   */
  public void setSeed(long seed) {
    mRandom.setSeed(seed);
  }
}
