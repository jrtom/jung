/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * 
 */
package edu.uci.ics.jung.samples;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.GradientVertexRenderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelAsShapeRenderer;


/**
 * This demo shows how to use the vertex labels themselves as 
 * the vertex shapes. Additionally, it shows html labels
 * so they are multi-line, and gradient painting of the
 * vertex labels.
 * 
 * @author Tom Nelson
 * 
 */
public class VertexLabelAsShapeDemo extends JApplet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1017336668368978842L;

    Graph<String,Number> graph;

    VisualizationViewer<String,Number> vv;
    
    Layout<String,Number> layout;
    
    /**
     * create an instance of a simple graph with basic controls
     */
    public VertexLabelAsShapeDemo() {
        
        // create a simple graph for the demo
        graph = TestGraphs.getOneComponentGraph();
        
        layout = new FRLayout<String,Number>(graph);

        Dimension preferredSize = new Dimension(400,400);
        final VisualizationModel<String,Number> visualizationModel = 
            new DefaultVisualizationModel<String,Number>(layout, preferredSize);
        vv =  new VisualizationViewer<String,Number>(visualizationModel, preferredSize);
        
        // this class will provide both label drawing and vertex shapes
        VertexLabelAsShapeRenderer<String,Number> vlasr = new VertexLabelAsShapeRenderer<String,Number>(vv.getRenderContext());
        
        // customize the render context
        vv.getRenderContext().setVertexLabelTransformer(
        		// this chains together Functions so that the html tags
        		// are prepended to the toString method output
        		Functions.<Object,String,String>compose(
        				new Function<String,String>(){
							public String apply(String input) {
								return "<html><center>Vertex<p>"+input;
							}}, new ToStringLabeller()));
        vv.getRenderContext().setVertexShapeTransformer(vlasr);
        vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.red));
        vv.getRenderContext().setEdgeDrawPaintTransformer(Functions.<Paint>constant(Color.yellow));
        vv.getRenderContext().setEdgeStrokeTransformer(Functions.<Stroke>constant(new BasicStroke(2.5f)));
        
        // customize the renderer
        vv.getRenderer().setVertexRenderer(new GradientVertexRenderer<String,Number>(Color.gray, Color.white, true));
        vv.getRenderer().setVertexLabelRenderer(vlasr);

        vv.setBackground(Color.black);
        
        // add a listener for ToolTips
        vv.setVertexToolTipTransformer(new ToStringLabeller());
        
        final DefaultModalGraphMouse<String,Number> graphMouse = 
            new DefaultModalGraphMouse<String,Number>();

        vv.setGraphMouse(graphMouse);
        vv.addKeyListener(graphMouse.getModeKeyListener());
        
        Container content = getContentPane();
        GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
        content.add(gzsp);
        
        JComboBox<?> modeBox = graphMouse.getModeComboBox();
        modeBox.addItemListener(graphMouse.getModeListener());
        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        
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
        JPanel zoomControls = new JPanel(new GridLayout(2,1));
        zoomControls.setBorder(BorderFactory.createTitledBorder("Zoom"));
        zoomControls.add(plus);
        zoomControls.add(minus);
        controls.add(zoomControls);
        controls.add(modeBox);
        content.add(controls, BorderLayout.SOUTH);
    }
    
    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(new VertexLabelAsShapeDemo());
        f.pack();
        f.setVisible(true);
    }
}


