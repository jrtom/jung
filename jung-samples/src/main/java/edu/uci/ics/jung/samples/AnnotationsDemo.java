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
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm;
import edu.uci.ics.jung.samples.util.ControlHelpers;
import edu.uci.ics.jung.visualization.BaseVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.annotations.AnnotatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.annotations.AnnotatingModalGraphMouse;
import edu.uci.ics.jung.visualization.annotations.AnnotationControls;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction;
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.*;
import javax.swing.*;

/**
 * Demonstrates annotation of graph elements.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class AnnotationsDemo extends JPanel {

  static final String instructions =
      "<html>"
          + "<b><h2><center>Instructions for Annotations</center></h2></b>"
          + "<p>The Annotation Controls allow you to select:"
          + "<ul>"
          + "<li>Shape"
          + "<li>Color"
          + "<li>Fill (or outline)"
          + "<li>Above or below (UPPER/LOWER) the graph display"
          + "</ul>"
          + "<p>Mouse Button one press starts a Shape,"
          + "<p>drag and release to complete."
          + "<p>Mouse Button three pops up an input dialog"
          + "<p>for text. This will create a text annotation."
          + "<p>You may use html for multi-line, etc."
          + "<p>You may even use an image tag and image url"
          + "<p>to put an image in the annotation."
          + "<p><p>"
          + "<p>To remove an annotation, shift-click on it"
          + "<p>in the Annotations mode."
          + "<p>If there is overlap, the Annotation with center"
          + "<p>closest to the mouse point will be removed.";

  JDialog helpDialog;

  /** create an instance of a simple graph in two views with controls to demo the features. */
  public AnnotationsDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    Network<String, Number> graph = TestGraphs.getOneComponentGraph();

    // the preferred sizes for the two views
    Dimension preferredSize1 = new Dimension(600, 600);

    // create one layout for the graph
    FRLayoutAlgorithm<String> layoutAlgorithm = new FRLayoutAlgorithm<>();
    layoutAlgorithm.setMaxIterations(500);

    VisualizationModel<String, Number> vm =
        new BaseVisualizationModel<>(graph, layoutAlgorithm, preferredSize1);

    // create 2 views that share the same model
    final VisualizationViewer<String, Number> vv = new VisualizationViewer<>(vm, preferredSize1);
    vv.setBackground(Color.white);
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableEdgePaintFunction<>(vv.getPickedEdgeState(), Color.black, Color.cyan));
    vv.getRenderContext()
        .setNodeFillPaintFunction(
            new PickableNodePaintFunction<>(vv.getPickedNodeState(), Color.red, Color.yellow));
    vv.getRenderContext().setNodeLabelFunction(Object::toString);
    vv.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.CNTR);

    // add default listener for ToolTips
    vv.setNodeToolTipFunction(n -> n);

    Container panel = new JPanel(new BorderLayout());

    GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
    panel.add(gzsp);

    helpDialog = new JDialog();
    helpDialog.getContentPane().add(new JLabel(instructions));

    RenderContext<String, Number> rc = vv.getRenderContext();
    AnnotatingGraphMousePlugin<String, Number> annotatingPlugin =
        new AnnotatingGraphMousePlugin<>(rc);
    // create a GraphMouse for the main view
    //
    final AnnotatingModalGraphMouse<String, Number> graphMouse =
        new AnnotatingModalGraphMouse<>(rc, annotatingPlugin);
    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());

    JComboBox<Mode> modeBox = graphMouse.getModeComboBox();
    modeBox.setSelectedItem(ModalGraphMouse.Mode.ANNOTATING);

    JButton help = new JButton("Help");
    help.addActionListener(
        e -> {
          helpDialog.pack();
          helpDialog.setVisible(true);
        });

    JPanel controls = new JPanel();
    controls.add(ControlHelpers.getZoomControls(vv, "Zoom"));

    JPanel modeControls = new JPanel();
    modeControls.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
    modeControls.add(graphMouse.getModeComboBox());
    controls.add(modeControls);

    JPanel annotationControlPanel = new JPanel();
    annotationControlPanel.setBorder(BorderFactory.createTitledBorder("Annotation Controls"));

    AnnotationControls<String, Number> annotationControls =
        new AnnotationControls<>(annotatingPlugin);

    annotationControlPanel.add(annotationControls.getAnnotationsToolBar());
    controls.add(annotationControlPanel);

    JPanel helpControls = new JPanel();
    helpControls.setBorder(BorderFactory.createTitledBorder("Help"));
    helpControls.add(help);
    controls.add(helpControls);
    add(panel);
    add(controls, BorderLayout.SOUTH);
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new AnnotationsDemo());
    f.pack();
    f.setVisible(true);
  }
}
