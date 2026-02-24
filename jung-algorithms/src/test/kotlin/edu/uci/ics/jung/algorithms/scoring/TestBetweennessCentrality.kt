/**
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description. Created on Sep 17, 2008
 */
package edu.uci.ics.jung.algorithms.scoring

import com.google.common.graph.NetworkBuilder
import junit.framework.TestCase

/** */
class TestBetweennessCentrality : TestCase() {

  fun testWeighted() {
    val graph = NetworkBuilder.directed().build<Int, Char>()

    var edge = 'a'
    graph.addEdge(0, 1, edge++)
    graph.addEdge(0, 2, edge++)
    graph.addEdge(2, 3, edge++)
    graph.addEdge(3, 1, edge++)
    graph.addEdge(1, 4, edge++)

    val weights = intArrayOf(1, 1, 1, 1, 1)

    val edgeWeights = { arg0: Char -> weights[arg0 - 'a'] }

    val bc = BetweennessCentrality<Int, Char>(graph, edgeWeights)
  }
}
