/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.flows;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Scott White, Joshua O'Madadhain, Tom Nelson
 */
public class TestEdmondsKarpMaxFlow extends TestCase {

  public static Test suite() {
    return new TestSuite(TestEdmondsKarpMaxFlow.class);
  }

  @Override
  protected void setUp() {}

  public void testSanityChecks() {
    MutableNetwork<Number, Number> g = NetworkBuilder.directed().build();
    Number source = new Integer(1);
    Number sink = new Integer(2);
    g.addNode(source);
    g.addNode(sink);

    Number v = new Integer(3);

    MutableNetwork<Number, Number> h = NetworkBuilder.directed().build();
    Number w = new Integer(4);
    g.addNode(w);

    try {
      new EdmondsKarpMaxFlow<Number, Number>(g, source, source, null, null, null);
      fail("source and sink nodes not distinct");
    } catch (IllegalArgumentException iae) {
    }

    try {
      new EdmondsKarpMaxFlow<Number, Number>(h, source, w, null, null, null);
      fail("source and sink nodes not both part of specified graph");
    } catch (IllegalArgumentException iae) {
    }

    try {
      new EdmondsKarpMaxFlow<Number, Number>(g, source, v, null, null, null);
      fail("source and sink nodes not both part of specified graph");
    } catch (IllegalArgumentException iae) {
    }
  }

  public void testSimpleFlow() {
    MutableNetwork<Number, Number> graph = NetworkBuilder.directed().build();
    Supplier<Number> edgeFactory =
        new Supplier<Number>() {
          int count = 0;

          public Number get() {
            return count++;
          }
        };

    Map<Number, Integer> edgeCapacityMap = new HashMap<>();
    for (int i = 0; i < 6; i++) {
      graph.addNode(i);
    }

    Map<Number, Integer> edgeFlowMap = new HashMap<>();

    graph.addEdge(0, 1, edgeFactory.get());
    edgeCapacityMap.put(0, 16);

    graph.addEdge(0, 2, edgeFactory.get());
    edgeCapacityMap.put(1, 13);

    graph.addEdge(1, 2, edgeFactory.get());
    edgeCapacityMap.put(2, 6);

    graph.addEdge(1, 3, edgeFactory.get());
    edgeCapacityMap.put(3, 12);

    graph.addEdge(2, 4, edgeFactory.get());
    edgeCapacityMap.put(4, 14);

    graph.addEdge(3, 2, edgeFactory.get());
    edgeCapacityMap.put(5, 9);

    graph.addEdge(3, 5, edgeFactory.get());
    edgeCapacityMap.put(6, 20);

    graph.addEdge(4, 3, edgeFactory.get());
    edgeCapacityMap.put(7, 7);

    graph.addEdge(4, 5, edgeFactory.get());
    edgeCapacityMap.put(8, 4);

    EdmondsKarpMaxFlow<Number, Number> ek =
        new EdmondsKarpMaxFlow<Number, Number>(
            graph, 0, 5, n -> edgeCapacityMap.get(n), edgeFlowMap, edgeFactory);
    ek.evaluate();

    assertTrue(ek.getMaxFlow() == 23);
    Set<Number> nodesInS = ek.getNodesInSourcePartition();
    assertEquals(4, nodesInS.size());

    for (Number v : nodesInS) {
      Assert.assertTrue(v.intValue() != 3 && v.intValue() != 5);
    }

    Set<Number> nodesInT = ek.getNodesInSinkPartition();
    assertEquals(2, nodesInT.size());

    for (Number v : nodesInT) {
      Assert.assertTrue(v.intValue() == 3 || v.intValue() == 5);
    }

    Set<Number> minCutEdges = ek.getMinCutEdges();
    int maxFlow = 0;
    for (Number e : minCutEdges) {
      Number flow = edgeFlowMap.get(e);
      maxFlow += flow.intValue();
    }
    Assert.assertEquals(23, maxFlow);
    Assert.assertEquals(3, minCutEdges.size());
  }

  public void testAnotherSimpleFlow() {
    MutableNetwork<Number, Number> graph = NetworkBuilder.directed().build();
    Supplier<Number> edgeFactory =
        new Supplier<Number>() {
          int count = 0;

          public Number get() {
            return count++;
          }
        };

    Map<Number, Integer> edgeCapacityMap = new HashMap<>();
    for (int i = 0; i < 6; i++) {
      graph.addNode(i);
    }

    Map<Number, Integer> edgeFlowMap = new HashMap<>();

    graph.addEdge(0, 1, edgeFactory.get());
    edgeCapacityMap.put(0, 5);

    graph.addEdge(0, 2, edgeFactory.get());
    edgeCapacityMap.put(1, 3);

    graph.addEdge(1, 5, edgeFactory.get());
    edgeCapacityMap.put(2, 2);

    graph.addEdge(1, 2, edgeFactory.get());
    edgeCapacityMap.put(3, 8);

    graph.addEdge(2, 3, edgeFactory.get());
    edgeCapacityMap.put(4, 4);

    graph.addEdge(2, 4, edgeFactory.get());
    edgeCapacityMap.put(5, 2);

    graph.addEdge(3, 4, edgeFactory.get());
    edgeCapacityMap.put(6, 3);

    graph.addEdge(3, 5, edgeFactory.get());
    edgeCapacityMap.put(7, 6);

    graph.addEdge(4, 5, edgeFactory.get());
    edgeCapacityMap.put(8, 1);

    EdmondsKarpMaxFlow<Number, Number> ek =
        new EdmondsKarpMaxFlow<Number, Number>(
            graph, 0, 5, n -> edgeCapacityMap.get(n), edgeFlowMap, edgeFactory);
    ek.evaluate();

    assertTrue(ek.getMaxFlow() == 7);
  }
}
