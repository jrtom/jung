/*
 * Created on May 3, 2004
 *
 * Copyright (c) 2004, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.io;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Needed tests: - edgeslist, arcslist - unit test to catch bug in readArcsOrEdges() [was skipping
 * until e_pred, not c_pred]
 *
 * @author Joshua O'Madadhain
 * @author Tom Nelson - converted to jung2
 */
public class PajekNetIOTest extends TestCase {
  protected String[] node_labels = {"alpha", "beta", "gamma", "delta", "epsilon"};

  Supplier<MutableNetwork<Number, Number>> directedGraphFactory =
      new Supplier<MutableNetwork<Number, Number>>() {
        public MutableNetwork<Number, Number> get() {
          return NetworkBuilder.directed().allowsSelfLoops(true).build();
        }
      };
  Supplier<MutableNetwork<Number, Number>> undirectedGraphFactory =
      new Supplier<MutableNetwork<Number, Number>>() {
        public MutableNetwork<Number, Number> get() {
          return NetworkBuilder.undirected().allowsSelfLoops(true).build();
        }
      };
  Supplier<Number> nodeFactory;
  Supplier<Number> edgeFactory;
  PajekNetReader<MutableNetwork<Number, Number>, Number, Number> pnr;

  @Override
  protected void setUp() {
    nodeFactory =
        new Supplier<Number>() {
          int n = 0;

          public Number get() {
            return n++;
          }
        };
    edgeFactory =
        new Supplier<Number>() {
          int n = 0;

          public Number get() {
            return n++;
          }
        };
    pnr =
        new PajekNetReader<MutableNetwork<Number, Number>, Number, Number>(
            nodeFactory, edgeFactory);
  }

  public void testNull() {}

  public void testFileNotFound() {
    try {
      pnr.load("/dev/null/foo", directedGraphFactory);
      fail("File load did not fail on nonexistent file");
    } catch (FileNotFoundException fnfe) {
    } catch (IOException ioe) {
      fail("unexpected IOException");
    }
  }

  public void testNoLabels() throws IOException {
    String test = "*Nodes 3\n1\n2\n3\n*Edges\n1 2\n2 2";
    Reader r = new StringReader(test);

    Network<Number, Number> g = pnr.load(r, undirectedGraphFactory);
    assertEquals(g.nodes().size(), 3);
    assertEquals(g.edges().size(), 2);
  }

  public void testDirectedSaveLoadSave() throws IOException {
    MutableNetwork<Number, Number> graph1 = directedGraphFactory.get();
    for (int i = 1; i <= 5; i++) {
      graph1.addNode(i);
    }
    List<Number> id = new ArrayList<Number>(graph1.nodes());
    GreekLabels<Number> gl = new GreekLabels<Number>(id);
    int j = 0;
    graph1.addEdge(1, 2, j++);
    graph1.addEdge(1, 3, j++);
    graph1.addEdge(2, 3, j++);
    graph1.addEdge(2, 4, j++);
    graph1.addEdge(2, 5, j++);
    graph1.addEdge(5, 3, j++);

    assertEquals(graph1.edges().size(), 6);

    String testFilename = "dtest.net";
    String testFilename2 = testFilename + "2";

    PajekNetWriter<Number, Number> pnw = new PajekNetWriter<Number, Number>();
    pnw.save(graph1, testFilename, gl, null, null);

    MutableNetwork<Number, Number> graph2 = pnr.load(testFilename, directedGraphFactory);

    assertEquals(graph1.nodes().size(), graph2.nodes().size());
    assertEquals(graph1.edges().size(), graph2.edges().size());

    pnw.save(graph2, testFilename2, pnr.getNodeLabeller(), null, null);

    compareIndexedGraphs(graph1, graph2);

    MutableNetwork<Number, Number> graph3 = pnr.load(testFilename2, directedGraphFactory);

    compareIndexedGraphs(graph2, graph3);

    File file1 = new File(testFilename);
    File file2 = new File(testFilename2);

    Assert.assertTrue(file1.length() == file2.length());
    file1.delete();
    file2.delete();
  }

  public void testUndirectedSaveLoadSave() throws IOException {
    MutableNetwork<Number, Number> graph1 = undirectedGraphFactory.get();
    for (int i = 1; i <= 5; i++) {
      graph1.addNode(i);
    }

    List<Number> id = new ArrayList<Number>(graph1.nodes());
    int j = 0;
    GreekLabels<Number> gl = new GreekLabels<Number>(id);
    graph1.addEdge(1, 2, j++);
    graph1.addEdge(1, 3, j++);
    graph1.addEdge(2, 3, j++);
    graph1.addEdge(2, 4, j++);
    graph1.addEdge(2, 5, j++);
    graph1.addEdge(5, 3, j++);

    assertEquals(graph1.edges().size(), 6);

    String testFilename = "utest.net";
    String testFilename2 = testFilename + "2";

    PajekNetWriter<Number, Number> pnw = new PajekNetWriter<Number, Number>();
    pnw.save(graph1, testFilename, gl, null, null);

    MutableNetwork<Number, Number> graph2 = pnr.load(testFilename, undirectedGraphFactory);

    assertEquals(graph1.nodes().size(), graph2.nodes().size());
    assertEquals(graph1.edges().size(), graph2.edges().size());

    pnw.save(graph2, testFilename2, pnr.getNodeLabeller(), null, null);
    compareIndexedGraphs(graph1, graph2);

    MutableNetwork<Number, Number> graph3 = pnr.load(testFilename2, undirectedGraphFactory);

    compareIndexedGraphs(graph2, graph3);

    File file1 = new File(testFilename);
    File file2 = new File(testFilename2);

    Assert.assertTrue(file1.length() == file2.length());
    file1.delete();
    file2.delete();
  }

  /**
   * Tests to see whether these two graphs are structurally equivalent, based on the connectivity of
   * the nodes with matching indices in each graph. Assumes a 0-based index.
   *
   * @param g1
   * @param g2
   */
  private void compareIndexedGraphs(Network<Number, Number> g1, Network<Number, Number> g2) {
    int n1 = g1.nodes().size();
    int n2 = g2.nodes().size();

    assertEquals(n1, n2);

    assertEquals(g1.edges().size(), g2.edges().size());

    List<Number> id1 = new ArrayList<Number>(g1.nodes());
    List<Number> id2 = new ArrayList<Number>(g2.nodes());

    for (int i = 0; i < n1; i++) {
      Number v1 = id1.get(i);
      Number v2 = id2.get(i);
      assertNotNull(v1);
      assertNotNull(v2);

      checkSets(g1.predecessors(v1), g2.predecessors(v2), id1, id2);
      checkSets(g1.successors(v1), g2.successors(v2), id1, id2);
    }
  }

  private void checkSets(
      Collection<Number> s1, Collection<Number> s2, List<Number> id1, List<Number> id2) {
    for (Number u : s1) {
      int j = id1.indexOf(u);
      assertTrue(s2.contains(id2.get(j)));
    }
  }

  private class GreekLabels<N> implements Function<N, String> {
    private List<N> id;

    public GreekLabels(List<N> id) {
      this.id = id;
    }

    public String apply(N v) {
      return node_labels[id.indexOf(v)];
    }
  }
}
