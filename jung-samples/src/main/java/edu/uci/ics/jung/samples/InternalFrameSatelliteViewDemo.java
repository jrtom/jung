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
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.layout.algorithms.ISOMLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.*;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction;
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction;
import java.awt.*;
import javax.swing.*;

/**
 * Similar to the SatelliteViewDemo, but using JInternalFrame.
 *
 * @author Tom Nelson
 */
public class InternalFrameSatelliteViewDemo {

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
          + "<li>Mouse1 on a Node selects the node"
          + "<li>Mouse1 elsewhere unselects all Nodes"
          + "<li>Mouse1+Shift on a Node adds/removes Node selection"
          + "<li>Mouse1+drag on a Node moves all selected Nodes"
          + "<li>Mouse1+drag elsewhere selects Nodes in a region"
          + "<li>Mouse1+Shift+drag adds selection of Nodes in a new region"
          + "<li>Mouse1+CTRL on a Node selects the node and centers the display on it"
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

    LayoutAlgorithm<String> layout = new ISOMLayoutAlgorithm<>();

    vv = new VisualizationViewer<>(graph, layout, new Dimension(600, 600));
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableEdgePaintFunction<>(vv.getPickedEdgeState(), Color.black, Color.cyan));
    vv.getRenderContext()
        .setNodeFillPaintFunction(
            new PickableNodePaintFunction<>(vv.getPickedNodeState(), Color.red, Color.yellow));

    // add my listener for ToolTips
    vv.setNodeToolTipFunction(Object::toString);
    final DefaultModalGraphMouse<String, Number> graphMouse = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(graphMouse);

    satellite = new SatelliteVisualizationViewer<>(vv, new Dimension(200, 200));
    satellite
        .getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableEdgePaintFunction<>(
                satellite.getPickedEdgeState(), Color.black, Color.cyan));
    satellite
        .getRenderContext()
        .setNodeFillPaintFunction(
            new PickableNodePaintFunction<>(
                satellite.getPickedNodeState(), Color.red, Color.yellow));

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
    vvFrame.setVisible(true); // necessary as of 1.3
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
