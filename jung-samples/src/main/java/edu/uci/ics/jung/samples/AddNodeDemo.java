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
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.graph.ObservableNetwork;
import edu.uci.ics.jung.graph.util.Graphs;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.layout.*;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A variation of old AddNodeDemo that animates transitions between graph algorithms.
 *
 * @author Tom Nelson
 */
public class AddNodeDemo extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(AddNodeDemo.class);

  private static final DomainModel<Point2D> domainModel = new AWTDomainModel();

  private static final long serialVersionUID = -5345319851341875800L;

  private MutableNetwork<Number, Number> g = null;

  private VisualizationViewer<Number, Number> vv = null;

  private AbstractLayoutAlgorithm<Number, Point2D> layoutAlgorithm = null;

  Timer timer;

  boolean done;

  protected JButton switchLayout;

  public static final int EDGE_LENGTH = 100;

  public AddNodeDemo() {

    MutableNetwork<Number, Number> original =
        NetworkBuilder.directed().allowsParallelEdges(true).allowsSelfLoops(true).build();
    MutableNetwork<Number, Number> ig = Graphs.synchronizedNetwork(original);
    ObservableNetwork<Number, Number> og = new ObservableNetwork<>(ig);
    if (log.isDebugEnabled()) {
      og.addGraphEventListener(evt -> log.debug("got " + evt));
    }

    this.g = og;

    layoutAlgorithm = new FRLayoutAlgorithm<>(domainModel);

    LayoutAlgorithm<Number, Point2D> staticLayoutAlgorithm =
        new StaticLayoutAlgorithm<>(domainModel);

    vv = new VisualizationViewer<>(ig, staticLayoutAlgorithm, new Dimension(600, 600));

    this.setLayout(new BorderLayout());
    this.setBackground(java.awt.Color.lightGray);
    this.setFont(new Font("Serif", Font.PLAIN, 12));

    vv.setGraphMouse(new DefaultModalGraphMouse<Number, Number>());

    vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
    vv.getRenderContext().setVertexLabelTransformer(Object::toString);
    vv.setForeground(Color.white);

    vv.addComponentListener(
        new ComponentAdapter() {

          /**
           * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
           */
          @Override
          public void componentResized(ComponentEvent arg0) {
            super.componentResized(arg0);
            log.debug("resized");
            vv.getModel().setLayoutSize(arg0.getComponent().getSize());
          }
        });

    this.add(vv);
    final JRadioButton animateChange = new JRadioButton("Animate Layout Change");
    switchLayout = new JButton("Switch to SpringLayout");
    switchLayout.addActionListener(
        ae -> {
          if (switchLayout.getText().indexOf("Spring") > 0) {
            switchLayout.setText("Switch to FRLayout");
            layoutAlgorithm = new SpringLayoutAlgorithm<>(domainModel, e -> EDGE_LENGTH);
          } else {
            switchLayout.setText("Switch to SpringLayout");
            layoutAlgorithm = new FRLayoutAlgorithm<>(domainModel);
          }
          if (animateChange.isSelected()) {
            LayoutAlgorithmTransition.animate(vv.getModel(), layoutAlgorithm);
          } else {
            LayoutAlgorithmTransition.apply(vv.getModel(), layoutAlgorithm);
          }
        });

    JPanel southPanel = new JPanel(new GridLayout(1, 2));
    southPanel.add(switchLayout);
    southPanel.add(animateChange);
    this.add(southPanel, BorderLayout.SOUTH);

    timer = new Timer();

    timer.schedule(new RemindTask(), 1000, 1000); //subsequent rate
    vv.repaint();
  }

  Integer v_prev = null;

  public void process() {

    vv.getRenderContext().getPickedVertexState().clear();
    vv.getRenderContext().getPickedEdgeState().clear();
    try {

      if (g.nodes().size() < 100) {
        //add a vertex
        Integer v1 = g.nodes().size();

        g.addNode(v1);
        vv.getRenderContext().getPickedVertexState().pick(v1, true);

        // wire it to some edges
        if (v_prev != null) {
          Integer edge = g.edges().size();
          vv.getRenderContext().getPickedEdgeState().pick(edge, true);
          g.addEdge(v_prev, v1, edge);
          // let's connect to a random vertex, too!
          int rand = (int) (Math.random() * g.nodes().size());
          edge = g.edges().size();
          vv.getRenderContext().getPickedEdgeState().pick(edge, true);
          g.addEdge(v1, rand, edge);
        }

        v_prev = v1;

        // accept the algorithm again so that it will turn off the old relaxer and start a new one
        vv.getModel().getLayoutModel().accept(layoutAlgorithm);

        vv.repaint();

      } else {
        done = true;
      }

    } catch (Exception e) {
      log.warn("exception:", e);
    }
  }

  class RemindTask extends TimerTask {

    @Override
    public void run() {
      process();
      if (done) {
        cancel();
      }
    }
  }

  public static void main(String[] args) {
    AddNodeDemo and = new AddNodeDemo();
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(and);
    frame.pack();
    frame.setVisible(true);
  }
}
