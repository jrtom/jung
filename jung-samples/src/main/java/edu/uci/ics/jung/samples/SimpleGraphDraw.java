/*
 * Copyright (c) 2008, The JUNG Authors
 * All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.samples;

import java.io.IOException;

import javax.swing.JFrame;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.io.PajekNetReader;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * A class that shows the minimal work necessary to load and visualize a graph.
 */
public class SimpleGraphDraw 
{

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) throws IOException 
    {
        JFrame jf = new JFrame();
		Graph g = getGraph();
        VisualizationViewer vv = new VisualizationViewer(new FRLayout(g));
        jf.getContentPane().add(vv);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.pack();
        jf.setVisible(true);
    }
    
    /**
     * Generates a graph: in this case, reads it from the file
     * "samples/datasetsgraph/simple.net"
     * @return A sample undirected graph
     * @throws IOException if there is an error in reading the file
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static Graph getGraph() throws IOException 
    {
        PajekNetReader pnr = new PajekNetReader(new Supplier(){
			public Object get() {
				return new Object();
			}});
        Graph g = new UndirectedSparseGraph();
        
        pnr.load("src/main/resources/datasets/simple.net", g);
        return g;
    }
}
