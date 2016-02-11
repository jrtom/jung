package edu.uci.ics.jung.graph;

import java.util.Collection;
import java.util.Collections;

import junit.framework.TestCase;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

public abstract class AbstractDirectedSparseMultigraphTest extends TestCase {

	protected Integer v0 = new Integer(0);
    protected Integer v1 = new Integer(1);
    protected Integer v2 = new Integer(2);
    
    protected Float e01 = new Float(.1f);
    protected Float e10 = new Float(.2f);
    protected Float e12 = new Float(.3f);
    protected Float e21 = new Float(.4f);
    
    protected Graph<Integer,Number> graph;

    public void testGetEdges() {
        assertEquals(graph.getEdgeCount(), 4);
    }

    public void testGetVertices() {
        assertEquals(graph.getVertexCount(), 3);
    }

    public void testAddVertex() {
        int count = graph.getVertexCount();
        graph.addVertex(new Integer(3));
        assertEquals(graph.getVertexCount(), count+1);
    }

    public void testRemoveEndVertex() {
        int vertexCount = graph.getVertexCount();
        graph.removeVertex(v0);
        assertEquals(vertexCount-1, graph.getVertexCount());
        assertEquals(2, graph.getEdgeCount());
    }

    public void testRemoveMiddleVertex() {
        int vertexCount = graph.getVertexCount();
        graph.removeVertex(v1);
        assertEquals(vertexCount-1, graph.getVertexCount());
        assertEquals(0, graph.getEdgeCount());
    }

    public void testAddEdge() {
        int edgeCount = graph.getEdgeCount();
        graph.addEdge(new Double(.5), v0, v1);
        assertEquals(graph.getEdgeCount(), edgeCount+1);
    }

    public void testRemoveEdge() {
        int edgeCount = graph.getEdgeCount();
        graph.removeEdge(e12);
        assertEquals(graph.getEdgeCount(), edgeCount-1);
    }
    
    public void testNullEndpoint() {
    	try {
    		graph.addEdge(.99, new Pair<Integer>(1,null));
    		fail("should not be able to add an edge with a null endpoint");
    	} catch(IllegalArgumentException e) {
    		// all is well
    	}
    }

    public void testGetInEdges() {
        assertEquals(graph.getInEdges(v1).size(), 2);
    }

    public void testGetOutEdges() {
        assertEquals(graph.getOutEdges(v1).size(), 2);
    }

    public void testGetPredecessors() {
        assertTrue(graph.getPredecessors(v0).containsAll(Collections.singleton(v1)));
    }

    public void testGetSuccessors() {
        assertTrue(graph.getPredecessors(v1).contains(v0));
        assertTrue(graph.getPredecessors(v1).contains(v2));
    }

    public void testGetNeighbors() {
        Collection<Integer> neighbors = graph.getNeighbors(v1);
        assertTrue(neighbors.contains(v0));
        assertTrue(neighbors.contains(v2));
    }

    public void testGetIncidentEdges() {
        assertEquals(graph.getIncidentEdges(v0).size(), 2);
    }

    public void testFindEdge() {
        Number edge = graph.findEdge(v1, v2);
        assertTrue(edge == e12 || edge == e21);
    }

    public void testGetEndpoints() {
        Pair<Integer> endpoints = graph.getEndpoints(e01);
        assertTrue((endpoints.getFirst() == v0 && endpoints.getSecond() == v1) ||
                endpoints.getFirst() == v1 && endpoints.getSecond() == v0);
    }

    public void testIsDirected() {
        for(Number edge : graph.getEdges()) {
            assertEquals(graph.getEdgeType(edge), EdgeType.DIRECTED);
        }
    }

    public void testAddDirectedEdge() {
        Float edge = new Float(.9);
        graph.addEdge(edge, v1, v2, EdgeType.DIRECTED);
        assertEquals(graph.getEdgeType(edge), EdgeType.DIRECTED);
    }
    
    public void testAddUndirectedEdge() {
        try {
            graph.addEdge(new Float(.9), v1, v2, EdgeType.UNDIRECTED);
            fail("Cannot add an undirected edge to this graph");
        } catch(IllegalArgumentException uoe) {
            // all is well
        }
    }

}
