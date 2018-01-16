/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.layout.model;

import com.google.common.collect.Maps;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.event.LayoutChange;
import edu.uci.ics.jung.layout.event.LayoutNodePositionChange;
import edu.uci.ics.jung.layout.event.LayoutStateChange;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * two dimensional layoutmodel. Acts as a Mediator between the Graph nodes and their locations in
 * the Cartesian coordinate system.
 *
 * @author Tom Nelson
 */
public interface LayoutModel<N>
    extends Function<N, Point>,
        LayoutChange.Producer,
        LayoutNodePositionChange.Producer<N>,
        LayoutStateChange.Producer {

  /** @return the width of the layout area */
  int getWidth();

  /** @return the height of the layout area */
  int getHeight();

  /**
   * allow the passed LayoutAlgorithm to operate on this LayoutModel
   *
   * @param layoutAlgorithm the algorithm to apply to this model's Points
   */
  void accept(LayoutAlgorithm<N> layoutAlgorithm);

  /** @return a mapping of Nodes to Point locations */
  default Map<N, Point> getLocations() {
    return Collections.unmodifiableMap(Maps.asMap(getGraph().nodes(), this::apply));
  }

  /**
   * @param width to set
   * @param helght to set
   */
  void setSize(int width, int helght);

  /** stop a relaxer Thread from continuing to operate */
  void stopRelaxer();

  /**
   * indicates that there is a relaxer thread operating on this LayoutModel
   *
   * @param relaxing
   */
  void setRelaxing(boolean relaxing);

  /**
   * indicates that there is a relaxer thread operating on this LayoutModel
   *
   * @return relaxing
   */
  boolean isRelaxing();

  /**
   * a handle to the relaxer thread; may be used to attach a process to run after relax is complete
   *
   * @return the CompletableFuture
   */
  CompletableFuture getTheFuture();

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
  void set(N node, Point location);

  /**
   * Changes the layout coordinates of {@code node} to {@code x, y}.
   *
   * @param node the node to set location for
   * @param x coordinate to set
   * @param y coordinate to set
   */
  void set(N node, double x, double y);

  /**
   * @param node the node of interest
   * @return the Point location for node
   */
  Point get(N node);

  /** @return the {@code Graph} that this model is mediating */
  Graph<N> getGraph();

  /** @param graph the {@code Graph} to set */
  void setGraph(Graph<N> graph);

  void lock(N node, boolean locked);

  void lock(boolean locked);

  boolean isLocked();

  void setInitializer(Function<N, Point> initializer);
}
