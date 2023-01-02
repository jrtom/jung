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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Joshua O'Madadhain
 */
public class TestPageRank extends TestCase {

  private Map<Integer, Number> edgeWeights;
  private MutableNetwork<Integer, Integer> graph;
  private Supplier<Integer> edgeFactory;

  public static Test suite() {
    return new TestSuite(TestPageRank.class);
  }

  @Override
  protected void setUp() {
    edgeWeights = new HashMap<Integer, Number>();
    edgeFactory =
        new Supplier<Integer>() {
          int i = 0;

          public Integer get() {
            return i++;
          }
        };
  }

  private void addEdge(Integer v1, Integer v2, double weight) {
    Integer edge = edgeFactory.get();
    graph.addEdge(v1, v2, edge);
    edgeWeights.put(edge, weight);
  }

  public void testRanker() {
    graph = NetworkBuilder.directed().build();

    addEdge(0, 1, 1.0);
    addEdge(1, 2, 1.0);
    addEdge(2, 3, 0.5);
    addEdge(3, 1, 1.0);
    addEdge(2, 1, 0.5);

    PageRank<Integer, Integer> pr = new PageRank<Integer, Integer>(graph, edgeWeights::get, 0);
    pr.evaluate();

    Assert.assertEquals(pr.getNodeScore(0), 0.0, pr.getTolerance());
    Assert.assertEquals(pr.getNodeScore(1), 0.4, pr.getTolerance());
    Assert.assertEquals(pr.getNodeScore(2), 0.4, pr.getTolerance());
    Assert.assertEquals(pr.getNodeScore(3), 0.2, pr.getTolerance());
  }
}
