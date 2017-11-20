/*
 * Copyright (c) 2008, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.samples;

import com.google.common.graph.MutableNetwork;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.StaticLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.PointModel;
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
import edu.uci.ics.jung.visualization.layout.AWTPointModel;
import edu.uci.ics.jung.visualization.layout.SpatialQuadTreeLayoutModel;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.spatial.SpatialQuadTree;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import javax.swing.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;

/** A class that shows the minimal work necessary to load and visualize a graph. */
public class SimpleGraphSpatialSearchTest {

  private static final Logger log = LogManager.getLogger(SimpleGraphSpatialSearchTest.class);

  private static final PointModel<Point2D> POINT_MODEL = new AWTPointModel();

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static void main(String[] args) throws IOException {

    // programmatically set the log level so that the spatial grid is drawn for this demo and the SpatialGrid logging is output
    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    Configuration config = ctx.getConfiguration();
    config
        .getLoggerConfig("edu.uci.ics.jung.visualization.BasicVisualizationServer")
        .setLevel(Level.TRACE);
    config.getLoggerConfig("edu.uci.ics.jung.visualization.spatial").setLevel(Level.DEBUG);
    ctx.updateLoggers();

    JFrame jf = new JFrame();
    MutableNetwork<String, Number> g = (MutableNetwork) TestGraphs.createChainPlusIsolates(0, 100);
    Dimension viewPreferredSize = new Dimension(600, 600);
    Dimension layoutPreferredSize = new Dimension(600, 600);
    LayoutAlgorithm layoutAlgorithm =
        new StaticLayoutAlgorithm(POINT_MODEL); //, layoutPreferredSize);

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
    //    vv.getRenderContext().setVertexShapeTransformer(n -> new Ellipse2D.Double(-4, -4, 8, 8));;

    vv.scaleToLayout(scaler);
    jf.getContentPane().add(vv);
    JPanel buttons = new JPanel();
    JButton search = new JButton("search");
    buttons.add(search);
    search.addActionListener(
        e -> {
          testClosestNodes(
              vv,
              g,
              model.getLayoutModel(),
              (SpatialQuadTree<String>)
                  ((SpatialQuadTreeLayoutModel) model.getLayoutModel()).getSpatial());
        });

    jf.getContentPane().add(buttons, BorderLayout.SOUTH);
    jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    jf.pack();
    jf.setVisible(true);
  }

  public static void testClosestNodes(
      VisualizationViewer<String, String> vv,
      MutableNetwork<String, Number> graph,
      LayoutModel<String, Point2D> layoutModel,
      SpatialQuadTree<String> tree) {
    vv.getPickedVertexState().clear();
    NetworkNodeAccessor<String, Point2D> slowWay =
        new RadiusNetworkNodeAccessor<String, Point2D>(
            graph.asGraph(), new AWTPointModel(), Double.MAX_VALUE);

    // look for nodes closest to 100 random locations
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
        //        Color saved = vv.getGraphics().getColor();
        //        vv.getGraphics().setColor(Color.GREEN);
        //        vv.getGraphics().drawRect((int)x, (int)y, 5, 5);
        //        vv.getGraphics().setColor(saved);
        break;
      }
    }
  }
}
