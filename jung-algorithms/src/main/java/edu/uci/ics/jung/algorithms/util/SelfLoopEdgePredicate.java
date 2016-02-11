package edu.uci.ics.jung.algorithms.util;

import com.google.common.base.Predicate;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * A <code>Predicate</code> that returns <code>true</code> if the input edge's 
 * endpoints in the input graph are identical.  (Thus, an edge which connects
 * its sole incident vertex to itself).
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class SelfLoopEdgePredicate<V,E> implements Predicate<Context<Graph<V,E>,E>> {

    public boolean apply(Context<Graph<V,E>,E> context) {
        Pair<V> endpoints = context.graph.getEndpoints(context.element);
        return endpoints.getFirst().equals(endpoints.getSecond());
    }
}
