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

import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.io.graphml.EdgeMetadata;

public class TestEdgeElementParser extends AbstractParserTest {

    @Test(expected= GraphIOException.class)
    public void testNoSource() throws Exception {
        
        String xml = 
            "<edge target=\"2\"/>";
        
        readObject(xml);
    }

    @Test(expected= GraphIOException.class)
    public void testNoTarget() throws Exception {
        
        String xml = 
            "<edge source=\"2\"/>";
        
        readObject(xml);
    }

    @Test
    public void testId() throws Exception {
        
        String xml = 
            "<edge source=\"1\" target=\"2\" id=\"e1\"/>";
        
        EdgeMetadata edge = (EdgeMetadata) readObject(xml);
        Assert.assertNotNull(edge);
        Assert.assertEquals("e1", edge.getId());
        Assert.assertEquals(null, edge.getDescription());
        Assert.assertEquals("1", edge.getSource());
        Assert.assertEquals("2", edge.getTarget());
        Assert.assertEquals(null, edge.isDirected());
        Assert.assertEquals(null, edge.getSourcePort());
        Assert.assertEquals(null, edge.getTargetPort());
    }

    @Test
    public void testDirectedTrue() throws Exception {
        
        String xml = 
            "<edge source=\"1\" target=\"2\" directed=\"true\"/>";
        
        EdgeMetadata edge = (EdgeMetadata) readObject(xml);
        Assert.assertNotNull(edge);
        Assert.assertEquals(null, edge.getId());
        Assert.assertEquals(null, edge.getDescription());
        Assert.assertEquals("1", edge.getSource());
        Assert.assertEquals("2", edge.getTarget());
        Assert.assertEquals(true, edge.isDirected());
        Assert.assertEquals(null, edge.getSourcePort());
        Assert.assertEquals(null, edge.getTargetPort());
    }

    @Test
    public void testDirectedFalse() throws Exception {
        
        String xml = 
            "<edge source=\"1\" target=\"2\" directed=\"false\"/>";
        
        EdgeMetadata edge = (EdgeMetadata) readObject(xml);
        Assert.assertNotNull(edge);
        Assert.assertEquals(null, edge.getId());
        Assert.assertEquals(null, edge.getDescription());
        Assert.assertEquals("1", edge.getSource());
        Assert.assertEquals("2", edge.getTarget());
        Assert.assertEquals(false, edge.isDirected());
        Assert.assertEquals(null, edge.getSourcePort());
        Assert.assertEquals(null, edge.getTargetPort());
    }

    @Test
    public void testSourceTargetPorts() throws Exception {
        
        String xml = 
            "<edge source=\"1\" target=\"2\" sourceport=\"a\" targetport=\"b\"/>";
        
        EdgeMetadata edge = (EdgeMetadata) readObject(xml);
        Assert.assertNotNull(edge);
        Assert.assertEquals(null, edge.getId());
        Assert.assertEquals(null, edge.getDescription());
        Assert.assertEquals("1", edge.getSource());
        Assert.assertEquals("2", edge.getTarget());
        Assert.assertEquals(null, edge.isDirected());
        Assert.assertEquals("a", edge.getSourcePort());
        Assert.assertEquals("b", edge.getTargetPort());
    }

    @Test
    public void testDesc() throws Exception {
        
        String xml = 
            "<edge source=\"1\" target=\"2\">" +
                "<desc>hello world</desc>" +
            "</edge>";
        
        EdgeMetadata edge = (EdgeMetadata) readObject(xml);
        Assert.assertNotNull(edge);
        Assert.assertEquals(null, edge.getId());
        Assert.assertEquals("hello world", edge.getDescription());
        Assert.assertEquals("1", edge.getSource());
        Assert.assertEquals("2", edge.getTarget());
        Assert.assertEquals(null, edge.isDirected());
        Assert.assertEquals(null, edge.getSourcePort());
        Assert.assertEquals(null, edge.getTargetPort());
    }

    @Test
    public void testUserAttributes() throws Exception {
        
        String xml = 
            "<edge source=\"1\" target=\"2\" bob=\"abc123\">" +         
            "</edge>";
        
        EdgeMetadata edge = (EdgeMetadata) readObject(xml);
        Assert.assertNotNull(edge);
        Assert.assertEquals(null, edge.getId());
        Assert.assertEquals(null, edge.getDescription());
        Assert.assertEquals("1", edge.getSource());
        Assert.assertEquals("2", edge.getTarget());
        Assert.assertEquals(null, edge.isDirected());
        Assert.assertEquals(null, edge.getSourcePort());
        Assert.assertEquals(null, edge.getTargetPort());
        Assert.assertEquals(1, edge.getProperties().size());
        Assert.assertEquals("abc123", edge.getProperty("bob"));
    }

    @Test
    public void testData() throws Exception {
        
        String xml = 
            "<edge source=\"1\" target=\"2\">" +
                "<data key=\"d1\">value1</data>" +
                "<data key=\"d2\">value2</data>" +
            "</edge>";
        
        EdgeMetadata edge = (EdgeMetadata) readObject(xml);
        Assert.assertNotNull(edge);
        Assert.assertEquals(null, edge.getId());
        Assert.assertEquals(null, edge.getDescription());
        Assert.assertEquals("1", edge.getSource());
        Assert.assertEquals("2", edge.getTarget());
        Assert.assertEquals(null, edge.isDirected());
        Assert.assertEquals(null, edge.getSourcePort());
        Assert.assertEquals(null, edge.getTargetPort());
        Assert.assertEquals(2, edge.getProperties().size());
        Assert.assertEquals("value1", edge.getProperty("d1"));
        Assert.assertEquals("value2", edge.getProperty("d2"));
    }
}
