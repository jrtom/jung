package edu.uci.ics.jung.layout.util;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.Graph;

/** Created by tanelso on 11/13/17. */
public class Tree {
  public static <N> ImmutableSet<N> roots(Graph<N> graph) {
    return graph
        .nodes()
        .stream()
        .filter(node -> graph.predecessors(node).isEmpty())
        .collect(toImmutableSet());
  }
}
