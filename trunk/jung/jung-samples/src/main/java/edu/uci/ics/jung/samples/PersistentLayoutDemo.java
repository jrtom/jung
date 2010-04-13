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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.PersistentLayout;
import edu.uci.ics.jung.visualization.layout.PersistentLayoutImpl;


/**
 * Demonstrates the use of <code>PersistentLayout</code>
 * and <code>PersistentLayoutImpl</code>.
 * 
 * @author Tom Nelson
 * 
 */
public class PersistentLayoutDemo {

    /**
     * the graph
     */
	Graph<String, Number> graph = TestGraphs.getOneComponentGraph();

    /**
     * the name of the file where the layout is saved
     */
    String fileName;

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<String,Number> vv;
    
    PersistentLayout<String,Number> persistentLayout;

    /**
     * create an instance of a simple graph with controls to
     * demo the persistence and zoom features.
     * 
     * @param fileName where to save/restore the graph positions
     */
    public PersistentLayoutDemo(final String fileName) {
        this.fileName = fileName;
        
        // create a simple graph for the demo
        persistentLayout = 
            new PersistentLayoutImpl<String,Number>(new FRLayout<String,Number>(graph));

        vv = new VisualizationViewer<String,Number>(persistentLayout);
        
        // add my listener for ToolTips
        vv.setVertexToolTipTransformer(new ToStringLabeller());
        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
        vv.setGraphMouse(gm);
        
        // create a frome to hold the graph
        final JFrame frame = new JFrame();
        frame.getContentPane().add(new GraphZoomScrollPane(vv));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // create a control panel and buttons for demo
        // functions
        JPanel p = new JPanel();
        
        JButton persist = new JButton("Save Layout");
        // saves the graph vertex positions to a file
        persist.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    persistentLayout.persist(fileName);
                } catch (IOException e1) {
                    System.err.println("got "+e1);
            	}
            }
        });
        p.add(persist);

        JButton restore = new JButton("Restore Layout");
        // restores the graph vertex positions from a file
        // if new vertices were added since the last 'persist',
        // they will be placed at random locations
        restore.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//                PersistentLayout<String,Number> pl = (PersistentLayout<String,Number>) vv.getGraphLayout();
                try {
                    persistentLayout.restore(fileName);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        p.add(restore);
        p.add(gm.getModeComboBox());

        frame.getContentPane().add(p, BorderLayout.SOUTH);
        frame.pack();//setSize(600, 600);
        frame.setVisible(true);
    }

    /**
     * a driver for this demo
     * @param args should hold the filename for the persistence demo
     */
    public static void main(String[] args) {
        String filename;
        if (args.length >= 1)
            filename = args[0];
        else
            filename = "PersistentLayoutDemo.out";
        new PersistentLayoutDemo(filename);
    }
}

