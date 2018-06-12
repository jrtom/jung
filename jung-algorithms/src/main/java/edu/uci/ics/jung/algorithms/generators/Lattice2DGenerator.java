/*
 * Copyright (c) 2009, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */

package edu.uci.ics.jung.algorithms.generators;

import static java.util.stream.Collectors.toSet;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Simple generator of graphs in the shape of an m x n lattice where each node is adjacent to each
 * of its neighbors (to the left, right, up, and down). May be toroidal, in which case the nodes on
 * the boundaries are connected to their counterparts on the opposite boundaries as well.
 *
 * @author Joshua O'Madadhain
 */
public class Lattice2DGenerator<N, E> {
  private final int rowCount;
  private final int colCount;
  private final boolean toroidal;

  // TODO: consider using a Builder here as well

  /**
   * Creates an instance which generates graphs of the specified dimensions. If {@code toroidal} is
   * true, the nodes on the column 0 are connected to the nodes on column n-1, and the nodes on row
   * 0 are connected to the nodes on row n-1, forming a torus shape.
   *
   * @param rowCount
   * @param colCount
   * @param toroidal
   */
  public Lattice2DGenerator(int rowCount, int colCount, boolean toroidal) {
    // TODO: relax the row/col count restrictions to be >= 3 once we get the random selection
    // mechanism
    // in KleinbergSmallWorld to behave better
    Preconditions.checkArgument(rowCount >= 4, "row count must be >= 4");
    Preconditions.checkArgument(colCount >= 4, "column count must be >= 4");
    this.rowCount = rowCount;
    this.colCount = colCount;
    this.toroidal = toroidal;
  }

  /**
   * Creates a lattice-shaped {@code Network} with the specified node and edge suppliers, and
   * direction.
   *
   * @param directed
   * @param nodeFactory
   * @param edgeFactory
   * @return
   */
  public MutableNetwork<N, E> generateNetwork(
      boolean directed, Supplier<N> nodeFactory, Supplier<E> edgeFactory) {
    Preconditions.checkNotNull(nodeFactory);
    Preconditions.checkNotNull(edgeFactory);

    int node_count = rowCount * colCount;

    int boundary_adjustment = (toroidal ? 0 : 1);
    int edge_count =
        colCount * (rowCount - boundary_adjustment)
            + // vertical edges
            rowCount * (colCount - boundary_adjustment); // horizontal edges
    if (directed) {
      edge_count *= 2;
    }

    NetworkBuilder<Object, Object> builder =
        directed ? NetworkBuilder.directed() : NetworkBuilder.undirected();
    MutableNetwork<N, E> graph =
        builder.expectedNodeCount(node_count).expectedEdgeCount(edge_count).build();

    for (int i = 0; i < node_count; i++) {
      N v = nodeFactory.get();
      graph.addNode(v);
    }
    List<N> elements = new ArrayList<N>(graph.nodes());

    int end_row = toroidal ? rowCount : rowCount - 1;
    int end_col = toroidal ? colCount : colCount - 1;

    // fill in edges
    // down
    for (int i = 0; i < end_row; i++) {
      for (int j = 0; j < colCount; j++) {
        graph.addEdge(
            elements.get(getIndex(i, j)), elements.get(getIndex(i + 1, j)), edgeFactory.get());
      }
    }
    // right
    for (int i = 0; i < rowCount; i++) {
      for (int j = 0; j < end_col; j++) {
        graph.addEdge(
            elements.get(getIndex(i, j)), elements.get(getIndex(i, j + 1)), edgeFactory.get());
      }
    }

    // if the graph is directed, fill in the edges going the other directions
    if (graph.isDirected()) {
      Set<EndpointPair<N>> endpointPairs =
          graph.edges().stream().map(graph::incidentNodes).collect(toSet());

      for (EndpointPair<N> endpoints : endpointPairs) {
        graph.addEdge(endpoints.target(), endpoints.source(), edgeFactory.get());
      }
    }
    return graph;
  }

  // TODO: this way of getting a Distance is kind of messed up: it shouldn't be possible to
  // get a Distance for a graph other than the one provided, but it is because of how the API works.
  // Fix this.

  /**
   * Returns a {@code Distance} implementation that assumes that {@code graph} is lattice-shaped.
   *
   * @param graph
   * @return
   */
  public Distance<N> distance(Graph<N> graph) {
    return new LatticeDistance(graph);
  }

  private class LatticeDistance implements Distance<N> {
    private final Map<N, Integer> nodeIndices = new HashMap<>();
    private final LoadingCache<N, LoadingCache<N, Number>> distances =
        CacheBuilder.newBuilder()
            .build(
                new CacheLoader<N, LoadingCache<N, Number>>() {
                  public LoadingCache<N, Number> load(N source) {
                    return CacheBuilder.newBuilder()
                        .build(
                            new CacheLoader<N, Number>() {
                              public Number load(N target) {
                                return getDistance(source, target);
                              }
                            });
                  }
                });

    private LatticeDistance(Graph<N> graph) {
      Preconditions.checkNotNull(graph);
      int index = 0;
      for (N node : graph.nodes()) {
        nodeIndices.put(node, index++);
      }
    }

    public Number getDistance(N source, N target) {
      int sourceIndex = nodeIndices.get(source);
      int targetIndex = nodeIndices.get(target);
      int sourceRow = getRow(sourceIndex);
      int sourceCol = getCol(sourceIndex);
      int targetRow = getRow(targetIndex);
      int targetCol = getCol(targetIndex);

      int v_dist = Math.abs(sourceRow - targetRow);
      int h_dist = Math.abs(sourceCol - targetCol);
      if (toroidal) {
        v_dist = Math.min(v_dist, Math.abs(rowCount - v_dist) + 1);
        h_dist = Math.min(h_dist, Math.abs(colCount - h_dist) + 1);
      }
      return v_dist + h_dist;
    }

    @Override
    public Map<N, ? extends Number> getDistanceMap(N source) {
      return distances.getUnchecked(source).asMap();
    }

    /**
     * @param i index of the node whose row we want
     * @return the row in which the node with index {@code i} is found
     */
    private int getRow(int i) {
      return i / colCount;
    }

    /**
     * @param i index of the node whose column we want
     * @return the column in which the node with index {@code i} is found
     */
    private int getCol(int i) {
      return i % colCount;
    }
  }

  int getIndex(int i, int j) {
    return ((mod(i, rowCount)) * colCount) + (mod(j, colCount));
  }

  private int mod(int i, int modulus) {
    int i_mod = i % modulus;
    return i_mod >= 0 ? i_mod : i_mod + modulus;
  }
}
