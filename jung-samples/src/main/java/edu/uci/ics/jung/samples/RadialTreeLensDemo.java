/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.samples;

import edu.uci.ics.jung.graph.CTreeNetwork;
import edu.uci.ics.jung.graph.MutableCTreeNetwork;
import edu.uci.ics.jung.graph.TreeNetworkBuilder;
import edu.uci.ics.jung.layout.algorithms.RadialTreeLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.PointModel;
import edu.uci.ics.jung.layout.model.PolarPoint;
import edu.uci.ics.jung.samples.util.ControlHelpers;
import edu.uci.ics.jung.visualization.BaseVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalLensGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.layout.AWTPointModel;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.transform.LensSupport;
import edu.uci.ics.jung.visualization.transform.shape.HyperbolicShapeTransformer;
import edu.uci.ics.jung.visualization.transform.shape.ViewLensSupport;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.ItemEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.*;

/**
 * Shows a RadialTreeLayout view of a Forest. A hyperbolic projection lens may also be applied to
 * the view
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class RadialTreeLensDemo extends JApplet {

  private static final PointModel<Point2D> POINT_MODEL = new AWTPointModel();

  CTreeNetwork<String, Integer> graph;

  VisualizationServer.Paintable rings;

  String root;

  //  TreeLayoutAlgorithm<String, Point2D> layoutAlgorithm;

  RadialTreeLayoutAlgorithm<String, Point2D> radialLayoutAlgorithm;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  /** provides a Hyperbolic lens for the view */
  LensSupport hyperbolicViewSupport;

  ScalingControl scaler;

  /** create an instance of a simple graph with controls to demo the zoomand hyperbolic features. */
  public RadialTreeLensDemo() {

    // create a simple graph for the demo
    graph = createTree();

    //    layoutAlgorithm = new TreeLayoutAlgorithm<>(pointModel);
    radialLayoutAlgorithm = new RadialTreeLayoutAlgorithm<>(POINT_MODEL);
    //    radialLayoutAlgorithm.setSize(new Dimension(600, 600));

    Dimension preferredSize = new Dimension(600, 600);

    final VisualizationModel<String, Integer, Point2D> visualizationModel =
        new BaseVisualizationModel<>(graph, radialLayoutAlgorithm, preferredSize);
    vv = new VisualizationViewer<>(visualizationModel, preferredSize);

    PickedState<String> ps = vv.getPickedVertexState();
    PickedState<Integer> pes = vv.getPickedEdgeState();
    vv.getRenderContext()
        .setVertexFillPaintTransformer(
            new PickableVertexPaintTransformer<>(ps, Color.red, Color.yellow));
    vv.getRenderContext()
        .setEdgeDrawPaintTransformer(
            new PickableEdgePaintTransformer<>(pes, Color.black, Color.cyan));
    vv.setBackground(Color.white);

    vv.getRenderContext().setVertexLabelTransformer(Object::toString);
    vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.line());

    // add a listener for ToolTips
    vv.setVertexToolTipTransformer(Object::toString);

    Container content = getContentPane();
    GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
    content.add(gzsp);

    final DefaultModalGraphMouse<String, Integer> graphMouse = new DefaultModalGraphMouse<>();

    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());
    rings = new Rings(vv.getModel().getLayoutModel());
    vv.addPreRenderPaintable(rings);

    hyperbolicViewSupport =
        new ViewLensSupport<>(
            vv,
            new HyperbolicShapeTransformer(
                vv, vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)),
            new ModalLensGraphMouse());

    final JRadioButton hyperView = new JRadioButton("Hyperbolic View");
    hyperView.addItemListener(
        e -> hyperbolicViewSupport.activate(e.getStateChange() == ItemEvent.SELECTED));

    graphMouse.addItemListener(hyperbolicViewSupport.getGraphMouse().getModeListener());

    JMenuBar menubar = new JMenuBar();
    menubar.add(graphMouse.getModeMenu());
    gzsp.setCorner(menubar);

    JPanel controls = new JPanel();
    JPanel hyperControls = new JPanel(new GridLayout(3, 2));
    hyperControls.setBorder(BorderFactory.createTitledBorder("Examiner Lens"));
    JPanel modeControls = new JPanel(new BorderLayout());
    modeControls.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
    modeControls.add(graphMouse.getModeComboBox());
    hyperControls.add(hyperView);

    controls.add(ControlHelpers.getZoomControls(vv, "Zoom"));
    controls.add(hyperControls);
    controls.add(modeControls);
    content.add(controls, BorderLayout.SOUTH);
  }

  private CTreeNetwork<String, Integer> createTree() {
    MutableCTreeNetwork<String, Integer> tree =
        TreeNetworkBuilder.builder().expectedNodeCount(27).build();

    tree.addNode("root");

    int edgeId = 0;
    tree.addEdge("root", "V0", edgeId++);
    tree.addEdge("V0", "V1", edgeId++);
    tree.addEdge("V0", "V2", edgeId++);
    tree.addEdge("V1", "V4", edgeId++);
    tree.addEdge("V2", "V3", edgeId++);
    tree.addEdge("V2", "V5", edgeId++);
    tree.addEdge("V4", "V6", edgeId++);
    tree.addEdge("V4", "V7", edgeId++);
    tree.addEdge("V3", "V8", edgeId++);
    tree.addEdge("V6", "V9", edgeId++);
    tree.addEdge("V4", "V10", edgeId++);

    tree.addEdge("root", "A0", edgeId++);
    tree.addEdge("A0", "A1", edgeId++);
    tree.addEdge("A0", "A2", edgeId++);
    tree.addEdge("A0", "A3", edgeId++);

    tree.addEdge("root", "B0", edgeId++);
    tree.addEdge("B0", "B1", edgeId++);
    tree.addEdge("B0", "B2", edgeId++);
    tree.addEdge("B1", "B4", edgeId++);
    tree.addEdge("B2", "B3", edgeId++);
    tree.addEdge("B2", "B5", edgeId++);
    tree.addEdge("B4", "B6", edgeId++);
    tree.addEdge("B4", "B7", edgeId++);
    tree.addEdge("B3", "B8", edgeId++);
    tree.addEdge("B6", "B9", edgeId++);

    return tree;
  }

  class Rings implements VisualizationServer.Paintable {

    Collection<Double> depths;
    LayoutModel<String, Point2D> layoutModel;

    public Rings(LayoutModel<String, Point2D> layoutModel) {
      this.layoutModel = layoutModel;
      depths = getDepths();
    }

    private Collection<Double> getDepths() {
      Set<Double> depths = new HashSet<>();
      Map<String, PolarPoint> polarLocations = radialLayoutAlgorithm.getPolarLocations();
      for (String v : graph.nodes()) {
        PolarPoint pp = polarLocations.get(v);
        depths.add(pp.getRadius());
      }
      return depths;
    }

    public void paint(Graphics g) {
      g.setColor(Color.gray);
      Graphics2D g2d = (Graphics2D) g;
      Point2D center = radialLayoutAlgorithm.getCenter(layoutModel);

      Ellipse2D ellipse = new Ellipse2D.Double();
      for (double d : depths) {
        ellipse.setFrameFromDiagonal(
            center.getX() - d, center.getY() - d, center.getX() + d, center.getY() + d);
        Shape shape = vv.getRenderContext().getMultiLayerTransformer().transform(ellipse);
        g2d.draw(shape);
      }
    }

    public boolean useTransform() {
      return false;
    }
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new RadialTreeLensDemo());
    f.pack();
    f.setVisible(true);
  }
}
