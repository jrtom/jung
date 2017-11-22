package edu.uci.ics.jung.visualization;

import static edu.uci.ics.jung.visualization.layout.AWT.POINT_MODEL;

import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.layout.algorithms.CircleLayoutAlgorithm;
import edu.uci.ics.jung.visualization.picking.PickedState;
import junit.framework.TestCase;

public class BasicVisualizationServerTest extends TestCase {

  /*
   * Previously, a bug was introduced where the RenderContext in BasicVisualizationServer was reassigned, resulting
   * in data like pickedVertexState to be lost.
   */
  public void testRenderContextNotOverridden() {
    Network<Object, Object> graph = NetworkBuilder.directed().build();
    CircleLayoutAlgorithm algorithm = new CircleLayoutAlgorithm(POINT_MODEL);

    BasicVisualizationServer server =
        new BasicVisualizationServer<Object, Object>(graph, algorithm);

    PickedState<Object> pickedVertexState = server.getRenderContext().getPickedVertexState();
    assertNotNull(pickedVertexState);
  }
}
