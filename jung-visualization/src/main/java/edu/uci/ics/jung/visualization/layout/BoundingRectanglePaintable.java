package edu.uci.ics.jung.visualization.layout;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;

public class BoundingRectanglePaintable<V,E> implements VisualizationServer.Paintable {

	protected RenderContext<V,E> rc;
	protected Graph<V,E> graph;
	protected Layout<V,E> layout;
	protected List<Rectangle2D> rectangles;
	
	public BoundingRectanglePaintable(RenderContext<V, E> rc, Layout<V, E> layout) {
		super();
		this.rc = rc;
		this.layout = layout;
		this.graph = layout.getGraph();
		final BoundingRectangleCollector<V,E> brc = new BoundingRectangleCollector<V,E>(rc, layout);
		this.rectangles = brc.getRectangles();
		if(layout instanceof ChangeEventSupport) {
			((ChangeEventSupport)layout).addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					brc.compute();
					rectangles = brc.getRectangles();
				}});
		}
	}
	
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g.setColor(Color.cyan);
		
		for(Rectangle2D r : rectangles) {
			g2d.draw(rc.getMultiLayerTransformer().transform(Layer.LAYOUT, r));
		}
	}

	public boolean useTransform() {
		return true;
	}

}
