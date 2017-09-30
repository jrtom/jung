/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.samples;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.decorators.VertexIconShapeTransformer;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * A demo that shows flag images as vertices, and uses unicode to render vertex labels.
 *
 * @author Tom Nelson
 */
public class UnicodeLabelDemo {

  /** the graph */
  Network<Integer, Number> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Integer, Number> vv;

  boolean showLabels;

  public UnicodeLabelDemo() {

    // create a simple graph for the demo
    graph = createGraph();
    Map<Integer, Icon> iconMap = new HashMap<Integer, Icon>();

    vv = new VisualizationViewer<Integer, Number>(graph, new FRLayout<Integer>(graph.asGraph()));
    vv.getRenderContext().setVertexLabelTransformer(new UnicodeVertexStringer());
    vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.cyan));
    vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.cyan));
    VertexIconShapeTransformer<Integer> vertexIconShapeFunction =
        new VertexIconShapeTransformer<Integer>(new EllipseVertexShapeTransformer<Integer>());
    Function<Integer, Icon> vertexIconFunction = iconMap::get;
    vv.getRenderContext().setVertexShapeTransformer(vertexIconShapeFunction);
    vv.getRenderContext().setVertexIconTransformer(vertexIconFunction);
    loadImages(iconMap);
    vertexIconShapeFunction.setIconMap(iconMap);
    vv.getRenderContext()
        .setVertexFillPaintTransformer(
            new PickableVertexPaintTransformer<Integer>(
                vv.getPickedVertexState(), Color.white, Color.yellow));
    vv.getRenderContext()
        .setEdgeDrawPaintTransformer(
            new PickableEdgePaintTransformer<Number>(
                vv.getPickedEdgeState(), Color.black, Color.lightGray));

    vv.setBackground(Color.white);

    // add my listener for ToolTips
    vv.setVertexToolTipTransformer(new ToStringLabeller());

    // create a frome to hold the graph
    final JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    content.add(panel);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    final DefaultModalGraphMouse<Integer, Number> gm =
        new DefaultModalGraphMouse<Integer, Number>();
    vv.setGraphMouse(gm);

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

    JCheckBox lo = new JCheckBox("Show Labels");
    lo.addItemListener(
        new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            showLabels = e.getStateChange() == ItemEvent.SELECTED;
            vv.repaint();
          }
        });
    lo.setSelected(true);

    JPanel controls = new JPanel();
    controls.add(plus);
    controls.add(minus);
    controls.add(lo);
    controls.add(gm.getModeComboBox());
    content.add(controls, BorderLayout.SOUTH);

    frame.pack();
    frame.setVisible(true);
  }

  class UnicodeVertexStringer implements Function<Integer, String> {

    Map<Integer, String> map = new HashMap<Integer, String>();
    Map<Integer, Icon> iconMap = new HashMap<Integer, Icon>();
    String[] labels = {
      "\u0057\u0065\u006C\u0063\u006F\u006D\u0065\u0020\u0074\u006F\u0020JUNG\u0021",
      "\u6B22\u8FCE\u4F7F\u7528\u0020\u0020JUNG\u0021",
      "\u0414\u043E\u0431\u0440\u043E\u0020\u043F\u043E\u0436\u0430\u043B\u043E\u0432\u0430\u0422\u044A\u0020\u0432\u0020JUNG\u0021",
      "\u0042\u0069\u0065\u006E\u0076\u0065\u006E\u0075\u0065\u0020\u0061\u0075\u0020JUNG\u0021",
      "\u0057\u0069\u006C\u006B\u006F\u006D\u006D\u0065\u006E\u0020\u007A\u0075\u0020JUNG\u0021",
      "JUNG\u3078\u3087\u3045\u3053\u305D\u0021",
      //                "\u0053\u00E9\u006A\u0061\u0020\u0042\u0065\u006D\u0076\u0069\u006E\u0064\u006F\u0020JUNG\u0021",
      "\u0042\u0069\u0065\u006E\u0076\u0065\u006E\u0069\u0064\u0061\u0020\u0061\u0020JUNG\u0021"
    };

    public UnicodeVertexStringer() {
      for (Integer node : graph.nodes()) {
        map.put(node, labels[node.intValue() % labels.length]);
      }
    }

    /**
     * @see edu.uci.ics.jung.graph.decorators.VertexStringer#getLabel(edu.uci.ics.jung.graph.Vertex)
     */
    public String getLabel(Integer v) {
      if (showLabels) {
        return map.get(v);
      } else {
        return "";
      }
    }

    public String apply(Integer input) {
      return getLabel(input);
    }
  }

  Network<Integer, Number> createGraph() {
    MutableNetwork<Integer, Number> graph = NetworkBuilder.directed().build();
    graph.addEdge(0, 1, new Double(Math.random()));
    graph.addEdge(3, 0, new Double(Math.random()));
    graph.addEdge(0, 4, new Double(Math.random()));
    graph.addEdge(4, 5, new Double(Math.random()));
    graph.addEdge(5, 3, new Double(Math.random()));
    graph.addEdge(2, 1, new Double(Math.random()));
    graph.addEdge(4, 1, new Double(Math.random()));
    graph.addEdge(8, 2, new Double(Math.random()));
    graph.addEdge(3, 8, new Double(Math.random()));
    graph.addEdge(6, 7, new Double(Math.random()));
    graph.addEdge(7, 5, new Double(Math.random()));
    graph.addEdge(0, 9, new Double(Math.random()));
    graph.addEdge(9, 8, new Double(Math.random()));
    graph.addEdge(7, 6, new Double(Math.random()));
    graph.addEdge(6, 5, new Double(Math.random()));
    graph.addEdge(4, 2, new Double(Math.random()));
    graph.addEdge(5, 4, new Double(Math.random()));
    graph.addEdge(4, 10, new Double(Math.random()));
    graph.addEdge(10, 4, new Double(Math.random()));

    return graph;
  }

  protected void loadImages(Map<Integer, Icon> imageMap) {

    ImageIcon[] icons = null;
    try {
      icons =
          new ImageIcon[] {
            new ImageIcon(getClass().getResource("/images/united-states.gif")),
            new ImageIcon(getClass().getResource("/images/china.gif")),
            new ImageIcon(getClass().getResource("/images/russia.gif")),
            new ImageIcon(getClass().getResource("/images/france.gif")),
            new ImageIcon(getClass().getResource("/images/germany.gif")),
            new ImageIcon(getClass().getResource("/images/japan.gif")),
            new ImageIcon(getClass().getResource("/images/spain.gif"))
          };
    } catch (Exception ex) {
      System.err.println("You need flags.jar in your classpath to see the flag icons.");
    }
    for (Integer node : graph.nodes()) {
      int i = node.intValue();
      imageMap.put(node, icons[i % icons.length]);
    }
  }

  public static void main(String[] args) {
    new UnicodeLabelDemo();
  }
}
