/*
 * Created on Aug 22, 2003
 *
 */
package edu.uci.ics.jung.algorithms.shortestpath;

import com.google.common.collect.BiMap;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.algorithms.util.Indexer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import junit.framework.TestCase;

/** @author Joshua O'Madadhain */
// TODO: needs major cleanup
public class TestShortestPath extends TestCase {
  private MutableNetwork<String, Integer> dg;
  private MutableNetwork<String, Integer> ug;
  // graph based on Weiss, _Data Structures and Algorithm Analysis_,
  // 1992, p. 292
  private static int[][] edges = {
    {1, 2, 2},
    {1, 4, 1}, // 0, 1
    {2, 4, 3},
    {2, 5, 10}, // 2, 3
    {3, 1, 4},
    {3, 6, 5}, // 4, 5
    {4, 3, 2},
    {4, 5, 2},
    {4, 6, 8},
    {4, 7, 4}, // 6,7,8,9
    {5, 7, 6}, // 10
    {7, 6, 1}, // 11
    {8, 9, 4}, // (12) these three edges define a second connected component
    {9, 10, 1}, // 13
    {10, 8, 2}
  }; // 14

  private static Integer[][] ug_incomingEdges = {
    {
      null,
      new Integer(0),
      new Integer(6),
      new Integer(1),
      new Integer(7),
      new Integer(11),
      new Integer(9),
      null,
      null,
      null
    },
    {
      new Integer(0),
      null,
      new Integer(6),
      new Integer(2),
      new Integer(7),
      new Integer(11),
      new Integer(9),
      null,
      null,
      null
    },
    {
      new Integer(1),
      new Integer(2),
      null,
      new Integer(6),
      new Integer(7),
      new Integer(5),
      new Integer(9),
      null,
      null,
      null
    },
    {
      new Integer(1),
      new Integer(2),
      new Integer(6),
      null,
      new Integer(7),
      new Integer(11),
      new Integer(9),
      null,
      null,
      null
    },
    {
      new Integer(1),
      new Integer(2),
      new Integer(6),
      new Integer(7),
      null,
      new Integer(11),
      new Integer(10),
      null,
      null,
      null
    },
    {
      new Integer(1),
      new Integer(2),
      new Integer(5),
      new Integer(9),
      new Integer(10),
      null,
      new Integer(11),
      null,
      null,
      null
    },
    {
      new Integer(1),
      new Integer(2),
      new Integer(5),
      new Integer(9),
      new Integer(10),
      new Integer(11),
      null,
      null,
      null,
      null
    },
    {null, null, null, null, null, null, null, null, new Integer(13), new Integer(14)},
    {null, null, null, null, null, null, null, new Integer(14), null, new Integer(13)},
    {null, null, null, null, null, null, null, new Integer(14), new Integer(13), null},
  };

  private static Integer[][] dg_incomingEdges = {
    {
      null,
      new Integer(0),
      new Integer(6),
      new Integer(1),
      new Integer(7),
      new Integer(11),
      new Integer(9),
      null,
      null,
      null
    },
    {
      new Integer(4),
      null,
      new Integer(6),
      new Integer(2),
      new Integer(7),
      new Integer(11),
      new Integer(9),
      null,
      null,
      null
    },
    {
      new Integer(4),
      new Integer(0),
      null,
      new Integer(1),
      new Integer(7),
      new Integer(5),
      new Integer(9),
      null,
      null,
      null
    },
    {
      new Integer(4),
      new Integer(0),
      new Integer(6),
      null,
      new Integer(7),
      new Integer(11),
      new Integer(9),
      null,
      null,
      null
    },
    {null, null, null, null, null, new Integer(11), new Integer(10), null, null, null},
    {null, null, null, null, null, null, null, null, null, null},
    {null, null, null, null, null, new Integer(11), null, null, null, null},
    {null, null, null, null, null, null, null, null, new Integer(12), new Integer(13)},
    {null, null, null, null, null, null, null, new Integer(14), null, new Integer(13)},
    {null, null, null, null, null, null, null, new Integer(14), new Integer(12), null}
  };

  private static double[][] dg_distances = {
    {
      0,
      2,
      3,
      1,
      3,
      6,
      5,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY
    },
    {
      9,
      0,
      5,
      3,
      5,
      8,
      7,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY
    },
    {
      4,
      6,
      0,
      5,
      7,
      5,
      9,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY
    },
    {
      6,
      8,
      2,
      0,
      2,
      5,
      4,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY
    },
    {
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      0,
      7,
      6,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY
    },
    {
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      0,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY
    },
    {
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      1,
      0,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY
    },
    {
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      0,
      4,
      5
    },
    {
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      3,
      0,
      1
    },
    {
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      2,
      6,
      0
    }
  };

  private static double[][] ug_distances = {
    {
      0,
      2,
      3,
      1,
      3,
      6,
      5,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY
    },
    {
      2,
      0,
      5,
      3,
      5,
      8,
      7,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY
    },
    {
      3,
      5,
      0,
      2,
      4,
      5,
      6,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY
    },
    {
      1,
      3,
      2,
      0,
      2,
      5,
      4,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY
    },
    {
      3,
      5,
      4,
      2,
      0,
      7,
      6,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY
    },
    {
      6,
      8,
      5,
      5,
      7,
      0,
      1,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY
    },
    {
      5,
      7,
      6,
      4,
      6,
      1,
      0,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY
    },
    {
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      0,
      3,
      2
    },
    {
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      3,
      0,
      1
    },
    {
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      Double.POSITIVE_INFINITY,
      2,
      1,
      0
    }
  };

  private static Integer[][] shortestPaths1 = {
    null,
    {new Integer(0)},
    {new Integer(1), new Integer(6)},
    {new Integer(1)},
    {new Integer(1), new Integer(7)},
    {new Integer(1), new Integer(9), new Integer(11)},
    {new Integer(1), new Integer(9)},
    null,
    null,
    null
  };

  private Map<Network<String, Integer>, Integer[]> edgeArrays;

  private Map<Integer, Number> edgeWeights;

  private Function<Integer, Number> nev;

  private Supplier<String> nodeFactoryDG =
      new Supplier<String>() {
        int count = 0;

        public String get() {
          return "V" + count++;
        }
      };
  private Supplier<String> nodeFactoryUG =
      new Supplier<String>() {
        int count = 0;

        public String get() {
          return "U" + count++;
        }
      };

  BiMap<String, Integer> did;
  BiMap<String, Integer> uid;

  @Override
  protected void setUp() {
    edgeWeights = new HashMap<>();
    nev = edgeWeights::get;
    dg = NetworkBuilder.directed().allowsParallelEdges(true).allowsSelfLoops(true).build();
    for (int i = 0; i < dg_distances.length; i++) {
      dg.addNode(nodeFactoryDG.get());
    }
    did = Indexer.create(dg.nodes(), 1);
    Integer[] dgArray = new Integer[edges.length];
    addEdges(dg, did, dgArray);

    ug = NetworkBuilder.undirected().allowsParallelEdges(true).allowsSelfLoops(true).build();
    for (int i = 0; i < ug_distances.length; i++) {
      ug.addNode(nodeFactoryUG.get());
    }
    uid = Indexer.create(ug.nodes(), 1);
    Integer[] ugArray = new Integer[edges.length];
    addEdges(ug, uid, ugArray);

    edgeArrays = new HashMap<>();
    edgeArrays.put(dg, dgArray);
    edgeArrays.put(ug, ugArray);
  }

  @Override
  protected void tearDown() throws Exception {}

  private void exceptionTest(
      MutableNetwork<String, Integer> g, BiMap<String, Integer> indexer, int index) {
    DijkstraShortestPath<String, Integer> dsp = new DijkstraShortestPath<>(g, nev);
    String start = indexer.inverse().get(index);
    Integer e = null;

    String v = "NOT IN GRAPH";

    try {
      dsp.getDistance(start, v);
      fail("getDistance(): illegal destination node");
    } catch (IllegalArgumentException iae) {
    }
    try {
      dsp.getDistance(v, start);
      fail("getDistance(): illegal source node");
    } catch (IllegalArgumentException iae) {
    }
    try {
      dsp.getDistanceMap(v, 1);
      fail("getDistanceMap(): illegal source node");
    } catch (IllegalArgumentException iae) {
    }
    try {
      dsp.getDistanceMap(start, 0);
      fail("getDistanceMap(): too few nodes requested");
    } catch (IllegalArgumentException iae) {
    }
    try {
      dsp.getDistanceMap(start, g.nodes().size() + 1);
      fail("getDistanceMap(): too many nodes requested");
    } catch (IllegalArgumentException iae) {
    }

    try {
      dsp.getIncomingEdge(start, v);
      fail("getIncomingEdge(): illegal destination node");
    } catch (IllegalArgumentException iae) {
    }
    try {
      dsp.getIncomingEdge(v, start);
      fail("getIncomingEdge(): illegal source node");
    } catch (IllegalArgumentException iae) {
    }
    try {
      dsp.getIncomingEdgeMap(v, 1);
      fail("getIncomingEdgeMap(): illegal source node");
    } catch (IllegalArgumentException iae) {
    }
    try {
      dsp.getIncomingEdgeMap(start, 0);
      fail("getIncomingEdgeMap(): too few nodes requested");
    } catch (IllegalArgumentException iae) {
    }
    try {
      dsp.getDistanceMap(start, g.nodes().size() + 1);
      fail("getIncomingEdgeMap(): too many nodes requested");
    } catch (IllegalArgumentException iae) {
    }

    try {
      // test negative edge weight exception
      String v1 = indexer.inverse().get(1);
      String v2 = indexer.inverse().get(7);
      e = g.edges().size() + 1;
      g.addEdge(v1, v2, e);
      edgeWeights.put(e, -2);
      dsp.reset();
      dsp.getDistanceMap(start);
      fail("DijkstraShortestPath should not accept negative edge weights");
    } catch (IllegalArgumentException iae) {
      g.removeEdge(e);
    }
  }

  public void testDijkstra() {
    setUp();
    exceptionTest(dg, did, 1);

    setUp();
    exceptionTest(ug, uid, 1);

    setUp();
    getPathTest(dg, did, 1);

    setUp();
    getPathTest(ug, uid, 1);

    for (int i = 1; i <= dg_distances.length; i++) {
      setUp();
      weightedTest(dg, did, i, true);

      setUp();
      weightedTest(dg, did, i, false);
    }

    for (int i = 1; i <= ug_distances.length; i++) {
      setUp();
      weightedTest(ug, uid, i, true);

      setUp();
      weightedTest(ug, uid, i, false);
    }
  }

  private void getPathTest(Network<String, Integer> g, BiMap<String, Integer> indexer, int index) {
    DijkstraShortestPath<String, Integer> dsp = new DijkstraShortestPath<>(g, nev);
    String start = indexer.inverse().get(index);
    Integer[] edgeArray = edgeArrays.get(g);
    Integer[] incomingEdges1 =
        g.isDirected() ? dg_incomingEdges[index - 1] : ug_incomingEdges[index - 1];
    assertEquals(incomingEdges1.length, g.nodes().size());

    // test getShortestPath(start, v)
    dsp.reset();
    for (int i = 1; i <= incomingEdges1.length; i++) {
      List<Integer> shortestPath = dsp.getPath(start, indexer.inverse().get(i));
      Integer[] indices = shortestPaths1[i - 1];
      for (ListIterator<Integer> iter = shortestPath.listIterator(); iter.hasNext(); ) {
        int j = iter.nextIndex();
        Integer e = iter.next();
        if (e != null) {
          assertEquals(edgeArray[indices[j]], e);
        } else {
          assertNull(indices[j]);
        }
      }
    }
  }

  private void weightedTest(
      Network<String, Integer> g, BiMap<String, Integer> indexer, int index, boolean cached) {
    String start = indexer.inverse().get(index);
    double[] distances1 = null;
    Integer[] incomingEdges1 = null;
    if (g.isDirected()) {
      distances1 = dg_distances[index - 1];
      incomingEdges1 = dg_incomingEdges[index - 1];
    } else {
      distances1 = ug_distances[index - 1];
      incomingEdges1 = ug_incomingEdges[index - 1];
    }
    assertEquals(distances1.length, g.nodes().size());
    assertEquals(incomingEdges1.length, g.nodes().size());
    DijkstraShortestPath<String, Integer> dsp = new DijkstraShortestPath<>(g, nev, cached);
    Integer[] edgeArray = edgeArrays.get(g);

    // test getDistance(start, v)
    for (int i = 1; i <= distances1.length; i++) {
      String v = indexer.inverse().get(i);
      Number n = dsp.getDistance(start, v);
      double d = distances1[i - 1];
      double dist;
      if (n == null) {
        dist = Double.POSITIVE_INFINITY;
      } else {
        dist = n.doubleValue();
      }

      assertEquals(d, dist, .001);
    }

    // test getIncomingEdge(start, v)
    dsp.reset();
    for (int i = 1; i <= incomingEdges1.length; i++) {
      String v = indexer.inverse().get(i);
      Integer e = dsp.getIncomingEdge(start, v);
      if (e != null) {
        assertEquals(edgeArray[incomingEdges1[i - 1]], e);
      } else {
        assertNull(incomingEdges1[i - 1]);
      }
    }

    // test getDistanceMap(v)
    dsp.reset();
    Map<String, Number> distances = dsp.getDistanceMap(start);
    assertTrue(distances.size() <= g.nodes().size());
    double dPrev = 0; // smallest possible distance
    Set<String> reachable = new HashSet<>();
    for (String cur : distances.keySet()) {
      double dCur = distances.get(cur).doubleValue();
      assertTrue(dCur >= dPrev);

      dPrev = dCur;
      int i = indexer.get(cur);
      assertEquals(distances1[i - 1], dCur, .001);
      reachable.add(cur);
    }
    // make sure that non-reachable nodes have no entries
    for (String v : g.nodes()) {
      assertEquals(reachable.contains(v), distances.keySet().contains(v));
    }

    // test getIncomingEdgeMap(v)
    dsp.reset();
    Map<String, Integer> incomingEdgeMap = dsp.getIncomingEdgeMap(start);
    assertTrue(incomingEdgeMap.size() <= g.nodes().size());
    for (String v : incomingEdgeMap.keySet()) {
      Integer e = incomingEdgeMap.get(v);
      int i = indexer.get(v);
      if (e != null) {
        assertEquals(edgeArray[incomingEdges1[i - 1]], e);
      } else {
        assertNull(incomingEdges1[i - 1]);
      }
    }

    // test getDistanceMap(v, k)
    dsp.reset();
    for (int i = 1; i <= distances1.length; i++) {
      distances = dsp.getDistanceMap(start, i);
      assertTrue(distances.size() <= i);
      dPrev = 0; // smallest possible distance

      reachable.clear();
      for (String cur : distances.keySet()) {
        double dCur = distances.get(cur).doubleValue();
        assertTrue(dCur >= dPrev);

        dPrev = dCur;
        int j = indexer.get(cur);

        assertEquals(distances1[j - 1], dCur, .001);
        reachable.add(cur);
      }
      for (String node : g.nodes()) {
        assertEquals(reachable.contains(node), distances.keySet().contains(node));
      }
    }

    // test getIncomingEdgeMap(v, k)
    dsp.reset();
    for (int i = 1; i <= incomingEdges1.length; i++) {
      incomingEdgeMap = dsp.getIncomingEdgeMap(start, i);
      assertTrue(incomingEdgeMap.size() <= i);
      for (String v : incomingEdgeMap.keySet()) {
        Integer e = incomingEdgeMap.get(v);
        int j = indexer.get(v);
        if (e != null) {
          assertEquals(edgeArray[incomingEdges1[j - 1]], e);
        } else {
          assertNull(incomingEdges1[j - 1]);
        }
      }
    }
  }

  private void addEdges(
      MutableNetwork<String, Integer> g, BiMap<String, Integer> indexer, Integer[] edgeArray) {
    for (int i = 0; i < edges.length; i++) {
      int[] edge = edges[i];
      Integer e = i;
      g.addEdge(indexer.inverse().get(edge[0]), indexer.inverse().get(edge[1]), i);
      edgeArray[i] = e;
      if (edge.length > 2) {
        edgeWeights.put(e, edge[2]);
      }
    }
  }
}
