package edu.uci.ics.jung.visualization.layout;

import java.awt.geom.Point2D;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public class LayoutTransition<V,E> implements IterativeContext {
	
	protected Layout<V,E> startLayout;
	protected Layout<V,E> endLayout;
	protected Layout<V,E> transitionLayout;
	protected boolean done = false;
	protected int count = 20;
	protected int counter = 0;
	protected VisualizationViewer<V,E> vv;

	/**
	 * @param startLayout
	 * @param endLayout
	 */
	public LayoutTransition(VisualizationViewer<V,E> vv, Layout<V, E> startLayout, Layout<V, E> endLayout) {
		this.vv = vv;
		this.startLayout = startLayout;
		this.endLayout = endLayout;
		if(endLayout instanceof IterativeContext) {
			Relaxer relaxer = new VisRunner((IterativeContext)endLayout);
			relaxer.prerelax();
		}
		this.transitionLayout =
			new StaticLayout<V,E>(startLayout.getGraph(), startLayout);
		vv.setGraphLayout(transitionLayout);
	}

	public boolean done() {
		return done;
	}

	public void step() {
		Graph<V,E> g = transitionLayout.getGraph();
		for(V v : g.getVertices()) {
			Point2D tp = transitionLayout.transform(v);
			Point2D fp = endLayout.transform(v);
			double dx = (fp.getX()-tp.getX())/(count-counter);
			double dy = (fp.getY()-tp.getY())/(count-counter);
			transitionLayout.setLocation(v, 
					new Point2D.Double(tp.getX()+dx,tp.getY()+dy));
		}
		counter++;
		if(counter >= count) {
			done = true;
			vv.setGraphLayout(endLayout);
		}
		vv.repaint();
	}
}
