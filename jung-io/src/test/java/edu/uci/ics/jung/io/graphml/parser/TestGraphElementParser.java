/*
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */

package edu.uci.ics.jung.io.graphml.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.io.graphml.EdgeMetadata;
import edu.uci.ics.jung.io.graphml.GraphMetadata;
import edu.uci.ics.jung.io.graphml.NodeMetadata;
import edu.uci.ics.jung.io.graphml.GraphMetadata.EdgeDefault;

public class TestGraphElementParser extends AbstractParserTest {

    @Test(expected= GraphIOException.class)
    public void testNoEdgeDefault() throws Exception {
        
        String xml = 
            "<graph/>";
        
        readObject(xml);
    }

    @Test
    public void testEdgeDefaultDirected() throws Exception {
        
        String xml = 
            "<graph edgedefault=\"directed\"/>";
        
        GraphMetadata g = (GraphMetadata) readObject(xml);
        Assert.assertNotNull(g);
        Assert.assertEquals(EdgeDefault.DIRECTED, g.getEdgeDefault());
        Assert.assertEquals(null, g.getId());
        Assert.assertEquals(null, g.getDescription());
        Assert.assertEquals(0, g.getNodeMap().size());
        Assert.assertEquals(0, g.getEdgeMap().size());
        Assert.assertEquals(0, g.getHyperEdgeMap().size());
    }

    @Test
    public void testEdgeDefaultUndirected() throws Exception {
        
        String xml = 
            "<graph edgedefault=\"undirected\"/>";
        
        GraphMetadata g = (GraphMetadata) readObject(xml);
        Assert.assertNotNull(g);
        Assert.assertEquals(EdgeDefault.UNDIRECTED, g.getEdgeDefault());
        Assert.assertEquals(null, g.getId());
        Assert.assertEquals(null, g.getDescription());
        Assert.assertEquals(0, g.getNodeMap().size());
        Assert.assertEquals(0, g.getEdgeMap().size());
        Assert.assertEquals(0, g.getHyperEdgeMap().size());
    }

    @Test
    public void testDesc() throws Exception {
        
        String xml = 
            "<graph edgedefault=\"undirected\">" +
                "<desc>hello world</desc>" +
            "</graph>";
        
        GraphMetadata g = (GraphMetadata) readObject(xml);
        Assert.assertNotNull(g);
        Assert.assertEquals(EdgeDefault.UNDIRECTED, g.getEdgeDefault());
        Assert.assertEquals(null, g.getId());
        Assert.assertEquals("hello world", g.getDescription());
        Assert.assertEquals(0, g.getNodeMap().size());
        Assert.assertEquals(0, g.getEdgeMap().size());
        Assert.assertEquals(0, g.getHyperEdgeMap().size());
    }

    @Test
    public void testNodes() throws Exception {
        
        String xml = 
            "<graph edgedefault=\"undirected\">" +
                "<node id=\"1\"/>" +
                "<node id=\"2\"/>" +
                "<node id=\"3\"/>" +
            "</graph>";
        
        GraphMetadata g = (GraphMetadata) readObject(xml);
        Assert.assertNotNull(g);
        Assert.assertEquals(EdgeDefault.UNDIRECTED, g.getEdgeDefault());
        Assert.assertEquals(null, g.getId());
        Assert.assertEquals(null, g.getDescription());
        Assert.assertEquals(3, g.getNodeMap().size());        
        List<NodeMetadata> nodes = new ArrayList<NodeMetadata>(g.getNodeMap().values());
        Collections.sort(nodes, new Comparator<NodeMetadata>() {
            public int compare(NodeMetadata o1, NodeMetadata o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        Assert.assertEquals("1", nodes.get(0).getId());
        Assert.assertEquals("2", nodes.get(1).getId());
        Assert.assertEquals("3", nodes.get(2).getId());
    }

    @Test
    public void testEdges() throws Exception {
        
        String xml = 
            "<graph edgedefault=\"undirected\">" +
                "<node id=\"1\"/>" +
                "<node id=\"2\"/>" +
                "<node id=\"3\"/>" +
                "<edge source=\"1\" target=\"2\"/>" +
                "<edge source=\"2\" target=\"3\"/>" +
            "</graph>";
        
        GraphMetadata g = (GraphMetadata) readObject(xml);
        Assert.assertNotNull(g);
        Assert.assertEquals(EdgeDefault.UNDIRECTED, g.getEdgeDefault());
        Assert.assertEquals(null, g.getId());
        Assert.assertEquals(null, g.getDescription());
        List<EdgeMetadata> edges = new ArrayList<EdgeMetadata>(g.getEdgeMap().values());
        Collections.sort(edges, new Comparator<EdgeMetadata>() {
            public int compare(EdgeMetadata o1, EdgeMetadata o2) {
                return o1.getSource().compareTo(o2.getSource());
            }
        });
        Assert.assertEquals(2, edges.size());
        Assert.assertEquals("1", edges.get(0).getSource());
        Assert.assertEquals("2", edges.get(1).getSource());
    }

    @Test
    public void testHyperEdges() throws Exception {
        
        String xml = 
            "<graph edgedefault=\"undirected\">" +
                "<node id=\"1\"/>" +
                "<node id=\"2\"/>" +
                "<node id=\"3\"/>" +
                "<hyperedge>" +
                    "<endpoint node=\"1\"/>" +
                    "<endpoint node=\"2\"/>" +
                "</hyperedge>" +
                "<hyperedge>" +
                    "<endpoint node=\"2\"/>" +
                    "<endpoint node=\"3\"/>" +
                "</hyperedge>" +
                "<hyperedge>" +
                    "<endpoint node=\"3\"/>" +
                    "<endpoint node=\"1\"/>" +
                "</hyperedge>" +
            "</graph>";
        
        GraphMetadata g = (GraphMetadata) readObject(xml);
        Assert.assertNotNull(g);
        Assert.assertEquals(EdgeDefault.UNDIRECTED, g.getEdgeDefault());
        Assert.assertEquals(null, g.getId());
        Assert.assertEquals(null, g.getDescription());
        Assert.assertEquals(3, g.getHyperEdgeMap().size());
    }

    @Test
    public void testUserAttributes() throws Exception {
        
        String xml = 
            "<graph edgedefault=\"undirected\" bob=\"abc123\">" +
            "</graph>";
        
        GraphMetadata g = (GraphMetadata) readObject(xml);
        Assert.assertNotNull(g);
        Assert.assertEquals(EdgeDefault.UNDIRECTED, g.getEdgeDefault());
        Assert.assertEquals(null, g.getId());
        Assert.assertEquals(null, g.getDescription());
        Assert.assertEquals(1, g.getProperties().size());
        Assert.assertEquals("abc123", g.getProperty("bob"));
    }

    @Test
    public void testData() throws Exception {
        
        String xml = 
            "<graph edgedefault=\"undirected\">" +
                "<data key=\"d1\">value1</data>" +
                "<data key=\"d2\">value2</data>" +
            "</graph>";
        
        GraphMetadata g = (GraphMetadata) readObject(xml);
        Assert.assertNotNull(g);
        Assert.assertEquals(EdgeDefault.UNDIRECTED, g.getEdgeDefault());
        Assert.assertEquals(null, g.getId());
        Assert.assertEquals(null, g.getDescription());
        Assert.assertEquals(2, g.getProperties().size());
        Assert.assertEquals("value1", g.getProperty("d1"));
        Assert.assertEquals("value2", g.getProperty("d2"));
    }
}
