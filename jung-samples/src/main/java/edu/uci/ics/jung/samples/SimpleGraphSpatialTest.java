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
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.io.PajekNetReader;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.function.Supplier;
import javax.swing.*;

/** A class that shows the minimal work necessary to load and visualize a graph. */
public class SimpleGraphSpatialTest {

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static void main(String[] args) throws IOException {
    JFrame jf = new JFrame();
    Network g = getGraph();
    Dimension viewPreferredSize = new Dimension(300, 300);
    Dimension layoutPreferredSize = new Dimension(400, 400);
    Layout layout = new FRLayout(g.asGraph(), layoutPreferredSize);

    ScalingControl scaler = new CrossoverScalingControl();
    VisualizationViewer vv = new VisualizationViewer(g, layout, viewPreferredSize);
    final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
    vv.setGraphMouse(graphMouse);

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
