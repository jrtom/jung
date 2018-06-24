package edu.uci.ics.jung.visualization;

import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.layout.algorithms.CircleLayoutAlgorithm;
import edu.uci.ics.jung.visualization.picking.PickedState;
import java.awt.*;
import junit.framework.TestCase;

public class BasicVisualizationServerTest extends TestCase {

  /*
   * Previously, a bug was introduced where the RenderContext in BasicVisualizationServer was reassigned, resulting
   * in data like pickedNodeState to be lost.
   */
  public void testRenderContextNotOverridden() {
    Network<Object, Object> graph = NetworkBuilder.directed().build();
    CircleLayoutAlgorithm algorithm = new CircleLayoutAlgorithm();

    BasicVisualizationServer server =
        new BasicVisualizationServer<Object, Object>(graph, algorithm, new Dimension(600, 600));

    PickedState<Object> pickedNodeState = server.getRenderContext().getPickedNodeState();
    assertNotNull(pickedNodeState);
  }
}
