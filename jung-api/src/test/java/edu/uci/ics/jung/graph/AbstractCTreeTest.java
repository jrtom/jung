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
import static com.google.common.truth.Truth8.assertThat;
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graphs;
import com.google.common.graph.ImmutableGraph;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
        assertThat(tree.hasEdgeConnecting(predecessor, node)).isTrue();
        assertThat(tree.depth(predecessor)).isLessThan(tree.depth(node));
      }

      for (N successor : sanityCheckSet(tree.successors(node))) {
        allEndpointPairs.add(EndpointPair.ordered(node, successor));
        assertThat(tree.predecessors(successor)).contains(node);
        assertThat(tree.hasEdgeConnecting(node, successor)).isTrue();
        assertThat(tree.depth(successor)).isGreaterThan(tree.depth(node));
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

  @Test
  public void edges_oneEdge() {
    putEdge(N1, N2);
    assertThat(tree.edges()).containsExactly(EndpointPair.ordered(N1, N2));
  }

  @Test
  public void edges_noEdges() {
    assertThat(tree.edges()).isEmpty();
  }

  @Test
  public void incidentEdges_oneOutgoingEdge() {
    putEdge(N1, N2);
    assertThat(tree.incidentEdges(N1)).containsExactly(EndpointPair.ordered(N1, N2));
  }

  @Test
  public void incidentEdges_oneIncomingEdge() {
    putEdge(N1, N2);
    assertThat(tree.incidentEdges(N2)).containsExactly(EndpointPair.ordered(N1, N2));
  }

  @Test
  public void incidentEdges_noEdges() {
    try {
      tree.incidentEdges(NODE_NOT_IN_TREE);
      fail(FAIL_ERROR_ELEMENT_NOT_IN_TREE);
    } catch (IllegalArgumentException e) {
      assertNodeNotInTreeErrorMessage(e);
    }
  }

  @Test
  public void incidentEdges_outgoingAndIncomingEdges() {
    putEdge(N1, N2);
    putEdge(N2, N3);
    putEdge(N2, N4);
    putEdge(N3, N5);
    assertThat(tree.incidentEdges(N1)).containsExactly(EndpointPair.ordered(N1, N2));
    assertThat(tree.incidentEdges(N2))
        .containsExactly(
            EndpointPair.ordered(N1, N2),
            EndpointPair.ordered(N2, N3),
            EndpointPair.ordered(N2, N4));
    assertThat(tree.incidentEdges(N3))
        .containsExactly(EndpointPair.ordered(N2, N3), EndpointPair.ordered(N3, N5));
    assertThat(tree.incidentEdges(N4)).containsExactly(EndpointPair.ordered(N2, N4));
    assertThat(tree.incidentEdges(N5)).containsExactly(EndpointPair.ordered(N3, N5));
  }

  @Test
  public void hasEdgeConnecting_oneEdge() {
    putEdge(N1, N2);
    assertThat(tree.hasEdgeConnecting(N1, N2)).isTrue();
    // Edge direction handled correctly
    assertThat(tree.hasEdgeConnecting(N2, N1)).isFalse();
  }

  @Test
  public void hasEdgeConnecting_noEdges() {
    ImmutableSet<Integer> allPossibleNodes = ImmutableSet.of(N1, N2, N3, N4, N5, N6);
    Set<List<Integer>> allPairs =
        Sets.cartesianProduct(ImmutableList.of(allPossibleNodes, allPossibleNodes));
    for (List<Integer> pair : allPairs) {
      try {
        tree.hasEdgeConnecting(pair.get(0), pair.get(1));
      } catch (IllegalArgumentException e) {
        assertNodeNotInTreeErrorMessage(e);
      }
    }
  }

  @Test
  public void hasEdgeConnecting_multipleEdges() {
    putEdge(N1, N2);
    putEdge(N2, N3);
    putEdge(N3, N4);
    putEdge(N3, N5);

    assertThat(tree.hasEdgeConnecting(N1, N2)).isTrue();
    // Edge direction handled correctly
    assertThat(tree.hasEdgeConnecting(N2, N1)).isFalse();

    assertThat(tree.hasEdgeConnecting(N2, N3)).isTrue();
    // Edge direction handled correctly
    assertThat(tree.hasEdgeConnecting(N3, N2)).isFalse();

    assertThat(tree.hasEdgeConnecting(N3, N4)).isTrue();
    // Edge direction handled correctly
    assertThat(tree.hasEdgeConnecting(N4, N3)).isFalse();
    assertThat(tree.hasEdgeConnecting(N3, N5)).isTrue();
    // Edge direction handled correctly
    assertThat(tree.hasEdgeConnecting(N5, N3)).isFalse();

    // Sanity check
    assertThat(tree.hasEdgeConnecting(N5, N1)).isFalse();
  }

  @Test
  public void root_oneNode() {
    addNode(N1);
    assertThat(tree.root()).hasValue(N1);
  }

  @Test
  public void root_noNodes() {
    assertThat(tree.root()).isEmpty();
  }

  @Test
  public void root_oneEdge() {
    putEdge(N1, N2);
    assertThat(tree.root()).hasValue(N1);
  }

  @Test
  public void root_multipleEdges() {
    buildTreeWithMultipleEdges();
    assertThat(tree.root()).hasValue(N1);
  }

  @Test
  public void depth_oneNode() {
    addNode(N1);
    assertThat(tree.depth(N1)).isEqualTo(0);
    try {
      tree.depth(N2);
      fail(FAIL_ERROR_ELEMENT_NOT_IN_TREE);
    } catch (IllegalArgumentException e) {
      assertNodeNotInTreeErrorMessage(e);
    }
  }

  @Test
  public void depth_noNodes() {
    try {
      tree.depth(N1);
      fail(FAIL_ERROR_ELEMENT_NOT_IN_TREE);
    } catch (IllegalArgumentException e) {
      assertNodeNotInTreeErrorMessage(e);
    }
  }

  @Test
  public void depth_oneEdge() {
    putEdge(N1, N2);
    assertThat(tree.depth(N1)).isEqualTo(0);
    assertThat(tree.depth(N2)).isEqualTo(1);
    try {
      tree.depth(N3);
      fail(FAIL_ERROR_ELEMENT_NOT_IN_TREE);
    } catch (IllegalArgumentException e) {
      assertNodeNotInTreeErrorMessage(e);
    }
  }

  @Test
  public void depth_multipleEdges() {
    buildTreeWithMultipleEdges();
    assertThat(tree.depth(N1)).named("tree.depth(%s)", N1).isEqualTo(0);
    assertThat(tree.depth(N2)).named("tree.depth(%s)", N2).isEqualTo(1);
    assertThat(tree.depth(N3)).named("tree.depth(%s)", N3).isEqualTo(1);
    assertThat(tree.depth(N4)).named("tree.depth(%s)", N4).isEqualTo(2);
    assertThat(tree.depth(N5)).named("tree.depth(%s)", N5).isEqualTo(2);
    assertThat(tree.depth(N6)).named("tree.depth(%s)", N6).isEqualTo(3);
  }

  @Test
  public void height_oneNode() {
    addNode(N1);
    assertThat(tree.height()).hasValue(0);
  }

  @Test
  public void height_noNodes() {
    assertThat(tree.height()).isEmpty();
  }

  @Test
  public void height_oneEdge() {
    putEdge(N1, N2);
    assertThat(tree.height()).hasValue(1);
  }

  @Test
  public void height_multipleEdges() {
    buildTreeWithMultipleEdges();
    assertThat(tree.height()).hasValue(3);
  }

  private void buildTreeWithMultipleEdges() {
    putEdge(N1, N2);
    putEdge(N1, N3);
    putEdge(N3, N4);
    putEdge(N2, N5);
    putEdge(N5, N6);
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
