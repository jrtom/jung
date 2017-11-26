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

import com.google.common.graph.EndpointPair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.RandomAccess;
import org.junit.Test;

/** Tests for repeated node and edge addition and removal in a {@link CTree}. */
public final class CTreeMutationTest {
  private static final int NUM_TRIALS = 50;
  private static final int NUM_NODES = 100;
  private static final int NUM_EDGES = 1000;
  private static final int NODE_POOL_SIZE = 1000; // must be >> NUM_NODES

  @Test
  public void mutableCTree() {
    testCTreeMutation(TreeBuilder.builder());
  }

  private static void testCTreeMutation(TreeBuilder<? super Integer> treeBuilder) {
    Random gen = new Random(42); // Fixed seed so test results are deterministic.

    for (int trial = 0; trial < NUM_TRIALS; ++trial) {
      MutableCTree<Integer> tree = treeBuilder.build();

      assertThat(tree.nodes()).isEmpty();
      assertThat(tree.edges()).isEmpty();
      AbstractCTreeTest.validateTree(tree);

      while (tree.nodes().size() < NUM_NODES) {
        tree.addNode(gen.nextInt(NODE_POOL_SIZE));
      }
      ArrayList<Integer> nodeList = new ArrayList<>(tree.nodes());
      while (tree.edges().size() < NUM_EDGES) {
        tree.putEdge(getRandomElement(nodeList, gen), getRandomElement(nodeList, gen));
      }
      ArrayList<EndpointPair<Integer>> edgeList = new ArrayList<>(tree.edges());

      assertThat(tree.nodes()).hasSize(NUM_NODES);
      assertThat(tree.edges()).hasSize(NUM_EDGES);
      AbstractCTreeTest.validateTree(tree);

      Collections.shuffle(edgeList, gen);
      int numEdgesToRemove = gen.nextInt(NUM_EDGES);
      for (int i = 0; i < numEdgesToRemove; ++i) {
        EndpointPair<Integer> edge = edgeList.get(i);
        assertThat(tree.removeEdge(edge.nodeU(), edge.nodeV())).isTrue();
      }

      assertThat(tree.nodes()).hasSize(NUM_NODES);
      assertThat(tree.edges()).hasSize(NUM_EDGES - numEdgesToRemove);
      AbstractCTreeTest.validateTree(tree);

      Collections.shuffle(nodeList, gen);
      int numNodesToRemove = gen.nextInt(NUM_NODES);
      for (int i = 0; i < numNodesToRemove; ++i) {
        assertThat(tree.removeNode(nodeList.get(i))).isTrue();
      }

      assertThat(tree.nodes()).hasSize(NUM_NODES - numNodesToRemove);
      // Number of edges remaining is unknown (node's incident edges have been removed).
      AbstractCTreeTest.validateTree(tree);

      for (int i = numNodesToRemove; i < NUM_NODES; ++i) {
        assertThat(tree.removeNode(nodeList.get(i))).isTrue();
      }

      assertThat(tree.nodes()).isEmpty();
      assertThat(tree.edges()).isEmpty(); // no edges can remain if there's no nodes
      AbstractCTreeTest.validateTree(tree);

      Collections.shuffle(nodeList, gen);
      for (Integer node : nodeList) {
        assertThat(tree.addNode(node)).isTrue();
      }
      Collections.shuffle(edgeList, gen);
      for (EndpointPair<Integer> edge : edgeList) {
        assertThat(tree.putEdge(edge.nodeU(), edge.nodeV())).isTrue();
      }

      assertThat(tree.nodes()).hasSize(NUM_NODES);
      assertThat(tree.edges()).hasSize(NUM_EDGES);
      AbstractCTreeTest.validateTree(tree);
    }
  }

  private static <L extends List<T> & RandomAccess, T> T getRandomElement(L list, Random gen) {
    return list.get(gen.nextInt(list.size()));
  }
}
