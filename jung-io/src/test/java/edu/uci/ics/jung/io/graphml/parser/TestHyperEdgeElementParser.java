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

import org.junit.Assert;
import org.junit.Test;

import edu.uci.ics.jung.io.graphml.HyperEdgeMetadata;

public class TestHyperEdgeElementParser extends AbstractParserTest {

    @Test
    public void testEmpty() throws Exception {
        
        String xml = 
            "<hyperedge/>";
        
        HyperEdgeMetadata edge = (HyperEdgeMetadata) readObject(xml);
        Assert.assertNotNull(edge);
        Assert.assertEquals(null, edge.getId());
        Assert.assertEquals(null, edge.getDescription());
        Assert.assertEquals(0, edge.getEndpoints().size());
    }

    @Test
    public void testId() throws Exception {
        
        String xml = 
            "<hyperedge id=\"e1\"/>";
        
        HyperEdgeMetadata edge = (HyperEdgeMetadata) readObject(xml);
        Assert.assertNotNull(edge);
        Assert.assertEquals("e1", edge.getId());
        Assert.assertEquals(null, edge.getDescription());
        Assert.assertEquals(0, edge.getEndpoints().size());
    }

    @Test
    public void testDesc() throws Exception {
        
        String xml = 
            "<hyperedge>" +
                "<desc>hello world</desc>" +
            "</hyperedge>";
        
        HyperEdgeMetadata edge = (HyperEdgeMetadata) readObject(xml);
        Assert.assertNotNull(edge);
        Assert.assertEquals(null, edge.getId());
        Assert.assertEquals("hello world", edge.getDescription());
        Assert.assertEquals(0, edge.getEndpoints().size());
    }

    @Test
    public void testEndpoints() throws Exception {
        
        String xml = 
            "<hyperedge>" +
                "<endpoint node=\"1\"/>" +
                "<endpoint node=\"2\"/>" +
                "<endpoint node=\"3\"/>" +
            "</hyperedge>";
        
        HyperEdgeMetadata edge = (HyperEdgeMetadata) readObject(xml);
        Assert.assertNotNull(edge);
        Assert.assertEquals(null, edge.getId());
        Assert.assertEquals(null, edge.getDescription());
        Assert.assertEquals(3, edge.getEndpoints().size());
        Assert.assertEquals("1", edge.getEndpoints().get(0).getNode());
        Assert.assertEquals("2", edge.getEndpoints().get(1).getNode());
        Assert.assertEquals("3", edge.getEndpoints().get(2).getNode());
    }

    @Test
    public void testUserAttributes() throws Exception {
        
        String xml = 
            "<hyperedge bob=\"abc123\"/>";
        
        HyperEdgeMetadata edge = (HyperEdgeMetadata) readObject(xml);
        Assert.assertNotNull(edge);
        Assert.assertEquals(null, edge.getId());
        Assert.assertEquals(null, edge.getDescription());
        Assert.assertEquals(0, edge.getEndpoints().size());
        Assert.assertEquals(1, edge.getProperties().size());
        Assert.assertEquals("abc123", edge.getProperty("bob"));
    }

    @Test
    public void testData() throws Exception {
        
        String xml = 
            "<hyperedge>" +
                "<data key=\"d1\">value1</data>" +
                "<data key=\"d2\">value2</data>" +
            "</hyperedge>";
        
        HyperEdgeMetadata edge = (HyperEdgeMetadata) readObject(xml);
        Assert.assertNotNull(edge);
        Assert.assertEquals(null, edge.getId());
        Assert.assertEquals(null, edge.getDescription());
        Assert.assertEquals(0, edge.getEndpoints().size());
        Assert.assertEquals(2, edge.getProperties().size());
        Assert.assertEquals("value1", edge.getProperty("d1"));
        Assert.assertEquals("value2", edge.getProperty("d2"));
    }
}
