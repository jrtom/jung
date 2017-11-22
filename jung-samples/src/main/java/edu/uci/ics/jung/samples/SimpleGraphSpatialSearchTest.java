/*
 * Copyright (c) 2008, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.samples;

import static edu.uci.ics.jung.visualization.layout.AWT.POINT_MODEL;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.google.common.graph.MutableNetwork;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.StaticLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.util.NetworkNodeAccessor;
import edu.uci.ics.jung.layout.util.RadiusNetworkNodeAccessor;
import edu.uci.ics.jung.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.visualization.BaseVisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.SpatialQuadTreeLayoutModel;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.spatial.SpatialQuadTree;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A test that puts a lot of nodes on the screen with a visible quadtree. When the button is pushed,
 * 1000 random points are generated in order to find the closest node for each point. The search is
 * done both with the SpatialQuadTree and with the RadiusNetworkElementAccessor. If they don't find
 * the same node, the testing halts after highlighting the problem nodes along with the search
 * point.
 *
 * @author Tom Nelson
 */
public class SimpleGraphSpatialSearchTest extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(SimpleGraphSpatialSearchTest.class);

  public SimpleGraphSpatialSearchTest() {
    setLayout(new BorderLayout());

    MutableNetwork<String, Number> g = (MutableNetwork) TestGraphs.createChainPlusIsolates(0, 100);
    Dimension viewPreferredSize = new Dimension(600, 600);
    Dimension layoutPreferredSize = new Dimension(600, 600);
    LayoutAlgorithm layoutAlgorithm = new StaticLayoutAlgorithm(POINT_MODEL);

    ScalingControl scaler = new CrossoverScalingControl();
    VisualizationModel model =
        new BaseVisualizationModel(
            g,
            layoutAlgorithm,
            new RandomLocationTransformer(POINT_MODEL, 600, 600, 0, System.currentTimeMillis()),
            layoutPreferredSize);
    VisualizationViewer vv = new VisualizationViewer(model, viewPreferredSize);
    final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
    vv.setGraphMouse(graphMouse);
    vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
    vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
    vv.addKeyListener(graphMouse.getModeKeyListener());
    vv.setToolTipText("<html><center>Type 'p' for Pick mode<p>Type 't' for Transform mode");

    vv.scaleToLayout(scaler);
    this.add(vv);
    JPanel buttons = new JPanel();
    JButton search = new JButton("search");
    buttons.add(search);
    search.addActionListener(
        e ->
            testClosestNodes(
                vv,
                g,
                model.getLayoutModel(),
                (SpatialQuadTree<String>)
                    ((SpatialQuadTreeLayoutModel) model.getLayoutModel()).getSpatial()));

    this.add(buttons, BorderLayout.SOUTH);
  }

  public void testClosestNodes(
      VisualizationViewer<String, String> vv,
      MutableNetwork<String, Number> graph,
      LayoutModel<String, Point2D> layoutModel,
      SpatialQuadTree<String> tree) {
    vv.getPickedVertexState().clear();
    NetworkNodeAccessor<String, Point2D> slowWay =
        new RadiusNetworkNodeAccessor<>(graph.asGraph(), POINT_MODEL, Double.MAX_VALUE);

    // look for nodes closest to 1000 random locations
    for (int i = 0; i < 1000; i++) {
      double x = Math.random() * layoutModel.getWidth();
      double y = Math.random() * layoutModel.getHeight();
      // use the slowWay
      String winnerOne = slowWay.getNode(layoutModel, x, y, 0);
      // use the quadtree
      String winnerTwo = tree.getClosestNode(x, y);

      log.debug("{} and {} should be the same...", winnerOne, winnerTwo);

      if (!winnerOne.equals(winnerTwo)) {
        log.info(
            "the radius distanceSq from winnerOne {} at {} to {},{} is {}",
            winnerOne,
            layoutModel.apply(winnerOne),
            x,
            y,
            layoutModel.apply(winnerOne).distanceSq(x, y));
        log.info(
            "the radius distanceSq from winnerTwo {} at {} to {},{} is {}",
            winnerTwo,
            layoutModel.apply(winnerTwo),
            x,
            y,
            layoutModel.apply(winnerTwo).distanceSq(x, y));

        log.info(
            "the cell for winnerOne {} is {}",
            winnerOne,
            tree.getContainingQuadTreeLeaf(winnerOne));
        log.info(
            "the cell for winnerTwo {} is {}",
            winnerTwo,
            tree.getContainingQuadTreeLeaf(winnerTwo));
        log.info(
            "the cell for the search point {},{} is {}",
            x,
            y,
            tree.getContainingQuadTreeLeaf(x, y));
        vv.getPickedVertexState().pick(winnerOne, true);
        vv.getPickedVertexState().pick(winnerTwo, true);
        graph.addNode("P");
        layoutModel.set("P", x, y);
        vv.getRenderContext().getPickedVertexState().pick("P", true);
        break;
      }
    }
  }

  public static void main(String[] args) throws IOException {

    // programmatically set the log level so that the spatial grid is drawn for this demo and the SpatialGrid logging is output
    // programmatically set the log level so that the spatial grid is drawn for this demo and the SpatialGrid logging is output
    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) log;
    LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
    ctx.getLogger("edu.uci.ics.jung.visualization.spatial").setLevel(Level.DEBUG);
    ctx.getLogger("edu.uci.ics.jung.visualization.BasicVisualizationServer").setLevel(Level.TRACE);

    JFrame jf = new JFrame();

    jf.getContentPane().add(new SimpleGraphSpatialSearchTest());
    jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    jf.pack();
    jf.setVisible(true);
  }
}
