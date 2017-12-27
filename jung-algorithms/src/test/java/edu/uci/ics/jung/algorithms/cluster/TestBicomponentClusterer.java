/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.cluster;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** @author Scott White */
public class TestBicomponentClusterer extends TestCase {
  public static Test suite() {
    return new TestSuite(TestBicomponentClusterer.class);
  }

  @Override
  protected void setUp() {}

  public void testExtract0() throws Exception {
    MutableGraph<String> graph = GraphBuilder.undirected().build();
    String[] v = {"0"};
    graph.addNode(v[0]);

    List<Set<String>> c = new ArrayList<Set<String>>();
    c.add(0, new HashSet<String>());
    c.get(0).add(v[0]);

    //        Set[] c = {new HashSet<String>()};

    //        c[0].add(v[0]);

    testComponents(graph, v, c);
  }

  public void testExtractEdge() throws Exception {
    MutableGraph<String> graph = GraphBuilder.undirected().build();
    String[] v = {"0", "1"};
    graph.putEdge(v[0], v[1]);

    List<Set<String>> c = new ArrayList<Set<String>>();
    c.add(0, new HashSet<String>());
    c.get(0).add(v[0]);
    c.get(0).add(v[1]);

    //        Set[] c = {new HashSet()};
    //
    //        c[0].add(v[0]);
    //        c[0].add(v[1]);

    testComponents(graph, v, c);
  }

  public void testExtractV() throws Exception {
    MutableGraph<String> graph = GraphBuilder.undirected().build();
    String[] v = {"0", "1", "2"};
    graph.putEdge(v[0], v[1]);
    graph.putEdge(v[0], v[2]);

    List<Set<String>> c = new ArrayList<Set<String>>();
    c.add(0, new HashSet<String>());
    c.add(1, new HashSet<String>());

    c.get(0).add(v[0]);
    c.get(0).add(v[1]);

    c.get(1).add(v[0]);
    c.get(1).add(v[2]);

    //        Set[] c = {new HashSet(), new HashSet()};
    //
    //        c[0].add(v[0]);
    //        c[0].add(v[1]);
    //
    //        c[1].add(v[0]);
    //        c[1].add(v[2]);

    testComponents(graph, v, c);
  }

  public void createEdges(String[] v, int[][] edge_array, MutableGraph<String> g) {
    for (int k = 0; k < edge_array.length; k++) {
      int i = edge_array[k][0];
      int j = edge_array[k][1];
      String v1 = getNode(v, i, g);
      String v2 = getNode(v, j, g);

      g.putEdge(v1, v2);
    }
  }

  public String getNode(String[] v_array, int i, MutableGraph<String> g) {
    String v = v_array[i];
    if (v == null) {
      v_array[i] = Character.toString((char) ('0' + i));
      g.addNode(v_array[i]);
      v = v_array[i];
    }
    return v;
  }

  public void testExtract1() {
    String[] v = new String[6];
    int[][] edges1 = {{0, 1}, {0, 5}, {0, 3}, {0, 4}, {1, 5}, {3, 4}, {2, 3}};
    MutableGraph<String> graph = GraphBuilder.undirected().build();
    createEdges(v, edges1, graph);

    List<Set<String>> c = new ArrayList<Set<String>>();
    for (int i = 0; i < 3; i++) {
      c.add(i, new HashSet<String>());
    }

    c.get(0).add(v[0]);
    c.get(0).add(v[1]);
    c.get(0).add(v[5]);

    c.get(1).add(v[0]);
    c.get(1).add(v[3]);
    c.get(1).add(v[4]);

    c.get(2).add(v[2]);
    c.get(2).add(v[3]);

    //        Set[] c = new Set[3];
    //        for (int i = 0; i < c.length; i++)
    //            c[i] = new HashSet();
    //
    //        c[0].add(v[0]);
    //        c[0].add(v[1]);
    //        c[0].add(v[5]);
    //
    //        c[1].add(v[0]);
    //        c[1].add(v[3]);
    //        c[1].add(v[4]);
    //
    //        c[2].add(v[2]);
    //        c[2].add(v[3]);

    testComponents(graph, v, c);
  }

  public void testExtract2() {
    String[] v = new String[9];
    int[][] edges1 = {
      {0, 2}, {0, 4}, {1, 0}, {2, 1}, {3, 0}, {4, 3}, {5, 3}, {6, 7}, {6, 8}, {8, 7}
    };
    MutableGraph<String> graph = GraphBuilder.undirected().build();
    createEdges(v, edges1, graph);

    List<Set<String>> c = new ArrayList<Set<String>>();
    for (int i = 0; i < 4; i++) {
      c.add(i, new HashSet<String>());
    }

    c.get(0).add(v[0]);
    c.get(0).add(v[1]);
    c.get(0).add(v[2]);

    c.get(1).add(v[0]);
    c.get(1).add(v[3]);
    c.get(1).add(v[4]);

    c.get(2).add(v[5]);
    c.get(2).add(v[3]);

    c.get(3).add(v[6]);
    c.get(3).add(v[7]);
    c.get(3).add(v[8]);

    //        Set[] c = new Set[4];
    //        for (int i = 0; i < c.length; i++)
    //            c[i] = new HashSet();
    //
    //        c[0].add(v[0]);
    //        c[0].add(v[1]);
    //        c[0].add(v[2]);
    //
    //        c[1].add(v[0]);
    //        c[1].add(v[3]);
    //        c[1].add(v[4]);
    //
    //        c[2].add(v[5]);
    //        c[2].add(v[3]);
    //
    //        c[3].add(v[6]);
    //        c[3].add(v[7]);
    //        c[3].add(v[8]);

    testComponents(graph, v, c);
  }

  public void testComponents(Graph<String> graph, String[] nodes, List<Set<String>> c) {
    BicomponentClusterer<String, Number> finder = new BicomponentClusterer<String, Number>();
    Set<Set<String>> bicomponents = finder.apply(graph);

    // check number of components
    assertEquals(bicomponents.size(), c.size());

    // diagnostic; should be commented out for typical unit tests
    //        for (int i = 0; i < bicomponents.size(); i++)
    //        {
    //            System.out.print("Component " + i + ": ");
    //            Set bicomponent = bicomponents.getCluster(i);
    //            for (Iterator iter = bicomponent.iterator(); iter.hasNext(); )
    //            {
    //                Node w = (Node)iter.next();
    //                System.out.print(sl.getLabel(w) + " ");
    //            }
    //            System.out.println();
    //        }
    //        System.out.println();

    // make sure that each set in c[] is found in bicomponents
    List<Set<String>> clusterList = new ArrayList<Set<String>>(bicomponents);
    boolean found = false;
    for (int i = 0; i < c.size(); i++) {
      for (int j = 0; j < bicomponents.size(); j++) {
        if (clusterList.get(j).equals(c.get(i))) {
          found = true;
          break;
        }
      }
      assertTrue(found);
    }

    // make sure that each node is represented in >=1 element of bicomponents
    Set<String> collapsedSet = new HashSet<String>();
    for (Set<String> set : bicomponents) {
      collapsedSet.addAll(set);
    }
    for (String v : graph.nodes()) {
      assertTrue(collapsedSet.contains(v));
      //        	assertFalse(((LinkedHashSet)vset).get(v).isEmpty());
    }
  }
}
