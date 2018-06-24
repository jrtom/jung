/*
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Sep 16, 2008
 */
package edu.uci.ics.jung.algorithms.scoring;

import com.google.common.base.Preconditions;
import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.util.MapBinaryHeap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;

/**
 * Computes betweenness centrality for each node and edge in the graph.
 *
 * @see "Ulrik Brandes: A Faster Algorithm for Betweenness Centrality. Journal of Mathematical
 *     Sociology 25(2):163-177, 2001."
 */
public class BetweennessCentrality<N, E> implements NodeScorer<N, Double>, EdgeScorer<E, Double> {
  protected Network<N, E> graph;
  protected Map<N, Double> node_scores;
  protected Map<E, Double> edge_scores;
  protected Map<N, BetweennessData> node_data;

  /**
   * Calculates betweenness scores based on the all-pairs unweighted shortest paths in the graph.
   *
   * @param graph the graph for which the scores are to be calculated
   */
  public BetweennessCentrality(Network<N, E> graph) {
    initialize(graph);
    computeBetweenness(new LinkedList<N>(), n -> 1);
  }

  /**
   * Calculates betweenness scores based on the all-pairs weighted shortest paths in the graph.
   *
   * <p>NOTE: This version of the algorithm may not work correctly on all graphs; we're still
   * working out the bugs. Use at your own risk.
   *
   * @param graph the graph for which the scores are to be calculated
   * @param edge_weights the edge weights to be used in the path length calculations
   */
  public BetweennessCentrality(
      Network<N, E> graph, Function<? super E, ? extends Number> edge_weights) {
    // reject negative-weight edges up front
    for (E e : graph.edges()) {
      double e_weight = edge_weights.apply(e).doubleValue();
      Preconditions.checkArgument(e_weight >= 0, "Weight for edge '%s' is < 0: %d", e, e_weight);
    }

    initialize(graph);
    computeBetweenness(
        new MapBinaryHeap<N>(
            (v1, v2) -> Double.compare(node_data.get(v1).distance, node_data.get(v2).distance)),
        edge_weights);
  }

  protected void initialize(Network<N, E> graph) {
    this.graph = graph;
    this.node_scores = new HashMap<N, Double>();
    this.edge_scores = new HashMap<E, Double>();
    this.node_data = new HashMap<N, BetweennessData>();

    for (N v : graph.nodes()) {
      this.node_scores.put(v, 0.0);
    }

    for (E e : graph.edges()) {
      this.edge_scores.put(e, 0.0);
    }
  }

  protected void computeBetweenness(
      Queue<N> queue, Function<? super E, ? extends Number> edge_weights) {
    for (N v : graph.nodes()) {
      // initialize the betweenness data for this new node
      for (N s : graph.nodes()) {
        this.node_data.put(s, new BetweennessData());
      }

      node_data.get(v).numSPs = 1;
      node_data.get(v).distance = 0;

      Deque<N> stack = new ArrayDeque<N>();
      queue.offer(v);

      while (!queue.isEmpty()) {
        //                N w = queue.remove();
        N w = queue.poll();
        stack.push(w);
        BetweennessData w_data = node_data.get(w);

        for (E e : graph.outEdges(w)) {
          N x = graph.incidentNodes(e).adjacentNode(w);
          if (x.equals(w)) {
            continue;
          }
          double wx_weight = edge_weights.apply(e).doubleValue();

          //                for(N x : graph.getSuccessors(w))
          //                {
          //                	if (x.equals(w))
          //                		continue;

          // FIXME: the other problem is that I need to
          // keep putting the neighbors of things we've just
          // discovered in the queue, if they're undiscovered or
          // at greater distance.

          // FIXME: this is the problem, right here, I think:
          // need to update position in queue if distance changes
          // (which can only happen with weighted edges).
          // for each outgoing edge e from w, get other end x
          // if x not already visited (dist x < 0)
          //   set x's distance to w's dist + edge weight
          //   add x to queue; pri in queue is x's dist
          // if w's dist + edge weight < x's dist
          //   update x's dist
          //   update x in queue (MapBinaryHeap)
          //   clear x's incoming edge list
          // if w's dist + edge weight = x's dist
          //   add e to x's incoming edge list

          BetweennessData x_data = node_data.get(x);
          double x_potential_dist = w_data.distance + wx_weight;

          if (x_data.distance < 0) {
            //                        queue.add(x);
            //                        node_data.get(x).distance = node_data.get(w).distance + 1;
            x_data.distance = x_potential_dist;
            queue.offer(x);
          }

          // note:
          // (1) this can only happen with weighted edges
          // (2) x's SP count and incoming edges are updated below
          if (x_data.distance > x_potential_dist) {
            x_data.distance = x_potential_dist;
            // invalidate previously identified incoming edges
            // (we have a new shortest path distance to x)
            x_data.incomingEdges.clear();
            // update x's position in queue
            ((MapBinaryHeap<N>) queue).update(x);
          }
          //                  if (node_data.get(x).distance == node_data.get(w).distance + 1)
          //
          //                    if (x_data.distance == x_potential_dist)
          //                    {
          //                        x_data.numSPs += w_data.numSPs;
          ////                        node_data.get(x).predecessors.add(w);
          //                        x_data.incomingEdges.add(e);
          //                    }
        }
        for (E e : graph.outEdges(w)) {
          N x = graph.incidentNodes(e).adjacentNode(w);
          if (x.equals(w)) {
            continue;
          }
          double e_weight = edge_weights.apply(e).doubleValue();
          BetweennessData x_data = node_data.get(x);
          double x_potential_dist = w_data.distance + e_weight;
          if (x_data.distance == x_potential_dist) {
            x_data.numSPs += w_data.numSPs;
            //                        node_data.get(x).predecessors.add(w);
            x_data.incomingEdges.add(e);
          }
        }
      }
      while (!stack.isEmpty()) {
        N x = stack.pop();

        //    		    for (N w : node_data.get(x).predecessors)
        for (E e : node_data.get(x).incomingEdges) {
          N w = graph.incidentNodes(e).adjacentNode(x);
          double partialDependency =
              node_data.get(w).numSPs
                  / node_data.get(x).numSPs
                  * (1.0 + node_data.get(x).dependency);
          node_data.get(w).dependency += partialDependency;
          //    		        E w_x = graph.findEdge(w, x);
          //    		        double w_x_score = edge_scores.get(w_x).doubleValue();
          //    		        w_x_score += partialDependency;
          //    		        edge_scores.put(w_x, w_x_score);
          double e_score = edge_scores.get(e).doubleValue();
          edge_scores.put(e, e_score + partialDependency);
        }
        if (!x.equals(v)) {
          double x_score = node_scores.get(x).doubleValue();
          x_score += node_data.get(x).dependency;
          node_scores.put(x, x_score);
        }
      }
    }

    if (!graph.isDirected()) {
      for (N v : graph.nodes()) {
        double v_score = node_scores.get(v).doubleValue();
        v_score /= 2.0;
        node_scores.put(v, v_score);
      }
      for (E e : graph.edges()) {
        double e_score = edge_scores.get(e).doubleValue();
        e_score /= 2.0;
        edge_scores.put(e, e_score);
      }
    }

    node_data.clear();
  }

  //	protected void computeWeightedBetweenness(Function<E, ? extends Number> edge_weights)
  //	{
  //		for (N v : graph.nodes())
  //		{
  //			// initialize the betweenness data for this new node
  //			for (N s : graph.nodes())
  //				this.node_data.put(s, new BetweennessData());
  //            node_data.get(v).numSPs = 1;
  //            node_data.get(v).distance = 0;
  //
  //            Stack<N> stack = new Stack<N>();
  ////            Buffer<N> queue = new UnboundedFifoBuffer<N>();
  //            SortedSet<N> pqueue = new TreeSet<N>(new BetweennessComparator());
  ////          queue.add(v);
  //            pqueue.add(v);
  //
  ////            while (!queue.isEmpty())
  //            while (!pqueue.isEmpty())
  //            {
  ////              N w = queue.remove();
  //            	V w = pqueue.first();
  //            	pqueue.remove(w);
  //                stack.push(w);
  //
  ////                for(N x : graph.getSuccessors(w))
  //                for (E e : graph.getOutEdges(w))
  //                {
  //                	// TODO (jrtom): change this to getOtherNodes(w, e)
  //                	V x = graph.getOpposite(w, e);
  //                	if (x.equals(w))
  //                		continue;
  //                	double e_weight = edge_weights.transform(e).doubleValue();
  //
  //                    if (node_data.get(x).distance < 0)
  //                    {
  ////                        queue.add(x);
  //                    	pqueue.add(v);
  ////                        node_data.get(x).distance = node_data.get(w).distance + 1;
  //                        node_data.get(x).distance =
  //                        	node_data.get(w).distance + e_weight;
  //                    }
  //
  ////                    if (node_data.get(x).distance == node_data.get(w).distance + 1)
  //                    if (node_data.get(x).distance ==
  //                    	node_data.get(w).distance + e_weight)
  //                    {
  //                        node_data.get(x).numSPs += node_data.get(w).numSPs;
  //                        node_data.get(x).predecessors.add(w);
  //                    }
  //                }
  //            }
  //            updateScores(v, stack);
  //        }
  //
  //        if(graph instanceof UndirectedGraph)
  //            adjustUndirectedScores();
  //
  //        node_data.clear();
  //	}

  @Override
  public Double getNodeScore(N v) {
    return node_scores.get(v);
  }

  @Override
  public Double getEdgeScore(E e) {
    return edge_scores.get(e);
  }

  @Override
  public Map<N, Double> nodeScores() {
    return Collections.unmodifiableMap(node_scores);
  }

  @Override
  public Map<E, Double> edgeScores() {
    return Collections.unmodifiableMap(edge_scores);
  }

  private class BetweennessData {
    double distance;
    double numSPs;
    //        List<N> predecessors;
    List<E> incomingEdges;
    double dependency;

    BetweennessData() {
      distance = -1;
      numSPs = 0;
      //            predecessors = new ArrayList<N>();
      incomingEdges = new ArrayList<E>();
      dependency = 0;
    }

    @Override
    public String toString() {
      return "[d:"
          + distance
          + ", sp:"
          + numSPs
          + ", p:"
          + incomingEdges
          + ", d:"
          + dependency
          + "]\n";
      //        		", p:" + predecessors + ", d:" + dependency + "]\n";
    }
  }
}
