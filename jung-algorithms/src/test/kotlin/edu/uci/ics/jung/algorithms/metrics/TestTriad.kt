package edu.uci.ics.jung.algorithms.metrics

import com.google.common.graph.GraphBuilder
import junit.framework.TestCase

class TestTriad : TestCase() {

  fun testConfigurationFromPaper() {
    val g = GraphBuilder.directed().build<Char>()
    val u = 'u'
    g.addNode(u)
    val v = 'v'
    g.addNode(v)
    val w = 'w'
    g.addNode(w)
    g.putEdge(w, u)
    g.putEdge(u, v)
    g.putEdge(v, u)

    assertEquals(35, TriadicCensus.triCode(g, u, v, w))
    assertEquals(7, TriadicCensus.triType(35))
    assertEquals("111D", TriadicCensus.TRIAD_NAMES[7])

    assertEquals(7, TriadicCensus.triType(TriadicCensus.triCode(g, u, w, v)))
    assertEquals(7, TriadicCensus.triType(TriadicCensus.triCode(g, v, u, w)))

    val counts = TriadicCensus.getCounts(g)

    for (i in 1..16) {
      if (i == 7) {
        assertEquals(1, counts[i])
      } else {
        assertEquals(0, counts[i])
      }
    }
  }

  fun testFourNodeGraph() {
    // we'll set up a graph of
    // t->u
    // u->v
    // and that's it.
    // total count:
    // 2: 1(t, u, w)(u, v, w)
    // 6: 1(t, u, v)
    // 1: 1(u, v, w)
    val g = GraphBuilder.directed().build<Char>()
    val u = 'u'
    g.addNode(u)
    val v = 'v'
    g.addNode(v)
    val w = 'w'
    g.addNode(w)
    val t = 't'
    g.addNode(t)

    g.putEdge(t, u)
    g.putEdge(u, v)

    var counts = TriadicCensus.getCounts(g)
    for (i in 1..16) {
      if (i == 2) {
        assertEquals("On $i", 2, counts[i])
      } else if (i == 6 || i == 1) {
        assertEquals("On $i", 1, counts[i])
      } else {
        assertEquals(0, counts[i])
      }
    }

    // now let's tweak to
    // t->u, u->v, v->t
    // w->u, v->w
    g.putEdge(v, t)
    g.putEdge(w, u)
    g.putEdge(v, w)

    // that's two 030Cs. it's a 021D (v-t, v-w) and an 021U (t-u, w-u)
    counts = TriadicCensus.getCounts(g)

    for (i in 1..16) {
      if (i == 10 /* 030C */) {
        assertEquals("On $i", 2, counts[i])
      } else if (i == 4 || i == 5) {
        assertEquals("On $i", 1, counts[i])
      } else {
        assertEquals("On $i", 0, counts[i])
      }
    }
  }

  fun testThreeDotsThreeDashes() {
    val g = GraphBuilder.directed().build<Char>()
    val u = 'u'
    g.addNode(u)
    val v = 'v'
    g.addNode(v)
    val w = 'w'
    g.addNode(w)

    var counts = TriadicCensus.getCounts(g)

    for (i in 1..16) {
      if (i == 1) {
        assertEquals(1, counts[i])
      } else {
        assertEquals(0, counts[i])
      }
    }

    g.putEdge(v, u)
    g.putEdge(u, v)
    g.putEdge(v, w)
    g.putEdge(w, v)
    g.putEdge(u, w)
    g.putEdge(w, u)

    counts = TriadicCensus.getCounts(g)

    for (i in 1..16) {
      if (i == 16) {
        assertEquals(1, counts[i])
      } else {
        assertEquals("Count on $i failed", 0, counts[i])
      }
    }
  }

  /** **************Boring accounting for zero graphs***********  */
  fun testNull() {
    val g = GraphBuilder.directed().build<Char>()
    val counts = TriadicCensus.getCounts(g)

    // t looks like a hashtable for the twelve keys
    for (i in 1 until TriadicCensus.MAX_TRIADS) {
      assertEquals("Empty Graph doesn't have count 0", 0, counts[i])
    }
  }

  fun testOneNode() {
    val g = GraphBuilder.directed().build<Char>()
    g.addNode('u')
    val counts = TriadicCensus.getCounts(g)

    // t looks like a hashtable for the twelve keys
    for (i in 1 until TriadicCensus.MAX_TRIADS) {
      assertEquals("One node Graph doesn't have count 0", 0, counts[i])
    }
  }

  fun testTwoNodes() {
    val g = GraphBuilder.directed().build<Char>()
    val v1 = 'u'
    g.addNode(v1)
    val v2 = 'v'
    g.addNode(v2)
    g.putEdge(v1, v2)
    val counts = TriadicCensus.getCounts(g)

    // t looks like a hashtable for the twelve keys
    for (i in 1 until TriadicCensus.MAX_TRIADS) {
      assertEquals("Two node Graph doesn't have count 0", 0, counts[i])
    }
  }
}
