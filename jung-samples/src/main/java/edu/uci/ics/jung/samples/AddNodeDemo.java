/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on May 10, 2004
 */

package edu.uci.ics.jung.samples;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.graph.ObservableNetwork;
import edu.uci.ics.jung.graph.event.NetworkEvent;
import edu.uci.ics.jung.graph.event.NetworkEventListener;
import edu.uci.ics.jung.graph.util.Graphs;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.*;
import edu.uci.ics.jung.visualization.layout.util.Relaxer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates visualization of a graph being actively updated.
 *
 * @author danyelf
 */
public class AddNodeDemo extends JPanel {

  private static final DomainModel<Point2D> domainModel = new AWTDomainModel();

  private static final Logger log = LoggerFactory.getLogger(AddNodeDemo.class);
  /** */
  private static final long serialVersionUID = -5345319851341875800L;

  private MutableNetwork<Number, Number> g = null;

  private VisualizationViewer<Number, Number> vv = null;

  private LayoutAlgorithm<Number, Point2D> layoutAlgorithm = null;

  Timer timer;

  boolean done;

  protected JButton switchLayout;

  public static final int EDGE_LENGTH = 100;

  public AddNodeDemo() {

    //create a graph
    MutableNetwork<Number, Number> original =
        NetworkBuilder.directed().allowsParallelEdges(true).allowsSelfLoops(true).build();
    MutableNetwork<Number, Number> ig = Graphs.synchronizedNetwork(original);
    ObservableNetwork<Number, Number> og = new ObservableNetwork<Number, Number>(ig);
    og.addGraphEventListener(
        new NetworkEventListener<Number, Number>() {
          public void handleGraphEvent(NetworkEvent<Number, Number> evt) {
            switch (evt.getType()) {
              case EDGE_ADDED:
              case EDGE_REMOVED:
                NetworkEvent.Edge<Number, Number> edgeEvent =
                    (NetworkEvent.Edge<Number, Number>) evt;
                if (log.isDebugEnabled()) {
                  log.debug(
                      "got {} with endpoints:{}", evt, original.incidentNodes(edgeEvent.getEdge()));
                }
                break;
              case VERTEX_ADDED:
              case VERTEX_REMOVED:
                if (log.isDebugEnabled()) {
                  log.debug("got {}", evt);
                }
                break;
              default:
                throw new IllegalArgumentException("Unrecognized event type: " + evt);
            }
          }
        });
    this.g = og;
    layoutAlgorithm = new FRLayoutAlgorithm<>(domainModel);

    vv = new VisualizationViewer<>(g, layoutAlgorithm, new Dimension(600, 600));

    this.setLayout(new BorderLayout());
    this.setBackground(java.awt.Color.lightGray);
    this.setFont(new Font("Serif", Font.PLAIN, 12));

    vv.setGraphMouse(new DefaultModalGraphMouse<Number, Number>());

    vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
    vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
    vv.setForeground(Color.white);
    this.add(vv);
    switchLayout = new JButton("Switch to SpringLayout");
    switchLayout.addActionListener(
        new ActionListener() {

          public void actionPerformed(ActionEvent ae) {
            if (switchLayout.getText().indexOf("Spring") > 0) {
              switchLayout.setText("Switch to FRLayout");
              layoutAlgorithm = new SpringLayoutAlgorithm<>(domainModel, e -> EDGE_LENGTH);
              vv.getModel().setLayoutAlgorithm(layoutAlgorithm);
            } else {
              switchLayout.setText("Switch to SpringLayout");
              layoutAlgorithm = new FRLayoutAlgorithm<>(domainModel);
              vv.getModel().setLayoutAlgorithm(layoutAlgorithm);
            }
          }
        });

    this.add(switchLayout, BorderLayout.SOUTH);

    timer = new Timer();

    timer.schedule(new RemindTask(), 1000, 1000); //subsequent rate
  }

  Integer v_prev = null;

  public void process() {

    try {

      if (g.nodes().size() < 100) {
        Integer v1 = g.nodes().size();

        Relaxer relaxer = vv.getModel().getLayoutModel().getRelaxer();
        relaxer.pause();
        g.addNode(v1);
        if (log.isDebugEnabled()) {
          log.debug("added node {}", v1);
        }

        // wire it to some edges
        if (v_prev != null) {
          g.addEdge(v_prev, v1, g.edges().size());
          // let's connect to a random vertex, too!
          int rand = (int) (Math.random() * g.nodes().size());
          g.addEdge(v1, rand, g.edges().size());
        }

        v_prev = v1;

        vv.getModel().getLayoutModel().accept(layoutAlgorithm);
        relaxer.resume();
      } else {
        done = true;
      }

    } catch (Exception e) {
      log.debug("got exception ", e);
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
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(and);
    frame.pack();
    frame.setVisible(true);
  }
}
