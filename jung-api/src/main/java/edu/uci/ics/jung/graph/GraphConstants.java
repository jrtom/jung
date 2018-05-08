package edu.uci.ics.jung.graph;

// TODO(jrtom): Consider making Guava's `common.graph.GraphConstants` public so that duplication
// here can be reduced.
final class GraphConstants {
  static final String NODE_NOT_IN_TREE = "Node %s is not an element of this tree.";
  static final String NODE_ROOT_OF_TREE =
      "Cannot add node %s, as node %s is already the root of this tree.";
  static final String SELF_LOOP_NOT_ALLOWED =
      "Cannot add self-loop edge on node %s, as self-loops are not allowed.";
  static final String EDGE_NOT_IN_TREE = "Edge %s is not an element of this tree.";
  static final String NODEU_NOT_IN_TREE =
      "Cannot add edge from nodeU %s to nodeV %s, as nodeU %s is not an element of this tree.";
  static final String NODEV_IN_TREE =
      "Cannot add edge from nodeU %s to nodeV %s, as nodeV %s is an element of this tree.";
}
