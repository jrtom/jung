package edu.uci.ics.jung.algorithms.scoring;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.algorithms.scoring.util.ScoringUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;

public class TestKStepMarkov extends TestCase {
  MutableNetwork<Number, Number> mGraph;
  double[][] mTransitionMatrix;
  Map<Number, Number> edgeWeights = new HashMap<Number, Number>();

  @Override
  protected void setUp() {
    mGraph = NetworkBuilder.directed().allowsParallelEdges(true).build();
    mTransitionMatrix =
        new double[][] {{0.0, 0.5, 0.5}, {1.0 / 3.0, 0.0, 2.0 / 3.0}, {1.0 / 3.0, 2.0 / 3.0, 0.0}};

    for (int i = 0; i < mTransitionMatrix.length; i++) {
      mGraph.addNode(i);
    }

    for (int i = 0; i < mTransitionMatrix.length; i++) {
      for (int j = 0; j < mTransitionMatrix[i].length; j++) {
        if (mTransitionMatrix[i][j] > 0) {
          int edge = i * mTransitionMatrix.length + j;
          mGraph.addEdge(i, j, edge);
          edgeWeights.put(edge, mTransitionMatrix[i][j]);
        }
      }
    }
  }

  // TODO(jrtom): this isn't actually testing anything
  public void testRanker() {

    Set<Number> priors = new HashSet<Number>();
    priors.add(1);
    priors.add(2);
    KStepMarkov<Number, Number> ranker =
        new KStepMarkov<Number, Number>(
            mGraph, e -> edgeWeights.get(e), ScoringUtils.getUniformRootPrior(priors), 2);
    //        ranker.evaluate();
    //        System.out.println(ranker.getIterations());

    for (int i = 0; i < 10; i++) {
      //            System.out.println(ranker.getIterations());
      //	        for (Number n : mGraph.getNodes())
      //	        	System.out.println(n + ": " + ranker.getNodeScore(n));
      ranker.step();
    }
    //        List<Ranking<?>> rankings = ranker.getRankings();
    //        System.out.println("New version:");
    //        System.out.println(rankings);
  }
}
