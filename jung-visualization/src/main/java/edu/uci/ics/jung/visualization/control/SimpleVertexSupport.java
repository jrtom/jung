package edu.uci.ics.jung.visualization.control;

import com.google.common.base.Preconditions;
import com.google.common.graph.MutableNetwork;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import java.awt.geom.Point2D;
import java.util.function.Supplier;

/**
 * sample implementation showing how to use the VertexSupport interface member of the
 * EditingGraphMousePlugin. override midVertexCreate and endVertexCreate for more elaborate
 * implementations
 *
 * @author tanelso
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class SimpleVertexSupport<V, E> implements VertexSupport<V, E> {

  protected Supplier<V> vertexFactory;

  public SimpleVertexSupport(Supplier<V> vertexFactory) {
    this.vertexFactory = vertexFactory;
  }

  public void startVertexCreate(BasicVisualizationServer<V, E> vv, Point2D point) {
    Preconditions.checkState(
        vv.getModel().getNetwork() instanceof MutableNetwork<?, ?>, "graph must be mutable");
    V newVertex = vertexFactory.get();
    Layout<V> layout = vv.getGraphLayout();
    MutableNetwork<V, E> graph = (MutableNetwork<V, E>) vv.getModel().getNetwork();
    graph.addNode(newVertex);
    layout.setLocation(
        newVertex, vv.getRenderContext().getMultiLayerTransformer().inverseTransform(point));
    vv.repaint();
  }

  public void midVertexCreate(BasicVisualizationServer<V, E> vv, Point2D point) {
    // noop
  }

  public void endVertexCreate(BasicVisualizationServer<V, E> vv, Point2D point) {
    //noop
  }

  public Supplier<V> getVertexFactory() {
    return vertexFactory;
  }

  public void setVertexFactory(Supplier<V> vertexFactory) {
    this.vertexFactory = vertexFactory;
  }
}
