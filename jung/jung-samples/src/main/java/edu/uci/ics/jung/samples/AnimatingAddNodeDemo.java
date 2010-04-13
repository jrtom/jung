/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 */


package edu.uci.ics.jung.samples;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.ObservableGraph;
import edu.uci.ics.jung.graph.event.GraphEvent;
import edu.uci.ics.jung.graph.event.GraphEventListener;
import edu.uci.ics.jung.graph.util.Graphs;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.util.Animator;

import org.apache.commons.collections15.functors.ConstantTransformer;

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
        layout = new FRLayout<Number,Number>(g);
        layout.setSize(new Dimension(600,600));
		Relaxer relaxer = new VisRunner((IterativeContext)layout);
		relaxer.stop();
		relaxer.prerelax();

		Layout<Number,Number> staticLayout =
			new StaticLayout<Number,Number>(g, layout);

        vv = new VisualizationViewer<Number,Number>(staticLayout, new Dimension(600,600));

        JRootPane rp = this.getRootPane();
        rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(java.awt.Color.lightGray);
        getContentPane().setFont(new Font("Serif", Font.PLAIN, 12));

        vv.setGraphMouse(new DefaultModalGraphMouse<Number,Number>());

        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Number>());
        vv.setForeground(Color.white);

        vv.addComponentListener(new ComponentAdapter() {

			/**
			 * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
			 */
			@Override
			public void componentResized(ComponentEvent arg0) {
				super.componentResized(arg0);
				System.err.println("resized");
				layout.setSize(arg0.getComponent().getSize());
			}});

        getContentPane().add(vv);
        switchLayout = new JButton("Switch to SpringLayout");
        switchLayout.addActionListener(new ActionListener() {

            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent ae) {
            	Dimension d = vv.getSize();//new Dimension(600,600);
                if (switchLayout.getText().indexOf("Spring") > 0) {
                    switchLayout.setText("Switch to FRLayout");
                    layout =
                    	new SpringLayout<Number,Number>(g, new ConstantTransformer(EDGE_LENGTH));
                    layout.setSize(d);
            		Relaxer relaxer = new VisRunner((IterativeContext)layout);
            		relaxer.stop();
            		relaxer.prerelax();
            		StaticLayout<Number,Number> staticLayout =
            			new StaticLayout<Number,Number>(g, layout);
    				LayoutTransition<Number,Number> lt =
    					new LayoutTransition<Number,Number>(vv, vv.getGraphLayout(),
    							staticLayout);
    				Animator animator = new Animator(lt);
    				animator.start();
    			//	vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
    				vv.repaint();

                } else {
                    switchLayout.setText("Switch to SpringLayout");
                    layout = new FRLayout<Number,Number>(g, d);
                    layout.setSize(d);
            		Relaxer relaxer = new VisRunner((IterativeContext)layout);
            		relaxer.stop();
            		relaxer.prerelax();
            		StaticLayout<Number,Number> staticLayout =
            			new StaticLayout<Number,Number>(g, layout);
    				LayoutTransition<Number,Number> lt =
    					new LayoutTransition<Number,Number>(vv, vv.getGraphLayout(),
    							staticLayout);
    				Animator animator = new Animator(lt);
    				animator.start();
    			//	vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
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

            if (g.getVertexCount() < 100) {
                //add a vertex
                Integer v1 = new Integer(g.getVertexCount());

                g.addVertex(v1);
                vv.getRenderContext().getPickedVertexState().pick(v1, true);

                // wire it to some edges
                if (v_prev != null) {
                	Integer edge = g.getEdgeCount();
                	vv.getRenderContext().getPickedEdgeState().pick(edge, true);
                    g.addEdge(edge, v_prev, v1);
                    // let's connect to a random vertex, too!
                    int rand = (int) (Math.random() * g.getVertexCount());
                    edge = g.getEdgeCount();
                	vv.getRenderContext().getPickedEdgeState().pick(edge, true);
                   g.addEdge(edge, v1, rand);
                }

                v_prev = v1;

                layout.initialize();

        		Relaxer relaxer = new VisRunner((IterativeContext)layout);
        		relaxer.stop();
        		relaxer.prerelax();
        		StaticLayout<Number,Number> staticLayout =
        			new StaticLayout<Number,Number>(g, layout);
				LayoutTransition<Number,Number> lt =
					new LayoutTransition<Number,Number>(vv, vv.getGraphLayout(),
							staticLayout);
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
            if(done) cancel();

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