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
import edu.uci.ics.jung.io.graphml.Key;

public class TestKeyElementParser extends AbstractParserTest {

    @Test(expected= GraphIOException.class)
    public void testNoId() throws Exception {
        
        String xml = 
            "<key/>";
        
        readObject(xml);
    }

    @Test
    public void testId() throws Exception {
        
        String xml = 
            "<key id=\"d1\"/>";
        
        Key key = (Key) readObject(xml);
        Assert.assertNotNull(key);
        Assert.assertEquals("d1", key.getId());
        Assert.assertEquals(null, key.getDescription());
        Assert.assertEquals(null, key.getDefaultValue());
        Assert.assertEquals(null, key.getAttributeName());
        Assert.assertEquals(null, key.getAttributeType());
        Assert.assertEquals(Key.ForType.ALL, key.getForType());
    }

    @Test
    public void testDesc() throws Exception {
        
        String xml = 
            "<key id=\"d1\">" +
                "<desc>this is my key</desc>" +
            "</key>";
        
        Key key = (Key) readObject(xml);
        Assert.assertNotNull(key);
        Assert.assertEquals("d1", key.getId());
        Assert.assertEquals("this is my key", key.getDescription());
        Assert.assertEquals(null, key.getDefaultValue());
        Assert.assertEquals(null, key.getAttributeName());
        Assert.assertEquals(null, key.getAttributeType());
        Assert.assertEquals(Key.ForType.ALL, key.getForType());
    }

    @Test
    public void testDefault() throws Exception {
        
        String xml = 
            "<key id=\"d1\">" +
                "<default>yellow</default>" +
            "</key>";
        
        Key key = (Key) readObject(xml);
        Assert.assertNotNull(key);
        Assert.assertEquals("d1", key.getId());
        Assert.assertEquals(null, key.getDescription());
        Assert.assertEquals("yellow", key.getDefaultValue());
        Assert.assertEquals(null, key.getAttributeName());
        Assert.assertEquals(null, key.getAttributeType());
        Assert.assertEquals(Key.ForType.ALL, key.getForType());
    }

    @Test
    public void testAttrNameType() throws Exception {
        
        String xml = 
            "<key id=\"d1\" attr.name=\"myattr\" attr.type=\"double\">" +
            "</key>";
        
        Key key = (Key) readObject(xml);
        Assert.assertNotNull(key);
        Assert.assertEquals("d1", key.getId());
        Assert.assertEquals(null, key.getDescription());
        Assert.assertEquals(null, key.getDefaultValue());
        Assert.assertEquals("myattr", key.getAttributeName());
        Assert.assertEquals("double", key.getAttributeType());
        Assert.assertEquals(Key.ForType.ALL, key.getForType());
    }

    @Test
    public void testForNode() throws Exception {
        
        String xml = 
            "<key id=\"d1\" for=\"node\">" +
            "</key>";
        
        Key key = (Key) readObject(xml);
        Assert.assertNotNull(key);
        Assert.assertEquals("d1", key.getId());
        Assert.assertEquals(null, key.getDescription());
        Assert.assertEquals(null, key.getDefaultValue());
        Assert.assertEquals(null, key.getAttributeName());
        Assert.assertEquals(null, key.getAttributeType());
        Assert.assertEquals(Key.ForType.NODE, key.getForType());
    }

    @Test
    public void testForEdge() throws Exception {
        
        String xml = 
            "<key id=\"d1\" for=\"edge\">" +
            "</key>";
        
        Key key = (Key) readObject(xml);
        Assert.assertNotNull(key);
        Assert.assertEquals("d1", key.getId());
        Assert.assertEquals(null, key.getDescription());
        Assert.assertEquals(null, key.getDefaultValue());
        Assert.assertEquals(null, key.getAttributeName());
        Assert.assertEquals(null, key.getAttributeType());
        Assert.assertEquals(Key.ForType.EDGE, key.getForType());
    }

    @Test
    public void testForGraph() throws Exception {
        
        String xml = 
            "<key id=\"d1\" for=\"graph\">" +
            "</key>";
        
        Key key = (Key) readObject(xml);
        Assert.assertNotNull(key);
        Assert.assertEquals("d1", key.getId());
        Assert.assertEquals(null, key.getDescription());
        Assert.assertEquals(null, key.getDefaultValue());
        Assert.assertEquals(null, key.getAttributeName());
        Assert.assertEquals(null, key.getAttributeType());
        Assert.assertEquals(Key.ForType.GRAPH, key.getForType());
    }

    @Test
    public void testForAll() throws Exception {
        
        String xml = 
            "<key id=\"d1\" for=\"all\">" +
            "</key>";
        
        Key key = (Key) readObject(xml);
        Assert.assertNotNull(key);
        Assert.assertEquals("d1", key.getId());
        Assert.assertEquals(null, key.getDescription());
        Assert.assertEquals(null, key.getDefaultValue());
        Assert.assertEquals(null, key.getAttributeName());
        Assert.assertEquals(null, key.getAttributeType());
        Assert.assertEquals(Key.ForType.ALL, key.getForType());
    }
}
