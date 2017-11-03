/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 *
 */
package edu.uci.ics.jung.samples;

/** */
import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.layout3d.*;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization3d.VisualizationViewer;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

/** @author Tom Nelson - tomnelson@dev.java.net */
public class GraphDemo extends JPanel {

  enum Layouts {
    SPRING,
    FRLAYOUT,
    KK,
    ISOM,
    SPHERE;
  }

  enum Graphs {
    DEMO,
    ONECOMPONENT,
    CHAIN,
    TEST,
    ISOLATES,
    DAG;
  }

  public GraphDemo() {
    super(new BorderLayout());

    JPanel vvHolder = new JPanel();
    vvHolder.setLayout(new GridLayout(1, 1));

    JComboBox<Layouts> layoutNameComboBox = new JComboBox(Layouts.values());
    JComboBox<Graphs> graphNameComboBox = new JComboBox(Graphs.values());
    JPanel flowPanel = new JPanel();
    flowPanel.add(layoutNameComboBox);
    flowPanel.add(graphNameComboBox);

    layoutNameComboBox.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            addVisualization(vvHolder, graphNameComboBox, layoutNameComboBox);
          }
        });

    graphNameComboBox.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            addVisualization(vvHolder, graphNameComboBox, layoutNameComboBox);
          }
        });

    add(vvHolder);
    addVisualization(vvHolder, graphNameComboBox, layoutNameComboBox);
    add(flowPanel, BorderLayout.SOUTH);
  }

  private void addVisualization(
      JPanel parent, JComboBox graphNameComboBox, JComboBox layoutNameComboBox) {

    Graphs graph = (Graphs) graphNameComboBox.getSelectedItem();
    Network network;
    VisualizationViewer vv = new VisualizationViewer();
    vv.getRenderContext().setVertexStringer(new ToStringLabeller());

    switch (graph) {
      case DEMO:
        network = TestGraphs.getDemoGraph();
        break;
      case ONECOMPONENT:
        network = TestGraphs.getOneComponentGraph();
        break;
      case DAG:
        network = TestGraphs.createDirectedAcyclicGraph(4, 10, 0.3);
        break;
      case TEST:
        network = TestGraphs.createTestGraph(false);
        break;
      case CHAIN:
        network = TestGraphs.createChainPlusIsolates(20, 4);
        break;
      case ISOLATES:
        network = TestGraphs.createChainPlusIsolates(0, 200);
        break;
      default:
        network = TestGraphs.getDemoGraph();
    }
    Layouts item = (Layouts) layoutNameComboBox.getSelectedItem();
    Layout layout;
    switch (item) {
      case SPRING:
        layout = new SpringLayout(network);
        break;
      case SPHERE:
        layout = new SphereLayout(network);
        break;
      case FRLAYOUT:
        layout = new FRLayout(network);
        break;
      case KK:
        layout = new KKLayout(network);
        break;
      case ISOM:
        layout = new ISOMLayout(network);
        break;

      default:
        layout = new SpringLayout(network);
    }
    vv.setGraphLayout(layout);
    parent.removeAll();
    parent.add(vv);
    vv.revalidate();
  }

  public static void main(String argv[]) {
    final GraphDemo demo = new GraphDemo();
    JFrame f = new JFrame();
    f.add(demo);
    f.setSize(600, 600);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setVisible(true);
  }
}
