package edu.uci.ics.jung.graph;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static edu.uci.ics.jung.graph.GraphConstants.EDGE_NOT_IN_TREE;
import static edu.uci.ics.jung.graph.GraphConstants.NODEU_NOT_IN_TREE;
import static edu.uci.ics.jung.graph.GraphConstants.NODEV_IN_TREE;
import static edu.uci.ics.jung.graph.GraphConstants.NODE_NOT_IN_TREE;
import static edu.uci.ics.jung.graph.GraphConstants.NODE_ROOT_OF_TREE;
import static edu.uci.ics.jung.graph.GraphConstants.SELF_LOOP_NOT_ALLOWED;

import com.google.common.collect.Iterables;
import com.google.common.graph.AbstractNetwork;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Traverser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// TODO: Add tests in similar fashion to CTreeTest and AbstractCTreeTest
class DelegateCTreeNetwork<N, E> extends AbstractNetwork<N, E>
    implements MutableCTreeNetwork<N, E> {
  private final MutableNetwork<N, E> delegate;
  private final Map<N, Integer> depths;
  private Optional<Integer> height;
  private Optional<N> root;

  DelegateCTreeNetwork(MutableNetwork<N, E> graph, Optional<N> root) {
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
  public Optional<E> inEdge(N node) {
    Set<E> inEdges = delegate.inEdges(node);
    checkState(inEdges.size() <= 1);
    return inEdges.isEmpty() ? Optional.<E>empty() : Optional.of(Iterables.getOnlyElement(inEdges));
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
  public Set<E> edges() {
    return delegate.edges();
  }

  @Override
  public Set<E> adjacentEdges(E edge) {
    checkNotNull(edge, "edge");
    return delegate.adjacentEdges(edge);
  }

  @Override
  public boolean allowsParallelEdges() {
    return false;
  }

  @Override
  public Graph<N> asGraph() {
    return delegate.asGraph();
  }

  @Override
  public ElementOrder<E> edgeOrder() {
    return delegate.edgeOrder();
  }

  @Override
  public Set<E> edgesConnecting(N nodeU, N nodeV) {
    checkNotNull(nodeU, "nodeU");
    checkNotNull(nodeV, "nodeV");
    checkArgument(delegate.nodes().contains(nodeU), NODE_NOT_IN_TREE, nodeU);
    checkArgument(delegate.nodes().contains(nodeV), NODE_NOT_IN_TREE, nodeV);
    return delegate.edgesConnecting(nodeU, nodeV);
  }

  @Override
  public Optional<E> edgeConnecting(N nodeU, N nodeV) {
    return delegate.edgeConnecting(nodeU, nodeV);
  }

  @Override
  public E edgeConnectingOrNull(N nodeU, N nodeV) {
    return delegate.edgeConnectingOrNull(nodeU, nodeV);
  }

  @Override
  public boolean hasEdgeConnecting(N nodeU, N nodeV) {
    return delegate.hasEdgeConnecting(nodeU, nodeV);
  }

  @Override
  public Set<E> inEdges(N node) {
    checkNotNull(node, "node");
    checkArgument(delegate.nodes().contains(node), NODE_NOT_IN_TREE, node);
    return delegate.inEdges(node);
  }

  @Override
  public Set<E> incidentEdges(N node) {
    checkNotNull(node, "node");
    checkArgument(delegate.nodes().contains(node), NODE_NOT_IN_TREE, node);
    return delegate.incidentEdges(node);
  }

  @Override
  public EndpointPair<N> incidentNodes(E edge) {
    checkNotNull(edge, "edge");
    checkArgument(delegate.edges().contains(edge), EDGE_NOT_IN_TREE, edge);
    return delegate.incidentNodes(edge);
  }

  @Override
  public Set<E> outEdges(N node) {
    checkNotNull(node, "node");
    checkArgument(delegate.nodes().contains(node), NODE_NOT_IN_TREE, node);
    return delegate.outEdges(node);
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
  public boolean addEdge(N nodeU, N nodeV, E edge) {
    checkNotNull(nodeU, "nodeU");
    checkNotNull(nodeV, "nodeV");
    checkNotNull(edge, "edge");
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
    return delegate.addEdge(nodeU, nodeV, edge);
  }

  @Override
  public boolean addEdge(EndpointPair<N> endpoints, E edge) {
    checkNotNull(endpoints, "endpoints");
    return addEdge(endpoints.nodeU(), endpoints.nodeV(), edge);
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
    this.height = Optional.empty();
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
  public boolean removeEdge(E edge) {
    checkNotNull(edge, "edge");
    delegate.removeEdge(edge);
    // remove the subtree rooted at this edge's target
    return removeNode(delegate.incidentNodes(edge).target());
  }
}
