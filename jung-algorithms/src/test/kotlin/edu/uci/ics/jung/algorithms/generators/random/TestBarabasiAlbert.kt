/*
 * Copyright (c) 2016, the JUNG Project and the Regents of the University
 * of California.  All rights reserved.
 *
 * This software is open-source under the BSD license; see
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.generators.random

import com.google.common.graph.NetworkBuilder
import junit.framework.TestCase
import java.util.function.Supplier

/**
 * @author W. Giordano
 * @author Scott White
 * @author James Marchant
 */
class TestBarabasiAlbert : TestCase() {
  protected lateinit var nodeFactory: Supplier<Integer>
  protected lateinit var edgeFactory: Supplier<Number>

  protected var init_nodes = 1
  protected var edges_to_add_per_timestep = 1
  protected var random_seed = 0
  protected var num_timesteps = 10
  protected var num_tests = 10

  override fun setUp() {
    nodeFactory = object : Supplier<Integer> {
      var count = 0
      override fun get(): Integer = count++ as Integer
    }
    edgeFactory = object : Supplier<Number> {
      var count = 0
      override fun get(): Number = count++
    }
  }

  // TODO(jrtom): add tests for
  // * parallel edges
  // * undirected edges
  // * ...
  fun testDirected() {
    val init_nodes = 1
    val edges_to_add_per_timestep = 1
    val random_seed = 0
    val num_tests = 10
    val num_timesteps = 10

    val nodeFactory = object : Supplier<Integer> {
      var count = 0
      override fun get(): Integer = count++ as Integer
    }
    val edgeFactory = object : Supplier<Number> {
      var count = 0
      override fun get(): Number = count++
    }

    val generator = BarabasiAlbertGenerator<Integer, Number>(
      NetworkBuilder.directed(),
      nodeFactory,
      edgeFactory,
      init_nodes,
      edges_to_add_per_timestep,
      random_seed
    )
    for (i in 1..num_tests) {
      generator.evolveGraph(num_timesteps)
      val graph = generator.get()
      assertEquals(graph.nodes().size, (i * num_timesteps) + init_nodes)
      assertEquals(graph.edges().size, edges_to_add_per_timestep * (i * num_timesteps))
      val seedNodes = generator.seedNodes

      for (v in graph.nodes()) {
        if (!seedNodes.contains(v)) {
          // Every non-seed node should have an out-degree AT LEAST equal to the number of
          // edges added per timestep (possibly more if the graph is undirected).
          assertTrue(graph.outDegree(v) >= edges_to_add_per_timestep)
        }

        // Check that not every edge goes to one node; the in-degree of any node
        // should be strictly less than the number of edges.
        assertTrue(graph.inDegree(v) < graph.edges().size)
      }
    }
  }

  @Suppress("UNUSED_VARIABLE")
  fun testPreconditions() {
    try {
      val generator = BarabasiAlbertGenerator<Integer, Number>(
        NetworkBuilder.directed(),
        nodeFactory,
        edgeFactory,
        0, // init_nodes
        edges_to_add_per_timestep,
        random_seed
      )
      fail("failed to reject init_nodes of <= 0")
    } catch (e: IllegalArgumentException) {
      // TODO: assert that the exception message contains "seed"
    }

    // test edges_to_add_per_timestep = 0
    try {
      val generator = BarabasiAlbertGenerator<Integer, Number>(
        NetworkBuilder.directed(),
        nodeFactory,
        edgeFactory,
        init_nodes,
        0, // edges_to_add_per_timestep
        random_seed
      )
      fail("failed to reject edges_to_add_per_timestamp of <= 0")
    } catch (e: IllegalArgumentException) {
      // TODO: assert that the exception message is approx:
      // "Number of edges to attach at each time step must be positive"
    }

    // test edges_to_add_per_timestep > init_nodes
    try {
      val nodesToAdd = 5
      val generator = BarabasiAlbertGenerator<Integer, Number>(
        NetworkBuilder.directed(),
        nodeFactory,
        edgeFactory,
        nodesToAdd, // init_nodes
        nodesToAdd + 1, // edges_to_add_per_timestep
        random_seed
      )
      fail("failed to reject edges_to_add_per_timestamp of > init_nodes")
    } catch (e: IllegalArgumentException) {
      // TODO: assert that the exception message is appropriate (see above)
    }
  }
}
