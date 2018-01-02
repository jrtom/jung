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
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.SatelliteVisualizationViewer;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction;
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction;
import edu.uci.ics.jung.visualization.renderers.GradientNodeRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.transform.shape.ShapeTransformer;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.geom.GeneralPath;
import javax.swing.*;

/**
 * Demonstrates the construction of a graph visualization with a main and a satellite view. The
 * satellite view is smaller, always contains the entire graph, and contains a lens shape that shows
 * the boundaries of the visible part of the graph in the main view. Using the mouse, you can pick,
 * translate, layout-scale, view-scale, rotate, shear, and region-select in either view. Using the
 * mouse in either window affects only the main view and the lens shape in the satellite view.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class SatelliteViewDemo extends JPanel {

  static final String instructions =
      "<html>"
          + "<b><h2><center>Instructions for Mouse Listeners</center></h2></b>"
          + "<p>There are two modes, Transforming and Picking."
          + "<p>The modes are selected with a combo box."
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
          + "<li>Mousewheel scales with a crossover value of 1.0.<p>"
          + "     - scales the graph layout when the combined scale is greater than 1<p>"
          + "     - scales the graph view when the combined scale is less than 1";

  JDialog helpDialog;

  VisualizationServer.Paintable viewGrid;

  /** create an instance of a simple graph in two views with controls to demo the features. */
  public SatelliteViewDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    Network<String, Number> graph = TestGraphs.getOneComponentGraph();

    // the preferred sizes for the two views
    Dimension preferredSize1 = new Dimension(600, 600);
    Dimension preferredSize2 = new Dimension(300, 300);

    // create one layout for the graph
    FRLayoutAlgorithm<String> layoutAlgorithm = new FRLayoutAlgorithm<>();
    // not used, for testing only
    //    CircleLayoutAlgorithm<String, Point2D> clayout = new CircleLayoutAlgorithm<>();
    layoutAlgorithm.setMaxIterations(500);

    // create one model that both views will share
    VisualizationModel<String, Number> vm =
        new BaseVisualizationModel<>(graph, layoutAlgorithm, preferredSize1);

    // create 2 views that share the same model
    final VisualizationViewer<String, Number> vv1 = new VisualizationViewer<>(vm, preferredSize1);
    final SatelliteVisualizationViewer<String, Number> vv2 =
        new SatelliteVisualizationViewer<>(vv1, preferredSize2);
    vv1.setBackground(Color.white);
    vv1.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableEdgePaintFunction<>(vv1.getPickedEdgeState(), Color.black, Color.cyan));
    vv1.getRenderContext()
        .setNodeFillPaintFunction(
            new PickableNodePaintFunction<>(vv1.getPickedNodeState(), Color.red, Color.yellow));
    vv2.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableEdgePaintFunction<>(vv2.getPickedEdgeState(), Color.black, Color.cyan));
    vv2.getRenderContext()
        .setNodeFillPaintFunction(
            new PickableNodePaintFunction<>(vv2.getPickedNodeState(), Color.red, Color.yellow));
    vv1.getRenderer()
        .setNodeRenderer(new GradientNodeRenderer<>(vv1, Color.red, Color.white, true));
    vv1.getRenderContext().setNodeLabelFunction(Object::toString);
    vv1.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.CNTR);

    ScalingControl vv2Scaler = new CrossoverScalingControl();
    vv2.scaleToLayout(vv2Scaler);

    viewGrid = new ViewGrid(vv2, vv1);

    // add default listener for ToolTips
    vv1.setNodeToolTipFunction(Object::toString);
    vv2.setNodeToolTipFunction(Object::toString);

    vv2.getRenderContext().setNodeLabelFunction(vv1.getRenderContext().getNodeLabelFunction());

    ToolTipManager.sharedInstance().setDismissDelay(10000);

    Container panel = new JPanel(new BorderLayout());
    Container rightPanel = new JPanel(new GridLayout(2, 1));

    GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv1);
    panel.add(gzsp);
    rightPanel.add(new JPanel());
    rightPanel.add(vv2);
    panel.add(rightPanel, BorderLayout.EAST);

    helpDialog = new JDialog();
    helpDialog.getContentPane().add(new JLabel(instructions));

    // create a GraphMouse for the main view
    final DefaultModalGraphMouse<String, Number> graphMouse = new DefaultModalGraphMouse<>();
    vv1.setGraphMouse(graphMouse);

    JComboBox<?> modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(((DefaultModalGraphMouse<?, ?>) vv2.getGraphMouse()).getModeListener());

    JCheckBox gridBox = new JCheckBox("Show Grid");
    gridBox.addItemListener(e -> showGrid(vv2, e.getStateChange() == ItemEvent.SELECTED));

    JButton help = new JButton("Help");
    help.addActionListener(
        e -> {
          helpDialog.pack();
          helpDialog.setVisible(true);
        });

    JPanel controls = new JPanel();
    controls.add(ControlHelpers.getZoomControls(vv1, ""));
    //    controls.add(minus);
    controls.add(modeBox);
    controls.add(gridBox);
    controls.add(help);
    add(panel);
    add(controls, BorderLayout.SOUTH);
  }

  protected void showGrid(VisualizationViewer<?, ?> vv, boolean state) {
    if (state) {
      vv.addPreRenderPaintable(viewGrid);
    } else {
      vv.removePreRenderPaintable(viewGrid);
    }
    vv.repaint();
  }

  /**
   * draws a grid on the SatelliteViewer's lens
   *
   * @author Tom Nelson
   */
  static class ViewGrid implements VisualizationServer.Paintable {

    VisualizationViewer<?, ?> master;
    VisualizationViewer<?, ?> vv;

    public ViewGrid(VisualizationViewer<?, ?> vv, VisualizationViewer<?, ?> master) {
      this.vv = vv;
      this.master = master;
    }

    public void paint(Graphics g) {
      ShapeTransformer masterViewTransformer =
          master.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW);
      ShapeTransformer masterLayoutTransformer =
          master.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
      ShapeTransformer vvLayoutTransformer =
          vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);

      Rectangle rect = master.getBounds();
      GeneralPath path = new GeneralPath();
      path.moveTo(rect.x, rect.y);
      path.lineTo(rect.width, rect.y);
      path.lineTo(rect.width, rect.height);
      path.lineTo(rect.x, rect.height);
      path.lineTo(rect.x, rect.y);

      for (int i = 0; i <= rect.width; i += rect.width / 10) {
        path.moveTo(rect.x + i, rect.y);
        path.lineTo(rect.x + i, rect.height);
      }
      for (int i = 0; i <= rect.height; i += rect.height / 10) {
        path.moveTo(rect.x, rect.y + i);
        path.lineTo(rect.width, rect.y + i);
      }
      Shape lens = path;
      lens = masterViewTransformer.inverseTransform(lens);
      lens = masterLayoutTransformer.inverseTransform(lens);
      lens = vvLayoutTransformer.transform(lens);
      Graphics2D g2d = (Graphics2D) g;
      Color old = g.getColor();
      g.setColor(Color.cyan);
      g2d.draw(lens);

      path = new GeneralPath();
      path.moveTo((float) rect.getMinX(), (float) rect.getCenterY());
      path.lineTo((float) rect.getMaxX(), (float) rect.getCenterY());
      path.moveTo((float) rect.getCenterX(), (float) rect.getMinY());
      path.lineTo((float) rect.getCenterX(), (float) rect.getMaxY());
      Shape crosshairShape = path;
      crosshairShape = masterViewTransformer.inverseTransform(crosshairShape);
      crosshairShape = masterLayoutTransformer.inverseTransform(crosshairShape);
      crosshairShape = vvLayoutTransformer.transform(crosshairShape);
      g.setColor(Color.black);
      g2d.setStroke(new BasicStroke(3));
      g2d.draw(crosshairShape);

      g.setColor(old);
    }

    public boolean useTransform() {
      return true;
    }
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new SatelliteViewDemo());
    f.pack();
    f.setVisible(true);
  }
}
