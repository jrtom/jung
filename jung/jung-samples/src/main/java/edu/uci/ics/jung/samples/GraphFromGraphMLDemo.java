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
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.xml.sax.SAXException;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.io.GraphMLReader;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.GradientVertexRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer.InsidePositioner;


/**
 * Demonstrates loading (and visualizing) a graph from a GraphML file.
 * 
 * @author Tom Nelson
 * 
 */
public class GraphFromGraphMLDemo {

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<Number, Number> vv;
    
    /**
     * create an instance of a simple graph with controls to
     * demo the zoom features.
     * @throws SAXException 
     * @throws ParserConfigurationException 
     * @throws IOException 
     * 
     */
    public GraphFromGraphMLDemo(String filename) throws ParserConfigurationException, SAXException, IOException {
        
    	Factory<Number> vertexFactory = new Factory<Number>() {
    		int n = 0;
    		public Number create() { return n++; }
    	};
    	Factory<Number> edgeFactory = new Factory<Number>() {
    		int n = 0;
    		public Number create() { return n++; }
    	};
    	
    	GraphMLReader<DirectedGraph<Number,Number>, Number, Number> gmlr = 
    	    new GraphMLReader<DirectedGraph<Number,Number>, Number, Number>(vertexFactory, edgeFactory);
    	final DirectedGraph<Number,Number> graph = new DirectedSparseMultigraph<Number,Number>();
    	gmlr.load(filename, graph);
    	
        // create a simple graph for the demo
        vv =  new VisualizationViewer<Number,Number>(new FRLayout<Number,Number>(graph));

        vv.addGraphMouseListener(new TestGraphMouseListener<Number>());
        vv.getRenderer().setVertexRenderer(
        		new GradientVertexRenderer<Number,Number>(
        				Color.white, Color.red, 
        				Color.white, Color.blue,
        				vv.getPickedVertexState(),
        				false));
        
        // add my listeners for ToolTips
        vv.setVertexToolTipTransformer(new ToStringLabeller<Number>());
        vv.setEdgeToolTipTransformer(new Transformer<Number,String>() {
			public String transform(Number edge) {
				return "E"+graph.getEndpoints(edge).toString();
			}});
        
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Number>());
        vv.getRenderer().getVertexLabelRenderer().setPositioner(new InsidePositioner());
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.AUTO);
        
        // create a frome to hold the graph
        final JFrame frame = new JFrame();
        Container content = frame.getContentPane();
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        content.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final AbstractModalGraphMouse graphMouse = new DefaultModalGraphMouse<Number,Number>();
        vv.setGraphMouse(graphMouse);
        vv.addKeyListener(graphMouse.getModeKeyListener());

        JMenuBar menubar = new JMenuBar();
        menubar.add(graphMouse.getModeMenu());
        panel.setCorner(menubar);

        
        vv.addKeyListener(graphMouse.getModeKeyListener());
        vv.setToolTipText("<html><center>Type 'p' for Pick mode<p>Type 't' for Transform mode");
        
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
        content.add(controls, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }
    
    /**
     * A nested class to demo the GraphMouseListener finding the
     * right vertices after zoom/pan
     */
    static class TestGraphMouseListener<V> implements GraphMouseListener<V> {
        
    		public void graphClicked(V v, MouseEvent me) {
    		    System.err.println("Vertex "+v+" was clicked at ("+me.getX()+","+me.getY()+")");
    		}
    		public void graphPressed(V v, MouseEvent me) {
    		    System.err.println("Vertex "+v+" was pressed at ("+me.getX()+","+me.getY()+")");
    		}
    		public void graphReleased(V v, MouseEvent me) {
    		    System.err.println("Vertex "+v+" was released at ("+me.getX()+","+me.getY()+")");
    		}
    }

    /**
     * a driver for this demo
     * @throws IOException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     */
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException 
    {
    	String filename = "simple.graphml";
    	if(args.length > 0) filename = args[0];
        new GraphFromGraphMLDemo(filename);
    }
}
