/*
 * Created on Aug 22, 2003
 *
 */
package edu.uci.ics.jung.algorithms.shortestpath;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Supplier;
import com.google.common.collect.BiMap;

import edu.uci.ics.jung.algorithms.util.Indexer;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;


/**
 * @author Joshua O'Madadhain
 */
public class TestShortestPath extends TestCase
{
    private DirectedGraph<String,Integer> dg;  
    private UndirectedGraph<String,Integer> ug;
    // graph based on Weiss, _Data Structures and Algorithm Analysis_,
    // 1992, p. 292
    private static int[][] edges = 
        {{1,2,2}, {1,4,1}, // 0, 1
            {2,4,3}, {2,5,10}, // 2, 3
            {3,1,4}, {3,6,5},  // 4, 5
            {4,3,2}, {4,5,2}, {4,6,8}, {4,7,4}, // 6,7,8,9
            {5,7,6}, // 10
            {7,6,1}, // 11
            {8,9,4}, // (12) these three edges define a second connected component
            {9,10,1}, // 13
            {10,8,2}}; // 14

    private static Integer[][] ug_incomingEdges = 
    {
        {null, new Integer(0), new Integer(6), new Integer(1), new Integer(7), new Integer(11), new Integer(9), null, null, null},
        {new Integer(0), null, new Integer(6), new Integer(2), new Integer(7), new Integer(11), new Integer(9), null, null, null},
        {new Integer(1), new Integer(2), null, new Integer(6), new Integer(7), new Integer(5), new Integer(9), null, null, null},
        {new Integer(1), new Integer(2), new Integer(6), null, new Integer(7), new Integer(11), new Integer(9), null, null, null},
        {new Integer(1), new Integer(2), new Integer(6), new Integer(7), null, new Integer(11), new Integer(10), null, null, null},
        {new Integer(1), new Integer(2), new Integer(5), new Integer(9), new Integer(10), null, new Integer(11), null, null, null},
        {new Integer(1), new Integer(2), new Integer(5), new Integer(9), new Integer(10), new Integer(11), null, null, null, null},
        {null, null, null, null, null, null, null, null, new Integer(13), new Integer(14)},
        {null, null, null, null, null, null, null, new Integer(14), null, new Integer(13)},
        {null, null, null, null, null, null, null, new Integer(14), new Integer(13), null},
    };      
    
    private static Integer[][] dg_incomingEdges = 
        {
            {null, new Integer(0), new Integer(6), new Integer(1), new Integer(7), new Integer(11), new Integer(9), null, null, null},
            {new Integer(4), null, new Integer(6), new Integer(2), new Integer(7), new Integer(11), new Integer(9), null, null, null},
            {new Integer(4), new Integer(0), null, new Integer(1), new Integer(7), new Integer(5), new Integer(9), null, null, null},
            {new Integer(4), new Integer(0), new Integer(6), null, new Integer(7), new Integer(11), new Integer(9), null, null, null},
            {null, null, null, null, null, new Integer(11), new Integer(10), null, null, null},
            {null, null, null, null, null, null, null, null, null, null},
            {null, null, null, null, null, new Integer(11), null, null, null, null},
            {null, null, null, null, null, null, null, null, new Integer(12), new Integer(13)},
            {null, null, null, null, null, null, null, new Integer(14), null, new Integer(13)},
            {null, null, null, null, null, null, null, new Integer(14), new Integer(12), null}
    };      
    
    private static double[][] dg_distances = 
    {
        {0, 2, 3, 1, 3, 6, 5, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
        {9, 0, 5, 3, 5, 8, 7, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
        {4, 6, 0, 5, 7, 5, 9, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
        {6, 8, 2, 0, 2, 5, 4, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
        {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0, 7, 6, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
        {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
        {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1, 0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
        {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0, 4, 5},
        {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 3, 0, 1},
        {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 2, 6, 0}
    };

    private static double[][] ug_distances = 
    {
        {0, 2, 3, 1, 3, 6, 5, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
        {2, 0, 5, 3, 5, 8, 7, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
        {3, 5, 0, 2, 4, 5, 6, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
        {1, 3, 2, 0, 2, 5, 4, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
        {3, 5, 4, 2, 0, 7, 6, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
        {6, 8, 5, 5, 7, 0, 1, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
        {5, 7, 6, 4, 6, 1, 0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
        {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0, 3, 2},
        {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 3, 0, 1},
        {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 2, 1, 0}
    };
    

    private static Integer[][] shortestPaths1 = 
    {
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
    
    private Map<Graph<String,Integer>,Integer[]> edgeArrays;
    
    private Map<Integer,Number> edgeWeights;
    
    private Function<Integer,Number> nev;
    
    private Supplier<String> vertexFactoryDG =
    	new Supplier<String>() {
    	int count = 0;
    	public String get() {
    		return "V"+count++;
    	}};
    	private Supplier<String> vertexFactoryUG =
    		new Supplier<String>() {
    		int count = 0;
    		public String get() {
    			return "U"+count++;
    		}};
    		
    BiMap<String,Integer> did;
    BiMap<String,Integer> uid;

    @Override
    protected void setUp() {
    	edgeWeights = new HashMap<Integer,Number>();
        nev = Functions.<Integer,Number>forMap(edgeWeights);
		dg = new DirectedSparseMultigraph<String,Integer>();
		for(int i=0; i<dg_distances.length; i++) {
			dg.addVertex(vertexFactoryDG.get());
		}
		did = Indexer.<String>create(dg.getVertices(), 1);
        Integer[] dg_array = new Integer[edges.length];
		addEdges(dg, did, dg_array);
        
        ug = new UndirectedSparseMultigraph<String,Integer>();
		for(int i=0; i<ug_distances.length; i++) {
			ug.addVertex(vertexFactoryUG.get());
		}
        uid = Indexer.<String>create(ug.getVertices(),1);
//        GraphUtils.addVertices(ug, ug_distances.length);
//        Indexer.newIndexer(ug, 1);
        Integer[] ug_array = new Integer[edges.length];
        addEdges(ug, uid, ug_array);
        
        edgeArrays = new HashMap<Graph<String,Integer>,Integer[]>();
        edgeArrays.put(dg, dg_array);
        edgeArrays.put(ug, ug_array);
    }
    
    @Override
    protected void tearDown() throws Exception {
    }

    public void exceptionTest(Graph<String,Integer> g, BiMap<String,Integer> indexer, int index)
    {
        DijkstraShortestPath<String,Integer> dsp = 
        	new DijkstraShortestPath<String,Integer>(g, nev);
//        Indexer id = Indexer.getIndexer(g);
        String start = indexer.inverse().get(index);
        Integer e = null;

        String v = "NOT IN GRAPH";
        
        try
        {
            dsp.getDistance(start, v);
            fail("getDistance(): illegal destination vertex");
        }
        catch (IllegalArgumentException iae) {}
        try
        {
            dsp.getDistance(v, start);
            fail("getDistance(): illegal source vertex");
        }
        catch (IllegalArgumentException iae) {}
        try
        {
            dsp.getDistanceMap(v, 1);
            fail("getDistanceMap(): illegal source vertex");
        }
        catch (IllegalArgumentException iae) {}
        try
        {
            dsp.getDistanceMap(start, 0);
            fail("getDistanceMap(): too few vertices requested");
        }
        catch (IllegalArgumentException iae) {}
        try
        {
            dsp.getDistanceMap(start, g.getVertexCount()+1);
            fail("getDistanceMap(): too many vertices requested");
        }
        catch (IllegalArgumentException iae) {}

        try
        {
            dsp.getIncomingEdge(start, v);
            fail("getIncomingEdge(): illegal destination vertex");
        }
        catch (IllegalArgumentException iae) {}
        try
        {
            dsp.getIncomingEdge(v, start);
            fail("getIncomingEdge(): illegal source vertex");
        }
        catch (IllegalArgumentException iae) {}
        try
        {
            dsp.getIncomingEdgeMap(v, 1);
            fail("getIncomingEdgeMap(): illegal source vertex");
        }
        catch (IllegalArgumentException iae) {}
        try
        {
            dsp.getIncomingEdgeMap(start, 0);
            fail("getIncomingEdgeMap(): too few vertices requested");
        }
        catch (IllegalArgumentException iae) {}
        try
        {
            dsp.getDistanceMap(start, g.getVertexCount()+1);
            fail("getIncomingEdgeMap(): too many vertices requested");
        }
        catch (IllegalArgumentException iae) {}

        try
        {
            // test negative edge weight exception
            String v1 = indexer.inverse().get(1);
            String v2 = indexer.inverse().get(7);
            e = g.getEdgeCount()+1;
         	g.addEdge(e, v1, v2);
         	edgeWeights.put(e, -2);
//            e.addUserDatum("weight", new Double(-2), UserData.REMOVE);
            dsp.reset();
            dsp.getDistanceMap(start);
//            for (Iterator it = g.getEdges().iterator(); it.hasNext(); )
//            {
//                Edge edge = (Edge)it.next();
//                double weight = ((Number)edge.getUserDatum("weight")).doubleValue();
//                Pair p = edge.getEndpoints();
//                int i = id.getIndex((Vertex)p.getFirst());
//                int j = id.getIndex((Vertex)p.getSecond());
//                System.out.print("(" + i + "," + j + "): " + weight);
//                if (weight < 0)
//                    System.out.print(" *******");
//                System.out.println();
//            }
            fail("DijkstraShortestPath should not accept negative edge weights");
        }
        catch (IllegalArgumentException iae) 
        {
            g.removeEdge(e);
        }
    }
    
    public void testDijkstra()
    {
        setUp();
        exceptionTest(dg, did, 1);
        
        setUp();
        exceptionTest(ug, uid, 1);
        
        setUp();
        getPathTest(dg, did, 1);
        
        setUp();
        getPathTest(ug, uid, 1);
                
        for (int i = 1; i <= dg_distances.length; i++)
        {
            setUp();
            weightedTest(dg, did, i, true);

            setUp();
            weightedTest(dg, did, i, false);
        }
        
        for (int i = 1; i <= ug_distances.length; i++)
        {
            setUp();
            weightedTest(ug, uid, i, true);
            
            setUp();
            weightedTest(ug, uid, i, false);
        }
        
    }

    private void getPathTest(Graph<String,Integer> g, BiMap<String,Integer> indexer, int index)
    {
        DijkstraShortestPath<String,Integer> dsp = 
        	new DijkstraShortestPath<String,Integer>(g, nev);
//        Indexer id = Indexer.getIndexer(g);
        String start = indexer.inverse().get(index);
        Integer[] edge_array = edgeArrays.get(g);
        Integer[] incomingEdges1 = null;
        if (g instanceof DirectedGraph)
            incomingEdges1 = dg_incomingEdges[index-1];
        if (g instanceof UndirectedGraph)
            incomingEdges1 = ug_incomingEdges[index-1];
        assertEquals(incomingEdges1.length, g.getVertexCount());
        
         // test getShortestPath(start, v)
        dsp.reset();
        for (int i = 1; i <= incomingEdges1.length; i++)
        {
          List<Integer> shortestPath = dsp.getPath(start, indexer.inverse().get(i));
            Integer[] indices = shortestPaths1[i-1];
            for (ListIterator<Integer> iter = shortestPath.listIterator(); iter.hasNext(); )
            {
                int j = iter.nextIndex();
                Integer e = iter.next();
                if (e != null)
                    assertEquals(edge_array[indices[j].intValue()], e);
                else
                    assertNull(indices[j]);
            }
        }
    }
    
    private void weightedTest(Graph<String,Integer> g, BiMap<String,Integer> indexer, int index, boolean cached) {
//        Indexer id = Indexer.getIndexer(g);
        String start = indexer.inverse().get(index);
        double[] distances1 = null;
        Integer[] incomingEdges1 = null;
        if (g instanceof DirectedGraph)
        {
            distances1 = dg_distances[index-1];
            incomingEdges1 = dg_incomingEdges[index-1];
        }
        if (g instanceof UndirectedGraph)
        {    
            distances1 = ug_distances[index-1];
            incomingEdges1 = ug_incomingEdges[index-1];
        }
        assertEquals(distances1.length, g.getVertexCount());
        assertEquals(incomingEdges1.length, g.getVertexCount());
        DijkstraShortestPath<String,Integer> dsp = 
        	new DijkstraShortestPath<String,Integer>(g, nev, cached);
        Integer[] edge_array = edgeArrays.get(g);
        
        // test getDistance(start, v)
        for (int i = 1; i <= distances1.length; i++) {
            String v = indexer.inverse().get(i);
            Number n = dsp.getDistance(start, v);
            double d = distances1[i-1];
            double dist;
            if (n == null)
                dist = Double.POSITIVE_INFINITY;
            else
                dist = n.doubleValue();
            
            assertEquals(d, dist, .001);
        }

        // test getIncomingEdge(start, v)
        dsp.reset();
        for (int i = 1; i <= incomingEdges1.length; i++)
        {
            String v = indexer.inverse().get(i);
            Integer e = dsp.getIncomingEdge(start, v);
            if (e != null)
                assertEquals(edge_array[incomingEdges1[i-1].intValue()], e);
            else
                assertNull(incomingEdges1[i-1]);
        }
        
        // test getDistanceMap(v)
        dsp.reset();
        Map<String,Number> distances = dsp.getDistanceMap(start);
        assertTrue(distances.size() <= g.getVertexCount());
        double d_prev = 0; // smallest possible distance
        Set<String> reachable = new HashSet<String>();
        for (Iterator<String> d_iter = distances.keySet().iterator(); d_iter.hasNext(); )
        {
            String cur = d_iter.next();
            double d_cur = ((Double)distances.get(cur)).doubleValue();
            assertTrue(d_cur >= d_prev);

            d_prev = d_cur;
            int i = indexer.get(cur);
            assertEquals(distances1[i-1], d_cur, .001);
            reachable.add(cur);
        }
        // make sure that non-reachable vertices have no entries
        for (Iterator<String> v_iter = g.getVertices().iterator(); v_iter.hasNext(); )
        {
            String v = v_iter.next();
            assertEquals(reachable.contains(v), distances.keySet().contains(v));
        }
        
        // test getIncomingEdgeMap(v)
        dsp.reset();
        Map<String,Integer> incomingEdgeMap = dsp.getIncomingEdgeMap(start);
        assertTrue(incomingEdgeMap.size() <= g.getVertexCount());
        for (Iterator<String> e_iter = incomingEdgeMap.keySet().iterator(); e_iter.hasNext(); )
        {
            String v = e_iter.next();
            Integer e = incomingEdgeMap.get(v);
            int i = indexer.get(v);
//            if (e != null)
//            {    
//                Pair endpoints = e.getEndpoints();
//                int j = id.getIndex((Vertex)endpoints.getFirst());
//                int k = id.getIndex((Vertex)endpoints.getSecond());
//                System.out.print(i + ": (" + j + "," + k + ");  ");
//            }
//            else
//                System.out.print(i + ": null;  ");
            if (e != null)
                assertEquals(edge_array[incomingEdges1[i-1].intValue()], e);
            else
                assertNull(incomingEdges1[i-1]);
        }
        
        // test getDistanceMap(v, k)
        dsp.reset();
        for (int i = 1; i <= distances1.length; i++)
        {
            distances = dsp.getDistanceMap(start, i);
            assertTrue(distances.size() <= i);
            d_prev = 0; // smallest possible distance

            reachable.clear();
            for (Iterator<String> d_iter = distances.keySet().iterator(); d_iter.hasNext(); )
            {
                String cur = d_iter.next();
                double d_cur = ((Double)distances.get(cur)).doubleValue();
                assertTrue(d_cur >= d_prev);

                d_prev = d_cur;
                int j = indexer.get(cur);

                assertEquals(distances1[j-1], d_cur, .001);
                reachable.add(cur);
            }
            for (Iterator<String> v_iter = g.getVertices().iterator(); v_iter.hasNext(); )
            {
                String v = v_iter.next();
                assertEquals(reachable.contains(v), distances.keySet().contains(v));
            }
        }
                
        // test getIncomingEdgeMap(v, k)
        dsp.reset();
        for (int i = 1; i <= incomingEdges1.length; i++)
        {
            incomingEdgeMap = dsp.getIncomingEdgeMap(start, i);
            assertTrue(incomingEdgeMap.size() <= i);
            for (Iterator<String> e_iter = incomingEdgeMap.keySet().iterator(); e_iter.hasNext(); )
            {
                String v = e_iter.next();
                Integer e = incomingEdgeMap.get(v);
                int j = indexer.get(v);
                if (e != null)
                    assertEquals(edge_array[incomingEdges1[j-1].intValue()], e);
                else
                    assertNull(incomingEdges1[j-1]);
            }
        }
    }
    
    public void addEdges(Graph<String,Integer> g, BiMap<String,Integer> indexer, Integer[] edge_array)
    {
    	
//        Indexer id = Indexer.getIndexer(g);
        for (int i = 0; i < edges.length; i++)
        {
            int[] edge = edges[i];
            Integer e = i;
            g.addEdge(i, indexer.inverse().get(edge[0]), indexer.inverse().get(edge[1]));
            edge_array[i] = e;
            if (edge.length > 2) {
            	edgeWeights.put(e, edge[2]);
//                e.addUserDatum("weight", edge[2]);
            }
        }
    }

    
//    private class UserDataEdgeWeight implements NumberEdgeValue
//    {
//        private Object ud_key;
//        
//        public UserDataEdgeWeight(Object key)
//        {
//            ud_key = key;
//        }
//        
//		/**
//		 * @see edu.uci.ics.jung.utils.NumberEdgeValue#getNumber(edu.uci.ics.jung.graph.ArchetypeEdge)
//		 */
//		public Number getNumber(ArchetypeEdge e)
//		{
//            return (Number)e.getUserDatum(ud_key);
//		}
//
//		/**
//		 * @see edu.uci.ics.jung.utils.NumberEdgeValue#setNumber(edu.uci.ics.jung.graph.ArchetypeEdge, java.lang.Number)
//		 */
//		public void setNumber(ArchetypeEdge e, Number n)
//		{
//            throw new UnsupportedOperationException();
//		}
//    }
}
