/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
/*
 * Created on Jul 2, 2003
 *
 */
package edu.uci.ics.jung.graph.util

import com.google.common.graph.MutableNetwork
import com.google.common.graph.Network
import com.google.common.graph.NetworkBuilder

/** Provides generators for several different test graphs. */
object TestGraphs {

  /**
   * A series of pairs that may be useful for generating graphs. The miniature graph consists of 8
   * edges, 10 nodes, and is formed of two connected components, one of 8 nodes, the other of 2.
   */
  @JvmField
  val pairs: Array<Array<String>> = arrayOf(
    arrayOf("a", "b", "3"),
    arrayOf("a", "c", "4"),
    arrayOf("a", "d", "5"),
    arrayOf("d", "c", "6"),
    arrayOf("d", "e", "7"),
    arrayOf("e", "f", "8"),
    arrayOf("f", "g", "9"),
    arrayOf("h", "i", "1"),
  )

  /**
   * Creates a small sample graph that can be used for testing purposes. The graph is as described
   * in the section on [pairs].
   *
   * @param directed true iff the graph created is to have directed edges
   * @return a graph consisting of eight edges and ten nodes.
   */
  @JvmStatic
  fun createTestGraph(directed: Boolean): Network<String, Number> {
    val graph: MutableNetwork<String, Number> = if (directed) {
      NetworkBuilder.directed().allowsParallelEdges(true).build()
    } else {
      NetworkBuilder.undirected().allowsParallelEdges(true).build()
    }

    for (pair in pairs) {
      graph.addEdge(pair[0], pair[1], Integer.parseInt(pair[2]))
    }
    return graph
  }

  /**
   * @param chainLength the length of the chain of nodes to add to the returned graph
   * @param isolateCount the number of isolated nodes to add to the returned graph
   * @return a graph consisting of a chain of [chainLength] nodes and [isolateCount]
   *     isolated nodes.
   */
  @JvmStatic
  fun createChainPlusIsolates(chainLength: Int, isolateCount: Int): Network<String, Number> {
    val g: MutableNetwork<String, Number> =
      NetworkBuilder.undirected().allowsParallelEdges(true).build()
    if (chainLength > 0) {
      val v = Array(chainLength) { "" }
      v[0] = "v0"
      g.addNode(v[0])
      for (i in 1 until chainLength) {
        v[i] = "v$i"
        g.addNode(v[i])
        g.addEdge(v[i], v[i - 1], Math.random())
      }
    }
    for (i in 0 until isolateCount) {
      val v = "v${chainLength + i}"
      g.addNode(v)
    }
    return g
  }

  /**
   * Creates a sample directed acyclic graph by generating several "layers", and connecting nodes
   * (randomly) to nodes in earlier (but never later) layers. The number of nodes in each layer is a
   * random value in the range [1, maxNodesPerLayer].
   *
   * @param layers the number of layers of nodes to create in the graph
   * @param maxNodesPerLayer the maximum number of nodes to put in any layer
   * @param linkprob the probability that this method will add an edge from a node in layer *k*
   *     to a node in layer *k+1*
   * @return the created graph
   */
  @JvmStatic
  fun createDirectedAcyclicGraph(
    layers: Int,
    maxNodesPerLayer: Int,
    linkprob: Double,
  ): Network<String, Number> {
    val dag: MutableNetwork<String, Number> =
      NetworkBuilder.directed().allowsParallelEdges(true).build()
    val previousLayers = HashSet<String>()
    val inThisLayer = HashSet<String>()
    for (i in 0 until layers) {
      val nodesThisLayer = (Math.random() * maxNodesPerLayer).toInt() + 1
      for (j in 0 until nodesThisLayer) {
        val v = "$i:$j"
        dag.addNode(v)
        inThisLayer.add(v)
        // for each previous node...
        for (v2 in previousLayers) {
          if (Math.random() < linkprob) {
            dag.addEdge(v, v2, Math.random())
          }
        }
      }
      previousLayers.addAll(inThisLayer)
      inThisLayer.clear()
    }
    return dag
  }

  private fun createEdge(
    g: MutableNetwork<String, Number>,
    v1Label: String,
    v2Label: String,
    @Suppress("UNUSED_PARAMETER") weight: Int,
  ) {
    g.addEdge(v1Label, v2Label, Math.random())
  }

  /**
   * Returns a bigger, undirected test graph with a just one component. This graph consists of a
   * clique of ten edges, a partial clique (randomly generated, with edges of 0.6 probability), and
   * one series of edges running from the first node to the last.
   *
   * @return the testgraph
   */
  @JvmStatic
  fun getOneComponentGraph(): Network<String, Number> {
    val g: MutableNetwork<String, Number> =
      NetworkBuilder.undirected().allowsParallelEdges(true).build()

    // let's throw in a clique, too
    for (i in 1..10) {
      for (j in (i + 1)..10) {
        val i1 = "$i"
        val i2 = "$j"
        g.addEdge(i1, i2, Math.pow((i + 2).toDouble(), j.toDouble()))
      }
    }

    // and, last, a partial clique
    for (i in 11..20) {
      for (j in (i + 1)..20) {
        if (Math.random() > 0.6) {
          continue
        }
        val i1 = "$i"
        val i2 = "$j"
        g.addEdge(i1, i2, Math.pow((i + 2).toDouble(), j.toDouble()))
      }
    }

    val nodeIt = g.nodes().iterator()
    var current = nodeIt.next()
    var i = 0
    while (nodeIt.hasNext()) {
      val next = nodeIt.next()
      g.addEdge(current, next, i++)
    }

    return g
  }

  /**
   * Returns a bigger test graph with a clique, several components, and other parts.
   *
   * @return a demonstration graph of type `UndirectedSparseMultiNetwork` with 28 nodes.
   */
  @JvmStatic
  fun getDemoGraph(): Network<String, Number> {
    val g: MutableNetwork<String, Number> =
      NetworkBuilder.undirected().allowsParallelEdges(true).build()

    for (pair in pairs) {
      createEdge(g, pair[0], pair[1], Integer.parseInt(pair[2]))
    }

    // let's throw in a clique, too
    for (i in 1..10) {
      for (j in (i + 1)..10) {
        val i1 = "c$i"
        val i2 = "c$j"
        g.addEdge(i1, i2, Math.pow((i + 2).toDouble(), j.toDouble()))
      }
    }

    // and, last, a partial clique
    for (i in 11..20) {
      for (j in (i + 1)..20) {
        if (Math.random() > 0.6) {
          continue
        }
        val i1 = "p$i"
        val i2 = "p$j"
        g.addEdge(i1, i2, Math.pow((i + 2).toDouble(), j.toDouble()))
      }
    }
    return g
  }
}
