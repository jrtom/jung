package edu.uci.ics.jung.graph.impl;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.graph.AbstractSparseTreeTest;
import edu.uci.ics.jung.graph.impl.DelegateTree;
import edu.uci.ics.jung.graph.impl.DirectedSparseMultigraph;

public class SparseTreeTest extends AbstractSparseTreeTest {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		graphFactory = DirectedSparseMultigraph.<String,Integer>getFactory();
		edgeFactory = new Supplier<Integer>() {
			int i=0;
			public Integer get() {
				return i++;
			}};
		tree = new DelegateTree<String,Integer>(graphFactory);
	}
}
