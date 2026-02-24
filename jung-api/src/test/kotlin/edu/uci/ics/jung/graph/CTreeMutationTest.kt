/*
 * Copyright (c) 2018, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */

package edu.uci.ics.jung.graph

import com.google.common.collect.Iterables
import com.google.common.collect.Iterators
import com.google.common.collect.Lists
import com.google.common.graph.EndpointPair
import com.google.common.graph.Graph
import com.google.common.graph.Traverser
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.ArrayList
import java.util.Collections
import java.util.Random
import java.util.function.Function
import java.util.stream.Collectors.toCollection
import java.util.stream.IntStream

/** Tests for repeated node and edge addition and removal in a [CTree]. */
class CTreeMutationTest {
  companion object {
    private const val NUM_TRIALS = 50
    private const val NUM_NODES = 1000
    private const val NUM_EDGES = NUM_NODES - 1
    private const val NODE_POOL_SIZE = 1500 // must be >> NUM_NODES

    // this method assumes that the input list is RandomAccess
    private fun <T> getRandomElement(list: List<T>, gen: Random): T {
      return list[gen.nextInt(list.size)]
    }

    private fun <N : Any> getRandomNode(tree: CTree<N>, gen: Random): N {
      val nodeList = ArrayList(tree.nodes())
      return getRandomElement(nodeList, gen)
    }

    // TODO: Consider moving to an external function e.g.
    // MoreGraphs.isLeaf(SuccessorsFunction<N>, N)
    private fun <N : Any> isLeaf(tree: Graph<N>, node: N): Boolean {
      return tree.successors(node).isEmpty()
    }

    private fun <N : Any> subTree(tree: CTree<N>, subTreeRoot: N): CTree<N> {
      val result: MutableCTree<N> = TreeBuilder.builder().build()
      for (node in Iterables.skip(
        Traverser.forTree(tree).breadthFirst(subTreeRoot),
        1
      )) { // skip the subtree's root node, for it has no predecessor

        // connect every non-root node to its predecessor
        result.putEdge(tree.predecessor(node).get(), node)
      }
      return result
    }
  }

  @Test
  fun mutableCTree() {
    testCTreeMutation(TreeBuilder.builder())
  }

  private fun testCTreeMutation(treeBuilder: TreeBuilder<in Int>) {
    val gen = Random(42) // Fixed seed so test results are deterministic.

    for (trial in 0 until NUM_TRIALS) {
      val tree: MutableCTree<Int> = treeBuilder.build()

      assertThat(tree.nodes()).isEmpty()
      assertThat(tree.edges()).isEmpty()
      AbstractCTreeTest.validateTree(tree)

      val nodeList: MutableList<Int> =
        IntStream.range(0, NODE_POOL_SIZE).boxed().collect(toCollection(::ArrayList))
      Collections.shuffle(nodeList, gen)

      tree.addNode(nodeList[0]) // setup root
      var i = 1
      while (tree.edges().size < NUM_EDGES) {
        val nodeU = getRandomNode(tree, gen)
        val nodeV = nodeList[i]
        tree.putEdge(nodeU, nodeV)
        i++
      }
      val edgeList = ArrayList(tree.edges())

      assertThat(tree.nodes()).hasSize(NUM_NODES)
      assertThat(tree.edges()).hasSize(NUM_EDGES)
      AbstractCTreeTest.validateTree(tree)

      // Remove a (semi-)random number of random "leaf edges" - that is, edges with leaf target
      // nodes - and assert for each such edge that it is in the tree before removing it.
      Collections.shuffle(edgeList, gen)
      val numEdgesToRemove = controlledNumEdgesToRemove(gen)
      val edgesRemoved1 =
        assertSuccessfulRemovalOfLeafEdgesUntil(
          tree, edgeList, Function { edgesRemoved -> edgesRemoved.size == numEdgesToRemove }
        )

      assertThat(tree.nodes()).hasSize(NUM_NODES - numEdgesToRemove)
      assertThat(tree.edges()).hasSize(NUM_EDGES - numEdgesToRemove)
      AbstractCTreeTest.validateTree(tree)

      // We take a random "leaf edge" from the tree. We then look at the predecessor of the leaf
      // edge's source node, and call those pair of nodes the "predecessor edge".
      //
      // We then go on to assert that calling
      // `tree.removeEdge(predecessorEdge.source(), precedessorEdge.target())` (i.e.,
      // removing the predecessor edge's target node) succeeds.
      //
      // And in the process of the predecessor edge being deleted, we assert that the subtree rooted
      // at the predecessor edge's target node (which includes the leaf edge) is also removed from
      // the tree.
      Collections.shuffle(edgeList, gen)
      val edgesRemoved2 = ArrayList<EndpointPair<Int>>()
      for (edge in edgeList) {
        val predecessorNode = tree.predecessor(edge.source())
        if (predecessorNode.isPresent && isLeaf(tree, edge.target())) {

          val leafEdge = edge
          val predecessorEdge =
            EndpointPair.ordered(predecessorNode.get(), leafEdge.source())

          assertThat(tree.edges()).contains(predecessorEdge) // sanity check
          assertThat(tree.edges()).contains(leafEdge) // sanity check

          val subTreeToBeRemoved = subTree(tree, leafEdge.source())

          assertThat(tree.removeEdge(predecessorEdge.source(), predecessorEdge.target())).isTrue()
          edgesRemoved2.add(predecessorEdge)
          assertThat(tree.edges()).doesNotContain(leafEdge)
          for (removedEdge in subTreeToBeRemoved.edges()) {
            assertThat(tree.edges()).doesNotContain(removedEdge)
          }
          edgesRemoved2.addAll(subTreeToBeRemoved.edges())
          // the leaf edge is by definition in 'subTreeToBeRemoved', so no need to add it as well

          break
        }
      }
      edgeList.removeAll(edgesRemoved2.toSet())

      assertThat(tree.nodes()).hasSize(NUM_NODES - numEdgesToRemove - edgesRemoved2.size)
      assertThat(tree.edges()).hasSize(NUM_EDGES - numEdgesToRemove - edgesRemoved2.size)
      AbstractCTreeTest.validateTree(tree)

      // We remove all the remaining edges, starting with the "leaf edges" - that is, edges with
      // leaf target nodes - and assert for each such edge that it was in the tree before
      // removing it.
      Collections.shuffle(edgeList, gen)
      val edgesRemoved3 =
        assertSuccessfulRemovalOfLeafEdgesUntil(
          tree, edgeList, Function { _ -> tree.edges().isEmpty() }
        )

      assertThat(tree.nodes()).containsExactly(tree.root().orElseThrow { AssertionError() })
      assertThat(tree.edges()).isEmpty()
      AbstractCTreeTest.validateTree(tree)

      val oldRoot = tree.root().orElseThrow { AssertionError() }
      tree.removeNode(oldRoot)

      assertThat(tree.nodes()).isEmpty()
      assertThat(tree.edges()).isEmpty()
      AbstractCTreeTest.validateTree(tree)

      // We add the edges back in such a way that we start with the root node, and then the edges
      // between the root and its successors, and then the edges between the successors and their
      // own successors, and so on and so forth.
      // We do it this way because CTrees cannot have more than one root at a any single time, so
      // adding edges randomly causes two or more independent roots to appear in the tree before
      // it's fully reconstructed.
      //
      // TODO: Consider just adding the edges in topological order.
      val allEdgesRemoved: MutableList<EndpointPair<Int>> =
        Lists.newArrayList(Iterables.concat(edgesRemoved1, edgesRemoved2, edgesRemoved3))
      Collections.shuffle(allEdgesRemoved, gen)

      tree.addNode(oldRoot)
      val cyclingEdgesIterator = Iterators.cycle(allEdgesRemoved)
      while (cyclingEdgesIterator.hasNext()) {
        val next = cyclingEdgesIterator.next()
        if (tree.nodes().contains(next.source())) {
          tree.putEdge(next.source(), next.target())
          cyclingEdgesIterator.remove()
          if (!cyclingEdgesIterator.hasNext()) {
            break
          }
        }
      }

      assertThat(tree.nodes()).hasSize(NUM_NODES)
      assertThat(tree.edges()).hasSize(NUM_EDGES)
      assertThat(tree.root()).hasValue(oldRoot)
      AbstractCTreeTest.validateTree(tree)
    }
  }

  private fun controlledNumEdgesToRemove(gen: Random): Int {
    var numEdgesToRemove: Int
    do {
      numEdgesToRemove = gen.nextInt(NUM_EDGES)
    } while (numEdgesToRemove == 0 // to ensure at that least one edge is removed
      || numEdgesToRemove >= NUM_EDGES - 10) // to ensure that not all edges are removed
    return numEdgesToRemove
  }

  private fun assertSuccessfulRemovalOfLeafEdgesUntil(
    tree: MutableCTree<Int>,
    // `edgeList` is a mutable copy of the edges of the tree
    edgeList: MutableList<EndpointPair<Int>>,
    stopFunction: Function<
        // input is the list of leaf edges removed so far
        List<EndpointPair<Int>>,
        // output is whether removal of leaf edges should continue or not
        Boolean>
  ): List<EndpointPair<Int>> {

    val edgesRemoved = ArrayList<EndpointPair<Int>>()
    // cycle over `edgeList` instead of `tree.edges()` to avoid `ConcurrentModificationException`s
    for (edge in Iterables.cycle(edgeList)) {
      if (tree.edges().contains(edge) && isLeaf(tree, edge.target())) {
        assertThat(tree.removeEdge(edge.source(), edge.target())).isTrue()
        edgesRemoved.add(edge)
        if (stopFunction.apply(edgesRemoved) == true) {
          break
        }
      }
    }
    edgeList.removeAll(edgesRemoved.toSet())
    return edgesRemoved
  }
}
