/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.samples;

import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.layout.DomainModel;
import edu.uci.ics.jung.algorithms.layout.ISOMLayoutAlgorithm;
import edu.uci.ics.jung.algorithms.layout.LayoutAlgorithm;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.SatelliteVisualizationViewer;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.layout.AWTDomainModel;
import java.awt.*;
import java.awt.geom.Point2D;
import javax.swing.*;

/**
 * Similar to the SatelliteViewDemo, but using JInternalFrame.
 *
 * @author Tom Nelson
 */
public class InternalFrameSatelliteViewDemo {

  private static final DomainModel<Point2D> domainModel = new AWTDomainModel();

  static final String instructions =
      "<html>"
          + "<b><h2><center>Instructions for Mouse Listeners</center></h2></b>"
          + "<p>There are two modes, Transforming and Picking."
          + "<p>The modes are selected with a toggle button."
          + "<p><p><b>Transforming Mode:</b>"
          + "<ul>"
          + "<li>Mouse1+drag pans the graph"
          + "<li>Mouse1+Shift+drag rotates the graph"
          + "<li>Mouse1+CTRL(or Command)+drag shears the graph"
          + "</ul>"
          + "<b>Picking Mode:</b>"
          + "<ul>"
          + "<li>Mouse1 on a Vertex selects the vertex"
          + "<li>Mouse1 elsewhere unselects all Vertices"
          + "<li>Mouse1+Shift on a Vertex adds/removes Vertex selection"
          + "<li>Mouse1+drag on a Vertex moves all selected Vertices"
          + "<li>Mouse1+drag elsewhere selects Vertices in a region"
          + "<li>Mouse1+Shift+drag adds selection of Vertices in a new region"
          + "<li>Mouse1+CTRL on a Vertex selects the vertex and centers the display on it"
          + "</ul>"
          + "<b>Both Modes:</b>"
          + "<ul>"
          + "<li>Mousewheel scales the layout &gt; 1 and scales the view &lt; 1";

  /** the graph */
  Network<String, Number> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Number> vv;

  VisualizationViewer<String, Number> satellite;

  JInternalFrame dialog;

  JDesktopPane desktop;

  /** create an instance of a simple graph with controls to demo the zoom features. */
  public InternalFrameSatelliteViewDemo() {

    // create a simple graph for the demo
    graph = TestGraphs.getOneComponentGraph();

    LayoutAlgorithm<String, Point2D> layout = new ISOMLayoutAlgorithm<>(domainModel);

    vv = new VisualizationViewer<>(graph, layout, new Dimension(600, 600));
    vv.getRenderContext()
        .setEdgeDrawPaintTransformer(
            new PickableEdgePaintTransformer<>(vv.getPickedEdgeState(), Color.black, Color.cyan));
    vv.getRenderContext()
        .setVertexFillPaintTransformer(
            new PickableVertexPaintTransformer<>(
                vv.getPickedVertexState(), Color.red, Color.yellow));

    // add my listener for ToolTips
    vv.setVertexToolTipTransformer(Object::toString);
    final DefaultModalGraphMouse<String, Number> graphMouse = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(graphMouse);

    satellite = new SatelliteVisualizationViewer<>(vv, new Dimension(200, 200));
    satellite
        .getRenderContext()
        .setEdgeDrawPaintTransformer(
            new PickableEdgePaintTransformer<>(
                satellite.getPickedEdgeState(), Color.black, Color.cyan));
    satellite
        .getRenderContext()
        .setVertexFillPaintTransformer(
            new PickableVertexPaintTransformer<>(
                satellite.getPickedVertexState(), Color.red, Color.yellow));

    ScalingControl satelliteScaler = new CrossoverScalingControl();
    satellite.scaleToLayout(satelliteScaler);

    JFrame frame = new JFrame();
    desktop = new JDesktopPane();
    Container content = frame.getContentPane();
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(desktop);
    content.add(panel);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    JInternalFrame vvFrame = new JInternalFrame();
    vvFrame.getContentPane().add(vv);
    vvFrame.pack();
    vvFrame.setVisible(true); //necessary as of 1.3
    desktop.add(vvFrame);
    try {
      vvFrame.setSelected(true);
    } catch (java.beans.PropertyVetoException e) {
    }

    dialog = new JInternalFrame();
    desktop.add(dialog);
    content = dialog.getContentPane();

    final ScalingControl scaler = new CrossoverScalingControl();

    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));
    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    JButton dismiss = new JButton("Dismiss");
    dismiss.addActionListener(e -> dialog.setVisible(false));

    JButton help = new JButton("Help");
    help.addActionListener(
        e ->
            JOptionPane.showInternalMessageDialog(
                dialog, instructions, "Instructions", JOptionPane.PLAIN_MESSAGE));
    JPanel controls = new JPanel(new GridLayout(2, 2));
    controls.add(plus);
    controls.add(minus);
    controls.add(dismiss);
    controls.add(help);
    content.add(satellite);
    content.add(controls, BorderLayout.SOUTH);

    JButton zoomer = new JButton("Show Satellite View");
    zoomer.addActionListener(
        e -> {
          dialog.pack();
          dialog.setLocation(desktop.getWidth() - dialog.getWidth(), 0);
          dialog.show();
          try {
            dialog.setSelected(true);
          } catch (java.beans.PropertyVetoException ex) {
          }
        });

    JComboBox<Mode> modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(((ModalGraphMouse) satellite.getGraphMouse()).getModeListener());
    JPanel p = new JPanel();
    p.add(zoomer);
    p.add(modeBox);

    frame.getContentPane().add(p, BorderLayout.SOUTH);
    frame.setSize(800, 800);
    frame.setVisible(true);
  }

  public static void main(String[] args) {
    new InternalFrameSatelliteViewDemo();
  }
}
