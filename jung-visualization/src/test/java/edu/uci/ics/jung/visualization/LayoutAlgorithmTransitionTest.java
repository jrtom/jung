package edu.uci.ics.jung.visualization;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.StaticLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel;
import edu.uci.ics.jung.layout.model.PointModel;
import edu.uci.ics.jung.visualization.layout.AWTPointModel;
import java.awt.geom.Point2D;
import org.junit.Test;

public class LayoutAlgorithmTransitionTest {

  @Test
  public void testTransition() throws Exception {
    PointModel<Point2D> pointModel = new AWTPointModel();
    MutableGraph<String> graph = GraphBuilder.undirected().build();
    graph.addNode("A");
    LayoutModel<String, Point2D> model =
        new LoadingCacheLayoutModel(graph, pointModel, 100, 100, 100);
    model.set("A", 0, 0);
    LayoutAlgorithm newLayoutAlgorithm = new StaticLayoutAlgorithm(pointModel);

    //        LayoutAlgorithmTransition transition = new LayoutAlgorithmTransition();
  }
}
