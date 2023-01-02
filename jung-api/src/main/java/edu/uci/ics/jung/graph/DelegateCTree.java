/*
 * Created on Feb 12, 2017
 *
 * Copyright (c) 2017, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.graph;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static edu.uci.ics.jung.graph.GraphConstants.NODEU_NOT_IN_TREE;
import static edu.uci.ics.jung.graph.GraphConstants.NODEV_IN_TREE;
import static edu.uci.ics.jung.graph.GraphConstants.NODE_NOT_IN_TREE;
import static edu.uci.ics.jung.graph.GraphConstants.NODE_ROOT_OF_TREE;
import static edu.uci.ics.jung.graph.GraphConstants.SELF_LOOP_NOT_ALLOWED;

import com.google.common.collect.Iterables;
import com.google.common.graph.AbstractGraph;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.Traverser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class DelegateCTree<N> extends AbstractGraph<N> implements MutableCTree<N> {
  private final MutableGraph<N> delegate;
  private final Map<N, Integer> depths;
  private Optional<Integer> height;
  private Optional<N> root;

  DelegateCTree(MutableGraph<N> graph, Optional<N> root) {
    this.delegate = checkNotNull(graph, "graph");
    this.depths = new HashMap<>();
    setRoot(checkNotNull(root, "root"));
  }

  @Override
  public Optional<N> root() {
    return root;
  }

  @Override
  public Optional<N> predecessor(N node) {
    checkNotNull(node, "node");
    Set<N> predecessors = delegate.predecessors(node);
    checkState(predecessors.size() <= 1);
    return predecessors.isEmpty()
        ? Optional.empty()
        : Optional.of(Iterables.getOnlyElement(predecessors));
  }

  @Override
  public int depth(N node) {
    checkNotNull(node, "node");
    checkArgument(delegate.nodes().contains(node), NODE_NOT_IN_TREE, node);
    return depths.get(node);
  }

  @Override
  public Optional<Integer> height() {
    if (!root.isPresent()) {
      return Optional.empty();
    }
    // Calculate the height
    if (!height.isPresent()) {
      calculateHeight();
    }
    return height;
  }

  @Override
  public boolean isDirected() {
    return true;
  }

  @Override
  public boolean allowsSelfLoops() {
    return false;
  }

  @Override
  public Set<N> adjacentNodes(N node) {
    checkNotNull(node, "node");
    checkArgument(delegate.nodes().contains(node), NODE_NOT_IN_TREE, node);
    return delegate.adjacentNodes(node);
  }

  @Override
  public int degree(N node) {
    checkNotNull(node, "node");
    checkArgument(delegate.nodes().contains(node), NODE_NOT_IN_TREE, node);
    return delegate.degree(node);
  }

  @Override
  public int inDegree(N node) {
    checkNotNull(node, "node");
    checkArgument(delegate.nodes().contains(node), NODE_NOT_IN_TREE, node);
    return delegate.inDegree(node);
  }

  @Override
  public ElementOrder<N> nodeOrder() {
    return delegate.nodeOrder();
  }

  @Override
  public Set<N> nodes() {
    return delegate.nodes();
  }

  @Override
  public int outDegree(N node) {
    checkNotNull(node, "node");
    checkArgument(delegate.nodes().contains(node), NODE_NOT_IN_TREE, node);
    return delegate.outDegree(node);
  }

  @Override
  public Set<N> predecessors(N node) {
    checkNotNull(node, "node");
    checkArgument(delegate.nodes().contains(node), NODE_NOT_IN_TREE, node);
    return delegate.predecessors(node);
  }

  @Override
  public Set<N> successors(N node) {
    checkNotNull(node, "node");
    checkArgument(delegate.nodes().contains(node), NODE_NOT_IN_TREE, node);
    return delegate.successors(node);
  }

  @Override
  public Set<EndpointPair<N>> edges() {
    return delegate.edges();
  }

  @Override
  public Set<EndpointPair<N>> incidentEdges(N node) {
    checkNotNull(node, "node");
    checkArgument(delegate.nodes().contains(node), NODE_NOT_IN_TREE, node);
    return delegate.incidentEdges(node);
  }

  @Override
  public boolean hasEdgeConnecting(N nodeU, N nodeV) {
    checkNotNull(nodeU, "nodeU");
    checkNotNull(nodeV, "nodeV");
    checkArgument(delegate.nodes().contains(nodeU), NODE_NOT_IN_TREE, nodeU);
    checkArgument(delegate.nodes().contains(nodeV), NODE_NOT_IN_TREE, nodeV);
    return delegate.hasEdgeConnecting(nodeU, nodeV);
  }

  @Override
  public boolean addNode(N node) {
    checkNotNull(node, "node");
    if (root.isPresent()) {
      N rootValue = root.get();
      checkArgument(rootValue.equals(node), NODE_ROOT_OF_TREE, node, rootValue);
      return false;
    }
    setRoot(Optional.of(node));
    return true;
  }

  @Override
  public boolean putEdge(N nodeU, N nodeV) {
    checkNotNull(nodeU, "nodeU");
    checkNotNull(nodeV, "nodeV");
    checkArgument(!nodeU.equals(nodeV), SELF_LOOP_NOT_ALLOWED, nodeU);
    if (!root.isPresent()) {
      setRoot(Optional.of(nodeU));
    } else {
      checkArgument(nodes().contains(nodeU), NODEU_NOT_IN_TREE, nodeU, nodeV, nodeU);
      if (successors(nodeU).contains(nodeV)) {
        return false; // edge is already present; no-op
      }
      // verify that nodeV is not in the tree
      checkArgument(!nodes().contains(nodeV), NODEV_IN_TREE, nodeU, nodeV, nodeV);
    }
    setDepth(nodeV, nodeU);
    return delegate.putEdge(nodeU, nodeV);
  }

  @Override
  public boolean putEdge(EndpointPair<N> endpoints) {
    checkNotNull(endpoints, "endpoints");
    return putEdge(endpoints.nodeU(), endpoints.nodeV());
  }

  private void setDepth(N node, N parent) {
    if (parent == null) { // root: both depth and height are 0
      depths.put(node, 0);
      height = Optional.of(0);
    } else {
      depths.putIfAbsent(parent, 0);
      int nodeDepth = Math.max(depths.get(parent) + 1, height.orElse(0));
      depths.put(node, nodeDepth);
      height = Optional.of(nodeDepth);
    }
  }

  private void calculateHeight() {
    // This method is only called when the root is present, so we don't need to check for that.
    int currentHeight = 0;
    List<N> currentLevel = new ArrayList<>(successors(root.orElseThrow(AssertionError::new)));
    List<N> nextLevel;
    while (!currentLevel.isEmpty()) {
      nextLevel = new ArrayList<>();
      currentHeight++; // there's at least one node in the current level
      for (N node : currentLevel) {
        nextLevel.addAll(successors(node));
      }
      currentLevel = nextLevel;
    }
    height = Optional.of(currentHeight);
  }

  @Override
  public boolean removeNode(N node) {
    checkNotNull(node, "node");
    if (!nodes().contains(node)) {
      return false;
    }
    for (N nodeToRemove : Traverser.forTree(delegate).breadthFirst(node)) {
      delegate.removeNode(nodeToRemove);
      depths.remove(nodeToRemove);
    }
    if (root.isPresent() && root.get().equals(node)) {
      setRoot(Optional.empty());
    }
    // Reset the height, since we don't know how it was affected by removing the subtree.
    height = Optional.empty();
    return true;
  }

  private void setRoot(Optional<N> root) {
    this.root = root;
    this.root.ifPresent(
        node -> {
          this.delegate.addNode(node);
          setDepth(node, null);
        });
  }

  @Override
  public boolean removeEdge(N nodeU, N nodeV) {
    checkNotNull(nodeU, "nodeU");
    checkNotNull(nodeV, "nodeV");
    delegate.removeEdge(nodeU, nodeV);
    return removeNode(nodeV);
  }

  @Override
  public boolean removeEdge(EndpointPair<N> endpoints) {
    checkNotNull(endpoints, "endpoints");
    return removeEdge(endpoints.nodeU(), endpoints.nodeV());
  }
}
