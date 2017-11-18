/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.cluster;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import java.util.Collection;
import java.util.Set;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** @author Scott White */
public class TestEdgeBetweennessClusterer extends TestCase {
  public static Test suite() {
    return new TestSuite(TestEdgeBetweennessClusterer.class);
  }

  @Override
  protected void setUp() {}

  public void testRanker() {

    MutableNetwork<Number, Number> graph = NetworkBuilder.directed().build();
    for (int i = 0; i < 10; i++) {
      graph.addNode(i + 1);
    }
    int j = 0;
    graph.addEdge(1, 2, j++);
    graph.addEdge(1, 3, j++);
    graph.addEdge(2, 3, j++);
    graph.addEdge(5, 6, j++);
    graph.addEdge(5, 7, j++);
    graph.addEdge(6, 7, j++);
    graph.addEdge(8, 10, j++);
    graph.addEdge(7, 8, j++);
    graph.addEdge(7, 10, j++);
    graph.addEdge(3, 4, j++);
    graph.addEdge(4, 6, j++);
    graph.addEdge(4, 8, j++);

    Assert.assertEquals(graph.nodes().size(), 10);
    Assert.assertEquals(graph.edges().size(), 12);

    EdgeBetweennessClusterer<Number, Number> clusterer =
        new EdgeBetweennessClusterer<Number, Number>(3);
    Collection<Set<Number>> clusters = clusterer.apply(graph);

    Assert.assertEquals(clusters.size(), 3);
  }
}
