/*
 * Copyright (C) 2016 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.jung.graph;

import static com.google.common.truth.Truth.assertThat;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.TreeTraverser;
import com.google.common.graph.EndpointPair;
import com.google.common.truth.Truth8;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
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
          new ArrayList<>(
              ContiguousSet.create(Range.closedOpen(0, NODE_POOL_SIZE), DiscreteDomain.integers()));
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

      // TODO: Refactor into `assertSuccessfulRemovalOfLeafEdgesUntil`?
      List<EndpointPair<Integer>> edgesRemoved1 = new ArrayList<>();
      for (EndpointPair<Integer> edge : Iterables.cycle(edgeList)) {
        // TODO: Remove `tree.edges().contains(edge)`?
        if (tree.edges().contains(edge) && isLeaf(tree, edge.target())) {
          assertThat(tree.removeEdge(edge.source(), edge.target())).isTrue();
          edgesRemoved1.add(edge);
          if (edgesRemoved1.size() == numEdgesToRemove) {
            break;
          }
        }
      }
      edgeList.removeAll(edgesRemoved1);

      assertThat(tree.nodes()).hasSize(NUM_NODES - numEdgesToRemove);
      assertThat(tree.edges()).hasSize(NUM_EDGES - numEdgesToRemove);
      AbstractCTreeTest.validateTree(tree);

      // We take a random "leaf edge" from the tree. We then take a random edge that is connected to
      // any of the predecessors of the leaf edge's source node - we call this edge the "predecessor
      // edge".
      // We then go on to assert that removing the predecessor edge succeeds, and that in the
      // process of it being deleted, all the "descendant edges" of the predecessor edge were
      // removed as well. (In this context, "descendant" has a very similar meaning to "reachable"
      // in com.google.common.graph.Graphs#reachableNodes, except that we're talking in terms of
      // edges instead of nodes, and the predecessor edge itself does not count as a descendant
      // edge. Thus, the leaf edge counts as a descendant edge.)
      Collections.shuffle(edgeList, gen);
      List<EndpointPair<Integer>> edgesRemoved2 = new ArrayList<>();
      for (EndpointPair<Integer> potentialLeafEdge : edgeList) {
        Optional<Integer> predecessorNode = tree.predecessor(potentialLeafEdge.source());
        if (predecessorNode.isPresent()
            && tree.root().equals(predecessorNode)
            && isLeaf(tree, potentialLeafEdge.target())) {

          EndpointPair<Integer> leafEdge = potentialLeafEdge;
          EndpointPair<Integer> predecessorEdge =
              EndpointPair.ordered(predecessorNode.get(), leafEdge.source());

          assertThat(tree.edges()).contains(predecessorEdge); // sanity check
          assertThat(tree.edges()).contains(leafEdge); // sanity check

          List<EndpointPair<Integer>> descendantEdges = descendantEdges(tree, predecessorEdge);
          Collections.shuffle(descendantEdges, gen);
          assertThat(tree.removeEdge(predecessorEdge.source(), predecessorEdge.target())).isTrue();
          assertThat(tree.edges().contains(leafEdge)).isFalse();
          for (EndpointPair<Integer> descendantEdge : descendantEdges) {
            assertThat(tree.edges().contains(descendantEdge)).isFalse();
          }

          edgesRemoved2.add(predecessorEdge);
          edgesRemoved2.addAll(descendantEdges); // the leaf edge is by definition a descendant edge

          break;
        }
      }
      edgeList.removeAll(edgesRemoved2);

      assertThat(tree.nodes()).hasSize(NUM_NODES - numEdgesToRemove - edgesRemoved2.size());
      assertThat(tree.edges()).hasSize(NUM_EDGES - numEdgesToRemove - edgesRemoved2.size());
      AbstractCTreeTest.validateTree(tree);

      // We remove all the remaining edges, starting with the "leaf edges" - that is, edges with
      // leaf target nodes - and assert for each such edge that it was in the tree whilst
      // removing it.
      Collections.shuffle(edgeList, gen);
      // TODO: Refactor into `assertSuccessfulRemovalOfLeafEdgesUntil`?
      List<EndpointPair<Integer>> edgesRemoved3 = new ArrayList<>();
      for (EndpointPair<Integer> edge : Iterables.cycle(edgeList)) {
        // TODO: Remove `tree.edges().contains(edge)`?
        if (tree.edges().contains(edge) && isLeaf(tree, edge.target())) {
          assertThat(tree.removeEdge(edge.source(), edge.target())).isTrue();
          edgesRemoved3.add(edge);
          if (tree.edges().isEmpty()) {
            break;
          }
        }
      }
      edgeList.removeAll(edgesRemoved3);

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
      Truth8.assertThat(tree.root()).hasValue(oldRoot);
      AbstractCTreeTest.validateTree(tree);
    }
  }

  private int controlledNumEdgesToRemove(Random gen) {
    int numEdgesToRemove;
    do {
      numEdgesToRemove = gen.nextInt(NUM_EDGES);
    } while (numEdgesToRemove == 0 || numEdgesToRemove < NUM_EDGES - 10);
    // the number 0 above is there to ensure at that least one edge gets removed
    // the number 10 above is arbitrary; it is only there to ensure that not all edges get removed
    return numEdgesToRemove;
  }

  private List<EndpointPair<Integer>> assertSuccessfulRemovalOfLeafEdgesUntil(
      MutableCTree<Integer> tree,
      List<EndpointPair<Integer>> mutableCopyOfEdgesFromTree,
      Function<
              // input is the list of leaf edges removed so far
              List<EndpointPair<Integer>>,
              // output is whether removal of leaf edges should continue or not
              Boolean>
          stopFunction) {

    List<EndpointPair<Integer>> edgesRemoved = new ArrayList<>();
    for (EndpointPair<Integer> edge : Iterables.cycle(mutableCopyOfEdgesFromTree)) {
      if (tree.edges().contains(edge) && isLeaf(tree, edge.target())) {
        assertThat(tree.removeEdge(edge.source(), edge.target())).isTrue();
        edgesRemoved.add(edge);
        if (stopFunction.apply(edgesRemoved).equals(TRUE)) {
          break;
        }
      }
    }
    mutableCopyOfEdgesFromTree.removeAll(edgesRemoved);
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

  private static <N> boolean isLeaf(CTree<N> tree, N node) {
    return tree.successors(node).isEmpty();
  }

  private static <N> List<EndpointPair<N>> descendantEdges(
      CTree<N> tree, EndpointPair<N> startingEdge) {
    // TODO: Migrate to com.google.common.graph.Traverser when it is available.
    return TreeTraverser.<EndpointPair<N>>using(edge -> outEdges(tree, edge))
        .breadthFirstTraversal(startingEdge)
        .skip(1) // skip the starting edge itself
        .copyInto(new ArrayList<>());
  }

  private static <N> Iterable<EndpointPair<N>> outEdges(CTree<N> tree, EndpointPair<N> edge) {
    N origin = edge.target();
    return tree.successors(origin)
        .stream()
        .map(s -> EndpointPair.ordered(origin, s))
        .collect(toList());
  }
}
