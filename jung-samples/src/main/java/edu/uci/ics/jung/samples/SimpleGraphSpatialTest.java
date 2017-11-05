/*
 * Copyright (c) 2008, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.samples;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.algorithms.layout.DomainModel;
import edu.uci.ics.jung.algorithms.layout.FRLayoutAlgorithm;
import edu.uci.ics.jung.algorithms.layout.LayoutAlgorithm;
import edu.uci.ics.jung.io.PajekNetReader;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.AWTDomainModel;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.function.Supplier;
import javax.swing.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;

/** A class that shows the minimal work necessary to load and visualize a graph. */
public class SimpleGraphSpatialTest {

  Logger log = LogManager.getLogger(SimpleGraphSpatialTest.class);

  private static final DomainModel<Point2D> domainModel = new AWTDomainModel();

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
    Network g = getGraph();
    Dimension viewPreferredSize = new Dimension(600, 600);
    Dimension layoutPreferredSize = new Dimension(600, 600);
    LayoutAlgorithm layoutAlgorithm = new FRLayoutAlgorithm(domainModel); //, layoutPreferredSize);

    ScalingControl scaler = new CrossoverScalingControl();
    VisualizationViewer vv =
        new VisualizationViewer(g, layoutAlgorithm, layoutPreferredSize, viewPreferredSize);
    final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
    vv.setGraphMouse(graphMouse);
    vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
    vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
    vv.addKeyListener(graphMouse.getModeKeyListener());
    vv.setToolTipText("<html><center>Type 'p' for Pick mode<p>Type 't' for Transform mode");

    vv.scaleToLayout(scaler);
    jf.getContentPane().add(vv);

    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jf.pack();
    jf.setVisible(true);
  }

  /**
   * Generates a graph: in this case, reads it from the file (in the classpath):
   * "datasets/simple.net"
   *
   * @return A sample undirected graph
   * @throws IOException if there is an error in reading the file
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public static Network getGraph() throws IOException {
    PajekNetReader pnr =
        new PajekNetReader(
            new Supplier() {
              public Object get() {
                return new Object();
              }
            });
    MutableNetwork g = NetworkBuilder.undirected().build();
    Reader reader =
        new InputStreamReader(
            SimpleGraphSpatialTest.class.getResourceAsStream("/datasets/simple.net"));
    pnr.load(reader, g);
    return g;
  }
}
