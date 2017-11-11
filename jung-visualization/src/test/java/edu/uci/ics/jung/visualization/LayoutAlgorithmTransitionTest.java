package edu.uci.ics.jung.visualization;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import edu.uci.ics.jung.visualization.layout.*;
import java.awt.geom.Point2D;
import org.junit.Test;

public class LayoutAlgorithmTransitionTest {

  @Test
  public void testTransition() throws Exception {
    DomainModel<Point2D> domainModel = new AWTDomainModel();
    MutableGraph<String> graph = GraphBuilder.undirected().build();
    graph.addNode("A");
    LayoutModel<String, Point2D> model = new LoadingCacheLayoutModel(graph, domainModel, 100, 100);
    model.set("A", 0, 0);
    LayoutAlgorithm newLayoutAlgorithm = new StaticLayoutAlgorithm(domainModel);

    //        LayoutAlgorithmTransition transition = new LayoutAlgorithmTransition();
  }
}
