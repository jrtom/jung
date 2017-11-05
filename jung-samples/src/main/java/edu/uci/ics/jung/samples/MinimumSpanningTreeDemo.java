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
import edu.uci.ics.jung.algorithms.shortestpath.MinimumSpanningTree;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.visualization.BaseVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.layout.*;
import edu.uci.ics.jung.visualization.picking.MultiPickedState;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.*;
import java.awt.geom.Point2D;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates a single graph with 3 layouts in 3 views. The first view is an undirected graph
 * using KKLayout The second view show a TreeLayout view of a MinimumSpanningTree of the first
 * graph. The third view shows the complete graph of the first view, using the layout positions of
 * the MinimumSpanningTree tree view.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class MinimumSpanningTreeDemo extends JApplet {

  private static final Logger log = LoggerFactory.getLogger(MinimumSpanningTreeDemo.class);

  private static final DomainModel<Point2D> domainModel = new AWTDomainModel();

  /** the graph */
  Network<String, Number> graph;

  Network<String, Number> tree;

  /** the visual components and renderers for the graph */
  VisualizationViewer<String, Number> vv0;

  VisualizationViewer<String, Number> vv1;
  VisualizationViewer<String, Number> vv2;

  Dimension preferredSize = new Dimension(300, 300);
  Dimension preferredSizeRect = new Dimension(800, 250);

  /** create an instance of a simple graph in two views with controls to demo the zoom features. */
  public MinimumSpanningTreeDemo() {

    // create a simple graph for the demo
    // both models will share one graph
    graph = TestGraphs.getDemoGraph();

    tree = MinimumSpanningTree.extractFrom(graph, e -> 1.0);

    LayoutAlgorithm<String, Point2D> layout0 = new KKLayoutAlgorithm<>(domainModel);
    LayoutAlgorithm<String, Point2D> layout1 = new TreeLayoutAlgorithm<>(domainModel);
    LayoutAlgorithm<String, Point2D> layout2 = new StaticLayoutAlgorithm<>(domainModel);

    // create the two models, each with a different layout
    VisualizationModel<String, Number, Point2D> vm0 =
        new BaseVisualizationModel<>(graph, layout0, preferredSize);
    VisualizationModel<String, Number, Point2D> vm1 =
        new BaseVisualizationModel<>(tree, layout1, preferredSizeRect);
    // initializer is the layout model for vm1
    // and the size is also set to the same size required for the Tree in layout1
    VisualizationModel<String, Number, Point2D> vm2 =
        new BaseVisualizationModel<>(graph, layout2, vm1.getLayoutModel(), vm1.getLayoutSize());

    // create the two views, one for each model
    // they share the same renderer
    vv0 = new VisualizationViewer<>(vm0, preferredSize);
    vv1 = new VisualizationViewer<>(vm1, preferredSizeRect);
    vv2 = new VisualizationViewer<>(vm2, preferredSizeRect);

    vv1.getRenderContext()
        .setMultiLayerTransformer(vv0.getRenderContext().getMultiLayerTransformer());
    vv2.getRenderContext()
        .setMultiLayerTransformer(vv0.getRenderContext().getMultiLayerTransformer());

    vv1.getRenderContext().setEdgeShapeTransformer(EdgeShape.line());

    vv0.addChangeListener(vv1);
    vv1.addChangeListener(vv2);

    vv0.getRenderContext().setVertexLabelTransformer(Object::toString);
    vv2.getRenderContext().setVertexLabelTransformer(Object::toString);

    Color back = Color.decode("0xffffbb");
    vv0.setBackground(back);
    vv1.setBackground(back);
    vv2.setBackground(back);

    vv0.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
    vv0.setForeground(Color.darkGray);
    vv1.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
    vv1.setForeground(Color.darkGray);
    vv2.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
    vv2.setForeground(Color.darkGray);

    // share one PickedState between the two views
    PickedState<String> ps = new MultiPickedState<>();
    vv0.setPickedVertexState(ps);
    vv1.setPickedVertexState(ps);
    vv2.setPickedVertexState(ps);

    PickedState<Number> pes = new MultiPickedState<>();
    vv0.setPickedEdgeState(pes);
    vv1.setPickedEdgeState(pes);
    vv2.setPickedEdgeState(pes);

    // set an edge paint function that will show picking for edges
    vv0.getRenderContext()
        .setEdgeDrawPaintTransformer(
            new PickableEdgePaintTransformer<>(vv0.getPickedEdgeState(), Color.black, Color.red));
    vv0.getRenderContext()
        .setVertexFillPaintTransformer(
            new PickableVertexPaintTransformer<>(
                vv0.getPickedVertexState(), Color.red, Color.yellow));
    vv1.getRenderContext()
        .setEdgeDrawPaintTransformer(
            new PickableEdgePaintTransformer<>(vv1.getPickedEdgeState(), Color.black, Color.red));
    vv1.getRenderContext()
        .setVertexFillPaintTransformer(
            new PickableVertexPaintTransformer<>(
                vv1.getPickedVertexState(), Color.red, Color.yellow));

    // add default listeners for ToolTips
    vv0.setVertexToolTipTransformer(Object::toString);
    vv1.setVertexToolTipTransformer(Object::toString);
    vv2.setVertexToolTipTransformer(Object::toString);

    vv0.setLayout(new BorderLayout());
    vv1.setLayout(new BorderLayout());
    vv2.setLayout(new BorderLayout());

    Font font = vv0.getFont().deriveFont(Font.BOLD, 16);
    JLabel vv0Label = new JLabel("<html>Original Network<p>using KKLayout");
    vv0Label.setFont(font);
    JLabel vv1Label = new JLabel("Minimum Spanning Trees");
    vv1Label.setFont(font);
    JLabel vv2Label = new JLabel("Original Graph using TreeLayout");
    vv2Label.setFont(font);
    JPanel flow0 = new JPanel();
    flow0.setOpaque(false);
    JPanel flow1 = new JPanel();
    flow1.setOpaque(false);
    JPanel flow2 = new JPanel();
    flow2.setOpaque(false);
    flow0.add(vv0Label);
    flow1.add(vv1Label);
    flow2.add(vv2Label);
    vv0.add(flow0, BorderLayout.NORTH);
    vv1.add(flow1, BorderLayout.NORTH);
    vv2.add(flow2, BorderLayout.NORTH);

    Container content = getContentPane();
    JPanel grid = new JPanel(new GridLayout(0, 1));
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(new GraphZoomScrollPane(vv0), BorderLayout.WEST);
    grid.add(new GraphZoomScrollPane(vv1));
    grid.add(new GraphZoomScrollPane(vv2));
    panel.add(grid);

    content.add(panel);

    // create a GraphMouse for each view
    DefaultModalGraphMouse<String, Number> gm0 = new DefaultModalGraphMouse<>();
    DefaultModalGraphMouse<String, Number> gm1 = new DefaultModalGraphMouse<>();
    DefaultModalGraphMouse<String, Number> gm2 = new DefaultModalGraphMouse<>();

    vv0.setGraphMouse(gm0);
    vv1.setGraphMouse(gm1);
    vv2.setGraphMouse(gm2);

    // create zoom buttons for scaling the Function that is
    // shared between the two models.
    final ScalingControl scaler = new CrossoverScalingControl();
    vv0.scaleToLayout(scaler);

    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv1, 1.1f, vv1.getCenter()));

    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv1, 1 / 1.1f, vv1.getCenter()));

    JPanel zoomPanel = new JPanel(new GridLayout(1, 2));
    zoomPanel.setBorder(BorderFactory.createTitledBorder("Zoom"));

    JPanel modePanel = new JPanel();
    modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
    gm1.getModeComboBox().addItemListener(gm2.getModeListener());
    gm1.getModeComboBox().addItemListener(gm0.getModeListener());
    modePanel.add(gm1.getModeComboBox());

    JPanel controls = new JPanel();
    zoomPanel.add(plus);
    zoomPanel.add(minus);
    controls.add(zoomPanel);
    controls.add(modePanel);
    content.add(controls, BorderLayout.SOUTH);
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new MinimumSpanningTreeDemo());
    f.pack();
    f.setVisible(true);
  }
}
