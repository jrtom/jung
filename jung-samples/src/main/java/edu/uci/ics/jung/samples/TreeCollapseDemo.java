package edu.uci.ics.jung.samples;
/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */

import com.google.common.graph.Network;
import edu.uci.ics.jung.graph.CTreeNetwork;
import edu.uci.ics.jung.graph.MutableCTreeNetwork;
import edu.uci.ics.jung.graph.TreeNetworkBuilder;
import edu.uci.ics.jung.layout.algorithms.BalloonLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.RadialTreeLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.TreeLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.PointModel;
import edu.uci.ics.jung.layout.model.PolarPoint;
import edu.uci.ics.jung.layout.util.LayoutAlgorithmTransition;
import edu.uci.ics.jung.samples.util.ControlHelpers;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.AWTPointModel;
import edu.uci.ics.jung.visualization.subLayout.TreeCollapser;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformerDecorator;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.swing.*;

/**
 * Demonstrates "collapsing"/"expanding" of a tree's subtrees.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class TreeCollapseDemo extends JApplet {

  private static final PointModel<Point2D> POINT_MODEL = new AWTPointModel();

  enum Layouts {
    TREE,
    RADIAL,
    BALLOON
  }

  /** the original graph */
  MutableCTreeNetwork<Object, Object> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Object, Object> vv;

  VisualizationServer.Paintable rings;

  VisualizationServer.Paintable balloonRings;

  TreeLayoutAlgorithm<Object, Point2D> layoutAlgorithm;

  RadialTreeLayoutAlgorithm<Object, Point2D> radialLayoutAlgorithm;

  BalloonLayoutAlgorithm<Object, Point2D> balloonLayoutAlgorithm;

  @SuppressWarnings("unchecked")
  public TreeCollapseDemo() {

    // create a simple graph for the demo
    graph = createTree();

    layoutAlgorithm = new TreeLayoutAlgorithm<>(POINT_MODEL);

    radialLayoutAlgorithm = new RadialTreeLayoutAlgorithm<>(POINT_MODEL);

    balloonLayoutAlgorithm = new BalloonLayoutAlgorithm<>(POINT_MODEL);

    vv = new VisualizationViewer(graph, layoutAlgorithm, new Dimension(600, 600));
    vv.setBackground(Color.white);
    vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.line());
    vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
    vv.getRenderContext().setVertexShapeTransformer(new ClusterVertexShapeFunction<>());
    // add a listener for ToolTips
    vv.setVertexToolTipTransformer(new ToStringLabeller());
    vv.getRenderContext().setArrowFillPaintTransformer(n -> Color.lightGray);

    Container content = getContentPane();
    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    content.add(panel);

    final DefaultModalGraphMouse<String, Integer> graphMouse = new DefaultModalGraphMouse<>();

    vv.setGraphMouse(graphMouse);

    JComboBox<?> modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(graphMouse.getModeListener());
    graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);

    JComboBox layoutComboBox = new JComboBox(Layouts.values());
    layoutComboBox.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            if (e.getItem() == Layouts.RADIAL) {
              LayoutAlgorithmTransition.animate(vv.getModel(), radialLayoutAlgorithm);

              vv.removePreRenderPaintable(balloonRings);
              vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
              if (rings == null) {
                rings = new Rings(vv.getModel().getLayoutModel());
              }
              vv.addPreRenderPaintable(rings);

            } else if (e.getItem() == Layouts.BALLOON) {
              LayoutAlgorithmTransition.animate(vv.getModel(), balloonLayoutAlgorithm);

              vv.removePreRenderPaintable(rings);
              vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
              if (balloonRings == null) {
                balloonRings = new BalloonRings(balloonLayoutAlgorithm);
              }

              vv.addPreRenderPaintable(balloonRings);

            } else {
              LayoutAlgorithmTransition.animate(vv.getModel(), layoutAlgorithm);

              vv.removePreRenderPaintable(rings);
              vv.removePreRenderPaintable(balloonRings);
              vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
            }
            vv.repaint();
          }
        });

    JButton collapse = new JButton("Collapse");
    collapse.addActionListener(
        e -> {
          Set<Object> picked = vv.getPickedVertexState().getPicked();
          if (picked.size() == 1) {
            Object root = picked.iterator().next();
            CTreeNetwork subTree = TreeCollapser.collapse(graph, root);
            LayoutModel objectLayoutModel = vv.getModel().getLayoutModel();
            objectLayoutModel.set(subTree, objectLayoutModel.apply(root));
            vv.getModel().setNetwork(graph, false);
            vv.getPickedVertexState().clear();
            vv.repaint();
          }
        });

    JButton expand = new JButton("Expand");
    expand.addActionListener(
        e -> {
          for (Object v : vv.getPickedVertexState().getPicked()) {
            if (v instanceof CTreeNetwork) {
              TreeCollapser.expand(graph, (CTreeNetwork) v);
              vv.getModel().setNetwork(graph);
            }
            vv.getPickedVertexState().clear();
            vv.repaint();
          }
        });

    JPanel controls = new JPanel();
    controls.add(layoutComboBox);
    controls.add(ControlHelpers.getZoomControls(vv, "Zoom"));
    controls.add(modeBox);
    controls.add(collapse);
    controls.add(expand);
    content.add(controls, BorderLayout.SOUTH);
  }

  class Rings implements VisualizationServer.Paintable {

    Collection<Double> depths;
    LayoutModel<Object, Point2D> layoutModel;

    public Rings(LayoutModel<Object, Point2D> layoutModel) {
      this.layoutModel = layoutModel;
      depths = getDepths();
    }

    private Collection<Double> getDepths() {
      Set<Double> depths = new HashSet<>();
      Map<Object, PolarPoint> polarLocations = radialLayoutAlgorithm.getPolarLocations();
      for (Object v : graph.nodes()) {
        PolarPoint pp = polarLocations.get(v);
        depths.add(pp.getRadius());
      }
      return depths;
    }

    public void paint(Graphics g) {
      g.setColor(Color.lightGray);

      Graphics2D g2d = (Graphics2D) g;
      Point2D center = radialLayoutAlgorithm.getCenter(layoutModel);

      Ellipse2D ellipse = new Ellipse2D.Double();
      for (double d : depths) {
        ellipse.setFrameFromDiagonal(
            center.getX() - d, center.getY() - d, center.getX() + d, center.getY() + d);
        Shape shape =
            vv.getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(Layer.LAYOUT)
                .transform(ellipse);
        g2d.draw(shape);
      }
    }

    public boolean useTransform() {
      return true;
    }
  }

  class BalloonRings implements VisualizationServer.Paintable {

    BalloonLayoutAlgorithm<?, Point2D> layoutAlgorithm;

    public BalloonRings(BalloonLayoutAlgorithm<?, Point2D> layoutAlgorithm) {
      this.layoutAlgorithm = layoutAlgorithm;
    }

    public void paint(Graphics g) {
      g.setColor(Color.gray);

      Graphics2D g2d = (Graphics2D) g;

      Ellipse2D ellipse = new Ellipse2D.Double();
      for (Object v : vv.getModel().getNetwork().nodes()) {
        Double radius = layoutAlgorithm.getRadii().get(v);
        if (radius == null) {
          continue;
        }
        Point2D p = vv.getModel().getLayoutModel().apply(v);
        ellipse.setFrame(-radius, -radius, 2 * radius, 2 * radius);
        AffineTransform at = AffineTransform.getTranslateInstance(p.getX(), p.getY());
        Shape shape = at.createTransformedShape(ellipse);

        MutableTransformer viewTransformer =
            vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW);

        if (viewTransformer instanceof MutableTransformerDecorator) {
          shape = vv.getRenderContext().getMultiLayerTransformer().transform(shape);
        } else {
          shape = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, shape);
        }

        g2d.draw(shape);
      }
    }

    public boolean useTransform() {
      return true;
    }
  }

  /** */
  private MutableCTreeNetwork<Object, Object> createTree() {
    MutableCTreeNetwork<Object, Object> tree =
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

  /**
   * a demo class that will create a vertex shape that is either a polygon or star. The number of
   * sides corresponds to the number of vertices that were collapsed into the vertex represented by
   * this shape.
   *
   * @author Tom Nelson
   * @param <V> the vertex type
   */
  class ClusterVertexShapeFunction<V> extends EllipseVertexShapeTransformer<V> {

    ClusterVertexShapeFunction() {
      setSizeTransformer(new ClusterVertexSizeFunction<>(20));
    }

    @Override
    public Shape apply(V v) {
      if (v instanceof Network) {
        @SuppressWarnings("rawtypes")
        int size = ((Network) v).nodes().size();
        if (size < 8) {
          int sides = Math.max(size, 3);
          return factory.getRegularPolygon(v, sides);
        } else {
          return factory.getRegularStar(v, size);
        }
      }
      return super.apply(v);
    }
  }

  /**
   * A demo class that will make vertices larger if they represent a collapsed collection of
   * original vertices
   *
   * @author Tom Nelson
   * @param <V> the vertex type
   */
  class ClusterVertexSizeFunction<V> implements Function<V, Integer> {
    int size;

    public ClusterVertexSizeFunction(Integer size) {
      this.size = size;
    }

    public Integer apply(V v) {
      if (v instanceof Network) {
        return 30;
      }
      return size;
    }
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new TreeCollapseDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
