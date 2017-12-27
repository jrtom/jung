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
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** Tests HITSWithPriors. */
public class TestHITSWithPriors extends TestCase {
  MutableNetwork<Number, Number> graph;
  Set<Number> roots;

  public static Test suite() {
    return new TestSuite(TestHITSWithPriors.class);
  }

  @Override
  protected void setUp() {
    graph = NetworkBuilder.directed().build();
    for (int i = 0; i < 4; i++) {
      graph.addNode(i);
    }
    int j = 0;
    graph.addEdge(0, 1, j++);
    graph.addEdge(1, 2, j++);
    graph.addEdge(2, 3, j++);
    graph.addEdge(3, 0, j++);
    graph.addEdge(2, 1, j++);

    roots = new HashSet<Number>();
    roots.add(2);
  }

  public void testRankings() {

    HITSWithPriors<Number, Number> ranker =
        new HITSWithPriors<Number, Number>(graph, ScoringUtils.getHITSUniformRootPrior(roots), 0.3);
    ranker.evaluate();

    double[] expected_auth = {0.0, 0.765, 0.365, 0.530};
    double[] expected_hub = {0.398, 0.190, 0.897, 0.0};

    double hub_sum = 0;
    double auth_sum = 0;
    for (Number n : graph.nodes()) {
      int i = n.intValue();
      double auth = ranker.getNodeScore(i).authority;
      double hub = ranker.getNodeScore(i).hub;
      Assert.assertEquals(auth, expected_auth[i], 0.001);
      Assert.assertEquals(hub, expected_hub[i], 0.001);
      hub_sum += hub * hub;
      auth_sum += auth * auth;
    }
    Assert.assertEquals(1.0, hub_sum, 0.001);
    Assert.assertEquals(1.0, auth_sum, 0.001);
  }
}
