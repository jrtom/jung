/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.layout;

import com.google.common.graph.Graph;
import edu.uci.ics.jung.visualization.layout.util.Relaxer;
import java.awt.*;
import java.util.function.Function;

/** */
public interface LayoutModel<N, P> extends Function<N, P> {

  int getWidth();

  int getHeight();

  void accept(LayoutAlgorithm<N, P> layoutAlgorithm);

  DomainModel<P> getDomainModel();

  void setSize(int width, int helght);

  Relaxer getRelaxer();

  /**
   * @param node the node whose locked state is being queried
   * @return <code>true</code> if the position of node <code>v</code> is locked
   */
  boolean isLocked(N node);

  /**
   * Changes the layout coordinates of {@code node} to {@code location}.
   *
   * @param node the node whose location is to be specified
   * @param location the coordinates of the specified location
   */
  void set(N node, P location);

  void set(N node, double x, double y);

  void set(N node, P location, boolean forceUpdate);

  void set(N node, double x, double y, boolean forceUpdate);

  P get(N node);

  Graph<N> getGraph();

  void setGraph(Graph<N> graph);

  void lock(N node, boolean locked);

  void lock(boolean locked);

  boolean isLocked();

  void setInitializer(Function<N, P> initializer);
}
