package edu.uci.ics.jung.graph;

import edu.uci.ics.jung.graph.DelegateTree;

public class TreeUtilsTest extends AbstractTreeUtilsTest {
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		tree = new DelegateTree<String,Integer>();
		tree.addVertex("A");
		tree.addEdge(1, "A", "B0");
		tree.addEdge(2, "A", "B1");
		tree.addEdge(3, "B0", "C0");
		tree.addEdge(4, "C0", "D0");
		tree.addEdge(5, "C0", "D1");
	}
	

}
