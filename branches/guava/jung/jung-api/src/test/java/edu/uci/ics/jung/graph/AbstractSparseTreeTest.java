package edu.uci.ics.jung.graph;

import junit.framework.TestCase;

import com.google.common.base.Supplier;


public abstract class AbstractSparseTreeTest extends TestCase {

	protected Tree<String,Integer> tree;
	protected Supplier<DirectedGraph<String,Integer>> graphFactory;
	protected Supplier<Integer> edgeFactory;

	public void testRemoveVertex() {
		tree.addVertex("A");
		tree.addEdge(edgeFactory.get(), "A", "B");
		tree.addEdge(edgeFactory.get(), "A", "C");
		tree.addEdge(edgeFactory.get(), "B", "E");
		tree.addEdge(edgeFactory.get(), "B", "F");
//		System.err.println("tree is "+tree);
		tree.removeVertex("B");
//		System.err.println("tree now "+tree);
	}
	
	public void testSimpleTree() {
		tree.addVertex("A");
		tree.addEdge(edgeFactory.get(), "A", "B");
		tree.addEdge(edgeFactory.get(), "A", "C");
	}
	
	public void testCreateLoop() {
		try {
			tree.addVertex("A");
			tree.addEdge(edgeFactory.get(), "A", "A");
			fail("should not be able to addChild(v,v)");
		} catch(IllegalArgumentException e) {
			// all is well
		}
		try {
			tree.addEdge(edgeFactory.get(), "A", "B");
			tree.addEdge(edgeFactory.get(), "B", "A");
			fail("should not allow loop");
		} catch(IllegalArgumentException e) {
			// all is well
		}
	}
	
	public void testHeightAndDepth() {
		tree.addVertex("V0");
        assertEquals(tree.getHeight(), 0);
        assertEquals(tree.getDepth("V0"), 0);
    	tree.addEdge(edgeFactory.get(), "V0", "V1");
        assertEquals(tree.getHeight(), 1);
        assertEquals(tree.getDepth("V1"), 1);
    	tree.addEdge(edgeFactory.get(), "V0", "V2");
        assertEquals(tree.getHeight(), 1);
        assertEquals(tree.getDepth("V2"), 1);
    	tree.addEdge(edgeFactory.get(), "V1", "V4");
        assertEquals(tree.getHeight(), 2);
        assertEquals(tree.getDepth("V4"), 2);
    	tree.addEdge(edgeFactory.get(), "V2", "V3");
        assertEquals(tree.getHeight(), 2);
        assertEquals(tree.getDepth("V3"), 2);
    	tree.addEdge(edgeFactory.get(), "V2", "V5");
        assertEquals(tree.getHeight(), 2);
        assertEquals(tree.getDepth("V5"), 2);
    	tree.addEdge(edgeFactory.get(), "V4", "V6");
        assertEquals(tree.getHeight(), 3);
        assertEquals(tree.getDepth("V6"), 3);
    	tree.addEdge(edgeFactory.get(), "V4", "V7");
        assertEquals(tree.getHeight(), 3);
        assertEquals(tree.getDepth("V7"), 3);
    	tree.addEdge(edgeFactory.get(), "V3", "V8");
        assertEquals(tree.getHeight(), 3);
        assertEquals(tree.getDepth("V8"), 3);
    	tree.addEdge(edgeFactory.get(), "V6", "V9");
        assertEquals(tree.getHeight(), 4);
        assertEquals(tree.getDepth("V9"), 4);
    	tree.addEdge(edgeFactory.get(), "V4", "V10");
        assertEquals(tree.getHeight(), 4);
        assertEquals(tree.getDepth("V10"), 3);
       	tree.addEdge(edgeFactory.get(), "V4", "V11");
        assertEquals(tree.getHeight(), 4);
        assertEquals(tree.getDepth("V11"), 3);
       	tree.addEdge(edgeFactory.get(), "V4", "V12");
        assertEquals(tree.getHeight(), 4);
        assertEquals(tree.getDepth("V12"), 3);
       	tree.addEdge(edgeFactory.get(), "V6", "V13");
        assertEquals(tree.getHeight(), 4);
        assertEquals(tree.getDepth("V13"), 4);
       	tree.addEdge(edgeFactory.get(), "V10", "V14");
        assertEquals(tree.getHeight(), 4);
        assertEquals(tree.getDepth("V14"), 4);
       	tree.addEdge(edgeFactory.get(), "V13", "V15");
        assertEquals(tree.getHeight(), 5);
        assertEquals(tree.getDepth("V15"), 5);
       	tree.addEdge(edgeFactory.get(), "V13", "V16");
       	assertEquals(tree.getHeight(), 5);
        assertEquals(tree.getDepth("V16"), 5);

	}
	
	
}
