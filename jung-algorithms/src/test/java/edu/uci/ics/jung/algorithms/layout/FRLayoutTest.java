package edu.uci.ics.jung.algorithms.layout;

import java.awt.Dimension;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.TestGraphs;

public class FRLayoutTest extends TestCase {
	
    protected Set<Integer> seedVertices = new HashSet<Integer>();

	public void testFRLayout() {
		
		Graph<String,Number> graph = TestGraphs.getOneComponentGraph();

		Layout<String,Number> layout = new FRLayout<String,Number>(graph);
		layout.setSize(new Dimension(600,600));
		if(layout instanceof IterativeContext) {
			layout.initialize();
			Relaxer relaxer = new VisRunner((IterativeContext)layout);
			relaxer.prerelax();
			relaxer.relax();
		}
	}
}
