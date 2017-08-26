/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */

package edu.uci.ics.jung.samples;

import com.google.common.base.Functions;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.ObservableNetwork;
import edu.uci.ics.jung.graph.event.NetworkEvent;
import edu.uci.ics.jung.graph.event.NetworkEventListener;
import edu.uci.ics.jung.graph.util.Graphs;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.util.Animator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JRootPane;

/**
 * A variation of AddNodeDemo that animates transitions between graph states.
 *
 * @author Tom Nelson
 */
public class AnimatingAddNodeDemo extends javax.swing.JApplet {

  /** */
  private static final long serialVersionUID = -5345319851341875800L;

  private MutableNetwork<Number, Number> g = null;

  private VisualizationViewer<Number, Number> vv = null;

  private AbstractLayout<Number> layout = null;

  Timer timer;

  boolean done;

  protected JButton switchLayout;

  public static final int EDGE_LENGTH = 100;

  @Override
  public void init() {

    //create a graph
    MutableNetwork<Number, Number> original =
        NetworkBuilder.directed().allowsParallelEdges(true).build();
    MutableNetwork<Number, Number> ig = Graphs.synchronizedNetwork(original);
    ObservableNetwork<Number, Number> og = new ObservableNetwork<Number, Number>(ig);
    og.addGraphEventListener(
        new NetworkEventListener<Number, Number>() {

          public void handleGraphEvent(NetworkEvent<Number, Number> evt) {
            System.err.println("got " + evt);
          }
        });
    this.g = og;
    //create a graphdraw
    layout = new FRLayout<Number>(g.asGraph());
    layout.setSize(new Dimension(600, 600));
    Relaxer relaxer = new VisRunner((IterativeContext) layout);
    relaxer.stop();
    relaxer.prerelax();

    Layout<Number> staticLayout = new StaticLayout<Number>(g.asGraph(), layout);

    vv = new VisualizationViewer<Number, Number>(ig, staticLayout, new Dimension(600, 600));

    JRootPane rp = this.getRootPane();
    rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);

    getContentPane().setLayout(new BorderLayout());
    getContentPane().setBackground(java.awt.Color.lightGray);
    getContentPane().setFont(new Font("Serif", Font.PLAIN, 12));

    vv.setGraphMouse(new DefaultModalGraphMouse<Number, Number>());

    vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
    vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
    vv.setForeground(Color.white);

    vv.addComponentListener(
        new ComponentAdapter() {

          /**
           * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
           */
          @Override
          public void componentResized(ComponentEvent arg0) {
            super.componentResized(arg0);
            System.err.println("resized");
            layout.setSize(arg0.getComponent().getSize());
          }
        });

    getContentPane().add(vv);
    switchLayout = new JButton("Switch to SpringLayout");
    switchLayout.addActionListener(
        new ActionListener() {

          public void actionPerformed(ActionEvent ae) {
            Dimension d = vv.getSize(); //new Dimension(600,600);
            if (switchLayout.getText().indexOf("Spring") > 0) {
              switchLayout.setText("Switch to FRLayout");
              layout = new SpringLayout<Number>(g.asGraph(), Functions.constant(EDGE_LENGTH));
              layout.setSize(d);
              Relaxer relaxer = new VisRunner((IterativeContext) layout);
              relaxer.stop();
              relaxer.prerelax();
              StaticLayout<Number> staticLayout = new StaticLayout<Number>(g.asGraph(), layout);
              LayoutTransition<Number, Number> lt =
                  new LayoutTransition<Number, Number>(vv, vv.getGraphLayout(), staticLayout);
              Animator animator = new Animator(lt);
              animator.start();
              vv.repaint();

            } else {
              switchLayout.setText("Switch to SpringLayout");
              layout = new FRLayout<Number>(g.asGraph(), d);
              layout.setSize(d);
              Relaxer relaxer = new VisRunner((IterativeContext) layout);
              relaxer.stop();
              relaxer.prerelax();
              StaticLayout<Number> staticLayout = new StaticLayout<Number>(g.asGraph(), layout);
              LayoutTransition<Number, Number> lt =
                  new LayoutTransition<Number, Number>(vv, vv.getGraphLayout(), staticLayout);
              Animator animator = new Animator(lt);
              animator.start();
              vv.repaint();
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

    vv.getRenderContext().getPickedVertexState().clear();
    vv.getRenderContext().getPickedEdgeState().clear();
    try {

      if (g.nodes().size() < 100) {
        //add a vertex
        Integer v1 = new Integer(g.nodes().size());

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

        layout.initialize();

        Relaxer relaxer = new VisRunner((IterativeContext) layout);
        relaxer.stop();
        relaxer.prerelax();
        StaticLayout<Number> staticLayout = new StaticLayout<Number>(g.asGraph(), layout);
        LayoutTransition<Number, Number> lt =
            new LayoutTransition<Number, Number>(vv, vv.getGraphLayout(), staticLayout);
        Animator animator = new Animator(lt);
        animator.start();
        //				vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
        vv.repaint();

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
    AnimatingAddNodeDemo and = new AnimatingAddNodeDemo();
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(and);

    and.init();
    and.start();
    frame.pack();
    frame.setVisible(true);
  }
}
