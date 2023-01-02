/**
 * Copyright (c) 2008, The JUNG Authors
 *
 * <p>All rights reserved.
 *
 * <p>This software is open-source under the BSD license; see either "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description. Created on Jul 14, 2008
 */
package edu.uci.ics.jung.algorithms.scoring;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import junit.framework.TestCase;

/**
 * @author jrtom
 */
public class TestVoltageScore extends TestCase {
  protected MutableNetwork<Number, Number> g;

  // TODO:
  // * test multiple sources/targets
  // * test weighted edges
  // * test exceptional cases

  public final void testDirectedVoltagesSourceTarget() {
    g = NetworkBuilder.directed().build();

    int j = 0;
    g.addEdge(0, 1, j++);
    g.addEdge(1, 2, j++);
    g.addEdge(2, 3, j++);
    g.addEdge(2, 4, j++);
    g.addEdge(3, 4, j++);
    g.addEdge(4, 1, j++);
    g.addEdge(4, 0, j++);

    VoltageScorer<Number, Number> vr = new VoltageScorer<>(g, n -> 1, 0, 3);
    double[] voltages = {1.0, 2.0 / 3, 2.0 / 3, 0, 1.0 / 3};

    vr.evaluate();
    checkVoltages(vr, voltages);
  }

  public final void testUndirectedSourceTarget() {
    g = NetworkBuilder.undirected().build();
    int j = 0;
    g.addEdge(0, 1, j++);
    g.addEdge(0, 2, j++);
    g.addEdge(1, 3, j++);
    g.addEdge(2, 3, j++);
    g.addEdge(3, 4, j++);
    g.addEdge(3, 5, j++);
    g.addEdge(4, 6, j++);
    g.addEdge(5, 6, j++);
    VoltageScorer<Number, Number> vr = new VoltageScorer<>(g, n -> 1, 0, 6);
    double[] voltages = {1.0, 0.75, 0.75, 0.5, 0.25, 0.25, 0};

    vr.evaluate();
    checkVoltages(vr, voltages);
  }

  private static final void checkVoltages(VoltageScorer<Number, Number> vr, double[] voltages) {
    assertEquals(vr.nodeScores().size(), voltages.length);
    System.out.println("scores: " + vr.nodeScores());
    System.out.println("voltages: " + voltages.toString());
    for (int i = 0; i < voltages.length; i++) {
      assertEquals(vr.getNodeScore(i), voltages[i], 0.01);
    }
  }
}
