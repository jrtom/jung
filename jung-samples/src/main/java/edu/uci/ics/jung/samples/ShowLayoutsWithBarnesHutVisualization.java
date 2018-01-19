/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.samples;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.algorithms.generators.random.BarabasiAlbertGenerator;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.layout.algorithms.CircleLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.FRBHVisitorLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.ISOMLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.KKLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.SpringBHVisitorLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.SpringLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.layout.spatial.BarnesHutQuadTree;
import edu.uci.ics.jung.layout.spatial.ForceObject;
import edu.uci.ics.jung.layout.spatial.Node;
import edu.uci.ics.jung.layout.spatial.Rectangle;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction;
import edu.uci.ics.jung.visualization.layout.LayoutAlgorithmTransition;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import javax.swing.*;

/**
 * This demo is adapted from ShowLayouts, but when a LayoutAlgorithm that uses the BarnesHutQuadTree
 * is selected, the Barnes-Hut structure is drawn on the view under the Graph. For the most dramatic
 * effect, choose the SpringBHVisitorLayoutAlgorithm, then, in picking mode, drag a node or nodes
 * around to watch the Barnes Hut Tree rebuild itself.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class ShowLayoutsWithBarnesHutVisualization extends JPanel {

  protected static Network[] g_array;
  protected static int graph_index;
  protected static String[] graph_names = {
    "Two component graph",
    "Random mixed-mode graph",
    "Miscellaneous multicomponent graph",
    "One component graph",
    "Chain+isolate graph",
    "Trivial (disconnected) graph",
    "Little Graph"
  };

  enum Layouts {
    KK("Kamada Kawai"),
    CIRCLE("Circle"),
    SELF_ORGANIZING_MAP("Self Organizing Map"),
    FR("Fruchterman Reingold (FR)"),
    FR_BH_VISITOR("FR with Barnes-Hut as Visitor"),
    SPRING("Spring"),
    SPRING_BH_VISITOR("Spring with Barnes-Hut as Visitor");

    Layouts(String name) {
      this.name = name;
    }

    private final String name;

    @Override
    public String toString() {
      return name;
    }
  }

  public ShowLayoutsWithBarnesHutVisualization() {

    g_array = new Network[graph_names.length];

    Supplier<Integer> nodeFactory =
        new Supplier<Integer>() {
          int count;

          public Integer get() {
            return count++;
          }
        };
    Supplier<Number> edgeFactory =
        new Supplier<Number>() {
          int count;

          public Number get() {
            return count++;
          }
        };

    g_array[0] = TestGraphs.createTestGraph(false);
    BarabasiAlbertGenerator<Integer, Number> generator =
        new BarabasiAlbertGenerator<>(
            NetworkBuilder.directed().allowsParallelEdges(true), nodeFactory, edgeFactory, 4, 3);
    generator.evolveGraph(20);
    g_array[1] = generator.get();
    g_array[2] = TestGraphs.getDemoGraph();
    g_array[3] = TestGraphs.getOneComponentGraph();
    g_array[4] = TestGraphs.createChainPlusIsolates(18, 5);
    g_array[5] = TestGraphs.createChainPlusIsolates(0, 20);
    MutableNetwork network = NetworkBuilder.directed().allowsParallelEdges(true).build();
    network.addEdge("A", "B", 1);
    network.addEdge("A", "C", 2);

    g_array[6] = network;

    Network g = g_array[3]; // initial graph

    VisualizationViewer vv =
        new VisualizationViewer(g, new Dimension(600, 600)) {

          @Override
          public void paint(Graphics g) {
            updatePaintables(this);
            super.paint(g);
          }
        };

    vv.setBackground(Color.white);
    vv.getRenderContext()
        .setNodeFillPaintFunction(
            new PickableNodePaintFunction<>(vv.getPickedNodeState(), Color.red, Color.yellow));

    vv.getRenderContext().setNodeLabelFunction(Object::toString);
    vv.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.CNTR);

    final DefaultModalGraphMouse<Integer, Number> graphMouse = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(graphMouse);

    // this reinforces that the generics (or lack of) declarations are correct
    vv.setNodeToolTipFunction(
        node ->
            node.toString() + ". with neighbors:" + vv.getModel().getNetwork().adjacentNodes(node));

    final ScalingControl scaler = new CrossoverScalingControl();

    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));
    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    JComboBox modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(
        ((DefaultModalGraphMouse<Integer, Number>) vv.getGraphMouse()).getModeListener());

    setBackground(Color.WHITE);
    setLayout(new BorderLayout());
    add(vv, BorderLayout.CENTER);
    Layouts[] combos = getCombos();
    final JRadioButton animateLayoutTransition = new JRadioButton("Animate Layout Transition");

    final JComboBox jcb = new JComboBox(combos);
    jcb.addActionListener(
        e -> {
          Layouts layoutType = (Layouts) jcb.getSelectedItem();
          LayoutAlgorithm layoutAlgorithm = createLayout(layoutType);
          if (animateLayoutTransition.isSelected()) {
            LayoutAlgorithmTransition.animate(vv, layoutAlgorithm);
          } else {
            LayoutAlgorithmTransition.apply(vv, layoutAlgorithm);
          }
        });

    jcb.setSelectedItem(Layouts.FR);

    JPanel control_panel = new JPanel(new GridLayout(2, 1));
    JPanel topControls = new JPanel();
    JPanel bottomControls = new JPanel();
    control_panel.add(topControls);
    control_panel.add(bottomControls);
    add(control_panel, BorderLayout.NORTH);

    final JComboBox graph_chooser = new JComboBox(graph_names);
    // do this before adding the listener so there is no event fired
    graph_chooser.setSelectedIndex(3);

    graph_chooser.addActionListener(
        e -> {
          graph_index = graph_chooser.getSelectedIndex();
          vv.getNodeSpatial().clear();
          vv.getEdgeSpatial().clear();
          vv.getModel().setNetwork(g_array[graph_index]);
        });

    topControls.add(jcb);
    topControls.add(graph_chooser);
    bottomControls.add(animateLayoutTransition);
    bottomControls.add(plus);
    bottomControls.add(minus);
    bottomControls.add(modeBox);
  }

  private LayoutAlgorithm createLayout(Layouts layoutType) {
    switch (layoutType) {
      case CIRCLE:
        return new CircleLayoutAlgorithm();
      case FR:
        return new FRLayoutAlgorithm();
      case KK:
        return new KKLayoutAlgorithm();
      case SELF_ORGANIZING_MAP:
        return new ISOMLayoutAlgorithm();
      case SPRING:
        return new SpringLayoutAlgorithm();
      case FR_BH_VISITOR:
        return new FRBHVisitorLayoutAlgorithm();
      case SPRING_BH_VISITOR:
        return new SpringBHVisitorLayoutAlgorithm();
      default:
        throw new IllegalArgumentException("Unrecognized layout type");
    }
  }

  private Layouts[] getCombos() {
    return Layouts.values();
  }

  // a hack because I do not want to expose the BH Tree in general
  // but i need a reference to is for this demo
  static BarnesHutQuadTree getBarnesHutQuadTreeFrom(LayoutAlgorithm layoutAlgorithm) {
    if (layoutAlgorithm instanceof FRBHVisitorLayoutAlgorithm) {
      try {
        FRBHVisitorLayoutAlgorithm bhLayoutAlgorithm = (FRBHVisitorLayoutAlgorithm) layoutAlgorithm;
        Field field = bhLayoutAlgorithm.getClass().getDeclaredField("tree");
        field.setAccessible(true);
        return (BarnesHutQuadTree) field.get(bhLayoutAlgorithm);
      } catch (Exception ex) {
        return null;
      }
    } else if (layoutAlgorithm instanceof SpringBHVisitorLayoutAlgorithm) {
      try {
        SpringBHVisitorLayoutAlgorithm bhLayoutAlgorithm =
            (SpringBHVisitorLayoutAlgorithm) layoutAlgorithm;
        Field field = bhLayoutAlgorithm.getClass().getDeclaredField("tree");
        field.setAccessible(true);
        return (BarnesHutQuadTree) field.get(bhLayoutAlgorithm);
      } catch (Exception ex) {
        return null;
      }
    }
    return null;
  }

  private void getShapes(Collection<Shape> shapes, Node node) {
    Rectangle bounds = node.getBounds();
    Rectangle2D r = new Rectangle2D.Double(bounds.x, bounds.y, bounds.width, bounds.height);
    shapes.add(r);
    ForceObject forceObject = node.getForceObject();
    if (forceObject != null) {
      Point center = node.getForceObject().p;
      Ellipse2D forceCenter = new Ellipse2D.Double(center.x - 4, center.y - 4, 8, 8);
      Point2D centerOfNode = new Point2D.Double((r.getCenterX()), r.getCenterY());
      Point2D centerOfForce = new Point2D.Double(center.x, center.y);
      shapes.add(new Line2D.Double(centerOfNode, centerOfForce));
      shapes.add(forceCenter);
    }
    if (node.getNW() != null) {
      getShapes(shapes, node.getNW());
    }
    if (node.getNE() != null) {
      getShapes(shapes, node.getNE());
    }
    if (node.getSW() != null) {
      getShapes(shapes, node.getSW());
    }
    if (node.getSE() != null) {
      getShapes(shapes, node.getSE());
    }
  }

  // save off the paintable so I can remove and re-create it each time
  VisualizationServer.Paintable paintable = null;

  private void updatePaintables(VisualizationViewer vv) {
    vv.removePreRenderPaintable(paintable);
    BarnesHutQuadTree tree = getBarnesHutQuadTreeFrom(vv.getModel().getLayoutAlgorithm());
    if (tree != null) {
      Set<Shape> shapes = new HashSet<>();
      getShapes(shapes, tree.getRoot());

      paintable =
          new VisualizationServer.Paintable() {

            @Override
            public void paint(Graphics g) {
              for (Shape shape : shapes) {
                shape = vv.getTransformSupport().transform(vv, shape);

                g.setColor(Color.blue);
                ((Graphics2D) g).draw(shape);
              }
            }

            @Override
            public boolean useTransform() {
              return false;
            }
          };
      vv.addPreRenderPaintable(paintable);
    }
  }

  public static void main(String[] args) {
    JPanel jp = new ShowLayoutsWithBarnesHutVisualization();

    JFrame jf = new JFrame();
    jf.getContentPane().add(jp);
    jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    jf.pack();
    jf.setVisible(true);
  }
}
