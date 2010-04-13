/*
 * Copyright (c) 2008, the JUNG Project and the Regents of the University of
 * California.  All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 */
package edu.uci.ics.jung.samples;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.io.PajekNetReader;
import edu.uci.ics.jung.visualization.VisualizationViewer;

import org.apache.commons.collections15.FactoryUtils;

import java.io.IOException;

import javax.swing.JFrame;

/**
 * A class that shows the minimal work necessary to load and visualize a graph.
 */
public class SimpleGraphDraw 
{

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
     */
    public static Graph getGraph() throws IOException 
    {
        PajekNetReader pnr = new PajekNetReader(FactoryUtils.instantiateFactory(Object.class));
        Graph g = new UndirectedSparseGraph();
        
        pnr.load("src/main/resources/datasets/simple.net", g);
        return g;
    }
}
