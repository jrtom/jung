/*
 * Created on Feb 12, 2017
 *
 * Copyright (c) 2017, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.graph;

import com.google.common.graph.Graph;

/** A subtype of Graph<N> that is a directed rooted tree. */
public interface CTree<N> extends BaseTree<N>, Graph<N> {

  /** Returns {@code true}; trees are always directed (away from the root). */
  @Override
  public boolean isDirected();

  /** Returns {@code false}; trees may never have self-loops. */
  @Override
  public boolean allowsSelfLoops();
}
