/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.shortestpath;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Scott White, adapted to jung2 by Tom Nelson
 */
public class TestBFSDistanceLabeler extends TestCase {
  public static Test suite() {
    return new TestSuite(TestBFSDistanceLabeler.class);
  }

  @Override
  protected void setUp() {}

  public void test() {
    MutableGraph<Number> graph = GraphBuilder.undirected().build();
    for (int i = 0; i < 6; i++) {
      graph.addNode(i);
    }
    graph.putEdge(0, 1);
    graph.putEdge(0, 5);
    graph.putEdge(0, 3);
    graph.putEdge(0, 4);
    graph.putEdge(1, 5);
    graph.putEdge(3, 4);
    graph.putEdge(3, 2);
    graph.putEdge(5, 2);
    Number root = 0;

    BFSDistanceLabeler<Number> labeler = new BFSDistanceLabeler<Number>();
    labeler.labelDistances(graph, root);

    Assert.assertEquals(labeler.getPredecessors(root).size(), 0);
    Assert.assertEquals(labeler.getPredecessors(1).size(), 1);
    Assert.assertEquals(labeler.getPredecessors(2).size(), 2);
    Assert.assertEquals(labeler.getPredecessors(3).size(), 1);
    Assert.assertEquals(labeler.getPredecessors(4).size(), 1);
    Assert.assertEquals(labeler.getPredecessors(5).size(), 1);

    Assert.assertEquals(labeler.getDistance(graph, 0), 0);
    Assert.assertEquals(labeler.getDistance(graph, 1), 1);
    Assert.assertEquals(labeler.getDistance(graph, 2), 2);
    Assert.assertEquals(labeler.getDistance(graph, 3), 1);
    Assert.assertEquals(labeler.getDistance(graph, 4), 1);
    Assert.assertEquals(labeler.getDistance(graph, 5), 1);
  }
}
