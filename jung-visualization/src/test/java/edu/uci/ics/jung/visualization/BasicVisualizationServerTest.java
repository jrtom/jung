package edu.uci.ics.jung.visualization;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.visualization.picking.PickedState;
import junit.framework.TestCase;

public class BasicVisualizationServerTest extends TestCase {

  /*
   * Previously, a bug was introduced where the RenderContext in BasicVisualizationServer was reassigned, resulting
   * in data like pickedVertexState to be lost.
   */
  public void testRenderContextNotOverridden() {
    SparseGraph<Object, Object> graph = new SparseGraph<Object, Object>();
    CircleLayout<Object, Object> layout = new CircleLayout<Object, Object>(graph);

    BasicVisualizationServer<Object, Object> server = new BasicVisualizationServer<Object, Object>(layout);

    PickedState<Object> pickedVertexState = server.getRenderContext().getPickedVertexState();
    assertNotNull(pickedVertexState);
  }
}
