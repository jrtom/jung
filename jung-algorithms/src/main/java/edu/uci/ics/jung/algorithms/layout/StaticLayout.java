/*
 * Created on Jul 21, 2005
 *
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.layout;

import com.google.common.graph.Graph;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.function.Function;

/**
 * StaticLayout places the nodes in the locations specified by its initializer, and has no other
 * behavior. node locations can be placed in a {@code Map<N,Point2D>} and then supplied to this
 * layout as follows: {@code Function<N,Point2D> nodeLocations = Functions.forMap(map);}
 *
 * @author Tom Nelson - tomnelson@dev.java.net
 */
public class StaticLayout<N> extends AbstractLayout<N> {

  public StaticLayout(Graph<N> graph, Function<N, Point2D> initializer, Dimension size) {
    super(graph, initializer, size);
  }

  public StaticLayout(Graph<N> graph, Function<N, Point2D> initializer) {
    super(graph, initializer);
  }

  public StaticLayout(Graph<N> graph) {
    super(graph);
  }

  public StaticLayout(Graph<N> graph, Dimension size) {
    super(graph, size);
  }

  public void initialize() {}

  public void reset() {}
}
