package edu.uci.ics.jung.visualization.control;

import java.awt.geom.Point2D;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;

/** 
 * sample implementation showing how to use the VertexSupport interface member of the
 * EditingGraphMousePlugin.
 * override midVertexCreate and endVertexCreate for more elaborate implementations
 * @author tanelso
 *
 * @param <V,E>
 */
public class SimpleVertexSupport<V,E> implements VertexSupport<V,E> {

	protected Supplier<V> vertexFactory;
	
	public SimpleVertexSupport(Supplier<V> vertexFactory) {
		this.vertexFactory = vertexFactory;
	}
	
//	@Override
	public void startVertexCreate(BasicVisualizationServer<V, E> vv,
			Point2D point) {
		V newVertex = vertexFactory.get();
		Layout<V,E> layout = vv.getGraphLayout();
		Graph<V,E> graph = layout.getGraph();
		graph.addVertex(newVertex);
		layout.setLocation(newVertex, vv.getRenderContext().getMultiLayerTransformer().inverseTransform(point));
		vv.repaint();
	}

//	@Override
	public void midVertexCreate(BasicVisualizationServer<V, E> vv,
			Point2D point) {
		// noop
	}

//	@Override
	public void endVertexCreate(BasicVisualizationServer<V, E> vv,
			Point2D point) {
		//noop
	}

	public Supplier<V> getVertexFactory() {
		return vertexFactory;
	}

	public void setVertexFactory(Supplier<V> vertexFactory) {
		this.vertexFactory = vertexFactory;
	}

}
