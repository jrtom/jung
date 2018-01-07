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

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.algorithms.scoring.util.ScoringUtils;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** @author Scott White */
public class TestPageRankWithPriors extends TestCase {
  private MutableNetwork<Integer, Integer> graph;
  private Supplier<Integer> edgeFactory;

  public static Test suite() {
    return new TestSuite(TestPageRankWithPriors.class);
  }

  @Override
  protected void setUp() {
    edgeFactory =
        new Supplier<Integer>() {
          int i = 0;

          public Integer get() {
            return i++;
          }
        };
  }

  private void addEdge(Integer v1, Integer v2) {
    Integer edge = edgeFactory.get();
    graph.addEdge(v1, v2, edge);
  }

  public void testGraphScoring() {
    graph = NetworkBuilder.directed().allowsParallelEdges(true).build();

    double[] expected_score = new double[] {0.1157, 0.2463, 0.4724, 0.1653};

    for (int i = 0; i < 4; i++) {
      graph.addNode(i);
    }
    addEdge(0, 1);
    addEdge(1, 2);
    addEdge(2, 3);
    addEdge(3, 0);
    addEdge(2, 1);

    Set<Integer> priors = new HashSet<Integer>();
    priors.add(2);

    PageRankWithPriors<Integer, Integer> pr =
        new PageRankWithPriors<Integer, Integer>(
            graph, ScoringUtils.getUniformRootPrior(priors), 0.3);
    pr.evaluate();

    double score_sum = 0;
    for (int i = 0; i < graph.nodes().size(); i++) {
      double score = pr.getNodeScore(i);
      Assert.assertEquals(expected_score[i], score, pr.getTolerance());
      score_sum += score;
    }
    Assert.assertEquals(1.0, score_sum, pr.getTolerance() * graph.nodes().size());
  }

  public void testHypergraphScoring() {}
}
