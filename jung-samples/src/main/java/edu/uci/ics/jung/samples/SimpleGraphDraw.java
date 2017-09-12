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
import edu.uci.ics.jung.io.PajekNetReader;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.io.IOException;
import java.util.function.Supplier;
import javax.swing.JFrame;

/** A class that shows the minimal work necessary to load and visualize a graph. */
public class SimpleGraphDraw {

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static void main(String[] args) throws IOException {
    JFrame jf = new JFrame();
    Network g = getGraph();
    VisualizationViewer vv = new VisualizationViewer(g, new FRLayout(g.asGraph()));
    jf.getContentPane().add(vv);
    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jf.pack();
    jf.setVisible(true);
  }

  /**
   * Generates a graph: in this case, reads it from the file "samples/datasetsgraph/simple.net"
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

    pnr.load("src/main/resources/datasets/simple.net", g);
    return g;
  }
}
