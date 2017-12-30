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
import edu.uci.ics.jung.visualization.BaseVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AnimatedPickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.LayoutScalingControl;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.RotatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ShearingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ViewScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction;
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction;
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor;
import edu.uci.ics.jung.visualization.picking.MultiPickedState;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.picking.ShapePickSupport;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.geom.Rectangle2D;
import javax.swing.*;

/**
 * Demonstrates 3 views of one graph in one model with one layout. Each view uses a different
 * scaling graph mouse.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class MultiViewDemo extends JPanel {

  /** the graph */
  Network<String, Number> graph;

  /** the visual components and renderers for the graph */
  VisualizationViewer<String, Number> vv1;

  VisualizationViewer<String, Number> vv2;
  VisualizationViewer<String, Number> vv3;

  Dimension preferredSize = new Dimension(300, 300);

  final String messageOne =
      "The mouse wheel will scale the model's layout when activated"
          + " in View 1. Since all three views share the same layout Function, all three views will"
          + " show the same scaling of the layout.";

  final String messageTwo =
      "The mouse wheel will scale the view when activated in"
          + " View 2. Since all three views share the same view Function, all three views will be affected.";

  final String messageThree =
      "   The mouse wheel uses a 'crossover' feature in View 3."
          + " When the combined layout and view scale is greater than '1', the model's layout will be scaled."
          + " Since all three views share the same layout Function, all three views will show the same "
          + " scaling of the layout.\n   When the combined scale is less than '1', the scaling function"
          + " crosses over to the view, and then, since all three views share the same view Function,"
          + " all three views will show the same scaling.";

  JTextArea textArea;
  JScrollPane scrollPane;

  /** create an instance of a simple graph in two views with controls to demo the zoom features. */
  public MultiViewDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = TestGraphs.getOneComponentGraph();

    // create one layout for the graph
    FRLayoutAlgorithm<String> layoutAlgorithm = new FRLayoutAlgorithm<>();
    layoutAlgorithm.setMaxIterations(1000);

    // create one model that all 3 views will share
    VisualizationModel<String, Number> visualizationModel =
        new BaseVisualizationModel<>(graph, layoutAlgorithm, preferredSize);

    // create 3 views that share the same model
    vv1 = new VisualizationViewer<>(visualizationModel, preferredSize);
    vv2 = new VisualizationViewer<>(visualizationModel, preferredSize);
    vv3 = new VisualizationViewer<>(visualizationModel, preferredSize);

    vv1.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv2.getRenderContext().setNodeShapeFunction(n -> new Rectangle2D.Float(-6, -6, 12, 12));

    vv2.getRenderContext().setEdgeShapeFunction(EdgeShape.quadCurve());

    vv3.getRenderContext().setEdgeShapeFunction(EdgeShape.cubicCurve());

    vv2.getRenderContext()
        .setMultiLayerTransformer(vv1.getRenderContext().getMultiLayerTransformer());
    vv3.getRenderContext()
        .setMultiLayerTransformer(vv1.getRenderContext().getMultiLayerTransformer());

    vv1.getRenderContext().getMultiLayerTransformer().addChangeListener(vv1);
    vv2.getRenderContext().getMultiLayerTransformer().addChangeListener(vv2);
    vv3.getRenderContext().getMultiLayerTransformer().addChangeListener(vv3);

    vv1.setBackground(Color.white);
    vv2.setBackground(Color.white);
    vv3.setBackground(Color.white);

    // create one pick support for all 3 views to share
    NetworkElementAccessor<String, Number> pickSupport = new ShapePickSupport<>(vv1);
    vv1.setPickSupport(pickSupport);
    vv2.setPickSupport(pickSupport);
    vv3.setPickSupport(pickSupport);

    // create one picked state for all 3 views to share
    PickedState<Number> pes = new MultiPickedState<>();
    PickedState<String> pvs = new MultiPickedState<>();
    vv1.setPickedNodeState(pvs);
    vv2.setPickedNodeState(pvs);
    vv3.setPickedNodeState(pvs);
    vv1.setPickedEdgeState(pes);
    vv2.setPickedEdgeState(pes);
    vv3.setPickedEdgeState(pes);

    // set an edge paint function that shows picked edges
    vv1.getRenderContext()
        .setEdgeDrawPaintFunction(new PickableEdgePaintFunction<>(pes, Color.black, Color.red));
    vv2.getRenderContext()
        .setEdgeDrawPaintFunction(new PickableEdgePaintFunction<>(pes, Color.black, Color.red));
    vv3.getRenderContext()
        .setEdgeDrawPaintFunction(new PickableEdgePaintFunction<>(pes, Color.black, Color.red));
    vv1.getRenderContext()
        .setNodeFillPaintFunction(new PickableNodePaintFunction<>(pvs, Color.red, Color.yellow));
    vv2.getRenderContext()
        .setNodeFillPaintFunction(new PickableNodePaintFunction<>(pvs, Color.blue, Color.cyan));
    vv3.getRenderContext()
        .setNodeFillPaintFunction(new PickableNodePaintFunction<>(pvs, Color.red, Color.yellow));

    // add default listener for ToolTips
    vv1.setNodeToolTipFunction(Object::toString);
    vv2.setNodeToolTipFunction(Object::toString);
    vv3.setNodeToolTipFunction(Object::toString);

    JPanel panel = new JPanel(new GridLayout(1, 0));

    final JPanel p1 = new JPanel(new BorderLayout());
    final JPanel p2 = new JPanel(new BorderLayout());
    final JPanel p3 = new JPanel(new BorderLayout());

    p1.add(new GraphZoomScrollPane(vv1));
    p2.add(new GraphZoomScrollPane(vv2));
    p3.add(new GraphZoomScrollPane(vv3));

    JButton h1 = new JButton("?");
    h1.addActionListener(
        e -> {
          textArea.setText(messageOne);
          JOptionPane.showMessageDialog(p1, scrollPane, "View 1", JOptionPane.PLAIN_MESSAGE);
        });

    JButton h2 = new JButton("?");
    h2.addActionListener(
        e -> {
          textArea.setText(messageTwo);
          JOptionPane.showMessageDialog(p2, scrollPane, "View 2", JOptionPane.PLAIN_MESSAGE);
        });

    JButton h3 = new JButton("?");
    h3.addActionListener(
        e -> {
          textArea.setText(messageThree);
          textArea.setCaretPosition(0);
          JOptionPane.showMessageDialog(p3, scrollPane, "View 3", JOptionPane.PLAIN_MESSAGE);
        });

    // create a GraphMouse for each view
    // each one has a different scaling plugin
    DefaultModalGraphMouse<String, Number> gm1 =
        new DefaultModalGraphMouse<String, Number>() {
          protected void loadPlugins() {
            pickingPlugin = new PickingGraphMousePlugin<String, Number>();
            animatedPickingPlugin = new AnimatedPickingGraphMousePlugin<String, Number>();
            translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
            scalingPlugin = new ScalingGraphMousePlugin(new LayoutScalingControl(), 0);
            rotatingPlugin = new RotatingGraphMousePlugin();
            shearingPlugin = new ShearingGraphMousePlugin();

            add(scalingPlugin);
            setMode(Mode.TRANSFORMING);
          }
        };

    DefaultModalGraphMouse<String, Number> gm2 =
        new DefaultModalGraphMouse<String, Number>() {
          protected void loadPlugins() {
            pickingPlugin = new PickingGraphMousePlugin<String, Number>();
            animatedPickingPlugin = new AnimatedPickingGraphMousePlugin<String, Number>();
            translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
            scalingPlugin = new ScalingGraphMousePlugin(new ViewScalingControl(), 0);
            rotatingPlugin = new RotatingGraphMousePlugin();
            shearingPlugin = new ShearingGraphMousePlugin();

            add(scalingPlugin);
            setMode(Mode.TRANSFORMING);
          }
        };

    DefaultModalGraphMouse<String, Number> gm3 = new DefaultModalGraphMouse<String, Number>() {};

    vv1.setGraphMouse(gm1);
    vv2.setGraphMouse(gm2);
    vv3.setGraphMouse(gm3);

    vv1.setToolTipText("<html><center>MouseWheel Scales Layout</center></html>");
    vv2.setToolTipText("<html><center>MouseWheel Scales View</center></html>");
    vv3.setToolTipText(
        "<html><center>MouseWheel Scales Layout and<p>crosses over to view<p>ctrl+MouseWheel scales view</center></html>");

    vv1.addPostRenderPaintable(new BannerLabel(vv1, "View 1"));
    vv2.addPostRenderPaintable(new BannerLabel(vv2, "View 2"));
    vv3.addPostRenderPaintable(new BannerLabel(vv3, "View 3"));

    textArea = new JTextArea(6, 30);
    scrollPane =
        new JScrollPane(
            textArea,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setEditable(false);

    JPanel flow = new JPanel();
    flow.add(h1);
    flow.add(gm1.getModeComboBox());
    p1.add(flow, BorderLayout.SOUTH);
    flow = new JPanel();
    flow.add(h2);
    flow.add(gm2.getModeComboBox());
    p2.add(flow, BorderLayout.SOUTH);
    flow = new JPanel();
    flow.add(h3);
    flow.add(gm3.getModeComboBox());
    p3.add(flow, BorderLayout.SOUTH);

    panel.add(p1);
    panel.add(p2);
    panel.add(p3);
    add(panel);
  }

  class BannerLabel implements VisualizationViewer.Paintable {
    int x;
    int y;
    Font font;
    FontMetrics metrics;
    int swidth;
    int sheight;
    String str;
    VisualizationViewer<String, Number> vv;

    public BannerLabel(VisualizationViewer<String, Number> vv, String label) {
      this.vv = vv;
      this.str = label;
    }

    public void paint(Graphics g) {
      Dimension d = vv.getSize();
      if (font == null) {
        font = new Font(g.getFont().getName(), Font.BOLD, 30);
        metrics = g.getFontMetrics(font);
        swidth = metrics.stringWidth(str);
        sheight = metrics.getMaxAscent() + metrics.getMaxDescent();
        x = (3 * d.width / 2 - swidth) / 2;
        y = d.height - sheight;
      }
      g.setFont(font);
      Color oldColor = g.getColor();
      g.setColor(Color.gray);
      g.drawString(str, x, y);
      g.setColor(oldColor);
    }

    public boolean useTransform() {
      return false;
    }
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new MultiViewDemo());
    f.pack();
    f.setVisible(true);
  }
}
