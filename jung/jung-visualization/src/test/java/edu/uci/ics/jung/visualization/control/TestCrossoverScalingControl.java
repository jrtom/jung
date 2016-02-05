package edu.uci.ics.jung.visualization.control;

import java.awt.geom.Point2D;

import junit.framework.TestCase;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationServer;

public class TestCrossoverScalingControl extends TestCase {

	CrossoverScalingControl sc;
	VisualizationServer<?, ?> vv;
	
	float crossover;
	float scale;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public void setUp() {
		sc = new CrossoverScalingControl();
		vv = new BasicVisualizationServer(new FRLayout(new SparseGraph()));
	}
	public void testCrossover() {
		crossover = 2.0f;
		scale = .5f;
		sc.setCrossover(crossover);
		sc.scale(vv, scale, new Point2D.Double());
//		System.err.println("crossover="+crossover);
//		System.err.println("scale="+scale);
//		System.err.println("layout scale = "+vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).getScale());
//		System.err.println("view scale = "+vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW).getScale());
	}
	public void testCrossover2() {
		crossover = 2.0f;
		scale = 1.5f;
		sc.setCrossover(crossover);
		sc.scale(vv, scale, new Point2D.Double());
//		System.err.println("crossover="+crossover);
//		System.err.println("scale="+scale);
//		System.err.println("layout scale = "+vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).getScale());
//		System.err.println("view scale = "+vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW).getScale());
		
	}
	public void testCrossover3() {
		crossover = 2.0f;
		scale = 2.5f;
		sc.setCrossover(crossover);
		sc.scale(vv, scale, new Point2D.Double());
//		System.err.println("crossover="+crossover);
//		System.err.println("scale="+scale);
//		System.err.println("layout scale = "+vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).getScale());
//		System.err.println("view scale = "+vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW).getScale());
	}
	public void testCrossover4() {
		crossover = 0.5f;
		scale = 2.5f;
		sc.setCrossover(crossover);
		sc.scale(vv, scale, new Point2D.Double());
//		System.err.println("crossover="+crossover);
//		System.err.println("scale="+scale);
//		System.err.println("layout scale = "+vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).getScale());
//		System.err.println("view scale = "+vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW).getScale());
	}
	public void testCrossover5() {
		crossover = 0.5f;
		scale = .3f;
		sc.setCrossover(crossover);
		sc.scale(vv, scale, new Point2D.Double());
//		System.err.println("crossover="+crossover);
//		System.err.println("scale="+scale);
//		System.err.println("layout scale = "+vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).getScale());
//		System.err.println("view scale = "+vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW).getScale());
	}
}
