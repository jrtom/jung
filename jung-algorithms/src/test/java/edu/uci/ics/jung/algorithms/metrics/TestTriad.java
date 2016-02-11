package edu.uci.ics.jung.algorithms.metrics;

import junit.framework.TestCase;
import edu.uci.ics.jung.algorithms.metrics.TriadicCensus;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

public class TestTriad extends TestCase {

	public void testConfigurationFromPaper() {
		DirectedGraph<Character,Number> g = new DirectedSparseMultigraph<Character,Number>();
		char u = 'u';
		g.addVertex(u);
		char v = 'v';
		g.addVertex(v);
		char w = 'w';
		g.addVertex(w);
		g.addEdge(0, w, u);
		g.addEdge(1, u, v);
		g.addEdge(2, v, u);

		assertEquals(35, TriadicCensus.<Character,Number>triCode(g, u, v, w));
		assertEquals(7, TriadicCensus.triType(35));
		assertEquals("111D", TriadicCensus.TRIAD_NAMES[7]);

		assertEquals(7, TriadicCensus.triType(TriadicCensus.<Character,Number>triCode(g, u, w, v)));
		assertEquals(7, TriadicCensus.triType(TriadicCensus.<Character,Number>triCode(g, v, u, w)));

        long[] counts = TriadicCensus.getCounts(g);

		for (int i = 1; i <= 16; i++) {
			if (i == 7) {
                assertEquals(1, counts[i]);
			} else {
                assertEquals(0, counts[i]);
			}
		}
	}

	public void testFourVertexGraph() {
		// we'll set up a graph of
		// t->u
		// u->v
		// and that's it.
		// total count:
		// 2: 1(t, u, w)(u, v, w)
		// 6: 1(t, u, v)
		// 1: 1(u, v, w)
		DirectedGraph<Character,Number> g = new DirectedSparseMultigraph<Character,Number>();
		char u = 'u';
		g.addVertex(u);
		char v = 'v';
		g.addVertex(v);
		char w = 'w';
		g.addVertex(w);
		char t = 't';
		g.addVertex(t);
		
		g.addEdge(0, t, u );
		g.addEdge(1, u, v );
				
        long[] counts = TriadicCensus.getCounts(g);
		for (int i = 1; i <= 16; i++) {
			if( i == 2 ) {
                assertEquals("On " + i, 2, counts[i]);              
			} else if (i == 6 || i == 1  ) {
                assertEquals("On " + i, 1, counts[i]);
			} else {
                assertEquals(0, counts[i]);
			}
		}
		
		// now let's tweak to 
		// t->u, u->v, v->t
		// w->u, v->w
		g.addEdge(2, v, t );
		g.addEdge(3, w, u );
		g.addEdge(4, v, w );

		// that's two 030Cs. it's a 021D (v-t, v-w) and an 021U (t-u, w-u)
        counts = TriadicCensus.getCounts(g);

		for (int i = 1; i <= 16; i++) {
			if( i == 10 /* 030C */ ) {
                assertEquals("On " + i, 2, counts[i]);              
			} else if (i == 4 || i == 5  ) {
                assertEquals("On " + i, 1, counts[i]);
			} else {
                assertEquals("On " + i , 0, counts[i]);
			}
		}
	}
	
	public void testThreeDotsThreeDashes() {
		DirectedGraph<Character,Number> g = new DirectedSparseMultigraph<Character,Number>();
		char u = 'u';
		g.addVertex(u);
		char v = 'v';
		g.addVertex(v);
		char w = 'w';
		g.addVertex(w);

        long[] counts = TriadicCensus.getCounts(g);

		for (int i = 1; i <= 16; i++) {
			if (i == 1) {
                assertEquals(1, counts[i]);
			} else {
                assertEquals(0, counts[i]);
			}
		}

		g.addEdge(0, v, u);
		g.addEdge(1, u, v);
		g.addEdge(2, v, w);
		g.addEdge(3, w, v);
		g.addEdge(4, u, w);
		g.addEdge(5, w, u);

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
		DirectedGraph<Character,Number> g = new DirectedSparseMultigraph<Character,Number>();
        long[] counts = TriadicCensus.getCounts(g);

		// t looks like a hashtable for the twelve keys
		for (int i = 1; i < TriadicCensus.MAX_TRIADS; i++) {
            assertEquals("Empty Graph doesn't have count 0", 0, counts[i]);
		}
	}

	public void testOneVertex() {
		DirectedGraph<Character,Number> g = new DirectedSparseMultigraph<Character,Number>();
		g.addVertex('u');
        long[] counts = TriadicCensus.getCounts(g);

		// t looks like a hashtable for the twelve keys
		for (int i = 1; i < TriadicCensus.MAX_TRIADS; i++) {
            assertEquals("One vertex Graph doesn't have count 0", 0, counts[i]);
		}
	}

	public void testTwoVertices() {
		DirectedGraph<Character,Number> g = new DirectedSparseMultigraph<Character,Number>();
		char v1, v2;
		g.addVertex(v1 = 'u');
		g.addVertex(v2 = 'v');
		g.addEdge(0, v1, v2);
        long[] counts = TriadicCensus.getCounts(g);

		// t looks like a hashtable for the twelve keys
		for (int i = 1; i < TriadicCensus.MAX_TRIADS; i++) {
            assertEquals("Two vertex Graph doesn't have count 0", 0, counts[i]);
		}
	}
}
