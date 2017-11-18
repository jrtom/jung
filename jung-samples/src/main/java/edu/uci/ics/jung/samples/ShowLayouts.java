/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.samples;

import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.algorithms.generators.random.BarabasiAlbertGenerator;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.layout.algorithms.CircleLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.immutable.FRLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.immutable.ISOMLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.immutable.KKLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.immutable.SpringLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.PointModel;
import edu.uci.ics.jung.layout.util.LayoutAlgorithmTransition;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.AWTPointModel;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.function.Supplier;
import javax.swing.*;

/**
 * Demonstrates several of the graph layout algorithms. Allows the user to interactively select one
 * of several graphs, and one of several layouts, and visualizes the combination.
 *
 * @author Danyel Fisher
 * @author Joshua O'Madadhain
 */
@SuppressWarnings("serial")
public class ShowLayouts extends JApplet {
  private static final PointModel<Point2D> POINT_MODEL = new AWTPointModel();
  protected static Network[] g_array;
  protected static int graph_index;
  protected static String[] graph_names = {
    "Two component graph",
    "Random mixed-mode graph",
    "Miscellaneous multicomponent graph",
    "One component graph",
    "Chain+isolate graph",
    "Trivial (disconnected) graph"
  };

  enum Layouts {
    KAMADA_KAWAI,
    FRUCHTERMAN_REINGOLD,
    CIRCLE,
    SPRING,
    SELF_ORGANIZING_MAP
  }

  public static class GraphChooser implements ActionListener {
    private final VisualizationViewer<Integer, Number> vv;

    public GraphChooser(VisualizationViewer<Integer, Number> vv) {
      this.vv = vv;
    }

    public void actionPerformed(ActionEvent e) {
      SwingUtilities.invokeLater(
          () -> {
            JComboBox<?> cb = (JComboBox<?>) e.getSource();
            graph_index = cb.getSelectedIndex();
            vv.getModel().setNetwork(g_array[graph_index]);
          });
    }
  }

  /** @author danyelf */
  private static final class LayoutChooser implements ActionListener {
    private final JComboBox<?> jcb;
    private final VisualizationViewer<Integer, Number> vv;

    private LayoutChooser(JComboBox<?> jcb, VisualizationViewer<Integer, Number> vv) {
      super();
      this.jcb = jcb;
      this.vv = vv;
    }

    public void actionPerformed(ActionEvent arg0) {
      SwingUtilities.invokeLater(
          () -> {
            Layouts layoutType = (Layouts) jcb.getSelectedItem();
            LayoutAlgorithm layoutAlgorithm = createLayout(layoutType);
            LayoutAlgorithmTransition.animate(vv.getModel(), layoutAlgorithm);
          });
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static JPanel getGraphPanel() {
    g_array = new Network[graph_names.length];

    Supplier<Integer> vertexFactory =
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
            NetworkBuilder.directed().allowsParallelEdges(true), vertexFactory, edgeFactory, 4, 3);
    generator.evolveGraph(20);
    g_array[1] = generator.get();
    g_array[2] = TestGraphs.getDemoGraph();
    g_array[3] = TestGraphs.getOneComponentGraph();
    g_array[4] = TestGraphs.createChainPlusIsolates(18, 5);
    g_array[5] = TestGraphs.createChainPlusIsolates(0, 20);

    Network g = g_array[3]; // initial graph

    final VisualizationViewer vv = new VisualizationViewer<>(g);

    vv.getRenderContext()
        .setVertexFillPaintTransformer(
            new PickableVertexPaintTransformer<>(
                vv.getPickedVertexState(), Color.red, Color.yellow));

    vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
    vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);

    final DefaultModalGraphMouse<Integer, Number> graphMouse = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(graphMouse);

    // this reinforces that the generics (or lack of) declarations are correct
    vv.setVertexToolTipTransformer(
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

    JPanel jp = new JPanel();
    jp.setBackground(Color.WHITE);
    jp.setLayout(new BorderLayout());
    jp.add(vv, BorderLayout.CENTER);
    Layouts[] combos = getCombos();
    final JComboBox jcb = new JComboBox(combos);
    // use a renderer to shorten the layout name presentation
    //        jcb.setRenderer(new DefaultListCellRenderer() {
    //            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    //                String valueString = value.toString();
    //                valueString = valueString.substring(valueString.lastIndexOf('.')+1);
    //                return super.getListCellRendererComponent(list, valueString, index, isSelected,
    //                        cellHasFocus);
    //            }
    //        });
    jcb.addActionListener(new LayoutChooser(jcb, vv));
    jcb.setSelectedItem(Layouts.FRUCHTERMAN_REINGOLD);

    JPanel control_panel = new JPanel(new GridLayout(2, 1));
    JPanel topControls = new JPanel();
    JPanel bottomControls = new JPanel();
    control_panel.add(topControls);
    control_panel.add(bottomControls);
    jp.add(control_panel, BorderLayout.NORTH);

    final JComboBox graph_chooser = new JComboBox(graph_names);
    // do this before adding the listener so there is no event fired
    graph_chooser.setSelectedIndex(3);

    graph_chooser.addActionListener(new GraphChooser(vv));

    topControls.add(jcb);
    topControls.add(graph_chooser);
    bottomControls.add(plus);
    bottomControls.add(minus);
    bottomControls.add(modeBox);
    return jp;
  }

  public void start() {
    this.getContentPane().add(getGraphPanel());
  }

  private static LayoutAlgorithm createLayout(Layouts layoutType) {
    switch (layoutType) {
      case CIRCLE:
        return new CircleLayoutAlgorithm(POINT_MODEL);
      case FRUCHTERMAN_REINGOLD:
        return new FRLayoutAlgorithm(POINT_MODEL);
      case KAMADA_KAWAI:
        return new KKLayoutAlgorithm(POINT_MODEL);
      case SELF_ORGANIZING_MAP:
        return new ISOMLayoutAlgorithm(POINT_MODEL);
      case SPRING:
        return new SpringLayoutAlgorithm(POINT_MODEL);
      default:
        throw new IllegalArgumentException("Unrecognized layout type");
    }
  }

  private static Layouts[] getCombos() {
    return Layouts.values();
  }

  public static void main(String[] args) {
    JPanel jp = getGraphPanel();

    JFrame jf = new JFrame();
    jf.getContentPane().add(jp);
    jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    jf.pack();
    jf.setVisible(true);
  }
}
