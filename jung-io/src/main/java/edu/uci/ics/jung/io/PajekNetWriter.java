/*
 * Created on May 4, 2004
 *
 * Copyright (c) 2004, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.io;

import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.google.common.base.Function;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Writes graphs in the Pajek NET format.
 * 
 * <p>Labels for vertices, edge weights, and vertex locations may each optionally
 * be specified.  Note that vertex location coordinates 
 * must be normalized to the interval [0, 1] on each axis in order to conform to the 
 * Pajek specification.
 * 
 * @author Joshua O'Madadhain
 * @author Tom Nelson - converted to jung2
 */
public class PajekNetWriter<V,E>
{
    /**
     * Creates a new instance.
     */
    public PajekNetWriter()
    {
    }

    /**
     * Saves the graph to the specified file.
     * @param g the graph to be saved
     * @param filename the filename of the file to write the graph to
     * @param vs mapping from vertices to labels
     * @param nev mapping from edges to weights
     * @param vld mapping from vertices to locations
     * @throws IOException if the graph cannot be saved
     */
    public void save(Graph<V,E> g, String filename, Function<V,String> vs, 
            Function<E,Number> nev, Function<V,Point2D> vld) throws IOException
    {
        save(g, new FileWriter(filename), vs, nev, vld);
    }
    
    /**
     * Saves the graph to the specified file.
     * @param g the graph to be saved
     * @param filename the filename of the file to write the graph to
     * @param vs mapping from vertices to labels
     * @param nev mapping from edges to weights
     * @throws IOException if the graph cannot be saved
     */
    public void save(Graph<V,E> g, String filename, Function<V,String> vs, 
            Function<E,Number> nev) throws IOException
    {
        save(g, new FileWriter(filename), vs, nev, null);
    }
    
    /**
     * Saves the graph to the specified file.  No vertex labels are written, and the 
     * edge weights are written as 1.0.
     * @param g the graph to be saved
     * @param filename the filename of the file to write the graph to
     * @throws IOException if the graph cannot be saved
     */
    public void save(Graph<V,E> g, String filename) throws IOException
    {
        save(g, filename, null, null, null);
    }

    /**
     * Saves the graph to the specified writer.  No vertex labels are written, and the 
     * edge weights are written as 1.0.
     * @param g the graph to be saved
     * @param w the writer instance to write the graph to
     * @throws IOException if the graph cannot be saved
     */
    public void save(Graph<V,E> g, Writer w) throws IOException
    {
        save(g, w, null, null, null);
    }

    /**
     * Saves the graph to the specified writer.
     * @param g the graph to be saved
     * @param w the writer instance to write the graph to
     * @param vs mapping from vertices to labels
     * @param nev mapping from edges to weights
     * @throws IOException if the graph cannot be saved
     */
    public void save(Graph<V,E> g, Writer w, Function<V,String> vs, 
            Function<E,Number> nev) throws IOException
    {
        save(g, w, vs, nev, null);
    }
    
    /**
     * Saves the graph to the specified writer.
     * @param graph the graph to be saved
     * @param w the writer instance to write the graph to
     * @param vs mapping from vertices to labels (no labels are written if null)
     * @param nev mapping from edges to weights (defaults to weights of 1.0 if null)
     * @param vld mapping from vertices to locations (no locations are written if null)
     * @throws IOException if the graph cannot be saved
     */
	public void save(Graph<V,E> graph, Writer w, Function<V,String> vs, 
	        Function<E,Number> nev, Function<V,Point2D> vld) throws IOException
    {
        /*
         * TODO: Changes we might want to make:
         * - optionally writing out in list form
         */
        
        BufferedWriter writer = new BufferedWriter(w);
        if (nev == null)
            nev = new Function<E, Number>() { public Number apply(E e) { return 1; } };
        writer.write("*Vertices " + graph.getVertexCount());
        writer.newLine();
        
        List<V> id = new ArrayList<V>(graph.getVertices());
        for (V currentVertex : graph.getVertices())
        {
            // convert from 0-based to 1-based index
            int v_id = id.indexOf(currentVertex) + 1;
            writer.write(""+v_id); 
            if (vs != null)
            { 
                String label = vs.apply(currentVertex);
                if (label != null)
                    writer.write (" \"" + label + "\"");
            }
            if (vld != null)
            {
                Point2D location = vld.apply(currentVertex);
                if (location != null)
                    writer.write (" " + location.getX() + " " + location.getY() + " 0.0");
            }
            writer.newLine();
        }

        Collection<E> d_set = new HashSet<E>();
        Collection<E> u_set = new HashSet<E>();

        boolean directed = graph instanceof DirectedGraph;

        boolean undirected = graph instanceof UndirectedGraph;

        // if it's strictly one or the other, no need to create extra sets
        if (directed)
            d_set.addAll(graph.getEdges());
        if (undirected)
            u_set.addAll(graph.getEdges());
        if (!directed && !undirected) // mixed-mode graph
        {
        	u_set.addAll(graph.getEdges());
        	d_set.addAll(graph.getEdges());
        	for(E e : graph.getEdges()) {
        		if(graph.getEdgeType(e) == EdgeType.UNDIRECTED) {
        			d_set.remove(e);
        		} else {
        			u_set.remove(e);
        		}
        	}
        }

        // write out directed edges
        if (!d_set.isEmpty())
        {
            writer.write("*Arcs");
            writer.newLine();
        }
        for (E e : d_set)
        {
            int source_id = id.indexOf(graph.getEndpoints(e).getFirst()) + 1;
            int target_id = id.indexOf(graph.getEndpoints(e).getSecond()) + 1;
            float weight = nev.apply(e).floatValue();
            writer.write(source_id + " " + target_id + " " + weight);
            writer.newLine();
        }

        // write out undirected edges
        if (!u_set.isEmpty())
        {
            writer.write("*Edges");
            writer.newLine();
        }
        for (E e : u_set)
        {
            Pair<V> endpoints = graph.getEndpoints(e);
            int v1_id = id.indexOf(endpoints.getFirst()) + 1;
            int v2_id = id.indexOf(endpoints.getSecond()) + 1;
            float weight = nev.apply(e).floatValue();
            writer.write(v1_id + " " + v2_id + " " + weight);
            writer.newLine();
        }
        writer.close();
    }
}
