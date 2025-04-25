/*
 * Copyright (c) 2025, the JUNG Project and the Regents of the University
 * of California.  All rights reserved.
 *
 * This software is open-source under the BSD license; see
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.layout.spatial;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the fix for incorrect Y-coordinate distribution in FRLayoutAlgorithm. Previously, nodes
 * were arranged in a square (width × width), ignoring the configured layout height.
 *
 * <p>To verify the fix: 1. Generate enough nodes to exceed the layout bounds. 2. Ensure all nodes
 * are positioned within the specified width * height area after layout.
 *
 * @author melanxoluk
 */
public class FRLayoutLocationsTest {
  private static final int layoutWidth = 2000;
  private static final int layoutHeight = 500;
  private static final int iterations = 200;
  private static final int nodes = 20;

  @Test
  public void testTargetCoordinates() {
    MutableGraph<Integer> graph = GraphBuilder.directed().build();
    LoadingCacheLayoutModel.Builder<Integer, ?> builder =
        LoadingCacheLayoutModel.<Integer>builder()
            .setGraph(graph)
            .setSize(layoutWidth, layoutHeight);

    LayoutModel<Integer> layoutModel = new FRLayoutsTest.TestLayoutModel<>(builder, iterations);

    for (int i = 0; i < nodes; i++) {
      graph.addNode(i);
      layoutModel.set(i, Point.of(i, i));
    }

    FRLayoutAlgorithm algorithm = new FRLayoutAlgorithm();
    layoutModel.accept(algorithm);

    Assert.assertTrue(
        "All points should has coordinates into target layout size",
        layoutModel.getLocations().values().stream()
            .allMatch(it -> it.y <= layoutHeight && it.x <= layoutWidth));
  }
}
