/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on May 10, 2004
 */


package edu.uci.ics.jung.samples;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.ObservableGraph;
import edu.uci.ics.jung.graph.event.GraphEvent;
import edu.uci.ics.jung.graph.event.GraphEventListener;
import edu.uci.ics.jung.graph.util.Graphs;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;

import org.apache.commons.collections15.functors.ConstantTransformer;

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

    /**
	 *
	 */
	private static final long serialVersionUID = -5345319851341875800L;

	private Graph<Number,Number> g = null;

    private VisualizationViewer<Number,Number> vv = null;

    private AbstractLayout<Number,Number> layout = null;

    Timer timer;

    boolean done;

    protected JButton switchLayout;

//    public static final LengthFunction<Number> UNITLENGTHFUNCTION = new SpringLayout.UnitLengthFunction<Number>(
//            100);
    public static final int EDGE_LENGTH = 100;

    @Override
    public void init() {

        //create a graph
    	Graph<Number,Number> ig = Graphs.<Number,Number>synchronizedDirectedGraph(new DirectedSparseMultigraph<Number,Number>());

        ObservableGraph<Number,Number> og = new ObservableGraph<Number,Number>(ig);
        og.addGraphEventListener(new GraphEventListener<Number,Number>() {

			public void handleGraphEvent(GraphEvent<Number, Number> evt) {
				System.err.println("got "+evt);

			}});
        this.g = og;
        //create a graphdraw
        layout = new FRLayout2<Number,Number>(g);
//        ((FRLayout)layout).setMaxIterations(200);

        vv = new VisualizationViewer<Number,Number>(layout, new Dimension(600,600));

        JRootPane rp = this.getRootPane();
        rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(java.awt.Color.lightGray);
        getContentPane().setFont(new Font("Serif", Font.PLAIN, 12));

        vv.getModel().getRelaxer().setSleepTime(500);
        vv.setGraphMouse(new DefaultModalGraphMouse<Number,Number>());

        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Number>());
        vv.setForeground(Color.white);
        getContentPane().add(vv);
        switchLayout = new JButton("Switch to SpringLayout");
        switchLayout.addActionListener(new ActionListener() {

            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent ae) {
            	Dimension d = new Dimension(600,600);
                if (switchLayout.getText().indexOf("Spring") > 0) {
                    switchLayout.setText("Switch to FRLayout");
                    layout = new SpringLayout<Number,Number>(g,
                        new ConstantTransformer(EDGE_LENGTH));
                    layout.setSize(d);
                    vv.getModel().setGraphLayout(layout, d);
                } else {
                    switchLayout.setText("Switch to SpringLayout");
                    layout = new FRLayout<Number,Number>(g, d);
                    vv.getModel().setGraphLayout(layout, d);
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

            if (g.getVertexCount() < 100) {
            	layout.lock(true);
                //add a vertex
                Integer v1 = new Integer(g.getVertexCount());

                Relaxer relaxer = vv.getModel().getRelaxer();
                relaxer.pause();
                g.addVertex(v1);
                System.err.println("added node " + v1);

                // wire it to some edges
                if (v_prev != null) {
                    g.addEdge(g.getEdgeCount(), v_prev, v1);
                    // let's connect to a random vertex, too!
                    int rand = (int) (Math.random() * g.getVertexCount());
                    g.addEdge(g.getEdgeCount(), v1, rand);
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
            if(done) cancel();

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