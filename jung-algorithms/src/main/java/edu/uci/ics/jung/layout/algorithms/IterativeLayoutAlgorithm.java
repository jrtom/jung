package edu.uci.ics.jung.layout.algorithms;

import edu.uci.ics.jung.algorithms.util.IterativeContext;

public interface IterativeLayoutAlgorithm<N> extends LayoutAlgorithm<N>, IterativeContext {
  boolean preRelax(); // may be a no-op depending on how the algorithm instance is created
}
