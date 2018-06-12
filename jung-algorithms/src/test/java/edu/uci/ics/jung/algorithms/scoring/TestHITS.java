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
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Scott White
 * @author Tom Nelson - adapted to jung2
 */
public class TestHITS extends TestCase {

  MutableNetwork<Number, Number> graph;

  public static Test suite() {
    return new TestSuite(TestHITS.class);
  }

  @Override
  protected void setUp() {
    graph = NetworkBuilder.directed().allowsParallelEdges(true).build();
    for (int i = 0; i < 5; i++) {
      graph.addNode(i);
    }

    int j = 0;
    graph.addEdge(0, 1, j++);
    graph.addEdge(1, 2, j++);
    graph.addEdge(2, 3, j++);
    graph.addEdge(3, 0, j++);
    graph.addEdge(2, 1, j++);
  }

  // TODO(jrtom): add tests for
  // * undirected graph
  // * self-loops
  // * parallel edges
  // * weighted edges
  public void testRanker() {

    HITS<Number, Number> ranker = new HITS<Number, Number>(graph);
    for (int i = 0; i < 10; i++) {
      ranker.step();
      //            // check hub scores in terms of previous authority scores
      //            Assert.assertEquals(t.transform(0).hub,
      //            		0.5*ranker.getAuthScore(1) + 0.2*ranker.getAuthScore(4));
      //            Assert.assertEquals(t.transform(1).hub,
      //            		ranker.getAuthScore(2) + 0.2*ranker.getAuthScore(4));
      //            Assert.assertEquals(t.transform(2).hub,
      //            		0.5*ranker.getAuthScore(1) + ranker.getAuthScore(3) +
      // 0.2*ranker.getAuthScore(4));
      //            Assert.assertEquals(t.transform(3).hub,
      //            		ranker.getAuthScore(0) + 0.2*ranker.getAuthScore(4));
      //            Assert.assertEquals(t.transform(4).hub,
      //            		0.2*ranker.getAuthScore(4));
      //
      //            // check authority scores in terms of previous hub scores
      //            Assert.assertEquals(t.transform(0).authority,
      //            		ranker.getNodeScore(3) + 0.2*ranker.getNodeScore(4));
      //            Assert.assertEquals(t.transform(1).authority,
      //            		ranker.getNodeScore(0) + 0.5 * ranker.getNodeScore(2) +
      // 0.2*ranker.getNodeScore(4));
      //            Assert.assertEquals(t.transform(2).authority,
      //            		ranker.getNodeScore(1) + 0.2*ranker.getNodeScore(4));
      //            Assert.assertEquals(t.transform(3).authority,
      //            		0.5*ranker.getNodeScore(2) + 0.2*ranker.getNodeScore(4));
      //            Assert.assertEquals(t.transform(4).authority,
      //            		0.2*ranker.getNodeScore(4));
      //
      // verify that sums of each scores are 1.0
      double auth_sum = 0;
      double hub_sum = 0;
      for (int j = 0; j < 5; j++) {
        //                auth_sum += ranker.getAuthScore(j);
        //                hub_sum += ranker.getNodeScore(j);
        //            	auth_sum += (ranker.getAuthScore(j) * ranker.getAuthScore(j));
        //            	hub_sum += (ranker.getNodeScore(j) * ranker.getNodeScore(j));
        HITS.Scores score = ranker.getNodeScore(j);
        auth_sum += score.authority * score.authority;
        hub_sum += score.hub * score.hub;
      }
      Assert.assertEquals(auth_sum, 1.0, 0.0001);
      Assert.assertEquals(hub_sum, 1.0, 0.0001);
    }

    ranker.evaluate();

    Assert.assertEquals(ranker.getNodeScore(0).authority, 0, .0001);
    Assert.assertEquals(ranker.getNodeScore(1).authority, 0.8507, .001);
    Assert.assertEquals(ranker.getNodeScore(2).authority, 0.0, .0001);
    Assert.assertEquals(ranker.getNodeScore(3).authority, 0.5257, .001);

    Assert.assertEquals(ranker.getNodeScore(0).hub, 0.5257, .001);
    Assert.assertEquals(ranker.getNodeScore(1).hub, 0.0, .0001);
    Assert.assertEquals(ranker.getNodeScore(2).hub, 0.8507, .0001);
    Assert.assertEquals(ranker.getNodeScore(3).hub, 0.0, .0001);

    // the values below assume scores sum to 1
    // (rather than that sum of squares of scores sum to 1)
    //        Assert.assertEquals(ranker.getNodeScore(0).authority, 0, .0001);
    //        Assert.assertEquals(ranker.getNodeScore(1).authority, 0.618, .001);
    //        Assert.assertEquals(ranker.getNodeScore(2).authority, 0.0, .0001);
    //        Assert.assertEquals(ranker.getNodeScore(3).authority, 0.3819, .001);
    //
    //        Assert.assertEquals(ranker.getNodeScore(0).hub, 0.38196, .001);
    //        Assert.assertEquals(ranker.getNodeScore(1).hub, 0.0, .0001);
    //        Assert.assertEquals(ranker.getNodeScore(2).hub, 0.618, .0001);
    //        Assert.assertEquals(ranker.getNodeScore(3).hub, 0.0, .0001);
  }
}
