/*
 * Copyright (C) 2014 The Guava Authors
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
import static edu.uci.ics.jung.graph.TestUtil.FAIL_ERROR_ELEMENT_NOT_IN_TREE;
import static edu.uci.ics.jung.graph.TestUtil.FAIL_ERROR_NODEU_NOT_IN_TREE;
import static edu.uci.ics.jung.graph.TestUtil.FAIL_ERROR_NODEV_IN_TREE;
import static edu.uci.ics.jung.graph.TestUtil.FAIL_ERROR_TREE_HAS_ROOT;
import static edu.uci.ics.jung.graph.TestUtil.assertNodeNotInTreeErrorMessage;
import static edu.uci.ics.jung.graph.TestUtil.assertNodeUNotInTreeErrorMessage;
import static edu.uci.ics.jung.graph.TestUtil.assertNodeVAlreadyElementOfTreeErrorMessage;
import static edu.uci.ics.jung.graph.TestUtil.assertStronglyEquivalent;
import static edu.uci.ics.jung.graph.TestUtil.assertTreeAlreadyHasRootErrorMessage;
import static edu.uci.ics.jung.graph.TestUtil.sanityCheckSet;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graphs;
import com.google.common.graph.ImmutableGraph;
import com.google.common.truth.Truth8;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

// TODO: Test CTree#root, CTree#depth and CTree#height
public abstract class AbstractCTreeTest {
  MutableCTree<Integer> tree;
  static final Integer N1 = 1;
  static final Integer N2 = 2;
  static final Integer N3 = 3;
  static final Integer N4 = 4;
  static final Integer N5 = 5;
  static final Integer N6 = 6;
  private static final Integer NODE_NOT_IN_TREE = 1000;

  static final String ERROR_MODIFIABLE_SET = "Set returned is unexpectedly modifiable";
  static final String ERROR_SELF_LOOP = "self-loops are not allowed";
  static final String ERROR_ADDED_SELF_LOOP = "Should not be allowed to add a self-loop edge.";

  /** Creates and returns an instance of the tree to be tested. */
  public abstract MutableCTree<Integer> createTree();

  /**
   * A proxy method that adds the node {@code n} to the tree being tested.
   *
   * @return {@code true} iff the tree was modified as a result of this call
   */
  protected boolean addNode(Integer n) {
    return tree.addNode(n);
  }

  /**
   * A proxy method that adds the edge {@code e} to the tree being tested.
   *
   * @return {@code true} iff the tree was modified as a result of this call
   */
  protected boolean putEdge(Integer n1, Integer n2) {
    return tree.putEdge(n1, n2);
  }

  @Before
  public void init() {
    tree = createTree();
  }

  @After
  public void validateTreeState() {
    validateTree(tree);
  }

  static <N> void validateTree(CTree<N> tree) {
    // TODO: Replace Graphs#copyOf with a tree-specific copyOf method if it ever turns up
    assertStronglyEquivalent(tree, Graphs.copyOf(tree));
    assertStronglyEquivalent(tree, ImmutableGraph.copyOf(tree));

    String treeString = tree.toString();
    assertThat(treeString).contains("isDirected: true");
    assertThat(treeString).contains("allowsSelfLoops: false");

    int nodeStart = treeString.indexOf("nodes:");
    int edgeStart = treeString.indexOf("edges:");
    String nodeString = treeString.substring(nodeStart, edgeStart);

    Set<EndpointPair<N>> allEndpointPairs = new HashSet<>();

    for (N node : sanityCheckSet(tree.nodes())) {
      assertThat(nodeString).contains(node.toString());

      assertThat(tree.degree(node)).isEqualTo(tree.inDegree(node) + tree.outDegree(node));
      assertThat(tree.predecessors(node)).hasSize(tree.inDegree(node));
      assertThat(tree.successors(node)).hasSize(tree.outDegree(node));

      for (N adjacentNode : sanityCheckSet(tree.adjacentNodes(node))) {
        assertThat(
                tree.predecessors(node).contains(adjacentNode)
                    || tree.successors(node).contains(adjacentNode))
            .isTrue();
      }

      for (N predecessor : sanityCheckSet(tree.predecessors(node))) {
        assertThat(tree.successors(predecessor)).contains(node);
        // TODO: Uncomment line below when we target Guava 23.0+
        // assertThat(tree.hasEdgeConnecting(predecessor, node)).isTrue();
      }

      for (N successor : sanityCheckSet(tree.successors(node))) {
        allEndpointPairs.add(EndpointPair.ordered(node, successor));
        assertThat(tree.predecessors(successor)).contains(node);
        // TODO: Uncomment line below when we target Guava 23.0+
        // assertThat(tree.hasEdgeConnecting(node, successor)).isTrue();
      }
    }

    sanityCheckSet(tree.edges());
    assertThat(tree.edges()).doesNotContain(EndpointPair.ordered(new Object(), new Object()));
    assertThat(tree.edges()).isEqualTo(allEndpointPairs);
  }

  /**
   * Verifies that the {@code Set} returned by {@code nodes} has the expected mutability property
   * (see Guava's {@code Graph} documentation for more information).
   */
  @Test
  public abstract void nodes_checkReturnedSetMutability();

  /**
   * Verifies that the {@code Set} returned by {@code adjacentNodes} has the expected mutability
   * property (see Guava's {@code Graph} documentation for more information).
   */
  @Test
  public abstract void adjacentNodes_checkReturnedSetMutability();

  /**
   * Verifies that the {@code Set} returned by {@code predecessors} has the expected mutability
   * property (see Guava's {@code Graph} documentation for more information).
   */
  @Test
  public abstract void predecessors_checkReturnedSetMutability();

  /**
   * Verifies that the {@code Set} returned by {@code successors} has the expected mutability
   * property (see Guava's {@code Graph} documentation for more information).
   */
  @Test
  public abstract void successors_checkReturnedSetMutability();

  @Test
  public void nodes_oneNode() {
    addNode(N1);
    assertThat(tree.nodes()).containsExactly(N1);
  }

  @Test
  public void nodes_noNodes() {
    assertThat(tree.nodes()).isEmpty();
  }

  @Test
  public void adjacentNodes_oneEdge() {
    putEdge(N1, N2);
    assertThat(tree.adjacentNodes(N1)).containsExactly(N2);
    assertThat(tree.adjacentNodes(N2)).containsExactly(N1);
  }

  @Test
  public void adjacentNodes_noAdjacentNodes() {
    addNode(N1);
    assertThat(tree.adjacentNodes(N1)).isEmpty();
  }

  @Test
  public void adjacentNodes_nodeNotInTree() {
    try {
      tree.adjacentNodes(NODE_NOT_IN_TREE);
      fail(FAIL_ERROR_ELEMENT_NOT_IN_TREE);
    } catch (IllegalArgumentException e) {
      assertNodeNotInTreeErrorMessage(e);
    }
  }

  @Test
  public void predecessors_noPredecessors() {
    addNode(N1);
    assertThat(tree.predecessors(N1)).isEmpty();
  }

  @Test
  public void predecessors_nodeNotInTree() {
    try {
      tree.predecessors(NODE_NOT_IN_TREE);
      fail(FAIL_ERROR_ELEMENT_NOT_IN_TREE);
    } catch (IllegalArgumentException e) {
      assertNodeNotInTreeErrorMessage(e);
    }
  }

  @Test
  public void successors_noSuccessors() {
    addNode(N1);
    assertThat(tree.successors(N1)).isEmpty();
  }

  @Test
  public void successors_nodeNotInTree() {
    try {
      tree.successors(NODE_NOT_IN_TREE);
      fail(TestUtil.FAIL_ERROR_ELEMENT_NOT_IN_TREE);
    } catch (IllegalArgumentException e) {
      assertNodeNotInTreeErrorMessage(e);
    }
  }

  @Test
  public void degree_oneEdge() {
    putEdge(N1, N2);
    assertThat(tree.degree(N1)).isEqualTo(1);
    assertThat(tree.degree(N2)).isEqualTo(1);
  }

  @Test
  public void degree_isolatedNode() {
    addNode(N1);
    assertThat(tree.degree(N1)).isEqualTo(0);
  }

  @Test
  public void degree_nodeNotInTree() {
    try {
      tree.degree(NODE_NOT_IN_TREE);
      fail(FAIL_ERROR_ELEMENT_NOT_IN_TREE);
    } catch (IllegalArgumentException e) {
      assertNodeNotInTreeErrorMessage(e);
    }
  }

  @Test
  public void inDegree_isolatedNode() {
    addNode(N1);
    assertThat(tree.inDegree(N1)).isEqualTo(0);
  }

  @Test
  public void inDegree_nodeNotInTree() {
    try {
      tree.inDegree(NODE_NOT_IN_TREE);
      fail(FAIL_ERROR_ELEMENT_NOT_IN_TREE);
    } catch (IllegalArgumentException e) {
      assertNodeNotInTreeErrorMessage(e);
    }
  }

  @Test
  public void outDegree_isolatedNode() {
    addNode(N1);
    assertThat(tree.outDegree(N1)).isEqualTo(0);
  }

  @Test
  public void outDegree_nodeNotInTree() {
    try {
      tree.outDegree(NODE_NOT_IN_TREE);
      fail(FAIL_ERROR_ELEMENT_NOT_IN_TREE);
    } catch (IllegalArgumentException e) {
      assertNodeNotInTreeErrorMessage(e);
    }
  }

  @Test
  public void predecessors_oneEdge() {
    putEdge(N1, N2);
    assertThat(tree.predecessors(N2)).containsExactly(N1);
    // Edge direction handled correctly
    assertThat(tree.predecessors(N1)).isEmpty();
  }

  @Test
  public void successors_oneEdge() {
    putEdge(N1, N2);
    assertThat(tree.successors(N1)).containsExactly(N2);
    // Edge direction handled correctly
    assertThat(tree.successors(N2)).isEmpty();
  }

  @Test
  public void inDegree_oneEdge() {
    putEdge(N1, N2);
    assertThat(tree.inDegree(N2)).isEqualTo(1);
    // Edge direction handled correctly
    assertThat(tree.inDegree(N1)).isEqualTo(0);
  }

  @Test
  public void outDegree_oneEdge() {
    putEdge(N1, N2);
    assertThat(tree.outDegree(N1)).isEqualTo(1);
    // Edge direction handled correctly
    assertThat(tree.outDegree(N2)).isEqualTo(0);
  }

  // Element Mutation

  @Test
  public void addNode_newNode() {
    assertThat(addNode(N1)).isTrue();
    assertThat(tree.nodes()).contains(N1);
  }

  @Test
  public void addNode_existingNode() {
    addNode(N1);
    ImmutableSet<Integer> nodes = ImmutableSet.copyOf(tree.nodes());
    assertThat(addNode(N1)).isFalse();
    assertThat(tree.nodes()).containsExactlyElementsIn(nodes);
  }

  @Test
  public void addNode_twoNodes() {
    addNode(N1);
    try {
      addNode(N2);
      fail(FAIL_ERROR_TREE_HAS_ROOT);
    } catch (IllegalArgumentException e) {
      assertTreeAlreadyHasRootErrorMessage(e);
    }
  }

  @Test
  public void addNode_existingEdge() {
    putEdge(N1, N2);
    try {
      addNode(N3);
      fail(FAIL_ERROR_TREE_HAS_ROOT);
    } catch (IllegalArgumentException e) {
      assertTreeAlreadyHasRootErrorMessage(e);
    }
  }

  @Test
  public void removeNode_existingNode() {
    putEdge(N1, N2);
    putEdge(N1, N4);
    assertThat(tree.removeNode(N2)).isTrue();
    assertThat(tree.removeNode(N2)).isFalse();
    assertThat(tree.nodes()).containsExactly(N1, N4);
    assertThat(tree.successors(N1)).containsExactly(N4);
    assertThat(tree.predecessors(N4)).containsExactly(N1);
  }

  @Test
  public void removeNode_nodeNotPresent() {
    addNode(N1);
    ImmutableSet<Integer> nodes = ImmutableSet.copyOf(tree.nodes());
    assertThat(tree.removeNode(NODE_NOT_IN_TREE)).isFalse();
    assertThat(tree.nodes()).containsExactlyElementsIn(nodes);
  }

  @Test
  public void removeNode_queryAfterRemoval() {
    addNode(N1);
    @SuppressWarnings("unused")
    Set<Integer> unused = tree.adjacentNodes(N1); // ensure cache (if any) is populated
    assertThat(tree.removeNode(N1)).isTrue();
    try {
      tree.adjacentNodes(N1);
      fail(FAIL_ERROR_ELEMENT_NOT_IN_TREE);
    } catch (IllegalArgumentException e) {
      assertNodeNotInTreeErrorMessage(e);
    }
  }

  @Test
  public void addEdge_existingRoot() {
    addNode(N1);
    assertThat(putEdge(N1, N2)).isTrue();
  }

  @Test
  public void addEdge_noExistingRoot() {
    assertThat(putEdge(N1, N2)).isTrue();
  }

  @Test
  public void addEdge_existingEdgeBetweenSameNodes() {
    putEdge(N1, N2);
    assertThat(putEdge(N1, N2)).isFalse();
  }

  @Test
  public void addEdge_selfLoop() {
    try {
      putEdge(N1, N1);
      fail(ERROR_ADDED_SELF_LOOP);
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().contains(ERROR_SELF_LOOP);
    }
  }

  @Test
  public void addEdge_antiparallelEdges() {
    putEdge(N1, N2);
    try {
      putEdge(N2, N1);
      fail(FAIL_ERROR_NODEV_IN_TREE);
    } catch (IllegalArgumentException e) {
      assertNodeVAlreadyElementOfTreeErrorMessage(e, N1);
    }
  }

  @Test
  public void addEdge_disconnectedEdges() {
    putEdge(N1, N2);
    try {
      putEdge(N3, N4);
      fail(FAIL_ERROR_NODEU_NOT_IN_TREE);
    } catch (IllegalArgumentException e) {
      assertNodeUNotInTreeErrorMessage(e, N3);
    }
  }

  @Test
  public void addEdge_nodesNotInTree() {
    addNode(N1);

    assertThat(putEdge(N1, N5)).isTrue();
    assertThat(putEdge(N1, N4)).isTrue();
    assertThat(putEdge(N1, N2)).isTrue();
    assertThat(putEdge(N4, N3)).isTrue();
    assertThat(putEdge(N3, N6)).isTrue();

    assertThat(tree.nodes()).containsExactly(N1, N5, N4, N2, N3, N6).inOrder();
    assertThat(tree.successors(N1)).containsExactly(N2, N4, N5);
    assertThat(tree.successors(N2)).isEmpty();
    assertThat(tree.successors(N3)).containsExactly(N6);
    assertThat(tree.successors(N4)).containsExactly(N3);
    assertThat(tree.successors(N5)).isEmpty();
    assertThat(tree.successors(N6)).isEmpty();

    assertThat(tree.predecessors(N1)).isEmpty();
    assertThat(tree.predecessors(N2)).containsExactly(N1);
    assertThat(tree.predecessors(N3)).containsExactly(N4);
    assertThat(tree.predecessors(N4)).containsExactly(N1);
    assertThat(tree.predecessors(N5)).containsExactly(N1);
    assertThat(tree.predecessors(N6)).containsExactly(N3);
  }

  @Test
  public void removeEdge_existingEdge() {
    putEdge(N1, N2);
    assertThat(tree.successors(N1)).containsExactly(N2);
    assertThat(tree.predecessors(N2)).containsExactly(N1);
    assertThat(tree.removeEdge(N1, N2)).isTrue();
    assertThat(tree.removeEdge(N1, N2)).isFalse();
    assertThat(tree.successors(N1)).isEmpty();
    try {
      tree.predecessors(N2);
      fail(FAIL_ERROR_ELEMENT_NOT_IN_TREE);
    } catch (IllegalArgumentException e) {
      assertNodeNotInTreeErrorMessage(e);
    }
  }

  @Test
  public void removeEdge_oneOfMany() {
    putEdge(N1, N2);
    putEdge(N1, N3);
    putEdge(N1, N4);
    assertThat(tree.removeEdge(N1, N3)).isTrue();
    assertThat(tree.adjacentNodes(N1)).containsExactly(N2, N4);
  }

  @Test
  public void removeEdge_nodeNotPresent() {
    putEdge(N1, N2);
    assertThat(tree.removeEdge(N1, NODE_NOT_IN_TREE)).isFalse();
    assertThat(tree.successors(N1)).contains(N2);
  }
}
