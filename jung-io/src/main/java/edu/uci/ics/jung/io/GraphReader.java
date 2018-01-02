/*
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */

package edu.uci.ics.jung.io;

import com.google.common.graph.MutableNetwork;

/**
 * Interface for a reader of graph objects
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 * @param <G> the graph type
 * @param <N> the node type the node type
 * @param <N> the edge type the edge type
 */
public interface GraphReader<G extends MutableNetwork<N, E>, N, E> {

  /**
   * Reads a single graph object, if one is available.
   *
   * @return the next graph object, or null if none exists.
   * @throws GraphIOException thrown if an error occurred.
   */
  G readGraph() throws GraphIOException;

  /**
   * Closes this resource and frees any resources.
   *
   * @throws GraphIOException thrown if an error occurred.
   */
  void close() throws GraphIOException;
}
