/*
 * Copyright (c) 2008, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.samples;

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
import edu.uci.ics.jung.visualization.MultiLayerTransformer;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.spatial.Spatial;
import edu.uci.ics.jung.visualization.spatial.SpatialQuadTree;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A test that puts a lot of nodes on the screen with a visible quadtree. When the button is pushed,
 * 1000 random points are generated in order to find the closest node for each point. The search is
 * done both with the SpatialQuadTree and with the RadiusNetworkElementAccessor. If they don't find
 * the same node, the testing halts after highlighting the problem nodes along with the search
 * point.
 *
 * <p>A mouse click at a location will highlight the closest edge to the pick point.
 *
 * <p>A toggle button will turn on/off the display of the quadtree features, including the expansion
 * of the search target (red circle) in order to find the closest node.
 *
 * @author Tom Nelson
 */
public class SimpleGraphSpatialEdgeSearchTest extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(SimpleGraphSpatialEdgeSearchTest.class);

  public SimpleGraphSpatialEdgeSearchTest() {
    setLayout(new BorderLayout());

    MutableNetwork<String, Number> g = (MutableNetwork) TestGraphs.getOneComponentGraph();
    Dimension viewPreferredSize = new Dimension(600, 600);
    Dimension layoutPreferredSize = new Dimension(600, 600);
    LayoutAlgorithm layoutAlgorithm = new StaticLayoutAlgorithm();

    ScalingControl scaler = new CrossoverScalingControl();
    VisualizationModel model =
        new BaseVisualizationModel(
            g,
            layoutAlgorithm,
            new RandomLocationTransformer(600, 600, System.currentTimeMillis()),
            layoutPreferredSize);
    VisualizationViewer vv = new VisualizationViewer(model, viewPreferredSize);

    vv.getRenderContext().setNodeLabelFunction(Object::toString);
    vv.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.CNTR);

    // use a QuadTree in this demo instead of the default R-Tree
    vv.setNodeSpatial(new SpatialQuadTree(model.getLayoutModel()));

    vv.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            MultiLayerTransformer multiLayerTransformer =
                vv.getRenderContext().getMultiLayerTransformer();
            Point2D layoutPoint = multiLayerTransformer.inverseTransform(e.getX(), e.getY());
            Object edge = vv.getEdgeSpatial().getClosestElement(layoutPoint);
            if (edge != null) {
              vv.getPickedEdgeState().clear();
              vv.getPickedEdgeState().pick(edge, true);
            }
          }
        });

    JRadioButton showSpatialEffects = new JRadioButton("Show Spatial Structure");
    showSpatialEffects.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            System.err.println("TURNED ON LOGGING");
            // turn on the logging
            // programmatically set the log level so that the spatial grid is drawn for this demo
            // and the SpatialGrid logging is output
            ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) log;
            LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
            ctx.getLogger("edu.uci.ics.jung.visualization.spatial").setLevel(Level.DEBUG);
            ctx.getLogger("edu.uci.ics.jung.visualization.BasicVisualizationServer")
                .setLevel(Level.TRACE);
            ctx.getLogger("edu.uci.ics.jung.visualization.picking").setLevel(Level.TRACE);
            repaint();

          } else if (e.getStateChange() == ItemEvent.DESELECTED) {
            System.err.println("TURNED OFF LOGGING");
            // turn off the logging
            // programmatically set the log level so that the spatial grid is drawn for this demo
            // and the SpatialGrid logging is output
            ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) log;
            LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
            ctx.getLogger("edu.uci.ics.jung.visualization.spatial").setLevel(Level.INFO);
            ctx.getLogger("edu.uci.ics.jung.visualization.BasicVisualizationServer")
                .setLevel(Level.INFO);
            ctx.getLogger("edu.uci.ics.jung.visualization.picking").setLevel(Level.INFO);
            repaint();
          }
        });

    vv.scaleToLayout(scaler);
    this.add(vv);
    JPanel buttons = new JPanel();
    JButton search = new JButton("Test 1000 Searches");
    buttons.add(search);
    buttons.add(showSpatialEffects);

    search.addActionListener(
        e -> testClosestNodes(vv, g, model.getLayoutModel(), vv.getNodeSpatial()));

    this.add(buttons, BorderLayout.SOUTH);
  }

  public void testClosestNodes(
      VisualizationViewer<String, String> vv,
      MutableNetwork<String, Number> graph,
      LayoutModel<String> layoutModel,
      Spatial<String> tree) {
    vv.getPickedNodeState().clear();
    NetworkNodeAccessor<String> slowWay = new RadiusNetworkNodeAccessor<>(Double.MAX_VALUE);

    // look for nodes closest to 1000 random locations
    for (int i = 0; i < 1000; i++) {
      double x = Math.random() * layoutModel.getWidth();
      double y = Math.random() * layoutModel.getHeight();
      // use the slowWay
      String winnerOne = slowWay.getNode(layoutModel, x, y);
      // use the quadtree
      String winnerTwo = tree.getClosestElement(x, y);

      log.trace("{} and {} should be the same...", winnerOne, winnerTwo);

      if (!winnerOne.equals(winnerTwo)) {
        log.info(
            "the radius distanceSq from winnerOne {} at {} to {},{} is {}",
            winnerOne,
            layoutModel.apply(winnerOne),
            x,
            y,
            layoutModel.apply(winnerOne).distanceSquared(x, y));
        log.info(
            "the radius distanceSq from winnerTwo {} at {} to {},{} is {}",
            winnerTwo,
            layoutModel.apply(winnerTwo),
            x,
            y,
            layoutModel.apply(winnerTwo).distanceSquared(x, y));

        log.info("the cell for winnerOne {} is {}", winnerOne, tree.getContainingLeaf(winnerOne));
        log.info("the cell for winnerTwo {} is {}", winnerTwo, tree.getContainingLeaf(winnerTwo));
        log.info("the cell for the search point {},{} is {}", x, y, tree.getContainingLeafs(x, y));
        vv.getPickedNodeState().pick(winnerOne, true);
        vv.getPickedNodeState().pick(winnerTwo, true);
        graph.addNode("P");
        layoutModel.set("P", x, y);
        vv.getRenderContext().getPickedNodeState().pick("P", true);
        break;
      }
    }
  }

  public static void main(String[] args) throws IOException {

    JFrame jf = new JFrame();

    jf.getContentPane().add(new SimpleGraphSpatialEdgeSearchTest());
    jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    jf.pack();
    jf.setVisible(true);
  }
}
