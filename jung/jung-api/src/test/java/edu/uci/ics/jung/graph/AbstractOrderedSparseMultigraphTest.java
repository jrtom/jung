package edu.uci.ics.jung.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

public abstract class AbstractOrderedSparseMultigraphTest extends TestCase {

    protected Integer v0 = 0;
    protected Integer v1 = 1;
    protected Integer v2 = 2;
    protected Number e01 = .1;
    protected Number e10 = .2;
    protected Number e12 = .3;
    protected Number e21 = .4;

    protected Supplier<Number> vertexFactory = new Supplier<Number>() {
    	int v=0;
		public Number get() {
			return v++;
		}
    };
    protected Supplier<Number> edgeFactory = new Supplier<Number>() {
    	int e=0;
		public Number get() {
			return e++;
		}
    };
    
    protected Graph<Number,Number> graph;
    protected int vertexCount = 50;
    protected Graph<Integer,Number> smallGraph;

    public void testGetEdges() {
        assertEquals(smallGraph.getEdgeCount(), 4);
//        System.err.println("getEdges()="+graph.getEdges());
    }

    public void testGetVertices() {
        assertEquals(smallGraph.getVertexCount(), 3);
//        System.err.println("getVertices()="+graph.getVertices());
    }

    public void testAddVertex() {
        int count = graph.getVertexCount();
        graph.addVertex(count);
        assertEquals(graph.getVertexCount(), count+1);
    }

    public void testRemoveEndVertex() {
        int vertexCount = graph.getVertexCount();
        int edgeCount = graph.getEdgeCount();
        Collection<Number> incident = graph.getIncidentEdges(vertexCount-1);
        graph.removeVertex(vertexCount-1);
        assertEquals(vertexCount-1, graph.getVertexCount());
        assertEquals(edgeCount - incident.size(), graph.getEdgeCount());
    }

    public void testRemoveMiddleVertex() {
        int vertexCount = graph.getVertexCount();
        int edgeCount = graph.getEdgeCount();
        Collection<Number> incident = graph.getIncidentEdges(vertexCount/2);
        graph.removeVertex(vertexCount/2);
        assertEquals(vertexCount-1, graph.getVertexCount());
        assertEquals(edgeCount - incident.size(), graph.getEdgeCount());
    }

    public void testAddEdge() {
        int edgeCount = graph.getEdgeCount();
        graph.addEdge(edgeFactory.get(), 0, 1);
        assertEquals(graph.getEdgeCount(), edgeCount+1);
    }
    
    public void testNullEndpoint() {
    	try {
    		graph.addEdge(edgeFactory.get(), new Pair<Number>(1,null));
    		fail("should not be able to add an edge with a null endpoint");
    	} catch(IllegalArgumentException e) {
    		// all is well
    	}
    }


    public void testRemoveEdge() {
    	List<Number> edgeList = new ArrayList<Number>(graph.getEdges());
        int edgeCount = graph.getEdgeCount();
        graph.removeEdge(edgeList.get(edgeList.size()/2));
        assertEquals(graph.getEdgeCount(), edgeCount-1);
    }

    public void testGetInOutEdges() {
    	for(Number v : graph.getVertices()) {
    		Collection<Number> incident = graph.getIncidentEdges(v);
    		Collection<Number> in = graph.getInEdges(v);
    		Collection<Number> out = graph.getOutEdges(v);
    		assertTrue(incident.containsAll(in));
    		assertTrue(incident.containsAll(out));
    		for(Number e : in) {
    			if(out.contains(e)) {
    				assertTrue(graph.getEdgeType(e) == EdgeType.UNDIRECTED);
    			}
    		}
    		for(Number e : out) {
    			if(in.contains(e)) {
    				assertTrue(graph.getEdgeType(e) == EdgeType.UNDIRECTED);
    			}
    		}
    	}
    	
        assertEquals(smallGraph.getInEdges(v1).size(), 4);
        assertEquals(smallGraph.getOutEdges(v1).size(), 3);
        assertEquals(smallGraph.getOutEdges(v0).size(), 2);
    }

    public void testGetPredecessors() {
        assertTrue(smallGraph.getPredecessors(v0).containsAll(Collections.singleton(v1)));
    }

    public void testGetSuccessors() {
        assertTrue(smallGraph.getPredecessors(v1).contains(v0));
        assertTrue(smallGraph.getPredecessors(v1).contains(v2));
    }

    public void testGetNeighbors() {
        Collection<Integer> neighbors = smallGraph.getNeighbors(v1);
        assertTrue(neighbors.contains(v0));
        assertTrue(neighbors.contains(v2));
    }

    public void testGetIncidentEdges() {
        assertEquals(smallGraph.getIncidentEdges(v0).size(), 2);
    }

    public void testFindEdge() {
        Number edge = smallGraph.findEdge(v1, v2);
        assertTrue(edge == e12 || edge == e21);
    }

    public void testGetEndpoints() {
        Pair<Integer> endpoints = smallGraph.getEndpoints(e01);
        assertTrue((endpoints.getFirst() == v0 && endpoints.getSecond() == v1) ||
                endpoints.getFirst() == v1 && endpoints.getSecond() == v0);
    }

    public void testIsDirected() {
        for(Number edge : smallGraph.getEdges()) {
        	if(edge == e21) {
        		assertEquals(smallGraph.getEdgeType(edge), EdgeType.DIRECTED);
        	} else {
        		assertEquals(smallGraph.getEdgeType(edge), EdgeType.UNDIRECTED);
        	}
        }
    }
}
