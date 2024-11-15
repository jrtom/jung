/*
 * Copyright (c) 2018, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */

package edu.uci.ics.jung.graph;

import static com.google.common.truth.Truth.assertThat;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toCollection;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.Traverser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.junit.Test;

/** Tests for repeated node and edge addition and removal in a {@link CTree}. */
public final class CTreeMutationTest {
  private static final int NUM_TRIALS = 50;
  private static final int NUM_NODES = 1000;
  private static final int NUM_EDGES = NUM_NODES - 1;
  private static final int NODE_POOL_SIZE = 1500; // must be >> NUM_NODES

  @Test
  public void mutableCTree() {
    testCTreeMutation(TreeBuilder.builder());
  }

  private void testCTreeMutation(TreeBuilder<? super Integer> treeBuilder) {
    Random gen = new Random(42); // Fixed seed so test results are deterministic.

    for (int trial = 0; trial < NUM_TRIALS; trial++) {
      MutableCTree<Integer> tree = treeBuilder.build();

      assertThat(tree.nodes()).isEmpty();
      assertThat(tree.edges()).isEmpty();
      AbstractCTreeTest.validateTree(tree);

      List<Integer> nodeList =
          IntStream.range(0, NODE_POOL_SIZE).boxed().collect(toCollection(ArrayList::new));
      Collections.shuffle(nodeList, gen);

      tree.addNode(nodeList.get(0)); // setup root
      for (int i = 1; tree.edges().size() < NUM_EDGES; i++) {
        Integer nodeU = getRandomNode(tree, gen);
        Integer nodeV = nodeList.get(i);
        tree.putEdge(nodeU, nodeV);
      }
      List<EndpointPair<Integer>> edgeList = new ArrayList<>(tree.edges());

      assertThat(tree.nodes()).hasSize(NUM_NODES);
      assertThat(tree.edges()).hasSize(NUM_EDGES);
      AbstractCTreeTest.validateTree(tree);

      // Remove a (semi-)random number of random "leaf edges" - that is, edges with leaf target
      // nodes - and assert for each such edge that it is in the tree before removing it.
      Collections.shuffle(edgeList, gen);
      int numEdgesToRemove = controlledNumEdgesToRemove(gen);
      List<EndpointPair<Integer>> edgesRemoved1 =
          assertSuccessfulRemovalOfLeafEdgesUntil(
              tree, edgeList, edgesRemoved -> edgesRemoved.size() == numEdgesToRemove);

      assertThat(tree.nodes()).hasSize(NUM_NODES - numEdgesToRemove);
      assertThat(tree.edges()).hasSize(NUM_EDGES - numEdgesToRemove);
      AbstractCTreeTest.validateTree(tree);

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
      Collections.shuffle(edgeList, gen);
      List<EndpointPair<Integer>> edgesRemoved2 = new ArrayList<>();
      for (EndpointPair<Integer> edge : edgeList) {
        Optional<Integer> predecessorNode = tree.predecessor(edge.source());
        if (predecessorNode.isPresent() && isLeaf(tree, edge.target())) {

          EndpointPair<Integer> leafEdge = edge;
          EndpointPair<Integer> predecessorEdge =
              EndpointPair.ordered(predecessorNode.get(), leafEdge.source());

          assertThat(tree.edges()).contains(predecessorEdge); // sanity check
          assertThat(tree.edges()).contains(leafEdge); // sanity check

          CTree<Integer> subTreeToBeRemoved = subTree(tree, leafEdge.source());

          assertThat(tree.removeEdge(predecessorEdge.source(), predecessorEdge.target())).isTrue();
          edgesRemoved2.add(predecessorEdge);
          assertThat(tree.edges()).doesNotContain(leafEdge);
          for (EndpointPair<Integer> removedEdge : subTreeToBeRemoved.edges()) {
            assertThat(tree.edges()).doesNotContain(removedEdge);
          }
          edgesRemoved2.addAll(subTreeToBeRemoved.edges());
          // the leaf edge is by definition in 'subTreeToBeRemoved', so no need to add it as well

          break;
        }
      }
      edgeList.removeAll(edgesRemoved2);

      assertThat(tree.nodes()).hasSize(NUM_NODES - numEdgesToRemove - edgesRemoved2.size());
      assertThat(tree.edges()).hasSize(NUM_EDGES - numEdgesToRemove - edgesRemoved2.size());
      AbstractCTreeTest.validateTree(tree);

      // We remove all the remaining edges, starting with the "leaf edges" - that is, edges with
      // leaf target nodes - and assert for each such edge that it was in the tree before
      // removing it.
      Collections.shuffle(edgeList, gen);
      List<EndpointPair<Integer>> edgesRemoved3 =
          assertSuccessfulRemovalOfLeafEdgesUntil(tree, edgeList, unused -> tree.edges().isEmpty());

      assertThat(tree.nodes()).containsExactly(tree.root().orElseThrow(AssertionError::new));
      assertThat(tree.edges()).isEmpty();
      AbstractCTreeTest.validateTree(tree);

      Integer oldRoot = tree.root().orElseThrow(AssertionError::new);
      tree.removeNode(oldRoot);

      assertThat(tree.nodes()).isEmpty();
      assertThat(tree.edges()).isEmpty();
      AbstractCTreeTest.validateTree(tree);

      // We add the edges back in such a way that we start with the root node, and then the edges
      // between the root and its successors, and then the edges between the successors and their
      // own successors, and so on and so forth.
      // We do it this way because CTrees cannot have more than one root at a any single time, so
      // adding edges randomly causes two or more independent roots to appear in the tree before
      // it's fully reconstructed.
      //
      // TODO: Consider just adding the edges in topological order.
      List<EndpointPair<Integer>> allEdgesRemoved =
          Lists.newArrayList(Iterables.concat(edgesRemoved1, edgesRemoved2, edgesRemoved3));
      Collections.shuffle(allEdgesRemoved, gen);

      tree.addNode(oldRoot);
      Iterator<EndpointPair<Integer>> cyclingEdgesIterator = Iterators.cycle(allEdgesRemoved);
      while (cyclingEdgesIterator.hasNext()) {
        EndpointPair<Integer> next = cyclingEdgesIterator.next();
        if (tree.nodes().contains(next.source())) {
          tree.putEdge(next.source(), next.target());
          cyclingEdgesIterator.remove();
          if (!cyclingEdgesIterator.hasNext()) {
            break;
          }
        }
      }

      assertThat(tree.nodes()).hasSize(NUM_NODES);
      assertThat(tree.edges()).hasSize(NUM_EDGES);
      assertThat(tree.root()).hasValue(oldRoot);
      AbstractCTreeTest.validateTree(tree);
    }
  }

  private int controlledNumEdgesToRemove(Random gen) {
    int numEdgesToRemove;
    do {
      numEdgesToRemove = gen.nextInt(NUM_EDGES);
    } while (numEdgesToRemove == 0 // to ensure at that least one edge is removed
        || numEdgesToRemove >= NUM_EDGES - 10); // to ensure that not all edges are removed
    return numEdgesToRemove;
  }

  private List<EndpointPair<Integer>> assertSuccessfulRemovalOfLeafEdgesUntil(
      MutableCTree<Integer> tree,
      // `edgeList` is a mutable copy of the edges of the tree
      List<EndpointPair<Integer>> edgeList,
      Function<
              // input is the list of leaf edges removed so far
              List<EndpointPair<Integer>>,
              // output is whether removal of leaf edges should continue or not
              Boolean>
          stopFunction) {

    List<EndpointPair<Integer>> edgesRemoved = new ArrayList<>();
    // cycle over `edgeList` instead of `tree.edges()` to avoid `ConcurrentModificationException`s
    for (EndpointPair<Integer> edge : Iterables.cycle(edgeList)) {
      if (tree.edges().contains(edge) && isLeaf(tree, edge.target())) {
        assertThat(tree.removeEdge(edge.source(), edge.target())).isTrue();
        edgesRemoved.add(edge);
        if (stopFunction.apply(edgesRemoved).equals(TRUE)) {
          break;
        }
      }
    }
    edgeList.removeAll(edgesRemoved);
    return edgesRemoved;
  }

  // this method assumes that the input list is RandomAccess
  private static <T> T getRandomElement(List<T> list, Random gen) {
    return list.get(gen.nextInt(list.size()));
  }

  private static <N> N getRandomNode(CTree<N> tree, Random gen) {
    List<N> nodeList = new ArrayList<>(tree.nodes());
    return getRandomElement(nodeList, gen);
  }

  // TODO: Consider moving to an external function e.g.
  // MoreGraphs.isLeaf(SuccessorsFunction<N>, N)
  private static <N> boolean isLeaf(Graph<N> tree, N node) {
    return tree.successors(node).isEmpty();
  }

  private static <N> CTree<N> subTree(CTree<N> tree, N subTreeRoot) {
    MutableCTree<N> result = TreeBuilder.builder().build();
    for (N node :
        Iterables.skip(
            Traverser.forTree(tree).breadthFirst(subTreeRoot),
            1)) { // skip the subtree's root node, for it has no predecessor

      // connect every non-root node to its predecessor
      result.putEdge(tree.predecessor(node).get(), node);
    }
    return result;
  }
}
