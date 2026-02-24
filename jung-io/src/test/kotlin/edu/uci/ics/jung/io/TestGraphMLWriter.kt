/*
 * Created on Jun 22, 2008
 *
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.io

import com.google.common.graph.MutableNetwork
import com.google.common.graph.Network
import com.google.common.graph.NetworkBuilder
import edu.uci.ics.jung.graph.util.TestGraphs
import java.io.File
import java.io.FileWriter
import java.util.function.Function
import junit.framework.Assert
import junit.framework.TestCase

class TestGraphMLWriter : TestCase() {

  fun testBasicWrite() {
    val g = TestGraphs.createTestGraph(true)
    val gmlw = GraphMLWriter<String, Number>()
    val edge_weight = Function<Number, String> { n -> n.toInt().toString() }

    val node_name = Function.identity<String>()

    gmlw.addEdgeData("weight", "integer value for the edge", Integer.toString(-1), edge_weight)
    gmlw.addNodeData("name", "identifier for the node", null, node_name)
    @Suppress("UNCHECKED_CAST")
    gmlw.setEdgeIDs(edge_weight as Function<in Number, String?>)
    gmlw.setNodeIDs(node_name)
    gmlw.save(g, FileWriter("src/test/resources/testbasicwrite.graphml"))

    // TODO: now read it back in and compare the graph connectivity
    // and other metadata with what's in TestGraphs.pairs[], etc.
    val gmlr = GraphMLReader<MutableNetwork<String, Any>, String, Any>()
    val g2: MutableNetwork<String, Any> = NetworkBuilder.directed().allowsSelfLoops(true).build()
    gmlr.load("src/test/resources/testbasicwrite.graphml", g2)
    val edge_metadata = gmlr.getEdgeMetadata()
    val edge_weight2 = edge_metadata["weight"]!!.transformer
    validateTopology(g, g2, edge_weight, edge_weight2)

    val f = File("src/test/resources/testbasicwrite.graphml")
    f.delete()
  }

  fun <T : Comparable<T>> validateTopology(
    g: Network<T, Number>,
    g2: Network<T, Any>,
    edge_weight: Function<Number, String>,
    edge_weight2: Function<Any, String>
  ) {
    Assert.assertEquals(g2.edges().size, g.edges().size)
    val g_nodes = ArrayList<T>(g.nodes())
    val g2_nodes = ArrayList<T>(g2.nodes())
    g_nodes.sort()
    g2_nodes.sort()
    Assert.assertEquals(g_nodes, g2_nodes)

    val g_edges = HashSet<String>()
    for (n in g.edges()) {
      g_edges.add(n.toString())
    }
    val g2_edges = HashSet<Any>(g2.edges())
    Assert.assertEquals(g_edges, g2_edges)

    for (v in g2.nodes()) {
      for (w in g2.nodes()) {
        Assert.assertEquals(g.adjacentNodes(v).contains(w), g2.adjacentNodes(v).contains(w))
        val e = HashSet<String>()
        for (n in g.edgesConnecting(v, w)) {
          e.add(n.toString())
        }
        val e2 = HashSet<Any>(g2.edgesConnecting(v, w))
        Assert.assertEquals(e.size, e2.size)
        Assert.assertEquals(e, e2)
      }
    }

    for (o in g2.edges()) {
      val weight = edge_weight.apply(java.lang.Double(o as String))
      val weight2 = edge_weight2.apply(o)
      Assert.assertEquals(weight2, weight)
    }
  }
}
