/*
 * 
 * Created on Oct 30, 2003
 */
package edu.uci.ics.jung.algorithms.matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.collections15.Factory;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

/**
 * 
 * @author Joshua O'Madadhain
 * @author Tom Nelson - converted to jung2
 */
public class GraphMatrixOperationsTest extends TestCase
{
	Factory<UndirectedGraph<String, String>> undirectedGraphFactory;
	Factory<DirectedGraph<String, String>> directedGraphFactory;
	Factory<Graph<String,String>> graphFactory;
	Factory<String> vertexFactory;
	Factory<String> edgeFactory;

    private Graph<String,String> g;
    Map<String,String> sl;
    private MatrixElementOperations<String> meo;
    private final static int NUM_VERTICES = 10;
    protected int[][] edges = 
        {{1,2,2}, {1,4,1}, 
         {2,4,3}, {2,5,10},
         {3,1,4}, {3,6,5},
         {4,3,2}, {4,5,2}, {4,6,8}, {4,7,4},
         {5,7,6},
         {7,6,1},
         {8,9,4}, // these three edges define a second connected component
         {9,10,1},
         {10,8,2}};
    Map<String,Number> weights;
    
    @Override
    protected void setUp()
    {
    	undirectedGraphFactory = new Factory<UndirectedGraph<String,String>>() {
    		public UndirectedGraph<String,String> create() {
    			return new UndirectedSparseGraph<String,String>();
    		}
    	};
    	directedGraphFactory = new Factory<DirectedGraph<String,String>>() {
    		public DirectedGraph<String,String> create() {
    			return new DirectedSparseGraph<String,String>();
    		}
    	};
    	graphFactory = new Factory<Graph<String,String>>() {
    		public Graph<String,String> create() {
    			return new SparseGraph<String,String>();
    		}
    	};
    	vertexFactory = new Factory<String>() {
    		int n = 0;
    		public String create() { return "V"+n++; }
    	};
    	edgeFactory = new Factory<String>() {
    		int i = 0;
    		public String create() { return "E"+i++; }
    	};


    	g = new DirectedSparseMultigraph<String,String>();
    	weights = new HashMap<String,Number>();
        meo = new RealMatrixElementOperations<String>(weights);
        // graph based on Weiss, _Data Structures and Algorithm Analysis_,
        // 1992, p. 292
        for(int i=1; i<NUM_VERTICES+1; i++) {
        	g.addVertex(vertexFactory.create());
        }
        sl = new HashMap<String,String>();
        addEdges(g, edges);    
    }

    public void testMatrixToGraphToMatrixDirected()
    {
        DoubleMatrix2D m = new SparseDoubleMatrix2D(g.getVertexCount(), g.getVertexCount());
        for (int i = 0; i < edges.length; i++)
            m.setQuick(edges[i][0] - 1, edges[i][1] - 1, edges[i][2]);
        
        Graph<String,String> g2 = GraphMatrixOperations.<String,String>matrixToGraph(m, 
        		directedGraphFactory, vertexFactory, edgeFactory, weights);
        
        DoubleMatrix2D m2 = GraphMatrixOperations.graphToSparseMatrix(g2, weights);
        
        assertEquals(m, m2);
    }
    
    public void testMatrixToGraphToMatrixUndirected()
    {
        DoubleMatrix2D m = new SparseDoubleMatrix2D(g.getVertexCount(), g.getVertexCount());
        for (int i = 0; i < edges.length; i++)
        {
            m.setQuick(edges[i][0] - 1, edges[i][1] - 1, edges[i][2]);
            m.setQuick(edges[i][1] - 1, edges[i][0] - 1, edges[i][2]);
        }
        
        Graph<String, String> g2 = GraphMatrixOperations.<String,String>matrixToGraph(m, 
        		undirectedGraphFactory, vertexFactory, edgeFactory,	weights);
        
        DoubleMatrix2D m2 = GraphMatrixOperations.graphToSparseMatrix(g2, weights);
        
        assertEquals(m, m2);
    }

    public void testSquare()
    {
        int[][] g3_edges = 
            {{1,3,2}, {1,4,6}, {1,5,22}, {1,6,8}, {1,7,4},
             {2,3,6}, {2,5,6}, {2,6,24}, {2,7,72},
             {3,2,8}, {3,4,4}, 
             {4,1,8}, {4,6,14}, {4,7,12},
             {5,6,6},
             {8,10,4},
             {9,8,2},
             {10,9,8}
            };
        
        Graph<String,String> g2 = 
        	GraphMatrixOperations.<String,String>square(g, edgeFactory, meo);
        
        Graph<String,String> g3 = new SparseMultigraph<String,String>();
        for (String v : g.getVertices())
        {
        	g3.addVertex(v);
        }
        addEdges(g3, g3_edges);
        
        // check vertex/edge set sizes
        assertTrue(g2.getVertexCount() == g3.getVertexCount());
        assertTrue(g2.getEdgeCount() == g3.getEdgeCount());      
        
        // check vertex sets
        assertEquals(g2.getVertexCount(), g3.getVertexCount());
        
        // check for equivalent vertices, edges, and edge weights
        for (int i = 0; i < g3_edges.length; i++)
        {
        	int src_idx = g3_edges[i][0];
            int dst_idx = g3_edges[i][1];
            int g3_weight = g3_edges[i][2];

            String g3_src = sl.get(new Integer(src_idx).toString());
            String g3_dst = sl.get(new Integer(dst_idx).toString());
            
            String g2_src = g3_src;
            assertNotNull(g2_src);
            String g2_dst = g3_dst;
            assertNotNull(g2_dst);
            
            String e = g2.findEdge(g2_src,g2_dst); 
            assertNotNull(e);
            // we already know that g3 has a corresponding edge, 
            // because we got it from g3_edges

            int g2_weight = weights.get(e).intValue();
            	//((MutableDouble)e.getUserDatum("weight")).intValue();

            assertEquals(g2_weight, g3_weight);
        }
        
    }
    
    private void addEdges(Graph<String,String> graph, int[][] edges)
    {
    	List<String> id = new ArrayList<String>(graph.getVertices());
        for (int i = 0; i < edges.length; i++)
        {
            int[] edge = edges[i];
				String s1 = new Integer(edge[0]).toString();
				String v1 = sl.get(s1);
				if (v1 == null)
                {                
                    v1 = id.get(edge[0]-1);
                    sl.put(s1, v1);
                }
                
				String s2 = new Integer(edge[1]).toString();
				String v2 = sl.get(s2);
				if (v2 == null)
                { 
                    v2 = id.get(edge[1]-1);
                    sl.put(s2, v2);
                }
                
                String e = edgeFactory.create();
                graph.addEdge(e, v1, v2);
                if (edge.length > 2)
                	weights.put(e, edge[2]);

        }
    }

}
