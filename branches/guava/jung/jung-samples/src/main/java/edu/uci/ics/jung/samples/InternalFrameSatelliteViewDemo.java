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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.SatelliteVisualizationViewer;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;

/**
 * Similar to the SatelliteViewDemo, but using JInternalFrame.
 * 
 * @author Tom Nelson
 * 
 */
public class InternalFrameSatelliteViewDemo {

    static final String instructions = 
        "<html>"+
        "<b><h2><center>Instructions for Mouse Listeners</center></h2></b>"+
        "<p>There are two modes, Transforming and Picking."+
        "<p>The modes are selected with a toggle button."+
        
        "<p><p><b>Transforming Mode:</b>"+
        "<ul>"+
        "<li>Mouse1+drag pans the graph"+
        "<li>Mouse1+Shift+drag rotates the graph"+
        "<li>Mouse1+CTRL(or Command)+drag shears the graph"+
        "</ul>"+
        
        "<b>Picking Mode:</b>"+
        "<ul>"+
        "<li>Mouse1 on a Vertex selects the vertex"+
        "<li>Mouse1 elsewhere unselects all Vertices"+
        "<li>Mouse1+Shift on a Vertex adds/removes Vertex selection"+
        "<li>Mouse1+drag on a Vertex moves all selected Vertices"+
        "<li>Mouse1+drag elsewhere selects Vertices in a region"+
        "<li>Mouse1+Shift+drag adds selection of Vertices in a new region"+
        "<li>Mouse1+CTRL on a Vertex selects the vertex and centers the display on it"+
        "</ul>"+
       "<b>Both Modes:</b>"+
       "<ul>"+
        "<li>Mousewheel scales the layout &gt; 1 and scales the view &lt; 1";
    
    /**
     * the graph
     */
    Graph<String,Number> graph;

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<String,Number> vv;
    VisualizationViewer<String,Number> satellite;
    
    JInternalFrame dialog;
    
    JDesktopPane desktop;

    /**
     * create an instance of a simple graph with controls to
     * demo the zoom features.
     * 
     */
    public InternalFrameSatelliteViewDemo() {
        
        // create a simple graph for the demo
        graph = TestGraphs.getOneComponentGraph();

        Layout<String,Number> layout = new ISOMLayout<String,Number>(graph);

        vv = new VisualizationViewer<String,Number>(layout, new Dimension(600,600));
        vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<Number>(vv.getPickedEdgeState(), Color.black, Color.cyan));
        vv.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<String>(vv.getPickedVertexState(), Color.red, Color.yellow));

        // add my listener for ToolTips
        vv.setVertexToolTipTransformer(new ToStringLabeller());
        final ModalGraphMouse graphMouse = new DefaultModalGraphMouse();
        vv.setGraphMouse(graphMouse);

        satellite =
            new SatelliteVisualizationViewer<String,Number>(vv, new Dimension(200,200));
        satellite.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<Number>(satellite.getPickedEdgeState(), Color.black, Color.cyan));
        satellite.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<String>(satellite.getPickedVertexState(), Color.red, Color.yellow));

        ScalingControl satelliteScaler = new CrossoverScalingControl();
        satellite.scaleToLayout(satelliteScaler);
        
        JFrame frame = new JFrame();
        desktop = new JDesktopPane();
        Container content = frame.getContentPane();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(desktop);
        content.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JInternalFrame vvFrame = new JInternalFrame();
        vvFrame.getContentPane().add(vv);
        vvFrame.pack();
        vvFrame.setVisible(true); //necessary as of 1.3
        desktop.add(vvFrame);
        try {
            vvFrame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {}

        dialog = new JInternalFrame();
        desktop.add(dialog);
        content = dialog.getContentPane();
        
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
        JButton dismiss = new JButton("Dismiss");
        dismiss.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        });
        JButton help = new JButton("Help");
        help.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showInternalMessageDialog(dialog, instructions,
                        "Instructions", JOptionPane.PLAIN_MESSAGE);
            }
        });
        JPanel controls = new JPanel(new GridLayout(2,2));
        controls.add(plus);
        controls.add(minus);
        controls.add(dismiss);
        controls.add(help);
        content.add(satellite);
        content.add(controls, BorderLayout.SOUTH);
         
        JButton zoomer = new JButton("Show Satellite View");
        zoomer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.pack();
                dialog.setLocation(desktop.getWidth()-dialog.getWidth(),0);
                dialog.show();
                try {
                    dialog.setSelected(true);
                } catch (java.beans.PropertyVetoException ex) {}
            }
        });

        JComboBox modeBox = ((DefaultModalGraphMouse)graphMouse).getModeComboBox();
        modeBox.addItemListener(((ModalGraphMouse)satellite.getGraphMouse()).getModeListener());
        JPanel p = new JPanel();
        p.add(zoomer);
        p.add(modeBox);

        frame.getContentPane().add(p, BorderLayout.SOUTH);
        frame.setSize(800, 800);
        frame.setVisible(true);
    }

    /**
     * a driver for this demo
     */
    public static void main(String[] args) {
        new InternalFrameSatelliteViewDemo();
    }
}

