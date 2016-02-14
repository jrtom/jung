/*
 * Created on Jun 22, 2008
 *
 * Copyright (c) 2008, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.xml.sax.SAXException;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.TestGraphs;

public class TestGraphMLWriter extends TestCase
{
    public void testBasicWrite() throws IOException, ParserConfigurationException, SAXException
    {
        Graph<String, Number> g = TestGraphs.createTestGraph(true);
        GraphMLWriter<String, Number> gmlw = new GraphMLWriter<String, Number>();
        Function<Number, String> edge_weight = new Function<Number, String>() 
		{ 
			public String apply(Number n) 
			{ 
				return String.valueOf(n.intValue()); 
			} 
		};

		Function<String, String> vertex_name = Functions.identity();
			//TransformerUtils.nopTransformer();
		
        gmlw.addEdgeData("weight", "integer value for the edge", 
        		Integer.toString(-1), edge_weight);
        gmlw.addVertexData("name", "identifier for the vertex", null, vertex_name);
        gmlw.setEdgeIDs(edge_weight);
        gmlw.setVertexIDs(vertex_name);
        gmlw.save(g, new FileWriter("src/test/resources/testbasicwrite.graphml"));
        
        // TODO: now read it back in and compare the graph connectivity 
        // and other metadata with what's in TestGraphs.pairs[], etc.
//        Factory<String> vertex_factory = null;
//        Factory<Object> edge_factory = FactoryUtils.instantiateFactory(Object.class);
//        GraphMLReader<Graph<String, Object>, String, Object> gmlr = 
//        	new GraphMLReader<Graph<String, Object>, String, Object>(
//        			vertex_factory, edge_factory);
        GraphMLReader<Graph<String, Object>, String, Object> gmlr = 
            new GraphMLReader<Graph<String, Object>, String, Object>();
        Graph<String, Object> g2 = new DirectedSparseGraph<String, Object>();
        gmlr.load("src/test/resources/testbasicwrite.graphml", g2);
        Map<String, GraphMLMetadata<Object>> edge_metadata = 
        	gmlr.getEdgeMetadata();
        Function<Object, String> edge_weight2 = 
        	edge_metadata.get("weight").transformer;
        validateTopology(g, g2, edge_weight, edge_weight2);
        
        // TODO: delete graph file when done
        File f = new File("src/test/resources/testbasicwrite.graphml");
        f.delete();
    }
    
    public void testMixedGraph() throws IOException, ParserConfigurationException, SAXException
    {
        Graph<String, Number> g = TestGraphs.getSmallGraph();
        GraphMLWriter<String, Number> gmlw = new GraphMLWriter<String, Number>();
        Function<Number, String> edge_weight = new Function<Number, String>() 
        { 
            public String apply(Number n) 
            { 
                return String.valueOf(n.doubleValue()); 
            } 
        };

        gmlw.addEdgeData("weight", "integer value for the edge", 
                Integer.toString(-1), edge_weight);
        gmlw.setEdgeIDs(edge_weight);
        gmlw.save(g, new FileWriter("src/test/resources/testmixedgraph.graphml"));

        // TODO: now read it back in and compare the graph connectivity 
        // and other metadata with what's in TestGraphs, etc.
        GraphMLReader<Graph<String,Object>,String,Object> gmlr = 
            new GraphMLReader<Graph<String,Object>,String,Object>();
        Graph<String,Object> g2 = new SparseMultigraph<String,Object>();
        gmlr.load("src/test/resources/testmixedgraph.graphml", g2);
        Map<String, GraphMLMetadata<Object>> edge_metadata = 
            gmlr.getEdgeMetadata();
        Function<Object, String> edge_weight2 = 
            edge_metadata.get("weight").transformer;
        validateTopology(g, g2, edge_weight, edge_weight2);
        
        // TODO: delete graph file when done
        File f = new File("src/test/resources/testmixedgraph.graphml");
        f.delete();
    }

    public <T extends Comparable<T>> void validateTopology(Graph<T,Number> g, Graph<T,Object> g2,
            Function<Number,String> edge_weight, Function<Object,String> edge_weight2)
    {
        Assert.assertEquals(g2.getEdgeCount(), g.getEdgeCount());
        List<T> g_vertices = new ArrayList<T>(g.getVertices());
        List<T> g2_vertices = new ArrayList<T>(g2.getVertices());
        Collections.sort(g_vertices); 
        Collections.sort(g2_vertices);
        Assert.assertEquals(g_vertices, g2_vertices);

        Set<String> g_edges = new HashSet<String>();
        for (Number n : g.getEdges())
            g_edges.add(String.valueOf(n));
        Set<Object> g2_edges = new HashSet<Object>(g2.getEdges());
        Assert.assertEquals(g_edges, g2_edges);
        
        for (T v : g2.getVertices())
        {
            for (T w : g2.getVertices())
            {
                Assert.assertEquals(g.isNeighbor(v, w), 
                        g2.isNeighbor(v, w));
                Set<String> e = new HashSet<String>();
                for (Number n : g.findEdgeSet(v, w))
                    e.add(String.valueOf(n));
                Set<Object> e2 = new HashSet<Object>(g2.findEdgeSet(v, w));
                Assert.assertEquals(e.size(), e2.size());
                Assert.assertEquals(e, e2);
            }
        }
        
        for (Object o : g2.getEdges())
        {
            String weight = edge_weight.apply(new Double((String)o));
            String weight2 = edge_weight2.apply(o);
            Assert.assertEquals(weight2, weight);
        }        
//                Number n = g.findEdge(v, w);
//                Object o = g2.findEdge(v, w);
//                if (n != null)
//                {
//                    String weight = edge_weight.apply(n);
//                    String weight2 = edge_weight2.apply(o);
//                    Assert.assertEquals(weight2, weight);
//                }
//            }
//        }
        
    }
    
}
