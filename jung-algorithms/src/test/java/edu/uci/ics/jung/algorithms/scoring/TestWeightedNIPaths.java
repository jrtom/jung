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

import com.google.common.collect.ImmutableMap;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** @author Scott White, adapted to jung2 by Tom Nelson */
public class TestWeightedNIPaths extends TestCase {

  Supplier<String> nodeFactory;
  Supplier<Number> edgeFactory;

  public static Test suite() {
    return new TestSuite(TestWeightedNIPaths.class);
  }

  @Override
  protected void setUp() {
    nodeFactory =
        new Supplier<String>() {
          char a = 'A';

          public String get() {
            return Character.toString(a++);
          }
        };
    edgeFactory =
        new Supplier<Number>() {
          int count;

          public Number get() {
            return count++;
          }
        };
  }

  public void testRanker() {

    MutableNetwork<String, Number> graph = NetworkBuilder.directed().build();
    for (int i = 0; i < 5; i++) {
      graph.addNode(nodeFactory.get());
    }

    graph.addEdge("A", "B", edgeFactory.get());
    graph.addEdge("A", "C", edgeFactory.get());
    graph.addEdge("A", "D", edgeFactory.get());
    graph.addEdge("B", "A", edgeFactory.get());
    graph.addEdge("B", "E", edgeFactory.get());
    graph.addEdge("B", "D", edgeFactory.get());
    graph.addEdge("C", "A", edgeFactory.get());
    graph.addEdge("C", "E", edgeFactory.get());
    graph.addEdge("C", "D", edgeFactory.get());
    graph.addEdge("D", "A", edgeFactory.get());
    graph.addEdge("D", "B", edgeFactory.get());
    graph.addEdge("D", "C", edgeFactory.get());
    graph.addEdge("D", "E", edgeFactory.get());

    Set<String> priors = new HashSet<String>();
    priors.add("A");

    WeightedNIPaths<String, Number> ranker =
        new WeightedNIPaths<String, Number>(graph, nodeFactory, edgeFactory, 2.0, 3, priors);

    Map<String, Double> expectedScores =
        ImmutableMap.of(
            "A", 0.277787,
            "B", 0.166676,
            "C", 0.166676,
            "D", 0.222222,
            "E", 0.166676);
    for (String node : graph.nodes()) {
      Assert.assertEquals(expectedScores.get(node), ranker.getNodeScore(node), 0.0001);
    }
  }
}
