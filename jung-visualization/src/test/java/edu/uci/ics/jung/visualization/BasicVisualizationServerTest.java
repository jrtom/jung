package edu.uci.ics.jung.visualization;

import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.visualization.picking.PickedState;
import junit.framework.TestCase;

public class BasicVisualizationServerTest extends TestCase {

  /*
   * Previously, a bug was introduced where the RenderContext in BasicVisualizationServer was reassigned, resulting
   * in data like pickedVertexState to be lost.
   */
  public void testRenderContextNotOverridden() {
    Network<Object, Object> graph = NetworkBuilder.directed().build();
    CircleLayout<Object> layout = new CircleLayout<Object>(graph.asGraph());

    BasicVisualizationServer<Object, Object> server =
        new BasicVisualizationServer<Object, Object>(graph, layout);

    PickedState<Object> pickedVertexState = server.getRenderContext().getPickedVertexState();
    assertNotNull(pickedVertexState);
  }
}
