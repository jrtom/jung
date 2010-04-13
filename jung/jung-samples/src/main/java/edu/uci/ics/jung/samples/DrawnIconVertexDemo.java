/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 * 
 */
package edu.uci.ics.jung.samples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;

/**
 * A demo that shows drawn Icons as vertices
 * 
 * @author Tom Nelson 
 * 
 */
public class DrawnIconVertexDemo {

    /**
     * the graph
     */
    Graph<Integer,Number> graph;

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<Integer,Number> vv;
    
    public DrawnIconVertexDemo() {
        
        // create a simple graph for the demo
        graph = new DirectedSparseGraph<Integer,Number>();
        Integer[] v = createVertices(10);
        createEdges(v);
        
        vv =  new VisualizationViewer<Integer,Number>(new FRLayout<Integer,Number>(graph));
        vv.getRenderContext().setVertexLabelTransformer(new Transformer<Integer,String>(){

			public String transform(Integer v) {
				return "Vertex "+v;
			}});
        vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.cyan));
        vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.cyan));

        vv.getRenderContext().setVertexIconTransformer(new Transformer<Integer,Icon>() {

        	/*
        	 * Implements the Icon interface to draw an Icon with background color and
        	 * a text label
        	 */
			public Icon transform(final Integer v) {
				return new Icon() {

					public int getIconHeight() {
						return 20;
					}

					public int getIconWidth() {
						return 20;
					}

					public void paintIcon(Component c, Graphics g,
							int x, int y) {
						if(vv.getPickedVertexState().isPicked(v)) {
							g.setColor(Color.yellow);
						} else {
							g.setColor(Color.red);
						}
						g.fillOval(x, y, 20, 20);
						if(vv.getPickedVertexState().isPicked(v)) {
							g.setColor(Color.black);
						} else {
							g.setColor(Color.white);
						}
						g.drawString(""+v, x+6, y+15);
						
					}};
			}});

        vv.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<Integer>(vv.getPickedVertexState(), Color.white,  Color.yellow));
        vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<Number>(vv.getPickedEdgeState(), Color.black, Color.lightGray));

        vv.setBackground(Color.white);

        // add my listener for ToolTips
        vv.setVertexToolTipTransformer(new ToStringLabeller<Integer>());
        
        // create a frome to hold the graph
        final JFrame frame = new JFrame();
        Container content = frame.getContentPane();
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        content.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        final ModalGraphMouse gm = new DefaultModalGraphMouse<Integer,Number>();
        vv.setGraphMouse(gm);
        
        final ScalingControl scaler = new CrossoverScalingControl();

        JButton plus = new JButton("+");
        plus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1.1f, vv.getCenter());
            }
        });
        JButton minus = new JButton("-");
        minus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1/1.1f, vv.getCenter());
            }
        });

        JPanel controls = new JPanel();
        controls.add(plus);
        controls.add(minus);
        controls.add(((DefaultModalGraphMouse<Integer,Number>) gm).getModeComboBox());
        content.add(controls, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }
    
    
    /**
     * create some vertices
     * @param count how many to create
     * @return the Vertices in an array
     */
    private Integer[] createVertices(int count) {
        Integer[] v = new Integer[count];
        for (int i = 0; i < count; i++) {
            v[i] = new Integer(i);
            graph.addVertex(v[i]);
        }
        return v;
    }

    /**
     * create edges for this demo graph
     * @param v an array of Vertices to connect
     */
    void createEdges(Integer[] v) {
        graph.addEdge(new Double(Math.random()), v[0], v[1], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[0], v[3], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[0], v[4], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[4], v[5], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[3], v[5], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[1], v[2], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[1], v[4], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[8], v[2], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[3], v[8], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[6], v[7], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[7], v[5], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[0], v[9], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[9], v[8], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[7], v[6], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[6], v[5], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[4], v[2], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[5], v[4], EdgeType.DIRECTED);
    }

    /**
     * a driver for this demo
     */
    public static void main(String[] args) 
    {
        new DrawnIconVertexDemo();
    }
}
