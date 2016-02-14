/*
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */

package edu.uci.ics.jung.io.graphml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Function;

import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.SetHypergraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.io.GraphIOException;

public class TestGraphMLReader2 {
    static final String graphMLDocStart = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
            + "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">";

    private GraphMLReader2<Hypergraph<DummyVertex, DummyEdge>, DummyVertex, DummyEdge> reader;

    @After
    public void tearDown() throws Exception {
        if (reader != null) {
            reader.close();
        }
        reader = null;
    }


    @Test(expected = GraphIOException.class)
    public void testEmptyFile() throws Exception {

        String xml = "";
        readGraph(xml, new DummyGraphObjectBase.UndirectedSparseGraphFactory(),
                new DummyVertex.Factory(), new DummyEdge.EdgeFactory(), new DummyEdge.HyperEdgeFactory());
    }

    @Test
    public void testBasics() throws Exception {

        String xml = graphMLDocStart
                + "<key id=\"d0\" for=\"node\" attr.name=\"color\" attr.type=\"string\">"
                + "<default>yellow</default>"
                + "</key>"
                + "<key id=\"d1\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\"/>"
                + "<graph id=\"G\" edgedefault=\"undirected\">"
                + "<node id=\"n0\">" + "<data key=\"d0\">green</data>"
                + "</node>" + "<node id=\"n1\"/>" + "<node id=\"n2\">"
                + "<data key=\"d0\">blue</data>" + "</node>"
                + "<edge id=\"e0\" source=\"n0\" target=\"n2\">"
                + "<data key=\"d1\">1.0</data>" + "</edge>" + "</graph>" + "</graphml>";

        // Read the graph object.
        Hypergraph<DummyVertex, DummyEdge> graph = readGraph(xml, new DummyGraphObjectBase.UndirectedSparseGraphFactory(),
                new DummyVertex.Factory(), new DummyEdge.EdgeFactory(), new DummyEdge.HyperEdgeFactory());

        // Check out the graph.
        Assert.assertNotNull(graph);
        Assert.assertEquals(3, graph.getVertexCount());
        Assert.assertEquals(1, graph.getEdgeCount());
        Assert.assertEquals(0, graph.getEdgeCount(EdgeType.DIRECTED));
        Assert.assertEquals(1, graph.getEdgeCount(EdgeType.UNDIRECTED));

        // Check out metadata.
        Assert.assertEquals(1, reader.getGraphMLDocument().getGraphMetadata().size());
        List<EdgeMetadata> edges = new ArrayList<EdgeMetadata>(reader.getGraphMLDocument().getGraphMetadata().get(0).getEdgeMap().values());
        Assert.assertEquals(1, edges.size());
        Assert.assertEquals("n0", edges.get(0).getSource());
        Assert.assertEquals("n2", edges.get(0).getTarget());
    }

    @Test
    public void testData() throws Exception {

        String xml =
                graphMLDocStart +
                        "<key id=\"d0\" for=\"node\" attr.name=\"color\" attr.type=\"string\">" +
                        "<default>yellow</default>" +
                        "</key>" +
                        "<key id=\"d1\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\"/>" +
                        "<graph id=\"G\" edgedefault=\"undirected\">" +
                        "<node id=\"n0\">" +
                        "<data key=\"d0\">green</data>" +
                        "</node>" +
                        "<node id=\"n1\"/>" +
                        "<node id=\"n2\">" +
                        "<data key=\"d0\">blue</data>" +
                        "</node>" +
                        "<edge id=\"e0\" source=\"n0\" target=\"n2\">" +
                        "<data key=\"d1\">1.0</data>" +
                        "</edge>" +
                        "</graph>" +
                        "</graphml>";

        // Read the graph object.
        readGraph(xml, new DummyGraphObjectBase.UndirectedSparseGraphFactory(),
                new DummyVertex.Factory(), new DummyEdge.EdgeFactory(), new DummyEdge.HyperEdgeFactory());

        // Check out metadata.
        Assert.assertEquals(1, reader.getGraphMLDocument().getGraphMetadata().size());
        List<EdgeMetadata> edges = new ArrayList<EdgeMetadata>(reader.getGraphMLDocument().getGraphMetadata().get(0).getEdgeMap().values());
        List<NodeMetadata> nodes = new ArrayList<NodeMetadata>(reader.getGraphMLDocument().getGraphMetadata().get(0).getNodeMap().values());
        Collections.sort(nodes, new Comparator<NodeMetadata>() {
            public int compare(NodeMetadata o1, NodeMetadata o2) {
                return o1.getId().compareTo(o2.getId());
            }            
        });
        Assert.assertEquals(1, edges.size());
        Assert.assertEquals("1.0", edges.get(0).getProperties().get("d1"));
        Assert.assertEquals(3, nodes.size());
        Assert.assertEquals("green", nodes.get(0).getProperties().get("d0"));
        Assert.assertEquals("yellow", nodes.get(1).getProperties().get("d0"));
        Assert.assertEquals("blue", nodes.get(2).getProperties().get("d0"));
    }

    @Test(expected = GraphIOException.class)
    public void testEdgeWithInvalidNode() throws Exception {

        String xml = graphMLDocStart
                + "<key id=\"d0\" for=\"node\" attr.name=\"color\" attr.type=\"string\">"
                + "<default>yellow</default>"
                + "</key>"
                + "<key id=\"d1\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\"/>"
                + "<graph id=\"G\" edgedefault=\"undirected\">"
                + "<node id=\"n0\">" + "<data key=\"d0\">green</data>"
                + "</node>" + "<node id=\"n1\"/>" + "<node id=\"n2\">"
                + "<data key=\"d0\">blue</data>" + "</node>"
                + "<edge id=\"e0\" source=\"n0\" target=\"n3\">" + // Invalid
                // node: n3
                "<data key=\"d1\">1.0</data>" + "</edge>" + "</graphml>";

        readGraph(xml, new DummyGraphObjectBase.UndirectedSparseGraphFactory(), new DummyVertex.Factory(),
                new DummyEdge.EdgeFactory(), new DummyEdge.HyperEdgeFactory());
    }

    @Test
    public void testHypergraph() throws Exception {

        String xml = graphMLDocStart
                + "<key id=\"d0\" for=\"node\" attr.name=\"color\" attr.type=\"string\">"
                + "<default>yellow</default>"
                + "</key>"
                + "<key id=\"d1\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\"/>"
                + "<graph id=\"G\" edgedefault=\"undirected\">"
                + "<node id=\"n0\">" + "<data key=\"d0\">green</data>"
                + "</node>" + "<node id=\"n1\"/>" + "<node id=\"n2\">"
                + "<data key=\"d0\">blue</data>" + "</node>"
                + "<hyperedge id=\"e0\">"
                + "<endpoint node=\"n0\"/>" + "<endpoint node=\"n1\"/>"
                + "<endpoint node=\"n2\"/>" + "</hyperedge>" + "</graph>" + "</graphml>";

        // Read the graph object.
        Hypergraph<DummyVertex, DummyEdge> graph = readGraph(xml, new DummyGraphObjectBase.SetHypergraphFactory(),
                new DummyVertex.Factory(), new DummyEdge.EdgeFactory(), new DummyEdge.HyperEdgeFactory());

        // Check out the graph.
        Assert.assertNotNull(graph);
        Assert.assertEquals(3, graph.getVertexCount());
        Assert.assertEquals(1, graph.getEdgeCount());
        Assert.assertEquals(0, graph.getEdgeCount(EdgeType.DIRECTED));
        Assert.assertEquals(1, graph.getEdgeCount(EdgeType.UNDIRECTED));

        // Check out metadata.
        Assert.assertEquals(1, reader.getGraphMLDocument().getGraphMetadata().size());
        List<HyperEdgeMetadata> edges = new ArrayList<HyperEdgeMetadata>(reader.getGraphMLDocument().getGraphMetadata().get(0).getHyperEdgeMap().values());
        Assert.assertEquals(1, edges.size());
        Assert.assertEquals(3, edges.get(0).getEndpoints().size());
        Assert.assertEquals("n0", edges.get(0).getEndpoints().get(0).getNode());
        Assert.assertEquals("n1", edges.get(0).getEndpoints().get(1).getNode());
        Assert.assertEquals("n2", edges.get(0).getEndpoints().get(2).getNode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidGraphFactory() throws Exception {

        // Need a hypergraph
        String xml = graphMLDocStart
                + "<key id=\"d0\" for=\"node\" attr.name=\"color\" attr.type=\"string\">"
                + "<default>yellow</default>"
                + "</key>"
                + "<key id=\"d1\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\"/>"
                + "<graph id=\"G\" edgedefault=\"undirected\">"
                + "<node id=\"n0\">" + "<data key=\"d0\">green</data>"
                + "</node>" + "<node id=\"n1\"/>" + "<node id=\"n2\">"
                + "<data key=\"d0\">blue</data>" + "</node>"
                + "<hyperedge id=\"e0\">"
                + "<endpoint node=\"n0\"/>" + "<endpoint node=\"n1\"/>"
                + "<endpoint node=\"n2\"/>" + "</hyperedge>" + "</graphml>";

	// This will attempt to add an edge with an invalid number of incident vertices (3)
	// for an UndirectedGraph, which should trigger an IllegalArgumentException.
        readGraph(xml, new DummyGraphObjectBase.UndirectedSparseGraphFactory(),
                new DummyVertex.Factory(), new DummyEdge.EdgeFactory(), new DummyEdge.HyperEdgeFactory());
    }

    @Test
    public void testAttributesFile() throws Exception {

        // Read the graph object.
        Hypergraph<DummyVertex, DummyEdge> graph = readGraphFromFile("attributes.graphml", new DummyGraphObjectBase.UndirectedSparseGraphFactory(),
                new DummyVertex.Factory(), new DummyEdge.EdgeFactory(), new DummyEdge.HyperEdgeFactory());

        Assert.assertEquals(6, graph.getVertexCount());
        Assert.assertEquals(7, graph.getEdgeCount());

        Assert.assertEquals(1, reader.getGraphMLDocument().getGraphMetadata().size());

        // Test node ids
        int id = 0;        
        List<NodeMetadata> nodes = new ArrayList<NodeMetadata>(reader.getGraphMLDocument().getGraphMetadata().get(0).getNodeMap().values());
        Collections.sort(nodes, new Comparator<NodeMetadata>() {
            public int compare(NodeMetadata o1, NodeMetadata o2) {
                return o1.getId().compareTo(o2.getId());
            }            
        });
        Assert.assertEquals(6, nodes.size());
        for (NodeMetadata md : nodes) {
            Assert.assertEquals('n', md.getId().charAt(0));
            Assert.assertEquals(id++, Integer.parseInt(md.getId().substring(1)));
        }

        // Test edge ids
        id = 0;
        List<EdgeMetadata> edges = new ArrayList<EdgeMetadata>(reader.getGraphMLDocument().getGraphMetadata().get(0).getEdgeMap().values());
        Collections.sort(edges, new Comparator<EdgeMetadata>() {
            public int compare(EdgeMetadata o1, EdgeMetadata o2) {
                return o1.getId().compareTo(o2.getId());
            }            
        });
        Assert.assertEquals(7, edges.size());
        for (EdgeMetadata md : edges) {
            Assert.assertEquals('e', md.getId().charAt(0));
            Assert.assertEquals(id++, Integer.parseInt(md.getId().substring(1)));
        }

        Assert.assertEquals("green", nodes.get(0).getProperties().get("d0"));
        Assert.assertEquals("yellow", nodes.get(1).getProperties().get("d0"));
        Assert.assertEquals("blue", nodes.get(2).getProperties().get("d0"));
        Assert.assertEquals("red", nodes.get(3).getProperties().get("d0"));
        Assert.assertEquals("yellow", nodes.get(4).getProperties().get("d0"));
        Assert.assertEquals("turquoise", nodes.get(5).getProperties().get("d0"));

        Assert.assertEquals("1.0", edges.get(0).getProperties().get("d1"));
        Assert.assertEquals("1.0", edges.get(1).getProperties().get("d1"));
        Assert.assertEquals("2.0", edges.get(2).getProperties().get("d1"));
        Assert.assertEquals(null, edges.get(3).getProperties().get("d1"));
        Assert.assertEquals(null, edges.get(4).getProperties().get("d1"));
        Assert.assertEquals(null, edges.get(5).getProperties().get("d1"));
        Assert.assertEquals("1.1", edges.get(6).getProperties().get("d1"));
    }

    @Test
    public void testHypergraphFile() throws Exception {

        Function<GraphMetadata, Hypergraph<Number, Number>> graphFactory = new Function<GraphMetadata, Hypergraph<Number, Number>>() {
            public Hypergraph<Number, Number> apply(GraphMetadata md) {
                return new SetHypergraph<Number, Number>();
            }
        };

        Function<NodeMetadata, Number> vertexFactory = new Function<NodeMetadata, Number>() {
            int n = 0;

            public Number apply(NodeMetadata md) {
                return n++;
            }
        };

        Function<EdgeMetadata, Number> edgeFactory = new Function<EdgeMetadata, Number>() {
            int n = 100;

            public Number apply(EdgeMetadata md) {
                return n++;
            }
        };

        Function<HyperEdgeMetadata, Number> hyperEdgeFactory = new Function<HyperEdgeMetadata, Number>() {
            int n = 0;

            public Number apply(HyperEdgeMetadata md) {
                return n++;
            }
        };

        // Read the graph object.        
        Reader fileReader = new InputStreamReader(getClass().getResourceAsStream("hyper.graphml"));
        GraphMLReader2<Hypergraph<Number, Number>, Number, Number> hyperreader =
                new GraphMLReader2<Hypergraph<Number, Number>, Number, Number>(fileReader,
                        graphFactory, vertexFactory, edgeFactory, hyperEdgeFactory);

        // Read the graph.
        Hypergraph<Number, Number> graph = hyperreader.readGraph();

        Assert.assertEquals(graph.getVertexCount(), 7);
        Assert.assertEquals(graph.getEdgeCount(), 4);

        // n0
        Set<Number> incident = new HashSet<Number>();
        incident.add(0);
        incident.add(100);
        Assert.assertEquals(incident, graph.getIncidentEdges(0));

        // n1
        incident.clear();
        incident.add(0);
        incident.add(2);
        Assert.assertEquals(incident, graph.getIncidentEdges(1));

        // n2
        incident.clear();
        incident.add(0);
        Assert.assertEquals(incident, graph.getIncidentEdges(2));

        // n3
        incident.clear();
        incident.add(1);
        incident.add(2);
        Assert.assertEquals(incident, graph.getIncidentEdges(3));

        // n4
        incident.clear();
        incident.add(1);
        incident.add(100);
        Assert.assertEquals(incident, graph.getIncidentEdges(4));

        // n5
        incident.clear();
        incident.add(1);
        Assert.assertEquals(incident, graph.getIncidentEdges(5));

        // n6
        incident.clear();
        incident.add(1);
        Assert.assertEquals(incident, graph.getIncidentEdges(6));
    }

    /*@Test
    public void testReader1Perf() throws Exception {
        String fileName = "attributes.graphml";                
        
        long totalTime = 0;
        int numTrials = 1000;

        for( int ix=0; ix<numTrials; ++ix ) {
            Reader fileReader = new InputStreamReader(getClass().getResourceAsStream(fileName));
                        
            GraphMLReader<Hypergraph<DummyVertex, DummyEdge>, DummyVertex, DummyEdge> reader = new GraphMLReader<Hypergraph<DummyVertex, DummyEdge>, DummyVertex, DummyEdge>(new Factory<DummyVertex>() {

                public DummyVertex create() {
                    return new DummyVertex();
                }
                
            }, new Factory<DummyEdge>() {
                public DummyEdge create() {
                    return new DummyEdge();
                }
            });   
            
            Thread.sleep(10);
            
            long start = System.currentTimeMillis();
            Hypergraph<DummyVertex, DummyEdge> graph = new UndirectedSparseGraph<DummyVertex, DummyEdge>();
            reader.load(fileReader, graph);
            long duration = System.currentTimeMillis() - start;
            totalTime += duration;
        }
        
        double avgTime = ((double)totalTime / (double)numTrials) / 1000.0; 
        
        System.out.printf("Reader1: totalTime=%6d, numTrials=%6d, avgTime=%2.6f seconds", totalTime, numTrials, avgTime);
        System.out.println();
    }

    @Test
    public void testReader2Perf() throws Exception {
        String fileName = "attributes.graphml";                
        
        long totalTime = 0;
        int numTrials = 1000;

        // Test reader2
        for( int ix=0; ix<numTrials; ++ix ) {
            Reader fileReader = new InputStreamReader(getClass().getResourceAsStream(fileName));       
            reader = new GraphMLReader2<Hypergraph<DummyVertex, DummyEdge>, DummyVertex, DummyEdge>(
                    fileReader, new DummyGraphObjectBase.UndirectedSparseGraphFactory(),
                    new DummyVertex.Factory(), new DummyEdge.EdgeFactory(), new DummyEdge.HyperEdgeFactory());
            reader.init();
            
            Thread.sleep(10);
            
            long start = System.currentTimeMillis();
            reader.readGraph();
            long duration = System.currentTimeMillis() - start;
            totalTime += duration;
            
            reader.close();
        }
        
        double avgTime = ((double)totalTime / (double)numTrials) / 1000.0; 
        
        System.out.printf("Reader2: totalTime=%6d, numTrials=%6d, avgTime=%2.6f seconds", totalTime, numTrials, avgTime);
        System.out.println();
    }*/

    private Hypergraph<DummyVertex, DummyEdge> readGraph(String xml, Function<GraphMetadata, Hypergraph<DummyVertex, DummyEdge>> gf,
                                                 DummyVertex.Factory nf, DummyEdge.EdgeFactory ef, DummyEdge.HyperEdgeFactory hef)
            throws GraphIOException {
        Reader fileReader = new StringReader(xml);
        reader = new GraphMLReader2<Hypergraph<DummyVertex, DummyEdge>, DummyVertex, DummyEdge>(
                fileReader, gf, nf, ef, hef);

        return reader.readGraph();
    }

    private Hypergraph<DummyVertex, DummyEdge> readGraphFromFile(String file, Function<GraphMetadata, Hypergraph<DummyVertex, DummyEdge>> gf,
            DummyVertex.Factory nf, DummyEdge.EdgeFactory ef, DummyEdge.HyperEdgeFactory hef)
            throws Exception {
        InputStream is = getClass().getResourceAsStream(file);
        Reader fileReader = new InputStreamReader(is);
        reader = new GraphMLReader2<Hypergraph<DummyVertex, DummyEdge>, DummyVertex, DummyEdge>(
                fileReader, gf, nf, ef, hef);

        return reader.readGraph();
    }

}
