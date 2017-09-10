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

import com.google.common.base.Functions;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.LayoutDecorator;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.graph.ObservableNetwork;
import edu.uci.ics.jung.graph.event.NetworkEvent;
import edu.uci.ics.jung.graph.event.NetworkEventListener;
import edu.uci.ics.jung.graph.util.Graphs;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JRootPane;

/**
 * Demonstrates visualization of a graph being actively updated.
 *
 * @author danyelf
 */
public class AddNodeDemo extends javax.swing.JApplet {

  /** */
  private static final long serialVersionUID = -5345319851341875800L;

  private MutableNetwork<Number, Number> g = null;

  private VisualizationViewer<Number, Number> vv = null;

  private AbstractLayout<Number> layout = null;

  Timer timer;

  boolean done;

  protected JButton switchLayout;

  //    public static final LengthFunction<Number> UNITLENGTHFUNCTION = new SpringLayout.UnitLengthFunction<Number>(
  //            100);
  public static final int EDGE_LENGTH = 100;

  @Override
  public void init() {

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
                System.err.println(
                    "got "
                        + evt
                        + " with endpoints: "
                        + original.incidentNodes(edgeEvent.getEdge()));
                break;
              case VERTEX_ADDED:
              case VERTEX_REMOVED:
                System.err.println("got " + evt);
                break;
              default:
                throw new IllegalArgumentException("Unrecognized event type: " + evt);
            }
          }
        });
    this.g = og;
    //create a graphdraw
    layout = new FRLayout2<Number>(g.asGraph());
    //        ((FRLayout)layout).setMaxIterations(200);

    vv = new VisualizationViewer<Number, Number>(g, layout, new Dimension(600, 600));

    JRootPane rp = this.getRootPane();
    rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);

    getContentPane().setLayout(new BorderLayout());
    getContentPane().setBackground(java.awt.Color.lightGray);
    getContentPane().setFont(new Font("Serif", Font.PLAIN, 12));

    vv.getModel().getRelaxer().setSleepTime(500);
    vv.setGraphMouse(new DefaultModalGraphMouse<Number, Number>());

    vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
    vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
    vv.setForeground(Color.white);
    getContentPane().add(vv);
    switchLayout = new JButton("Switch to SpringLayout");
    switchLayout.addActionListener(
        new ActionListener() {

          public void actionPerformed(ActionEvent ae) {
            Dimension d = new Dimension(600, 600);
            if (switchLayout.getText().indexOf("Spring") > 0) {
              switchLayout.setText("Switch to FRLayout");
              layout =
                  new SpringLayout<Number>(g.asGraph(), Functions.<Integer>constant(EDGE_LENGTH));
              layout.setSize(d);
              vv.getModel().setGraphLayout(layout, d);
              Layout<Number> delegateLayout =
                  ((LayoutDecorator<Number>) vv.getModel().getGraphLayout()).getDelegate();
              System.err.println("layout: " + delegateLayout.getClass().getName());
              //                    vv.repaint();
            } else {
              switchLayout.setText("Switch to SpringLayout");
              layout = new FRLayout<Number>(g.asGraph(), d);
              vv.getModel().setGraphLayout(layout, d);
              Layout<Number> delegateLayout =
                  ((LayoutDecorator<Number>) vv.getModel().getGraphLayout()).getDelegate();
              System.err.println("layout: " + delegateLayout.getClass().getName());
              //                    vv.repaint();
            }
          }
        });

    getContentPane().add(switchLayout, BorderLayout.SOUTH);

    timer = new Timer();
  }

  @Override
  public void start() {
    validate();
    //set timer so applet will change
    timer.schedule(new RemindTask(), 1000, 1000); //subsequent rate
    vv.repaint();
  }

  Integer v_prev = null;

  public void process() {

    try {

      if (g.nodes().size() < 100) {
        layout.lock(true);
        Integer v1 = g.nodes().size();

        Relaxer relaxer = vv.getModel().getRelaxer();
        relaxer.pause();
        g.addNode(v1);
        System.err.println("added node " + v1);

        // wire it to some edges
        if (v_prev != null) {
          g.addEdge(v_prev, v1, g.edges().size());
          // let's connect to a random vertex, too!
          int rand = (int) (Math.random() * g.nodes().size());
          g.addEdge(v1, rand, g.edges().size());
        }

        v_prev = v1;

        layout.initialize();
        relaxer.resume();
        layout.lock(false);
      } else {
        done = true;
      }

    } catch (Exception e) {
      System.out.println(e);
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

    and.init();
    and.start();
    frame.pack();
    frame.setVisible(true);
  }
}
