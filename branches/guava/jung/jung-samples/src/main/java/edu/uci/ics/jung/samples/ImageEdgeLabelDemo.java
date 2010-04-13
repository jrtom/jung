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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;

/**
 * Demonstrates the use of images on graph edge labels.
 * 
 * @author Tom Nelson
 * 
 */
public class ImageEdgeLabelDemo extends JApplet {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4332663871914930864L;
	
	private static final int VERTEX_COUNT=11;

	/**
     * the graph
     */
    DirectedGraph<Number, Number> graph;

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<Number, Number> vv;
    
    public ImageEdgeLabelDemo() {
        
        // create a simple graph for the demo
        graph = new DirectedSparseMultigraph<Number,Number>();
        createGraph(VERTEX_COUNT);
        
        FRLayout<Number, Number> layout = new FRLayout<Number, Number>(graph);
        layout.setMaxIterations(100);
        vv =  new VisualizationViewer<Number, Number>(layout, new Dimension(400,400));
        
        vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<Number>(vv.getPickedEdgeState(), Color.black, Color.cyan));

        vv.setBackground(Color.white);
        
        vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.cyan));
        vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.cyan));
        vv.getRenderContext().setEdgeLabelTransformer(new Transformer<Number,String>() {
        	URL url = getClass().getResource("/images/lightning-s.gif");
			public String transform(Number input) {
				return "<html><img src="+url+" height=10 width=21>";
			}});
        
        // add a listener for ToolTips
        vv.setVertexToolTipTransformer(new ToStringLabeller<Number>());
        vv.setEdgeToolTipTransformer(new ToStringLabeller<Number>());
        Container content = getContentPane();
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        content.add(panel);
        
        final DefaultModalGraphMouse<Number, Number> graphMouse = new DefaultModalGraphMouse<Number, Number>();
        vv.setGraphMouse(graphMouse);
        vv.addKeyListener(graphMouse.getModeKeyListener());
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

        JComboBox modeBox = graphMouse.getModeComboBox();
        JPanel modePanel = new JPanel();
        modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
        modePanel.add(modeBox);
        
        JPanel scaleGrid = new JPanel(new GridLayout(1,0));
        scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));
        JPanel controls = new JPanel();
        scaleGrid.add(plus);
        scaleGrid.add(minus);
        controls.add(scaleGrid);
        controls.add(modePanel);
        content.add(controls, BorderLayout.SOUTH);
    }
    
    /**
     * create some vertices
     * @param count how many to create
     * @return the Vertices in an array
     */
    private void createGraph(int vertexCount) {
        for (int i = 0; i < vertexCount; i++) {
            graph.addVertex(i);
        }
    	int j=0;
        graph.addEdge(j++, 0, 1, EdgeType.DIRECTED);
        graph.addEdge(j++, 3, 0, EdgeType.DIRECTED);
        graph.addEdge(j++, 0, 4, EdgeType.DIRECTED);
        graph.addEdge(j++, 4, 5, EdgeType.DIRECTED);
        graph.addEdge(j++, 5, 3, EdgeType.DIRECTED);
        graph.addEdge(j++, 2, 1, EdgeType.DIRECTED);
        graph.addEdge(j++, 4, 1, EdgeType.DIRECTED);
        graph.addEdge(j++, 8, 2, EdgeType.DIRECTED);
        graph.addEdge(j++, 3, 8, EdgeType.DIRECTED);
        graph.addEdge(j++, 6, 7, EdgeType.DIRECTED);
        graph.addEdge(j++, 7, 5, EdgeType.DIRECTED);
        graph.addEdge(j++, 0, 9, EdgeType.DIRECTED);
        graph.addEdge(j++, 9, 8, EdgeType.DIRECTED);
        graph.addEdge(j++, 7, 6, EdgeType.DIRECTED);
        graph.addEdge(j++, 6, 5, EdgeType.DIRECTED);
        graph.addEdge(j++, 4, 2, EdgeType.DIRECTED);
        graph.addEdge(j++, 5, 4, EdgeType.DIRECTED);
        graph.addEdge(j++, 4, 10, EdgeType.DIRECTED);
        graph.addEdge(j++, 10, 4, EdgeType.DIRECTED);
    }

    /**
	 * a driver for this demo
	 */
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        Container content = frame.getContentPane();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        content.add(new ImageEdgeLabelDemo());
        frame.pack();
        frame.setVisible(true);
    }
}
