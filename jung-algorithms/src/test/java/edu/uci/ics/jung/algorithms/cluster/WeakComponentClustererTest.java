package edu.uci.ics.jung.algorithms.cluster;

import com.google.common.graph.Network;
import edu.uci.ics.jung.graph.util.TestGraphs;
import junit.framework.TestCase;

public class WeakComponentClustererTest extends TestCase {

  Network<String, Number> graph = TestGraphs.getDemoGraph();

  public void testWeakComponent() {
    WeakComponentClusterer<String, Number> clusterer = new WeakComponentClusterer<String, Number>();
    //		Set<Set<String>> clusterSet =
    clusterer.apply(graph);
    //		System.err.println("set is "+clusterSet);
  }
}
