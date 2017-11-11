package edu.uci.ics.jung.visualization.control;

import com.google.common.base.Preconditions;
import com.google.common.graph.MutableNetwork;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationModel;
import java.awt.geom.Point2D;
import java.util.function.Supplier;

/**
 * sample implementation showing how to use the VertexSupport interface member of the
 * EditingGraphMousePlugin. override midVertexCreate and endVertexCreate for more elaborate
 * implementations
 *
 * @author tanelso
 * @param <N> the vertex type
 * @param <E> the edge type
 */
public class SimpleVertexSupport<N, E> implements VertexSupport<N, E> {

  protected Supplier<N> vertexFactory;

  public SimpleVertexSupport(Supplier<N> vertexFactory) {
    this.vertexFactory = vertexFactory;
  }

  public void startVertexCreate(BasicVisualizationServer<N, E> vv, Point2D point) {
    Preconditions.checkState(
        vv.getModel().getNetwork() instanceof MutableNetwork<?, ?>, "graph must be mutable");
    N newVertex = vertexFactory.get();
    VisualizationModel<N, E, Point2D> visualizationModel = vv.getModel();
    MutableNetwork<N, E> graph = (MutableNetwork<N, E>) visualizationModel.getNetwork();
    graph.addNode(newVertex);
    visualizationModel
        .getLayoutModel()
        .set(newVertex, vv.getRenderContext().getMultiLayerTransformer().inverseTransform(point));
    vv.repaint();
  }

  public void midVertexCreate(BasicVisualizationServer<N, E> vv, Point2D point) {
    // noop
  }

  public void endVertexCreate(BasicVisualizationServer<N, E> vv, Point2D point) {
    //noop
  }

  public Supplier<N> getVertexFactory() {
    return vertexFactory;
  }

  public void setVertexFactory(Supplier<N> vertexFactory) {
    this.vertexFactory = vertexFactory;
  }
}
