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
import edu.uci.ics.jung.algorithms.layout.FRLayoutAlgorithm;
import edu.uci.ics.jung.algorithms.layout.ISOMLayoutAlgorithm;
import edu.uci.ics.jung.algorithms.layout.LayoutAlgorithm;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.samples.util.ControlHelpers;
import edu.uci.ics.jung.visualization.BaseVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.layout.*;
import edu.uci.ics.jung.visualization.picking.MultiPickedState;
import edu.uci.ics.jung.visualization.picking.PickedState;
import java.awt.*;
import java.awt.geom.Point2D;
import javax.swing.*;

/**
 * Demonstrates a single graph with 2 layouts in 2 views. They share picking, transforms, and a
 * pluggable renderer
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class TwoModelDemo extends JApplet {

  private static final DomainModel<Point2D> domainModel = new AWTDomainModel();

  /** the graph */
  Network<String, Number> graph;

  /** the visual components and renderers for the graph */
  VisualizationViewer<String, Number> vv1;

  VisualizationViewer<String, Number> vv2;

  Dimension preferredSize = new Dimension(300, 300);

  /** create an instance of a simple graph in two views with controls to demo the zoom features. */
  public TwoModelDemo() {

    // create a simple graph for the demo
    // both models will share one graph
    graph = TestGraphs.getOneComponentGraph();

    // create two layouts for the one graph, one layout for each model
    LayoutAlgorithm<String, Point2D> layoutAlgorithm1 = new FRLayoutAlgorithm<>(domainModel);
    LayoutAlgorithm<String, Point2D> layoutAlgorithm2 = new ISOMLayoutAlgorithm<>(domainModel);

    // create the two models, each with a different layout
    VisualizationModel<String, Number, Point2D> vm1 =
        new BaseVisualizationModel<>(graph, layoutAlgorithm1, preferredSize);
    VisualizationModel<String, Number, Point2D> vm2 =
        new BaseVisualizationModel<>(graph, layoutAlgorithm2, preferredSize);

    // create the two views, one for each model
    // they share the same renderer
    vv1 = new VisualizationViewer<>(vm1, preferredSize);
    vv2 = new VisualizationViewer<>(vm2, preferredSize);
    vv1.setRenderContext(vv2.getRenderContext());

    // share the model Function between the two models
    //        layoutTransformer = vv1.getLayoutTransformer();
    //        vv2.setLayoutTransformer(layoutTransformer);
    //
    //        // share the view Function between the two models
    //        vv2.setViewTransformer(vv1.getViewTransformer());

    vv2.getRenderContext()
        .setMultiLayerTransformer(vv1.getRenderContext().getMultiLayerTransformer());
    vv2.getRenderContext().getMultiLayerTransformer().addChangeListener(vv1);

    vv1.setBackground(Color.white);
    vv2.setBackground(Color.white);

    // share one PickedState between the two views
    PickedState<String> ps = new MultiPickedState<>();
    vv1.setPickedVertexState(ps);
    vv2.setPickedVertexState(ps);
    PickedState<Number> pes = new MultiPickedState<>();
    vv1.setPickedEdgeState(pes);
    vv2.setPickedEdgeState(pes);

    // set an edge paint function that will show picking for edges
    vv1.getRenderContext()
        .setEdgeDrawPaintTransformer(
            new PickableEdgePaintTransformer<>(vv1.getPickedEdgeState(), Color.black, Color.red));
    vv1.getRenderContext()
        .setVertexFillPaintTransformer(
            new PickableVertexPaintTransformer<>(
                vv1.getPickedVertexState(), Color.red, Color.yellow));
    // add default listeners for ToolTips
    vv1.setVertexToolTipTransformer(Object::toString);
    vv2.setVertexToolTipTransformer(Object::toString);

    Container content = getContentPane();
    JPanel panel = new JPanel(new GridLayout(1, 0));
    panel.add(new GraphZoomScrollPane(vv1));
    panel.add(new GraphZoomScrollPane(vv2));

    content.add(panel);

    // create a GraphMouse for each view
    final DefaultModalGraphMouse<String, Number> gm1 = new DefaultModalGraphMouse<>();

    DefaultModalGraphMouse<String, Number> gm2 = new DefaultModalGraphMouse<>();

    vv1.setGraphMouse(gm1);
    vv2.setGraphMouse(gm2);

    JPanel modePanel = new JPanel();
    modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
    gm1.getModeComboBox().addItemListener(gm2.getModeListener());
    modePanel.add(gm1.getModeComboBox());

    JPanel controls = new JPanel();
    controls.add(ControlHelpers.getZoomControls(vv1, "Zoom"));
    controls.add(modePanel);
    content.add(controls, BorderLayout.SOUTH);
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new TwoModelDemo());
    f.pack();
    f.setVisible(true);
  }
}
