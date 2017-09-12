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
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.util.Animator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Supplier;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Demonstrates several of the graph layout algorithms. Allows the user to interactively select one
 * of several graphs, and one of several layouts, and visualizes the combination.
 *
 * @author Danyel Fisher
 * @author Joshua O'Madadhain
 */
@SuppressWarnings("serial")
public class ShowLayouts extends JApplet {
  protected static Network<? extends Object, ? extends Object>[] g_array;
  protected static int graph_index;
  protected static String[] graph_names = {
    "Two component graph",
    "Random mixed-mode graph",
    "Miscellaneous multicomponent graph",
    "Random directed acyclic graph",
    "One component graph",
    "Chain+isolate graph",
    "Trivial (disconnected) graph"
  };

  enum Layouts {
    KAMADA_KAWAI,
    FRUCHTERMAN_REINGOLD,
    CIRCLE,
    SPRING,
    SPRING2,
    SELF_ORGANIZING_MAP
  };

  public static class GraphChooser implements ActionListener {
    private JComboBox<?> layout_combo;

    public GraphChooser(JComboBox<?> layout_combo) {
      this.layout_combo = layout_combo;
    }

    public void actionPerformed(ActionEvent e) {
      JComboBox<?> cb = (JComboBox<?>) e.getSource();
      graph_index = cb.getSelectedIndex();
      layout_combo.setSelectedIndex(layout_combo.getSelectedIndex()); // rebuild the layout
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
      Layouts layoutType = (Layouts) jcb.getSelectedItem();
      try {
        // TODO: is this the right input network?  or should it be g_array[graph_index]?
        Layout<Integer> layout = createLayout(layoutType, vv.getModel().getNetwork());
        layout.setInitializer(vv.getGraphLayout());
        layout.setSize(vv.getSize());
        LayoutTransition<Integer, Number> lt =
            new LayoutTransition<>(vv, vv.getGraphLayout(), layout);
        Animator animator = new Animator(lt);
        animator.start();
        vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
        vv.repaint();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static JPanel getGraphPanel() {
    g_array = (Network<? extends Object, ? extends Object>[]) new Network<?, ?>[graph_names.length];

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
        new BarabasiAlbertGenerator<Integer, Number>(
            NetworkBuilder.directed().allowsParallelEdges(true), vertexFactory, edgeFactory, 4, 3);
    generator.evolveGraph(20);
    g_array[1] = generator.get();
    g_array[2] = TestGraphs.getDemoGraph();
    g_array[3] = TestGraphs.createDirectedAcyclicGraph(4, 4, 0.3);
    g_array[4] = TestGraphs.getOneComponentGraph();
    g_array[5] = TestGraphs.createChainPlusIsolates(18, 5);
    g_array[6] = TestGraphs.createChainPlusIsolates(0, 20);

    Network g = g_array[4]; // initial graph

    final VisualizationViewer<Integer, Number> vv =
        new VisualizationViewer<Integer, Number>(g, new FRLayout(g.asGraph()));

    vv.getRenderContext()
        .setVertexFillPaintTransformer(
            new PickableVertexPaintTransformer<Integer>(
                vv.getPickedVertexState(), Color.red, Color.yellow));

    final DefaultModalGraphMouse<Integer, Number> graphMouse =
        new DefaultModalGraphMouse<Integer, Number>();
    vv.setGraphMouse(graphMouse);

    final ScalingControl scaler = new CrossoverScalingControl();

    JButton plus = new JButton("+");
    plus.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            scaler.scale(vv, 1.1f, vv.getCenter());
          }
        });
    JButton minus = new JButton("-");
    minus.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            scaler.scale(vv, 1 / 1.1f, vv.getCenter());
          }
        });
    JButton reset = new JButton("reset");
    reset.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            Layout<Integer> layout = vv.getGraphLayout();
            layout.initialize();
            Relaxer relaxer = vv.getModel().getRelaxer();
            if (relaxer != null) {
              //				if(layout instanceof IterativeContext) {
              relaxer.stop();
              relaxer.prerelax();
              relaxer.relax();
            }
          }
        });

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
    //        jcb.setSelectedItem(FRLayout.class);
    jcb.setSelectedItem(Layouts.FRUCHTERMAN_REINGOLD);

    JPanel control_panel = new JPanel(new GridLayout(2, 1));
    JPanel topControls = new JPanel();
    JPanel bottomControls = new JPanel();
    control_panel.add(topControls);
    control_panel.add(bottomControls);
    jp.add(control_panel, BorderLayout.NORTH);

    final JComboBox graph_chooser = new JComboBox(graph_names);

    graph_chooser.addActionListener(new GraphChooser(jcb));

    topControls.add(jcb);
    topControls.add(graph_chooser);
    bottomControls.add(plus);
    bottomControls.add(minus);
    bottomControls.add(modeBox);
    bottomControls.add(reset);
    return jp;
  }

  public void start() {
    this.getContentPane().add(getGraphPanel());
  }

  private static <N, E> Layout<N> createLayout(Layouts layoutType, Network<N, E> network) {
    switch (layoutType) {
      case CIRCLE:
        return new CircleLayout<N>(network.asGraph());
      case FRUCHTERMAN_REINGOLD:
        return new FRLayout<N>(network.asGraph());
      case KAMADA_KAWAI:
        return new KKLayout<N>(network.asGraph());
      case SELF_ORGANIZING_MAP:
        return new ISOMLayout<N, E>(network);
      case SPRING:
        return new SpringLayout<N>(network.asGraph());
      case SPRING2:
        return new SpringLayout2<N>(network.asGraph());
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
    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jf.pack();
    jf.setVisible(true);
  }
}
