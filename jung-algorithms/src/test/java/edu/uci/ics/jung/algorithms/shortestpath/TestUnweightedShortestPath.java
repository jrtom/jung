/*
 * Created on Aug 22, 2003
 *
 */
package edu.uci.ics.jung.algorithms.shortestpath;

import com.google.common.collect.BiMap;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import edu.uci.ics.jung.algorithms.util.Indexer;
import java.util.function.Supplier;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Scott White
 */
public class TestUnweightedShortestPath extends TestCase {
  private Supplier<String> nodeFactory =
      new Supplier<String>() {
        int count = 0;

        public String get() {
          return "V" + count++;
        }
      };

  BiMap<String, Integer> id;

  @Override
  protected void setUp() {}

  public static Test suite() {
    return new TestSuite(TestUnweightedShortestPath.class);
  }

  public void testUndirected() {
    MutableGraph<String> ug = GraphBuilder.undirected().allowsSelfLoops(true).build();
    for (int i = 0; i < 5; i++) {
      ug.addNode(nodeFactory.get());
    }
    id = Indexer.<String>create(ug.nodes());

    //		GraphUtils.addNodes(ug,5);
    //		Indexer id = Indexer.getIndexer(ug);
    ug.putEdge(id.inverse().get(0), id.inverse().get(1));
    ug.putEdge(id.inverse().get(1), id.inverse().get(2));
    ug.putEdge(id.inverse().get(2), id.inverse().get(3));
    ug.putEdge(id.inverse().get(0), id.inverse().get(4));
    ug.putEdge(id.inverse().get(4), id.inverse().get(3));

    UnweightedShortestPath<String> usp = new UnweightedShortestPath<String>(ug);
    Assert.assertEquals(usp.getDistance(id.inverse().get(0), id.inverse().get(3)).intValue(), 2);
    Assert.assertEquals(
        (usp.getDistanceMap(id.inverse().get(0)).get(id.inverse().get(3))).intValue(), 2);
    Assert.assertNull(usp.getIncomingEdgeMap(id.inverse().get(0)).get(id.inverse().get(0)));
    Assert.assertNotNull(usp.getIncomingEdgeMap(id.inverse().get(0)).get(id.inverse().get(3)));
  }

  public void testDirected() {
    MutableGraph<String> dg = GraphBuilder.directed().allowsSelfLoops(true).build();
    for (int i = 0; i < 5; i++) {
      dg.addNode(nodeFactory.get());
    }
    id = Indexer.<String>create(dg.nodes());
    dg.putEdge(id.inverse().get(0), id.inverse().get(1));
    dg.putEdge(id.inverse().get(1), id.inverse().get(2));
    dg.putEdge(id.inverse().get(2), id.inverse().get(3));
    dg.putEdge(id.inverse().get(0), id.inverse().get(4));
    dg.putEdge(id.inverse().get(4), id.inverse().get(3));
    dg.putEdge(id.inverse().get(3), id.inverse().get(0));

    UnweightedShortestPath<String> usp = new UnweightedShortestPath<String>(dg);
    Assert.assertEquals(usp.getDistance(id.inverse().get(0), id.inverse().get(3)).intValue(), 2);
    Assert.assertEquals(
        (usp.getDistanceMap(id.inverse().get(0)).get(id.inverse().get(3))).intValue(), 2);
    Assert.assertNull(usp.getIncomingEdgeMap(id.inverse().get(0)).get(id.inverse().get(0)));
    Assert.assertNotNull(usp.getIncomingEdgeMap(id.inverse().get(0)).get(id.inverse().get(3)));
  }
}
