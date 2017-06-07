package edu.uci.ics.jung.algorithms.layout;

import java.awt.Dimension;
import java.util.HashSet;
import java.util.Set;

import com.google.common.graph.Network;

import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.util.TestGraphs;
import junit.framework.TestCase;

public class FRLayout2Test extends TestCase {
	
    protected Set<Integer> seedVertices = new HashSet<Integer>();

	public void testFRLayout() {
		
		Network<String,Number> graph = TestGraphs.getOneComponentGraph();

		Layout<String> layout = new FRLayout2<String>(graph.asGraph());
		layout.setSize(new Dimension(600,600));
		if(layout instanceof IterativeContext) {
			layout.initialize();
			Relaxer relaxer = new VisRunner((IterativeContext)layout);
			relaxer.prerelax();
			relaxer.relax();
		}
	}
}
