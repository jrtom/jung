package edu.uci.ics.jung.algorithms.metrics;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import junit.framework.TestCase;

public class TestTriad extends TestCase {

  public void testConfigurationFromPaper() {
    MutableGraph<Character> g = GraphBuilder.directed().build();
    char u = 'u';
    g.addNode(u);
    char v = 'v';
    g.addNode(v);
    char w = 'w';
    g.addNode(w);
    g.putEdge(w, u);
    g.putEdge(u, v);
    g.putEdge(v, u);

    assertEquals(35, TriadicCensus.<Character, Number>triCode(g, u, v, w));
    assertEquals(7, TriadicCensus.triType(35));
    assertEquals("111D", TriadicCensus.TRIAD_NAMES[7]);

    assertEquals(7, TriadicCensus.triType(TriadicCensus.<Character, Number>triCode(g, u, w, v)));
    assertEquals(7, TriadicCensus.triType(TriadicCensus.<Character, Number>triCode(g, v, u, w)));

    long[] counts = TriadicCensus.getCounts(g);

    for (int i = 1; i <= 16; i++) {
      if (i == 7) {
        assertEquals(1, counts[i]);
      } else {
        assertEquals(0, counts[i]);
      }
    }
  }

  public void testFourNodeGraph() {
    // we'll set up a graph of
    // t->u
    // u->v
    // and that's it.
    // total count:
    // 2: 1(t, u, w)(u, v, w)
    // 6: 1(t, u, v)
    // 1: 1(u, v, w)
    MutableGraph<Character> g = GraphBuilder.directed().build();
    char u = 'u';
    g.addNode(u);
    char v = 'v';
    g.addNode(v);
    char w = 'w';
    g.addNode(w);
    char t = 't';
    g.addNode(t);

    g.putEdge(t, u);
    g.putEdge(u, v);

    long[] counts = TriadicCensus.getCounts(g);
    for (int i = 1; i <= 16; i++) {
      if (i == 2) {
        assertEquals("On " + i, 2, counts[i]);
      } else if (i == 6 || i == 1) {
        assertEquals("On " + i, 1, counts[i]);
      } else {
        assertEquals(0, counts[i]);
      }
    }

    // now let's tweak to
    // t->u, u->v, v->t
    // w->u, v->w
    g.putEdge(v, t);
    g.putEdge(w, u);
    g.putEdge(v, w);

    // that's two 030Cs. it's a 021D (v-t, v-w) and an 021U (t-u, w-u)
    counts = TriadicCensus.getCounts(g);

    for (int i = 1; i <= 16; i++) {
      if (i == 10 /* 030C */) {
        assertEquals("On " + i, 2, counts[i]);
      } else if (i == 4 || i == 5) {
        assertEquals("On " + i, 1, counts[i]);
      } else {
        assertEquals("On " + i, 0, counts[i]);
      }
    }
  }

  public void testThreeDotsThreeDashes() {
    MutableGraph<Character> g = GraphBuilder.directed().build();
    char u = 'u';
    g.addNode(u);
    char v = 'v';
    g.addNode(v);
    char w = 'w';
    g.addNode(w);

    long[] counts = TriadicCensus.getCounts(g);

    for (int i = 1; i <= 16; i++) {
      if (i == 1) {
        assertEquals(1, counts[i]);
      } else {
        assertEquals(0, counts[i]);
      }
    }

    g.putEdge(v, u);
    g.putEdge(u, v);
    g.putEdge(v, w);
    g.putEdge(w, v);
    g.putEdge(u, w);
    g.putEdge(w, u);

    counts = TriadicCensus.getCounts(g);

    for (int i = 1; i <= 16; i++) {
      if (i == 16) {
        assertEquals(1, counts[i]);
      } else {
        assertEquals("Count on " + i + " failed", 0, counts[i]);
      }
    }
  }

  /** **************Boring accounting for zero graphs*********** */
  public void testNull() {
    MutableGraph<Character> g = GraphBuilder.directed().build();
    long[] counts = TriadicCensus.getCounts(g);

    // t looks like a hashtable for the twelve keys
    for (int i = 1; i < TriadicCensus.MAX_TRIADS; i++) {
      assertEquals("Empty Graph doesn't have count 0", 0, counts[i]);
    }
  }

  public void testOneNode() {
    MutableGraph<Character> g = GraphBuilder.directed().build();
    g.addNode('u');
    long[] counts = TriadicCensus.getCounts(g);

    // t looks like a hashtable for the twelve keys
    for (int i = 1; i < TriadicCensus.MAX_TRIADS; i++) {
      assertEquals("One node Graph doesn't have count 0", 0, counts[i]);
    }
  }

  public void testTwoNodes() {
    MutableGraph<Character> g = GraphBuilder.directed().build();
    char v1, v2;
    g.addNode(v1 = 'u');
    g.addNode(v2 = 'v');
    g.putEdge(v1, v2);
    long[] counts = TriadicCensus.getCounts(g);

    // t looks like a hashtable for the twelve keys
    for (int i = 1; i < TriadicCensus.MAX_TRIADS; i++) {
      assertEquals("Two node Graph doesn't have count 0", 0, counts[i]);
    }
  }
}
