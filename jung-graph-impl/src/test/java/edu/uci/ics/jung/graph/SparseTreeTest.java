package edu.uci.ics.jung.graph;

import com.google.common.base.Supplier;

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
