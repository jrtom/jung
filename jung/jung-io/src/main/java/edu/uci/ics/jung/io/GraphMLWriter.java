/*
 * Created on June 16, 2008
 *
 * Copyright (c) 2008, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.uci.ics.jung.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.TransformerUtils;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Writes graphs out in GraphML format.
 *
 * Current known issues: 
 * <ul>
 * <li/>Only supports one graph per output file.
 * <li/>Does not indent lines for text-format readability.
 * </ul>
 * 
 */
public class GraphMLWriter<V,E> 
{
    protected Transformer<V, String> vertex_ids;
    protected Transformer<E, String> edge_ids;
    protected Map<String, GraphMLMetadata<Hypergraph<V,E>>> graph_data;
    protected Map<String, GraphMLMetadata<V>> vertex_data;
    protected Map<String, GraphMLMetadata<E>> edge_data;
    protected Transformer<V, String> vertex_desc;
    protected Transformer<E, String> edge_desc;
    protected Transformer<Hypergraph<V,E>, String> graph_desc;
	protected boolean directed;
	protected int nest_level;
    
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public GraphMLWriter() 
	{
	    vertex_ids = new Transformer<V,String>()
	    { 
	        public String transform(V v) 
	        { 
	            return v.toString(); 
	        }
	    };
	    edge_ids = TransformerUtils.nullTransformer();
	    graph_data = Collections.emptyMap();
        vertex_data = Collections.emptyMap();
        edge_data = Collections.emptyMap();
        vertex_desc = TransformerUtils.nullTransformer();
        edge_desc = TransformerUtils.nullTransformer();
        graph_desc = TransformerUtils.nullTransformer();
        nest_level = 0;
	}
	
	
	/**
	 * 
	 * @param graph
	 * @param w
	 * @throws IOException 
	 */
	public void save(Hypergraph<V,E> graph, Writer w) throws IOException
	{
		BufferedWriter bw = new BufferedWriter(w);

		// write out boilerplate header
		bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		bw.write("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns/graphml\"\n" +
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  \n");
		bw.write("xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns/graphml\">\n");
		
		// write out data specifiers, including defaults
		for (String key : graph_data.keySet())
			writeKeySpecification(key, "graph", graph_data.get(key), bw);
		for (String key : vertex_data.keySet())
			writeKeySpecification(key, "node", vertex_data.get(key), bw);
		for (String key : edge_data.keySet())
			writeKeySpecification(key, "edge", edge_data.get(key), bw);

		// write out graph-level information
		// set edge default direction
		bw.write("<graph edgedefault=\"");
		directed = !(graph instanceof UndirectedGraph);
        if (directed)
            bw.write("directed\">\n");
        else 
            bw.write("undirected\">\n");

        // write graph description, if any
		String desc = graph_desc.transform(graph);
		if (desc != null)
			bw.write("<desc>" + desc + "</desc>\n");
		
		// write graph data out if any
		for (String key : graph_data.keySet())
		{
			Transformer<Hypergraph<V,E>, ?> t = graph_data.get(key).transformer;
			Object value = t.transform(graph);
			if (value != null)
				bw.write(format("data", "key", key, value.toString()) + "\n");
		}
        
		// write vertex information
        writeVertexData(graph, bw);
		
		// write edge information
        writeEdgeData(graph, bw);

        // close graph
        bw.write("</graph>\n");
        bw.write("</graphml>\n");
        bw.flush();
        
        bw.close();
	}

//	public boolean save(Collection<Hypergraph<V,E>> graphs, Writer w)
//	{
//		return true;
//	}

	protected void writeIndentedText(BufferedWriter w, String to_write) throws IOException
	{
	    for (int i = 0; i < nest_level; i++)
	        w.write("  ");
	    w.write(to_write);
	}
	
	protected void writeVertexData(Hypergraph<V,E> graph, BufferedWriter w) throws IOException
	{
		for (V v: graph.getVertices())
		{
			String v_string = String.format("<node id=\"%s\"", vertex_ids.transform(v));
			boolean closed = false;
			// write description out if any
			String desc = vertex_desc.transform(v);
			if (desc != null)
			{
				w.write(v_string + ">\n");
				closed = true;
				w.write("<desc>" + desc + "</desc>\n");
			}
			// write data out if any
			for (String key : vertex_data.keySet())
			{
				Transformer<V, ?> t = vertex_data.get(key).transformer;
				if (t != null)
				{
    				Object value = t.transform(v);
    				if (value != null)
    				{
    					if (!closed)
    					{
    						w.write(v_string + ">\n");
    						closed = true;
    					}
    					w.write(format("data", "key", key, value.toString()) + "\n");
    				}
				}
			}
			if (!closed)
				w.write(v_string + "/>\n"); // no contents; close the node with "/>"
			else
			    w.write("</node>\n");
		}
	}

	protected void writeEdgeData(Hypergraph<V,E> g, Writer w) throws IOException
	{
		for (E e: g.getEdges())
		{
			Collection<V> vertices = g.getIncidentVertices(e);
			String id = edge_ids.transform(e);
			String e_string;
			boolean is_hyperedge = !(g instanceof Graph);
            if (is_hyperedge)
            {
                e_string = "<hyperedge ";
                // add ID if present
                if (id != null)
                    e_string += "id=\"" + id + "\" ";
            }
            else
			{
				Pair<V> endpoints = new Pair<V>(vertices);
				V v1 = endpoints.getFirst();
				V v2 = endpoints.getSecond();
				e_string = "<edge ";
				// add ID if present
				if (id != null)
					e_string += "id=\"" + id + "\" ";
				// add edge type if doesn't match default
				EdgeType edge_type = g.getEdgeType(e);
				if (directed && edge_type == EdgeType.UNDIRECTED)
					e_string += "directed=\"false\" ";
				if (!directed && edge_type == EdgeType.DIRECTED)
					e_string += "directed=\"true\" ";
				e_string += "source=\"" + vertex_ids.transform(v1) + 
					"\" target=\"" + vertex_ids.transform(v2) + "\"";
			}
			
			boolean closed = false;
			// write description out if any
			String desc = edge_desc.transform(e);
			if (desc != null)
			{
				w.write(e_string + ">\n");
				closed = true;
				w.write("<desc>" + desc + "</desc>\n");
			}
			// write data out if any
			for (String key : edge_data.keySet())
			{
				Transformer<E, ?> t = edge_data.get(key).transformer;
				Object value = t.transform(e);
				if (value != null)
				{
					if (!closed)
					{
						w.write(e_string + ">\n");
						closed = true;
					}
					w.write(format("data", "key", key, value.toString()) + "\n");
				}
			}
			// if this is a hyperedge, write endpoints out if any
			if (is_hyperedge)
			{
				for (V v : vertices)
				{
					if (!closed)
					{
						w.write(e_string + ">\n");
						closed = true;
					}
					w.write("<endpoint node=\"" + vertex_ids.transform(v) + "\"/>\n");
				}
			}
			
			if (!closed)
				w.write(e_string + "/>\n"); // no contents; close the edge with "/>"
			else
			    if (is_hyperedge)
			        w.write("</hyperedge>\n");
			    else
			        w.write("</edge>\n");
		}
	}

	protected void writeKeySpecification(String key, String type, 
			GraphMLMetadata<?> ds, BufferedWriter bw) throws IOException
	{
		bw.write("<key id=\"" + key + "\" for=\"" + type + "\"");
		boolean closed = false;
		// write out description if any
		String desc = ds.description;
		if (desc != null)
		{
			if (!closed)
			{
				bw.write(">\n");
				closed = true;
			}
			bw.write("<desc>" + desc + "</desc>\n");
		}
		// write out default if any
		Object def = ds.default_value;
		if (def != null)
		{
			if (!closed)
			{
				bw.write(">\n");
				closed = true;
			}
			bw.write("<default>" + def.toString() + "</default>\n");
		}
		if (!closed)
		    bw.write("/>\n");
		else
		    bw.write("</key>\n");
	}
	
	protected String format(String type, String attr, String value, String contents)
	{
		return String.format("<%s %s=\"%s\">%s</%s>", 
				type, attr, value, contents, type);
	}
	
	/**
	 * Provides an ID that will be used to identify a vertex in the output file.
	 * If the vertex IDs are not set, the ID for each vertex will default to
	 * the output of <code>toString</code> 
	 * (and thus not guaranteed to be unique).
	 * 
	 * @param vertex_ids
	 */
	public void setVertexIDs(Transformer<V, String> vertex_ids) 
	{
		this.vertex_ids = vertex_ids;
	}


	/**
	 * Provides an ID that will be used to identify an edge in the output file.
	 * If any edge ID is missing, no ID will be written out for the
	 * corresponding edge.
	 * 
	 * @param edge_ids
	 */
	public void setEdgeIDs(Transformer<E, String> edge_ids) 
	{
		this.edge_ids = edge_ids;
	}

	/**
	 * Provides a map from data type name to graph data.
	 */
	public void setGraphData(Map<String, GraphMLMetadata<Hypergraph<V,E>>> graph_map)
	{
		graph_data = graph_map;
	}
	
    /**
     * Provides a map from data type name to vertex data.
     */
	public void setVertexData(Map<String, GraphMLMetadata<V>> vertex_map)
	{
		vertex_data = vertex_map;
	}
	
    /**
     * Provides a map from data type name to edge data.
     */
	public void setEdgeData(Map<String, GraphMLMetadata<E>> edge_map)
	{
		edge_data = edge_map;
	}
	
	/**
	 * Adds a new graph data specification.
	 */
	public void addGraphData(String id, String description, String default_value,
			Transformer<Hypergraph<V,E>, String> graph_transformer)
	{
		if (graph_data.equals(Collections.EMPTY_MAP))
			graph_data = new HashMap<String, GraphMLMetadata<Hypergraph<V,E>>>();
		graph_data.put(id, new GraphMLMetadata<Hypergraph<V,E>>(description, 
				default_value, graph_transformer));
	}
	
    /**
     * Adds a new vertex data specification.
     */
	public void addVertexData(String id, String description, String default_value,
			Transformer<V, String> vertex_transformer)
	{
		if (vertex_data.equals(Collections.EMPTY_MAP))
			vertex_data = new HashMap<String, GraphMLMetadata<V>>();
		vertex_data.put(id, new GraphMLMetadata<V>(description, default_value, 
				vertex_transformer));
	}

    /**
     * Adds a new edge data specification.
     */
	public void addEdgeData(String id, String description, String default_value,
			Transformer<E, String> edge_transformer)
	{
		if (edge_data.equals(Collections.EMPTY_MAP))
			edge_data = new HashMap<String, GraphMLMetadata<E>>();
		edge_data.put(id, new GraphMLMetadata<E>(description, default_value, 
				edge_transformer));
	}

	/**
	 * Provides vertex descriptions.
	 */
	public void setVertexDescriptions(Transformer<V, String> vertex_desc) 
	{
		this.vertex_desc = vertex_desc;
	}

    /**
     * Provides edge descriptions.
     */
	public void setEdgeDescriptions(Transformer<E, String> edge_desc) 
	{
		this.edge_desc = edge_desc;
	}

    /**
     * Provides graph descriptions.
     */
	public void setGraphDescriptions(Transformer<Hypergraph<V,E>, String> graph_desc) 
	{
		this.graph_desc = graph_desc;
	}
}
