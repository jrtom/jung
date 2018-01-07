package edu.uci.ics.jung.visualization;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.StaticLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel;
import org.junit.Test;

public class LayoutAlgorithmTransitionTest {

  @Test
  public void testTransition() throws Exception {
    MutableGraph<String> graph = GraphBuilder.undirected().build();
    graph.addNode("A");
    LayoutModel<String> model =
        LoadingCacheLayoutModel.<String>builder().setGraph(graph).setSize(100, 100).build();

    model.set("A", 0, 0);
    LayoutAlgorithm newLayoutAlgorithm = new StaticLayoutAlgorithm();
  }
}
