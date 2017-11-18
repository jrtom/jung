/*
 * Created on Sep 24, 2005
 *
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.util;

import com.google.common.graph.Network;
import edu.uci.ics.jung.graph.util.ParallelEdgeIndexFunction;
import java.util.function.Predicate;

/**
 * A class which creates and maintains indices for parallel edges. Edges are evaluated by a
 * Predicate function and those that evaluate to true are excluded from computing a parallel offset.
 *
 * @author Tom Nelson
 */
public class PredicatedParallelEdgeIndexFunction<V, E> extends ParallelEdgeIndexFunction<V, E> {
  protected Predicate<E> predicate;

  /** @param graph the graph for which this index function is defined */
  public PredicatedParallelEdgeIndexFunction(Network<V, E> graph, Predicate<E> predicate) {
    super(graph);
    this.predicate = predicate;
  }

  /**
   * Returns the index for the specified edge, or 0 if {@code edge} is accepted by the Predicate.
   *
   * @param e the edge whose index is to be calculated
   */
  @Override
  public int getIndex(E edge) {
    return predicate.test(edge) ? 0 : super.getIndex(edge);
  }

  public Predicate<E> getPredicate() {
    return predicate;
  }

  public void setPredicate(Predicate<E> predicate) {
    this.predicate = predicate;
  }
}
