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
import edu.uci.ics.jung.io.graphml.NodeMetadata;

public class TestNodeElementParser extends AbstractParserTest {

    @Test(expected= GraphIOException.class)
    public void testNoId() throws Exception {
        
        String xml = 
            "<node/>";
        
        readObject(xml);
    }

    @Test
    public void testId() throws Exception {
        
        String xml = 
            "<node id=\"1\"/>";
        
        NodeMetadata node = (NodeMetadata) readObject(xml);
        Assert.assertNotNull(node);
        Assert.assertEquals("1", node.getId());
        Assert.assertEquals(null, node.getDescription());
        Assert.assertEquals(0, node.getPorts().size());
    }

    @Test
    public void testDesc() throws Exception {
        
        String xml = 
            "<node id=\"1\">" +
                "<desc>this is my node</desc>" +
            "</node>";
        
        NodeMetadata node = (NodeMetadata) readObject(xml);
        Assert.assertNotNull(node);
        Assert.assertEquals("1", node.getId());
        Assert.assertEquals("this is my node", node.getDescription());
        Assert.assertEquals(0, node.getPorts().size());
    }

    @Test
    public void testPort() throws Exception {
                    
        String xml = 
            "<node id=\"1\">" +
                "<desc>this is my node</desc>" +
                "<port name=\"p1\">" +
                    "<desc>port 1</desc>" +
                "</port>" +
            "</node>";
        
        NodeMetadata node = (NodeMetadata) readObject(xml);
        Assert.assertNotNull(node);
        Assert.assertEquals("1", node.getId());
        Assert.assertEquals("this is my node", node.getDescription());
        Assert.assertEquals(1, node.getPorts().size());
        Assert.assertEquals("p1", node.getPorts().get(0).getName());
    }

    @Test
    public void testMultiPort() throws Exception {
        
        String xml = 
            "<node id=\"1\">" +
                "<desc>this is my node</desc>" +
                "<port name=\"p1\"/>" +
                "<port name=\"p2\"/>" +
                "<port name=\"p3\"/>" +
                "<port name=\"p4\"/>" +
            "</node>";
        
        NodeMetadata node = (NodeMetadata) readObject(xml);
        Assert.assertNotNull(node);
        Assert.assertEquals("1", node.getId());
        Assert.assertEquals("this is my node", node.getDescription());
        Assert.assertEquals(4, node.getPorts().size());
        Assert.assertEquals("p1", node.getPorts().get(0).getName());
        Assert.assertEquals("p2", node.getPorts().get(1).getName());
        Assert.assertEquals("p3", node.getPorts().get(2).getName());
        Assert.assertEquals("p4", node.getPorts().get(3).getName());
    }

    @Test
    public void testUserAttributes() throws Exception {
        
        String xml = 
            "<node id=\"1\" bob=\"abc123\"/>";
        
        NodeMetadata node = (NodeMetadata) readObject(xml);
        Assert.assertNotNull(node);
        Assert.assertEquals("1", node.getId());
        Assert.assertEquals(1, node.getProperties().size());
        Assert.assertEquals("abc123", node.getProperty("bob"));
        Assert.assertEquals(0, node.getPorts().size());
    }

    @Test
    public void testData() throws Exception {
        
        String xml = 
            "<node id=\"1\">" +
                "<data key=\"d1\">value1</data>" +
                "<data key=\"d2\">value2</data>" +
            "</node>";
        
        NodeMetadata node = (NodeMetadata) readObject(xml);
        Assert.assertNotNull(node);
        Assert.assertEquals("1", node.getId());
        Assert.assertEquals(2, node.getProperties().size());
        Assert.assertEquals("value1", node.getProperty("d1"));
        Assert.assertEquals("value2", node.getProperty("d2"));
        Assert.assertEquals(0, node.getPorts().size());
    }
}
