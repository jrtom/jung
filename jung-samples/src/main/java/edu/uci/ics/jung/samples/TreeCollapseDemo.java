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
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.layout.model.PolarPoint;
import edu.uci.ics.jung.samples.util.ControlHelpers;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.EllipseNodeShapeFunction;
import edu.uci.ics.jung.visualization.layout.LayoutAlgorithmTransition;
import edu.uci.ics.jung.visualization.subLayout.TreeCollapser;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformerDecorator;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
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
public class TreeCollapseDemo extends JPanel {

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

  TreeLayoutAlgorithm<Object> layoutAlgorithm;

  RadialTreeLayoutAlgorithm<Object> radialLayoutAlgorithm;

  BalloonLayoutAlgorithm<Object> balloonLayoutAlgorithm;

  @SuppressWarnings("unchecked")
  public TreeCollapseDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = createTree();

    layoutAlgorithm = new TreeLayoutAlgorithm<>();

    radialLayoutAlgorithm = new RadialTreeLayoutAlgorithm<>();

    balloonLayoutAlgorithm = new BalloonLayoutAlgorithm<>();

    vv = new VisualizationViewer(graph, layoutAlgorithm, new Dimension(600, 600));
    vv.setBackground(Color.white);
    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
    vv.getRenderContext().setNodeLabelFunction(Object::toString);
    vv.getRenderContext().setNodeShapeFunction(new ClusterNodeShapeFunction<>());
    // add a listener for ToolTips
    vv.setNodeToolTipFunction(Object::toString);
    vv.getRenderContext().setArrowFillPaintFunction(n -> Color.lightGray);

    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    add(panel);

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
              LayoutAlgorithmTransition.animate(vv, radialLayoutAlgorithm);

              vv.removePreRenderPaintable(balloonRings);
              vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
              if (rings == null) {
                rings = new Rings(vv.getModel().getLayoutModel());
              }
              vv.addPreRenderPaintable(rings);

            } else if (e.getItem() == Layouts.BALLOON) {
              LayoutAlgorithmTransition.animate(vv, balloonLayoutAlgorithm);

              vv.removePreRenderPaintable(rings);
              vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
              if (balloonRings == null) {
                balloonRings = new BalloonRings(balloonLayoutAlgorithm);
              }

              vv.addPreRenderPaintable(balloonRings);

            } else {
              LayoutAlgorithmTransition.animate(vv, layoutAlgorithm);

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
          Set<Object> picked = vv.getPickedNodeState().getPicked();
          if (picked.size() == 1) {
            Object root = picked.iterator().next();
            CTreeNetwork subTree = TreeCollapser.collapse(graph, root);
            LayoutModel<Object> objectLayoutModel = vv.getModel().getLayoutModel();
            objectLayoutModel.set(subTree, objectLayoutModel.apply(root));
            vv.getModel().setNetwork(graph, true);
            vv.getPickedNodeState().clear();
            vv.repaint();
          }
        });

    JButton expand = new JButton("Expand");
    expand.addActionListener(
        e -> {
          for (Object v : vv.getPickedNodeState().getPicked()) {
            if (v instanceof MutableCTreeNetwork) {
              graph = TreeCollapser.expand(graph, (MutableCTreeNetwork) v);
              LayoutModel<Object> objectLayoutModel = vv.getModel().getLayoutModel();
              objectLayoutModel.set(graph, objectLayoutModel.apply(v));
              vv.getModel().setNetwork(graph, true);
            }
            vv.getPickedNodeState().clear();
            vv.repaint();
          }
        });

    JPanel controls = new JPanel();
    controls.add(layoutComboBox);
    controls.add(ControlHelpers.getZoomControls(vv, "Zoom"));
    controls.add(modeBox);
    controls.add(collapse);
    controls.add(expand);
    add(controls, BorderLayout.SOUTH);
  }

  class Rings implements VisualizationServer.Paintable {

    Collection<Double> depths;
    LayoutModel<Object> layoutModel;

    public Rings(LayoutModel<Object> layoutModel) {
      this.layoutModel = layoutModel;
      depths = getDepths();
    }

    private Collection<Double> getDepths() {
      Set<Double> depths = new HashSet<>();
      Map<Object, PolarPoint> polarLocations = radialLayoutAlgorithm.getPolarLocations();
      for (Object v : graph.nodes()) {
        PolarPoint pp = polarLocations.get(v);
        depths.add(pp.radius);
      }
      return depths;
    }

    public void paint(Graphics g) {
      g.setColor(Color.lightGray);

      Graphics2D g2d = (Graphics2D) g;
      Point center = radialLayoutAlgorithm.getCenter(layoutModel);

      Ellipse2D ellipse = new Ellipse2D.Double();
      for (double d : depths) {
        ellipse.setFrameFromDiagonal(center.x - d, center.y - d, center.x + d, center.y + d);
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

    BalloonLayoutAlgorithm<?> layoutAlgorithm;

    public BalloonRings(BalloonLayoutAlgorithm<?> layoutAlgorithm) {
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
        Point p = vv.getModel().getLayoutModel().apply(v);
        ellipse.setFrame(-radius, -radius, 2 * radius, 2 * radius);
        AffineTransform at = AffineTransform.getTranslateInstance(p.x, p.y);
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
   * a demo class that will create a node shape that is either a polygon or star. The number of
   * sides corresponds to the number of nodes that were collapsed into the node represented by this
   * shape.
   *
   * @author Tom Nelson
   * @param <N> the node type
   */
  class ClusterNodeShapeFunction<N> extends EllipseNodeShapeFunction<N> {

    ClusterNodeShapeFunction() {
      setSizeTransformer(new ClusterNodeSizeFunction<>(20));
    }

    @Override
    public Shape apply(N v) {
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
   * A demo class that will make nodes larger if they represent a collapsed collection of original
   * nodes
   *
   * @author Tom Nelson
   * @param <N> the node type
   */
  class ClusterNodeSizeFunction<N> implements Function<N, Integer> {
    int size;

    public ClusterNodeSizeFunction(Integer size) {
      this.size = size;
    }

    public Integer apply(N v) {
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
