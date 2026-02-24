/**
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description. Created on Jul 14, 2008
 */
package edu.uci.ics.jung.algorithms.scoring.util

import com.google.common.graph.Network
import java.util.function.Function

/**
 * An edge weight function that assigns weights as uniform transition probabilities:
 *
 * - for undirected edges, returns 1/degree(v) (where 'v' is the node in the VEPair)
 * - for directed edges, returns 1/outdegree(source(e)) (where 'e' is the edge in the VEPair)
 */
class UniformDegreeWeight<N : Any, E : Any>(
  private val graph: Network<N, E>
) : Function<VEPair<N, E>, Double> {

  override fun apply(ve_pair: VEPair<N, E>): Double {
    val e = ve_pair.e
    val v = ve_pair.v
    return if (graph.isDirected)
      1.0 / graph.outDegree(graph.incidentNodes(e).source())
    else
      1.0 / graph.degree(v)
  }
}
