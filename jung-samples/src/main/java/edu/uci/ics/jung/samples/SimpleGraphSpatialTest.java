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
import com.google.common.graph.Network;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.visualization.BaseVisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A test program to show the SpatialLayout structure and allow users to manipulate the graph ('p'
 * for pick mode, 't' for transform mode) and watch the Spatial structure update
 *
 * @author Tom Nelson
 */
public class SimpleGraphSpatialTest extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(SimpleGraphSpatialTest.class);

  public SimpleGraphSpatialTest() {
    setLayout(new BorderLayout());

    Network g = TestGraphs.getOneComponentGraph();

    Dimension viewPreferredSize = new Dimension(600, 600);
    Dimension layoutPreferredSize = new Dimension(600, 600);
    LayoutAlgorithm layoutAlgorithm = new FRLayoutAlgorithm();

    ScalingControl scaler = new CrossoverScalingControl();
    VisualizationModel model =
        new BaseVisualizationModel(
            g,
            layoutAlgorithm,
            new RandomLocationTransformer(600, 600, System.currentTimeMillis()),
            layoutPreferredSize);
    VisualizationViewer vv = new VisualizationViewer(model, viewPreferredSize);
    final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
    vv.setGraphMouse(graphMouse);
    vv.getRenderContext().setNodeLabelFunction(Object::toString);
    vv.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.CNTR);
    vv.addKeyListener(graphMouse.getModeKeyListener());
    vv.setToolTipText("<html><center>Type 'p' for Pick mode<p>Type 't' for Transform mode");
    vv.setForeground(Color.white);
    vv.scaleToLayout(scaler);
    this.add(vv);
  }

  public static void main(String[] args) throws IOException {

    // programmatically set the log level so that the spatial grid is drawn for this demo and the
    // SpatialGrid logging is output
    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) log;
    LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
    ctx.getLogger("edu.uci.ics.jung.visualization.spatial").setLevel(Level.DEBUG);
    ctx.getLogger("edu.uci.ics.jung.visualization.BasicVisualizationServer").setLevel(Level.TRACE);
    ctx.getLogger("edu.uci.ics.jung.visualization.picking").setLevel(Level.TRACE);

    JFrame jf = new JFrame();

    jf.getContentPane().add(new SimpleGraphSpatialTest());
    jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    jf.pack();
    jf.setVisible(true);
  }
}
