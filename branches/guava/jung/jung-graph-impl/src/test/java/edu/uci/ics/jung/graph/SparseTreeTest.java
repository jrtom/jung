package edu.uci.ics.jung.graph;

import org.apache.commons.collections15.Factory;


public class SparseTreeTest extends AbstractSparseTreeTest {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		graphFactory = DirectedSparseMultigraph.<String,Integer>getFactory();
		edgeFactory = new Factory<Integer>() {
			int i=0;
			public Integer create() {
				return i++;
			}};
		tree = new DelegateTree<String,Integer>(graphFactory);
	}
}
