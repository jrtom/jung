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
 * @author Tom Nelson
 */
public class SimpleVertexSupport implements VertexSupport {

  protected Supplier<Object> vertexFactory;

  public SimpleVertexSupport(Supplier<Object> vertexFactory) {
    this.vertexFactory = vertexFactory;
  }

  public void startVertexCreate(BasicVisualizationServer vv, Point2D point) {
    Preconditions.checkState(
        vv.getModel().getLayoutMediator().getNetwork() instanceof MutableNetwork<?, ?>,
        "graph must be mutable");
    Object newVertex = vertexFactory.get();
    Layout<Object> layout = vv.getGraphLayout();
    MutableNetwork graph = (MutableNetwork) vv.getModel().getLayoutMediator().getNetwork();
    graph.addNode(newVertex);
    layout.setLocation(
        newVertex, vv.getRenderContext().getMultiLayerTransformer().inverseTransform(point));
    vv.repaint();
  }

  public void midVertexCreate(BasicVisualizationServer vv, Point2D point) {
    // noop
  }

  public void endVertexCreate(BasicVisualizationServer vv, Point2D point) {
    //noop
  }

  public Supplier<Object> getVertexFactory() {
    return vertexFactory;
  }

  public void setVertexFactory(Supplier<Object> vertexFactory) {
    this.vertexFactory = vertexFactory;
  }
}
